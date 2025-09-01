SELECT
        cus.first_name,
        cus.last_name,
        cus.email,
        cit.city AS mycity,
        cus.create_date,
        cit.country_id
      FROM
        customer cus
        JOIN address adr ON cus.address_id = adr.address_id
        JOIN city cit ON adr.city_id = cit.city_id
        JOIN country cou ON cit.country_id = cou.country_id
      WHERE
        cou.country = 'India'
        AND cus.customer_id = 300
        AND cus.first_name ='john'