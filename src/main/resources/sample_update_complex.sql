UPDATE customer 
SET first_name = ?,
    email = CASE 
        WHEN customer.store_id = ? THEN ?
        ELSE customer.email
    END,
    last_update = ?
WHERE customer_id = ? 
AND EXISTS (
    SELECT 1 
    FROM rental r 
    INNER JOIN inventory i ON r.inventory_id = i.inventory_id 
    WHERE r.customer_id = customer.customer_id 
    AND i.film_id = ?
    AND r.return_date IS NULL
)