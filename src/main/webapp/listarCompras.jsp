<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="controller.KardexController" %>
<%@ page import="java.util.List" %>
<%@ page import="controller.ProductoController" %>
<%@ page import="model.Productos" %>
<%@ page import="service.KardexService" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="model.Kardex" %>

<%
    // Instanciar el servicio
    KardexService kardexService = new KardexService();
    ProductoController productController = new ProductoController();

    // Obtener la lista de productos
    List<Productos> productos = productController.listarTodos();

    // Obtener parámetros de búsqueda
    String productoID = request.getParameter("productoID");
    String estado = request.getParameter("estado"); // 'A' o 'I'

    Productos productoSeleccionado = null;
    List<Kardex> compras = null;

    // Lógica de búsqueda según el productoID y el estado
    if (productoID != null && !productoID.isEmpty()) {
        productoSeleccionado = productController.obtenerProductoPorID(Integer.parseInt(productoID));

        if (estado != null && (estado.equals("A") || estado.equals("I"))) {
            compras = kardexService.buscarPorProductoYEstado(Integer.parseInt(productoID), estado);
        } else {
            compras = kardexService.buscarComprasPorProducto(Integer.parseInt(productoID));
        }
    } else if (estado != null && (estado.equals("A") || estado.equals("I"))) {
        compras = kardexService.buscarPorEstado(estado);
    } else {
        compras = kardexService.buscarTodosActivos();
    }

    // Formateador de fechas
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMMM-yyyy", new java.util.Locale("es", "ES"));
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Listado de Compras</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/css/bootstrap.min.css">
</head>
<body>
<!-- Navbar -->
<nav class="navbar navbar-expand-lg navbar-dark bg-dark">
    <div class="container-fluid">
        <a class="navbar-brand" href="#">Sistema Kardex</a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarNav">
            <ul class="navbar-nav ms-auto">
                <li class="nav-item"><a class="nav-link" href="index.jsp">Inicio</a></li>
                <li class="nav-item"><a class="nav-link" href="movimientos.jsp">Movimientos</a></li>
                <li class="nav-item"><a class="nav-link" href="productos.jsp">Productos</a></li>
                <li class="nav-item"><a class="nav-link" href="registroKardex.jsp">Registro de Compras o Ventas</a></li>
                <li class="nav-item"><a class="nav-link" href="listarCompras.jsp">Listado compras</a></li>
            </ul>
        </div>
    </div>
</nav>

<div class="container mt-5">
    <!-- Filtro por producto -->
    <div class="row">
        <div class="col-md-8">
            <form action="listarCompras.jsp" method="get">
                <div class="mb-3">
                    <label for="productoID" class="form-label">Seleccionar Producto:</label>
                    <select id="productoID" name="productoID" class="form-select">
                        <option value="">Seleccione un producto</option>
                        <% for (Productos producto : productos) { %>
                        <option value="<%= producto.getProductoID() %>"
                                <%= (productoID != null && productoID.equals(String.valueOf(producto.getProductoID()))) ? "selected" : "" %>>
                            <%= producto.getNombre() %>
                        </option>
                        <% } %>
                    </select>
                </div>
                <button type="submit" class="btn btn-primary">Buscar</button>
            </form>
        </div>
    </div>

    <!-- Botones para listar activos/inactivos -->
    <% if (productoID != null && !productoID.isEmpty()) { %>
    <div class="row mt-3">
        <div class="col-md-6">
            <form action="listarCompras.jsp" method="get" style="display: inline;">
                <input type="hidden" name="productoID" value="<%= productoID %>">
                <input type="hidden" name="estado" value="A">
                <button type="submit" class="btn btn-success">Listar Activos</button>
            </form>
            <form action="listarCompras.jsp" method="get" style="display: inline;">
                <input type="hidden" name="productoID" value="<%= productoID %>">
                <input type="hidden" name="estado" value="I">
                <button type="submit" class="btn btn-danger">Listar Inactivos</button>
            </form>
        </div>
    </div>
    <% } %>

    <!-- Tabla de resultados -->
    <% if (compras != null && !compras.isEmpty()) { %>
    <div class="row mt-4">
        <div class="col-12">
            <h4 class="text-center">Listado de Compras</h4>
            <table class="table table-bordered">
                <thead>
                <tr>
                    <th>Fecha Compra</th>
                    <th>Cantidad</th>
                    <th>Precio Unitario</th>
                    <th>Monto Total</th>
                    <th>Acciones</th>
                </tr>
                </thead>
                <tbody>
                <% for (Kardex compra : compras) {
                    String fechaCompraStr = dateFormat.format(compra.getFecha());
                    double montoTotal = compra.getCantidad() * compra.getPrecioUnitario();
                %>
                <tr>
                    <td><%= fechaCompraStr %></td>
                    <td><%= compra.getCantidad() %></td>
                    <td><%= compra.getPrecioUnitario() %></td>
                    <td><%= montoTotal %></td>
                    <td>
                        <form action="eliminarMovimiento" method="post" style="display: inline;">
                            <input type="hidden" name="kardexID" value="<%= compra.getKardexID() %>">
                            <button type="submit" class="btn btn-sm btn-danger">Eliminar</button>
                        </form>
                    </td>
                </tr>
                <% } %>
                </tbody>
            </table>
        </div>
    </div>
    <% } else { %>
    <div class="row mt-4">
        <div class="col-12">
            <p class="text-center text-muted">No hay movimientos para mostrar.</p>
        </div>
    </div>
    <% } %>
</div>
</body>
</html>
