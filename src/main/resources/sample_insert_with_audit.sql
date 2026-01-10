-- Sample INSERT with audit fields
-- The generator will auto-populate created_at and created_by
INSERT INTO customer (
    first_name,
    last_name,
    email,
    address_id,
    active,
    created_at,      -- Audit field: auto-populated with current timestamp
    created_by       -- Audit field: auto-populated with authenticated user ID
) VALUES (?, ?, ?, ?, ?, ?, ?)
