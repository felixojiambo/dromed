CREATE TABLE drones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    serial_number VARCHAR(100) NOT NULL UNIQUE,
    model VARCHAR(50) NOT NULL,
    weight_limit INT NOT NULL,
    battery_capacity INT NOT NULL,
    state VARCHAR(50) NOT NULL
);

CREATE TABLE medications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    weight INT NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    image_url VARCHAR(255)
);

CREATE TABLE drone_medications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    drone_id BIGINT NOT NULL,
    medication_id BIGINT NOT NULL,
    FOREIGN KEY (drone_id) REFERENCES drones(id),
    FOREIGN KEY (medication_id) REFERENCES medications(id)
);
