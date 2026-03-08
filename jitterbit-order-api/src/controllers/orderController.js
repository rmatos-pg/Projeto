const orderClient = require('../clients/orderClient');
const idempotency = require('../middleware/idempotency');
const { handleAsync } = require('../utils/httpHandler');

const create = handleAsync(async (req, res) => {
  const body = req.body;
  if (!body?.numeroPedido) {
    res.status(400).json({ error: 'numeroPedido é obrigatório' });
    return;
  }
  const order = await orderClient.createOrder(body);
  if (req.idempotencyKey) idempotency.set(req.idempotencyKey, order);
  res.status(201).json(order);
});

const getById = handleAsync(async (req, res) => {
  const order = await orderClient.getOrder(req.params.orderId);
  if (!order) {
    res.status(404).json({ error: 'Pedido não encontrado' });
    return;
  }
  res.json(order);
});

const list = handleAsync(async (req, res) => {
  const orders = await orderClient.listOrders();
  res.json(orders);
});

const update = handleAsync(async (req, res) => {
  const order = await orderClient.updateOrder(req.params.orderId, req.body);
  if (!order) {
    res.status(404).json({ error: 'Pedido não encontrado' });
    return;
  }
  if (req.idempotencyKey) idempotency.set(req.idempotencyKey, order);
  res.json(order);
});

const remove = handleAsync(async (req, res) => {
  const deleted = await orderClient.deleteOrder(req.params.orderId);
  if (!deleted) {
    res.status(404).json({ error: 'Pedido não encontrado' });
    return;
  }
  res.status(204).send();
});

module.exports = { create, getById, list, update, remove };
