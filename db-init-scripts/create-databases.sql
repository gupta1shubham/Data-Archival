-- Create multiple logical databases for each service
CREATE DATABASE archivaldb;
CREATE DATABASE configdb;
CREATE DATABASE securitydb;


-- Switch to primarydb
\c primarydb;

-- Create customer_records table
CREATE TABLE IF NOT EXISTS customer_records (
                                                id BIGINT NOT NULL PRIMARY KEY,
                                                data VARCHAR(255),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
    );

-- Create order_history table
CREATE TABLE IF NOT EXISTS order_history (
                                             id BIGINT NOT NULL PRIMARY KEY,
                                             data VARCHAR(255),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
    );


INSERT INTO customer_records (id, data, created_date, last_updated_date, is_active) VALUES
                                                                                        (1, 'Customer Data 1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
                                                                                        (2, 'Customer Data 2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
                                                                                        (3, 'Customer Data 3', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
                                                                                        (4, 'Customer Data 4', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
                                                                                        (5, 'Customer Data 5', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
                                                                                        (6, 'Customer Data 6', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
                                                                                        (7, 'Customer Data 7', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
                                                                                        (8, 'Customer Data 8', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
                                                                                        (9, 'Customer Data 9', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
                                                                                        (10, 'Customer Data 10', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE);

-- Insert 10 rows into order_history
INSERT INTO order_history (id, data, created_date, last_updated_date, is_active) VALUES
                                                                                     (1, 'Order Data 1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
                                                                                     (2, 'Order Data 2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
                                                                                     (3, 'Order Data 3', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
                                                                                     (4, 'Order Data 4', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
                                                                                     (5, 'Order Data 5', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
                                                                                     (6, 'Order Data 6', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
                                                                                     (7, 'Order Data 7', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
                                                                                     (8, 'Order Data 8', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
                                                                                     (9, 'Order Data 9', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),

                                                                     (10, 'Order Data 10', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE);
INSERT INTO customer_records (id, data, created_date, last_updated_date, is_active) VALUES
                                                                                        (11, 'Old Customer Data 1', CURRENT_TIMESTAMP - INTERVAL '1 year', CURRENT_TIMESTAMP - INTERVAL '1 year', TRUE),
                                                                                        (12, 'Old Customer Data 2', CURRENT_TIMESTAMP - INTERVAL '1 year', CURRENT_TIMESTAMP - INTERVAL '1 year', TRUE),
                                                                                        (13, 'Old Customer Data 3', CURRENT_TIMESTAMP - INTERVAL '1 year', CURRENT_TIMESTAMP - INTERVAL '1 year', TRUE),
                                                                                        (14, 'Old Customer Data 4', CURRENT_TIMESTAMP - INTERVAL '1 year', CURRENT_TIMESTAMP - INTERVAL '1 year', TRUE),
                                                                                        (15, 'Old Customer Data 5', CURRENT_TIMESTAMP - INTERVAL '1 year', CURRENT_TIMESTAMP - INTERVAL '1 year', TRUE);
INSERT INTO order_history (id, data, created_date, last_updated_date, is_active) VALUES
                                                                                     (11, 'Old Order Data 1', CURRENT_TIMESTAMP - INTERVAL '1 year', CURRENT_TIMESTAMP - INTERVAL '1 year', TRUE),
                                                                                     (12, 'Old Order Data 2', CURRENT_TIMESTAMP - INTERVAL '1 year', CURRENT_TIMESTAMP - INTERVAL '1 year', TRUE),
                                                                                     (13, 'Old Order Data 3', CURRENT_TIMESTAMP - INTERVAL '1 year', CURRENT_TIMESTAMP - INTERVAL '1 year', TRUE),
                                                                                     (14, 'Old Order Data 4', CURRENT_TIMESTAMP - INTERVAL '1 year', CURRENT_TIMESTAMP - INTERVAL '1 year', TRUE),
                                                                                     (15, 'Old Order Data 5', CURRENT_TIMESTAMP - INTERVAL '1 year', CURRENT_TIMESTAMP - INTERVAL '1 year', TRUE);
INSERT INTO customer_records (id, data, created_date, last_updated_date, is_active) VALUES
                                                                                        (16, 'Old Customer Data 1', CURRENT_TIMESTAMP - INTERVAL '6 months', CURRENT_TIMESTAMP - INTERVAL '6 months', TRUE),
                                                                                        (17, 'Old Customer Data 2', CURRENT_TIMESTAMP - INTERVAL '6 months', CURRENT_TIMESTAMP - INTERVAL '6 months', TRUE),
                                                                                        (18, 'Old Customer Data 3', CURRENT_TIMESTAMP - INTERVAL '6 months', CURRENT_TIMESTAMP - INTERVAL '6 months', TRUE),
                                                                                        (19, 'Old Customer Data 4', CURRENT_TIMESTAMP - INTERVAL '6 months', CURRENT_TIMESTAMP - INTERVAL '6 months', TRUE),
                                                                                        (20, 'Old Customer Data 5', CURRENT_TIMESTAMP - INTERVAL '6 months', CURRENT_TIMESTAMP - INTERVAL '6 months', TRUE);
INSERT INTO order_history (id, data, created_date, last_updated_date, is_active) VALUES
                                                                                     (16, 'Old Order Data 1', CURRENT_TIMESTAMP - INTERVAL '6 months', CURRENT_TIMESTAMP - INTERVAL '6 months', TRUE),
                                                                                     (17, 'Old Order Data 2', CURRENT_TIMESTAMP - INTERVAL '6 months', CURRENT_TIMESTAMP - INTERVAL '6 months', TRUE),
                                                                                     (18, 'Old Order Data 3', CURRENT_TIMESTAMP - INTERVAL '6 months', CURRENT_TIMESTAMP - INTERVAL '6 months', TRUE),
                                                                                     (19, 'Old Order Data 4', CURRENT_TIMESTAMP - INTERVAL '6 months', CURRENT_TIMESTAMP - INTERVAL '6 months', TRUE),
                                                                                     (20, 'Old Order Data 5', CURRENT_TIMESTAMP - INTERVAL '6 months', CURRENT_TIMESTAMP - INTERVAL '6 months', TRUE);
