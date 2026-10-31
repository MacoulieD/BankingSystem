package com.Cesde.BankingSystem.Modelo;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name="cuenta_ahorros")
@DiscriminatorValue("AHORRO")
public class MCuentaAhorro extends MCuenta {
    @Column(length = 5,nullable = false)
    private Double tasainteres;

    //Contructores
    //Lleno
    public MCuentaAhorro(int id, String numerocuenta, String propietario, Double saldo, Boolean estado, LocalDateTime createdat, Double tasainteres) {
        super(id, numerocuenta, propietario, saldo, estado, createdat);
        this.tasainteres = tasainteres;
    }

    //Vacio
    public MCuentaAhorro() {
        super();
    }

    //Emcapsulamiento

    public Double getTasainteres() {
        return tasainteres;
    }

    public void setTasainteres(Double tasainteres) {
        this.tasainteres = tasainteres;
    }
}
