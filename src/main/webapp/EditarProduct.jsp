<%@ page import="controller.ProductoC" %>
<%@ page import="model.Productos" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%
    // Obtener el ID del producto a editar
    int productoID = Integer.parseInt(request.getParameter("id"));

    // Instanciar el controlador y buscar el producto
    ProductoC productoController = new ProductoC();
    Productos producto = productoController.buscarPorId(productoID);
%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Editar Producto</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css">
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
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
    <h1 class="text-center">Editar Producto</h1>
    <div class="row justify-content-center">
        <div class="col-md-6">
            <form id="editarForm" action="ProductoService" method="POST" accept-charset="UTF-8">

                <!-- Código del Producto -->
                <div class="mb-3">
                    <label for="codigoProducto" class="form-label">Código del Producto</label>
                    <input type="text" class="form-control" id="codigoProducto" name="codigoProducto"
                           value="<%= producto.getCodigoProducto() %>" readonly>
                </div>

                <!-- Nombre del Producto -->
                <div class="mb-3">
                    <label for="nombre" class="form-label">Marca del producto</label>
                    <input type="text" class="form-control" id="nombre" name="nombre"
                           value="<%= producto.getNombre() %>" required>
                </div>

                <!-- Precio de Compra -->
                <div class="mb-3">
                    <label for="precioCompra" class="form-label">Precio de Compra</label>
                    <input type="number" class="form-control" id="precioCompra" name="precioCompra"
                           value="<%= producto.getPrecioCompra() %>" required min="1" step="1">
                    <div class="invalid-feedback">
                        El precio de compra debe ser un número entero mayor a 0.
                    </div>
                </div>

                <!-- Precio de Venta -->
                <div class="mb-3">
                    <label for="precioVenta" class="form-label">Precio de Venta</label>
                    <input type="number" class="form-control" id="precioVenta" name="precioVenta"
                           value="<%= producto.getPrecioVenta() %>" required min="1" step="1">
                    <div class="invalid-feedback">
                        El precio de venta debe ser un número entero mayor a 0.
                    </div>
                </div>

                <!-- Stock Inicial -->
                <div class="mb-3">
                    <label for="stockInicial" class="form-label">Stock Inicial</label>
                    <input type="number" class="form-control" id="stockInicial" name="stockInicial"
                           value="<%= producto.getStockInicial() %>" required min="1" step="1">
                    <div class="invalid-feedback">
                        El stock inicial debe ser un número entero mayor a 0.
                    </div>
                </div>

                <!-- Stock Actual -->
                <div class="mb-3">
                    <label for="stockActual" class="form-label">Stock Actual</label>
                    <input type="number" class="form-control" id="stockActual" name="stockActual"
                           value="<%= producto.getStockActual() %>" required min="1" step="1">
                    <div class="invalid-feedback">
                        El stock actual debe ser un número entero mayor a 0.
                    </div>
                </div>

                <!-- Acción -->
                <input type="hidden" name="action" value="edit">
                <input type="hidden" name="productoID" value="<%= producto.getProductoID() %>">

                <!-- Botón de Envío -->
                <div class="d-grid">
                    <button type="submit" class="btn btn-primary">Actualizar Producto</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
    document.querySelector("#editarForm").addEventListener("submit", function (event) {
        const precioCompra = document.getElementById("precioCompra");
        const precioVenta = document.getElementById("precioVenta");
        const stockInicial = document.getElementById("stockInicial");
        const stockActual = document.getElementById("stockActual");

        let isValid = true;

        // Validar Precio de Compra
        if (precioCompra.value <= 0 || !Number.isInteger(Number(precioCompra.value))) {
            precioCompra.classList.add("is-invalid");
            isValid = false;
        } else {
            precioCompra.classList.remove("is-invalid");
        }

        // Validar Precio de Venta
        if (precioVenta.value <= 0 || !Number.isInteger(Number(precioVenta.value))) {
            precioVenta.classList.add("is-invalid");
            isValid = false;
        } else {
            precioVenta.classList.remove("is-invalid");
        }

        // Validar Stock Inicial
        if (stockInicial.value <= 0 || !Number.isInteger(Number(stockInicial.value))) {
            stockInicial.classList.add("is-invalid");
            isValid = false;
        } else {
            stockInicial.classList.remove("is-invalid");
        }

        // Validar Stock Actual
        if (stockActual.value <= 0 || !Number.isInteger(Number(stockActual.value))) {
            stockActual.classList.add("is-invalid");
            isValid = false;
        } else {
            stockActual.classList.remove("is-invalid");
        }

        // Prevenir el envío si los valores no son válidos
        if (!isValid) {
            event.preventDefault();
            return;
        }

        // Mostrar SweetAlert de éxito y redirigir al listado
        event.preventDefault(); // Prevenir envío por defecto
        Swal.fire({
            title: "Producto Actualizado",
            text: "El producto se ha actualizado correctamente.",
            icon: "success",
            confirmButtonText: "OK",
        }).then((result) => {
            if (result.isConfirmed) {
                document.querySelector("#editarForm").submit(); // Enviar el formulario después del mensaje
            }
        });
    });
</script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
