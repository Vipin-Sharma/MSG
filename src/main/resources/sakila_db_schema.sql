--Not working right now due to KEY keyword not being supported in JSQLParser. Raised a bug to understand if it is supported.

CREATE TABLE actor (
  actor_id INT NOT NULL IDENTITY ,
  first_name VARCHAR(45) NOT NULL,
  last_name VARCHAR(45) NOT NULL,
  last_update DATETIME NOT NULL,
  PRIMARY KEY NONCLUSTERED (actor_id)
  );

--
-- Table structure for table country
--


CREATE TABLE country (
  country_id INT NOT NULL IDENTITY ,
  country VARCHAR(50) NOT NULL,
  last_update DATETIME,
  PRIMARY KEY NONCLUSTERED (country_id)
);

--
-- Table structure for table city
--

CREATE TABLE city (
  city_id INT NOT NULL IDENTITY ,
  city VARCHAR(50) NOT NULL,
  country_id INT NOT NULL,
  last_update DATETIME NOT NULL,
  PRIMARY KEY NONCLUSTERED (city_id),
  CONSTRAINT fk_city_country FOREIGN KEY (country_id) REFERENCES country (country_id) ON DELETE NO ACTION ON UPDATE CASCADE
);

--
-- Table structure for table address
--

CREATE TABLE address (
  address_id INT NOT NULL IDENTITY ,
  address VARCHAR(50) NOT NULL,
  address2 VARCHAR(50) DEFAULT NULL,
  district VARCHAR(20) NOT NULL,
  city_id INT  NOT NULL,
  postal_code VARCHAR(10) DEFAULT NULL,
  phone VARCHAR(20) NOT NULL,
  last_update DATETIME NOT NULL,
  PRIMARY KEY NONCLUSTERED (address_id)
);
--
-- Table structure for table language
--

CREATE TABLE language (
  language_id INT NOT NULL IDENTITY,
  name CHAR(20) NOT NULL,
  last_update DATETIME NOT NULL,
  PRIMARY KEY NONCLUSTERED (language_id)
);

--
-- Table structure for table category
--

CREATE TABLE category (
  category_id INT NOT NULL IDENTITY,
  name VARCHAR(25) NOT NULL,
  last_update DATETIME NOT NULL,
  PRIMARY KEY NONCLUSTERED (category_id)
);

--
-- Table structure for table customer
--

CREATE TABLE customer (
  customer_id INT NOT NULL IDENTITY ,
  store_id INT NOT NULL,
  first_name VARCHAR(45) NOT NULL,
  last_name VARCHAR(45) NOT NULL,
  email VARCHAR(50) DEFAULT NULL,
  address_id INT NOT NULL,
  active CHAR(1) NOT NULL DEFAULT 'Y',
  create_date DATETIME NOT NULL,
  last_update DATETIME NOT NULL,
  PRIMARY KEY NONCLUSTERED (customer_id),
  CONSTRAINT fk_customer_address FOREIGN KEY (address_id) REFERENCES address (address_id) ON DELETE NO ACTION ON UPDATE CASCADE
);
--
-- Table structure for table film
--

CREATE TABLE film (
  film_id INT NOT NULL IDENTITY ,
  title VARCHAR(255) NOT NULL,
  description TEXT DEFAULT NULL,
  release_year VARCHAR(4) NULL,
  language_id INT NOT NULL,
  original_language_id INT DEFAULT NULL,
  rental_duration TINYINT NOT NULL DEFAULT 3,
  rental_rate DECIMAL(4,2) NOT NULL DEFAULT 4.99,
  length SMALLINT DEFAULT NULL,
  replacement_cost DECIMAL(5,2) NOT NULL DEFAULT 19.99,
  rating VARCHAR(10) DEFAULT 'G',
  special_features VARCHAR(255) DEFAULT NULL,
  last_update DATETIME NOT NULL,
  PRIMARY KEY NONCLUSTERED (film_id),
  CONSTRAINT fk_film_language FOREIGN KEY (language_id) REFERENCES language (language_id) ,
  CONSTRAINT fk_film_language_original FOREIGN KEY (original_language_id) REFERENCES language (language_id)
);


--
-- Table structure for table film_actor
--

CREATE TABLE film_actor (
  actor_id INT NOT NULL,
  film_id  INT NOT NULL,
  last_update DATETIME NOT NULL,
  PRIMARY KEY NONCLUSTERED (actor_id,film_id),
  CONSTRAINT fk_film_actor_actor FOREIGN KEY (actor_id) REFERENCES actor (actor_id) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT fk_film_actor_film FOREIGN KEY (film_id) REFERENCES film (film_id) ON DELETE NO ACTION ON UPDATE CASCADE
);

--
-- Table structure for table film_category
--

CREATE TABLE film_category (
  film_id INT NOT NULL,
  category_id INT  NOT NULL,
  last_update DATETIME NOT NULL,
  PRIMARY KEY NONCLUSTERED (film_id, category_id),
  CONSTRAINT fk_film_category_film FOREIGN KEY (film_id) REFERENCES film (film_id) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT fk_film_category_category FOREIGN KEY (category_id) REFERENCES category (category_id) ON DELETE NO ACTION ON UPDATE CASCADE
);
--
-- Table structure for table film_text
--

CREATE TABLE film_text (
  film_id INT NOT NULL,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  PRIMARY KEY NONCLUSTERED (film_id),
)

--
-- Table structure for table inventory
--

CREATE TABLE inventory (
  inventory_id INT NOT NULL IDENTITY,
  film_id INT NOT NULL,
  store_id INT NOT NULL,
  last_update DATETIME NOT NULL,
  PRIMARY KEY NONCLUSTERED (inventory_id),
  CONSTRAINT fk_inventory_film FOREIGN KEY (film_id) REFERENCES film (film_id) ON DELETE NO ACTION ON UPDATE CASCADE
);

--
-- Table structure for table staff
--

CREATE TABLE staff (
  staff_id INT NOT NULL IDENTITY,
  first_name VARCHAR(45) NOT NULL,
  last_name VARCHAR(45) NOT NULL,
  address_id INT NOT NULL,
  picture IMAGE DEFAULT NULL,
  email VARCHAR(50) DEFAULT NULL,
  store_id INT NOT NULL,
  active BIT NOT NULL DEFAULT 1,
  username VARCHAR(16) NOT NULL,
  password VARCHAR(40) DEFAULT NULL,
  last_update DATETIME NOT NULL,
  PRIMARY KEY NONCLUSTERED (staff_id),
  CONSTRAINT fk_staff_address FOREIGN KEY (address_id) REFERENCES address (address_id) ON DELETE NO ACTION ON UPDATE CASCADE
);
--
-- Table structure for table store
--

CREATE TABLE store (
  store_id INT NOT NULL IDENTITY,
  manager_staff_id INT NOT NULL,
  address_id INT NOT NULL,
  last_update DATETIME NOT NULL,
  PRIMARY KEY NONCLUSTERED (store_id),
  CONSTRAINT fk_store_staff FOREIGN KEY (manager_staff_id) REFERENCES staff (staff_id) ,
  CONSTRAINT fk_store_address FOREIGN KEY (address_id) REFERENCES address (address_id)
);

--
-- Table structure for table payment
--

CREATE TABLE payment (
  payment_id INT NOT NULL IDENTITY ,
  customer_id INT  NOT NULL,
  staff_id INT NOT NULL,
  rental_id INT DEFAULT NULL,
  amount DECIMAL(5,2) NOT NULL,
  payment_date DATETIME NOT NULL,
  last_update DATETIME NOT NULL,
  PRIMARY KEY NONCLUSTERED (payment_id),
  CONSTRAINT fk_payment_customer FOREIGN KEY (customer_id) REFERENCES customer (customer_id) ,
  CONSTRAINT fk_payment_staff FOREIGN KEY (staff_id) REFERENCES staff (staff_id)
);
--
-- Table structure for table rental
--

CREATE TABLE rental (
  rental_id INT NOT NULL IDENTITY,
  rental_date DATETIME NOT NULL,
  inventory_id INT  NOT NULL,
  customer_id INT  NOT NULL,
  return_date DATETIME DEFAULT NULL,
  staff_id INT  NOT NULL,
  last_update DATETIME NOT NULL,
  PRIMARY KEY NONCLUSTERED (rental_id),
  CONSTRAINT fk_rental_staff FOREIGN KEY (staff_id) REFERENCES staff (staff_id) ,
  CONSTRAINT fk_rental_inventory FOREIGN KEY (inventory_id) REFERENCES inventory (inventory_id) ,
  CONSTRAINT fk_rental_customer FOREIGN KEY (customer_id) REFERENCES customer (customer_id)
);
