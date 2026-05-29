package bankingsystem.services;

import bankingsystem.domain.Person;
import bankingsystem.Persistence.repository.PersonRepository;
import bankingsystem.services.input.LoginService;

import java.time.LocalDateTime;

public class LoginServiceImpl implements LoginService {

    private final PersonRepository personRepository;
    private static final int MAX_ATTEMPTS = 3;


    public LoginServiceImpl(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @Override
    public Person login(String username, String password) {
        // 1. Buscamos al usuario usando el método unificado del repositorio
        Person person = personRepository.findPersonaByUsername(username);

        // 2. Validación: ¿El usuario existe?
        if (person == null) {
            System.out.println("❌ Error: El nombre de usuario no existe en el sistema.");
            return null;
        }

        // 3. Validación: ¿La cuenta está bloqueada por tiempo?
        if (person.isAccountBlocked()) {
            System.out.println("🚨 CUENTA BLOQUEADA TEMPORALMENTE.");
            System.out.println("🔒 Podrá acceder después de: " + person.getBlockedUntil());
            return null;
        }

        // 4. Validación: ¿La contraseña es correcta?
        if (person.getPassword().equals(password)) {
            // Si venía de tener intentos fallidos, reiniciamos su historial a 0
            if (person.getFailedLoginAttempts() > 0) {
                person.setFailedLoginAttempts(0);
                person.setBlockedUntil(null);
                personRepository.updatePerson(person); // 💾 Guarda el reinicio en MySQL
            }
            return person; // Login exitoso
        }

        // 5. Flujo de contraseña incorrecta
        else {
            int attempts = person.getFailedLoginAttempts() + 1;
            person.setFailedLoginAttempts(attempts);

            System.out.println("\n❌ Contraseña incorrecta.");

            // Si llega al límite, disparamos el bloqueo por 24 horas
            if (attempts >= MAX_ATTEMPTS) {
                person.setBlockedUntil(LocalDateTime.now().plusHours(24));
                System.out.println("🚨 Has superado el límite de intentos permitidos.");
                System.out.println("🔒 Tu cuenta ha sido bloqueada por las próximas 24 horas.");
            } else {
                System.out.println("⚠️ Intentos fallidos registrados: " + attempts + " de " + MAX_ATTEMPTS);
            }

            // 100% Necesario: Guardamos los intentos o el nuevo bloqueo en la Base de Datos
            personRepository.updatePerson(person);

            return null;
        }
    }
}