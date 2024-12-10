package service;

import controller.KardexController;
import controller.ProductoController;
import db.ConexionDB;
import model.Kardex;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class KardexService {

    private final KardexController kardexController;
    private final ProductoController productoController;

    public KardexService() {
        this.kardexController = new KardexController();
        this.productoController = new ProductoController();
    }

    public List<Kardex> buscarComprasPorProducto(int productoID) {
        // Obtener todos los movimientos del producto
        List<Kardex> movimientos = kardexController.buscarTodosPorProducto(productoID);

        // Filtrar solo las compras
        return movimientos.stream()
                .filter(movimiento -> movimiento.getTipoMovimiento().equalsIgnoreCase("C"))
                .collect(Collectors.toList());
    }
    // Método para obtener el saldo actual del producto
    public int obtenerSaldoActual(int productoID) {
        return productoController.obtenerStockActual(productoID);
    }

    public List<Kardex> buscarPorEstado(String estado) {
        List<Kardex> movimientos = new ArrayList<>();
        String query = "SELECT * FROM Kardex WHERE Estado = ?";

        try (Connection connection = ConexionDB.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, estado);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Kardex movimiento = new Kardex();
                    movimiento.setKardexID(rs.getInt("KardexID"));
                    movimiento.setProductoID(rs.getInt("ProductoID"));
                    movimiento.setFecha(rs.getDate("Fecha"));
                    movimiento.setTipoMovimiento(rs.getString("TipoMovimiento"));
                    movimiento.setCantidad(rs.getInt("Cantidad"));
                    movimiento.setPrecioUnitario(rs.getDouble("PrecioUnitario"));
                    movimiento.setStockInicial(rs.getInt("StockInicial"));
                    movimiento.setStockFinal(rs.getInt("StockFinal"));
                    movimiento.setEstado(rs.getString("Estado").charAt(0));
                    movimientos.add(movimiento);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return movimientos;
    }

    public boolean reactivarMovimiento(int kardexID) {
        String query = "UPDATE Kardex SET Estado = 'A' WHERE KardexID = ?";
        try (Connection connection = ConexionDB.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, kardexID);
            return stmt.executeUpdate() > 0; // Devuelve true si se actualizó correctamente
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public List<Kardex> buscarPorProductoYEstado(int productoID, String estado) {
        List<Kardex> movimientos = new ArrayList<>();
        String query = "SELECT * FROM Kardex WHERE ProductoID = ? AND Estado = ? ORDER BY Fecha";

        try (Connection connection = ConexionDB.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, productoID);
            stmt.setString(2, estado);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Kardex movimiento = new Kardex();
                    movimiento.setKardexID(rs.getInt("KardexID"));
                    movimiento.setProductoID(rs.getInt("ProductoID"));
                    movimiento.setFecha(rs.getDate("Fecha"));
                    movimiento.setTipoMovimiento(rs.getString("TipoMovimiento"));
                    movimiento.setCantidad(rs.getInt("Cantidad"));
                    movimiento.setPrecioUnitario(rs.getDouble("PrecioUnitario"));
                    movimiento.setStockInicial(rs.getInt("StockInicial"));
                    movimiento.setStockFinal(rs.getInt("StockFinal"));
                    movimiento.setEstado(rs.getString("Estado").charAt(0));

                    movimientos.add(movimiento);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return movimientos;
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
    public List<Map<String, Object>> generarKardexAgrupado(int productoID) {
        int saldoInicial = productoController.obtenerStockInicial(productoID);

        List<Kardex> movimientos = kardexController.buscarTodosPorProducto(productoID);
        Map<String, Map<String, Object>> kardexAgrupado = new LinkedHashMap<>();
        int saldoAcumulado = saldoInicial;

        SimpleDateFormat monthYearFormatter = new SimpleDateFormat("MMMM yyyy", Locale.forLanguageTag("es"));

        for (Kardex movimiento : movimientos) {
            // Obtener clave del mes y año
            String mesClave = monthYearFormatter.format(movimiento.getFecha()).toUpperCase();

            // Si el mes no existe, inicializar una fila
            if (!kardexAgrupado.containsKey(mesClave)) {
                Map<String, Object> fila = new HashMap<>();
                fila.put("mes", mesClave);
                fila.put("fechaCompra", null);
                fila.put("cantidadCompra", 0);
                fila.put("montoCompra", 0.0);
                fila.put("fechaVenta", null);
                fila.put("cantidadVenta", 0);
                fila.put("montoVenta", 0.0);
                fila.put("saldoFinal", 0);
                fila.put("totalFinal", 0.0);
                kardexAgrupado.put(mesClave, fila);
            }

            Map<String, Object> fila = kardexAgrupado.get(mesClave);

            if (movimiento.getTipoMovimiento().equals("C")) {
                // Si no hay fecha de compra registrada, asignar la actual (primera compra del mes)
                if (fila.get("fechaCompra") == null) {
                    fila.put("fechaCompra", movimiento.getFecha());
                }
                fila.put("cantidadCompra", (int) fila.get("cantidadCompra") + movimiento.getCantidad());
                fila.put("montoCompra", (double) fila.get("montoCompra") + (movimiento.getCantidad() * movimiento.getPrecioUnitario()));
                saldoAcumulado += movimiento.getCantidad();
            }

            if (movimiento.getTipoMovimiento().equals("V")) {
                // Siempre actualizar con la última fecha de venta
                fila.put("fechaVenta", movimiento.getFecha());
                fila.put("cantidadVenta", (int) fila.get("cantidadVenta") + movimiento.getCantidad());
                fila.put("montoVenta", (double) fila.get("montoVenta") + (movimiento.getCantidad() * movimiento.getPrecioUnitario()));
                saldoAcumulado -= movimiento.getCantidad();
            }

            fila.put("saldoFinal", saldoAcumulado);
            fila.put("totalFinal", saldoAcumulado * movimiento.getPrecioUnitario());
        }

        return new ArrayList<>(kardexAgrupado.values());
    }

    // Método para registrar un movimiento en el Kardex y actualizar el stock en Productos
    public boolean registrarMovimiento(Kardex kardex) {
        try {
            // Validar el stock actual antes de registrar
            int stockActual = productoController.obtenerStockActual(kardex.getProductoID());
            int nuevoStock = kardex.getTipoMovimiento().equals("C") ?
                    stockActual + kardex.getCantidad() :
                    stockActual - kardex.getCantidad();

            // Validar que el nuevo stock no sea negativo
            if (nuevoStock < 0) {
                throw new IllegalArgumentException("El stock no puede ser negativo.");
            }

            // Registrar movimiento en Kardex
            boolean registrado = kardexController.registrarMovimiento(kardex);
            if (!registrado) {
                throw new Exception("No se pudo registrar el movimiento en Kardex.");
            }

            // Actualizar el stock en Productos
            boolean actualizado = productoController.actualizarStockActual(kardex.getProductoID(), nuevoStock);
            if (!actualizado) {
                throw new Exception("No se pudo actualizar el stock en Productos.");
            }

            return true;
        } catch (Exception e) {
            System.out.println("Error al registrar el movimiento: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizarMovimiento(Kardex movimiento) {
        String query = "UPDATE Kardex SET Cantidad = ?, PrecioUnitario = ?, StockInicial = ?, StockFinal = ? WHERE KardexID = ?";
        try (Connection connection = ConexionDB.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, movimiento.getCantidad());
            stmt.setDouble(2, movimiento.getPrecioUnitario());
            stmt.setInt(3, movimiento.getStockInicial());
            stmt.setInt(4, movimiento.getStockFinal());
            stmt.setInt(5, movimiento.getKardexID());
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Kardex> buscarComprasPorProductos(int productoID) {
        List<Kardex> compras = new ArrayList<>();
        String query = "SELECT * FROM Kardex WHERE ProductoID = ? AND Estado = 'A' AND TipoMovimiento = 'C' ORDER BY Fecha";

        try (Connection connection = ConexionDB.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, productoID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Kardex compra = new Kardex();
                    compra.setKardexID(rs.getInt("KardexID"));
                    compra.setProductoID(rs.getInt("ProductoID"));
                    compra.setFecha(rs.getDate("Fecha"));
                    compra.setTipoMovimiento(rs.getString("TipoMovimiento"));
                    compra.setCantidad(rs.getInt("Cantidad"));
                    compra.setPrecioUnitario(rs.getDouble("PrecioUnitario"));
                    compra.setStockInicial(rs.getInt("StockInicial"));
                    compra.setStockFinal(rs.getInt("StockFinal"));
                    compra.setEstado(rs.getString("Estado").charAt(0));

                    compras.add(compra);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return compras;
    }




    public boolean eliminarLogicamente(int kardexID) {
        String query = "UPDATE Kardex SET Estado = 'I' WHERE KardexID = ?";
        try (Connection connection = ConexionDB.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, kardexID);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }




}
