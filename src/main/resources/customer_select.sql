SELECT 
    c.customer_id,
    c.first_name,
    c.last_name,
    c.email,
    c.active,
    c.create_date
FROM customer c 
WHERE c.active = ? AND c.customer_id = ?