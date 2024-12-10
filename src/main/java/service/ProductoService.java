package service;

import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import controller.ProductoC;
import model.Productos;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.util.List;

@WebServlet("/ProductoService")
public class ProductoService extends HttpServlet {

    private final ProductoC productoController = new ProductoC();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Configurar la codificación de la solicitud y la respuesta
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");

        String action = request.getParameter("action");

        if ("add".equals(action)) {
            try {
                String nombre = request.getParameter("nombre");
                double precioCompra = Double.parseDouble(request.getParameter("precioCompra"));
                double precioVenta = Double.parseDouble(request.getParameter("precioVenta"));
                int stockInicial = Integer.parseInt(request.getParameter("stockInicial"));

                // Validar que los precios sean mayores a 0
                if (precioCompra <= 0 || precioVenta <= 0) {
                    throw new IllegalArgumentException("Los precios deben ser mayores a 0.");
                }

                // Crear el nuevo producto
                Productos nuevoProducto = new Productos();
                nuevoProducto.setNombre(nombre);
                nuevoProducto.setPrecioCompra(precioCompra);
                nuevoProducto.setPrecioVenta(precioVenta);
                nuevoProducto.setStockInicial(stockInicial);
                nuevoProducto.setStockActual(stockInicial);

                // Llamar al controlador para agregar el producto
                productoController.agregarProducto(nuevoProducto);

                // Redirigir al listado de productos
                response.sendRedirect("ProductoList.jsp");
            } catch (Exception e) {
                // Manejo de errores (redirigir a un mensaje de error)
                e.printStackTrace();
                response.sendRedirect("RegistrarProduct.jsp?error=validation");
            }
        } else if ("edit".equals(action)) {
            int productoID = Integer.parseInt(request.getParameter("productoID"));
            String nombre = request.getParameter("nombre");
            double precioCompra = Double.parseDouble(request.getParameter("precioCompra"));
            double precioVenta = Double.parseDouble(request.getParameter("precioVenta"));
            int stockInicial = Integer.parseInt(request.getParameter("stockInicial"));
            int stockActual = Integer.parseInt(request.getParameter("stockActual"));

            Productos productoActualizado = new Productos();
            productoActualizado.setProductoID(productoID);
            productoActualizado.setNombre(nombre);
            productoActualizado.setPrecioCompra(precioCompra);
            productoActualizado.setPrecioVenta(precioVenta);
            productoActualizado.setStockInicial(stockInicial);
            productoActualizado.setStockActual(stockActual);

            productoController.editarProducto(productoActualizado);
            response.sendRedirect("ProductoList.jsp");
        } else if ("delete".equals(action)) {
            int productoID = Integer.parseInt(request.getParameter("productoID"));
            productoController.eliminarLogico(productoID);
            response.sendRedirect("ProductoList.jsp?estado=A&success=delete"); // Incluye el parámetro success
        } else if ("reactivate".equals(action)) {
            int productoID = Integer.parseInt(request.getParameter("productoID"));
            productoController.reactivarProducto(productoID);
            response.sendRedirect("ProductoList.jsp?estado=I&success=reactivate"); // Incluye el parámetro success
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        String estado = request.getParameter("estado"); // Obtener el estado (A o I)

        if ("export".equals(action)) {
            String tipoExportacion = request.getParameter("type");

            // Listar los productos según el estado
            List<Productos> productos = productoController.listarPorEstado(estado);

            // Exportar según el tipo seleccionado
            if ("pdf".equals(tipoExportacion)) {
                exportarPDF(productos, response);
            } else if ("csv".equals(tipoExportacion)) {
                exportarCSV(productos, response);
            } else if ("xls".equals(tipoExportacion)) {
                exportarXLS(productos, response);
            }
        }
    }

    // Métodos para exportar a diferentes formatos

    private void exportarPDF(List<Productos> productos, HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"productos.pdf\"");

        Document documento = new Document(PageSize.A3.rotate());
        try (OutputStream out = response.getOutputStream()) {
            PdfWriter.getInstance(documento, out);
            documento.open();

            Font tituloFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph titulo = new Paragraph("Listado de Productos", tituloFont);
            titulo.setAlignment(Element.ALIGN_CENTER);
            documento.add(titulo);
            documento.add(new Paragraph(" "));

            PdfPTable tabla = new PdfPTable(6);
            tabla.setWidthPercentage(100);

            String[] encabezados = {"Código", "Nombre", "Precio Compra", "Precio Venta", "Stock Inicial", "Stock Actual", "Fecha Registro"};
            for (String encabezado : encabezados) {
                PdfPCell celda = new PdfPCell(new Phrase(encabezado));
                celda.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabla.addCell(celda);
            }

            for (Productos producto : productos) {
                tabla.addCell(producto.getCodigoProducto());
                tabla.addCell(producto.getNombre());
                tabla.addCell(String.valueOf(producto.getPrecioCompra()));
                tabla.addCell(String.valueOf(producto.getPrecioVenta()));
                tabla.addCell(String.valueOf(producto.getStockInicial()));
                tabla.addCell(String.valueOf(producto.getStockActual()));
                tabla.addCell(producto.getFechaRegistro().toString());
            }

            documento.add(tabla);
            documento.close();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    private void exportarCSV(List<Productos> productos, HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"productos.csv\"");

        try (PrintWriter writer = response.getWriter()) {
            writer.println("Código,Nombre,Precio Compra,Precio Venta, Stock Inicial,Stock Actual,Fecha Registro");

            for (Productos producto : productos) {
                writer.println(String.join(",",
                        producto.getCodigoProducto(),
                        producto.getNombre(),
                        String.valueOf(producto.getPrecioCompra()),
                        String.valueOf(producto.getPrecioVenta()),
                        String.valueOf(producto.getStockInicial()),
                        String.valueOf(producto.getStockActual()),
                        producto.getFechaRegistro().toString()
                ));
            }
        }
    }

    private void exportarXLS(List<Productos> productos, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"productos.xlsx\"");

        try (Workbook workbook = new XSSFWorkbook(); OutputStream out = response.getOutputStream()) {
            Sheet sheet = workbook.createSheet("Productos");

            String[] headers = {"Código", "Nombre", "Precio Compra", "Precio Venta", "Stock Inicial", "Stock Actual", "Fecha Registro"};
            Row headerRow = sheet.createRow(0);

            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            int rowIndex = 1;
            for (Productos producto : productos) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(producto.getCodigoProducto());
                row.createCell(1).setCellValue(producto.getNombre());
                row.createCell(2).setCellValue(producto.getPrecioCompra());
                row.createCell(3).setCellValue(producto.getPrecioVenta());
                row.createCell(4).setCellValue(producto.getStockInicial());
                row.createCell(5).setCellValue(producto.getStockActual());
                row.createCell(6).setCellValue(producto.getFechaRegistro().toString());
            }

            workbook.write(out);
        }
    }
}
