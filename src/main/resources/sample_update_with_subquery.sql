UPDATE customer 
SET email = ?, 
    last_update = ?
WHERE customer_id IN (
    SELECT customer_id 
    FROM address a 
    INNER JOIN city c ON a.city_id = c.city_id 
    WHERE c.country_id = ? 
    AND a.district = ?
) 
AND active = ?