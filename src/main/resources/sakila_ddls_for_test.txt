CREATE TABLE actor (
  actor_id INT NOT NULL IDENTITY ,
  first_name VARCHAR(45) NOT NULL,
  last_name VARCHAR(45) NOT NULL,
  last_update DATETIME NOT NULL
  );
CREATE TABLE country (
  country_id INT NOT NULL IDENTITY ,
  country VARCHAR(50) NOT NULL,
  last_update DATETIME
);
CREATE TABLE city (
  city_id INT NOT NULL IDENTITY ,
  city VARCHAR(50) NOT NULL,
  country_id INT NOT NULL,
  last_update DATETIME NOT NULL
);
CREATE TABLE address (
  address_id INT NOT NULL IDENTITY ,
  address VARCHAR(50) NOT NULL,
  address2 VARCHAR(50) DEFAULT NULL,
  district VARCHAR(20) NOT NULL,
  city_id INT  NOT NULL,
  postal_code VARCHAR(10) DEFAULT NULL,
  phone VARCHAR(20) NOT NULL,
  last_update DATETIME NOT NULL
);
CREATE TABLE language (
  language_id INT NOT NULL IDENTITY,
  name CHAR(20) NOT NULL,
  last_update DATETIME NOT NULL
);
CREATE TABLE category (
  category_id INT NOT NULL IDENTITY,
  name VARCHAR(25) NOT NULL,
  last_update DATETIME NOT NULL
);
CREATE TABLE customer (
  customer_id INT NOT NULL IDENTITY ,
  store_id INT NOT NULL,
  first_name VARCHAR(45) NOT NULL,
  last_name VARCHAR(45) NOT NULL,
  email VARCHAR(50) DEFAULT NULL,
  address_id INT NOT NULL,
  active CHAR(1) NOT NULL DEFAULT 'Y',
  create_date DATETIME NOT NULL,
  last_update DATETIME NOT NULL
);
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
  last_update DATETIME NOT NULL
);
CREATE TABLE film_actor (
  actor_id INT NOT NULL,
  film_id  INT NOT NULL,
  last_update DATETIME NOT NULL
);
CREATE TABLE film_category (
  film_id INT NOT NULL,
  category_id INT  NOT NULL,
  last_update DATETIME NOT NULL
);
CREATE TABLE film_text (
  film_id INT NOT NULL,
  title VARCHAR(255) NOT NULL,
  description TEXT
);
CREATE TABLE inventory (
  inventory_id INT NOT NULL IDENTITY,
  film_id INT NOT NULL,
  store_id INT NOT NULL,
  last_update DATETIME NOT NULL
);
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
  last_update DATETIME NOT NULL
);
CREATE TABLE store (
  store_id INT NOT NULL IDENTITY,
  manager_staff_id INT NOT NULL,
  address_id INT NOT NULL,
  last_update DATETIME NOT NULL
);
CREATE TABLE payment (
  payment_id INT NOT NULL IDENTITY ,
  customer_id INT  NOT NULL,
  staff_id INT NOT NULL,
  rental_id INT DEFAULT NULL,
  amount DECIMAL(5,2) NOT NULL,
  payment_date DATETIME NOT NULL,
  last_update DATETIME NOT NULL
);
CREATE TABLE rental (
  rental_id INT NOT NULL IDENTITY,
  rental_date DATETIME NOT NULL,
  inventory_id INT  NOT NULL,
  customer_id INT  NOT NULL,
  return_date DATETIME DEFAULT NULL,
  staff_id INT  NOT NULL,
  last_update DATETIME NOT NULL
);
