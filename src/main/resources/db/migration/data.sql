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


INSERT INTO attraction (name, intensity, category, minimum_height, minimum_age, minimum_weight, description, photo_url, is_active, map_position_x, map_position_y, maintenance_status)
VALUES

    ('Dragon Fury', 'HIGH', 'ROLLER_COASTER', 140, 14, 40, 'La montaña rusa mas extrema del parque con 3 inversiones y una caida de 50 metros', '/img/dragon_fury.jpg', TRUE, 85.0, 80.0, 'OPERATIONAL'),
    ('Thunder Tower', 'HIGH', 'DROP_TOWER', 130, 12, 35, 'Torre de caida libre de 60 metros con vistas panoramicas del parque', '/img/thunder_tower.jpg', TRUE, 90.0, 70.0, 'OPERATIONAL'),


    ('Velocity Coaster', 'HIGH', 'ROLLER_COASTER', 135, 12, 35, 'Montaña rusa lanzada con aceleración de 0 a 100 km/h en 3 segundos', '/img/velocity_coaster.jpg', TRUE, 85.0, 35.0, 'OPERATIONAL'),
    ('Storm Chaser', 'HIGH', 'DROP_TOWER', 125, 10, 30, 'Torre de rebote con múltiples caidas y subidas', '/img/storm_chaser.jpg', TRUE, 90.0, 25.0, 'OPERATIONAL'),


    ('Splash Mountain', 'MEDIUM', 'WATER_RIDE', 110, 8, 25, 'Emocionante descenso acuatico con una cascada final de 20 metros', '/img/splash_mountain.jpg', TRUE, 15.0, 70.0, 'OPERATIONAL'),
    ('Pirate Rapids', 'MEDIUM', 'WATER_RIDE', 100, 6, 20, 'Aventura en balsas por rapidos y cuevas piratas', '/img/pirate_rapids.jpg', TRUE, 10.0, 80.0, 'OPERATIONAL'),


    ('Magic Carousel', 'LOW', 'CAROUSEL', 0, 0, 0, 'Carrusel clásico con caballos magicos y música encantadora', '/img/magic_carousel.jpg', TRUE, 15.0, 35.0, 'OPERATIONAL'),
    ('Mini Carousel', 'LOW', 'CAROUSEL', 0, 0, 0, 'Carrusel especial para los mas pequeños con animales de granja', '/img/mini_carousel.jpg', TRUE, 10.0, 25.0, 'OPERATIONAL'),


    ('Haunted Manor', 'MEDIUM', 'HAUNTED_HOUSE', 100, 8, 0, 'Casa encantada interactiva con efectos especiales y sustos inesperados', '/img/haunted_manor.jpg', TRUE, 55.0, 85.0, 'OPERATIONAL'),
    ('Sky Swings', 'MEDIUM', 'SWING_RIDE', 120, 10, 30, 'Sillas voladoras a 30 metros de altura con rotacion de 360 grados', '/img/sky_swings.jpg', TRUE, 45.0, 85.0, 'OPERATIONAL'),


    ('Wild River', 'MEDIUM', 'WATER_RIDE', 105, 7, 22, 'Recorrido por un río salvaje con sorpresas acuáticas', '/img/wild_river.jpg', TRUE, 92.0, 55.0, 'OPERATIONAL'),
    ('Bumper Kingdom', 'LOW', 'BUMPER_CARS', 90, 5, 15, 'Coches de choque con tematica medieval para toda la familia', '/img/bumper_kingdom.jpg', TRUE, 70.0, 60.0, 'OPERATIONAL'),


    ('Enchanted Train', 'LOW', 'TRAIN_RIDE', 0, 0, 0, 'Recorrido panoramico por todo el parque en un tren de epoca', '/img/enchanted_train.jpg', TRUE, 8.0, 55.0, 'OPERATIONAL'),
    ('Flying Carpet', 'LOW', 'SWING_RIDE', 90, 5, 15, 'Alfombra voladora que gira y sube suavemente', '/img/flying_carpet.jpg', TRUE, 30.0, 60.0, 'OPERATIONAL'),


    ('Space Journey', 'MEDIUM', 'OTHER', 100, 6, 0, 'Viaje espacial interactivo con proyecciones 4D', '/img/space_journey.jpg', TRUE, 65.0, 15.0, 'OPERATIONAL'),
    ('Teacups Spin', 'LOW', 'OTHER', 0, 0, 0, 'Tazas giratorias para toda la familia', '/img/teacups.jpg', TRUE, 35.0, 15.0, 'OPERATIONAL'),


    ('Giant Wheel', 'LOW', 'FERRIS_WHEEL', 0, 0, 0, 'Noria gigante de 50 metros con cabinas climatizadas y vistas espectaculares', '/img/giant_wheel.jpg', TRUE, 50.0, 65.0, 'OPERATIONAL');



INSERT  INTO purchase (purchase_date, buyer_id)
SELECT CURDATE(), u.id FROM users u WHERE u.username = 'user1';


INSERT  INTO purchase_line (valid_date, quantity, purchase_id, total_cost, ticket_type_name)
SELECT CURDATE() + INTERVAL 1 DAY, 2, p.id, 2 * t.cost, t.type_name
FROM purchase p
JOIN users u ON p.buyer_id = u.id
JOIN ticket_type t ON t.type_name = 'Adult'
WHERE u.username = 'user1';


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
SELECT p.purchase_date + INTERVAL 1 DAY, 1, p.id, t.cost, t.type_name
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

-- Weekly schedules for current and next week - generated programmatically
-- Rules implemented:
--  - Two weeks: current week (Monday) and next week
--  - Each employee works 5 of 7 days (5 consecutive days, rotating start by employee index)
--  - Every attraction has one OPERATOR every day
--  - Every park zone has one SECURITY every day
--  - At least one MEDICAL, MAINTENANCE and GUEST_SERVICES employee per day

-- Remove existing generated schedules for current and next week (safe to re-run)
SET @monday := DATE_SUB(CURDATE(), INTERVAL (DAYOFWEEK(CURDATE()) - 2 + 7) % 7 DAY);
DELETE FROM weekly_schedule WHERE week_start_date IN (@monday, DATE_ADD(@monday, INTERVAL 7 DAY));

-- Helper derived table for days (0..6)
-- We'll build inline derived tables in each INSERT below to avoid CTEs

-- 1) OPERATORS -> assign one operator per attraction per day for both weeks
INSERT INTO weekly_schedule (employee_id, week_start_date, day_of_week, shift, break_group, attraction_id, zone_id)
SELECT DISTINCT
  o.employee_id,
  DATE_ADD(w.week_start, INTERVAL w.week_index * 7 DAY) AS week_start_date,
  d.day_name AS day_of_week,
  'FULL_DAY' AS shift,
  'A' AS break_group,
  a.id AS attraction_id,
  NULL AS zone_id
FROM
  (
    SELECT DATE_SUB(CURDATE(), INTERVAL (DAYOFWEEK(CURDATE()) - 2 + 7) % 7 DAY) AS week_start, 0 AS week_index
    UNION ALL
    SELECT DATE_ADD(DATE_SUB(CURDATE(), INTERVAL (DAYOFWEEK(CURDATE()) - 2 + 7) % 7 DAY), INTERVAL 7 DAY), 1
  ) w
CROSS JOIN (
  SELECT 0 AS day_index, 'MONDAY' AS day_name UNION ALL
  SELECT 1, 'TUESDAY' UNION ALL
  SELECT 2, 'WEDNESDAY' UNION ALL
  SELECT 3, 'THURSDAY' UNION ALL
  SELECT 4, 'FRIDAY' UNION ALL
  SELECT 5, 'SATURDAY' UNION ALL
  SELECT 6, 'SUNDAY'
) d
CROSS JOIN (
  SELECT t.id, (@a_idx := @a_idx + 1) - 1 AS attr_idx
  FROM (SELECT @a_idx := 0) init, (SELECT id FROM attraction WHERE is_active = TRUE ORDER BY id) t
) a
CROSS JOIN (
  SELECT e.id AS employee_id, (@o_idx := @o_idx + 1) - 1 AS emp_idx
  FROM (SELECT @o_idx := 0) init2, (SELECT id FROM employee WHERE role = 'OPERATOR' ORDER BY id) e
) o
WHERE
  (SELECT COUNT(*) FROM employee WHERE role = 'OPERATOR') > 0
  AND MOD(a.attr_idx + d.day_index + w.week_index, (SELECT COUNT(*) FROM employee WHERE role = 'OPERATOR')) = o.emp_idx
  AND MOD(d.day_index - o.emp_idx + 7, 7) < 5
  AND NOT EXISTS (
    SELECT 1 FROM weekly_schedule ws
    WHERE ws.week_start_date = DATE_ADD(w.week_start, INTERVAL w.week_index * 7 DAY)
      AND ws.day_of_week = d.day_name
      AND ws.attraction_id = a.id
  );

-- 2) SECURITY -> assign one security per zone per day for both weeks
INSERT INTO weekly_schedule (employee_id, week_start_date, day_of_week, shift, break_group, attraction_id, zone_id)
SELECT DISTINCT
  s.employee_id,
  DATE_ADD(w.week_start, INTERVAL w.week_index * 7 DAY) AS week_start_date,
  d.day_name AS day_of_week,
  'FULL_DAY' AS shift,
  'B' AS break_group,
  NULL AS attraction_id,
  z.id AS zone_id
FROM
  (
    SELECT DATE_SUB(CURDATE(), INTERVAL (DAYOFWEEK(CURDATE()) - 2 + 7) % 7 DAY) AS week_start, 0 AS week_index
    UNION ALL
    SELECT DATE_ADD(DATE_SUB(CURDATE(), INTERVAL (DAYOFWEEK(CURDATE()) - 2 + 7) % 7 DAY), INTERVAL 7 DAY), 1
  ) w
CROSS JOIN (
  SELECT 0 AS day_index, 'MONDAY' AS day_name UNION ALL
  SELECT 1, 'TUESDAY' UNION ALL
  SELECT 2, 'WEDNESDAY' UNION ALL
  SELECT 3, 'THURSDAY' UNION ALL
  SELECT 4, 'FRIDAY' UNION ALL
  SELECT 5, 'SATURDAY' UNION ALL
  SELECT 6, 'SUNDAY'
) d
CROSS JOIN (
  SELECT zt.id, (@z_idx := @z_idx + 1) - 1 AS zone_idx
  FROM (SELECT @z_idx := 0) iz, (SELECT id FROM park_zone ORDER BY id) zt
) z
CROSS JOIN (
  SELECT se.id AS employee_id, (@s_idx := @s_idx + 1) - 1 AS emp_idx
  FROM (SELECT @s_idx := 0) isec, (SELECT id FROM employee WHERE role = 'SECURITY' ORDER BY id) se
) s
WHERE
  (SELECT COUNT(*) FROM employee WHERE role = 'SECURITY') > 0
  AND MOD(z.zone_idx + d.day_index + w.week_index, (SELECT COUNT(*) FROM employee WHERE role = 'SECURITY')) = s.emp_idx
  AND MOD(d.day_index - s.emp_idx + 7, 7) < 5
  AND NOT EXISTS (
    SELECT 1 FROM weekly_schedule ws
    WHERE ws.week_start_date = DATE_ADD(w.week_start, INTERVAL w.week_index * 7 DAY)
      AND ws.day_of_week = d.day_name
      AND ws.zone_id = z.id
  );

-- 3) Roles MEDICAL, MAINTENANCE, GUEST_SERVICES -> ensure at least one employee per day
-- MEDICAL
INSERT INTO weekly_schedule (employee_id, week_start_date, day_of_week, shift, break_group, attraction_id, zone_id)
SELECT DISTINCT
  r.employee_id,
  DATE_ADD(w.week_start, INTERVAL w.week_index * 7 DAY) AS week_start_date,
  d.day_name AS day_of_week,
  'FULL_DAY' AS shift,
  'C' AS break_group,
  NULL AS attraction_id,
  NULL AS zone_id
FROM
  (
    SELECT DATE_SUB(CURDATE(), INTERVAL (DAYOFWEEK(CURDATE()) - 2 + 7) % 7 DAY) AS week_start, 0 AS week_index
    UNION ALL
    SELECT DATE_ADD(DATE_SUB(CURDATE(), INTERVAL (DAYOFWEEK(CURDATE()) - 2 + 7) % 7 DAY), INTERVAL 7 DAY), 1
  ) w
CROSS JOIN (
  SELECT 0 AS day_index, 'MONDAY' AS day_name UNION ALL
  SELECT 1, 'TUESDAY' UNION ALL
  SELECT 2, 'WEDNESDAY' UNION ALL
  SELECT 3, 'THURSDAY' UNION ALL
  SELECT 4, 'FRIDAY' UNION ALL
  SELECT 5, 'SATURDAY' UNION ALL
  SELECT 6, 'SUNDAY'
) d
CROSS JOIN (
  SELECT e.id AS employee_id, (@m_idx := @m_idx + 1) - 1 AS emp_idx
  FROM (SELECT @m_idx := 0) im, (SELECT id FROM employee WHERE role = 'MEDICAL' ORDER BY id) e
) r
WHERE
  (SELECT COUNT(*) FROM employee WHERE role = 'MEDICAL') > 0
  AND MOD(d.day_index - r.emp_idx + 7, 7) < 5
  AND r.emp_idx = MOD(d.day_index + w.week_index, (SELECT COUNT(*) FROM employee WHERE role = 'MEDICAL'))
  AND NOT EXISTS (
    SELECT 1 FROM weekly_schedule ws
    WHERE ws.week_start_date = DATE_ADD(w.week_start, INTERVAL w.week_index * 7 DAY)
      AND ws.day_of_week = d.day_name
      AND ws.employee_id = r.employee_id
  );

-- MAINTENANCE
INSERT INTO weekly_schedule (employee_id, week_start_date, day_of_week, shift, break_group, attraction_id, zone_id)
SELECT DISTINCT
  r.employee_id,
  DATE_ADD(w.week_start, INTERVAL w.week_index * 7 DAY) AS week_start_date,
  d.day_name AS day_of_week,
  'FULL_DAY' AS shift,
  'A' AS break_group,
  NULL AS attraction_id,
  NULL AS zone_id
FROM
  (
    SELECT DATE_SUB(CURDATE(), INTERVAL (DAYOFWEEK(CURDATE()) - 2 + 7) % 7 DAY) AS week_start, 0 AS week_index
    UNION ALL
    SELECT DATE_ADD(DATE_SUB(CURDATE(), INTERVAL (DAYOFWEEK(CURDATE()) - 2 + 7) % 7 DAY), INTERVAL 7 DAY), 1
  ) w
CROSS JOIN (
  SELECT 0 AS day_index, 'MONDAY' AS day_name UNION ALL
  SELECT 1, 'TUESDAY' UNION ALL
  SELECT 2, 'WEDNESDAY' UNION ALL
  SELECT 3, 'THURSDAY' UNION ALL
  SELECT 4, 'FRIDAY' UNION ALL
  SELECT 5, 'SATURDAY' UNION ALL
  SELECT 6, 'SUNDAY'
) d
CROSS JOIN (
  SELECT e.id AS employee_id, (@mm_idx := @mm_idx + 1) - 1 AS emp_idx
  FROM (SELECT @mm_idx := 0) imm, (SELECT id FROM employee WHERE role = 'MAINTENANCE' ORDER BY id) e
) r
WHERE
  (SELECT COUNT(*) FROM employee WHERE role = 'MAINTENANCE') > 0
  AND MOD(d.day_index - r.emp_idx + 7, 7) < 5
  AND r.emp_idx = MOD(d.day_index + w.week_index, (SELECT COUNT(*) FROM employee WHERE role = 'MAINTENANCE'))
  AND NOT EXISTS (
    SELECT 1 FROM weekly_schedule ws
    WHERE ws.week_start_date = DATE_ADD(w.week_start, INTERVAL w.week_index * 7 DAY)
      AND ws.day_of_week = d.day_name
      AND ws.employee_id = r.employee_id
  );

-- GUEST_SERVICES
INSERT INTO weekly_schedule (employee_id, week_start_date, day_of_week, shift, break_group, attraction_id, zone_id)
SELECT DISTINCT
  r.employee_id,
  DATE_ADD(w.week_start, INTERVAL w.week_index * 7 DAY) AS week_start_date,
  d.day_name AS day_of_week,
  'FULL_DAY' AS shift,
  'B' AS break_group,
  NULL AS attraction_id,
  NULL AS zone_id
FROM
  (
    SELECT DATE_SUB(CURDATE(), INTERVAL (DAYOFWEEK(CURDATE()) - 2 + 7) % 7 DAY) AS week_start, 0 AS week_index
    UNION ALL
    SELECT DATE_ADD(DATE_SUB(CURDATE(), INTERVAL (DAYOFWEEK(CURDATE()) - 2 + 7) % 7 DAY), INTERVAL 7 DAY), 1
  ) w
CROSS JOIN (
  SELECT 0 AS day_index, 'MONDAY' AS day_name UNION ALL
  SELECT 1, 'TUESDAY' UNION ALL
  SELECT 2, 'WEDNESDAY' UNION ALL
  SELECT 3, 'THURSDAY' UNION ALL
  SELECT 4, 'FRIDAY' UNION ALL
  SELECT 5, 'SATURDAY' UNION ALL
  SELECT 6, 'SUNDAY'
) d
CROSS JOIN (
  SELECT e.id AS employee_id, (@g_idx := @g_idx + 1) - 1 AS emp_idx
  FROM (SELECT @g_idx := 0) ig, (SELECT id FROM employee WHERE role = 'GUEST_SERVICES' ORDER BY id) e
) r
WHERE
  (SELECT COUNT(*) FROM employee WHERE role = 'GUEST_SERVICES') > 0
  AND MOD(d.day_index - r.emp_idx + 7, 7) < 5
  AND r.emp_idx = MOD(d.day_index + w.week_index, (SELECT COUNT(*) FROM employee WHERE role = 'GUEST_SERVICES'))
  AND NOT EXISTS (
    SELECT 1 FROM weekly_schedule ws
    WHERE ws.week_start_date = DATE_ADD(w.week_start, INTERVAL w.week_index * 7 DAY)
      AND ws.day_of_week = d.day_name
      AND ws.employee_id = r.employee_id
  );

-- End of generated schedules

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

