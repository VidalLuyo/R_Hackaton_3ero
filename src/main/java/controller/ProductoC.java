package controller;

import db.ConexionDB;
import model.Productos;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoC {

    // Generar siguiente código de producto (PCJM001, PCJM002, ...)
    public String generarSiguienteCodigoProducto() {
        String ultimoCodigo = "PCJM000";
        String query = "SELECT TOP 1 CodigoProducto FROM Productos ORDER BY ProductoID DESC";

        try (Connection connection = ConexionDB.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                ultimoCodigo = rs.getString("CodigoProducto").trim();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Generar el siguiente código correlativo
        int numero = Integer.parseInt(ultimoCodigo.substring(4)) + 1;
        return String.format("PCJM%03d", numero);
    }

    // Listar todos los productos
    public List<Productos> listarTodos() {
        List<Productos> productList = new ArrayList<>();
        String query = "SELECT * FROM Productos";

        try (Connection connection = ConexionDB.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Productos producto = new Productos();
                producto.setProductoID(rs.getInt("ProductoID"));
                producto.setCodigoProducto(rs.getString("CodigoProducto"));
                producto.setNombre(rs.getString("Nombre"));
                producto.setPrecioCompra(rs.getDouble("PrecioCompra"));
                producto.setPrecioVenta(rs.getDouble("PrecioVenta"));
                producto.setStockInicial(rs.getInt("StockInicial"));
                producto.setStockActual(rs.getInt("StockActual"));
                producto.setFechaRegistro(rs.getDate("FechaRegistro"));

                productList.add(producto);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return productList;
    }

    // Agregar un nuevo producto
    public void agregarProducto(Productos producto) {
        String query = "INSERT INTO Productos (CodigoProducto, Nombre, PrecioCompra, PrecioVenta, StockInicial, StockActual, FechaRegistro) " +
                "VALUES (?, ?, ?, ?, ?, ?, GETDATE())";

        // Generar el siguiente código de producto correlativo
        producto.setCodigoProducto(generarSiguienteCodigoProducto());

        try (Connection connection = ConexionDB.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setString(1, producto.getCodigoProducto());
            pstmt.setString(2, producto.getNombre());
            pstmt.setDouble(3, producto.getPrecioCompra());
            pstmt.setDouble(4, producto.getPrecioVenta());
            pstmt.setInt(5, producto.getStockInicial());
            pstmt.setInt(6, producto.getStockInicial()); // Stock inicial igual al actual

            pstmt.executeUpdate();
            System.out.println("Producto creado exitosamente con código: " + producto.getCodigoProducto());
        } catch (Exception e) {
            System.out.println("Error al crear el producto.");
            e.printStackTrace();
        }
    }

    // Editar un producto
    public void editarProducto(Productos producto) {
        String query = "UPDATE Productos SET Nombre = ?, PrecioCompra = ?, PrecioVenta = ?, StockInicial = ?, StockActual = ? WHERE ProductoID = ?";

        try (Connection connection = ConexionDB.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setString(1, producto.getNombre());
            pstmt.setDouble(2, producto.getPrecioCompra());
            pstmt.setDouble(3, producto.getPrecioVenta());
            pstmt.setInt(4, producto.getStockInicial());
            pstmt.setInt(5, producto.getStockActual());
            pstmt.setInt(6, producto.getProductoID());

            pstmt.executeUpdate();
            System.out.println("Producto actualizado exitosamente.");
        } catch (Exception e) {
            System.out.println("Error al actualizar el producto.");
            e.printStackTrace();
        }
    }

    // Eliminar un producto lógicamente (Estado = 'I')
    public void eliminarLogico(int productoID) {
        String query = "UPDATE Productos SET Estado = 'I' WHERE ProductoID = ?";

        try (Connection connection = ConexionDB.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setInt(1, productoID);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Producto marcado como inactivo (eliminado lógicamente): " + productoID);
            } else {
                System.out.println("No se encontró el producto para eliminar lógicamente: " + productoID);
            }
        } catch (Exception e) {
            System.out.println("Error al marcar el producto como inactivo.");
            e.printStackTrace();
        }
    }


    // Reactivar un producto (Estado = 'A')
    public void reactivarProducto(int productoID) {
        String query = "UPDATE Productos SET Estado = 'A' WHERE ProductoID = ?";

        try (Connection connection = ConexionDB.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setInt(1, productoID);
            pstmt.executeUpdate();
            System.out.println("Producto reactivado exitosamente.");
        } catch (Exception e) {
            System.out.println("Error al reactivar el producto.");
            e.printStackTrace();
        }
    }

    // Buscar un producto por ID
    public Productos buscarPorId(int productoID) {
        Productos producto = null;
        String query = "SELECT * FROM Productos WHERE ProductoID = ?";

        try (Connection connection = ConexionDB.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setInt(1, productoID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                producto = new Productos();
                producto.setProductoID(rs.getInt("ProductoID"));
                producto.setCodigoProducto(rs.getString("CodigoProducto"));
                producto.setNombre(rs.getString("Nombre"));
                producto.setPrecioCompra(rs.getDouble("PrecioCompra"));
                producto.setPrecioVenta(rs.getDouble("PrecioVenta"));
                producto.setStockInicial(rs.getInt("StockInicial"));
                producto.setStockActual(rs.getInt("StockActual"));
                producto.setFechaRegistro(rs.getDate("FechaRegistro"));
            }
        } catch (Exception e) {
            System.out.println("Error al buscar el producto.");
            e.printStackTrace();
        }
        return producto;
    }

    // Listar productos por estado (Activos o Inactivos)
    public List<Productos> listarPorEstado(String estado) {
        List<Productos> productList = new ArrayList<>();
        String query = "SELECT * FROM Productos WHERE Estado = ?";

        try (Connection connection = ConexionDB.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setString(1, estado);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Productos producto = new Productos();
                producto.setProductoID(rs.getInt("ProductoID"));
                producto.setCodigoProducto(rs.getString("CodigoProducto"));
                producto.setNombre(rs.getString("Nombre"));
                producto.setPrecioCompra(rs.getDouble("PrecioCompra"));
                producto.setPrecioVenta(rs.getDouble("PrecioVenta"));
                producto.setStockInicial(rs.getInt("StockInicial"));
                producto.setStockActual(rs.getInt("StockActual")); // Valor actualizado
                producto.setFechaRegistro(rs.getDate("FechaRegistro"));

                productList.add(producto);
            }
        } catch (Exception e) {
            System.out.println("Error al listar productos por estado.");
            e.printStackTrace();
        }
        return productList;
    }

}
