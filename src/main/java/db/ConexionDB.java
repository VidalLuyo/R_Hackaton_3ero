package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {
    // Cambia la URL a tu servidor local de SQL Server
    private static final String URL = "jdbc:sqlserver://localhost:14033;database=T08_SistemaVentas;encrypt=false;sendStringParametersAsUnicode=false;characterEncoding=UTF-8;";

    // Usuario y contraseña para autenticarse
    private static final String USER = "sa";
    private static final String PASSWORD = "Vidalis_01";

    public static Connection getConnection() {
        Connection connection = null;
        try {
            // Cargar el driver manualmente
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            // Establecer la conexión pasando URL, usuario y contraseña
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Conexión exitosa a SQL Server");
        } catch (ClassNotFoundException e) {
            System.out.println("Error al cargar el driver de SQL Server");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Error al conectar a la base de datos: " + e.getMessage());
            e.printStackTrace();
        }
        return connection;
    }

    public static void main(String[] args) {
        // Prueba la conexión
        Connection connection = getConnection();
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}

