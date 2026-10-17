package com.Cesde.BankingSystem.Modelo;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "cuenta")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "tipocuenta", length = 15)
public abstract class MCuenta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(length = 11,nullable = false)
    private int id;
    @Column(length = 50,nullable = false,unique = true)
    private String numerocuenta;
    @Column(length = 50,nullable = false)
    private String propietario;
    @Column(length = 15,nullable = false)
    private Double saldo;
    @Column(nullable = false)
    private Boolean estado;
    @Column(nullable = false)
    private LocalDateTime createdat;

    // Relación entre las tablas cuenta y la tabla Mmovimiento
    @OneToMany(mappedBy = "cuenta")
    @JsonManagedReference
    private List<MMovimiento> movimientos;

    //Relación entre las tablas cuenta y la tabla MTarjetaCredito
    @OneToMany(mappedBy = "cuenta")
    @JsonManagedReference
    private List<MTarjetaCredito> tarjetaCreditos;

    //Relación entre las tablas cuenta y la tabla MPersona
    @ManyToOne
    @JoinColumn(name = "pkpersona",referencedColumnName = "idperson")
    @JsonBackReference
    private MPerson persona;

    //Constructores
    //Lleno

    public MCuenta(int id, String numerocuenta, String propietario, Double saldo, Boolean estado, LocalDateTime createdat) {
        this.id = id;
        this.numerocuenta = numerocuenta;
        this.propietario = propietario;
        this.saldo = saldo;
        this.estado = estado;
        this.createdat = createdat;
    }


    //Vacio

    public MCuenta() {
    }

    //Emcapsulamiento


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNumerocuenta() {
        return numerocuenta;
    }

    public void setNumerocuenta(String numerocuenta) {
        this.numerocuenta = numerocuenta;
    }

    public String getPropietario() {
        return propietario;
    }

    public void setPropietario(String propietario) {
        this.propietario = propietario;
    }

    public Double getSaldo() {
        return saldo;
    }

    public void setSaldo(Double saldo) {
        this.saldo = saldo;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }

    public LocalDateTime getCreatedat() {
        return createdat;
    }

    public void setCreatedat(LocalDateTime createdat) {
        this.createdat = createdat;
    }

    public MPerson getPersona() {
        return persona;
    }

    public void setPersona(MPerson persona) {
        this.persona = persona;
    }

    //Encapsulamiento de las relaciones

    public List<MMovimiento> getMovimientos() {
        return movimientos;
    }

    public void setMovimientos(List<MMovimiento> movimientos) {
        this.movimientos = movimientos;
    }

    public List<MTarjetaCredito> getTarjetaCreditos() {
        return tarjetaCreditos;
    }

    public void setTarjetaCreditos(List<MTarjetaCredito> tarjetaCreditos) {
        this.tarjetaCreditos = tarjetaCreditos;
    }
}
