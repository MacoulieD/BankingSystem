package bankingsystem.domain;

import java.time.LocalDateTime;

public class Person {


    private int id;
    private String name;
    private String telephone;
    private String email;
    private String username;
    private double initialBalance;
    private String password;
    private String confirmPassword;

    // NUEVOS ATRIBUTOS PARA SEGURIDAD (CRITERIOS DE LOGIN)
    private int failedLoginAttempts;
    private LocalDateTime blockedUntil;

    public Person(int id, String name, String telephone, String email, String username, double initialBalance,String password, String confirmPassword, int failedLoginAttempts, LocalDateTime blockedUntil) {
        this.id = id;
        this.name = name;
        this.telephone = telephone;
        this.email = email;
        this.username = username;
        this.initialBalance = initialBalance;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.failedLoginAttempts = failedLoginAttempts;
        this.blockedUntil = blockedUntil;
    }

    public Person() {

    }


    // Getters y Setters existentes

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public double getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(double initialBalance) {
        this.initialBalance = initialBalance;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    // NUEVOS GETTERS Y SETTERS PARA SEGURIDAD
    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public LocalDateTime getBlockedUntil() {
        return blockedUntil;
    }

    public void setBlockedUntil(LocalDateTime blockedUntil) {
        this.blockedUntil = blockedUntil;
    }

    public boolean isAccountBlocked() {
        if (blockedUntil == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(blockedUntil);
    }

}