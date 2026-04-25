package bankingsystem.view;

import bankingsystem.domain.Person;
import bankingsystem.services.LoginService;
import bankingsystem.repository.PersonRepository;
import bankingsystem.utils.PersonFormValidation;

public class LoginView {
    private final LoginService loginService;
    private final PersonRepository personRepository;

    // CORRECCIÓN: Se agregó PersonRepository al parámetro del constructor
    public LoginView(LoginService loginService, PersonRepository personRepository) {
        this.loginService = loginService;
        this.personRepository = personRepository;
    }

    public Person executeLogin() {
        System.out.println("\n--- ACCESO AL SISTEMA ---");
        String username = PersonFormValidation.validateString("Usuario: ");

        // 1. Verificación inmediata de existencia
        if (!doesUserExist(username)) {
            System.out.println("❌ Error: El nombre de usuario no existe.");
            // Retornamos null para que MenuApp decida si registrar o no
            return null;
        }

        // 2. Bucle de intentos para la contraseña
        while (true) {
            if (isUserBlocked(username)) {
                // El mensaje de bloqueo ya lo da el Service, aquí solo salimos
                return null;
            }

            String pass = PersonFormValidation.validateString("Contraseña: ");
            Person loggedInUser = loginService.login(username, pass);

            if (loggedInUser != null) {
                return loggedInUser;
            }

            // Si se bloqueó tras el último intento fallido, salimos del bucle
            if (isUserBlocked(username)) {
                return null;
            }

            // Si no está bloqueado, permitimos reintentar la contraseña
            System.out.println("--- Reintente su contraseña ---");
        }
    }

    private boolean doesUserExist(String username) {
        return personRepository.existsByUsername(username);
    }

    private boolean isUserBlocked(String username) {
        Person person = personRepository.findByUsername(username);
        return person != null && person.isAccountBlocked();
    }
}