package controller;

import service.KardexService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet(name = "EliminarMovimientoServlet", urlPatterns = {"/eliminarMovimiento"})
public class EliminarMovimientoServlet extends HttpServlet {
    private final KardexService kardexService = new KardexService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String kardexIDParam = request.getParameter("kardexID");
            if (kardexIDParam == null || kardexIDParam.trim().isEmpty()) {
                System.out.println("El parámetro 'kardexID' es nulo o vacío.");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            int kardexID = Integer.parseInt(kardexIDParam);
            System.out.println("kardexID recibido: " + kardexID);

            boolean eliminado = kardexService.eliminarLogicamente(kardexID);
            if (eliminado) {
                response.sendRedirect("listarCompras.jsp");
            } else {
                response.sendRedirect("listarCompras.jsp?error=NoSePudoEliminar");
            }
        } catch (NumberFormatException e) {
            System.out.println("Error al convertir 'kardexID' a entero: " + e.getMessage());
            response.sendRedirect("listarCompras.jsp?error=ParametroInvalido");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("listarCompras.jsp?error=ErrorServidor");
        }
    }
}



