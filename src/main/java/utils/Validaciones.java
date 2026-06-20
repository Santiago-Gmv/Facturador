package utils;

import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class Validaciones {

    public static void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public static void mostrarInfo(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public static boolean mostrarConfirmacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        return alert.showAndWait().isPresent() &&
               alert.getResult().getText().equals("OK");
    }

    public static boolean validarSoloLetras(String texto) {
        return texto.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*");
    }

    public static boolean validarSoloNumeros(String texto) {
        return texto.matches("[0-9]*");
    }

    public static boolean validarEmail(String email) {
        return email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }

    public static boolean validarTelefono(String telefono) {
        return telefono.matches("[0-9]{6,20}");
    }

    public static boolean validarDNI(String dni) {
        return dni.matches("[0-9]{7,15}");
    }

    public static boolean validarContrasena(String contrasena) {
        return contrasena.length() >= 8;
    }

    public static boolean validarPrecio(String precio) {
        return precio.matches("^[0-9]+(\\.[0-9]{1,2})?$");
    }

    public static boolean validarCantidad(String cantidad) {
        return cantidad.matches("^[1-9][0-9]*$");
    }

    public static boolean validarNoVacio(String texto) {
        return texto != null && !texto.trim().isEmpty();
    }

    public static void configurarSoloLetras(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!validarSoloLetras(newValue)) {
                textField.setText(oldValue);
            }
        });
    }

    public static void configurarSoloNumeros(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!validarSoloNumeros(newValue)) {
                textField.setText(oldValue);
            }
        });
    }

    public static void configurarSoloDecimales(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) return;
            if (!newValue.matches("^[0-9]*(\\.[0-9]{0,2})?$")) {
                textField.setText(oldValue);
            }
        });
    }

    public static void configurarSoloEnteros(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) return;
            if (!newValue.matches("^[1-9][0-9]*$") && !newValue.equals("0")) {
                textField.setText(oldValue);
            }
        });
    }

    public static void limitarCaracteres(TextField textField, int max) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > max) {
                textField.setText(oldValue);
            }
        });
    }
}