UPDATE customer 
SET first_name = ?, 
    last_name = ?, 
    email = ?,
    last_update = ?
WHERE customer_id = ? 
AND active = ?