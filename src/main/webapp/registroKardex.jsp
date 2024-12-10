<%@ page import="java.util.Map" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="controller.KardexController" %>
<%@ page import="java.util.List" %>
<%
    KardexController kardexController = new KardexController();
    List<Map<String, Object>> productos = kardexController.listarTodosProductos();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Registro de Movimiento</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/css/bootstrap.min.css">
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
    <style>
        .is-valid { border-color: #198754; }
        .is-invalid { border-color: #dc3545; }
    </style>
    <script>
        function inicializarFormulario() {
            const precioUnitarioField = document.getElementById("precioUnitario");
            const cantidadField = document.getElementById("cantidad");
            const totalField = document.getElementById("total");
            const form = document.querySelector("form");

            precioUnitarioField.value = "1000.00";

            cantidadField.addEventListener("input", () => {
                const cantidad = cantidadField.value;
                if (/^\d+$/.test(cantidad)) {
                    cantidadField.classList.remove("is-invalid");
                    cantidadField.classList.add("is-valid");
                    totalField.value = (cantidad * parseFloat(precioUnitarioField.value)).toFixed(2);
                } else {
                    cantidadField.classList.remove("is-valid");
                    cantidadField.classList.add("is-invalid");
                    totalField.value = "0.00";
                }
            });

            form.addEventListener("submit", (e) => {
                e.preventDefault();
                if (!cantidadField.classList.contains("is-valid")) {
                    Swal.fire({
                        icon: "error",
                        title: "Error",
                        text: "Por favor, ingresa una cantidad válida.",
                    });
                    return;
                }

                Swal.fire({
                    icon: "success",
                    title: "Movimiento registrado",
                    text: "El movimiento se registró correctamente.",
                }).then(() => {
                    form.submit();
                });
            });
        }

        document.addEventListener("DOMContentLoaded", inicializarFormulario);
    </script>
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
<div class="container mt-5">
    <h2>Registrar Movimiento</h2>
    <form action="registrarMovimiento" method="post">
        <div class="mb-3">
            <label for="productoID">Producto:</label>
            <select id="productoID" name="productoID" class="form-select" required>
                <% for (Map<String, Object> producto : productos) { %>
                <option value="<%= producto.get("productoID") %>"><%= producto.get("nombre") %></option>
                <% } %>
            </select>
        </div>
        <div class="mb-3">
            <label for="tipoMovimiento">Tipo de Movimiento:</label>
            <select id="tipoMovimiento" name="tipoMovimiento" class="form-select" required>
                <option value="C">Compra</option>
                <option value="V">Venta</option>
            </select>
        </div>
        <div class="mb-3">
            <label for="cantidad">Cantidad:</label>
            <input type="number" id="cantidad" name="cantidad" class="form-control" min="1" required>
        </div>
        <div class="mb-3">
            <label for="precioUnitario">Precio Unitario:</label>
            <input type="number" id="precioUnitario" name="precioUnitario" step="0.01" class="form-control" readonly>
        </div>
        <div class="mb-3">
            <label for="total">Total:</label>
            <input type="number" id="total" name="total" step="0.01" class="form-control" readonly>
        </div>
        <div class="mb-3">
            <label for="fecha">Fecha:</label>
            <input type="date" id="fecha" name="fecha" class="form-control" required>
        </div>
        <button type="submit" class="btn btn-primary">Registrar</button>
    </form>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
