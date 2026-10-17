package com.Cesde.BankingSystem.Modelo;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name="cuenta_corriente")
@DiscriminatorValue("CORRIENTE")
public class MCuentaCorriente extends MCuenta {
    @Column(length = 15,nullable = false)
    private Double sobregiro;

    //Conctructores
    //Lleno
    public MCuentaCorriente(int id, String numerocuenta, String propietario, Double saldo, Boolean estado, LocalDateTime createdat, Double sobregiro) {
        super(id, numerocuenta, propietario, saldo, estado, createdat);
        this.sobregiro = sobregiro;
    }

    //Vacio
    public MCuentaCorriente() {
        super();
    }

    //Emcapsulamiento

    public Double getSobregiro() {
        return sobregiro;
    }

    public void setSobregiro(Double sobregiro) {
        this.sobregiro = sobregiro;
    }
}
