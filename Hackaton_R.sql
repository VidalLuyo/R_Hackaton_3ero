-- Crear la base de datos
USE master;
GO

DROP DATABASE IF EXISTS InventarioKardex;
GO

-- Crear base de datos
CREATE DATABASE InventarioKardex;
GO

-- Usar la base de datos
USE InventarioKardex;
GO

-- Crear tabla de Productos
CREATE TABLE Productos (
    ProductoID INT IDENTITY(1,1) PRIMARY KEY,
    CodigoProducto CHAR(10) NOT NULL UNIQUE, -- Código único para identificar el producto
    Nombre VARCHAR(100) NOT NULL,
    PrecioCompra DECIMAL(10, 2) NOT NULL,
    PrecioVenta DECIMAL(10, 2) NOT NULL,
    StockInicial INT NOT NULL,
    StockActual INT NOT NULL,
    FechaRegistro DATE DEFAULT GETDATE(),
	Estado char(1) default 'A'
);
GO

ALTER TABLE Productos ALTER COLUMN Nombre NVARCHAR(100);


-- Insertar datos de ejemplo en la tabla Productos
INSERT INTO Productos (CodigoProducto, Nombre, PrecioCompra, PrecioVenta, StockInicial, StockActual)
VALUES 
('PCJM001', 'pantalones jean - Nike', 1000.00, 1000.00, 50, 50),
('PCJM002', 'pantalones jean - Burberry', 1000.00, 1000.00, 50, 50),
('PCJM003', 'pantalones jean - H&M', 1000.00, 1000.00, 50, 50),
('PCJM004', 'pantalones jean - Mango', 1000.00, 1000.00, 50, 50),
('PCJM005', 'pantalones jean - Calvin Klein', 1000.00, 1000.00, 50, 50),
('PCJM006', 'pantalones jean - The North Face', 1000.00, 1000.00, 50, 50),
('PCJM007', 'pantalones jean - Victorias Secret', 1000.00, 1000.00, 50, 50),
('PCJM008', 'pantalones jean - Adidas', 1000.00, 1000.00, 50, 50),
('PCJM009', 'pantalones jean - Zara', 1000.00, 1000.00, 50, 50),
('PCJM010', 'pantalones jean - Lululemon', 1000.00, 1000.00, 50, 50),
('PCJM011', 'pantalones jean - Tommy Hilfiger', 1000.00, 1000.00, 50, 50),
('PCJM012', 'pantalones jean - H&MS', 1000.00, 1200.00, 50, 50);
GO

-- Crear tabla Kardex ajustada
CREATE TABLE Kardex (
    KardexID INT IDENTITY(1,1) PRIMARY KEY,
    ProductoID INT NOT NULL FOREIGN KEY REFERENCES Productos(ProductoID),
    Fecha DATE NOT NULL,
    TipoMovimiento CHAR(1), 
    Cantidad INT NOT NULL,
    PrecioUnitario DECIMAL(10, 2) NOT NULL, -- Precio por unidad del movimiento
    MontoTotal AS (Cantidad * PrecioUnitario) PERSISTED, -- Calculado automáticamente
    StockInicial INT NOT NULL,
    StockFinal INT NOT NULL,
	Estado char(1) default 'A'
);
GO

ALTER TABLE Productos
ADD CONSTRAINT chk_StockActual CHECK (StockActual >= 0);
GO

-- Trigger para actualizar el StockActual después de un INSERT en Kardex
CREATE TRIGGER trg_UpdateStockProductos
ON Kardex
AFTER INSERT
AS
BEGIN
    -- Verificar si es Compra ('C') o Venta ('V') y actualizar el StockActual
    UPDATE Productos
    SET StockActual = 
        CASE 
            WHEN i.TipoMovimiento = 'C' THEN StockActual + i.Cantidad -- Aumentar stock para compras
            WHEN i.TipoMovimiento = 'V' THEN StockActual - i.Cantidad -- Reducir stock para ventas
            ELSE StockActual
        END
    FROM Productos p
    INNER JOIN inserted i ON p.ProductoID = i.ProductoID
    WHERE i.TipoMovimiento IN ('C', 'V');
END;
GO


DECLARE @ProductoID INT = 1; -- Cambiar al ProductoID que deseas consultar

SELECT 
    CASE 
        WHEN ROW_NUMBER() OVER (PARTITION BY ProductoID ORDER BY Fecha) = 1 THEN 'ENERO 2024'
        ELSE UPPER(DATENAME(MONTH, Fecha)) + ' ' + CAST(YEAR(Fecha) AS VARCHAR)
    END AS Mes,
    CASE WHEN TipoMovimiento = 'C' THEN FORMAT(Fecha, 'dd-MMMM-yyyy', 'es-ES') ELSE NULL END AS Fecha_Compra,
    CASE WHEN TipoMovimiento = 'C' THEN Cantidad ELSE NULL END AS Cantidad_Compra,
    CASE WHEN TipoMovimiento = 'C' THEN PrecioUnitario * Cantidad ELSE NULL END AS Monto_Compra,
    CASE WHEN TipoMovimiento = 'V' THEN FORMAT(Fecha, 'dd-MMMM-yyyy', 'es-ES') ELSE NULL END AS Fecha_Venta,
    CASE WHEN TipoMovimiento = 'V' THEN Cantidad ELSE NULL END AS Cantidad_Venta,
    CASE WHEN TipoMovimiento = 'V' THEN PrecioUnitario * Cantidad ELSE NULL END AS Monto_Venta,
    StockFinal AS Saldo_Final,
    StockFinal * 1000 AS Total_Final -- Asegurando el cálculo correcto de Total_Final
FROM Kardex
WHERE ProductoID = @ProductoID
ORDER BY Fecha;



-- Insertar compras y ventas para todos los productos en todos los meses
INSERT INTO Kardex (ProductoID, Fecha, TipoMovimiento, Cantidad, PrecioUnitario, StockInicial, StockFinal)
VALUES 
-- Producto PCJM001
(1, '2024-01-01', 'C', 30, 1000.00, 50, 80), -- Compra en enero
(1, '2024-01-31', 'V', 20, 1000.00, 80, 60), -- Venta en enero
(1, '2024-02-01', 'C', 25, 1000.00, 60, 85), -- Compra en febrero
(1, '2024-02-28', 'V', 15, 1000.00, 85, 70), -- Venta en febrero
(1, '2024-03-01', 'C', 20, 1000.00, 70, 90), -- Compra en marzo
(1, '2024-03-31', 'V', 25, 1000.00, 90, 65), -- Venta en marzo
(1, '2024-04-01', 'C', 30, 1000.00, 50, 80), -- Compra en enero
(1, '2024-04-30', 'V', 20, 1000.00, 80, 60), -- Venta en enero
(1, '2024-05-01', 'C', 25, 1000.00, 60, 85), -- Compra en febrero
(1, '2024-05-28', 'V', 15, 1000.00, 85, 70), -- Venta en febrero
(1, '2024-06-01', 'C', 20, 1000.00, 70, 90), -- Compra en marzo
(1, '2024-06-30', 'V', 25, 1000.00, 90, 65), -- Venta en marzo
(1, '2024-07-01', 'C', 30, 1000.00, 50, 80), -- Compra en enero
(1, '2024-07-30', 'V', 20, 1000.00, 80, 60), -- Venta en enero
(1, '2024-08-01', 'C', 25, 1000.00, 60, 85), -- Compra en febrero
(1, '2024-08-28', 'V', 15, 1000.00, 85, 70), -- Venta en febrero
(1, '2024-09-01', 'C', 20, 1000.00, 70, 90), -- Compra en marzo
(1, '2024-09-30', 'V', 25, 1000.00, 90, 65), -- Venta en marzo
(1, '2024-10-01', 'C', 20, 1000.00, 70, 90), -- Compra en marzo
(1, '2024-10-30', 'V', 25, 1000.00, 90, 65), -- Venta en marzo
(1, '2024-11-01', 'C', 30, 1000.00, 50, 80), -- Compra en enero
(1, '2024-11-30', 'V', 20, 1000.00, 80, 60), -- Venta en enero
(1, '2024-12-01', 'C', 25, 1000.00, 60, 85), -- Compra en febrero
(1, '2024-12-28', 'V', 15, 1000.00, 85, 70), -- Venta en febrero

-- Producto PCJM002
(2, '2024-01-01', 'C', 40, 1000.00, 50, 90), -- Compra en enero
(2, '2024-01-31', 'V', 25, 1000.00, 90, 65), -- Venta en enero
(2, '2024-02-01', 'C', 30, 1000.00, 65, 95), -- Compra en febrero
(2, '2024-02-28', 'V', 20, 1000.00, 95, 75), -- Venta en febrero
(2, '2024-03-01', 'C', 35, 1000.00, 75, 110), -- Compra en marzo
(2, '2024-03-31', 'V', 30, 1000.00, 110, 80), -- Venta en marzo

-- Producto PCJM003
(3, '2024-01-01', 'C', 50, 1000.00, 50, 100), -- Compra en enero
(3, '2024-01-31', 'V', 40, 1000.00, 100, 60), -- Venta en enero
(3, '2024-02-01', 'C', 20, 1000.00, 60, 80), -- Compra en febrero
(3, '2024-02-28', 'V', 10, 1000.00, 80, 70), -- Venta en febrero
(3, '2024-03-01', 'C', 30, 1000.00, 70, 100), -- Compra en marzo
(3, '2024-03-31', 'V', 20, 1000.00, 100, 80), -- Venta en marzo

-- Producto PCJM004
(4, '2024-01-01', 'C', 20, 1000.00, 50, 70), -- Compra en enero
(4, '2024-01-31', 'V', 10, 1000.00, 70, 60), -- Venta en enero
(4, '2024-02-01', 'C', 15, 1000.00, 60, 75), -- Compra en febrero
(4, '2024-02-28', 'V', 10, 1000.00, 75, 65), -- Venta en febrero
(4, '2024-03-01', 'C', 25, 1000.00, 65, 90), -- Compra en marzo
(4, '2024-03-31', 'V', 20, 1000.00, 90, 70); -- Venta en marzo
GO



SELECT * FROM Productos;

SELECT * FROM Kardex where ProductoID = 15;
