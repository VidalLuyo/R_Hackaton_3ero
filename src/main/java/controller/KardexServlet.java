package controller;

import com.google.gson.Gson;
import model.Productos;
import service.KardexService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "KardexServlet", urlPatterns = {"/buscarKardex", "/obtenerPrecio"})
public class KardexServlet extends HttpServlet {

    private final KardexService kardexService;

    public KardexServlet() {
        this.kardexService = new KardexService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getServletPath();

        if ("/buscarKardex".equals(path)) {
            buscarKardex(request, response);
        } else if ("/obtenerPrecio".equals(path)) {
            obtenerPrecio(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Ruta no encontrada.");
        }
    }

    private void buscarKardex(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            int productoID = Integer.parseInt(request.getParameter("productoID"));
            Productos productoSeleccionado = ProductoController.obtenerProductoPorID(productoID);
            List<Map<String, Object>> kardexAgrupado = kardexService.generarKardexAgrupado(productoID);
            List<Productos> productos = ProductoController.listarTodos();

            request.setAttribute("productoSeleccionado", productoSeleccionado);
            request.setAttribute("productos", productos);
            request.setAttribute("kardexAgrupado", kardexAgrupado);
            request.getRequestDispatcher("movimientos.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al procesar la solicitud.");
        }
    }

    private void obtenerPrecio(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // Obtener el parámetro productoID y verificar si es válido
            String productoIDParam = request.getParameter("productoID");
            String tipoMovimiento = request.getParameter("tipoMovimiento");

            if (productoIDParam == null || productoIDParam.trim().isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "El productoID es obligatorio.");
                return;
            }

            int productoID = Integer.parseInt(productoIDParam); // Convertir a entero
            Productos producto = ProductoController.obtenerProductoPorID(productoID);
            if (producto == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Producto no encontrado.");
                return;
            }

            // Obtener el precio unitario según el tipo de movimiento
            double precioUnitario = 0.0;
            if ("C".equals(tipoMovimiento)) {
                precioUnitario = producto.getPrecioCompra(); // Precio de compra
            } else if ("V".equals(tipoMovimiento)) {
                precioUnitario = producto.getPrecioVenta(); // Precio de venta
            }

            // Responder en formato JSON
            Map<String, Object> jsonResponse = new HashMap<>();
            jsonResponse.put("precioUnitario", precioUnitario);

            String json = new Gson().toJson(jsonResponse);
            response.setContentType("application/json");
            response.getWriter().write(json);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "El productoID debe ser un número válido.");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al obtener el precio unitario.");
        }
    }

}

