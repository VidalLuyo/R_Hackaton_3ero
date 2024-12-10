package controller;

import db.ConexionDB;
import model.Kardex;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KardexController {

    /* Listar todos los registros de Kardex */
    public List<Kardex> listarTodos() {
        List<Kardex> kardexList = new ArrayList<>();
        String query = "SELECT * FROM Kardex";

        try (Connection connection = ConexionDB.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Kardex kardex = mapearKardex(rs);
                kardexList.add(kardex);
            }
        } catch (Exception e) {
            System.out.println("Error al listar registros del Kardex.");
            e.printStackTrace();
        }
        return kardexList;
    }

    /* Listar todos los productos */
    public List<Map<String, Object>> listarTodosProductos() {
        List<Map<String, Object>> productos = new ArrayList<>();
        String query = "SELECT ProductoID, Nombre FROM Productos WHERE Estado = 'A'";

        try (Connection connection = ConexionDB.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Map<String, Object> producto = new HashMap<>();
                producto.put("productoID", rs.getInt("ProductoID")); // ID del producto
                producto.put("nombre", rs.getString("Nombre")); // Nombre del producto
                productos.add(producto);
            }
        } catch (Exception e) {
            System.out.println("Error al listar los productos: " + e.getMessage());
            e.printStackTrace();
        }
        return productos;
    }



    public int obtenerStockInicialProducto(int productoID) {
        String query = "SELECT StockInicial FROM Productos WHERE ProductoID = ?";
        try (Connection connection = ConexionDB.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, productoID);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("StockInicial");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0; // Devuelve 0 si no se encuentra el producto
    }

    public List<Kardex> buscarTodosPorProducto(int productoID) {
        List<Kardex> movimientos = new ArrayList<>();
        String query = "SELECT * FROM Kardex WHERE ProductoID = ? ORDER BY Fecha";

        try (Connection connection = ConexionDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, productoID);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Kardex kardex = new Kardex();
                kardex.setProductoID(resultSet.getInt("ProductoID"));
                kardex.setFecha(resultSet.getDate("Fecha"));
                kardex.setTipoMovimiento(resultSet.getString("TipoMovimiento"));
                kardex.setCantidad(resultSet.getInt("Cantidad"));
                kardex.setPrecioUnitario(resultSet.getDouble("PrecioUnitario"));
                kardex.setMontoTotal(resultSet.getDouble("MontoTotal"));
                kardex.setStockInicial(resultSet.getInt("StockInicial"));
                kardex.setStockFinal(resultSet.getInt("StockFinal"));

                movimientos.add(kardex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return movimientos;
    }

    public boolean registrarMovimiento(Kardex kardex) {
        Connection connection = null;
        try {
            connection = ConexionDB.getConnection();
            connection.setAutoCommit(false); // Iniciar transacción

            // Validar si la operación es válida (verificar stock para ventas)
            int stockActual = obtenerSaldoActual(kardex.getProductoID());
            if (kardex.getTipoMovimiento().equals("V") && kardex.getCantidad() > stockActual) {
                throw new IllegalArgumentException("No hay suficiente stock para realizar la venta.");
            }

            // Insertar el movimiento en la tabla Kardex
            String insertarKardex = "INSERT INTO Kardex (ProductoID, Fecha, TipoMovimiento, Cantidad, PrecioUnitario, StockInicial, StockFinal) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(insertarKardex)) {
                pstmt.setInt(1, kardex.getProductoID());
                pstmt.setDate(2, new java.sql.Date(kardex.getFecha().getTime()));
                pstmt.setString(3, kardex.getTipoMovimiento());
                pstmt.setInt(4, kardex.getCantidad());
                pstmt.setDouble(5, kardex.getPrecioUnitario());
                pstmt.setInt(6, stockActual);
                pstmt.setInt(7, stockActual + (kardex.getTipoMovimiento().equals("C") ? kardex.getCantidad() : -kardex.getCantidad()));
                pstmt.executeUpdate();
            }

            // Actualizar el StockActual en la tabla Productos
            String actualizarStock = "UPDATE Productos SET StockActual = StockActual + ? WHERE ProductoID = ?";
            int cantidad = kardex.getCantidad() * (kardex.getTipoMovimiento().equals("C") ? 1 : -1);
            try (PreparedStatement pstmt = connection.prepareStatement(actualizarStock)) {
                pstmt.setInt(1, cantidad);
                pstmt.setInt(2, kardex.getProductoID());
                pstmt.executeUpdate();
            }

            connection.commit(); // Confirmar transacción
            return true;
        } catch (Exception e) {
            if (connection != null) {
                try {
                    connection.rollback(); // Revertir transacción en caso de error
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true); // Restaurar configuración predeterminada
                    connection.close();
                } catch (SQLException closeEx) {
                    closeEx.printStackTrace();
                }
            }
        }
    }

    public boolean actualizarCompra(int kardexID, int cantidad, double precioUnitario) {
        String query = "UPDATE Kardex SET Cantidad = ?, PrecioUnitario = ? WHERE KardexID = ?";

        try (Connection connection = ConexionDB.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setInt(1, cantidad);
            pstmt.setDouble(2, precioUnitario);
            pstmt.setInt(3, kardexID);

            int filasActualizadas = pstmt.executeUpdate();
            return filasActualizadas > 0; // Retorna true si se actualizó al menos una fila
        } catch (Exception e) {
            System.out.println("Error al actualizar la compra: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    /* Obtener el StockActual de un producto */
    public int obtenerStockActual(int productoID) {
        String query = "SELECT StockActual FROM Productos WHERE ProductoID = ?";
        try (Connection connection = ConexionDB.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setInt(1, productoID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("StockActual");
            }
        } catch (Exception e) {
            System.out.println("Error al obtener el StockActual: " + e.getMessage());
            e.printStackTrace();
        }
        return 0; // Devuelve 0 si no se encuentra el producto o hay un error
    }


    public int obtenerSaldoActual(int productoID) {
        String query = "SELECT StockActual FROM Productos WHERE ProductoID = ?";
        try (Connection connection = ConexionDB.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setInt(1, productoID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("StockActual");
            }
        } catch (Exception e) {
            System.out.println("Error al obtener saldo actual: " + e.getMessage());
            e.printStackTrace();
        }
        return 0; // Si no se encuentra el producto, retorna 0
    }


    /* Obtener lista de productos */
    public List<Map<String, Object>> obtenerProductos(int movimientoId) {
        List<Map<String, Object>> productos = new ArrayList<>();
        String query = "SELECT ProductoID, Nombre FROM Productos";

        try (Connection connection = ConexionDB.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Map<String, Object> producto = new HashMap<>();
                producto.put("productoID", rs.getInt("ProductoID"));
                producto.put("nombre", rs.getString("Nombre"));
                productos.add(producto);
            }
        } catch (Exception e) {
            System.out.println("Error al obtener lista de productos: " + e.getMessage());
        }
        return productos;
    }


    /* Mapear datos de ResultSet a un objeto Kardex */
    private Kardex mapearKardex(ResultSet rs) throws SQLException {
        Kardex kardex = new Kardex();
        kardex.setKardexID(rs.getInt("KardexID"));
        kardex.setProductoID(rs.getInt("ProductoID"));
        kardex.setFecha(rs.getDate("Fecha"));
        kardex.setTipoMovimiento(String.valueOf(rs.getString("TipoMovimiento").charAt(0)));
        kardex.setCantidad(rs.getInt("Cantidad"));
        kardex.setPrecioUnitario(rs.getDouble("PrecioUnitario"));
        kardex.setMontoTotal(rs.getDouble("MontoTotal"));
        kardex.setStockInicial(rs.getInt("StockInicial"));
        kardex.setStockFinal(rs.getInt("StockFinal"));
        return kardex;
    }

    public static Kardex obtenerCompraPorId(int movimientoId) {
        Kardex movimiento = null;
        String query = "SELECT * FROM Kardex WHERE KardexID = ?";
        try (Connection connection = ConexionDB.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, movimientoId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                movimiento = new Kardex();
                movimiento.setKardexID(rs.getInt("KardexID"));
                movimiento.setProductoID(rs.getInt("ProductoID"));
                movimiento.setFecha(rs.getDate("Fecha"));
                movimiento.setTipoMovimiento(rs.getString("TipoMovimiento"));
                movimiento.setCantidad(rs.getInt("Cantidad"));
                movimiento.setPrecioUnitario(rs.getDouble("PrecioUnitario"));
                movimiento.setStockInicial(rs.getInt("StockInicial"));
                movimiento.setStockFinal(rs.getInt("StockFinal"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return movimiento;
    }

    public List<Kardex> buscarTodosActivos() {
        List<Kardex> movimientos = new ArrayList<>();
        String query = "SELECT * FROM Kardex WHERE Estado = 'A'";

        try (Connection connection = ConexionDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Kardex kardex = new Kardex();
                kardex.setKardexID(resultSet.getInt("KardexID"));
                kardex.setProductoID(resultSet.getInt("ProductoID"));
                kardex.setFecha(resultSet.getDate("Fecha"));
                kardex.setTipoMovimiento(resultSet.getString("TipoMovimiento"));
                kardex.setCantidad(resultSet.getInt("Cantidad"));
                kardex.setPrecioUnitario(resultSet.getDouble("PrecioUnitario"));
                kardex.setStockInicial(resultSet.getInt("StockInicial"));
                kardex.setStockFinal(resultSet.getInt("StockFinal"));

                movimientos.add(kardex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return movimientos;
    }


    public boolean actualizarCompra(Kardex compra) {
        String query = "UPDATE Kardex SET Cantidad = ?, PrecioUnitario = ?, StockInicial = ?, StockFinal = ? WHERE KardexID = ?";
        try (Connection connection = ConexionDB.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setInt(1, compra.getCantidad());
            stmt.setDouble(2, compra.getPrecioUnitario());
            stmt.setInt(3, compra.getStockInicial());
            stmt.setInt(4, compra.getStockFinal());
            stmt.setString(5, compra.getTipoMovimiento());

            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
