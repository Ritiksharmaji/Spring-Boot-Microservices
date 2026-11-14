-- Orders created before V3 got payment_status = 'PENDING' by default; derive the real value
-- from the order status and the payment module's record.
UPDATE orders SET payment_status = 'REFUNDED'
WHERE payment_status = 'PENDING' AND status = 'CANCELLED'
  AND id IN (SELECT order_id FROM payments WHERE status = 'REFUNDED');

UPDATE orders SET payment_status = 'FAILED'
WHERE payment_status = 'PENDING' AND status = 'CANCELLED'
  AND id IN (SELECT order_id FROM payments WHERE status = 'FAILED');

UPDATE orders SET payment_status = 'NOT_CHARGED'
WHERE payment_status = 'PENDING' AND status = 'CANCELLED';

UPDATE orders SET payment_status = 'PAID'
WHERE payment_status = 'PENDING' AND status IN ('CONFIRMED', 'SHIPPED', 'DELIVERED');
