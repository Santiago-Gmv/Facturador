package controllers;

import database.ConexionDB;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import utils.Validaciones;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtContrasena;

    @FXML
    private void handleIniciarSesion() {
        String usuario = txtUsuario.getText().trim();
        String contrasena = txtContrasena.getText();

        if (!Validaciones.validarNoVacio(usuario) || !Validaciones.validarNoVacio(contrasena)) {
            Validaciones.mostrarError("Campos vacios", "Por favor ingrese usuario y contrasena");
            return;
        }

        try {
            Connection conn = ConexionDB.getConexion();
            String sql = "SELECT id, nombre, apellido, usuario, rol FROM usuarios " +
                         "WHERE usuario = ? AND contrasena = ? AND activo = 1";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, usuario);
            stmt.setString(2, contrasena);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("id");
                String nombre = rs.getString("nombre");
                String apellido = rs.getString("apellido");
                String usuarioNombre = rs.getString("usuario");
                String rol = rs.getString("rol");

                switch (rol) {
                    case "ADMIN" -> abrirDashboard("/fxml/admin.fxml", nombre + " " + apellido, userId);
                    case "VENDEDOR" -> abrirDashboard("/fxml/vendedor.fxml", nombre + " " + apellido, userId);
                    case "DEPOSITARIO" -> abrirDashboard("/fxml/deposito.fxml", nombre + " " + apellido, userId);
                    default -> Validaciones.mostrarError("Error", "Rol de usuario no valido");
                }

                Stage stage = (Stage) txtUsuario.getScene().getWindow();
                stage.close();

            } else {
                Validaciones.mostrarError("Credenciales incorrectas",
                        "Usuario o contrasena invalidos");
            }

            rs.close();
            stmt.close();

        } catch (Exception e) {
            Validaciones.mostrarError("Error de conexion",
                    "No se pudo conectar con la base de datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void abrirDashboard(String fxmlPath, String nombreUsuario, int userId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof AdminController) {
                ((AdminController) controller).inicializar(nombreUsuario, userId);
            } else if (controller instanceof VendedorController) {
                ((VendedorController) controller).inicializar(nombreUsuario, userId);
            } else if (controller instanceof DepositoController) {
                ((DepositoController) controller).inicializar(nombreUsuario, userId);
            }

            Stage stage = new Stage();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                getClass().getResource("/css/styles.css").toExternalForm()
            );
            stage.setTitle("Sistema de Facturacion - " + nombreUsuario);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();

        } catch (Exception e) {
            Validaciones.mostrarError("Error", "No se pudo abrir el dashboard");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRegistrarse() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/registro.fxml"));
            Stage stage = (Stage) txtUsuario.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                getClass().getResource("/css/styles.css").toExternalForm()
            );
            stage.setScene(scene);
            stage.setTitle("Registro - Sistema de Facturacion");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}