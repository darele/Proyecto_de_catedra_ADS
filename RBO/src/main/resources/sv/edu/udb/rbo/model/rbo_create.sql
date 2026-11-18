-- ---------------------------------------------------------------------------
-- Esquema de la base de datos rbo_marcas.
--
-- La aplicación ejecuta este script al arrancar (EsquemaPeer), así que las tablas
-- se crean solas. También se puede ejecutar a mano en phpMyAdmin, primero con:
--     CREATE DATABASE IF NOT EXISTS rbo_marcas;  USE rbo_marcas;
--
-- Usa CREATE TABLE IF NOT EXISTS: ejecutarlo de nuevo no borra datos.
-- Los nombres de tablas y columnas son los mismos que generaba la antigua clase DBHandler,
-- para no romper las bases que ya existen.
-- Cada sentencia debe terminar en ';' al final de una línea.
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS Empleado (
  id_empleado   VARCHAR(10) PRIMARY KEY,
  nombre        VARCHAR(30) NOT NULL,
  Apellido      VARCHAR(30) NOT NULL,
  tarifa        DOUBLE,
  hora_entrada  TIME NOT NULL,
  hora_salida   TIME NOT NULL,
  tiempo_receso INT NOT NULL,
  rol           ENUM('admin', 'RH', 'Contabilidad', 'Empleado') NOT NULL,
  password      VARCHAR(64) NOT NULL
);

CREATE TABLE IF NOT EXISTS Marca (
  id_marca    INT PRIMARY KEY AUTO_INCREMENT,
  fecha_marca DATE DEFAULT (CURRENT_DATE) NOT NULL,
  hora_marca  TIME DEFAULT (CURRENT_TIME) NOT NULL,
  tipo_marca  ENUM('entrada', 'receso', 'salida') NOT NULL,
  comentario  VARCHAR(100) NOT NULL,
  id_empleado VARCHAR(10) NOT NULL,
  FOREIGN KEY (id_empleado) REFERENCES Empleado(id_empleado)
);

CREATE TABLE IF NOT EXISTS Tarifa (
  id_tarifa    INT PRIMARY KEY AUTO_INCREMENT,
  fecha_tarifa DATE NOT NULL,
  hora_entrada TIME NOT NULL,
  hora_salida  TIME NOT NULL,
  tarifa       DOUBLE NOT NULL,
  id_empleado  VARCHAR(10) NOT NULL,
  FOREIGN KEY (id_empleado) REFERENCES Empleado(id_empleado)
);
