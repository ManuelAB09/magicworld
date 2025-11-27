-- Password for 'admin' is 'AdminPassword123!'
INSERT  INTO users (username, firstname, lastname, email, password, user_role)
VALUES ('admin', 'Admin', 'Principal', 'admin@example.com', '$2a$10$LwU2fAnk66AtbSaUB9umZ.PV8TZ/IbkLO9DRDUg0/eGMj/9nWk2Nm', 'ADMIN');

-- Password for user is 'SecurePassword123!'
INSERT  INTO users (username, firstname, lastname, email, password, user_role)
VALUES ('user1', 'User', '1', 'user1@example.com', '$2a$10$JZA..SzcDSPyDZXa.ESvYeUCdbz51tkOMOdp7237iOivFmx5suRjy', 'USER');


INSERT  INTO ticket_type (cost, currency, type_name, description, max_per_day, photo_url)
VALUES (29.90, 'EUR', 'Adult', 'Entrada general para adultos', 100, '/img/adult_ticket.jpg'),
       (19.90, 'EUR', 'Child', 'Entrada para peques', 50, '/img/child_ticket.jpg');


INSERT  INTO discount (discount_percentage, expiry_date, discount_code)
VALUES (10, CURDATE() + INTERVAL 30 DAY, 'SUMMER10');


INSERT  INTO discount_ticket_type (discount_id, ticket_type_id)
SELECT d.id, t.id
FROM discount d
JOIN ticket_type t ON 1=1
WHERE d.discount_code = 'SUMMER10' AND t.type_name = 'Adult';


INSERT  INTO attraction (name, intensity, minimum_height, minimum_age, minimum_weight, description, photo_url, is_active)
VALUES ('Roller Coaster', 'HIGH', 120, 12, 30, 'Cool roller coaster', '/img/roller.jpg', TRUE);


INSERT  INTO purchase (purchase_date, buyer_id)
SELECT CURDATE(), u.id FROM users u WHERE u.username = 'user1';


INSERT  INTO purchase_line (valid_date, quantity, purchase_id, total_cost, ticket_type_name)
SELECT CURDATE() + INTERVAL 1 DAY, 2, p.id, 2 * t.cost, t.type_name
FROM purchase p
JOIN users u ON p.buyer_id = u.id
JOIN ticket_type t ON t.type_name = 'Adult'
WHERE u.username = 'user1';


INSERT  INTO review (stars, publication_date, description, user_id)
SELECT 4.5, CURDATE(), 'Muy divertida y emocionante', u.id
FROM users u
WHERE u.username = 'user1';
