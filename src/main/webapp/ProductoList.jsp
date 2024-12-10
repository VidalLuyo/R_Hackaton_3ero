<%@ page import="controller.ProductoC" %>
<%@ page import="model.Productos" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%
    ProductoC productoController = new ProductoC();

    String estado = request.getParameter("estado");
    if (estado == null || (!estado.equals("A") && !estado.equals("I"))) {
        estado = "A";
    }
    List<Productos> productosAMostrar = productoController.listarPorEstado(estado);

    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMMM-yyyy", new java.util.Locale("es", "ES"));
%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Listado de Productos</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdn.datatables.net/1.13.5/css/jquery.dataTables.min.css">
    <link rel="stylesheet" href="https://cdn.datatables.net/1.13.5/css/dataTables.bootstrap5.min.css">
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
                <li class="nav-item"><a class="nav-link" href="ProductoList.jsp">Productos</a></li>
                <li class="nav-item"><a class="nav-link" href="registroKardex.jsp">Registro de Compras o Ventas</a></li>
                <li class="nav-item"><a class="nav-link" href="listarCompras.jsp">Listado compras</a></li>
            </ul>
        </div>
    </div>
</nav>

<div class="container mt-4">
    <h1 class="text-center mb-4">Listado de Productos</h1>

    <!-- Botones y campo de búsqueda -->
    <div class="d-flex justify-content-between mb-3">
        <div>
            <a href="ProductoList.jsp?estado=A" class="btn btn-success">
                <i class="fas fa-check-circle"></i> Activos
            </a>
            <a href="ProductoList.jsp?estado=I" class="btn btn-danger">
                <i class="fas fa-times-circle"></i> Inactivos
            </a>
            <a href="RegistrarProduct.jsp" class="btn btn-primary">
                <i class="fas fa-plus-circle"></i> Agregar Producto
            </a>
        </div>
        <div>
            <a href="ProductoService?action=export&type=pdf&estado=<%= estado %>" class="btn btn-danger">
                <i class="fas fa-file-pdf"></i> Exportar PDF
            </a>
            <a href="ProductoService?action=export&type=csv&estado=<%= estado %>" class="btn btn-info">
                <i class="fas fa-file-csv"></i> Exportar CSV
            </a>
            <a href="ProductoService?action=export&type=xls&estado=<%= estado %>" class="btn btn-success">
                <i class="fas fa-file-excel"></i> Exportar XLS
            </a>
        </div>
    </div>

    <!-- Campo de búsqueda -->
    <div class="mb-3">
        <input type="text" id="searchCodigo" class="form-control" placeholder="Buscar por código de producto">
    </div>

    <!-- Tabla de productos -->
    <div class="table-responsive">
        <table id="productosTable" class="table table-striped table-bordered">
            <thead>
            <tr>
                <th>Código</th>
                <th>Nombre</th>
                <th>Precio Compra</th>
                <th>Precio Venta</th>
                <th>Stock Inicial</th>
                <th>Stock Actual</th>
                <th>Fecha Registro</th>
                <th>Acciones</th>
            </tr>
            </thead>
            <tbody>
            <%
                if (productosAMostrar.isEmpty()) {
            %>
            <tr>
                <td colspan="8" class="text-center">No hay productos disponibles.</td>
            </tr>
            <%
            } else {
                for (Productos producto : productosAMostrar) {
            %>
            <tr>
                <td class="text-center"><%= producto.getCodigoProducto() %></td>
                <td class="text-center"><%= producto.getNombre() %></td>
                <td class="text-center">S/. <%= producto.getPrecioCompra() %></td>
                <td class="text-center">S/. <%= producto.getPrecioVenta() %></td>
                <td class="text-center"><%= producto.getStockInicial() %></td>
                <td class="text-center"><%= producto.getStockActual() %></td>
                <td class="text-center"><%= dateFormat.format(producto.getFechaRegistro()) %></td>
                <td class="text-center">
                    <% if (estado.equals("I")) { %>
                    <button class="btn btn-success btn-sm"
                            onclick="confirmAction('reactivate', <%= producto.getProductoID() %>)">
                        <i class="fas fa-undo"></i> Reactivar
                    </button>
                    <% } else { %>
                    <a href="EditarProduct.jsp?id=<%= producto.getProductoID() %>" class="btn btn-primary btn-sm">
                        <i class="fas fa-edit"></i> Editar
                    </a>
                    <button class="btn btn-danger btn-sm"
                            onclick="confirmAction('delete', <%= producto.getProductoID() %>)">
                        <i class="fas fa-trash"></i> Eliminar
                    </button>
                    <% } %>
                </td>
            </tr>
            <%
                    }
                }
            %>
            </tbody>
        </table>
    </div>
</div>

<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.datatables.net/1.13.5/js/jquery.dataTables.min.js"></script>
<script src="https://cdn.datatables.net/1.13.5/js/dataTables.bootstrap5.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>

<script>
    $(document).ready(function () {
        const table = $('#productosTable').DataTable({
            "pageLength": 10,
            "lengthMenu": [10, 15, 20],
            "language": {
                "url": "//cdn.datatables.net/plug-ins/1.13.5/i18n/es-ES.json"
            },
            "dom": '<"top">rt<"bottom"ip><"clear">'
        });

        // Filtro por código
        $('#searchCodigo').on('keyup', function () {
            table.column(0).search(this.value).draw();
        });
    });

    function confirmAction(action, productoID) {
        const message = action === "delete"
            ? "¿Estás seguro de eliminar este producto?"
            : "¿Estás seguro de reactivar este producto?";

        const confirmButtonText = action === "delete" ? "Eliminar" : "Reactivar";

        Swal.fire({
            title: message,
            icon: "warning",
            showCancelButton: true,
            confirmButtonColor: "#d33",
            cancelButtonColor: "#3085d6",
            confirmButtonText: confirmButtonText,
            cancelButtonText: "Cancelar"
        }).then((result) => {
            if (result.isConfirmed) {
                const form = document.createElement("form");
                form.method = "POST";
                form.action = "ProductoService";

                const actionInput = document.createElement("input");
                actionInput.type = "hidden";
                actionInput.name = "action";
                actionInput.value = action;
                form.appendChild(actionInput);

                const idInput = document.createElement("input");
                idInput.type = "hidden";
                idInput.name = "productoID";
                idInput.value = productoID;
                form.appendChild(idInput);

                document.body.appendChild(form);
                form.submit();
            }
        });
    }
</script>
<script>
    const urlParams = new URLSearchParams(window.location.search);
    const success = urlParams.get('success');

    if (success) {
        const message = success === "delete"
            ? "Producto eliminado exitosamente."
            : "Producto reactivado exitosamente.";
        Swal.fire({
            title: "Éxito",
            text: message,
            icon: "success",
            confirmButtonColor: "#3085d6",
            confirmButtonText: "OK"
        });
    }
</script>

</body>
</html>
