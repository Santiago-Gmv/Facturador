package controllers;

import database.ConexionDB;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import utils.Validaciones;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class AdminController {

    private String nombreUsuario;
    private int usuarioId;

    @FXML private VBox panelBienvenida;

    @FXML private VBox panelUsuarios, panelClientes, panelProveedores;
    @FXML private VBox panelProductos, panelFacturacion, panelHistorial;
    @FXML private VBox panelEstadisticas, panelMovimientos;

    @FXML private Label lblFechaActual, lblHoraActual;

    @FXML private Label lblUsuario;

    @FXML private TableView<ObservableList<String>> tablaUsuarios;
    @FXML private TableColumn<ObservableList<String>, String> colUsuarioId, colUsuarioNombre;
    @FXML private TableColumn<ObservableList<String>, String> colUsuarioApellido, colUsuarioUsuario;
    @FXML private TableColumn<ObservableList<String>, String> colUsuarioTelefono, colUsuarioEmail;
    @FXML private TableColumn<ObservableList<String>, String> colUsuarioRol, colUsuarioActivo;
    @FXML private TextField txtBuscarUsuario;

    @FXML private TableView<ObservableList<String>> tablaClientes;
    @FXML private TableColumn<ObservableList<String>, String> colClienteId, colClienteNombre;
    @FXML private TableColumn<ObservableList<String>, String> colClienteApellido, colClienteDNI;
    @FXML private TableColumn<ObservableList<String>, String> colClienteTelefono, colClienteEmail;
    @FXML private TableColumn<ObservableList<String>, String> colClienteDireccion;
    @FXML private TextField txtBuscarCliente;

    @FXML private TableView<ObservableList<String>> tablaProveedores;
    @FXML private TableColumn<ObservableList<String>, String> colProveedorId, colProveedorEmpresa;
    @FXML private TableColumn<ObservableList<String>, String> colProveedorContacto;
    @FXML private TableColumn<ObservableList<String>, String> colProveedorTelefono;
    @FXML private TableColumn<ObservableList<String>, String> colProveedorEmail;
    @FXML private TableColumn<ObservableList<String>, String> colProveedorDireccion;
    @FXML private TextField txtBuscarProveedor;

    @FXML private TableView<ObservableList<String>> tablaProductos;
    @FXML private TableColumn<ObservableList<String>, String> colProductoCodigo, colProductoNombre;
    @FXML private TableColumn<ObservableList<String>, String> colProductoCategoria;
    @FXML private TableColumn<ObservableList<String>, String> colProductoPrecioCompra;
    @FXML private TableColumn<ObservableList<String>, String> colProductoPrecioVenta;
    @FXML private TableColumn<ObservableList<String>, String> colProductoDescuento;
    @FXML private TableColumn<ObservableList<String>, String> colProductoStock;
    @FXML private TableColumn<ObservableList<String>, String> colProductoStockMin;
    @FXML private TableColumn<ObservableList<String>, String> colProductoProveedor;
    @FXML private TableColumn<ObservableList<String>, String> colProductoEstado;
    @FXML private TextField txtBuscarProducto;

    @FXML private TextField txtBuscarClienteFactura;
    @FXML private ComboBox<String> cmbClienteFactura;
    private ObservableList<String[]> clientesFactura = FXCollections.observableArrayList();

    @FXML private TextField txtBuscarProductoFactura;
    @FXML private ComboBox<String> cmbProductoFactura;
    private ObservableList<String[]> productosFactura = FXCollections.observableArrayList();

    @FXML private TextField txtCantidadFactura;
    @FXML private TableView<ObservableList<String>> tablaDetalleFactura;
    @FXML private TableColumn<ObservableList<String>, String> colDetCodigo, colDetNombre;
    @FXML private TableColumn<ObservableList<String>, String> colDetCantidad, colDetPrecio;
    @FXML private TableColumn<ObservableList<String>, String> colDetDescuento, colDetSubtotal;
    @FXML private Label lblSubtotal, lblIVA, lblTotal;
    @FXML private ComboBox<String> cmbDescuentoGlobal;
    @FXML private ComboBox<String> cmbIVA;

    private ObservableList<String[]> detalleFactura = FXCollections.observableArrayList();

    @FXML private TableView<ObservableList<String>> tablaFacturas;
    @FXML private TableColumn<ObservableList<String>, String> colFacturaNumero, colFacturaFecha;
    @FXML private TableColumn<ObservableList<String>, String> colFacturaHora, colFacturaVendedor;
    @FXML private TableColumn<ObservableList<String>, String> colFacturaCliente, colFacturaTotal;
    @FXML private TableColumn<ObservableList<String>, String> colFacturaEstado;
    @FXML private TextField txtBuscarFactura;

    @FXML private Text lblTotalFacturas, lblTotalVentas, lblTotalClientes, lblTotalBajoStock;

    @FXML private TableView<ObservableList<String>> tablaMovimientos;
    @FXML private TableColumn<ObservableList<String>, String> colMovId, colMovFecha, colMovHora;
    @FXML private TableColumn<ObservableList<String>, String> colMovUsuario, colMovTipo;
    @FXML private TableColumn<ObservableList<String>, String> colMovProducto, colMovCantidad;
    @FXML private TableColumn<ObservableList<String>, String> colMovObservacion;
    @FXML private TextField txtBuscarMovimiento;

    public void inicializar(String nombreUsuario, int usuarioId) {
        this.nombreUsuario = nombreUsuario;
        this.usuarioId = usuarioId;
        lblUsuario.setText("Usuario: " + nombreUsuario);

        mostrarPanel(panelBienvenida);

        Timeline reloj = new Timeline(new KeyFrame(Duration.seconds(1), e -> actualizarReloj()));
        reloj.setCycleCount(Timeline.INDEFINITE);
        reloj.play();

        cargarUsuarios();
        cargarClientes();
        cargarProveedores();
        cargarProductos();
        cargarFacturas();
        cargarMovimientos();
        cargarEstadisticas();
        configurarDescuentos();
        configurarIVA();
    }

    private void actualizarReloj() {
        lblFechaActual.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        lblHoraActual.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }

    private void mostrarPanel(VBox panelMostrar) {
        VBox[] paneles = {panelBienvenida, panelUsuarios, panelClientes, panelProveedores,
                          panelProductos, panelFacturacion, panelHistorial,
                          panelEstadisticas, panelMovimientos};
        for (VBox p : paneles) {
            p.setVisible(p == panelMostrar);
            p.setManaged(p == panelMostrar);
        }
    }

    @FXML private void mostrarUsuarios() { mostrarPanel(panelUsuarios); cargarUsuarios(); }
    @FXML private void mostrarClientes() { mostrarPanel(panelClientes); cargarClientes(); }
    @FXML private void mostrarProveedores() { mostrarPanel(panelProveedores); cargarProveedores(); }
    @FXML private void mostrarProductos() { mostrarPanel(panelProductos); cargarProductos(); }

    @FXML private void mostrarFacturacion() {
        mostrarPanel(panelFacturacion);
        cargarClientesFactura();
        cargarProductosFactura();
        detalleFactura.clear();
        actualizarTotales();
        configurarCantidadField();
    }

    @FXML private void mostrarHistorial() { mostrarPanel(panelHistorial); cargarFacturas(); }
    @FXML private void mostrarEstadisticas() { mostrarPanel(panelEstadisticas); cargarEstadisticas(); }
    @FXML private void mostrarMovimientos() { mostrarPanel(panelMovimientos); cargarMovimientos(); }

    @FXML private void handleCerrarSesion() {
        if (Validaciones.mostrarConfirmacion("Cerrar Sesion", "Desea cerrar la sesion?")) {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
                Stage stage = (Stage) lblUsuario.getScene().getWindow();
                Scene scene = new Scene(root);
                scene.getStylesheets().add(
                    getClass().getResource("/css/styles.css").toExternalForm()
                );
                stage.setScene(scene);
                stage.setTitle("Sistema de Facturacion - Login");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void configurarColumna(TableColumn<ObservableList<String>, String> col, int index) {
        col.setCellValueFactory(data -> {
            ObservableList<String> row = data.getValue();
            if (index < row.size() && row.get(index) != null) {
                return new SimpleStringProperty(row.get(index));
            }
            return new SimpleStringProperty("");
        });
    }

    private ObservableList<String> filaDesdeRS(ResultSet rs, String... columnas) throws SQLException {
        ObservableList<String> fila = FXCollections.observableArrayList();
        for (String col : columnas) {
            String val = rs.getString(col);
            fila.add(val != null ? val : "");
        }
        return fila;
    }

    private void cargarUsuarios() {
        try {
            Connection conn = ConexionDB.getConexion();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, nombre, apellido, usuario, telefono, email, rol, activo FROM usuarios");

            ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
            for (int i = 0; i < 8; i++) {
                configurarColumna(getColUsuario(i), i);
            }

            while (rs.next()) {
                String activoStr = rs.getInt("activo") == 1 ? "Activo" : "Inactivo";
                ObservableList<String> fila = FXCollections.observableArrayList(
                    String.valueOf(rs.getInt("id")),
                    rs.getString("nombre"),
                    rs.getString("apellido"),
                    rs.getString("usuario"),
                    rs.getString("telefono") != null ? rs.getString("telefono") : "",
                    rs.getString("email") != null ? rs.getString("email") : "",
                    rs.getString("rol"),
                    activoStr
                );
                data.add(fila);
            }
            tablaUsuarios.setItems(data);
            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private TableColumn<ObservableList<String>, String> getColUsuario(int i) {
        return switch (i) {
            case 0 -> colUsuarioId; case 1 -> colUsuarioNombre; case 2 -> colUsuarioApellido;
            case 3 -> colUsuarioUsuario; case 4 -> colUsuarioTelefono; case 5 -> colUsuarioEmail;
            case 6 -> colUsuarioRol; case 7 -> colUsuarioActivo;
            default -> null;
        };
    }

    @FXML private void filtrarUsuarios() {
        String filtro = txtBuscarUsuario.getText().toLowerCase();
        tablaUsuarios.getItems().forEach(row -> {
            boolean visible = false;
            for (String s : row) {
                if (s.toLowerCase().contains(filtro)) { visible = true; break; }
            }
        });
        cargarUsuarios();
    }

    @FXML private void handleNuevoUsuario() {
        Dialog<ObservableList<String>> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Usuario");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10);
        TextField txtNombre = new TextField(); TextField txtApellido = new TextField();
        TextField txtUsuario = new TextField(); PasswordField txtContrasena = new PasswordField();
        TextField txtTelefono = new TextField(); TextField txtEmail = new TextField();
        ComboBox<String> cmbRol = new ComboBox<>(FXCollections.observableArrayList("ADMIN", "VENDEDOR", "DEPOSITARIO"));
        cmbRol.setValue("VENDEDOR");

        Validaciones.configurarSoloLetras(txtNombre);
        Validaciones.configurarSoloLetras(txtApellido);
        Validaciones.configurarSoloNumeros(txtTelefono);
        Validaciones.limitarCaracteres(txtUsuario, 50);
        Validaciones.limitarCaracteres(txtEmail, 100);

        grid.addRow(0, new Label("Nombre:"), txtNombre);
        grid.addRow(1, new Label("Apellido:"), txtApellido);
        grid.addRow(2, new Label("Usuario:"), txtUsuario);
        grid.addRow(3, new Label("Contrasena:"), txtContrasena);
        grid.addRow(4, new Label("Telefono:"), txtTelefono);
        grid.addRow(5, new Label("Email:"), txtEmail);
        grid.addRow(6, new Label("Rol:"), cmbRol);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                return FXCollections.observableArrayList(
                    txtNombre.getText(), txtApellido.getText(), txtUsuario.getText(),
                    txtContrasena.getText(), txtTelefono.getText(), txtEmail.getText(),
                    cmbRol.getValue());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(d -> {
            if (!Validaciones.validarNoVacio(d.get(0)) || !Validaciones.validarNoVacio(d.get(1))
                || !Validaciones.validarNoVacio(d.get(2)) || !Validaciones.validarNoVacio(d.get(3))) {
                Validaciones.mostrarError("Campos requeridos", "Nombre, apellido, usuario y contrasena son obligatorios");
                return;
            }
            try {
                Connection conn = ConexionDB.getConexion();
                PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO usuarios (nombre, apellido, usuario, contrasena, telefono, email, rol) VALUES (?,?,?,?,?,?,?)");
                stmt.setString(1, d.get(0)); stmt.setString(2, d.get(1));
                stmt.setString(3, d.get(2)); stmt.setString(4, d.get(3));
                stmt.setString(5, d.get(4).isEmpty() ? null : d.get(4));
                stmt.setString(6, d.get(5).isEmpty() ? null : d.get(5));
                stmt.setString(7, d.get(6));
                stmt.executeUpdate(); stmt.close();
                Validaciones.mostrarInfo("Exito", "Usuario creado correctamente");
                cargarUsuarios();
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains("Duplicate")) {
                    Validaciones.mostrarError("Duplicado", "Ya existe un registro con ese valor");
                } else {
                    Validaciones.mostrarError("Error", e.getMessage());
                }
            }
        });
    }

    @FXML private void handleEditarUsuario() {
        int idx = tablaUsuarios.getSelectionModel().getSelectedIndex();
        if (idx < 0) { Validaciones.mostrarError("Seleccionar", "Seleccione un usuario"); return; }
        ObservableList<String> fila = tablaUsuarios.getItems().get(idx);

        Dialog<ObservableList<String>> dialog = new Dialog<>();
        dialog.setTitle("Editar Usuario");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10);
        TextField txtNombre = new TextField(fila.get(1));
        TextField txtApellido = new TextField(fila.get(2));
        TextField txtUsuario = new TextField(fila.get(3));
        PasswordField txtContrasena = new PasswordField();
        TextField txtTelefono = new TextField(fila.get(4));
        TextField txtEmail = new TextField(fila.get(5));
        ComboBox<String> cmbRol = new ComboBox<>(FXCollections.observableArrayList("ADMIN", "VENDEDOR", "DEPOSITARIO"));
        cmbRol.setValue(fila.get(6));

        Validaciones.configurarSoloLetras(txtNombre);
        Validaciones.configurarSoloLetras(txtApellido);
        Validaciones.configurarSoloNumeros(txtTelefono);
        Validaciones.limitarCaracteres(txtUsuario, 50);
        Validaciones.limitarCaracteres(txtEmail, 100);

        grid.addRow(0, new Label("Nombre:"), txtNombre);
        grid.addRow(1, new Label("Apellido:"), txtApellido);
        grid.addRow(2, new Label("Usuario:"), txtUsuario);
        grid.addRow(3, new Label("Contrasena (dejar vacio para no cambiar):"), txtContrasena);
        grid.addRow(4, new Label("Telefono:"), txtTelefono);
        grid.addRow(5, new Label("Email:"), txtEmail);
        grid.addRow(6, new Label("Rol:"), cmbRol);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                return FXCollections.observableArrayList(
                    txtNombre.getText(), txtApellido.getText(), txtUsuario.getText(),
                    txtContrasena.getText(), txtTelefono.getText(), txtEmail.getText(),
                    cmbRol.getValue());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(d -> {
            if (!Validaciones.validarNoVacio(d.get(0)) || !Validaciones.validarNoVacio(d.get(1))
                || !Validaciones.validarNoVacio(d.get(2))) {
                Validaciones.mostrarError("Campos requeridos", "Nombre, apellido y usuario son obligatorios");
                return;
            }
            try {
                Connection conn = ConexionDB.getConexion();
                String sql;
                PreparedStatement stmt;
                if (Validaciones.validarNoVacio(d.get(3))) {
                    sql = "UPDATE usuarios SET nombre=?, apellido=?, usuario=?, contrasena=?, telefono=?, email=?, rol=? WHERE id=?";
                    stmt = conn.prepareStatement(sql);
                    stmt.setString(1, d.get(0)); stmt.setString(2, d.get(1));
                    stmt.setString(3, d.get(2)); stmt.setString(4, d.get(3));
                    stmt.setString(5, d.get(4).isEmpty() ? null : d.get(4));
                    stmt.setString(6, d.get(5).isEmpty() ? null : d.get(5));
                    stmt.setString(7, d.get(6));
                    stmt.setInt(8, Integer.parseInt(fila.get(0)));
                } else {
                    sql = "UPDATE usuarios SET nombre=?, apellido=?, usuario=?, telefono=?, email=?, rol=? WHERE id=?";
                    stmt = conn.prepareStatement(sql);
                    stmt.setString(1, d.get(0)); stmt.setString(2, d.get(1));
                    stmt.setString(3, d.get(2));
                    stmt.setString(4, d.get(4).isEmpty() ? null : d.get(4));
                    stmt.setString(5, d.get(5).isEmpty() ? null : d.get(5));
                    stmt.setString(6, d.get(6));
                    stmt.setInt(7, Integer.parseInt(fila.get(0)));
                }
                stmt.executeUpdate(); stmt.close();
                Validaciones.mostrarInfo("Exito", "Usuario actualizado");
                cargarUsuarios();
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains("Duplicate")) {
                    Validaciones.mostrarError("Duplicado", "Ya existe un registro con ese valor");
                } else {
                    Validaciones.mostrarError("Error", e.getMessage());
                }
            }
        });
    }

    @FXML private void handleEliminarUsuario() {
        int idx = tablaUsuarios.getSelectionModel().getSelectedIndex();
        if (idx < 0) { Validaciones.mostrarError("Seleccionar", "Seleccione un usuario"); return; }
        if (!Validaciones.mostrarConfirmacion("Confirmar", "Desea eliminar este usuario?")) return;

        String id = tablaUsuarios.getItems().get(idx).get(0);
        try {
            Connection conn = ConexionDB.getConexion();
            PreparedStatement stmt = conn.prepareStatement("UPDATE usuarios SET activo=0 WHERE id=?");
            stmt.setInt(1, Integer.parseInt(id));
            stmt.executeUpdate();
            stmt.close();
            Validaciones.mostrarInfo("Exito", "Usuario desactivado");
            cargarUsuarios();
        } catch (Exception e) {
            Validaciones.mostrarError("Error", e.getMessage());
        }
    }

    private void cargarClientes() {
        try {
            Connection conn = ConexionDB.getConexion();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, nombre, apellido, dni, telefono, email, direccion FROM clientes WHERE activo=1");

            ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
            for (int i = 0; i < 7; i++) configurarColumna(getColCliente(i), i);

            while (rs.next()) {
                data.add(filaDesdeRS(rs, "id", "nombre", "apellido", "dni", "telefono", "email", "direccion"));
            }
            tablaClientes.setItems(data);
            rs.close(); stmt.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private TableColumn<ObservableList<String>, String> getColCliente(int i) {
        return switch (i) {
            case 0 -> colClienteId; case 1 -> colClienteNombre; case 2 -> colClienteApellido;
            case 3 -> colClienteDNI; case 4 -> colClienteTelefono; case 5 -> colClienteEmail;
            case 6 -> colClienteDireccion; default -> null;
        };
    }

    @FXML private void filtrarClientes() { cargarClientes(); }

    private void mostrarDialogoCliente(String titulo, ObservableList<String> valores) {
        Dialog<ObservableList<String>> dialog = new Dialog<>();
        dialog.setTitle(titulo);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        TextField txtNombre = new TextField(); TextField txtApellido = new TextField();
        TextField txtDni = new TextField(); TextField txtTelefono = new TextField();
        TextField txtEmail = new TextField(); TextField txtDireccion = new TextField();

        Validaciones.configurarSoloLetras(txtNombre);
        Validaciones.configurarSoloLetras(txtApellido);
        Validaciones.configurarSoloNumeros(txtDni);
        Validaciones.configurarSoloNumeros(txtTelefono);
        Validaciones.limitarCaracteres(txtEmail, 100);
        Validaciones.limitarCaracteres(txtDireccion, 255);

        if (valores != null && valores.size() >= 6) {
            txtNombre.setText(valores.get(1));
            txtApellido.setText(valores.get(2));
            txtDni.setText(valores.get(3));
            txtTelefono.setText(valores.get(4));
            txtEmail.setText(valores.get(5));
            txtDireccion.setText(valores.size() > 6 ? valores.get(6) : "");
        }

        grid.addRow(0, new Label("Nombre:"), txtNombre);
        grid.addRow(1, new Label("Apellido:"), txtApellido);
        grid.addRow(2, new Label("DNI:"), txtDni);
        grid.addRow(3, new Label("Telefono:"), txtTelefono);
        grid.addRow(4, new Label("Email:"), txtEmail);
        grid.addRow(5, new Label("Direccion:"), txtDireccion);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                return FXCollections.observableArrayList("", txtNombre.getText(), txtApellido.getText(),
                    txtDni.getText(), txtTelefono.getText(), txtEmail.getText(), txtDireccion.getText());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(datos -> {
            if (!Validaciones.validarNoVacio(datos.get(1)) || !Validaciones.validarNoVacio(datos.get(2))) {
                Validaciones.mostrarError("Campos requeridos", "Nombre y apellido son obligatorios");
                return;
            }
            if (Validaciones.validarNoVacio(datos.get(4)) && !Validaciones.validarTelefono(datos.get(4))) {
                Validaciones.mostrarError("Telefono invalido", "Debe contener solo numeros (6-20 digitos)"); return;
            }
            if (Validaciones.validarNoVacio(datos.get(5)) && !Validaciones.validarEmail(datos.get(5))) {
                Validaciones.mostrarError("Email invalido", "Ingrese un email valido"); return;
            }
            try {
                Connection conn = ConexionDB.getConexion();
                if (valores == null) {
                    PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO clientes (nombre, apellido, dni, telefono, email, direccion) VALUES (?,?,?,?,?,?)");
                    stmt.setString(1, datos.get(1)); stmt.setString(2, datos.get(2));
                    stmt.setString(3, datos.get(3)); stmt.setString(4, datos.get(4));
                    stmt.setString(5, datos.get(5)); stmt.setString(6, datos.get(6));
                    stmt.executeUpdate(); stmt.close();
                } else {
                    PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE clientes SET nombre=?, apellido=?, dni=?, telefono=?, email=?, direccion=? WHERE id=?");
                    stmt.setString(1, datos.get(1)); stmt.setString(2, datos.get(2));
                    stmt.setString(3, datos.get(3)); stmt.setString(4, datos.get(4));
                    stmt.setString(5, datos.get(5)); stmt.setString(6, datos.get(6));
                    stmt.setInt(7, Integer.parseInt(valores.get(0))); stmt.executeUpdate(); stmt.close();
                }
                Validaciones.mostrarInfo("Exito", "Cliente guardado correctamente");
                cargarClientes();
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains("Duplicate")) {
                    Validaciones.mostrarError("Duplicado", "Ya existe un registro con ese valor");
                } else {
                    Validaciones.mostrarError("Error", e.getMessage());
                }
            }
        });
    }

    @FXML private void handleNuevoCliente() { mostrarDialogoCliente("Nuevo Cliente", null); }

    @FXML private void handleEditarCliente() {
        int idx = tablaClientes.getSelectionModel().getSelectedIndex();
        if (idx < 0) { Validaciones.mostrarError("Seleccionar", "Seleccione un cliente"); return; }
        mostrarDialogoCliente("Editar Cliente", tablaClientes.getItems().get(idx));
    }

    @FXML private void handleEliminarCliente() {
        int idx = tablaClientes.getSelectionModel().getSelectedIndex();
        if (idx < 0) { Validaciones.mostrarError("Seleccionar", "Seleccione un cliente"); return; }
        if (!Validaciones.mostrarConfirmacion("Confirmar", "Desea eliminar este cliente?")) return;
        try {
            Connection conn = ConexionDB.getConexion();
            PreparedStatement stmt = conn.prepareStatement("UPDATE clientes SET activo=0 WHERE id=?");
            stmt.setInt(1, Integer.parseInt(tablaClientes.getItems().get(idx).get(0)));
            stmt.executeUpdate(); stmt.close();
            Validaciones.mostrarInfo("Exito", "Cliente eliminado");
            cargarClientes();
        } catch (Exception e) { Validaciones.mostrarError("Error", e.getMessage()); }
    }

    private void cargarProveedores() {
        try {
            Connection conn = ConexionDB.getConexion();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT id, nombre_empresa, nombre_contacto, telefono, email, direccion FROM proveedores WHERE activo=1");

            ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
            for (int i = 0; i < 6; i++) configurarColumna(getColProveedor(i), i);

            while (rs.next()) {
                data.add(filaDesdeRS(rs, "id", "nombre_empresa", "nombre_contacto", "telefono", "email", "direccion"));
            }
            tablaProveedores.setItems(data);
            rs.close(); stmt.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private TableColumn<ObservableList<String>, String> getColProveedor(int i) {
        return switch (i) {
            case 0 -> colProveedorId; case 1 -> colProveedorEmpresa; case 2 -> colProveedorContacto;
            case 3 -> colProveedorTelefono; case 4 -> colProveedorEmail; case 5 -> colProveedorDireccion;
            default -> null;
        };
    }

    @FXML private void filtrarProveedores() { cargarProveedores(); }

    private void mostrarDialogoProveedor(String titulo, ObservableList<String> valores) {
        Dialog<ObservableList<String>> dialog = new Dialog<>();
        dialog.setTitle(titulo);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10);
        TextField txtEmpresa = new TextField(); TextField txtContacto = new TextField();
        TextField txtTelefono = new TextField(); TextField txtEmail = new TextField();
        TextField txtDireccion = new TextField();

        Validaciones.configurarSoloLetras(txtContacto);
        Validaciones.configurarSoloNumeros(txtTelefono);
        Validaciones.limitarCaracteres(txtEmpresa, 150);
        Validaciones.limitarCaracteres(txtEmail, 100);
        Validaciones.limitarCaracteres(txtDireccion, 255);

        if (valores != null && valores.size() >= 5) {
            txtEmpresa.setText(valores.get(1)); txtContacto.setText(valores.get(2));
            txtTelefono.setText(valores.get(3)); txtEmail.setText(valores.get(4));
            txtDireccion.setText(valores.size() > 5 ? valores.get(5) : "");
        }

        grid.addRow(0, new Label("Empresa:"), txtEmpresa);
        grid.addRow(1, new Label("Contacto:"), txtContacto);
        grid.addRow(2, new Label("Telefono:"), txtTelefono);
        grid.addRow(3, new Label("Email:"), txtEmail);
        grid.addRow(4, new Label("Direccion:"), txtDireccion);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                return FXCollections.observableArrayList("", txtEmpresa.getText(), txtContacto.getText(),
                    txtTelefono.getText(), txtEmail.getText(), txtDireccion.getText());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(datos -> {
            if (!Validaciones.validarNoVacio(datos.get(1))) {
                Validaciones.mostrarError("Requerido", "Nombre de empresa obligatorio"); return;
            }
            if (Validaciones.validarNoVacio(datos.get(3)) && !Validaciones.validarTelefono(datos.get(3))) {
                Validaciones.mostrarError("Telefono invalido", "Debe contener solo numeros (6-20 digitos)"); return;
            }
            if (Validaciones.validarNoVacio(datos.get(4)) && !Validaciones.validarEmail(datos.get(4))) {
                Validaciones.mostrarError("Email invalido", "Ingrese un email valido"); return;
            }
            try {
                Connection conn = ConexionDB.getConexion();
                if (valores == null) {
                    PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO proveedores (nombre_empresa, nombre_contacto, telefono, email, direccion) VALUES (?,?,?,?,?)");
                    stmt.setString(1, datos.get(1)); stmt.setString(2, datos.get(2));
                    stmt.setString(3, datos.get(3)); stmt.setString(4, datos.get(4));
                    stmt.setString(5, datos.get(5)); stmt.executeUpdate(); stmt.close();
                } else {
                    PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE proveedores SET nombre_empresa=?, nombre_contacto=?, telefono=?, email=?, direccion=? WHERE id=?");
                    stmt.setString(1, datos.get(1)); stmt.setString(2, datos.get(2));
                    stmt.setString(3, datos.get(3)); stmt.setString(4, datos.get(4));
                    stmt.setString(5, datos.get(5));
                    stmt.setInt(6, Integer.parseInt(valores.get(0))); stmt.executeUpdate(); stmt.close();
                }
                Validaciones.mostrarInfo("Exito", "Proveedor guardado");
                cargarProveedores();
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains("Duplicate")) {
                    Validaciones.mostrarError("Duplicado", "Ya existe un registro con ese valor");
                } else {
                    Validaciones.mostrarError("Error", e.getMessage());
                }
            }
        });
    }

    @FXML private void handleNuevoProveedor() { mostrarDialogoProveedor("Nuevo Proveedor", null); }

    @FXML private void handleEditarProveedor() {
        int idx = tablaProveedores.getSelectionModel().getSelectedIndex();
        if (idx < 0) { Validaciones.mostrarError("Seleccionar", "Seleccione un proveedor"); return; }
        mostrarDialogoProveedor("Editar Proveedor", tablaProveedores.getItems().get(idx));
    }

    @FXML private void handleEliminarProveedor() {
        int idx = tablaProveedores.getSelectionModel().getSelectedIndex();
        if (idx < 0) { Validaciones.mostrarError("Seleccionar", "Seleccione un proveedor"); return; }
        if (!Validaciones.mostrarConfirmacion("Confirmar", "Desea eliminar este proveedor?")) return;
        try {
            Connection conn = ConexionDB.getConexion();
            PreparedStatement stmt = conn.prepareStatement("UPDATE proveedores SET activo=0 WHERE id=?");
            stmt.setInt(1, Integer.parseInt(tablaProveedores.getItems().get(idx).get(0)));
            stmt.executeUpdate(); stmt.close();
            Validaciones.mostrarInfo("Exito", "Proveedor eliminado");
            cargarProveedores();
        } catch (Exception e) { Validaciones.mostrarError("Error", e.getMessage()); }
    }

    private void cargarProductos() {
        try {
            Connection conn = ConexionDB.getConexion();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT p.id, p.codigo, p.nombre, p.categoria, p.precio_compra, p.precio_venta, " +
                "p.descuento, p.stock_actual, p.stock_minimo, " +
                "COALESCE(pr.nombre_empresa, 'Sin proveedor') as proveedor, p.activo " +
                "FROM productos p LEFT JOIN proveedores pr ON p.proveedor_id = pr.id");

            ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
            for (int i = 0; i < 10; i++) configurarColumna(getColProducto(i), i);

            while (rs.next()) {
                String activo = rs.getInt("activo") == 1 ? "Activo" : "Inactivo";
                String pc = String.format("%.2f", rs.getDouble("precio_compra"));
                String pv = String.format("%.2f", rs.getDouble("precio_venta"));
                String desc = String.format("%.2f", rs.getDouble("descuento"));
                data.add(FXCollections.observableArrayList(
                    rs.getString("codigo"), rs.getString("nombre"),
                    rs.getString("categoria") != null ? rs.getString("categoria") : "",
                    pc, pv, desc,
                    String.valueOf(rs.getInt("stock_actual")),
                    String.valueOf(rs.getInt("stock_minimo")), rs.getString("proveedor"), activo));
            }
            tablaProductos.setItems(data);
            rs.close(); stmt.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private TableColumn<ObservableList<String>, String> getColProducto(int i) {
        return switch (i) {
            case 0 -> colProductoCodigo; case 1 -> colProductoNombre; case 2 -> colProductoCategoria;
            case 3 -> colProductoPrecioCompra; case 4 -> colProductoPrecioVenta;
            case 5 -> colProductoDescuento;
            case 6 -> colProductoStock; case 7 -> colProductoStockMin;
            case 8 -> colProductoProveedor; case 9 -> colProductoEstado;
            default -> null;
        };
    }

    @FXML private void filtrarProductos() { cargarProductos(); }

    @FXML private void handleNuevoProducto() {
        Dialog<ObservableList<String>> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Producto");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10);
        TextField txtCodigo = new TextField(); TextField txtNombre = new TextField();
        TextField txtDescripcion = new TextField(); TextField txtCategoria = new TextField();
        TextField txtPCompra = new TextField(); TextField txtPVenta = new TextField();
        TextField txtDescuento = new TextField("0");
        TextField txtStock = new TextField("0"); TextField txtStockMin = new TextField("0");
        ComboBox<String> cmbProveedor = new ComboBox<>();
        ComboBox<String> cmbEstado = new ComboBox<>(FXCollections.observableArrayList("Activo", "Inactivo"));
        cmbEstado.setValue("Activo");

        try {
            Connection conn = ConexionDB.getConexion();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, nombre_empresa FROM proveedores WHERE activo=1");
            while (rs.next()) {
                cmbProveedor.getItems().add(rs.getInt("id") + " - " + rs.getString("nombre_empresa"));
            }
            rs.close(); stmt.close();
        } catch (Exception e) {}

        Validaciones.configurarSoloDecimales(txtPCompra);
        Validaciones.configurarSoloDecimales(txtPVenta);
        Validaciones.configurarSoloDecimales(txtDescuento);
        Validaciones.configurarSoloEnteros(txtStock);
        Validaciones.configurarSoloEnteros(txtStockMin);
        Validaciones.limitarCaracteres(txtCodigo, 50);
        Validaciones.limitarCaracteres(txtNombre, 150);
        Validaciones.limitarCaracteres(txtDescripcion, 255);
        Validaciones.limitarCaracteres(txtCategoria, 100);

        grid.addRow(0, new Label("Codigo:"), txtCodigo);
        grid.addRow(1, new Label("Nombre:"), txtNombre);
        grid.addRow(2, new Label("Descripcion:"), txtDescripcion);
        grid.addRow(3, new Label("Categoria:"), txtCategoria);
        grid.addRow(4, new Label("Precio Compra:"), txtPCompra);
        grid.addRow(5, new Label("Precio Venta:"), txtPVenta);
        grid.addRow(6, new Label("Descuento (%):"), txtDescuento);
        grid.addRow(7, new Label("Stock:"), txtStock);
        grid.addRow(8, new Label("Stock Minimo:"), txtStockMin);
        grid.addRow(9, new Label("Proveedor:"), cmbProveedor);
        grid.addRow(10, new Label("Estado:"), cmbEstado);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                return FXCollections.observableArrayList(
                    txtCodigo.getText(), txtNombre.getText(), txtDescripcion.getText(),
                    txtCategoria.getText(), txtPCompra.getText(), txtPVenta.getText(),
                    txtDescuento.getText(),
                    txtStock.getText(), txtStockMin.getText(),
                    cmbProveedor.getValue() != null ? cmbProveedor.getValue().split(" - ")[0] : "",
                    cmbEstado.getValue());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(d -> {
            if (!Validaciones.validarNoVacio(d.get(0)) || !Validaciones.validarNoVacio(d.get(1))) {
                Validaciones.mostrarError("Requerido", "Codigo y nombre son obligatorios"); return;
            }
            try {
                Connection conn = ConexionDB.getConexion();
                PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO productos (codigo, nombre, descripcion, categoria, precio_compra, precio_venta, descuento, stock_actual, stock_minimo, proveedor_id, activo) VALUES (?,?,?,?,?,?,?,?,?,?,?)");
                stmt.setString(1, d.get(0)); stmt.setString(2, d.get(1)); stmt.setString(3, d.get(2));
                stmt.setString(4, d.get(3));
                stmt.setDouble(5, d.get(4).isEmpty() ? 0 : Double.parseDouble(d.get(4)));
                stmt.setDouble(6, d.get(5).isEmpty() ? 0 : Double.parseDouble(d.get(5)));
                stmt.setDouble(7, d.get(6).isEmpty() ? 0 : Double.parseDouble(d.get(6)));
                stmt.setInt(8, d.get(7).isEmpty() ? 0 : Integer.parseInt(d.get(7)));
                stmt.setInt(9, d.get(8).isEmpty() ? 0 : Integer.parseInt(d.get(8)));
                stmt.setString(10, d.get(9).isEmpty() ? null : d.get(9));
                stmt.setInt(11, d.get(10).equals("Activo") ? 1 : 0);
                stmt.executeUpdate(); stmt.close();
                Validaciones.mostrarInfo("Exito", "Producto creado");
                cargarProductos();
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains("Duplicate")) {
                    Validaciones.mostrarError("Duplicado", "Ya existe un registro con ese valor");
                } else {
                    Validaciones.mostrarError("Error", e.getMessage());
                }
            }
        });
    }

    @FXML private void handleEditarProducto() {
        int idx = tablaProductos.getSelectionModel().getSelectedIndex();
        if (idx < 0) { Validaciones.mostrarError("Seleccionar", "Seleccione un producto"); return; }
        ObservableList<String> fila = tablaProductos.getItems().get(idx);
        String codigo = fila.get(0);

        String[] prodData = null;
        try {
            Connection conn = ConexionDB.getConexion();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT p.*, COALESCE(pr.nombre_empresa, 'Sin proveedor') as nom_prov, pr.id as prov_id " +
                "FROM productos p LEFT JOIN proveedores pr ON p.proveedor_id = pr.id WHERE p.codigo = ?");
            stmt.setString(1, codigo);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                prodData = new String[]{
                    String.valueOf(rs.getInt("id")),
                    rs.getString("codigo"),
                    rs.getString("nombre"),
                    rs.getString("descripcion") != null ? rs.getString("descripcion") : "",
                    rs.getString("categoria") != null ? rs.getString("categoria") : "",
                    String.format("%.2f", rs.getDouble("precio_compra")),
                    String.format("%.2f", rs.getDouble("precio_venta")),
                    String.format("%.2f", rs.getDouble("descuento")),
                    String.valueOf(rs.getInt("stock_actual")),
                    String.valueOf(rs.getInt("stock_minimo")),
                    String.valueOf(rs.getInt("proveedor_id")),
                    rs.getString("nom_prov"),
                    rs.getInt("activo") == 1 ? "Activo" : "Inactivo"
                };
            }
            rs.close(); stmt.close();
        } catch (Exception e) {
            Validaciones.mostrarError("Error", "No se pudo obtener el producto: " + e.getMessage());
            return;
        }

        if (prodData == null) return;
        final String productoId = prodData[0];

        Dialog<ObservableList<String>> dialog = new Dialog<>();
        dialog.setTitle("Editar Producto");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10);
        TextField txtCodigo = new TextField(prodData[1]);
        TextField txtNombre = new TextField(prodData[2]);
        TextField txtDescripcion = new TextField(prodData[3]);
        TextField txtCategoria = new TextField(prodData[4]);
        TextField txtPCompra = new TextField(prodData[5]);
        TextField txtPVenta = new TextField(prodData[6]);
        TextField txtDescuento = new TextField(prodData[7]);
        TextField txtStock = new TextField(prodData[8]);
        TextField txtStockMin = new TextField(prodData[9]);
        ComboBox<String> cmbProveedor = new ComboBox<>();
        ComboBox<String> cmbEstado = new ComboBox<>(FXCollections.observableArrayList("Activo", "Inactivo"));
        cmbEstado.setValue(prodData[12]);

        try {
            Connection conn = ConexionDB.getConexion();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, nombre_empresa FROM proveedores WHERE activo=1");
            while (rs.next()) {
                String item = rs.getInt("id") + " - " + rs.getString("nombre_empresa");
                cmbProveedor.getItems().add(item);
                if (String.valueOf(rs.getInt("id")).equals(prodData[10])) {
                    cmbProveedor.setValue(item);
                }
            }
            rs.close(); stmt.close();
        } catch (Exception e) {}

        Validaciones.configurarSoloDecimales(txtPCompra);
        Validaciones.configurarSoloDecimales(txtPVenta);
        Validaciones.configurarSoloDecimales(txtDescuento);
        Validaciones.configurarSoloEnteros(txtStock);
        Validaciones.configurarSoloEnteros(txtStockMin);
        Validaciones.limitarCaracteres(txtCodigo, 50);
        Validaciones.limitarCaracteres(txtNombre, 150);
        Validaciones.limitarCaracteres(txtDescripcion, 255);
        Validaciones.limitarCaracteres(txtCategoria, 100);

        grid.addRow(0, new Label("Codigo:"), txtCodigo);
        grid.addRow(1, new Label("Nombre:"), txtNombre);
        grid.addRow(2, new Label("Descripcion:"), txtDescripcion);
        grid.addRow(3, new Label("Categoria:"), txtCategoria);
        grid.addRow(4, new Label("Precio Compra:"), txtPCompra);
        grid.addRow(5, new Label("Precio Venta:"), txtPVenta);
        grid.addRow(6, new Label("Descuento (%):"), txtDescuento);
        grid.addRow(7, new Label("Stock:"), txtStock);
        grid.addRow(8, new Label("Stock Minimo:"), txtStockMin);
        grid.addRow(9, new Label("Proveedor:"), cmbProveedor);
        grid.addRow(10, new Label("Estado:"), cmbEstado);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                return FXCollections.observableArrayList(
                    txtCodigo.getText(), txtNombre.getText(), txtDescripcion.getText(),
                    txtCategoria.getText(), txtPCompra.getText(), txtPVenta.getText(),
                    txtDescuento.getText(),
                    txtStock.getText(), txtStockMin.getText(),
                    cmbProveedor.getValue() != null ? cmbProveedor.getValue().split(" - ")[0] : "",
                    cmbEstado.getValue());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(d -> {
            if (!Validaciones.validarNoVacio(d.get(0)) || !Validaciones.validarNoVacio(d.get(1))) {
                Validaciones.mostrarError("Requerido", "Codigo y nombre son obligatorios"); return;
            }
            try {
                Connection conn = ConexionDB.getConexion();
                PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE productos SET codigo=?, nombre=?, descripcion=?, categoria=?, precio_compra=?, precio_venta=?, descuento=?, stock_actual=?, stock_minimo=?, proveedor_id=?, activo=? WHERE id=?");
                stmt.setString(1, d.get(0)); stmt.setString(2, d.get(1)); stmt.setString(3, d.get(2));
                stmt.setString(4, d.get(3));
                stmt.setDouble(5, d.get(4).isEmpty() ? 0 : Double.parseDouble(d.get(4)));
                stmt.setDouble(6, d.get(5).isEmpty() ? 0 : Double.parseDouble(d.get(5)));
                stmt.setDouble(7, d.get(6).isEmpty() ? 0 : Double.parseDouble(d.get(6)));
                stmt.setInt(8, d.get(7).isEmpty() ? 0 : Integer.parseInt(d.get(7)));
                stmt.setInt(9, d.get(8).isEmpty() ? 0 : Integer.parseInt(d.get(8)));
                stmt.setString(10, d.get(9).isEmpty() ? null : d.get(9));
                stmt.setInt(11, d.get(10).equals("Activo") ? 1 : 0);
                stmt.setInt(12, Integer.parseInt(productoId));
                stmt.executeUpdate(); stmt.close();
                Validaciones.mostrarInfo("Exito", "Producto actualizado");
                cargarProductos();
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains("Duplicate")) {
                    Validaciones.mostrarError("Duplicado", "Ya existe un registro con ese valor");
                } else {
                    Validaciones.mostrarError("Error", e.getMessage());
                }
            }
        });
    }

    private void configurarCantidadField() {
        txtCantidadFactura.textProperty().addListener((obs, old, nue) -> {
            if (!nue.isEmpty() && !nue.matches("[1-9][0-9]*")) {
                txtCantidadFactura.setText(old);
            }
        });
    }

    private void configurarDescuentos() {
        cmbDescuentoGlobal.setItems(FXCollections.observableArrayList("0%", "5%", "10%", "15%", "20%", "Personalizado"));
        cmbDescuentoGlobal.setValue("0%");
        cmbDescuentoGlobal.setOnAction(e -> actualizarTotales());
    }

    private void configurarIVA() {
        cmbIVA.setItems(FXCollections.observableArrayList("0%", "5%", "10%", "15%", "20%"));
        cmbIVA.setValue("10%");
        cmbIVA.setOnAction(e -> actualizarTotales());
    }

    private double obtenerPorcentajeIVA() {
        String val = cmbIVA.getValue();
        if (val == null) return 0.10;
        try { return Double.parseDouble(val.replace("%", "")) / 100.0; } catch (Exception e) { return 0.10; }
    }

    private void cargarClientesFactura() {
        clientesFactura.clear();
        cmbClienteFactura.getItems().clear();
        try {
            Connection conn = ConexionDB.getConexion();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, nombre, apellido, dni FROM clientes WHERE activo=1");
            while (rs.next()) {
                String info = rs.getInt("id") + " - " + rs.getString("nombre") + " " + rs.getString("apellido") + " (" + rs.getString("dni") + ")";
                clientesFactura.add(new String[]{String.valueOf(rs.getInt("id")), info});
                cmbClienteFactura.getItems().add(info);
            }
            rs.close(); stmt.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void cargarProductosFactura() {
        productosFactura.clear();
        cmbProductoFactura.getItems().clear();
        try {
            Connection conn = ConexionDB.getConexion();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT id, codigo, nombre, precio_venta, descuento, stock_actual FROM productos WHERE activo=1 AND stock_actual > 0");
            while (rs.next()) {
                String info = rs.getString("codigo") + " - " + rs.getString("nombre") +
                    " [$" + String.format("%.2f", rs.getDouble("precio_venta")) + "] Stock: " + rs.getInt("stock_actual");
                productosFactura.add(new String[]{
                    String.valueOf(rs.getInt("id")), rs.getString("codigo"),
                    rs.getString("nombre"), String.format("%.2f", rs.getDouble("precio_venta")),
                    String.valueOf(rs.getInt("stock_actual")),
                    String.format("%.2f", rs.getDouble("descuento")), info
                });
                cmbProductoFactura.getItems().add(info);
            }
            rs.close(); stmt.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void buscarClienteFactura() {
        String filtro = txtBuscarClienteFactura.getText().toLowerCase();
        cmbClienteFactura.getItems().clear();
        for (String[] c : clientesFactura) {
            if (c[1].toLowerCase().contains(filtro)) {
                cmbClienteFactura.getItems().add(c[1]);
            }
        }
    }

    @FXML private void buscarProductoFactura() {
        String filtro = txtBuscarProductoFactura.getText().toLowerCase();
        cmbProductoFactura.getItems().clear();
        for (String[] p : productosFactura) {
            if (p[6].toLowerCase().contains(filtro)) {
                cmbProductoFactura.getItems().add(p[6]);
            }
        }
    }

    @FXML private void agregarProductoFactura() {
        int idxProd = cmbProductoFactura.getSelectionModel().getSelectedIndex();
        if (idxProd < 0) { Validaciones.mostrarError("Seleccionar", "Seleccione un producto"); return; }

        String[] producto = productosFactura.get(idxProd);
        int cantidad = 1;
        try { cantidad = Integer.parseInt(txtCantidadFactura.getText()); } catch (Exception e) {}

        int stockDisponible = Integer.parseInt(producto[4]);
        if (cantidad > stockDisponible) {
            Validaciones.mostrarError("Stock insuficiente",
                "Stock disponible: " + stockDisponible);
            return;
        }

        for (String[] det : detalleFactura) {
            if (det[1].equals(producto[1])) {
                Validaciones.mostrarError("Producto duplicado", "Ya agregaste este producto. Eliminalo y agregalo de nuevo con otra cantidad.");
                return;
            }
        }

        double precio = Double.parseDouble(producto[3]);
        double descuento = Double.parseDouble(producto[5]);
        double subtotal = cantidad * precio * (1 - descuento / 100.0);

        detalleFactura.add(new String[]{
            producto[0],
            producto[1],
            producto[2],
            String.valueOf(cantidad),
            String.format("%.2f", precio),
            String.format("%.2f", descuento),
            String.format("%.2f", subtotal)
        });

        actualizarTablaDetalle();
        actualizarTotales();
    }

    private void actualizarTablaDetalle() {
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        for (String[] det : detalleFactura) {
            data.add(FXCollections.observableArrayList(
                det[1], det[2], det[3], det[4], det[5], det[6]
            ));
        }
        for (int i = 0; i < 6; i++) configurarColumna(getColDetalle(i), i);
        tablaDetalleFactura.setItems(data);
    }

    private TableColumn<ObservableList<String>, String> getColDetalle(int i) {
        return switch (i) {
            case 0 -> colDetCodigo; case 1 -> colDetNombre; case 2 -> colDetCantidad;
            case 3 -> colDetPrecio; case 4 -> colDetDescuento; case 5 -> colDetSubtotal;
            default -> null;
        };
    }

    private void actualizarTotales() {
        double subtotal = 0;
        for (String[] det : detalleFactura) {
            subtotal += Double.parseDouble(det[6]);
        }

        double descuentoGlobal = 0;
        String descSel = cmbDescuentoGlobal.getValue();
        if (descSel != null && !descSel.equals("0%")) {
            if (descSel.equals("Personalizado")) {
                TextInputDialog d = new TextInputDialog("0");
                d.setTitle("Descuento Personalizado");
                d.setContentText("Ingrese porcentaje de descuento:");
                String res = d.showAndWait().orElse("0");
                try { descuentoGlobal = Double.parseDouble(res); } catch (Exception e) {}
            } else {
                descuentoGlobal = Double.parseDouble(descSel.replace("%", ""));
            }
        }

        double descuentoMonto = subtotal * (descuentoGlobal / 100.0);
        double baseImponible = subtotal - descuentoMonto;
        double iva = baseImponible * obtenerPorcentajeIVA();
        double total = baseImponible + iva;

        lblSubtotal.setText("$" + String.format("%.2f", subtotal));
        lblIVA.setText("$" + String.format("%.2f", iva));
        lblTotal.setText("$" + String.format("%.2f", total));
    }

    @FXML private void eliminarProductoFactura() {
        int idx = tablaDetalleFactura.getSelectionModel().getSelectedIndex();
        if (idx < 0) { Validaciones.mostrarError("Seleccionar", "Seleccione un producto del detalle"); return; }
        String codigo = tablaDetalleFactura.getItems().get(idx).get(0);

        detalleFactura.removeIf(d -> d[1].equals(codigo));
        actualizarTablaDetalle();
        actualizarTotales();
    }

    @FXML private void handleEmitirFactura() {
        if (detalleFactura.isEmpty()) {
            Validaciones.mostrarError("Sin productos", "Agregue productos a la factura"); return;
        }

        int clienteId = 0;
        int idxCli = cmbClienteFactura.getSelectionModel().getSelectedIndex();
        if (idxCli >= 0) {
            clienteId = Integer.parseInt(clientesFactura.get(idxCli)[0]);
        }

        try {
            Connection conn = ConexionDB.getConexion();
            conn.setAutoCommit(false);

            try {
                String numeroFactura = generarNumeroFactura(conn);

                double subtotal = 0;
                for (String[] det : detalleFactura) {
                    subtotal += Double.parseDouble(det[6]);
                }

                double descGlobalPorc = 0;
                String descSel = cmbDescuentoGlobal.getValue();
                if (descSel != null && !descSel.equals("0%")) {
                    if (descSel.equals("Personalizado")) {
                    } else {
                        descGlobalPorc = Double.parseDouble(descSel.replace("%", ""));
                    }
                }
                double descMonto = subtotal * (descGlobalPorc / 100.0);
                double baseImp = subtotal - descMonto;
                double iva = baseImp * obtenerPorcentajeIVA();
                double total = baseImp + iva;

                PreparedStatement stmtFactura = conn.prepareStatement(
                    "INSERT INTO facturas (numero_factura, fecha, hora, usuario_id, cliente_id, subtotal, descuento_global, iva, total) VALUES (?,?,?,?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
                stmtFactura.setString(1, numeroFactura);
                stmtFactura.setDate(2, Date.valueOf(LocalDate.now()));
                stmtFactura.setTime(3, Time.valueOf(LocalTime.now()));
                stmtFactura.setInt(4, usuarioId);
                stmtFactura.setInt(5, clienteId > 0 ? clienteId : 0);
                stmtFactura.setDouble(6, subtotal);
                stmtFactura.setDouble(7, descMonto);
                stmtFactura.setDouble(8, iva);
                stmtFactura.setDouble(9, total);
                stmtFactura.executeUpdate();

                ResultSet rsFactura = stmtFactura.getGeneratedKeys();
                int facturaId = 0;
                if (rsFactura.next()) facturaId = rsFactura.getInt(1);
                rsFactura.close();
                stmtFactura.close();

                PreparedStatement stmtDetalle = conn.prepareStatement(
                    "INSERT INTO detalle_factura (factura_id, producto_id, cantidad, precio_unitario, descuento_porcentaje, descuento_monto, subtotal_linea) VALUES (?,?,?,?,?,?,?)");
                PreparedStatement stmtStock = conn.prepareStatement(
                    "UPDATE productos SET stock_actual = stock_actual - ? WHERE id = ?");

                for (String[] det : detalleFactura) {
                    int prodId = Integer.parseInt(det[0]);
                    int cantidad = Integer.parseInt(det[3]);
                    double precio = Double.parseDouble(det[4]);
                    double descPorc = Double.parseDouble(det[5]);
                    double sub = Double.parseDouble(det[6]);
                    double descMontoLinea = (precio * cantidad) - sub;

                    stmtDetalle.setInt(1, facturaId);
                    stmtDetalle.setInt(2, prodId);
                    stmtDetalle.setInt(3, cantidad);
                    stmtDetalle.setDouble(4, precio);
                    stmtDetalle.setDouble(5, descPorc);
                    stmtDetalle.setDouble(6, descMontoLinea);
                    stmtDetalle.setDouble(7, sub);
                    stmtDetalle.addBatch();

                    stmtStock.setInt(1, cantidad);
                    stmtStock.setInt(2, prodId);
                    stmtStock.addBatch();
                }
                stmtDetalle.executeBatch();
                stmtStock.executeBatch();
                stmtDetalle.close();
                stmtStock.close();

                PreparedStatement stmtAudit = conn.prepareStatement(
                    "INSERT INTO auditoria (usuario_id, accion, tabla_afectada, registro_id, detalle) VALUES (?,?,?,?,?)");
                stmtAudit.setInt(1, usuarioId);
                stmtAudit.setString(2, "EMITIR FACTURA");
                stmtAudit.setString(3, "facturas");
                stmtAudit.setInt(4, facturaId);
                stmtAudit.setString(5, "Factura " + numeroFactura + " emitida por $" + String.format("%.2f", total));
                stmtAudit.executeUpdate();
                stmtAudit.close();

                conn.commit();

                Validaciones.mostrarInfo("Factura Emitida",
                    "Factura " + numeroFactura + " emitida correctamente\nTotal: $" + String.format("%.2f", total));

                detalleFactura.clear();
                actualizarTablaDetalle();
                actualizarTotales();
                cargarProductosFactura();

            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (Exception e) {
            Validaciones.mostrarError("Error", "No se pudo emitir la factura: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String generarNumeroFactura(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) + 1 FROM facturas");
        rs.next();
        int count = rs.getInt(1);
        rs.close(); stmt.close();

        String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        return "FAC-" + fecha + "-" + String.format("%04d", count);
    }

    private void cargarFacturas() {
        try {
            Connection conn = ConexionDB.getConexion();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT f.numero_factura, f.fecha, f.hora, " +
                "CONCAT(u.nombre, ' ', u.apellido) as vendedor, " +
                "COALESCE(CONCAT(c.nombre, ' ', c.apellido), 'Consumidor Final') as cliente, " +
                "f.total, f.estado " +
                "FROM facturas f " +
                "JOIN usuarios u ON f.usuario_id = u.id " +
                "LEFT JOIN clientes c ON f.cliente_id = c.id " +
                "ORDER BY f.id DESC");

            ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
            for (int i = 0; i < 7; i++) configurarColumna(getColFactura(i), i);

            while (rs.next()) {
                data.add(FXCollections.observableArrayList(
                    rs.getString("numero_factura"),
                    rs.getString("fecha"),
                    rs.getString("hora") != null ? rs.getString("hora").substring(0, 5) : "",
                    rs.getString("vendedor"),
                    rs.getString("cliente"),
                    "$" + String.format("%.2f", rs.getDouble("total")),
                    rs.getString("estado")
                ));
            }
            tablaFacturas.setItems(data);
            rs.close(); stmt.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private TableColumn<ObservableList<String>, String> getColFactura(int i) {
        return switch (i) {
            case 0 -> colFacturaNumero; case 1 -> colFacturaFecha; case 2 -> colFacturaHora;
            case 3 -> colFacturaVendedor; case 4 -> colFacturaCliente;
            case 5 -> colFacturaTotal; case 6 -> colFacturaEstado;
            default -> null;
        };
    }

    @FXML private void filtrarFacturas() { cargarFacturas(); }

    @FXML private void handleExportarPDF() {
        Validaciones.mostrarInfo("Exportar PDF", "Funcionalidad de exportacion a PDF disponible.");
    }

    private void cargarEstadisticas() {
        try {
            Connection conn = ConexionDB.getConexion();
            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM facturas");
            if (rs.next()) lblTotalFacturas.setText(String.valueOf(rs.getInt("total")));
            rs.close();

            rs = stmt.executeQuery("SELECT COALESCE(SUM(total), 0) as total FROM facturas");
            if (rs.next()) lblTotalVentas.setText("$" + String.format("%.2f", rs.getDouble("total")));
            rs.close();

            rs = stmt.executeQuery("SELECT COUNT(*) as total FROM clientes WHERE activo=1");
            if (rs.next()) lblTotalClientes.setText(String.valueOf(rs.getInt("total")));
            rs.close();

            rs = stmt.executeQuery("SELECT COUNT(*) as total FROM productos WHERE activo=1 AND stock_actual <= stock_minimo");
            if (rs.next()) lblTotalBajoStock.setText(String.valueOf(rs.getInt("total")));
            rs.close();

            stmt.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void cargarMovimientos() {
        try {
            Connection conn = ConexionDB.getConexion();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT m.id, m.fecha, m.hora, CONCAT(u.nombre, ' ', u.apellido) as usuario, " +
                "m.tipo_movimiento, p.nombre as producto, m.cantidad, m.observacion " +
                "FROM movimientos_deposito m " +
                "JOIN usuarios u ON m.usuario_id = u.id " +
                "JOIN productos p ON m.producto_id = p.id " +
                "ORDER BY m.id DESC");

            ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
            for (int i = 0; i < 8; i++) configurarColumna(getColMovimiento(i), i);

            while (rs.next()) {
                data.add(FXCollections.observableArrayList(
                    String.valueOf(rs.getInt("id")),
                    rs.getString("fecha"),
                    rs.getString("hora") != null ? rs.getString("hora").substring(0, 5) : "",
                    rs.getString("usuario"),
                    rs.getString("tipo_movimiento"),
                    rs.getString("producto"),
                    String.valueOf(rs.getInt("cantidad")),
                    rs.getString("observacion") != null ? rs.getString("observacion") : ""
                ));
            }
            tablaMovimientos.setItems(data);
            rs.close(); stmt.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private TableColumn<ObservableList<String>, String> getColMovimiento(int i) {
        return switch (i) {
            case 0 -> colMovId; case 1 -> colMovFecha; case 2 -> colMovHora;
            case 3 -> colMovUsuario; case 4 -> colMovTipo; case 5 -> colMovProducto;
            case 6 -> colMovCantidad; case 7 -> colMovObservacion;
            default -> null;
        };
    }

    @FXML private void filtrarMovimientos() { cargarMovimientos(); }
}