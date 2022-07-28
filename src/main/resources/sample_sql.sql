select cus.first_name, cus.last_name, cus.email, cit.city as mycity
                         from customer cus
                         join address adr
                         on cus.address_id = adr.address_id
                         join city cit
                         on adr.city_id = cit.city_id
                         join country cou
                         on cit.country_id = cou.country_id
                         where cou.country = 'HARDCODE_AS_STRING{India}'
                         and cus.customer_id = 'HARDCODE_AS_{300}'
                         and cus.first_name = 'John'
