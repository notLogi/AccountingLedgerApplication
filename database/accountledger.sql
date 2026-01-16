USE sys;

DROP DATABASE IF EXISTS accountingledger;

CREATE DATABASE IF NOT EXISTS accountingledger;

USE accountingledger;

CREATE TABLE users (
                       user_id INT NOT NULL AUTO_INCREMENT,
                       name VARCHAR(50) NOT NULL,
                       password VARCHAR(50) NOT NULL,
                       PRIMARY KEY (user_id)
);

CREATE TABLE transactions (
                              transaction_id INT AUTO_INCREMENT,
                              user_id INT NOT NULL,
                              name VARCHAR(50) NOT NULL,
                              date DATE NOT NULL,
                              time TIME NOT NULL,
                              description VARCHAR(255) NOT NULL,
                              vendor VARCHAR(50) NOT NULL,
                              amount DECIMAL(19, 2) NOT NULL,
                              PRIMARY KEY (transaction_id),
                              FOREIGN KEY (user_id) REFERENCES users (user_id)
);
