-- Password for 'admin' is 'AdminPassword123!'
INSERT  INTO users (username, firstname, lastname, email, password, user_role)
VALUES ('admin', 'Admin', 'Principal', 'admin@example.com', '$2a$10$LwU2fAnk66AtbSaUB9umZ.PV8TZ/IbkLO9DRDUg0/eGMj/9nWk2Nm', 'ADMIN');

-- Password for user is 'SecurePassword123!'
INSERT  INTO users (username, firstname, lastname, email, password, user_role)
VALUES ('user1', 'User', '1', 'user1@example.com', '$2a$10$JZA..SzcDSPyDZXa.ESvYeUCdbz51tkOMOdp7237iOivFmx5suRjy', 'USER');


INSERT  INTO ticket_type (cost, type_name, description, max_per_day, photo_url)
VALUES (29.90, 'Adult', 'Entrada general para adultos', 300, '/images/ticket-types/e9dc4e2d-1636-418a-a2ae-f512b67bbf71.png'),
       (19.90, 'Child', 'Entrada para peques', 200, '/images/ticket-types/bb720bd8-b4d4-4cd5-9452-5c12cdef52f2.png');


INSERT  INTO discount (discount_percentage, expiry_date, discount_code)
VALUES (10, CURDATE() + INTERVAL 30 DAY, 'SUMMER10');


INSERT  INTO discount_ticket_type (discount_id, ticket_type_id)
SELECT d.id, t.id
FROM discount d
JOIN ticket_type t ON 1=1
WHERE d.discount_code = 'SUMMER10' AND t.type_name = 'Adult';


INSERT INTO attraction (name, intensity, category, minimum_height, minimum_age, minimum_weight, description, photo_url, is_active, map_position_x, map_position_y, maintenance_status, opening_time, closing_time)
VALUES

    ('Dragon Fury', 'HIGH', 'ROLLER_COASTER', 140, 14, 40, 'La montaña rusa mas extrema del parque con 3 inversiones y una caida de 50 metros', '/images/attractions/7fd38b89-47d6-4d2c-bfe5-25153a2a9d94.png', TRUE, 63.5, 37.6, 'OPERATIONAL', '10:00', '18:00'),
    ('Thunder Tower', 'HIGH', 'DROP_TOWER', 130, 12, 35, 'Torre de caida libre de 60 metros con vistas panoramicas del parque', '/images/attractions/af8e40ed-c299-46ad-b82b-9a3f815c666b.png', TRUE, 74.8, 44.6, 'OPERATIONAL', '10:00', '18:00'),


    ('Velocity Coaster', 'HIGH', 'ROLLER_COASTER', 135, 12, 35, 'Montaña rusa lanzada con aceleración de 0 a 100 km/h en 3 segundos', '/images/attractions/7fd38b89-47d6-4d2c-bfe5-25153a2a9d94.png', TRUE, 39.9, 81.4, 'OPERATIONAL', '10:00', '17:00'),
    ('Storm Chaser', 'HIGH', 'DROP_TOWER', 125, 10, 30, 'Torre de rebote con múltiples caidas y subidas', '/images/attractions/af8e40ed-c299-46ad-b82b-9a3f815c666b.png', TRUE, 24.2, 66.4, 'OPERATIONAL', '10:00', '17:30'),


    ('Splash Mountain', 'MEDIUM', 'WATER_RIDE', 110, 8, 25, 'Emocionante descenso acuatico con una cascada final de 20 metros', '/images/attractions/57c48af7-097a-46ce-b5d1-229db137dc56.png', TRUE, 31.0, 38.9, 'OPERATIONAL', '10:30', '18:00'),
    ('Pirate Rapids', 'MEDIUM', 'WATER_RIDE', 100, 6, 20, 'Aventura en balsas por rapidos y cuevas piratas', '/images/attractions/57c48af7-097a-46ce-b5d1-229db137dc56.png', TRUE, 27.9, 58.2, 'OPERATIONAL', '11:00', '18:00'),


    ('Magic Carousel', 'LOW', 'CAROUSEL', 0, 0, 0, 'Carrusel clásico con caballos magicos y música encantadora', '/images/attractions/693e1193-9c8c-402f-94ab-41ca7863d9db.png', TRUE, 72.8, 61.9, 'OPERATIONAL', '09:00', '17:00'),
    ('Mini Carousel', 'LOW', 'CAROUSEL', 0, 0, 0, 'Carrusel especial para los mas pequeños con animales de granja', '/images/attractions/693e1193-9c8c-402f-94ab-41ca7863d9db.png', TRUE, 41.1, 68.5, 'OPERATIONAL', '09:30', '16:30'),


    ('Haunted Manor', 'MEDIUM', 'HAUNTED_HOUSE', 100, 8, 0, 'Casa encantada interactiva con efectos especiales y sustos inesperados', '/images/attractions/74bf0fad-523b-4aee-ba86-c0d89315ad1b.png', TRUE, 58.3, 83.0, 'OPERATIONAL', '11:00', '19:00'),
    ('Sky Swings', 'MEDIUM', 'SWING_RIDE', 120, 10, 30, 'Sillas voladoras a 30 metros de altura con rotacion de 360 grados', '/images/attractions/f7fb7944-fb48-4a5f-92dd-8499256a7730.png', TRUE, 16.2, 61.5, 'OPERATIONAL', '10:00', '17:00'),


    ('Wild River', 'MEDIUM', 'WATER_RIDE', 105, 7, 22, 'Recorrido por un río salvaje con sorpresas acuáticas', '/images/attractions/57c48af7-097a-46ce-b5d1-229db137dc56.png', TRUE, 87.2, 83.8, 'OPERATIONAL', '10:30', '17:30'),
    ('Bumper Kingdom', 'LOW', 'BUMPER_CARS', 90, 5, 15, 'Coches de choque con tematica medieval para toda la familia', '/images/attractions/bc6c7c42-1c9e-43a2-81d4-2955f1f25a55.png', TRUE, 12.0, 23.3, 'OPERATIONAL', '10:00', '17:30'),


    ('Enchanted Train', 'LOW', 'TRAIN_RIDE', 0, 0, 0, 'Recorrido panoramico por todo el parque en un tren de epoca', '/images/attractions/8cbda956-fba3-49d9-976a-3d89d35a9d3f.png', TRUE, 87.7, 43.1, 'OPERATIONAL', '09:00', '16:00'),
    ('Flying Carpet', 'LOW', 'SWING_RIDE', 90, 5, 15, 'Alfombra voladora que gira y sube suavemente', '/images/attractions/f7fb7944-fb48-4a5f-92dd-8499256a7730.png', TRUE, 35.0, 25.0, 'OPERATIONAL', '10:00', '17:30'),


    ('Space Journey', 'MEDIUM', 'OTHER', 100, 6, 0, 'Viaje espacial interactivo con proyecciones 4D', '/images/attractions/db5351aa-3c0c-46f4-bd88-5c4c65c96ce1.png', TRUE, 76.8, 19.7, 'OPERATIONAL', '10:00', '17:00'),
    ('Teacups Spin', 'LOW', 'OTHER', 0, 0, 0, 'Tazas giratorias para toda la familia', '/images/attractions/db5351aa-3c0c-46f4-bd88-5c4c65c96ce1.png', TRUE, 11.2, 70.9, 'OPERATIONAL', '09:30', '16:30'),


    ('Giant Wheel', 'LOW', 'FERRIS_WHEEL', 0, 0, 0, 'Noria gigante de 50 metros con cabinas climatizadas y vistas espectaculares', '/images/attractions/21ffc9aa-afd5-4085-af00-ab5f7ec9ec19.png', TRUE, 61.0, 70.5, 'OPERATIONAL', '09:00', '17:00');



INSERT  INTO purchase (purchase_date, buyer_id)
SELECT CURDATE(), u.id FROM users u WHERE u.username = 'user1';


INSERT  INTO purchase_line (valid_date, quantity, purchase_id, total_cost, ticket_type_name)
SELECT CURDATE() + INTERVAL 1 DAY, 200, p.id, 200 * t.cost, t.type_name
FROM purchase p
JOIN users u ON p.buyer_id = u.id
JOIN ticket_type t ON t.type_name = 'Adult'
WHERE u.username = 'user1';

INSERT INTO purchase_line (valid_date, quantity, purchase_id, total_cost, ticket_type_name)
SELECT CURDATE() + INTERVAL 1 DAY, 100, p.id, 100 * t.cost, t.type_name
FROM purchase p
JOIN users u ON p.buyer_id = u.id
JOIN ticket_type t ON t.type_name = 'Child'
WHERE u.username = 'user1'
  AND p.purchase_date = CURDATE();


INSERT  INTO review (stars, publication_date, visit_date, description, purchase_id)
SELECT 4.5, CURDATE(), CURDATE() + INTERVAL 1 DAY, 'Muy divertida y emocionante', p.id
FROM purchase p
JOIN users u ON p.buyer_id = u.id
WHERE u.username = 'user1';

INSERT INTO purchase (purchase_date, buyer_id)
VALUES
    (CURDATE() - INTERVAL 12 DAY, (SELECT id FROM users WHERE username = 'user1')),
    (CURDATE() - INTERVAL 11 DAY, (SELECT id FROM users WHERE username = 'user1')),
    (CURDATE() - INTERVAL 10 DAY, (SELECT id FROM users WHERE username = 'user1')),
    (CURDATE() - INTERVAL 9 DAY, (SELECT id FROM users WHERE username = 'user1')),
    (CURDATE() - INTERVAL 8 DAY, (SELECT id FROM users WHERE username = 'user1')),
    (CURDATE() - INTERVAL 7 DAY, (SELECT id FROM users WHERE username = 'user1')),
    (CURDATE() - INTERVAL 6 DAY, (SELECT id FROM users WHERE username = 'user1')),
    (CURDATE() - INTERVAL 5 DAY, (SELECT id FROM users WHERE username = 'user1')),
    (CURDATE() - INTERVAL 4 DAY, (SELECT id FROM users WHERE username = 'user1')),
    (CURDATE() - INTERVAL 3 DAY, (SELECT id FROM users WHERE username = 'user1')),
    (CURDATE() - INTERVAL 2 DAY, (SELECT id FROM users WHERE username = 'user1'));

INSERT INTO purchase_line (valid_date, quantity, purchase_id, total_cost, ticket_type_name)
SELECT p.purchase_date + INTERVAL 1 DAY, 180, p.id, 180 * t.cost, t.type_name
FROM purchase p
JOIN users u ON p.buyer_id = u.id
JOIN ticket_type t ON t.type_name = 'Adult'
WHERE u.username = 'user1'
  AND p.purchase_date IN (
      CURDATE() - INTERVAL 12 DAY,
      CURDATE() - INTERVAL 11 DAY,
      CURDATE() - INTERVAL 10 DAY,
      CURDATE() - INTERVAL 9 DAY,
      CURDATE() - INTERVAL 8 DAY,
      CURDATE() - INTERVAL 7 DAY,
      CURDATE() - INTERVAL 6 DAY,
      CURDATE() - INTERVAL 5 DAY,
      CURDATE() - INTERVAL 4 DAY,
      CURDATE() - INTERVAL 3 DAY,
      CURDATE() - INTERVAL 2 DAY
  );

INSERT INTO purchase_line (valid_date, quantity, purchase_id, total_cost, ticket_type_name)
SELECT p.purchase_date + INTERVAL 1 DAY, 120, p.id, 120 * t.cost, t.type_name
FROM purchase p
JOIN users u ON p.buyer_id = u.id
JOIN ticket_type t ON t.type_name = 'Child'
WHERE u.username = 'user1'
  AND p.purchase_date IN (
      CURDATE() - INTERVAL 12 DAY,
      CURDATE() - INTERVAL 11 DAY,
      CURDATE() - INTERVAL 10 DAY,
      CURDATE() - INTERVAL 9 DAY,
      CURDATE() - INTERVAL 8 DAY,
      CURDATE() - INTERVAL 7 DAY,
      CURDATE() - INTERVAL 6 DAY,
      CURDATE() - INTERVAL 5 DAY,
      CURDATE() - INTERVAL 4 DAY,
      CURDATE() - INTERVAL 3 DAY,
      CURDATE() - INTERVAL 2 DAY
  );

INSERT INTO review (stars, publication_date, visit_date, description, purchase_id)
SELECT 5, CURDATE(), p.purchase_date + INTERVAL 1 DAY, 'Muy divertida y emocionante', p.id
FROM purchase p
JOIN users u ON p.buyer_id = u.id
WHERE u.username = 'user1'
  AND p.purchase_date IN (
      CURDATE() - INTERVAL 12 DAY,
      CURDATE() - INTERVAL 11 DAY,
      CURDATE() - INTERVAL 10 DAY,
      CURDATE() - INTERVAL 9 DAY,
      CURDATE() - INTERVAL 8 DAY,
      CURDATE() - INTERVAL 7 DAY,
      CURDATE() - INTERVAL 6 DAY,
      CURDATE() - INTERVAL 5 DAY,
      CURDATE() - INTERVAL 4 DAY,
      CURDATE() - INTERVAL 3 DAY,
      CURDATE() - INTERVAL 2 DAY
  );


INSERT INTO park_event (event_type, timestamp, visitor_count, queue_size, metadata)
VALUES
    ('PARK_ENTRY', NOW() - INTERVAL 3 HOUR, 1, NULL, NULL),
    ('PARK_ENTRY', NOW() - INTERVAL 2 HOUR - INTERVAL 45 MINUTE, 1, NULL, NULL),
    ('PARK_ENTRY', NOW() - INTERVAL 2 HOUR - INTERVAL 30 MINUTE, 1, NULL, NULL),
    ('PARK_ENTRY', NOW() - INTERVAL 2 HOUR - INTERVAL 15 MINUTE, 1, NULL, NULL),
    ('PARK_ENTRY', NOW() - INTERVAL 2 HOUR, 1, NULL, NULL),
    ('PARK_ENTRY', NOW() - INTERVAL 1 HOUR - INTERVAL 45 MINUTE, 1, NULL, NULL),
    ('PARK_ENTRY', NOW() - INTERVAL 1 HOUR - INTERVAL 30 MINUTE, 1, NULL, NULL),
    ('PARK_ENTRY', NOW() - INTERVAL 1 HOUR - INTERVAL 15 MINUTE, 1, NULL, NULL),
    ('PARK_ENTRY', NOW() - INTERVAL 1 HOUR, 1, NULL, NULL),
    ('PARK_ENTRY', NOW() - INTERVAL 45 MINUTE, 1, NULL, NULL),
    ('PARK_ENTRY', NOW() - INTERVAL 30 MINUTE, 1, NULL, NULL),
    ('PARK_ENTRY', NOW() - INTERVAL 15 MINUTE, 1, NULL, NULL);

-- Some exits
INSERT INTO park_event (event_type, timestamp, visitor_count, queue_size, metadata)
VALUES
    ('PARK_EXIT', NOW() - INTERVAL 30 MINUTE, 1, NULL, NULL),
    ('PARK_EXIT', NOW() - INTERVAL 20 MINUTE, 1, NULL, NULL);

-- Queue events for Dragon Fury (attraction id 1)
INSERT INTO park_event (event_type, timestamp, attraction_id, queue_size, metadata)
VALUES
    ('ATTRACTION_QUEUE_JOIN', NOW() - INTERVAL 2 HOUR, (SELECT id FROM attraction WHERE name = 'Dragon Fury'), 15, NULL),
    ('ATTRACTION_QUEUE_JOIN', NOW() - INTERVAL 1 HOUR - INTERVAL 30 MINUTE, (SELECT id FROM attraction WHERE name = 'Dragon Fury'), 25, NULL),
    ('ATTRACTION_QUEUE_JOIN', NOW() - INTERVAL 1 HOUR, (SELECT id FROM attraction WHERE name = 'Dragon Fury'), 35, NULL),
    ('ATTRACTION_QUEUE_JOIN', NOW() - INTERVAL 30 MINUTE, (SELECT id FROM attraction WHERE name = 'Dragon Fury'), 42, NULL),
    ('ATTRACTION_QUEUE_JOIN', NOW() - INTERVAL 15 MINUTE, (SELECT id FROM attraction WHERE name = 'Dragon Fury'), 48, NULL);

-- Queue events for Thunder Tower
INSERT INTO park_event (event_type, timestamp, attraction_id, queue_size, metadata)
VALUES
    ('ATTRACTION_QUEUE_JOIN', NOW() - INTERVAL 2 HOUR, (SELECT id FROM attraction WHERE name = 'Thunder Tower'), 10, NULL),
    ('ATTRACTION_QUEUE_JOIN', NOW() - INTERVAL 1 HOUR, (SELECT id FROM attraction WHERE name = 'Thunder Tower'), 18, NULL),
    ('ATTRACTION_QUEUE_JOIN', NOW() - INTERVAL 30 MINUTE, (SELECT id FROM attraction WHERE name = 'Thunder Tower'), 22, NULL);

-- Queue events for Splash Mountain
INSERT INTO park_event (event_type, timestamp, attraction_id, queue_size, metadata)
VALUES
    ('ATTRACTION_QUEUE_JOIN', NOW() - INTERVAL 1 HOUR - INTERVAL 30 MINUTE, (SELECT id FROM attraction WHERE name = 'Splash Mountain'), 30, NULL),
    ('ATTRACTION_QUEUE_JOIN', NOW() - INTERVAL 45 MINUTE, (SELECT id FROM attraction WHERE name = 'Splash Mountain'), 55, NULL),
    ('ATTRACTION_QUEUE_JOIN', NOW() - INTERVAL 20 MINUTE, (SELECT id FROM attraction WHERE name = 'Splash Mountain'), 65, NULL);

INSERT INTO park_event (event_type, timestamp, attraction_id, queue_size, metadata)
VALUES
    ('ATTRACTION_QUEUE_JOIN', NOW() - INTERVAL 1 HOUR, (SELECT id FROM attraction WHERE name = 'Giant Wheel'), 8, NULL),
    ('ATTRACTION_QUEUE_JOIN', NOW() - INTERVAL 30 MINUTE, (SELECT id FROM attraction WHERE name = 'Giant Wheel'), 12, NULL);

INSERT INTO park_alert (alert_type, severity, message, suggestion, attraction_id, timestamp, is_active, resolved_at)
VALUES
    ('HIGH_QUEUE', 'WARNING', 'Cola alta: más de 50 personas esperando', 'Considerar abrir más puertas o redirigir personal',
     (SELECT id FROM attraction WHERE name = 'Splash Mountain'), NOW() - INTERVAL 25 MINUTE, TRUE, NULL),
    ('HIGH_QUEUE', 'WARNING', 'Cola alta: más de 50 personas esperando', 'Considerar abrir más puertas o redirigir personal',
     (SELECT id FROM attraction WHERE name = 'Dragon Fury'), NOW() - INTERVAL 40 MINUTE, TRUE, NULL);

INSERT INTO park_alert (alert_type, severity, message, suggestion, attraction_id, timestamp, is_active, resolved_at)
VALUES
    ('ATTRACTION_DOWN', 'CRITICAL', 'Atracción cerrada por mantenimiento', 'Equipo técnico notificado',
     (SELECT id FROM attraction WHERE name = 'Velocity Coaster'), NOW() - INTERVAL 4 HOUR, FALSE, NOW() - INTERVAL 2 HOUR),
    ('HIGH_QUEUE', 'WARNING', 'Cola alta detectada', 'Se redirigió personal adicional',
     (SELECT id FROM attraction WHERE name = 'Thunder Tower'), NOW() - INTERVAL 3 HOUR, FALSE, NOW() - INTERVAL 2 HOUR - INTERVAL 30 MINUTE);

-- Park Zones
INSERT INTO park_zone (zone_name, description) VALUES
    ('THRILL_ZONE', 'Extreme attractions zone with roller coasters and drop towers'),
    ('WATER_ZONE', 'Water rides and aquatic attractions'),
    ('FAMILY_ZONE', 'Family-friendly attractions for all ages'),
    ('ADVENTURE_ZONE', 'Adventure and themed experiences'),
    ('PANORAMIC_ZONE', 'Scenic rides with park views');

-- Assign attractions to zones
UPDATE attraction SET zone_id = (SELECT id FROM park_zone WHERE zone_name = 'THRILL_ZONE')
WHERE name IN ('Dragon Fury', 'Thunder Tower', 'Velocity Coaster', 'Storm Chaser');

UPDATE attraction SET zone_id = (SELECT id FROM park_zone WHERE zone_name = 'WATER_ZONE')
WHERE name IN ('Splash Mountain', 'Pirate Rapids', 'Wild River');

UPDATE attraction SET zone_id = (SELECT id FROM park_zone WHERE zone_name = 'FAMILY_ZONE')
WHERE name IN ('Magic Carousel', 'Mini Carousel', 'Bumper Kingdom', 'Flying Carpet', 'Teacups Spin');

UPDATE attraction SET zone_id = (SELECT id FROM park_zone WHERE zone_name = 'ADVENTURE_ZONE')
WHERE name IN ('Haunted Manor', 'Sky Swings', 'Space Journey');

UPDATE attraction SET zone_id = (SELECT id FROM park_zone WHERE zone_name = 'PANORAMIC_ZONE')
WHERE name IN ('Enchanted Train', 'Giant Wheel');

-- Employees
INSERT INTO employee (first_name, last_name, email, phone, role, status, hire_date) VALUES
    -- Operators (17 - one per attraction + extras)
    ('Carlos', 'Martinez', 'carlos.martinez@magicworld.com', '612345001', 'OPERATOR', 'ACTIVE', '2024-01-15'),
    ('Ana', 'Garcia', 'ana.garcia@magicworld.com', '612345002', 'OPERATOR', 'ACTIVE', '2024-02-01'),
    ('Pedro', 'Lopez', 'pedro.lopez@magicworld.com', '612345003', 'OPERATOR', 'ACTIVE', '2024-01-20'),
    ('Maria', 'Sanchez', 'maria.sanchez@magicworld.com', '612345004', 'OPERATOR', 'ACTIVE', '2024-03-10'),
    ('Juan', 'Fernandez', 'juan.fernandez@magicworld.com', '612345005', 'OPERATOR', 'ACTIVE', '2024-02-15'),
    ('Laura', 'Diaz', 'laura.diaz@magicworld.com', '612345006', 'OPERATOR', 'ACTIVE', '2024-04-01'),
    ('Miguel', 'Torres', 'miguel.torres@magicworld.com', '612345007', 'OPERATOR', 'ACTIVE', '2024-01-10'),
    ('Sofia', 'Ruiz', 'sofia.ruiz@magicworld.com', '612345008', 'OPERATOR', 'ACTIVE', '2024-05-01'),
    ('David', 'Moreno', 'david.moreno@magicworld.com', '612345009', 'OPERATOR', 'ACTIVE', '2024-03-15'),
    ('Elena', 'Jimenez', 'elena.jimenez@magicworld.com', '612345010', 'OPERATOR', 'ACTIVE', '2024-02-20'),
    ('Pablo', 'Alvarez', 'pablo.alvarez@magicworld.com', '612345011', 'OPERATOR', 'ACTIVE', '2024-04-15'),
    ('Carmen', 'Romero', 'carmen.romero@magicworld.com', '612345012', 'OPERATOR', 'ACTIVE', '2024-01-25'),
    ('Jorge', 'Navarro', 'jorge.navarro@magicworld.com', '612345013', 'OPERATOR', 'ACTIVE', '2024-06-01'),
    ('Lucia', 'Gutierrez', 'lucia.gutierrez@magicworld.com', '612345014', 'OPERATOR', 'ACTIVE', '2024-03-20'),
    ('Alberto', 'Serrano', 'alberto.serrano@magicworld.com', '612345015', 'OPERATOR', 'ACTIVE', '2024-05-10'),
    ('Marta', 'Castro', 'marta.castro@magicworld.com', '612345016', 'OPERATOR', 'ACTIVE', '2024-02-10'),
    ('Fernando', 'Ortiz', 'fernando.ortiz@magicworld.com', '612345017', 'OPERATOR', 'ACTIVE', '2024-04-20'),
    ('Isabel', 'Munoz', 'isabel.munoz@magicworld.com', '612345018', 'OPERATOR', 'ACTIVE', '2024-07-01'),
    ('Raul', 'Blanco', 'raul.blanco@magicworld.com', '612345019', 'OPERATOR', 'ACTIVE', '2024-06-15'),
    ('Patricia', 'Herrera', 'patricia.herrera@magicworld.com', '612345020', 'OPERATOR', 'ACTIVE', '2024-08-01'),
    -- Security (6 - one per zone + extra)
    ('Antonio', 'Vega', 'antonio.vega@magicworld.com', '612345021', 'SECURITY', 'ACTIVE', '2023-11-01'),
    ('Rosa', 'Molina', 'rosa.molina@magicworld.com', '612345022', 'SECURITY', 'ACTIVE', '2024-01-05'),
    ('Francisco', 'Ramos', 'francisco.ramos@magicworld.com', '612345023', 'SECURITY', 'ACTIVE', '2024-02-28'),
    ('Teresa', 'Santos', 'teresa.santos@magicworld.com', '612345024', 'SECURITY', 'ACTIVE', '2024-03-25'),
    ('Manuel', 'Gil', 'manuel.gil@magicworld.com', '612345025', 'SECURITY', 'ACTIVE', '2024-05-20'),
    ('Cristina', 'Perez', 'cristina.perez@magicworld.com', '612345026', 'SECURITY', 'ACTIVE', '2024-04-10'),
    -- Medical (3)
    ('Dr. Roberto', 'Mendez', 'roberto.mendez@magicworld.com', '612345027', 'MEDICAL', 'ACTIVE', '2023-06-01'),
    ('Dra. Angela', 'Flores', 'angela.flores@magicworld.com', '612345028', 'MEDICAL', 'ACTIVE', '2024-01-15'),
    ('Enf. Diego', 'Luna', 'diego.luna@magicworld.com', '612345029', 'MEDICAL', 'ACTIVE', '2024-07-10'),
    -- Maintenance (4)
    ('Ricardo', 'Cabrera', 'ricardo.cabrera@magicworld.com', '612345030', 'MAINTENANCE', 'ACTIVE', '2023-09-01'),
    ('Silvia', 'Campos', 'silvia.campos@magicworld.com', '612345031', 'MAINTENANCE', 'ACTIVE', '2024-02-05'),
    ('Oscar', 'Reyes', 'oscar.reyes@magicworld.com', '612345032', 'MAINTENANCE', 'ACTIVE', '2024-05-25'),
    ('Beatriz', 'Medina', 'beatriz.medina@magicworld.com', '612345033', 'MAINTENANCE', 'ACTIVE', '2024-08-15'),
    -- Guest Services (4)
    ('Alejandra', 'Vargas', 'alejandra.vargas@magicworld.com', '612345034', 'GUEST_SERVICES', 'ACTIVE', '2024-01-08'),
    ('Victor', 'Ibarra', 'victor.ibarra@magicworld.com', '612345035', 'GUEST_SERVICES', 'ACTIVE', '2024-03-12'),
    ('Natalia', 'Aguilar', 'natalia.aguilar@magicworld.com', '612345036', 'GUEST_SERVICES', 'ACTIVE', '2024-06-20'),
    ('Sergio', 'Fuentes', 'sergio.fuentes@magicworld.com', '612345037', 'GUEST_SERVICES', 'ACTIVE', '2024-09-01');

-- Additional employees for reinforcement pool
INSERT INTO employee (first_name, last_name, email, phone, role, status, hire_date) VALUES
    -- Extra Security
    ('Marcos', 'Rios', 'marcos.rios@magicworld.com', '612345038', 'SECURITY', 'ACTIVE', '2024-10-01'),
    ('Daniela', 'Soto', 'daniela.soto@magicworld.com', '612345039', 'SECURITY', 'ACTIVE', '2024-11-15'),
    ('Emilio', 'Cruz', 'emilio.cruz@magicworld.com', '612345040', 'SECURITY', 'ACTIVE', '2025-01-10'),
    -- Extra Medical
    ('Dr. Andres', 'Pineda', 'andres.pineda@magicworld.com', '612345041', 'MEDICAL', 'ACTIVE', '2024-09-20'),
    ('Enf. Carolina', 'Espinoza', 'carolina.espinoza@magicworld.com', '612345042', 'MEDICAL', 'ACTIVE', '2025-02-01'),
    -- Extra Maintenance
    ('Hector', 'Cordero', 'hector.cordero@magicworld.com', '612345043', 'MAINTENANCE', 'ACTIVE', '2024-12-01'),
    ('Adriana', 'Pacheco', 'adriana.pacheco@magicworld.com', '612345044', 'MAINTENANCE', 'ACTIVE', '2025-03-15'),
    -- Extra Guest Services
    ('Felipe', 'Miranda', 'felipe.miranda@magicworld.com', '612345045', 'GUEST_SERVICES', 'ACTIVE', '2024-11-01'),
    ('Lorena', 'Rivas', 'lorena.rivas@magicworld.com', '612345046', 'GUEST_SERVICES', 'ACTIVE', '2025-01-20'),
    -- Extra Operators for reinforcement
    ('Gabriel', 'Paredes', 'gabriel.paredes@magicworld.com', '612345047', 'OPERATOR', 'ACTIVE', '2025-02-10'),
    ('Valeria', 'Montes', 'valeria.montes@magicworld.com', '612345048', 'OPERATOR', 'ACTIVE', '2025-03-01'),
    ('Ignacio', 'Rojas', 'ignacio.rojas@magicworld.com', '612345049', 'OPERATOR', 'ACTIVE', '2025-04-01'),
    ('Camila', 'Pena', 'camila.pena@magicworld.com', '612345050', 'OPERATOR', 'ACTIVE', '2025-05-01');

-- Weekly schedules for current and next week.
-- This follows the same principles as ScheduleService.autoAssignWeek:
--  - two rest days per employee (offset and offset+3)
--  - rotating assignments for operators and security
--  - full coverage of attractions/zones with seeded staffing counts
--  - overtime only for explicit reinforcement entries
SET @monday := DATE_SUB(CURDATE(), INTERVAL (DAYOFWEEK(CURDATE()) - 2 + 7) % 7 DAY);
DELETE FROM weekly_schedule WHERE week_start_date IN (@monday, DATE_ADD(@monday, INTERVAL 7 DAY));

DROP TEMPORARY TABLE IF EXISTS tmp_schedule_weeks;
CREATE TEMPORARY TABLE tmp_schedule_weeks (
  week_start DATE PRIMARY KEY
);
INSERT INTO tmp_schedule_weeks (week_start)
VALUES (@monday), (DATE_ADD(@monday, INTERVAL 7 DAY));

DROP TEMPORARY TABLE IF EXISTS tmp_schedule_days;
CREATE TEMPORARY TABLE tmp_schedule_days (
  day_index INT PRIMARY KEY,
  day_name VARCHAR(10) NOT NULL
);
INSERT INTO tmp_schedule_days (day_index, day_name)
VALUES
  (0, 'MONDAY'),
  (1, 'TUESDAY'),
  (2, 'WEDNESDAY'),
  (3, 'THURSDAY'),
  (4, 'FRIDAY'),
  (5, 'SATURDAY'),
  (6, 'SUNDAY');

DROP TEMPORARY TABLE IF EXISTS tmp_active_attractions;
CREATE TEMPORARY TABLE tmp_active_attractions AS
SELECT
  a.id AS attraction_id,
  ROW_NUMBER() OVER (ORDER BY a.id) - 1 AS attraction_idx
FROM attraction a
WHERE a.is_active = TRUE;

DROP TEMPORARY TABLE IF EXISTS tmp_active_zones;
CREATE TEMPORARY TABLE tmp_active_zones AS
SELECT
  z.id AS zone_id,
  ROW_NUMBER() OVER (ORDER BY z.id) - 1 AS zone_idx
FROM park_zone z;

SET @attraction_count := (SELECT COUNT(*) FROM tmp_active_attractions);
SET @zone_count := (SELECT COUNT(*) FROM tmp_active_zones);

-- OPERATORS (offset 0)
DROP TEMPORARY TABLE IF EXISTS tmp_operator_employees;
CREATE TEMPORARY TABLE tmp_operator_employees AS
SELECT
  e.id AS employee_id,
  ROW_NUMBER() OVER (ORDER BY e.id) - 1 AS emp_idx
FROM employee e
WHERE e.role = 'OPERATOR' AND e.status = 'ACTIVE';

DROP TEMPORARY TABLE IF EXISTS tmp_operator_slots;
CREATE TEMPORARY TABLE tmp_operator_slots AS
SELECT
  w.week_start,
  d.day_index,
  d.day_name,
  oe.employee_id,
  oe.emp_idx,
  ROW_NUMBER() OVER (PARTITION BY w.week_start, d.day_index ORDER BY oe.emp_idx) - 1 AS available_idx
FROM tmp_schedule_weeks w
CROSS JOIN tmp_schedule_days d
JOIN tmp_operator_employees oe
  ON d.day_index <> MOD(oe.emp_idx, 7)
 AND d.day_index <> MOD(oe.emp_idx + 3, 7);

INSERT INTO weekly_schedule (employee_id, week_start_date, day_of_week, shift, break_group, attraction_id, zone_id)
SELECT
  os.employee_id,
  os.week_start,
  os.day_name,
  'FULL_DAY',
  CASE MOD(os.emp_idx, 4)
    WHEN 0 THEN 'A'
    WHEN 1 THEN 'B'
    WHEN 2 THEN 'C'
    ELSE 'D'
  END AS break_group,
  aa.attraction_id,
  NULL
FROM tmp_operator_slots os
JOIN tmp_active_attractions aa
  ON aa.attraction_idx = MOD(os.available_idx + os.day_index, NULLIF(@attraction_count, 0))
WHERE @attraction_count > 0;

-- SECURITY (offset 1)
DROP TEMPORARY TABLE IF EXISTS tmp_security_employees;
CREATE TEMPORARY TABLE tmp_security_employees AS
SELECT
  e.id AS employee_id,
  ROW_NUMBER() OVER (ORDER BY e.id) - 1 AS emp_idx
FROM employee e
WHERE e.role = 'SECURITY' AND e.status = 'ACTIVE';

DROP TEMPORARY TABLE IF EXISTS tmp_security_slots;
CREATE TEMPORARY TABLE tmp_security_slots AS
SELECT
  w.week_start,
  d.day_index,
  d.day_name,
  se.employee_id,
  se.emp_idx,
  ROW_NUMBER() OVER (PARTITION BY w.week_start, d.day_index ORDER BY se.emp_idx) - 1 AS available_idx
FROM tmp_schedule_weeks w
CROSS JOIN tmp_schedule_days d
JOIN tmp_security_employees se
  ON d.day_index <> MOD(se.emp_idx + 1, 7)
 AND d.day_index <> MOD(se.emp_idx + 4, 7);

INSERT INTO weekly_schedule (employee_id, week_start_date, day_of_week, shift, break_group, attraction_id, zone_id)
SELECT
  ss.employee_id,
  ss.week_start,
  ss.day_name,
  'FULL_DAY',
  CASE MOD(ss.emp_idx, 4)
    WHEN 0 THEN 'A'
    WHEN 1 THEN 'B'
    WHEN 2 THEN 'C'
    ELSE 'D'
  END AS break_group,
  NULL,
  az.zone_id
FROM tmp_security_slots ss
JOIN tmp_active_zones az
  ON az.zone_idx = MOD(ss.available_idx + ss.day_index, NULLIF(@zone_count, 0))
WHERE @zone_count > 0;

-- MEDICAL (offset 2)
DROP TEMPORARY TABLE IF EXISTS tmp_medical_employees;
CREATE TEMPORARY TABLE tmp_medical_employees AS
SELECT
  e.id AS employee_id,
  ROW_NUMBER() OVER (ORDER BY e.id) - 1 AS emp_idx
FROM employee e
WHERE e.role = 'MEDICAL' AND e.status = 'ACTIVE';

INSERT INTO weekly_schedule (employee_id, week_start_date, day_of_week, shift, break_group, attraction_id, zone_id)
SELECT
  me.employee_id,
  w.week_start,
  d.day_name,
  'FULL_DAY',
  CASE MOD(me.emp_idx, 4)
    WHEN 0 THEN 'A'
    WHEN 1 THEN 'B'
    WHEN 2 THEN 'C'
    ELSE 'D'
  END AS break_group,
  NULL,
  NULL
FROM tmp_schedule_weeks w
CROSS JOIN tmp_schedule_days d
JOIN tmp_medical_employees me
  ON d.day_index <> MOD(me.emp_idx + 2, 7)
 AND d.day_index <> MOD(me.emp_idx + 5, 7);

-- MAINTENANCE (offset 3)
DROP TEMPORARY TABLE IF EXISTS tmp_maintenance_employees;
CREATE TEMPORARY TABLE tmp_maintenance_employees AS
SELECT
  e.id AS employee_id,
  ROW_NUMBER() OVER (ORDER BY e.id) - 1 AS emp_idx
FROM employee e
WHERE e.role = 'MAINTENANCE' AND e.status = 'ACTIVE';

INSERT INTO weekly_schedule (employee_id, week_start_date, day_of_week, shift, break_group, attraction_id, zone_id)
SELECT
  me.employee_id,
  w.week_start,
  d.day_name,
  'FULL_DAY',
  CASE MOD(me.emp_idx, 4)
    WHEN 0 THEN 'A'
    WHEN 1 THEN 'B'
    WHEN 2 THEN 'C'
    ELSE 'D'
  END AS break_group,
  NULL,
  NULL
FROM tmp_schedule_weeks w
CROSS JOIN tmp_schedule_days d
JOIN tmp_maintenance_employees me
  ON d.day_index <> MOD(me.emp_idx + 3, 7)
 AND d.day_index <> MOD(me.emp_idx + 6, 7);

-- GUEST_SERVICES (offset 4)
DROP TEMPORARY TABLE IF EXISTS tmp_guest_services_employees;
CREATE TEMPORARY TABLE tmp_guest_services_employees AS
SELECT
  e.id AS employee_id,
  ROW_NUMBER() OVER (ORDER BY e.id) - 1 AS emp_idx
FROM employee e
WHERE e.role = 'GUEST_SERVICES' AND e.status = 'ACTIVE';

INSERT INTO weekly_schedule (employee_id, week_start_date, day_of_week, shift, break_group, attraction_id, zone_id)
SELECT
  ge.employee_id,
  w.week_start,
  d.day_name,
  'FULL_DAY',
  CASE MOD(ge.emp_idx, 4)
    WHEN 0 THEN 'A'
    WHEN 1 THEN 'B'
    WHEN 2 THEN 'C'
    ELSE 'D'
  END AS break_group,
  NULL,
  NULL
FROM tmp_schedule_weeks w
CROSS JOIN tmp_schedule_days d
JOIN tmp_guest_services_employees ge
  ON d.day_index <> MOD(ge.emp_idx + 4, 7)
 AND d.day_index <> MOD(ge.emp_idx + 7, 7);

-- Seed one realistic reinforcement day as overtime (same shape as DailyOperationsService).
SET @today_name := CASE DAYOFWEEK(CURDATE())
  WHEN 2 THEN 'MONDAY'
  WHEN 3 THEN 'TUESDAY'
  WHEN 4 THEN 'WEDNESDAY'
  WHEN 5 THEN 'THURSDAY'
  WHEN 6 THEN 'FRIDAY'
  WHEN 7 THEN 'SATURDAY'
  ELSE 'SUNDAY'
END;

INSERT INTO weekly_schedule (
  employee_id,
  week_start_date,
  day_of_week,
  shift,
  break_group,
  attraction_id,
  zone_id,
  is_overtime,
  is_reinforcement
)
SELECT
  e.id,
  @monday,
  @today_name,
  'FULL_DAY',
  'D',
  NULL,
  NULL,
  TRUE,
  TRUE
FROM employee e
WHERE e.role = 'OPERATOR'
  AND e.status = 'ACTIVE'
  AND NOT EXISTS (
    SELECT 1
    FROM weekly_schedule ws
    WHERE ws.employee_id = e.id
      AND ws.week_start_date = @monday
      AND ws.day_of_week = @today_name
  )
ORDER BY e.id
LIMIT 1;

INSERT INTO reinforcement_call (employee_id, call_time, alert_id, status, response_time, arrival_time, is_overtime)
SELECT
  ws.employee_id,
  NOW() - INTERVAL 40 MINUTE,
  (SELECT pa.id FROM park_alert pa WHERE pa.is_active = TRUE ORDER BY pa.timestamp DESC LIMIT 1),
  'ACCEPTED',
  NOW() - INTERVAL 35 MINUTE,
  NOW() - INTERVAL 25 MINUTE,
  TRUE
FROM weekly_schedule ws
WHERE ws.week_start_date = @monday
  AND ws.day_of_week = @today_name
  AND ws.is_reinforcement = TRUE
ORDER BY ws.id DESC
LIMIT 1;

DROP TEMPORARY TABLE IF EXISTS tmp_guest_services_employees;
DROP TEMPORARY TABLE IF EXISTS tmp_maintenance_employees;
DROP TEMPORARY TABLE IF EXISTS tmp_medical_employees;
DROP TEMPORARY TABLE IF EXISTS tmp_security_slots;
DROP TEMPORARY TABLE IF EXISTS tmp_security_employees;
DROP TEMPORARY TABLE IF EXISTS tmp_operator_slots;
DROP TEMPORARY TABLE IF EXISTS tmp_operator_employees;
DROP TEMPORARY TABLE IF EXISTS tmp_active_zones;
DROP TEMPORARY TABLE IF EXISTS tmp_active_attractions;
DROP TEMPORARY TABLE IF EXISTS tmp_schedule_days;
DROP TEMPORARY TABLE IF EXISTS tmp_schedule_weeks;

-- Bulk ticket purchases for today and future dates (simulating varying occupancy)
-- These create purchase_lines with valid_date in the future for capacity simulation

-- User for bulk purchases
INSERT INTO users (username, firstname, lastname, email, password, user_role)
SELECT 'bulk_buyer', 'Bulk', 'Buyer', 'bulk@magicworld.com', '$2a$10$npnS7VQwMJgzjsVSOe6YLuCfUZSh7Lql2OPjZXedTnhgaEih7GxMa', 'USER'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'bulk_buyer');

-- Create purchases for future dates
INSERT INTO purchase (purchase_date, buyer_id)
SELECT dates.d, (SELECT id FROM users WHERE username = 'bulk_buyer')
FROM (
    -- March 2026 (high season)
    SELECT '2026-03-01' AS d UNION ALL SELECT '2026-03-05' UNION ALL SELECT '2026-03-10' UNION ALL
    SELECT '2026-03-15' UNION ALL SELECT '2026-03-20' UNION ALL SELECT '2026-03-25' UNION ALL
    -- April 2026 (peak)
    SELECT '2026-04-01' UNION ALL SELECT '2026-04-05' UNION ALL SELECT '2026-04-10' UNION ALL
    SELECT '2026-04-15' UNION ALL SELECT '2026-04-20' UNION ALL SELECT '2026-04-25' UNION ALL
    -- May 2026
    SELECT '2026-05-01' UNION ALL SELECT '2026-05-10' UNION ALL SELECT '2026-05-15' UNION ALL
    SELECT '2026-05-20' UNION ALL SELECT '2026-05-25' UNION ALL
    -- June 2026
    SELECT '2026-06-01' UNION ALL SELECT '2026-06-10' UNION ALL SELECT '2026-06-15' UNION ALL
    SELECT '2026-06-20' UNION ALL SELECT '2026-06-25'
) dates;

-- March: high season (200 per day)
INSERT INTO purchase_line (valid_date, quantity, purchase_id, total_cost, ticket_type_name)
SELECT p.purchase_date, 130, p.id, 130 * t.cost, 'Adult'
FROM purchase p JOIN users u ON p.buyer_id = u.id JOIN ticket_type t ON t.type_name = 'Adult'
WHERE u.username = 'bulk_buyer' AND p.purchase_date BETWEEN '2026-03-01' AND '2026-03-31';

INSERT INTO purchase_line (valid_date, quantity, purchase_id, total_cost, ticket_type_name)
SELECT p.purchase_date, 70, p.id, 70 * t.cost, 'Child'
FROM purchase p JOIN users u ON p.buyer_id = u.id JOIN ticket_type t ON t.type_name = 'Child'
WHERE u.username = 'bulk_buyer' AND p.purchase_date BETWEEN '2026-03-01' AND '2026-03-31';

-- April: peak season (350 per day)
INSERT INTO purchase_line (valid_date, quantity, purchase_id, total_cost, ticket_type_name)
SELECT p.purchase_date, 230, p.id, 230 * t.cost, 'Adult'
FROM purchase p JOIN users u ON p.buyer_id = u.id JOIN ticket_type t ON t.type_name = 'Adult'
WHERE u.username = 'bulk_buyer' AND p.purchase_date BETWEEN '2026-04-01' AND '2026-04-30';

INSERT INTO purchase_line (valid_date, quantity, purchase_id, total_cost, ticket_type_name)
SELECT p.purchase_date, 120, p.id, 120 * t.cost, 'Child'
FROM purchase p JOIN users u ON p.buyer_id = u.id JOIN ticket_type t ON t.type_name = 'Child'
WHERE u.username = 'bulk_buyer' AND p.purchase_date BETWEEN '2026-04-01' AND '2026-04-30';

-- May-June: moderate season (200 per day)
INSERT INTO purchase_line (valid_date, quantity, purchase_id, total_cost, ticket_type_name)
SELECT p.purchase_date, 130, p.id, 130 * t.cost, 'Adult'
FROM purchase p JOIN users u ON p.buyer_id = u.id JOIN ticket_type t ON t.type_name = 'Adult'
WHERE u.username = 'bulk_buyer' AND p.purchase_date BETWEEN '2026-05-01' AND '2026-06-30';

INSERT INTO purchase_line (valid_date, quantity, purchase_id, total_cost, ticket_type_name)
SELECT p.purchase_date, 70, p.id, 70 * t.cost, 'Child'
FROM purchase p JOIN users u ON p.buyer_id = u.id JOIN ticket_type t ON t.type_name = 'Child'
WHERE u.username = 'bulk_buyer' AND p.purchase_date BETWEEN '2026-05-01' AND '2026-06-30';

-- Recent bulk_buyer purchases (last 2 weeks — complements user1's 300/day up to ~450)
INSERT INTO purchase (purchase_date, buyer_id)
SELECT dates.d, (SELECT id FROM users WHERE username = 'bulk_buyer')
FROM (
    SELECT CURDATE() - INTERVAL 12 DAY AS d UNION ALL SELECT CURDATE() - INTERVAL 11 DAY
    UNION ALL SELECT CURDATE() - INTERVAL 10 DAY UNION ALL SELECT CURDATE() - INTERVAL 9 DAY
    UNION ALL SELECT CURDATE() - INTERVAL 8 DAY UNION ALL SELECT CURDATE() - INTERVAL 7 DAY
    UNION ALL SELECT CURDATE() - INTERVAL 6 DAY UNION ALL SELECT CURDATE() - INTERVAL 5 DAY
    UNION ALL SELECT CURDATE() - INTERVAL 4 DAY UNION ALL SELECT CURDATE() - INTERVAL 3 DAY
    UNION ALL SELECT CURDATE() - INTERVAL 2 DAY UNION ALL SELECT CURDATE() - INTERVAL 1 DAY
    UNION ALL SELECT CURDATE()
) dates;

INSERT INTO purchase_line (valid_date, quantity, purchase_id, total_cost, ticket_type_name)
SELECT p.purchase_date, 100, p.id, 100 * t.cost, 'Adult'
FROM purchase p JOIN users u ON p.buyer_id = u.id JOIN ticket_type t ON t.type_name = 'Adult'
WHERE u.username = 'bulk_buyer' AND p.purchase_date >= CURDATE() - INTERVAL 12 DAY AND p.purchase_date <= CURDATE();

INSERT INTO purchase_line (valid_date, quantity, purchase_id, total_cost, ticket_type_name)
SELECT p.purchase_date, 50, p.id, 50 * t.cost, 'Child'
FROM purchase p JOIN users u ON p.buyer_id = u.id JOIN ticket_type t ON t.type_name = 'Child'
WHERE u.username = 'bulk_buyer' AND p.purchase_date >= CURDATE() - INTERVAL 12 DAY AND p.purchase_date <= CURDATE();

-- Add is_overtime column defaults for existing data
-- (Hibernate will auto-create the column; existing records default to false)

-- Seasonal Pricing Rules (admin-managed, these are default initial rules)
INSERT INTO seasonal_pricing (name, start_date, end_date, multiplier, apply_on_weekdays, apply_on_weekends) VALUES
    ('Recargo fin de semana', '2026-01-01', '2026-12-31', 1.25, FALSE, TRUE),
    ('Temporada verano', '2026-06-01', '2026-08-31', 1.30, TRUE, TRUE);

-- Park Closure Days - Festivos Andalucia 2026 + Octubre + Enero cerrado
-- Note: only dates at least 2 months from now (2026-03-06) are inserted for validation
INSERT INTO park_closure_day (closure_date, reason) VALUES
    -- Festivos Andalucia 2026
    ('2026-06-01', 'Dia del Trabajador (puente)'),
    ('2026-08-15', 'Asuncion de la Virgen'),
    ('2026-10-12', 'Fiesta Nacional de Espana'),
    ('2026-11-01', 'Dia de Todos los Santos'),
    ('2026-12-06', 'Dia de la Constitucion'),
    ('2026-12-08', 'Inmaculada Concepcion'),
    ('2026-12-25', 'Navidad'),
    -- Octubre 2026 cerrado completo
    ('2026-10-01', 'Cierre octubre - mantenimiento'),
    ('2026-10-02', 'Cierre octubre - mantenimiento'),
    ('2026-10-03', 'Cierre octubre - mantenimiento'),
    ('2026-10-04', 'Cierre octubre - mantenimiento'),
    ('2026-10-05', 'Cierre octubre - mantenimiento'),
    ('2026-10-06', 'Cierre octubre - mantenimiento'),
    ('2026-10-07', 'Cierre octubre - mantenimiento'),
    ('2026-10-08', 'Cierre octubre - mantenimiento'),
    ('2026-10-09', 'Cierre octubre - mantenimiento'),
    ('2026-10-10', 'Cierre octubre - mantenimiento'),
    ('2026-10-11', 'Cierre octubre - mantenimiento'),
    ('2026-10-13', 'Cierre octubre - mantenimiento'),
    ('2026-10-14', 'Cierre octubre - mantenimiento'),
    ('2026-10-15', 'Cierre octubre - mantenimiento'),
    ('2026-10-16', 'Cierre octubre - mantenimiento'),
    ('2026-10-17', 'Cierre octubre - mantenimiento'),
    ('2026-10-18', 'Cierre octubre - mantenimiento'),
    ('2026-10-19', 'Cierre octubre - mantenimiento'),
    ('2026-10-20', 'Cierre octubre - mantenimiento'),
    ('2026-10-21', 'Cierre octubre - mantenimiento'),
    ('2026-10-22', 'Cierre octubre - mantenimiento'),
    ('2026-10-23', 'Cierre octubre - mantenimiento'),
    ('2026-10-24', 'Cierre octubre - mantenimiento'),
    ('2026-10-25', 'Cierre octubre - mantenimiento'),
    ('2026-10-26', 'Cierre octubre - mantenimiento'),
    ('2026-10-27', 'Cierre octubre - mantenimiento'),
    ('2026-10-28', 'Cierre octubre - mantenimiento'),
    ('2026-10-29', 'Cierre octubre - mantenimiento'),
    ('2026-10-30', 'Cierre octubre - mantenimiento'),
    ('2026-10-31', 'Cierre octubre - mantenimiento'),
    -- Enero 2027 cerrado completo
    ('2027-01-01', 'Cierre enero - mantenimiento'),
    ('2027-01-02', 'Cierre enero - mantenimiento'),
    ('2027-01-03', 'Cierre enero - mantenimiento'),
    ('2027-01-04', 'Cierre enero - mantenimiento'),
    ('2027-01-05', 'Cierre enero - mantenimiento'),
    ('2027-01-06', 'Cierre enero - mantenimiento'),
    ('2027-01-07', 'Cierre enero - mantenimiento'),
    ('2027-01-08', 'Cierre enero - mantenimiento'),
    ('2027-01-09', 'Cierre enero - mantenimiento'),
    ('2027-01-10', 'Cierre enero - mantenimiento'),
    ('2027-01-11', 'Cierre enero - mantenimiento'),
    ('2027-01-12', 'Cierre enero - mantenimiento'),
    ('2027-01-13', 'Cierre enero - mantenimiento'),
    ('2027-01-14', 'Cierre enero - mantenimiento'),
    ('2027-01-15', 'Cierre enero - mantenimiento'),
    ('2027-01-16', 'Cierre enero - mantenimiento'),
    ('2027-01-17', 'Cierre enero - mantenimiento'),
    ('2027-01-18', 'Cierre enero - mantenimiento'),
    ('2027-01-19', 'Cierre enero - mantenimiento'),
    ('2027-01-20', 'Cierre enero - mantenimiento'),
    ('2027-01-21', 'Cierre enero - mantenimiento'),
    ('2027-01-22', 'Cierre enero - mantenimiento'),
    ('2027-01-23', 'Cierre enero - mantenimiento'),
    ('2027-01-24', 'Cierre enero - mantenimiento'),
    ('2027-01-25', 'Cierre enero - mantenimiento'),
    ('2027-01-26', 'Cierre enero - mantenimiento'),
    ('2027-01-27', 'Cierre enero - mantenimiento'),
    ('2027-01-28', 'Cierre enero - mantenimiento'),
    ('2027-01-29', 'Cierre enero - mantenimiento'),
    ('2027-01-30', 'Cierre enero - mantenimiento'),
    ('2027-01-31', 'Cierre enero - mantenimiento');


-- =====================================================================
-- DAILY ASSIGNMENTS for current week (to power statistics/hours)
-- These simulate that employees actually worked their scheduled days
-- =====================================================================

INSERT INTO daily_assignment (employee_id, assignment_date, current_status, current_zone_id, current_attraction_id, break_group, is_overtime, break_start_time, break_end_time)
SELECT
  ws.employee_id,
  DATE_ADD(ws.week_start_date, INTERVAL (
    CASE ws.day_of_week
      WHEN 'MONDAY' THEN 0 WHEN 'TUESDAY' THEN 1 WHEN 'WEDNESDAY' THEN 2
      WHEN 'THURSDAY' THEN 3 WHEN 'FRIDAY' THEN 4 WHEN 'SATURDAY' THEN 5 WHEN 'SUNDAY' THEN 6
    END
  ) DAY) AS assignment_date,
  CASE
    WHEN DATE_ADD(ws.week_start_date, INTERVAL (
      CASE ws.day_of_week
        WHEN 'MONDAY' THEN 0 WHEN 'TUESDAY' THEN 1 WHEN 'WEDNESDAY' THEN 2
        WHEN 'THURSDAY' THEN 3 WHEN 'FRIDAY' THEN 4 WHEN 'SATURDAY' THEN 5 WHEN 'SUNDAY' THEN 6
      END
    ) DAY) < CURDATE() THEN 'FINISHED'
    WHEN DATE_ADD(ws.week_start_date, INTERVAL (
      CASE ws.day_of_week
        WHEN 'MONDAY' THEN 0 WHEN 'TUESDAY' THEN 1 WHEN 'WEDNESDAY' THEN 2
        WHEN 'THURSDAY' THEN 3 WHEN 'FRIDAY' THEN 4 WHEN 'SATURDAY' THEN 5 WHEN 'SUNDAY' THEN 6
      END
    ) DAY) = CURDATE() THEN 'WORKING'
    ELSE 'NOT_STARTED'
  END AS current_status,
  ws.zone_id,
  ws.attraction_id,
  ws.break_group,
  COALESCE(ws.is_overtime, FALSE),
  CASE ws.break_group
    WHEN 'A' THEN '12:00:00' WHEN 'B' THEN '12:30:00'
    WHEN 'C' THEN '13:00:00' WHEN 'D' THEN '13:30:00'
  END,
  CASE ws.break_group
    WHEN 'A' THEN '12:30:00' WHEN 'B' THEN '13:00:00'
    WHEN 'C' THEN '13:30:00' WHEN 'D' THEN '14:00:00'
  END
FROM weekly_schedule ws
WHERE ws.week_start_date = @monday
  AND DATE_ADD(ws.week_start_date, INTERVAL (
    CASE ws.day_of_week
      WHEN 'MONDAY' THEN 0 WHEN 'TUESDAY' THEN 1 WHEN 'WEDNESDAY' THEN 2
      WHEN 'THURSDAY' THEN 3 WHEN 'FRIDAY' THEN 4 WHEN 'SATURDAY' THEN 5 WHEN 'SUNDAY' THEN 6
    END
  ) DAY) <= CURDATE()
  AND NOT EXISTS (
    SELECT 1 FROM daily_assignment da
    WHERE da.employee_id = ws.employee_id
      AND da.assignment_date = DATE_ADD(ws.week_start_date, INTERVAL (
        CASE ws.day_of_week
          WHEN 'MONDAY' THEN 0 WHEN 'TUESDAY' THEN 1 WHEN 'WEDNESDAY' THEN 2
          WHEN 'THURSDAY' THEN 3 WHEN 'FRIDAY' THEN 4 WHEN 'SATURDAY' THEN 5 WHEN 'SUNDAY' THEN 6
        END
      ) DAY)
  );


-- =====================================================================
-- WORK LOG ENTRIES — Audit trail of admin adjustments
-- =====================================================================

INSERT INTO work_log (employee_id, target_date, action, hours_affected, is_overtime, reason, performed_by, created_at)
SELECT e.id, CURDATE() - INTERVAL 2 DAY, 'ADD_OVERTIME_HOURS', 4.00, TRUE,
       'Cubrir turno extra por alerta critica en Dragon Fury', 'admin', NOW() - INTERVAL 2 DAY
FROM employee e WHERE e.email = 'miguel.torres@magicworld.com';

INSERT INTO work_log (employee_id, target_date, action, hours_affected, is_overtime, reason, performed_by, created_at)
SELECT e.id, CURDATE() - INTERVAL 3 DAY, 'ADD_OVERTIME_HOURS', 4.00, TRUE,
       'Horas extra evento especial nocturno', 'admin', NOW() - INTERVAL 3 DAY
FROM employee e WHERE e.email = 'sofia.ruiz@magicworld.com';

INSERT INTO work_log (employee_id, target_date, action, hours_affected, is_overtime, reason, performed_by, created_at)
SELECT e.id, CURDATE() - INTERVAL 1 DAY, 'REMOVE_SCHEDULED_DAY', 7.50, FALSE,
       'Baja por enfermedad justificada', 'admin', NOW() - INTERVAL 1 DAY
FROM employee e WHERE e.email = 'david.moreno@magicworld.com';

INSERT INTO work_log (employee_id, target_date, action, hours_affected, is_overtime, reason, performed_by, created_at)
SELECT e.id, CURDATE() - INTERVAL 4 DAY, 'ADD_ABSENCE', 8.00, FALSE,
       'Falta injustificada - no se presento al turno', 'admin', NOW() - INTERVAL 4 DAY
FROM employee e WHERE e.email = 'jorge.navarro@magicworld.com';

INSERT INTO work_log (employee_id, target_date, action, hours_affected, is_overtime, reason, performed_by, created_at)
SELECT e.id, CURDATE() - INTERVAL 5 DAY, 'ADD_OVERTIME_HOURS', 3.00, TRUE,
       'Refuerzo de seguridad para evento VIP', 'admin', NOW() - INTERVAL 5 DAY
FROM employee e WHERE e.email = 'antonio.vega@magicworld.com';

INSERT INTO work_log (employee_id, target_date, action, hours_affected, is_overtime, reason, performed_by, created_at)
SELECT e.id, CURDATE() - INTERVAL 2 DAY, 'ADD_OVERTIME_HOURS', 5.00, TRUE,
       'Guardia medica extendida por incidencia', 'admin', NOW() - INTERVAL 2 DAY
FROM employee e WHERE e.email = 'roberto.mendez@magicworld.com';

INSERT INTO work_log (employee_id, target_date, action, hours_affected, is_overtime, reason, performed_by, created_at)
SELECT e.id, CURDATE() - INTERVAL 4 DAY, 'REMOVE_ABSENCE', 8.00, FALSE,
       'Correccion: la falta fue un error administrativo', 'admin', NOW() - INTERVAL 4 DAY
FROM employee e WHERE e.email = 'jorge.navarro@magicworld.com';

INSERT INTO work_log (employee_id, target_date, action, hours_affected, is_overtime, reason, performed_by, created_at)
SELECT e.id, CURDATE() - INTERVAL 5 DAY, 'ADD_OVERTIME_HOURS', 2.50, TRUE,
       'Mantenimiento urgente en Splash Mountain fuera de horario', 'admin', NOW() - INTERVAL 5 DAY
FROM employee e WHERE e.email = 'ricardo.cabrera@magicworld.com';


-- =====================================================================
-- ADDITIONAL PARK EVENTS — More queue data for attraction performance stats
-- =====================================================================

INSERT INTO park_event (event_type, timestamp, attraction_id, queue_size, metadata)
VALUES
    ('ATTRACTION_QUEUE_JOIN', NOW() - INTERVAL 3 HOUR, (SELECT id FROM attraction WHERE name = 'Velocity Coaster'), 20, NULL),
    ('ATTRACTION_QUEUE_JOIN', NOW() - INTERVAL 2 HOUR, (SELECT id FROM attraction WHERE name = 'Velocity Coaster'), 38, NULL),
    ('ATTRACTION_QUEUE_JOIN', NOW() - INTERVAL 1 HOUR, (SELECT id FROM attraction WHERE name = 'Velocity Coaster'), 55, NULL),
    ('ATTRACTION_QUEUE_JOIN', NOW() - INTERVAL 30 MINUTE, (SELECT id FROM attraction WHERE name = 'Velocity Coaster'), 62, NULL);

INSERT INTO park_event (event_type, timestamp, attraction_id, queue_size, metadata)
VALUES
    ('ATTRACTION_QUEUE_JOIN', NOW() - INTERVAL 2 HOUR - INTERVAL 30 MINUTE, (SELECT id FROM attraction WHERE name = 'Magic Carousel'), 12, NULL),
    ('ATTRACTION_QUEUE_JOIN', NOW() - INTERVAL 1 HOUR - INTERVAL 30 MINUTE, (SELECT id FROM attraction WHERE name = 'Magic Carousel'), 18, NULL),
    ('ATTRACTION_QUEUE_JOIN', NOW() - INTERVAL 45 MINUTE, (SELECT id FROM attraction WHERE name = 'Magic Carousel'), 22, NULL);

INSERT INTO park_event (event_type, timestamp, attraction_id, queue_size, metadata)
VALUES
    ('ATTRACTION_QUEUE_JOIN', NOW() - INTERVAL 2 HOUR, (SELECT id FROM attraction WHERE name = 'Pirate Rapids'), 25, NULL),
    ('ATTRACTION_QUEUE_JOIN', NOW() - INTERVAL 1 HOUR, (SELECT id FROM attraction WHERE name = 'Pirate Rapids'), 40, NULL),
    ('ATTRACTION_QUEUE_JOIN', NOW() - INTERVAL 20 MINUTE, (SELECT id FROM attraction WHERE name = 'Pirate Rapids'), 50, NULL);

INSERT INTO park_event (event_type, timestamp, attraction_id, queue_size, metadata)
VALUES
    ('ATTRACTION_QUEUE_JOIN', NOW() - INTERVAL 2 HOUR - INTERVAL 15 MINUTE, (SELECT id FROM attraction WHERE name = 'Haunted Manor'), 15, NULL),
    ('ATTRACTION_QUEUE_JOIN', NOW() - INTERVAL 1 HOUR - INTERVAL 15 MINUTE, (SELECT id FROM attraction WHERE name = 'Haunted Manor'), 28, NULL),
    ('ATTRACTION_QUEUE_JOIN', NOW() - INTERVAL 30 MINUTE, (SELECT id FROM attraction WHERE name = 'Haunted Manor'), 35, NULL);

INSERT INTO park_event (event_type, timestamp, attraction_id, queue_size, metadata)
VALUES
    ('ATTRACTION_QUEUE_JOIN', NOW() - INTERVAL 1 HOUR - INTERVAL 45 MINUTE, (SELECT id FROM attraction WHERE name = 'Sky Swings'), 18, NULL),
    ('ATTRACTION_QUEUE_JOIN', NOW() - INTERVAL 50 MINUTE, (SELECT id FROM attraction WHERE name = 'Sky Swings'), 30, NULL);

INSERT INTO park_event (event_type, timestamp, attraction_id, queue_size, metadata)
VALUES
    ('ATTRACTION_QUEUE_JOIN', NOW() - INTERVAL 2 HOUR - INTERVAL 30 MINUTE, (SELECT id FROM attraction WHERE name = 'Storm Chaser'), 22, NULL),
    ('ATTRACTION_QUEUE_JOIN', NOW() - INTERVAL 1 HOUR - INTERVAL 30 MINUTE, (SELECT id FROM attraction WHERE name = 'Storm Chaser'), 42, NULL),
    ('ATTRACTION_QUEUE_JOIN', NOW() - INTERVAL 40 MINUTE, (SELECT id FROM attraction WHERE name = 'Storm Chaser'), 58, NULL);

INSERT INTO park_event (event_type, timestamp, attraction_id, queue_size, metadata)
VALUES
    ('ATTRACTION_QUEUE_JOIN', NOW() - INTERVAL 2 HOUR, (SELECT id FROM attraction WHERE name = 'Space Journey'), 10, NULL),
    ('ATTRACTION_QUEUE_JOIN', NOW() - INTERVAL 1 HOUR, (SELECT id FROM attraction WHERE name = 'Space Journey'), 16, NULL);

INSERT INTO park_event (event_type, timestamp, visitor_count, queue_size, metadata)
VALUES
    ('PARK_ENTRY', NOW() - INTERVAL 4 HOUR, 1, NULL, NULL),
    ('PARK_ENTRY', NOW() - INTERVAL 3 HOUR - INTERVAL 30 MINUTE, 1, NULL, NULL),
    ('PARK_ENTRY', NOW() - INTERVAL 3 HOUR - INTERVAL 15 MINUTE, 1, NULL, NULL),
    ('PARK_ENTRY', NOW() - INTERVAL 2 HOUR - INTERVAL 45 MINUTE, 1, NULL, NULL),
    ('PARK_ENTRY', NOW() - INTERVAL 2 HOUR - INTERVAL 15 MINUTE, 1, NULL, NULL),
    ('PARK_ENTRY', NOW() - INTERVAL 1 HOUR - INTERVAL 45 MINUTE, 1, NULL, NULL),
    ('PARK_ENTRY', NOW() - INTERVAL 1 HOUR - INTERVAL 15 MINUTE, 1, NULL, NULL),
    ('PARK_ENTRY', NOW() - INTERVAL 50 MINUTE, 1, NULL, NULL),
    ('PARK_ENTRY', NOW() - INTERVAL 35 MINUTE, 1, NULL, NULL),
    ('PARK_ENTRY', NOW() - INTERVAL 10 MINUTE, 1, NULL, NULL);


-- =====================================================================
-- ADDITIONAL PURCHASE DATA — Past/future months for seasonality
-- =====================================================================

INSERT INTO users (username, firstname, lastname, email, password, user_role)
SELECT 'stats_buyer', 'Stats', 'Buyer', 'stats@magicworld.com', '$2a$10$npnS7VQwMJgzjsVSOe6YLuCfUZSh7Lql2OPjZXedTnhgaEih7GxMa', 'USER'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'stats_buyer');

-- February 2026 (low season — winter)
INSERT INTO purchase (purchase_date, buyer_id)
SELECT dates.d, (SELECT id FROM users WHERE username = 'stats_buyer')
FROM (
    SELECT '2026-02-01' AS d UNION ALL SELECT '2026-02-07' UNION ALL SELECT '2026-02-14' UNION ALL SELECT '2026-02-21'
) dates;

INSERT INTO purchase_line (valid_date, quantity, purchase_id, total_cost, ticket_type_name)
SELECT p.purchase_date, 40, p.id, 40 * t.cost, 'Adult'
FROM purchase p JOIN users u ON p.buyer_id = u.id JOIN ticket_type t ON t.type_name = 'Adult'
WHERE u.username = 'stats_buyer' AND p.purchase_date BETWEEN '2026-02-01' AND '2026-02-28';

INSERT INTO purchase_line (valid_date, quantity, purchase_id, total_cost, ticket_type_name)
SELECT p.purchase_date, 20, p.id, 20 * t.cost, 'Child'
FROM purchase p JOIN users u ON p.buyer_id = u.id JOIN ticket_type t ON t.type_name = 'Child'
WHERE u.username = 'stats_buyer' AND p.purchase_date BETWEEN '2026-02-01' AND '2026-02-28';

-- July 2026 (peak summer)
INSERT INTO purchase (purchase_date, buyer_id)
SELECT dates.d, (SELECT id FROM users WHERE username = 'stats_buyer')
FROM (
    SELECT '2026-07-01' AS d UNION ALL SELECT '2026-07-05' UNION ALL SELECT '2026-07-10' UNION ALL
    SELECT '2026-07-15' UNION ALL SELECT '2026-07-20' UNION ALL SELECT '2026-07-25' UNION ALL SELECT '2026-07-30'
) dates;

INSERT INTO purchase_line (valid_date, quantity, purchase_id, total_cost, ticket_type_name)
SELECT p.purchase_date, 280, p.id, 280 * t.cost, 'Adult'
FROM purchase p JOIN users u ON p.buyer_id = u.id JOIN ticket_type t ON t.type_name = 'Adult'
WHERE u.username = 'stats_buyer' AND p.purchase_date BETWEEN '2026-07-01' AND '2026-07-31';

INSERT INTO purchase_line (valid_date, quantity, purchase_id, total_cost, ticket_type_name)
SELECT p.purchase_date, 150, p.id, 150 * t.cost, 'Child'
FROM purchase p JOIN users u ON p.buyer_id = u.id JOIN ticket_type t ON t.type_name = 'Child'
WHERE u.username = 'stats_buyer' AND p.purchase_date BETWEEN '2026-07-01' AND '2026-07-31';

-- August 2026 (peak summer)
INSERT INTO purchase (purchase_date, buyer_id)
SELECT dates.d, (SELECT id FROM users WHERE username = 'stats_buyer')
FROM (
    SELECT '2026-08-01' AS d UNION ALL SELECT '2026-08-05' UNION ALL SELECT '2026-08-10' UNION ALL
    SELECT '2026-08-15' UNION ALL SELECT '2026-08-20' UNION ALL SELECT '2026-08-25'
) dates;

INSERT INTO purchase_line (valid_date, quantity, purchase_id, total_cost, ticket_type_name)
SELECT p.purchase_date, 280, p.id, 280 * t.cost, 'Adult'
FROM purchase p JOIN users u ON p.buyer_id = u.id JOIN ticket_type t ON t.type_name = 'Adult'
WHERE u.username = 'stats_buyer' AND p.purchase_date BETWEEN '2026-08-01' AND '2026-08-31';

INSERT INTO purchase_line (valid_date, quantity, purchase_id, total_cost, ticket_type_name)
SELECT p.purchase_date, 120, p.id, 120 * t.cost, 'Child'
FROM purchase p JOIN users u ON p.buyer_id = u.id JOIN ticket_type t ON t.type_name = 'Child'
WHERE u.username = 'stats_buyer' AND p.purchase_date BETWEEN '2026-08-01' AND '2026-08-31';

-- September 2026 (moderate — back to school)
INSERT INTO purchase (purchase_date, buyer_id)
SELECT dates.d, (SELECT id FROM users WHERE username = 'stats_buyer')
FROM (
    SELECT '2026-09-05' AS d UNION ALL SELECT '2026-09-12' UNION ALL SELECT '2026-09-19' UNION ALL SELECT '2026-09-26'
) dates;

INSERT INTO purchase_line (valid_date, quantity, purchase_id, total_cost, ticket_type_name)
SELECT p.purchase_date, 80, p.id, 80 * t.cost, 'Adult'
FROM purchase p JOIN users u ON p.buyer_id = u.id JOIN ticket_type t ON t.type_name = 'Adult'
WHERE u.username = 'stats_buyer' AND p.purchase_date BETWEEN '2026-09-01' AND '2026-09-30';

INSERT INTO purchase_line (valid_date, quantity, purchase_id, total_cost, ticket_type_name)
SELECT p.purchase_date, 30, p.id, 30 * t.cost, 'Child'
FROM purchase p JOIN users u ON p.buyer_id = u.id JOIN ticket_type t ON t.type_name = 'Child'
WHERE u.username = 'stats_buyer' AND p.purchase_date BETWEEN '2026-09-01' AND '2026-09-30';

-- November 2026 (low season)
INSERT INTO purchase (purchase_date, buyer_id)
SELECT dates.d, (SELECT id FROM users WHERE username = 'stats_buyer')
FROM (
    SELECT '2026-11-07' AS d UNION ALL SELECT '2026-11-14' UNION ALL SELECT '2026-11-21'
) dates;

INSERT INTO purchase_line (valid_date, quantity, purchase_id, total_cost, ticket_type_name)
SELECT p.purchase_date, 50, p.id, 50 * t.cost, 'Adult'
FROM purchase p JOIN users u ON p.buyer_id = u.id JOIN ticket_type t ON t.type_name = 'Adult'
WHERE u.username = 'stats_buyer' AND p.purchase_date BETWEEN '2026-11-01' AND '2026-11-30';

-- December 2026 (Christmas boost)
INSERT INTO purchase (purchase_date, buyer_id)
SELECT dates.d, (SELECT id FROM users WHERE username = 'stats_buyer')
FROM (
    SELECT '2026-12-20' AS d UNION ALL SELECT '2026-12-23' UNION ALL SELECT '2026-12-26' UNION ALL SELECT '2026-12-30'
) dates;

INSERT INTO purchase_line (valid_date, quantity, purchase_id, total_cost, ticket_type_name)
SELECT p.purchase_date, 180, p.id, 180 * t.cost, 'Adult'
FROM purchase p JOIN users u ON p.buyer_id = u.id JOIN ticket_type t ON t.type_name = 'Adult'
WHERE u.username = 'stats_buyer' AND p.purchase_date BETWEEN '2026-12-01' AND '2026-12-31';

INSERT INTO purchase_line (valid_date, quantity, purchase_id, total_cost, ticket_type_name)
SELECT p.purchase_date, 100, p.id, 100 * t.cost, 'Child'
FROM purchase p JOIN users u ON p.buyer_id = u.id JOIN ticket_type t ON t.type_name = 'Child'
WHERE u.username = 'stats_buyer' AND p.purchase_date BETWEEN '2026-12-01' AND '2026-12-31';

-- Normalize seeded daily ticket sales to the [300, 450] range.
DROP TEMPORARY TABLE IF EXISTS tmp_target_sales_dates;
CREATE TEMPORARY TABLE tmp_target_sales_dates AS
SELECT DISTINCT pl.valid_date
FROM purchase_line pl;

INSERT INTO purchase (purchase_date, buyer_id)
SELECT d.valid_date, u.id
FROM tmp_target_sales_dates d
JOIN users u ON u.username = 'bulk_buyer'
LEFT JOIN purchase p ON p.purchase_date = d.valid_date AND p.buyer_id = u.id
WHERE p.id IS NULL;

DROP TEMPORARY TABLE IF EXISTS tmp_target_sales_purchase;
CREATE TEMPORARY TABLE tmp_target_sales_purchase AS
SELECT p.purchase_date AS valid_date, MIN(p.id) AS purchase_id
FROM purchase p
JOIN users u ON p.buyer_id = u.id
JOIN tmp_target_sales_dates d ON d.valid_date = p.purchase_date
WHERE u.username = 'bulk_buyer'
GROUP BY p.purchase_date;

DELETE pl
FROM purchase_line pl
JOIN tmp_target_sales_dates d ON d.valid_date = pl.valid_date;

INSERT INTO purchase_line (valid_date, quantity, purchase_id, total_cost, ticket_type_name)
SELECT v.valid_date,
     v.adult_quantity,
     tp.purchase_id,
     v.adult_quantity * t.cost,
     'Adult'
FROM (
  SELECT d.valid_date,
       300 + MOD(DAYOFYEAR(d.valid_date), 151) AS target_quantity,
       FLOOR((300 + MOD(DAYOFYEAR(d.valid_date), 151)) * 0.65) AS adult_quantity
  FROM tmp_target_sales_dates d
) v
JOIN tmp_target_sales_purchase tp ON tp.valid_date = v.valid_date
JOIN ticket_type t ON t.type_name = 'Adult';

INSERT INTO purchase_line (valid_date, quantity, purchase_id, total_cost, ticket_type_name)
SELECT v.valid_date,
     v.target_quantity - v.adult_quantity,
     tp.purchase_id,
     (v.target_quantity - v.adult_quantity) * t.cost,
     'Child'
FROM (
  SELECT d.valid_date,
       300 + MOD(DAYOFYEAR(d.valid_date), 151) AS target_quantity,
       FLOOR((300 + MOD(DAYOFYEAR(d.valid_date), 151)) * 0.65) AS adult_quantity
  FROM tmp_target_sales_dates d
) v
JOIN tmp_target_sales_purchase tp ON tp.valid_date = v.valid_date
JOIN ticket_type t ON t.type_name = 'Child';

DROP TEMPORARY TABLE IF EXISTS tmp_target_sales_purchase;
DROP TEMPORARY TABLE IF EXISTS tmp_target_sales_dates;

