DROP TABLE IF EXISTS `user`;
CREATE TABLE IF NOT EXISTS `user` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    fecha_registro DATE
    );
INSERT INTO `user` (nombre, email, fecha_registro) VALUES
('Ana García', 'ana@example.com', '2024-06-01'),
('Luis Pérez', 'luis@example.com', '2024-06-02');