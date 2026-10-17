package com.Cesde.BankingSystem.Modelo;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "person")
public  class MPerson{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(length = 11,nullable = false)
    private int idperson;
    // CC = Cedula de ciudadania, PA = Pasaporte (mismos valores del formulario de registro)
    @Column(length = 2,nullable = false)
    private String tipodocumento;
    // varchar y no int: una cedula de 10 digitos no cabe en un int y el pasaporte lleva letras
    @Column(length = 10,nullable = false,unique = true)
    private String documento;
    @Column(length = 60,nullable = false)
    private String name;
    @Column(length = 20,nullable = false)
    private String  telephone;
    @Column(length = 50,unique = true)
    private String email;
    @Column(length = 20,nullable = false,unique = true)
    private String  username;
    // la contrasena se guarda tal cual (sin cifrar); el formulario permite hasta 50 caracteres
    @Column(length = 60,nullable = false)
    private String userpassword;
    @Column(nullable = false)
    private Boolean activo;
    @Column(length = 11,nullable = false)
    private int failedAttempts;

    //Relacion entre las tablas persona y cuenta
    @OneToMany(mappedBy = "persona")
    @JsonManagedReference
    private List<MCuenta> cuentas;


    //Constructores
    //Lleno
    public MPerson(int idperson, String tipodocumento, String documento, String name, String telephone, String email, String username, String userpassword, Boolean activo, int failedAttempts) {
        this.idperson = idperson;
        this.tipodocumento = tipodocumento;
        this.documento = documento;
        this.name = name;
        this.telephone = telephone;
        this.email = email;
        this.username = username;
        this.userpassword = userpassword;
        this.activo = activo;
        this.failedAttempts = failedAttempts;
    }

    //Vacio
    public MPerson() {
    }

    //Encapsulamiento
    public String getTipodocumento() {
        return tipodocumento;
    }

    public void setTipodocumento(String tipodocumento) {
        this.tipodocumento = tipodocumento;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public int getIdperson() {
        return idperson;
    }

    public void setIdperson(int idperson) {
        this.idperson = idperson;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getUserpassword() {
        return userpassword;
    }

    public void setUserpassword(String userpassword) {
        this.userpassword = userpassword;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(int faled_attempts) {
        this.failedAttempts = faled_attempts;
    }
}

