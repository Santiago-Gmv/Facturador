package controllers;

import database.ConexionDB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import utils.Validaciones;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;

public class DepositoController {

    private String nombreUsuario;
    private int usuarioId;

    @FXML private Label lblUsuario;

    @FXML private VBox panelBienvenida, panelIngreso, panelEgreso, panelHistorial, panelStock;

    @FXML private ComboBox<String> cmbProductoIngreso;
    @FXML private TextField txtCantidadIngreso, txtObservacionIngreso;

    @FXML private ComboBox<String> cmbProductoEgreso;
    @FXML private TextField txtCantidadEgreso, txtObservacionEgreso;

    @FXML private TextField txtBuscarMovimiento;
    @FXML private TableView<ObservableList<String>> tablaMovimientos;
    @FXML private TableColumn<ObservableList<String>, String> colMovFecha, colMovHora;
    @FXML private TableColumn<ObservableList<String>, String> colMovTipo, colMovProducto;
    @FXML private TableColumn<ObservableList<String>, String> colMovCantidad, colMovObservacion;

    @FXML private TextField txtBuscarStock;
    @FXML private TableView<ObservableList<String>> tablaStock;
    @FXML private TableColumn<ObservableList<String>, String> colStockCodigo, colStockNombre;
    @FXML private TableColumn<ObservableList<String>, String> colStockCategoria;
    @FXML private TableColumn<ObservableList<String>, String> colStockActual, colStockMinimo;

    public void inicializar(String nombreUsuario, int usuarioId) {
        this.nombreUsuario = nombreUsuario;
        this.usuarioId = usuarioId;
        lblUsuario.setText("Usuario: " + nombreUsuario);

        mostrarPanel(panelBienvenida);

        Validaciones.configurarSoloEnteros(txtCantidadIngreso);
        Validaciones.configurarSoloEnteros(txtCantidadEgreso);

        cargarProductos();
        cargarMovimientos();
        cargarStock();
    }

    private void mostrarPanel(VBox panel) {
        VBox[] paneles = {panelBienvenida, panelIngreso, panelEgreso, panelHistorial, panelStock};
        for (VBox p : paneles) {
            p.setVisible(p == panel);
            p.setManaged(p == panel);
        }
    }

    private void configurarColumna(TableColumn<ObservableList<String>, String> col, int index) {
        col.setCellValueFactory(data -> {
            ObservableList<String> row = data.getValue();
            if (index < row.size() && row.get(index) != null)
                return new javafx.beans.property.SimpleStringProperty(row.get(index));
            return new javafx.beans.property.SimpleStringProperty("");
        });
    }

    @FXML private void mostrarIngreso() { mostrarPanel(panelIngreso); cargarProductos(); }
    @FXML private void mostrarEgreso() { mostrarPanel(panelEgreso); cargarProductos(); }
    @FXML private void mostrarHistorial() { mostrarPanel(panelHistorial); cargarMovimientos(); }
    @FXML private void mostrarStock() { mostrarPanel(panelStock); cargarStock(); }

    @FXML private void handleCerrarSesion() {
        if (Validaciones.mostrarConfirmacion("Cerrar Sesion", "Desea cerrar la sesion?")) {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
                Stage stage = (Stage) lblUsuario.getScene().getWindow();
                Scene scene = new Scene(root);
                scene.getStylesheets().add(
                    getClass().getResource("/css/styles.css").toExternalForm());
                stage.setScene(scene);
                stage.setTitle("Sistema de Facturacion - Login");
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void cargarProductos() {
        cmbProductoIngreso.getItems().clear();
        cmbProductoEgreso.getItems().clear();

        try {
            Connection conn = ConexionDB.getConexion();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT id, codigo, nombre, stock_actual FROM productos WHERE activo=1");

            while (rs.next()) {
                String info = rs.getInt("id") + " - " + rs.getString("codigo") + " - " +
                    rs.getString("nombre") + " [Stock: " + rs.getInt("stock_actual") + "]";
                cmbProductoIngreso.getItems().add(info);
                cmbProductoEgreso.getItems().add(info);
            }
            rs.close(); stmt.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private int getProductoIdSeleccionado(ComboBox<String> cmb) {
        String seleccion = cmb.getValue();
        if (seleccion == null) return -1;
        try {
            return Integer.parseInt(seleccion.split(" - ")[0]);
        } catch (Exception e) { return -1; }
    }

    @FXML
    private void handleRegistrarIngreso() {
        int productoId = getProductoIdSeleccionado(cmbProductoIngreso);
        if (productoId < 0) {
            Validaciones.mostrarError("Seleccionar", "Seleccione un producto");
            return;
        }

        String cantText = txtCantidadIngreso.getText().trim();
        if (!Validaciones.validarCantidad(cantText)) {
            Validaciones.mostrarError("Cantidad invalida", "Ingrese una cantidad valida (numero entero positivo)");
            return;
        }

        int cantidad = Integer.parseInt(cantText);
        String observacion = txtObservacionIngreso.getText().trim();

        try {
            Connection conn = ConexionDB.getConexion();
            conn.setAutoCommit(false);

            try {

                PreparedStatement stmtMov = conn.prepareStatement(
                    "INSERT INTO movimientos_deposito (fecha, hora, usuario_id, tipo_movimiento, producto_id, cantidad, observacion) VALUES (?,?,?,?,?,?,?)");
                stmtMov.setDate(1, Date.valueOf(LocalDate.now()));
                stmtMov.setTime(2, Time.valueOf(LocalTime.now()));
                stmtMov.setInt(3, usuarioId);
                stmtMov.setString(4, "INGRESO");
                stmtMov.setInt(5, productoId);
                stmtMov.setInt(6, cantidad);
                stmtMov.setString(7, observacion.isEmpty() ? null : observacion);
                stmtMov.executeUpdate();
                stmtMov.close();

                PreparedStatement stmtStock = conn.prepareStatement(
                    "UPDATE productos SET stock_actual = stock_actual + ? WHERE id = ?");
                stmtStock.setInt(1, cantidad);
                stmtStock.setInt(2, productoId);
                stmtStock.executeUpdate();
                stmtStock.close();

                PreparedStatement stmtAudit = conn.prepareStatement(
                    "INSERT INTO auditoria (usuario_id, accion, tabla_afectada, detalle) VALUES (?,?,?,?)");
                stmtAudit.setInt(1, usuarioId);
                stmtAudit.setString(2, "INGRESO STOCK");
                stmtAudit.setString(3, "movimientos_deposito");
                stmtAudit.setString(4, "Ingreso de " + cantidad + " unidades (prod ID: " + productoId + ")");
                stmtAudit.executeUpdate();
                stmtAudit.close();

                conn.commit();

                Validaciones.mostrarInfo("Ingreso registrado",
                    "Se ingresaron " + cantidad + " unidades correctamente");

                txtCantidadIngreso.clear();
                txtObservacionIngreso.clear();
                cargarProductos();

            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (Exception e) {
            Validaciones.mostrarError("Error", "No se pudo registrar el ingreso: " + e.getMessage());
        }
    }

    @FXML
    private void handleNuevoProducto() {
        Dialog<ObservableList<String>> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Producto");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10);
        TextField txtCodigo = new TextField();
        TextField txtNombre = new TextField();
        TextField txtCantidad = new TextField("1");

        Validaciones.configurarSoloEnteros(txtCantidad);
        Validaciones.limitarCaracteres(txtCodigo, 50);
        Validaciones.limitarCaracteres(txtNombre, 150);

        grid.addRow(0, new Label("Codigo:"), txtCodigo);
        grid.addRow(1, new Label("Nombre:"), txtNombre);
        grid.addRow(2, new Label("Cantidad Ingreso:"), txtCantidad);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                return FXCollections.observableArrayList(
                    txtCodigo.getText(), txtNombre.getText(), txtCantidad.getText());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(d -> {
            if (!Validaciones.validarNoVacio(d.get(0)) || !Validaciones.validarNoVacio(d.get(1))) {
                Validaciones.mostrarError("Requerido", "Codigo y nombre son obligatorios"); return;
            }
            int cantidad;
            try { cantidad = Integer.parseInt(d.get(2)); if (cantidad < 1) throw new Exception(); }
            catch (Exception e) { Validaciones.mostrarError("Cantidad", "Ingrese una cantidad valida"); return; }

            try {
                Connection conn = ConexionDB.getConexion();
                conn.setAutoCommit(false);
                try {
                    PreparedStatement stmtProd = conn.prepareStatement(
                        "INSERT INTO productos (codigo, nombre, precio_compra, precio_venta, descuento, stock_actual, stock_minimo, activo) VALUES (?,?,0,0,0,?,0,1)",
                        Statement.RETURN_GENERATED_KEYS);
                    stmtProd.setString(1, d.get(0));
                    stmtProd.setString(2, d.get(1));
                    stmtProd.setInt(3, cantidad);
                    stmtProd.executeUpdate();

                    ResultSet rs = stmtProd.getGeneratedKeys();
                    int prodId = 0;
                    if (rs.next()) prodId = rs.getInt(1);
                    rs.close(); stmtProd.close();

                    PreparedStatement stmtMov = conn.prepareStatement(
                        "INSERT INTO movimientos_deposito (fecha, hora, usuario_id, tipo_movimiento, producto_id, cantidad, observacion) VALUES (?,?,?,?,?,?,?)");
                    stmtMov.setDate(1, Date.valueOf(LocalDate.now()));
                    stmtMov.setTime(2, Time.valueOf(LocalTime.now()));
                    stmtMov.setInt(3, usuarioId);
                    stmtMov.setString(4, "INGRESO");
                    stmtMov.setInt(5, prodId);
                    stmtMov.setInt(6, cantidad);
                    stmtMov.setString(7, "Producto nuevo");
                    stmtMov.executeUpdate(); stmtMov.close();

                    conn.commit();
                    Validaciones.mostrarInfo("Exito", "Producto creado con " + cantidad + " unidades");
                    cargarProductos();
                } catch (Exception e) {
                    conn.rollback(); throw e;
                } finally {
                    conn.setAutoCommit(true);
                }
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains("Duplicate")) {
                    Validaciones.mostrarError("Duplicado", "Ya existe un producto con ese codigo");
                } else {
                    Validaciones.mostrarError("Error", e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleRegistrarEgreso() {
        int productoId = getProductoIdSeleccionado(cmbProductoEgreso);
        if (productoId < 0) {
            Validaciones.mostrarError("Seleccionar", "Seleccione un producto");
            return;
        }

        String cantText = txtCantidadEgreso.getText().trim();
        if (!Validaciones.validarCantidad(cantText)) {
            Validaciones.mostrarError("Cantidad invalida", "Ingrese una cantidad valida (numero entero positivo)");
            return;
        }

        int cantidad = Integer.parseInt(cantText);
        String observacion = txtObservacionEgreso.getText().trim();

        try {
            Connection conn = ConexionDB.getConexion();

            PreparedStatement stmtVerif = conn.prepareStatement(
                "SELECT stock_actual FROM productos WHERE id = ?");
            stmtVerif.setInt(1, productoId);
            ResultSet rs = stmtVerif.executeQuery();
            if (!rs.next()) {
                Validaciones.mostrarError("Error", "Producto no encontrado");
                return;
            }
            int stockActual = rs.getInt("stock_actual");
            rs.close();
            stmtVerif.close();

            if (cantidad > stockActual) {
                Validaciones.mostrarError("Stock insuficiente",
                    "Stock actual: " + stockActual + ". No se pueden retirar " + cantidad + " unidades.");
                return;
            }

            conn.setAutoCommit(false);

            try {

                PreparedStatement stmtMov = conn.prepareStatement(
                    "INSERT INTO movimientos_deposito (fecha, hora, usuario_id, tipo_movimiento, producto_id, cantidad, observacion) VALUES (?,?,?,?,?,?,?)");
                stmtMov.setDate(1, Date.valueOf(LocalDate.now()));
                stmtMov.setTime(2, Time.valueOf(LocalTime.now()));
                stmtMov.setInt(3, usuarioId);
                stmtMov.setString(4, "EGRESO");
                stmtMov.setInt(5, productoId);
                stmtMov.setInt(6, cantidad);
                stmtMov.setString(7, observacion.isEmpty() ? null : observacion);
                stmtMov.executeUpdate();
                stmtMov.close();

                PreparedStatement stmtStock = conn.prepareStatement(
                    "UPDATE productos SET stock_actual = stock_actual - ? WHERE id = ?");
                stmtStock.setInt(1, cantidad);
                stmtStock.setInt(2, productoId);
                stmtStock.executeUpdate();
                stmtStock.close();

                PreparedStatement stmtAudit = conn.prepareStatement(
                    "INSERT INTO auditoria (usuario_id, accion, tabla_afectada, detalle) VALUES (?,?,?,?)");
                stmtAudit.setInt(1, usuarioId);
                stmtAudit.setString(2, "EGRESO STOCK");
                stmtAudit.setString(3, "movimientos_deposito");
                stmtAudit.setString(4, "Egreso de " + cantidad + " unidades (prod ID: " + productoId + ")");
                stmtAudit.executeUpdate();
                stmtAudit.close();

                conn.commit();

                Validaciones.mostrarInfo("Egreso registrado",
                    "Se retiraron " + cantidad + " unidades correctamente");

                txtCantidadEgreso.clear();
                txtObservacionEgreso.clear();
                cargarProductos();

            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (Exception e) {
            Validaciones.mostrarError("Error", "No se pudo registrar el egreso: " + e.getMessage());
        }
    }

    private void cargarMovimientos() {
        try {
            Connection conn = ConexionDB.getConexion();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT m.fecha, m.hora, m.tipo_movimiento, p.nombre as producto, " +
                "m.cantidad, m.observacion " +
                "FROM movimientos_deposito m " +
                "JOIN productos p ON m.producto_id = p.id " +
                "ORDER BY m.id DESC");

            ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
            for (int i = 0; i < 6; i++) configurarColumna(getColMov(i), i);

            while (rs.next()) {
                data.add(FXCollections.observableArrayList(
                    rs.getString("fecha"),
                    rs.getString("hora") != null ? rs.getString("hora").substring(0, 5) : "",
                    rs.getString("tipo_movimiento"),
                    rs.getString("producto"),
                    String.valueOf(rs.getInt("cantidad")),
                    rs.getString("observacion") != null ? rs.getString("observacion") : ""));
            }
            tablaMovimientos.setItems(data);
            rs.close(); stmt.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private TableColumn<ObservableList<String>, String> getColMov(int i) {
        return switch (i) {
            case 0 -> colMovFecha; case 1 -> colMovHora; case 2 -> colMovTipo;
            case 3 -> colMovProducto; case 4 -> colMovCantidad; case 5 -> colMovObservacion;
            default -> null;
        };
    }

    @FXML private void filtrarMovimientos() { cargarMovimientos(); }

    private void cargarStock() {
        try {
            Connection conn = ConexionDB.getConexion();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT codigo, nombre, categoria, stock_actual, stock_minimo " +
                "FROM productos WHERE activo=1");

            ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
            for (int i = 0; i < 5; i++) configurarColumna(getColStock(i), i);

            while (rs.next()) {
                data.add(FXCollections.observableArrayList(
                    rs.getString("codigo"), rs.getString("nombre"),
                    rs.getString("categoria") != null ? rs.getString("categoria") : "",
                    String.valueOf(rs.getInt("stock_actual")),
                    String.valueOf(rs.getInt("stock_minimo"))));
            }
            tablaStock.setItems(data);
            rs.close(); stmt.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private TableColumn<ObservableList<String>, String> getColStock(int i) {
        return switch (i) {
            case 0 -> colStockCodigo; case 1 -> colStockNombre; case 2 -> colStockCategoria;
            case 3 -> colStockActual; case 4 -> colStockMinimo;
            default -> null;
        };
    }

    @FXML private void filtrarStock() { cargarStock(); }
}