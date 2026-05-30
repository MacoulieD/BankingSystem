package bankingsystem.view;

import bankingsystem.domain.Person;
import bankingsystem.services.input.LoginService;
import bankingsystem.services.outputport.PersonaPersistencePort;
import bankingsystem.utils.FormValidation;

public class LoginView {
    private final LoginService loginService;
    private final PersonaPersistencePort personRepository;

    public LoginView(LoginService loginService, PersonaPersistencePort personRepository) {
        this.loginService = loginService;
        this.personRepository = personRepository;
    }

    public Person executeLogin() {
        System.out.println("\n--- ACCESO AL SISTEMA ---");
        String username = FormValidation.validateString("Usuario: ");

        if (!doesUserExist(username)) {
            return null;
        }

        while (true) {
            if (isUserBlocked(username)) {
                return null;
            }

            String pass = FormValidation.validateString("Contraseña: ");
            Person loggedInUser = loginService.login(username, pass);

            if (loggedInUser != null) {
                System.out.println("🎉 ¡Inicio de sesión exitoso! Bienvenido.");
                return loggedInUser;
            }

            if (isUserBlocked(username)) {
                return null;
            }

            System.out.println("\n❌ Credenciales incorrectas.");
            System.out.println("--- Reintente su contraseña ---");
        }
    }

    public boolean doesUserExist(String username) {
        Person person = personRepository.findPersonaByUsername(username);

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
        Person person = personRepository.findPersonaByUsername(username);
        if (person != null && person.isAccountBlocked()) {
            System.out.println("🔒 Esta cuenta se encuentra bloqueada temporalmente hasta: " + person.getBlockedUntil());
            return true;
        }
        return false;
    }
}