package controller;

import db.ConexionDB;
import model.Productos;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductoController {

    /* Obtener el stock inicial de un producto */
    public int obtenerStockInicial(int productoID) {
        String query = "SELECT StockInicial FROM Productos WHERE ProductoID = ?";
        try (Connection connection = ConexionDB.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setInt(1, productoID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("StockInicial");
            }
        } catch (Exception e) {
            System.out.println("Error al obtener el stock inicial: " + e.getMessage());
        }
        return 0; // Devuelve 0 si no se encuentra el producto o hay un error
    }

    public static Productos obtenerProductoPorID(int productoID) {
        String query = "SELECT * FROM Productos WHERE ProductoID = ?";
        try (Connection connection = ConexionDB.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, productoID);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Productos producto = new Productos();
                producto.setProductoID(rs.getInt("ProductoID"));
                producto.setCodigoProducto(rs.getString("CodigoProducto"));
                producto.setNombre(rs.getString("Nombre"));
                producto.setPrecioCompra(rs.getDouble("PrecioCompra"));
                producto.setPrecioVenta(rs.getDouble("PrecioVenta"));
                producto.setStockInicial(rs.getInt("StockInicial"));
                producto.setStockActual(rs.getInt("StockActual"));
                return producto;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Productos> listarTodos() {
        List<Productos> productos = new ArrayList<>();
        String query = "SELECT ProductoID, Nombre FROM Productos";

        try (Connection connection = ConexionDB.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Productos producto = new Productos();
                producto.setProductoID(rs.getInt("ProductoID"));
                producto.setNombre(rs.getString("Nombre"));
                productos.add(producto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return productos;
    }


    /* Obtener el stock actual de un producto */
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
            System.out.println("Error al obtener el stock actual: " + e.getMessage());
        }
        return 0; // Devuelve 0 si no se encuentra el producto o hay un error
    }

    /* Actualizar el stock actual de un producto */
    public boolean actualizarStockActual(int productoID, int nuevoStock) {
        String query = "UPDATE Productos SET StockActual = ? WHERE ProductoID = ?";
        try (Connection connection = ConexionDB.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setInt(1, nuevoStock);
            pstmt.setInt(2, productoID);

            int filasAfectadas = pstmt.executeUpdate();
            return filasAfectadas > 0;
        } catch (Exception e) {
            System.out.println("Error al actualizar el stock actual: " + e.getMessage());
        }
        return false; // Devuelve false si no se pudo actualizar
    }

    /* Listar todos los productos */
    public List<Map<String, Object>> listarTodosProductos() {
        List<Map<String, Object>> productos = new ArrayList<>();
        String query = "SELECT ProductoID, Nombre FROM Productos";

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
}
