package bankingsystem.services;

import bankingsystem.domain.Person;
import bankingsystem.services.input.LoginService;
import bankingsystem.services.outputport.PersonaPersistencePort;

import java.time.LocalDateTime;

public class LoginServiceImpl implements LoginService {

    private final PersonaPersistencePort personRepository;
    private static final int MAX_ATTEMPTS = 3;

    public LoginServiceImpl(PersonaPersistencePort personRepository) {
        this.personRepository = personRepository;
    }

    @Override
    public Person login(String username, String password) {
        Person person = personRepository.findPersonaByUsername(username);

        if (person == null) {
            System.out.println("❌ Error: El nombre de usuario no existe en el sistema.");
            return null;
        }

        if (person.isAccountBlocked()) {
            System.out.println("🚨 CUENTA BLOQUEADA TEMPORALMENTE.");
            System.out.println("🔒 Podrá acceder después de: " + person.getBlockedUntil());
            return null;
        }

        if (person.getPassword().equals(password)) {
            if (person.getFailedLoginAttempts() > 0) {
                person.setFailedLoginAttempts(0);
                person.setBlockedUntil(null);
                personRepository.updatePerson(person);
            }
            return person;
        } else {
            int attempts = person.getFailedLoginAttempts() + 1;
            person.setFailedLoginAttempts(attempts);

            System.out.println("\n❌ Contraseña incorrecta.");

            if (attempts >= MAX_ATTEMPTS) {
                person.setBlockedUntil(LocalDateTime.now().plusHours(24));
                System.out.println("🚨 Has superado el límite de intentos permitidos.");
                System.out.println("🔒 Tu cuenta ha sido bloqueada por las próximas 24 horas.");
            } else {
                System.out.println("⚠️ Intentos fallidos registrados: " + attempts + " de " + MAX_ATTEMPTS);
            }

            personRepository.updatePerson(person);
            return null;
        }
    }
}