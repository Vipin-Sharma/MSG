WITH active_customers AS (
  SELECT customer_id, first_name, last_name, email
  FROM customer 
  WHERE active = 1
    AND create_date >= ?
),
customer_rentals AS (
  SELECT customer_id, COUNT(*) as rental_count
  FROM rental
  WHERE rental_date >= ?
  GROUP BY customer_id
)
SELECT 
  ac.customer_id,
  ac.first_name,
  ac.last_name, 
  ac.email,
  cr.rental_count
FROM active_customers ac
LEFT JOIN customer_rentals cr ON ac.customer_id = cr.customer_id  
WHERE ac.customer_id = ?
  AND cr.rental_count > ?