package database;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {

    private static Connection conexion = null;

    public static Connection getConexion() {
        if (conexion == null) {
            try {

                Dotenv dotenv = Dotenv.configure()
                        .directory(".")
                        .ignoreIfMissing()
                        .load();

                String host = dotenv.get("DB_HOST", "localhost");
                String port = dotenv.get("DB_PORT", "3306");
                String dbName = dotenv.get("DB_NAME", "facturacion_db");
                String user = dotenv.get("DB_USER", "root");
                String password = dotenv.get("DB_PASSWORD", "");

                String url = "jdbc:mariadb://" + host + ":" + port + "/" + dbName
                        + "?useSSL=false&serverTimezone=UTC";

                conexion = DriverManager.getConnection(url, user, password);
                System.out.println("Conexion exitosa a la base de datos");

            } catch (SQLException e) {
                System.err.println("Error al conectar a la base de datos: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return conexion;
    }

    public static void cerrarConexion() {
        if (conexion != null) {
            try {
                conexion.close();
                conexion = null;
                System.out.println("Conexion cerrada correctamente");
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexion: " + e.getMessage());
            }
        }
    }
}