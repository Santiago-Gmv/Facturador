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

public class RegistroController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtContrasena;
    @FXML private PasswordField txtConfirmarContrasena;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtEmail;

    @FXML
    private void initialize() {

        Validaciones.configurarSoloLetras(txtNombre);
        Validaciones.configurarSoloLetras(txtApellido);
        Validaciones.configurarSoloNumeros(txtTelefono);
        Validaciones.limitarCaracteres(txtTelefono, 20);
        Validaciones.limitarCaracteres(txtUsuario, 50);
    }

    @FXML
    private void handleRegistrarse() {
        String nombre = txtNombre.getText().trim();
        String apellido = txtApellido.getText().trim();
        String usuario = txtUsuario.getText().trim();
        String contrasena = txtContrasena.getText();
        String confirmar = txtConfirmarContrasena.getText();
        String telefono = txtTelefono.getText().trim();
        String email = txtEmail.getText().trim();

        if (!Validaciones.validarNoVacio(nombre)) {
            Validaciones.mostrarError("Campo requerido", "El nombre es obligatorio");
            return;
        }
        if (!Validaciones.validarNoVacio(apellido)) {
            Validaciones.mostrarError("Campo requerido", "El apellido es obligatorio");
            return;
        }
        if (!Validaciones.validarNoVacio(usuario)) {
            Validaciones.mostrarError("Campo requerido", "El usuario es obligatorio");
            return;
        }

        if (!Validaciones.validarContrasena(contrasena)) {
            Validaciones.mostrarError("Contrasena debil",
                    "La contrasena debe tener al menos 8 caracteres");
            return;
        }

        if (!contrasena.equals(confirmar)) {
            Validaciones.mostrarError("Contrasenas no coinciden",
                    "Las contrasenas ingresadas no son iguales");
            return;
        }

        if (Validaciones.validarNoVacio(email) && !Validaciones.validarEmail(email)) {
            Validaciones.mostrarError("Email invalido",
                    "Por favor ingrese un email valido");
            return;
        }

        if (Validaciones.validarNoVacio(telefono) && !Validaciones.validarTelefono(telefono)) {
            Validaciones.mostrarError("Telefono invalido",
                    "El telefono debe contener solo numeros (6-20 digitos)");
            return;
        }

        try {
            Connection conn = ConexionDB.getConexion();
            String sql = "INSERT INTO usuarios (nombre, apellido, usuario, contrasena, telefono, email, rol) " +
                         "VALUES (?, ?, ?, ?, ?, ?, 'VENDEDOR')";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, nombre);
            stmt.setString(2, apellido);
            stmt.setString(3, usuario);
            stmt.setString(4, contrasena);
            stmt.setString(5, telefono.isEmpty() ? null : telefono);
            stmt.setString(6, email.isEmpty() ? null : email);

            int resultado = stmt.executeUpdate();

            if (resultado > 0) {
                Validaciones.mostrarInfo("Registro exitoso",
                        "Usuario creado correctamente. Ahora puede iniciar sesion.");

                handleCancelar();
            }

            stmt.close();

        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("Duplicate")) {
                Validaciones.mostrarError("Usuario existente",
                        "El nombre de usuario ya esta registrado");
            } else {
                Validaciones.mostrarError("Error de base de datos",
                        "No se pudo registrar el usuario: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleCancelar() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) txtNombre.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                getClass().getResource("/css/styles.css").toExternalForm()
            );
            stage.setScene(scene);
            stage.setTitle("Sistema de Facturacion - Login");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}