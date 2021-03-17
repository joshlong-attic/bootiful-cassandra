create table if not exists orders_by_customer
(
    customer_id   uuid,
    order_id      uuid,
    customer_name text static,
    primary key ((customer_id) , order_id)
) WITH CLUSTERING ORDER BY (order_id DESC);

-- insert into orders_by_customer (customer_id, order_id, customer_name)
-- values (uuid(), uuid(),  );

--
-- INSERT INTO products_by_order(order_id, product_id, product_quantity, product_name, product_price, added_to_order_at)
-- VALUES(uuid(), uuid(), 1, 'iPhone', 699.99, toTimestamp(now()));
-- INSERT INTO products_by_order(order_id, product_id, product_quantity, product_name, product_price, added_to_order_at)
-- VALUES(uuid(), uuid(), 1, 'iPad', 799.99, toTimestamp(now()));
-- INSERT INTO products_by_order(order_id, product_id, product_quantity, product_name, product_price, added_to_order_at)
-- VALUES(uuid(), uuid(), 1, 'Macbook', 2399.3, toTimestamp(now()));
