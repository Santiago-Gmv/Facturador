package controllers;

import database.ConexionDB;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import utils.Validaciones;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class VendedorController {

    private String nombreUsuario;
    private int usuarioId;

    @FXML private Label lblUsuario;

    @FXML private VBox panelBienvenida, panelFacturacion, panelClientes, panelStock, panelMisFacturas;

    @FXML private Label lblFechaActual, lblHoraActual;
    @FXML private TextField txtBuscarProductoFactura, txtCantidadFactura;
    @FXML private ComboBox<String> cmbClienteFactura, cmbProductoFactura;
    @FXML private TableView<ObservableList<String>> tablaDetalleFactura;
    @FXML private TableColumn<ObservableList<String>, String> colDetCodigo, colDetNombre;
    @FXML private TableColumn<ObservableList<String>, String> colDetCantidad, colDetPrecio;
    @FXML private TableColumn<ObservableList<String>, String> colDetDescuento, colDetSubtotal;
    @FXML private Label lblSubtotal, lblIVA, lblTotal;
    @FXML private ComboBox<String> cmbDescuentoGlobal;
    @FXML private ComboBox<String> cmbIVA;

    private ObservableList<String[]> clientesFactura = FXCollections.observableArrayList();
    private ObservableList<String[]> productosFactura = FXCollections.observableArrayList();
    private ObservableList<String[]> detalleFactura = FXCollections.observableArrayList();

    @FXML private TextField txtBuscarCliente;
    @FXML private TableView<ObservableList<String>> tablaClientes;
    @FXML private TableColumn<ObservableList<String>, String> colClienteNombre, colClienteApellido;
    @FXML private TableColumn<ObservableList<String>, String> colClienteDNI, colClienteTelefono;
    @FXML private TableColumn<ObservableList<String>, String> colClienteEmail, colClienteDireccion;

    @FXML private TextField txtBuscarProductoStock;
    @FXML private TableView<ObservableList<String>> tablaStock;
    @FXML private TableColumn<ObservableList<String>, String> colStockCodigo, colStockNombre;
    @FXML private TableColumn<ObservableList<String>, String> colStockCategoria;
    @FXML private TableColumn<ObservableList<String>, String> colStockPrecio;
    @FXML private TableColumn<ObservableList<String>, String> colStockActual, colStockMinimo;

    @FXML private TextField txtBuscarMiFactura;
    @FXML private TableView<ObservableList<String>> tablaMisFacturas;
    @FXML private TableColumn<ObservableList<String>, String> colMFNumero, colMFFecha, colMFHora;
    @FXML private TableColumn<ObservableList<String>, String> colMFCliente, colMFTotal, colMFEstado;

    public void inicializar(String nombreUsuario, int usuarioId) {
        this.nombreUsuario = nombreUsuario;
        this.usuarioId = usuarioId;
        lblUsuario.setText("Usuario: " + nombreUsuario);

        mostrarPanel(panelBienvenida);

        Timeline reloj = new Timeline(new KeyFrame(Duration.seconds(1), e -> actualizarReloj()));
        reloj.setCycleCount(Timeline.INDEFINITE);
        reloj.play();

        configurarDescuentos();
        configurarIVA();
        cargarClientesFactura();
        cargarProductosFactura();
        cargarClientes();
        cargarStock();
        cargarMisFacturas();
    }

    private void actualizarReloj() {
        lblFechaActual.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        lblHoraActual.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }

    private void mostrarPanel(VBox panel) {
        VBox[] paneles = {panelBienvenida, panelFacturacion, panelClientes, panelStock, panelMisFacturas};
        for (VBox p : paneles) {
            p.setVisible(p == panel);
            p.setManaged(p == panel);
        }
    }

    @FXML private void mostrarFacturacion() {
        mostrarPanel(panelFacturacion);
        cargarClientesFactura();
        cargarProductosFactura();
        detalleFactura.clear();
        actualizarTotales();
        Validaciones.configurarSoloEnteros(txtCantidadFactura);
    }

    @FXML private void mostrarClientes() { mostrarPanel(panelClientes); cargarClientes(); }
    @FXML private void mostrarStock() { mostrarPanel(panelStock); cargarStock(); }

    @FXML private void mostrarMisFacturas() {
        mostrarPanel(panelMisFacturas);
        cargarMisFacturas();
    }

    @FXML private void handleCerrarSesion() {
        if (Validaciones.mostrarConfirmacion("Cerrar Sesion", "Desea cerrar la sesion?")) {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
                Stage stage = (Stage) lblUsuario.getScene().getWindow();
                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
                stage.setScene(scene);
                stage.setTitle("Sistema de Facturacion - Login");
            } catch (Exception e) { e.printStackTrace(); }
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

    private void cargarClientes() {
        try {
            Connection conn = ConexionDB.getConexion();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT nombre, apellido, dni, telefono, email, direccion FROM clientes WHERE activo=1");

            ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
            for (int i = 0; i < 6; i++) configurarColumna(getColCliente(i), i);
            while (rs.next()) {
                data.add(FXCollections.observableArrayList(
                    rs.getString("nombre"), rs.getString("apellido"), rs.getString("dni"),
                    rs.getString("telefono") != null ? rs.getString("telefono") : "",
                    rs.getString("email") != null ? rs.getString("email") : "",
                    rs.getString("direccion") != null ? rs.getString("direccion") : ""));
            }
            tablaClientes.setItems(data);
            rs.close(); stmt.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private TableColumn<ObservableList<String>, String> getColCliente(int i) {
        return switch (i) {
            case 0 -> colClienteNombre; case 1 -> colClienteApellido; case 2 -> colClienteDNI;
            case 3 -> colClienteTelefono; case 4 -> colClienteEmail; case 5 -> colClienteDireccion;
            default -> null;
        };
    }

    @FXML private void filtrarClientes() { cargarClientes(); }

    private void cargarStock() {
        try {
            Connection conn = ConexionDB.getConexion();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT codigo, nombre, categoria, precio_venta, stock_actual, stock_minimo " +
                "FROM productos WHERE activo=1");

            ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
            for (int i = 0; i < 6; i++) configurarColumna(getColStock(i), i);
            while (rs.next()) {
                data.add(FXCollections.observableArrayList(
                    rs.getString("codigo"), rs.getString("nombre"),
                    rs.getString("categoria") != null ? rs.getString("categoria") : "",
                    "$" + String.format("%.2f", rs.getDouble("precio_venta")),
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
            case 3 -> colStockPrecio; case 4 -> colStockActual; case 5 -> colStockMinimo;
            default -> null;
        };
    }

    @FXML private void filtrarProductosStock() { cargarStock(); }

    private void cargarMisFacturas() {
        try {
            Connection conn = ConexionDB.getConexion();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT f.numero_factura, f.fecha, f.hora, " +
                "COALESCE(CONCAT(c.nombre, ' ', c.apellido), 'Consumidor Final') as cliente, " +
                "f.total, f.estado " +
                "FROM facturas f " +
                "LEFT JOIN clientes c ON f.cliente_id = c.id " +
                "WHERE f.usuario_id = ? " +
                "ORDER BY f.id DESC");
            stmt.setInt(1, usuarioId);
            ResultSet rs = stmt.executeQuery();

            ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
            for (int i = 0; i < 6; i++) configurarColumna(getColMF(i), i);
            while (rs.next()) {
                data.add(FXCollections.observableArrayList(
                    rs.getString("numero_factura"), rs.getString("fecha"),
                    rs.getString("hora") != null ? rs.getString("hora").substring(0, 5) : "",
                    rs.getString("cliente"),
                    "$" + String.format("%.2f", rs.getDouble("total")),
                    rs.getString("estado")));
            }
            tablaMisFacturas.setItems(data);
            rs.close(); stmt.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private TableColumn<ObservableList<String>, String> getColMF(int i) {
        return switch (i) {
            case 0 -> colMFNumero; case 1 -> colMFFecha; case 2 -> colMFHora;
            case 3 -> colMFCliente; case 4 -> colMFTotal; case 5 -> colMFEstado;
            default -> null;
        };
    }

    @FXML private void filtrarMisFacturas() { cargarMisFacturas(); }
    @FXML private void handleExportarPDF() {
        Validaciones.mostrarInfo("Exportar PDF", "Funcionalidad de exportacion a PDF disponible.");
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
            if (conn == null) { System.err.println("[cargarProductosFactura] conn is null"); return; }
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT id, codigo, nombre, precio_venta, stock_actual, descuento FROM productos WHERE activo=1 AND stock_actual > 0");
            int count = 0;
            while (rs.next()) {
                double desc = rs.getDouble("descuento");
                String info = rs.getString("codigo") + " - " + rs.getString("nombre") +
                    " [$" + String.format("%.2f", rs.getDouble("precio_venta")) + "] Stock: " + rs.getInt("stock_actual") +
                    (desc > 0 ? " (Descuento: " + String.format("%.0f", desc) + "%)" : "");
                productosFactura.add(new String[]{
                    String.valueOf(rs.getInt("id")), rs.getString("codigo"),
                    rs.getString("nombre"), String.format("%.2f", rs.getDouble("precio_venta")),
                    String.valueOf(rs.getInt("stock_actual")), String.valueOf(desc), info
                });
                cmbProductoFactura.getItems().add(info);
                count++;
            }
            System.out.println("[cargarProductosFactura] Cargados " + count + " productos, combo items: " + cmbProductoFactura.getItems().size());
            rs.close(); stmt.close();
        } catch (Exception e) { System.err.println("[cargarProductosFactura] Error: " + e.getMessage()); e.printStackTrace(); }
    }

    @FXML private void buscarProductoFactura() {
        String filtro = txtBuscarProductoFactura.getText().toLowerCase();
        cmbProductoFactura.getItems().clear();
        for (String[] p : productosFactura) {
            if (p[6].toLowerCase().contains(filtro)) cmbProductoFactura.getItems().add(p[6]);
        }
    }

    @FXML private void agregarProductoFactura() {
        int idxProd = cmbProductoFactura.getSelectionModel().getSelectedIndex();
        if (idxProd < 0) { Validaciones.mostrarError("Seleccionar", "Seleccione un producto"); return; }

        String[] producto = productosFactura.get(idxProd);
        int cantidad = 1;
        try { cantidad = Integer.parseInt(txtCantidadFactura.getText()); } catch (Exception e) {}

        if (cantidad < 1) {
            Validaciones.mostrarError("Cantidad invalida", "Ingrese una cantidad mayor a 0");
            return;
        }

        if (cantidad > Integer.parseInt(producto[4])) {
            Validaciones.mostrarError("Stock insuficiente", "Stock disponible: " + producto[4]);
            return;
        }

        for (String[] det : detalleFactura) {
            if (det[1].equals(producto[1])) {
                Validaciones.mostrarError("Duplicado", "Producto ya agregado");
                return;
            }
        }

        double precio = Double.parseDouble(producto[3]);
        double descuento = Double.parseDouble(producto[5]);
        double subtotal = cantidad * precio * (1 - descuento / 100);

        detalleFactura.add(new String[]{producto[0], producto[1], producto[2],
            String.valueOf(cantidad), String.format("%.2f", precio), String.valueOf(descuento), String.format("%.2f", subtotal)});

        actualizarTablaDetalle();
        actualizarTotales();
    }

    private void actualizarTablaDetalle() {
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        for (String[] det : detalleFactura) {
            data.add(FXCollections.observableArrayList(det[1], det[2], det[3], det[4], det[5], det[6]));
        }
        for (int i = 0; i < 6; i++) configurarColumna(getColDet(i), i);
        tablaDetalleFactura.setItems(data);
    }

    private TableColumn<ObservableList<String>, String> getColDet(int i) {
        return switch (i) {
            case 0 -> colDetCodigo; case 1 -> colDetNombre; case 2 -> colDetCantidad;
            case 3 -> colDetPrecio; case 4 -> colDetDescuento; case 5 -> colDetSubtotal;
            default -> null;
        };
    }

    private void actualizarTotales() {
        double subtotal = 0;
        for (String[] det : detalleFactura) subtotal += Double.parseDouble(det[6]);

        double descGlobalPorc = 0;
        String descSel = cmbDescuentoGlobal.getValue();
        if (descSel != null && !descSel.equals("0%")) {
            if (descSel.equals("Personalizado")) {
                TextInputDialog d = new TextInputDialog("0");
                d.setTitle("Descuento"); d.setContentText("Porcentaje:");
                try { descGlobalPorc = Double.parseDouble(d.showAndWait().orElse("0")); } catch (Exception ex) {}
            } else {
                descGlobalPorc = Double.parseDouble(descSel.replace("%", ""));
            }
        }

        double descMonto = subtotal * (descGlobalPorc / 100.0);
        double baseImp = subtotal - descMonto;
        double iva = baseImp * obtenerPorcentajeIVA();
        double total = baseImp + iva;

        lblSubtotal.setText("$" + String.format("%.2f", subtotal));
        lblIVA.setText("$" + String.format("%.2f", iva));
        lblTotal.setText("$" + String.format("%.2f", total));
    }

    @FXML private void eliminarProductoFactura() {
        int idx = tablaDetalleFactura.getSelectionModel().getSelectedIndex();
        if (idx < 0) { Validaciones.mostrarError("Seleccionar", "Seleccione un producto"); return; }
        String codigo = tablaDetalleFactura.getItems().get(idx).get(0);
        detalleFactura.removeIf(d -> d[1].equals(codigo));
        actualizarTablaDetalle();
        actualizarTotales();
    }

    @FXML private void handleEmitirFactura() {
        if (detalleFactura.isEmpty()) {
            Validaciones.mostrarError("Sin productos", "Agregue productos a la factura");
            return;
        }

        int clienteId = 0;
        int idxCli = cmbClienteFactura.getSelectionModel().getSelectedIndex();
        if (idxCli >= 0) clienteId = Integer.parseInt(clientesFactura.get(idxCli)[0]);

        try {
            Connection conn = ConexionDB.getConexion();
            conn.setAutoCommit(false);

            try {
                String numeroFactura = generarNumeroFactura(conn);

                double subtotal = 0;
                for (String[] det : detalleFactura) subtotal += Double.parseDouble(det[6]);

                double descGlobalPorc = 0;
                String descSel = cmbDescuentoGlobal.getValue();
                if (descSel != null && !descSel.equals("0%")) {
                    if (!descSel.equals("Personalizado"))
                        descGlobalPorc = Double.parseDouble(descSel.replace("%", ""));
                }
                double descMonto = subtotal * (descGlobalPorc / 100.0);
                double baseImp = subtotal - descMonto;
                double iva = baseImp * obtenerPorcentajeIVA();
                double total = baseImp + iva;

                PreparedStatement stmtFact = conn.prepareStatement(
                    "INSERT INTO facturas (numero_factura, fecha, hora, usuario_id, cliente_id, subtotal, descuento_global, iva, total) VALUES (?,?,?,?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
                stmtFact.setString(1, numeroFactura);
                stmtFact.setDate(2, Date.valueOf(LocalDate.now()));
                stmtFact.setTime(3, Time.valueOf(LocalTime.now()));
                stmtFact.setInt(4, usuarioId);
                stmtFact.setInt(5, clienteId > 0 ? clienteId : 0);
                stmtFact.setDouble(6, subtotal);
                stmtFact.setDouble(7, descMonto);
                stmtFact.setDouble(8, iva);
                stmtFact.setDouble(9, total);
                stmtFact.executeUpdate();

                ResultSet rsFact = stmtFact.getGeneratedKeys();
                int facturaId = 0;
                if (rsFact.next()) facturaId = rsFact.getInt(1);
                rsFact.close();
                stmtFact.close();

                PreparedStatement stmtDet = conn.prepareStatement(
                    "INSERT INTO detalle_factura (factura_id, producto_id, cantidad, precio_unitario, descuento_porcentaje, descuento_monto, subtotal_linea) VALUES (?,?,?,?,?,?,?)");
                PreparedStatement stmtStock = conn.prepareStatement(
                    "UPDATE productos SET stock_actual = stock_actual - ? WHERE id = ?");
                PreparedStatement stmtAudit = conn.prepareStatement(
                    "INSERT INTO auditoria (usuario_id, accion, tabla_afectada, registro_id, detalle) VALUES (?,?,?,?,?)");

                for (String[] det : detalleFactura) {
                    int prodId = Integer.parseInt(det[0]);
                    int cant = Integer.parseInt(det[3]);
                    double precio = Double.parseDouble(det[4]);
                    double descuento = Double.parseDouble(det[5]);
                    double sub = Double.parseDouble(det[6]);
                    double descuentoMonto = (cant * precio) * (descuento / 100);

                    stmtDet.setInt(1, facturaId);
                    stmtDet.setInt(2, prodId);
                    stmtDet.setInt(3, cant);
                    stmtDet.setDouble(4, precio);
                    stmtDet.setDouble(5, descuento);
                    stmtDet.setDouble(6, descuentoMonto);
                    stmtDet.setDouble(7, sub);
                    stmtDet.addBatch();

                    stmtStock.setInt(1, cant);
                    stmtStock.setInt(2, prodId);
                    stmtStock.addBatch();
                }
                stmtDet.executeBatch();
                stmtStock.executeBatch();

                stmtAudit.setInt(1, usuarioId);
                stmtAudit.setString(2, "EMITIR FACTURA");
                stmtAudit.setString(3, "facturas");
                stmtAudit.setInt(4, facturaId);
                stmtAudit.setString(5, "Factura " + numeroFactura + " emitida");
                stmtAudit.executeUpdate();

                stmtDet.close(); stmtStock.close(); stmtAudit.close();

                conn.commit();

                Validaciones.mostrarInfo("Factura Emitida",
                    "Factura " + numeroFactura + " emitida\nTotal: $" + String.format("%.2f", total));

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
            Validaciones.mostrarError("Error", "No se pudo emitir: " + e.getMessage());
        }
    }

    private String generarNumeroFactura(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) + 1 FROM facturas");
        rs.next();
        int count = rs.getInt(1);
        rs.close(); stmt.close();
        return "FAC-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + "-" + String.format("%04d", count);
    }
}