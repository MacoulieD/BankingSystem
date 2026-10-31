package com.Cesde.BankingSystem.Modelo;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;

// Historial de la tarjeta de credito: compras a cuotas (card-purchase) y
// pagos (card-payment). Los campos capital, cuotas, valorcuota y
// saldopendiente solo se llenan en las compras.
@Entity
@Table(name = "movimiento_tarjeta")
public class MMovimientoTarjeta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(length = 11,nullable = false)
    private int id;
    @Column(length = 20,nullable = false)
    private String tipo;
    @Column(length = 255,nullable = false)
    private String descripcion;
    @Column(nullable = false)
    private Double monto;
    @Column(nullable = false)
    private Double deudaposterior;
    @Column(nullable = false)
    private LocalDateTime fecha;
    // valor de la compra sin intereses
    private Double capital;
    private Integer cuotas;
    private Double valorcuota;
    // lo que falta pagar de esta compra
    private Double saldopendiente;

    //Relacion entre las tablas movimiento_tarjeta y targeta_credito
    @ManyToOne
    @JoinColumn(name = "pktarjeta",referencedColumnName = "id",nullable = false)
    @JsonBackReference
    private MTarjetaCredito tarjeta;

    //Constructores
    //Vacio
    public MMovimientoTarjeta() {
    }

    //Emcapsulamiento

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Double getMonto() {
        return monto;
    }

    public void setMonto(Double monto) {
        this.monto = monto;
    }

    public Double getDeudaposterior() {
        return deudaposterior;
    }

    public void setDeudaposterior(Double deudaposterior) {
        this.deudaposterior = deudaposterior;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Double getCapital() {
        return capital;
    }

    public void setCapital(Double capital) {
        this.capital = capital;
    }

    public Integer getCuotas() {
        return cuotas;
    }

    public void setCuotas(Integer cuotas) {
        this.cuotas = cuotas;
    }

    public Double getValorcuota() {
        return valorcuota;
    }

    public void setValorcuota(Double valorcuota) {
        this.valorcuota = valorcuota;
    }

    public Double getSaldopendiente() {
        return saldopendiente;
    }

    public void setSaldopendiente(Double saldopendiente) {
        this.saldopendiente = saldopendiente;
    }

    //Encapsulamiento de las relaciones

    public MTarjetaCredito getTarjeta() {
        return tarjeta;
    }

    public void setTarjeta(MTarjetaCredito tarjeta) {
        this.tarjeta = tarjeta;
    }
}
