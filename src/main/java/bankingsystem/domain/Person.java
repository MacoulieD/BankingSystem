package bankingsystem.domain;

import java.time.LocalDateTime;

public class Person {
    private int id;
    private String name;
    private int telephone;
    private String email;
    private String username;
    private double initialBalance;
    private String password;

    // NUEVOS ATRIBUTOS PARA SEGURIDAD (CRITERIOS DE LOGIN)
    private int failedLoginAttempts;
    private LocalDateTime blockedUntil;

    public Person() {
        this.failedLoginAttempts = 0;
        this.blockedUntil = null;
    }

    public Person(int id, String name, int telephone, String email, String username, double initialBalance, String password) {
        this.id = id;
        this.name = name;
        this.telephone = telephone;
        this.email = email;
        this.username = username;
        this.initialBalance = initialBalance;
        this.password = password;
        this.failedLoginAttempts = 0; // Inicia en 0
        this.blockedUntil = null;     // Inicia sin bloqueo
    }

    // Getters y Setters existentes
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getTelephone() { return telephone; }
    public void setTelephone(int telephone) { this.telephone = telephone; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public double getInitialBalance() { return initialBalance; }
    public void setInitialBalance(double initialBalance) { this.initialBalance = initialBalance; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

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