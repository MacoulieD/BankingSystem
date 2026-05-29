package bankingsystem.view;

import bankingsystem.domain.Person;
import bankingsystem.services.input.LoginService;
import bankingsystem.Persistence.repository.PersonRepository;
import bankingsystem.utils.FormValidation;

public class LoginView {
    private final LoginService loginService;
    private final PersonRepository personRepository;

    public LoginView(LoginService loginService, PersonRepository personRepository) {
        this.loginService = loginService;
        this.personRepository = personRepository;
    }

    public Person executeLogin() {
        System.out.println("\n--- ACCESO AL SISTEMA ---");
        String username = FormValidation.validateString("Usuario: ");

        // 1. Verificación básica: ¿Existe el usuario en la BD?
        if (!doesUserExist(username)) {
            return null;
        }

        // 2. Bucle para validar la contraseña e iniciar sesión
        while (true) {
            if (isUserBlocked(username)) {
                return null;
            }

            String pass = FormValidation.validateString("Contraseña: ");

            // Dejamos que el servicio haga la validación y maneje el conteo de intentos internamente
            Person loggedInUser = loginService.login(username, pass);

            if (loggedInUser != null) {
                System.out.println("🎉 ¡Inicio de sesión exitoso! Bienvenido.");
                return loggedInUser;
            }

            // Si el servicio retornó null, verificamos si esa última contraseña incorrecta lo bloqueó
            if (isUserBlocked(username)) {
                return null;
            }

            System.out.println("\n❌ Credenciales incorrectas.");
            System.out.println("--- Reintente su contraseña ---");
        }
    }

    // 🛠️ CORREGIDO: Este método AHORA SOLO VERIFICA si existe, no pide contraseñas de más
    public boolean doesUserExist(String username) {
        Person person = personRepository.findByUsername(username);

        if (person == null) {
            System.out.println("❌ Error: El nombre de usuario no está registrado.");
            return false;
        }

        if (person.isAccountBlocked()) {
            System.out.println("🔒 Cuenta bloqueada temporalmente. Intente de nuevo después de: " + person.getBlockedUntil());
            return false;
        }

        return true;
    }

    private boolean isUserBlocked(String username) {
        Person person = personRepository.findByUsername(username);
        if (person != null && person.isAccountBlocked()) {
            System.out.println("🔒 Esta cuenta se encuentra bloqueada temporalmente hasta: " + person.getBlockedUntil());
            return true;
        }
        return false;
    }
}