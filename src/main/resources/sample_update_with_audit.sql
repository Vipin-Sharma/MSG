-- Sample UPDATE with audit fields
-- The generator will auto-populate updated_at and updated_by
UPDATE customer
SET first_name = ?,
    last_name = ?,
    email = ?,
    updated_at = ?,    -- Audit field: auto-populated with current timestamp
    updated_by = ?     -- Audit field: auto-populated with authenticated user ID
WHERE customer_id = ?
  AND active = ?
