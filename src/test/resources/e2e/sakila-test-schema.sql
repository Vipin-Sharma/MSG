-- Minimal Sakila schema for E2E testing

-- Country table
CREATE TABLE country (
    country_id int IDENTITY(1,1) PRIMARY KEY,
    country varchar(50) NOT NULL,
    last_update datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- City table
CREATE TABLE city (
    city_id int IDENTITY(1,1) PRIMARY KEY,
    city varchar(50) NOT NULL,
    country_id int NOT NULL,
    last_update datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (country_id) REFERENCES country(country_id)
);

-- Address table
CREATE TABLE address (
    address_id int IDENTITY(1,1) PRIMARY KEY,
    address varchar(50) NOT NULL,
    address2 varchar(50),
    district varchar(20) NOT NULL,
    city_id int NOT NULL,
    postal_code varchar(10),
    phone varchar(20) NOT NULL,
    last_update datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (city_id) REFERENCES city(city_id)
);

-- Customer table
CREATE TABLE customer (
    customer_id int IDENTITY(1,1) PRIMARY KEY,
    first_name varchar(45) NOT NULL,
    last_name varchar(45) NOT NULL,
    email varchar(50),
    address_id int NOT NULL,
    active char(1) NOT NULL DEFAULT 'Y',
    create_date datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_update datetime DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (address_id) REFERENCES address(address_id)
);

-- Insert test data
INSERT INTO country (country) VALUES 
('United States'),
('Canada'),
('United Kingdom');

INSERT INTO city (city, country_id) VALUES 
('New York', 1),
('Toronto', 2),
('London', 3);

INSERT INTO address (address, district, city_id, phone) VALUES 
('123 Main St', 'Downtown', 1, '555-0100'),
('456 Oak Ave', 'Uptown', 2, '555-0200'),
('789 High St', 'Central', 3, '555-0300');

INSERT INTO customer (first_name, last_name, email, address_id, active) VALUES 
('John', 'Doe', 'john.doe@test.com', 1, 'Y'),
('Jane', 'Smith', 'jane.smith@test.com', 2, 'Y'),
('Bob', 'Johnson', 'bob.johnson@test.com', 3, 'N');