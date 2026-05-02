INSERT INTO items (id, name, price, active, created_at, updated_at)
VALUES ('11111111-1111-1111-1111-111111111111', 'Integration test item', 10.00, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;
