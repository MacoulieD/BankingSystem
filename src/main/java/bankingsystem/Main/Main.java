package bankingsystem.Main;

// conectar con la clase Userregister y Login
import bankingsystem.Userregister;
import bankingsystem.Login;


public class Main {
    public static void main(String[] args) {

        // Crear un nuevo usuario
        Userregister user = new Userregister();
        user.createUser(user);

        //Iniciar sesión con el usuario creado
        Login userLogin = new Login();
        userLogin.validateLogin(userLogin.login(), user);
    }

}
