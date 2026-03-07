package bankingsystem;

import java.util.Scanner;

public class Userregister {

    // Atributos de la clase Userregister
    private int id;
    private String username;
    private String email;
    private double initialBalance;
    private String password;
    private String repeatPassword;

    // Constructor de la clase Userregister
    public Userregister(int id, String username, String email, double initialBalance, String password, String repeatPassword) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.initialBalance = initialBalance;
        this.password = password;
        this.repeatPassword = repeatPassword;
    }

    // clase sin argumento
    public Userregister() {

    }
    // Getters y Setters de la clase Userregister

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(double initialBalance) {
        this.initialBalance = initialBalance;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRepeatPassword() {
        return repeatPassword;
    }

    public void setRepeatPassword(String repeatPassword) {
        this.repeatPassword = repeatPassword;
    }

    // Método createUser() que recibe un objeto Userregister y devuelve el mismo objeto después de realizar la lógica de creación de usuario y validacion de contraseñas


    Scanner sc = new Scanner(System.in);

    public Userregister createUser(Userregister user) {
        System.out.println("Ingrese su ID: ");
        int id = sc.nextInt();
        sc.nextLine();
        user.id = id;
        System.out.println("Ingrese su nombre de usuario: ");
        String username = sc.nextLine();
        user.username = username;
        System.out.println("Ingrese su correo electrónico: ");
        String email = sc.nextLine();
        user.email = email;
        System.out.println("Ingrese su saldo inicial: ");
        double initialBalance = sc.nextDouble();
        sc.nextLine();
        user.initialBalance = initialBalance;
        System.out.println("Ingrese su contraseña: ");
        String password = sc.nextLine();
        user.password = password;
        System.out.println("Repita su contraseña: ");
        String repeatPassword = sc.nextLine();
        user.repeatPassword = repeatPassword;

        while (!user.password.equals(user.repeatPassword)) {
            System.out.println("Las contraseñas no coinciden. Por favor, ingrese nuevamente.");
            System.out.println("Ingrese su contraseña: ");
            user.password = sc.nextLine();
            System.out.println("Repita su contraseña: ");
            user.repeatPassword = sc.nextLine();
        }

        System.out.println("¡Usuario Creado con Exito!");

        return user;
    }
    //
}






