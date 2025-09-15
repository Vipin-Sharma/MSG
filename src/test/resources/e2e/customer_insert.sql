INSERT INTO customer (
    first_name, 
    last_name, 
    email, 
    address_id, 
    active, 
    create_date
) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)