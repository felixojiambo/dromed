INSERT INTO drones (serial_number, model, weight_limit, battery_capacity, state) VALUES
('SN123456', 'LIGHTWEIGHT', 300, 100, 'IDLE'),
('SN123457', 'MIDDLEWEIGHT', 400, 100, 'IDLE');

INSERT INTO medications (name, weight, code, image_url) VALUES
('Med1', 100, 'MED1', 'image1.jpg'),
('Med2', 200, 'MED2', 'image2.jpg');

INSERT INTO drone_medications (drone_id, medication_id) VALUES
(1, 1),
(2, 2);
