CREATE DATABASE IF NOT EXISTS the_binding_of_wachin;
USE the_binding_of_wachin;

CREATE TABLE IF NOT EXISTS Entidades (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    vida INT NOT NULL,
    vida_max INT NOT NULL,
    dano INT NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    experiencia INT NOT NULL DEFAULT 0,
    nivel INT NOT NULL DEFAULT 1,
    monedas INT NOT NULL DEFAULT 0,
    energia INT NOT NULL DEFAULT 0,
    energia_maxima INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS partidas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL DEFAULT 'Partida',
    fecha_guardado DATETIME DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    sala_actual INT NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS jugadores_partida (
    partida_id INT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    vida INT NOT NULL,
    vida_maxima INT NOT NULL,
    dano INT NOT NULL,
    experiencia INT NOT NULL DEFAULT 0,
    nivel INT NOT NULL DEFAULT 1,
    monedas INT NOT NULL DEFAULT 0,
    energia INT NOT NULL DEFAULT 0,
    energia_maxima INT NOT NULL DEFAULT 0,
    posicion_x DOUBLE NOT NULL DEFAULT 0,
    posicion_y DOUBLE NOT NULL DEFAULT 0,
    FOREIGN KEY (partida_id) REFERENCES partidas(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS enemigos_partida (
    id INT AUTO_INCREMENT PRIMARY KEY,
    partida_id INT NOT NULL,
    nombre VARCHAR(50) NOT NULL,
    vida INT NOT NULL,
    vida_maxima INT NOT NULL,
    dano INT NOT NULL,
    posicion_x DOUBLE NOT NULL DEFAULT 0,
    posicion_y DOUBLE NOT NULL DEFAULT 0,
    sala INT NOT NULL DEFAULT 1,
    derrotado BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (partida_id) REFERENCES partidas(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS invocaciones_partida (
    id INT AUTO_INCREMENT PRIMARY KEY,
    partida_id INT NOT NULL,
    nombre VARCHAR(50) NOT NULL,
    vida INT NOT NULL,
    vida_maxima INT NOT NULL,
    dano INT NOT NULL,
    duracion_restante INT NOT NULL DEFAULT 0,
    posicion_x DOUBLE NOT NULL DEFAULT 0,
    posicion_y DOUBLE NOT NULL DEFAULT 0,
    sala INT NOT NULL DEFAULT 1,
    FOREIGN KEY (partida_id) REFERENCES partidas(id)
        ON DELETE CASCADE
);

-- Tres espacios locales compartidos en esta instalación de MySQL.
CREATE TABLE IF NOT EXISTS partida_slots (
    slot_numero TINYINT NOT NULL PRIMARY KEY,
    partida_id INT NULL UNIQUE,
    CONSTRAINT chk_partida_slot CHECK (slot_numero BETWEEN 1 AND 3),
    FOREIGN KEY (partida_id) REFERENCES partidas(id) ON DELETE SET NULL
);

INSERT IGNORE INTO partida_slots (slot_numero, partida_id) VALUES
    (1, NULL), (2, NULL), (3, NULL);

-- Instantánea completa y versionada: añade estado sin invalidar las tablas existentes.
CREATE TABLE IF NOT EXISTS partida_snapshot (
    partida_id INT NOT NULL PRIMARY KEY,
    version_snapshot SMALLINT NOT NULL,
    payload LONGBLOB NOT NULL,
    fecha_guardado DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (partida_id) REFERENCES partidas(id) ON DELETE CASCADE
);

SHOW TABLES;
