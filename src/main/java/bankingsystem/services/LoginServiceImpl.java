package bankingsystem.services;

import bankingsystem.domain.Person;
import bankingsystem.repository.PersonRepository;
import java.time.LocalDateTime;

public class LoginServiceImpl implements LoginService {

    private final PersonRepository personRepository;
    private static final int MAX_ATTEMPTS = 3;

    // Inyección de dependencia igual que en PersonServiceImpl
    public LoginServiceImpl(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @Override
    public Person login(String username, String password) {
        // Buscamos a la persona en el repositorio por su usuario
        Person person = personRepository.findByUsername(username);

        // Criterio: Validación de existencia
        if (person == null) {
            System.out.println("❌ Error: El nombre de usuario no existe.");
            return null;
        }

        // Criterio: Alerta de cuenta bloqueada (Valida si ya pasaron las 24h)
        if (person.isAccountBlocked()) {
            System.out.println("🚨 CUENTA BLOQUEADA.");
            System.out.println("Podrá acceder después de: " + person.getBlockedUntil());
            return null;
        }

        // Criterio: Validación de contraseña
        if (person.getPassword().equals(password)) {
            // Éxito: Reiniciamos contadores
            person.setFailedLoginAttempts(0);
            person.setBlockedUntil(null);
            return person;
        } else {
            // Fallo: Aumentar contador y mostrar intentos (Criterios de aceptación)
            int attempts = person.getFailedLoginAttempts() + 1;
            person.setFailedLoginAttempts(attempts);

            System.out.println("❌ Contraseña incorrecta.");
            System.out.println("⚠️ Intento " + attempts + " de " + MAX_ATTEMPTS);

            // Criterio: Bloqueo al tercer intento por 24 horas
            if (attempts >= MAX_ATTEMPTS) {
                person.setBlockedUntil(LocalDateTime.now().plusHours(24));
                System.out.println("🚨 Has superado el límite de intentos.");
                System.out.println("Tu cuenta ha sido bloqueada por 24 horas.");
            }
            return null;
        }
    }
}