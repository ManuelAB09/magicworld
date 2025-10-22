DROP TABLE IF EXISTS users;
CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    firstname VARCHAR(50) NOT NULL,
    lastname VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL
);

-- Password for 'admin' is 'AdminPassword123!'
INSERT INTO users (username, firstname, lastname, email, password, role)
VALUES ('admin', 'Admin', 'Principal', 'admin@example.com', '$2a$10$LwU2fAnk66AtbSaUB9umZ.PV8TZ/IbkLO9DRDUg0/eGMj/9nWk2Nm', 'ADMIN');

-- Password for user is 'SecurePassword123!'
INSERT INTO users (username, firstname, lastname, email, password, role)
VALUES ('jdoe', 'John', 'Doe', 'jdoe@example.com', '$2a$10$JZA..SzcDSPyDZXa.ESvYeUCdbz51tkOMOdp7237iOivFmx5suRjy', 'USER');