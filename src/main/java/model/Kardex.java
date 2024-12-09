package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Kardex {
    private int kardexID;
    private int productoID;
    private Date fecha;
    private String tipoMovimiento;
    private int cantidad;
    private double precioUnitario;
    private double montoTotal;
    private int stockInicial;
    private int stockFinal;
    private char estado; // Nuevo campo para el estado ('A' o 'I')

    // Constructor específico que coincide con el llamado en el controlador
    public Kardex(int kardexID, int productoID, int cantidad, double precioUnitario, int stockInicial, int stockFinal) {
        this.kardexID = kardexID;
        this.productoID = productoID;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.stockInicial = stockInicial;
        this.stockFinal = stockFinal;
        this.estado = 'A'; // Por defecto, se establece como activo
    }
}
