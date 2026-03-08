const express = require('express');
const orderController = require('../controllers/orderController');

const router = express.Router();

router.post('/order', orderController.create);
router.get('/order/list', orderController.list);
router.get('/order/:orderId', orderController.getById);
router.put('/order/:orderId', orderController.update);
router.delete('/order/:orderId', orderController.remove);

module.exports = router;
