-- Password for 'admin' is 'AdminPassword123!'
INSERT  INTO users (username, firstname, lastname, email, password, user_role)
VALUES ('admin', 'Admin', 'Principal', 'admin@example.com', '$2a$10$LwU2fAnk66AtbSaUB9umZ.PV8TZ/IbkLO9DRDUg0/eGMj/9nWk2Nm', 'ADMIN');

-- Password for user is 'SecurePassword123!'
INSERT  INTO users (username, firstname, lastname, email, password, user_role)
VALUES ('user1', 'User', '1', 'user1@example.com', '$2a$10$JZA..SzcDSPyDZXa.ESvYeUCdbz51tkOMOdp7237iOivFmx5suRjy', 'USER');


INSERT  INTO ticket_type (cost, type_name, description, max_per_day, photo_url)
VALUES (29.90, 'Adult', 'Entrada general para adultos', 100, '/img/adult_ticket.jpg'),
       (19.90, 'Child', 'Entrada para peques', 50, '/img/child_ticket.jpg');


INSERT  INTO discount (discount_percentage, expiry_date, discount_code)
VALUES (10, CURDATE() + INTERVAL 30 DAY, 'SUMMER10');


INSERT  INTO discount_ticket_type (discount_id, ticket_type_id)
SELECT d.id, t.id
FROM discount d
JOIN ticket_type t ON 1=1
WHERE d.discount_code = 'SUMMER10' AND t.type_name = 'Adult';


INSERT INTO attraction (name, intensity, category, minimum_height, minimum_age, minimum_weight, description, photo_url, is_active, map_position_x, map_position_y)
VALUES

    ('Dragon Fury', 'HIGH', 'ROLLER_COASTER', 140, 14, 40, 'La montaña rusa mas extrema del parque con 3 inversiones y una caida de 50 metros', '/img/dragon_fury.jpg', TRUE, 85.0, 80.0),
    ('Thunder Tower', 'HIGH', 'DROP_TOWER', 130, 12, 35, 'Torre de caida libre de 60 metros con vistas panoramicas del parque', '/img/thunder_tower.jpg', TRUE, 90.0, 70.0),


    ('Velocity Coaster', 'HIGH', 'ROLLER_COASTER', 135, 12, 35, 'Montaña rusa lanzada con aceleración de 0 a 100 km/h en 3 segundos', '/img/velocity_coaster.jpg', TRUE, 85.0, 35.0),
    ('Storm Chaser', 'HIGH', 'DROP_TOWER', 125, 10, 30, 'Torre de rebote con múltiples caidas y subidas', '/img/storm_chaser.jpg', TRUE, 90.0, 25.0),


    ('Splash Mountain', 'MEDIUM', 'WATER_RIDE', 110, 8, 25, 'Emocionante descenso acuatico con una cascada final de 20 metros', '/img/splash_mountain.jpg', TRUE, 15.0, 70.0),
    ('Pirate Rapids', 'MEDIUM', 'WATER_RIDE', 100, 6, 20, 'Aventura en balsas por rapidos y cuevas piratas', '/img/pirate_rapids.jpg', TRUE, 10.0, 80.0),


    ('Magic Carousel', 'LOW', 'CAROUSEL', 0, 0, 0, 'Carrusel clásico con caballos magicos y música encantadora', '/img/magic_carousel.jpg', TRUE, 15.0, 35.0),
    ('Mini Carousel', 'LOW', 'CAROUSEL', 0, 0, 0, 'Carrusel especial para los mas pequeños con animales de granja', '/img/mini_carousel.jpg', TRUE, 10.0, 25.0),


    ('Haunted Manor', 'MEDIUM', 'HAUNTED_HOUSE', 100, 8, 0, 'Casa encantada interactiva con efectos especiales y sustos inesperados', '/img/haunted_manor.jpg', TRUE, 55.0, 85.0),
    ('Sky Swings', 'MEDIUM', 'SWING_RIDE', 120, 10, 30, 'Sillas voladoras a 30 metros de altura con rotacion de 360 grados', '/img/sky_swings.jpg', TRUE, 45.0, 85.0),


    ('Wild River', 'MEDIUM', 'WATER_RIDE', 105, 7, 22, 'Recorrido por un río salvaje con sorpresas acuáticas', '/img/wild_river.jpg', TRUE, 92.0, 55.0),
    ('Bumper Kingdom', 'LOW', 'BUMPER_CARS', 90, 5, 15, 'Coches de choque con tematica medieval para toda la familia', '/img/bumper_kingdom.jpg', TRUE, 70.0, 60.0),


    ('Enchanted Train', 'LOW', 'TRAIN_RIDE', 0, 0, 0, 'Recorrido panoramico por todo el parque en un tren de epoca', '/img/enchanted_train.jpg', TRUE, 8.0, 55.0),
    ('Flying Carpet', 'LOW', 'SWING_RIDE', 90, 5, 15, 'Alfombra voladora que gira y sube suavemente', '/img/flying_carpet.jpg', TRUE, 30.0, 60.0),


    ('Space Journey', 'MEDIUM', 'OTHER', 100, 6, 0, 'Viaje espacial interactivo con proyecciones 4D', '/img/space_journey.jpg', TRUE, 65.0, 15.0),
    ('Teacups Spin', 'LOW', 'OTHER', 0, 0, 0, 'Tazas giratorias para toda la familia', '/img/teacups.jpg', TRUE, 35.0, 15.0),


    ('Giant Wheel', 'LOW', 'FERRIS_WHEEL', 0, 0, 0, 'Noria gigante de 50 metros con cabinas climatizadas y vistas espectaculares', '/img/giant_wheel.jpg', TRUE, 50.0, 65.0);



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
