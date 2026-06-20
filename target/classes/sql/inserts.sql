USE facturacion_db;

INSERT INTO usuarios (nombre, apellido, usuario, contrasena, telefono, email, rol) VALUES
('Admin', 'Principal', 'admin', 'admin123', '099100001', 'admin@facturacion.com', 'ADMIN'),
('Carlos', 'Lopez', 'vendedor1', 'vendedor1', '099100002', 'carlos@facturacion.com', 'VENDEDOR'),
('Maria', 'Gonzalez', 'vendedor2', 'vendedor2', '099100003', 'maria@facturacion.com', 'VENDEDOR'),
('Juan', 'Perez', 'deposito1', 'deposito1', '099100004', 'juan@facturacion.com', 'DEPOSITARIO');

INSERT INTO clientes (nombre, apellido, dni, telefono, email, direccion) VALUES
('Pedro', 'Ramirez', '12345678', '098200001', 'pedro@email.com', 'Av. Principal 123'),
('Laura', 'Martinez', '23456789', '098200002', 'laura@email.com', 'Calle Secundaria 456'),
('Roberto', 'Gimenez', '34567890', '098200003', 'roberto@email.com', 'Av. Central 789'),
('Ana', 'Fernandez', '45678901', '098200004', 'ana@email.com', 'Calle Norte 321');

INSERT INTO proveedores (nombre_empresa, nombre_contacto, telefono, email, direccion) VALUES
('Distribuidora ABC', 'Jorge Benitez', '021500001', 'abc@proveedor.com', 'Av. Industrial 100'),
('Mayorista XYZ', 'Sofia Acosta', '021500002', 'xyz@proveedor.com', 'Calle Comercio 200'),
('Importadora del Sur', 'Miguel Torres', '021500003', 'sur@proveedor.com', 'Ruta Nacional Km 5'),
('Tech Solutions', 'Lucia Vega', '021500004', 'tech@proveedor.com', 'Av. Tecnologia 300');

INSERT INTO productos (codigo, nombre, descripcion, categoria, precio_compra, precio_venta, descuento, stock_actual, stock_minimo, proveedor_id) VALUES
('PROD-001', 'Laptop HP', 'Laptop HP 15.6 pulgadas 8GB RAM', 'Informatica', 3500.00, 5200.00, 5.00, 15, 5, 4),
('PROD-002', 'Mouse Inalambrico', 'Mouse optico inalambrico USB', 'Informatica', 80.00, 150.00, 0.00, 50, 10, 1),
('PROD-003', 'Teclado Mecanico', 'Teclado mecanico retroiluminado RGB', 'Informatica', 250.00, 450.00, 10.00, 30, 5, 1),
('PROD-004', 'Monitor 24 Pulgadas', 'Monitor LED Full HD 24 pulgadas', 'Informatica', 1200.00, 1800.00, 0.00, 20, 5, 4),
('PROD-005', 'Silla Ergo Pro', 'Silla ergonomica de oficina', 'Muebles', 800.00, 1400.00, 15.00, 10, 3, 2),
('PROD-006', 'Escritorio Ejecutivo', 'Escritorio de madera 1.50m', 'Muebles', 600.00, 1100.00, 0.00, 8, 2, 2),
('PROD-007', 'Papel A4 5000 hojas', 'Resma de papel bond A4', 'Papeleria', 150.00, 250.00, 0.00, 100, 20, 3),
('PROD-008', 'Boligrafo x10', 'Caja de boligrafos azules x10', 'Papeleria', 25.00, 50.00, 0.00, 200, 30, 3);