UPDATE customer 
SET customer.first_name = ?,
    customer.last_name = ?,
    customer.email = ?
FROM customer 
INNER JOIN address ON customer.address_id = address.address_id
INNER JOIN city ON address.city_id = city.city_id
WHERE city.city = ? 
AND customer.customer_id = ?