package controller;

import service.KardexService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "ReactivarMovimientoServlet", urlPatterns = {"/reactivarMovimiento"})
public class ReactivarMovimientoServlet extends HttpServlet {
    private final KardexService kardexService = new KardexService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int kardexID = Integer.parseInt(request.getParameter("kardexID"));
            boolean reactivado = kardexService.reactivarMovimiento(kardexID); // Método para reactivar el movimiento

            if (reactivado) {
                response.sendRedirect("listarCompras.jsp"); // Redirigir de vuelta a la página de listado
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No se pudo reactivar el movimiento.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Solicitud inválida.");
        }
    }
}
