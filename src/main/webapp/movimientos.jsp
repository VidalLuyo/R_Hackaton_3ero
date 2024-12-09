<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="controller.KardexController" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="controller.ProductoController" %>
<%@ page import="model.Productos" %>
<%@ page import="service.KardexService" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Calendar" %>
<%
    // Instanciar el servicio
    KardexService kardexService = new KardexService();
    ProductoController productController = new ProductoController();

    // Obtener la lista de productos
    List<Productos> productos = productController.listarTodos();

    // Obtener producto seleccionado y movimientos
    String productoID = request.getParameter("productoID");
    String mesSeleccionado = request.getParameter("mes");
    Productos productoSeleccionado = null;
    List<Map<String, Object>> movimientos = null;

    if (productoID != null) {
        productoSeleccionado = productController.obtenerProductoPorID(Integer.parseInt(productoID));
        movimientos = kardexService.generarKardexAgrupado(Integer.parseInt(productoID));

        // Filtrar los movimientos si se seleccionó un mes
        if (mesSeleccionado != null && !mesSeleccionado.isEmpty()) {
            int mesFiltro = Integer.parseInt(mesSeleccionado);

            // Filtrar los movimientos según el mes
            movimientos = movimientos.stream()
                    .filter(movimiento -> {
                        Date fechaCompra = (Date) movimiento.get("fechaCompra");
                        Date fechaVenta = (Date) movimiento.get("fechaVenta");
                        Calendar calendar = Calendar.getInstance();

                        // Verificar fecha de compra
                        if (fechaCompra != null) {
                            calendar.setTime(fechaCompra);
                            if (calendar.get(Calendar.MONTH) + 1 == mesFiltro) {
                                return true;
                            }
                        }

                        // Verificar fecha de venta
                        if (fechaVenta != null) {
                            calendar.setTime(fechaVenta);
                            if (calendar.get(Calendar.MONTH) + 1 == mesFiltro) {
                                return true;
                            }
                        }

                        return false;
                    })
                    .toList(); // Convertir nuevamente a lista
        }
    }

    // Configurar el formato deseado para las fechas
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMMM-yyyy", new java.util.Locale("es", "ES"));
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Kardex</title>
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
    <div class="row">
        <div class="col-md-6">
            <h2>Kardex - Selección de Producto</h2>

            <!-- Formulario de selección -->
            <form action="movimientos.jsp" method="get">
                <div class="mb-3">
                    <label for="productoID" class="form-label">Seleccionar Producto:</label>
                    <select id="productoID" name="productoID" class="form-select" required>
                        <%
                            for (Productos producto : productos) {
                        %>
                        <option value="<%= producto.getProductoID() %>"
                                <%= (productoID != null && productoID.equals(String.valueOf(producto.getProductoID()))) ? "selected" : "" %>>
                            <%= producto.getNombre() %>
                        </option>
                        <%
                            }
                        %>
                    </select>
                </div>
                <div class="mb-3">
                    <label for="mes" class="form-label">Seleccionar Mes:</label>
                    <select id="mes" name="mes" class="form-select">
                        <option value="">Todos</option>
                        <option value="1" <%= "1".equals(mesSeleccionado) ? "selected" : "" %>>Enero</option>
                        <option value="2" <%= "2".equals(mesSeleccionado) ? "selected" : "" %>>Febrero</option>
                        <option value="3" <%= "3".equals(mesSeleccionado) ? "selected" : "" %>>Marzo</option>
                        <option value="4" <%= "4".equals(mesSeleccionado) ? "selected" : "" %>>Abril</option>
                        <option value="5" <%= "5".equals(mesSeleccionado) ? "selected" : "" %>>Mayo</option>
                        <option value="6" <%= "6".equals(mesSeleccionado) ? "selected" : "" %>>Junio</option>
                        <option value="7" <%= "7".equals(mesSeleccionado) ? "selected" : "" %>>Julio</option>
                        <option value="8" <%= "8".equals(mesSeleccionado) ? "selected" : "" %>>Agosto</option>
                        <option value="9" <%= "9".equals(mesSeleccionado) ? "selected" : "" %>>Septiembre</option>
                        <option value="10" <%= "10".equals(mesSeleccionado) ? "selected" : "" %>>Octubre</option>
                        <option value="11" <%= "11".equals(mesSeleccionado) ? "selected" : "" %>>Noviembre</option>
                        <option value="12" <%= "12".equals(mesSeleccionado) ? "selected" : "" %>>Diciembre</option>
                    </select>
                </div>
                <button type="submit" class="btn btn-primary">Buscar</button>
            </form>
        </div>

        <% if (productoSeleccionado != null) { %>
        <div class="col-md-6">
            <!-- Detalles del producto seleccionado -->
            <h4>Detalles del Producto</h4>
            <table class="table table-bordered">
                <tr>
                    <th>Código:</th>
                    <td><%= productoSeleccionado.getCodigoProducto() %></td>
                </tr>
                <tr>
                    <th>Nombre:</th>
                    <td><%= productoSeleccionado.getNombre() %></td>
                </tr>
                <tr>
                    <th>Precio de Compra:</th>
                    <td><%= productoSeleccionado.getPrecioCompra() %></td>
                </tr>
                <tr>
                    <th>Precio de Venta:</th>
                    <td><%= productoSeleccionado.getPrecioVenta() %></td>
                </tr>
                <tr>
                    <th>Stock Inicial:</th>
                    <td><%= productoSeleccionado.getStockInicial() %></td>
                </tr>
                <tr>
                    <th>Total del Stock Inicial (Compra):</th>
                    <td><%= productoSeleccionado.getStockInicial() * productoSeleccionado.getPrecioCompra() %></td>
                </tr>
                <tr>
                    <th>Stock Actual:</th>
                    <td>
                        <%
                            int stockActual = productoSeleccionado.getStockInicial();
                            if (movimientos != null) {
                                for (Map<String, Object> movimiento : movimientos) {
                                    stockActual += (int) movimiento.get("cantidadCompra") - (int) movimiento.get("cantidadVenta");
                                }
                            }
                            out.print(stockActual);
                        %>
                    </td>
                </tr>
            </table>
        </div>
        <% } %>
    </div>

    <% if (movimientos != null && !movimientos.isEmpty()) { %>
    <div class="row mt-4">
        <div class="col-12">
            <h4 class="text-center">Kardex</h4>
            <table class="table table-bordered">
                <thead>
                <tr>
                    <th>Mes</th>
                    <th>Stock Inicial</th>
                    <th>Fecha Compra</th>
                    <th>Cantidad Compra</th>
                    <th>Monto Compra</th>
                    <th>Fecha Venta</th>
                    <th>Cantidad Venta</th>
                    <th>Monto Venta</th>
                    <th>Saldo Final</th>
                    <th>Total Final</th>
                </tr>
                </thead>
                <tbody>
                <%
                    int saldoAcumulado = productoSeleccionado.getStockInicial();
                    for (Map<String, Object> movimiento : movimientos) {
                        int cantidadCompra = (int) movimiento.get("cantidadCompra");
                        int cantidadVenta = (int) movimiento.get("cantidadVenta");
                        double precioCompra = productoSeleccionado.getPrecioCompra();
                        double precioVenta = productoSeleccionado.getPrecioVenta();

                        saldoAcumulado += cantidadCompra - cantidadVenta;

                        Date fechaCompra = (Date) movimiento.get("fechaCompra");
                        Date fechaVenta = (Date) movimiento.get("fechaVenta");

                        String fechaCompraStr = fechaCompra != null ? dateFormat.format(fechaCompra) : "";
                        String fechaVentaStr = fechaVenta != null ? dateFormat.format(fechaVenta) : "";
                %>
                <tr>
                    <td><%= movimiento.get("mes") %></td>
                    <td><%= saldoAcumulado - cantidadCompra + cantidadVenta %></td>
                    <td><%= fechaCompraStr %></td>
                    <td><%= cantidadCompra %></td>
                    <td><%= cantidadCompra * precioCompra %></td>
                    <td><%= fechaVentaStr %></td>
                    <td><%= cantidadVenta %></td>
                    <td><%= cantidadVenta * precioVenta %></td>
                    <td><%= saldoAcumulado %></td>
                    <td><%= saldoAcumulado * precioCompra %></td>
                </tr>
                <%
                    }
                %>
                </tbody>
            </table>
        </div>
    </div>
    <% } %>
</div>
</body>
</html>
