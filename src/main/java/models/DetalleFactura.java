package models;

public class DetalleFactura {
    private int id;
    private int facturaId;
    private int productoId;
    private String nombreProducto;
    private String codigoProducto;
    private int cantidad;
    private double precioUnitario;
    private double descuentoPorcentaje;
    private double descuentoMonto;
    private double subtotalLinea;

    public DetalleFactura() {}

    public DetalleFactura(int id, int facturaId, int productoId, String nombreProducto,
                          String codigoProducto, int cantidad, double precioUnitario,
                          double descuentoPorcentaje, double descuentoMonto, double subtotalLinea) {
        this.id = id;
        this.facturaId = facturaId;
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.codigoProducto = codigoProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.descuentoPorcentaje = descuentoPorcentaje;
        this.descuentoMonto = descuentoMonto;
        this.subtotalLinea = subtotalLinea;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getFacturaId() { return facturaId; }
    public void setFacturaId(int facturaId) { this.facturaId = facturaId; }

    public int getProductoId() { return productoId; }
    public void setProductoId(int productoId) { this.productoId = productoId; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public String getCodigoProducto() { return codigoProducto; }
    public void setCodigoProducto(String codigoProducto) { this.codigoProducto = codigoProducto; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }

    public double getDescuentoPorcentaje() { return descuentoPorcentaje; }
    public void setDescuentoPorcentaje(double descuentoPorcentaje) { this.descuentoPorcentaje = descuentoPorcentaje; }

    public double getDescuentoMonto() { return descuentoMonto; }
    public void setDescuentoMonto(double descuentoMonto) { this.descuentoMonto = descuentoMonto; }

    public double getSubtotalLinea() { return subtotalLinea; }
    public void setSubtotalLinea(double subtotalLinea) { this.subtotalLinea = subtotalLinea; }

    public double getSubtotalSinDescuento() {
        return cantidad * precioUnitario;
    }

    public double getTotalConDescuento() {
        return subtotalLinea;
    }
}