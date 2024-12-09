package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Productos {
    private int productoID;
    private String codigoProducto; // Código único para identificar el producto
    private String nombre;
    private double precioCompra;
    private double precioVenta;
    private int stockInicial;
    private int stockActual;
    private Date fechaRegistro; // Fecha por defecto: GETDATE() en SQL Server
}
