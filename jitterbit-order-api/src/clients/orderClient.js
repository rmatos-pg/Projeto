const { getDb } = require('../database/connection');
const Order = require('../models/Order');
const Item = require('../models/Item');
const Outbox = require('../models/Outbox');
const { mapInputToOrder, mapOrderToOutput } = require('../services/orderMapper');
const { logAction } = require('../utils/logAction');
const { get: cacheGet, set: cacheSet, del: cacheDel } = require('../utils/cache');
const { withRetry, withTimeout } = require('../utils/async');
const { loadConfig } = require('../utils/configLoader');

function execute(fn) {
  const config = loadConfig();
  return withRetry(() =>
    withTimeout(Promise.resolve().then(fn), config.operationTimeoutMs)
  );
}

async function createOrderWithOutbox(order, items) {
  const pool = getDb();
  const client = await pool.connect();
  try {
    await client.query('BEGIN');
    await client.query(
      'INSERT INTO orders ("orderId", value, "creationDate") VALUES ($1, $2, $3)',
      [order.orderId, order.value, order.creationDate]
    );
    for (const item of items) {
      await client.query(
        'INSERT INTO items ("orderId", "productId", quantity, price) VALUES ($1, $2, $3, $4)',
        [order.orderId, item.productId, item.quantity, item.price]
      );
    }
    await Outbox.insert(
      client,
      'Order',
      order.orderId,
      'OrderCreated',
      JSON.stringify({
        orderId: order.orderId,
        value: order.value,
        creationDate: order.creationDate,
        items
      })
    );
    await client.query('COMMIT');
  } catch (e) {
    await client.query('ROLLBACK');
    throw e;
  } finally {
    client.release();
  }
}

const orderClient = {
  async createOrder(input) {
    const order = mapInputToOrder(input);
    await execute(() => createOrderWithOutbox(order, order.items));
    logAction('CREATE', `Order ${order.orderId} created`);
    cacheDel(order.orderId);
    return mapOrderToOutput(order, order.items);
  },

  async getOrder(orderId) {
    const cached = cacheGet(orderId);
    if (cached) return cached;

    const row = await execute(() => Order.findByOrderId(orderId));
    if (!row) return null;

    const items = await execute(() => Item.findByOrderId(orderId));
    const result = mapOrderToOutput(row, items);
    cacheSet(orderId, result, 30000);
    return result;
  },

  async listOrders() {
    const rows = await execute(() => Order.findAll());
    const result = [];
    for (const row of rows) {
      const items = await execute(() => Item.findByOrderId(row.orderId));
      result.push(mapOrderToOutput(row, items));
    }
    return result;
  },

  async updateOrder(orderId, input) {
    const order = mapInputToOrder(input);
    const result = await execute(() =>
      Order.update(orderId, order.value, order.creationDate)
    );
    if (result.changes === 0) return null;

    await execute(() => Item.removeByOrderId(orderId));
    await execute(() => Item.createMany(orderId, order.items));
    logAction('UPDATE', `Order ${orderId} updated`);
    cacheDel(orderId);
    return this.getOrder(orderId);
  },

  async deleteOrder(orderId) {
    await execute(() => Item.removeByOrderId(orderId));
    const result = await execute(() => Order.remove(orderId));
    if (result.changes === 0) return false;
    logAction('DELETE', `Order ${orderId} deleted`);
    cacheDel(orderId);
    return true;
  }
};

module.exports = orderClient;
