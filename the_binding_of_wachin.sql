CREATE TABLE Entidades (
    id INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(50) NOT NULL,
    vida INT NOT NULL,
    vida_max INT NOT NULL,
    dano INT NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    experiencia INT NOT NULL DEFAULT 0,
    nivel INT NOT NULL DEFAULT 1,
    monedas INT NOT NULL DEFAULT 0,
    energia INT NOT NULL DEFAULT 0,
    energia_maxima INT NOT NULL DEFAULT 0,

    PRIMARY KEY (id)
);