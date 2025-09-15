UPDATE customer 
SET first_name = ?, 
    last_name = ?, 
    email = ?,
    last_update = CURRENT_TIMESTAMP
WHERE customer_id = ? 
AND active = ?