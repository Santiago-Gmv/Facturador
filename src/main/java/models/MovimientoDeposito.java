package models;

import java.time.LocalDate;
import java.time.LocalTime;

public class MovimientoDeposito {
    private int id;
    private LocalDate fecha;
    private LocalTime hora;
    private int usuarioId;
    private String nombreUsuario;
    private String tipoMovimiento;
    private int productoId;
    private String nombreProducto;
    private String codigoProducto;
    private int cantidad;
    private String observacion;

    public MovimientoDeposito() {}

    public MovimientoDeposito(int id, LocalDate fecha, LocalTime hora, int usuarioId,
                              String nombreUsuario, String tipoMovimiento, int productoId,
                              String nombreProducto, String codigoProducto, int cantidad,
                              String observacion) {
        this.id = id;
        this.fecha = fecha;
        this.hora = hora;
        this.usuarioId = usuarioId;
        this.nombreUsuario = nombreUsuario;
        this.tipoMovimiento = tipoMovimiento;
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.codigoProducto = codigoProducto;
        this.cantidad = cantidad;
        this.observacion = observacion;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }

    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getTipoMovimiento() { return tipoMovimiento; }
    public void setTipoMovimiento(String tipoMovimiento) { this.tipoMovimiento = tipoMovimiento; }

    public int getProductoId() { return productoId; }
    public void setProductoId(int productoId) { this.productoId = productoId; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public String getCodigoProducto() { return codigoProducto; }
    public void setCodigoProducto(String codigoProducto) { this.codigoProducto = codigoProducto; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
}