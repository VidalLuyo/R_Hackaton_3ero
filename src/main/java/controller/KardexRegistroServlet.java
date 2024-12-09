package controller;

import model.Kardex;
import service.KardexService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "KardexRegistroServlet", urlPatterns = {"/registrarMovimiento"})
public class KardexRegistroServlet extends HttpServlet {

    private final KardexService kardexService;

    public KardexRegistroServlet() {
        this.kardexService = new KardexService();
    }
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String movimientoIdStr = request.getParameter("movimientoId");
        if (movimientoIdStr != null && !movimientoIdStr.isEmpty()) {
            int movimientoId = Integer.parseInt(movimientoIdStr);
            Kardex compra = KardexController.obtenerCompraPorId(movimientoId);

            if (compra != null) {
                request.setAttribute("compra", compra);
            } else {
                request.setAttribute("error", "No se encontró la compra con el ID especificado.");
            }
        } else {
            request.setAttribute("error", "El parámetro movimientoId es inválido o está vacío.");
        }
        request.getRequestDispatcher("/editarCompra.jsp").forward(request, response);
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // Obtener datos del formulario
            int productoID = Integer.parseInt(request.getParameter("productoID"));
            String tipoMovimiento = request.getParameter("tipoMovimiento");
            int cantidad = Integer.parseInt(request.getParameter("cantidad"));
            double precioUnitario = Double.parseDouble(request.getParameter("precioUnitario"));
            java.sql.Date fecha = java.sql.Date.valueOf(request.getParameter("fecha"));

            // Crear y registrar el movimiento en Kardex
            Kardex kardex = new Kardex();
            kardex.setProductoID(productoID);
            kardex.setTipoMovimiento(tipoMovimiento);
            kardex.setCantidad(cantidad);
            kardex.setPrecioUnitario(precioUnitario);
            kardex.setFecha(fecha);
            kardex.setMontoTotal(cantidad * precioUnitario);

            boolean exito = kardexService.registrarMovimiento(kardex);

            if (exito) {
                // Redirigir a movimientos.jsp después de registrar
                response.sendRedirect("movimientos.jsp");
            } else {
                // Manejar error en el registro
                request.setAttribute("error", "Error al registrar el movimiento.");
                request.getRequestDispatcher("registroKardex.jsp").forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error en el procesamiento de los datos.");
            request.getRequestDispatcher("registroKardex.jsp").forward(request, response);
        }
    }

}
