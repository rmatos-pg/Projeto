function mapInputToOrder(input) {
  const orderId = (input.numeroPedido || '').replace(/-01$/, '') || input.numeroPedido;
  return {
    orderId,
    value: Number(input.valorTotal) || 0,
    creationDate: new Date(input.dataCriacao).toISOString(),
    items: (input.items || []).map((item) => ({
      productId: parseInt(item.idItem, 10) || 0,
      quantity: parseInt(item.quantidadeItem, 10) || 0,
      price: Number(item.valorItem) || 0
    }))
  };
}

function mapOrderToOutput(row, items) {
  return {
    orderId: row.orderId,
    value: row.value,
    creationDate: row.creationDate,
    items: (items || []).map((i) => ({
      productId: i.productId,
      quantity: i.quantity,
      price: i.price
    }))
  };
}

module.exports = { mapInputToOrder, mapOrderToOutput };
