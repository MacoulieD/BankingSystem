package bankingsystem;

import java.util.Scanner;

public class Login {

     // Atributos de la clase Login
    private String username;
    private String password;

    // Constructor de la clase Login
    public Login(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Constructor sin argumentos
    public Login() {

    }

    // Getters y Setters de la clase Login
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    // Método para solicitar el nombre de usuario y la contraseña al usuario
    Scanner sc = new Scanner(System.in);
    public Login login() {
        System.out.println("¡Bienvenido al inicio de Sistema Bancario Mi plata!");
        System.out.println("Ingrese su nombre de usuario");
        String username = sc.nextLine();
        System.out.println("Ingrese su contraseña");
        String password = sc.nextLine();
        return new Login(username, password);
    }
    // Método para validar el inicio de sesión. si el nombre de usuario y la contraseña coinciden con los datos almacenados, dar mensaje de bienvenida, de lo contrario, mostrar un mensaje de error. Volver a solicitar el nombre de usuario y la contraseña hasta que el usuario ingrese los datos correctos.

    public void validateLogin(Login login, Userregister user) {
        while (!login.username.equals(user.getUsername()) || !login.password.equals(user.getPassword())) {
            System.out.println("Nombre de usuario o contraseña incorrectos. Por favor, intente de nuevo.");
            login = login();
        }
        System.out.println("Bienvenido " + user.getUsername() + "!");
    }






}


