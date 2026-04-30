package bankingsystem.services;

import bankingsystem.domain.Person;
import bankingsystem.repository.PersonRepository;
import java.time.LocalDateTime;

public class LoginServiceImpl implements LoginService {

    private final PersonRepository personRepository;
    private static final int MAX_ATTEMPTS = 3;


    public LoginServiceImpl(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @Override
    public Person login(String username, String password) {

        Person person = personRepository.findByUsername(username);


        if (person == null) {
            System.out.println("❌ Error: El nombre de usuario no existe.");
            return null;
        }


        if (person.isAccountBlocked()) {
            System.out.println("🚨 CUENTA BLOQUEADA.");
            System.out.println("Podrá acceder después de: " + person.getBlockedUntil());
            return null;
        }


        if (person.getPassword().equals(password)) {

            person.setFailedLoginAttempts(0);
            person.setBlockedUntil(null);
            return person;
        } else {

            int attempts = person.getFailedLoginAttempts() + 1;
            person.setFailedLoginAttempts(attempts);

            System.out.println("❌ Contraseña incorrecta.");
            System.out.println("⚠️ Intento " + attempts + " de " + MAX_ATTEMPTS);


            if (attempts >= MAX_ATTEMPTS) {
                person.setBlockedUntil(LocalDateTime.now().plusHours(24));
                System.out.println("🚨 Has superado el límite de intentos.");
                System.out.println("Tu cuenta ha sido bloqueada por 24 horas.");
            }
            return null;
        }
    }
}