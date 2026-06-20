package models;

import java.time.LocalDate;
import java.time.LocalTime;

public class Factura {
    private int id;
    private String numeroFactura;
    private LocalDate fecha;
    private LocalTime hora;
    private int usuarioId;
    private String nombreVendedor;
    private int clienteId;
    private String nombreCliente;
    private double subtotal;
    private double descuentoGlobal;
    private double iva;
    private double total;
    private String estado;

    public Factura() {}

    public Factura(int id, String numeroFactura, LocalDate fecha, LocalTime hora,
                   int usuarioId, String nombreVendedor, int clienteId, String nombreCliente,
                   double subtotal, double descuentoGlobal, double iva, double total, String estado) {
        this.id = id;
        this.numeroFactura = numeroFactura;
        this.fecha = fecha;
        this.hora = hora;
        this.usuarioId = usuarioId;
        this.nombreVendedor = nombreVendedor;
        this.clienteId = clienteId;
        this.nombreCliente = nombreCliente;
        this.subtotal = subtotal;
        this.descuentoGlobal = descuentoGlobal;
        this.iva = iva;
        this.total = total;
        this.estado = estado;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNumeroFactura() { return numeroFactura; }
    public void setNumeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }

    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    public String getNombreVendedor() { return nombreVendedor; }
    public void setNombreVendedor(String nombreVendedor) { this.nombreVendedor = nombreVendedor; }

    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public double getDescuentoGlobal() { return descuentoGlobal; }
    public void setDescuentoGlobal(double descuentoGlobal) { this.descuentoGlobal = descuentoGlobal; }

    public double getIva() { return iva; }
    public void setIva(double iva) { this.iva = iva; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}