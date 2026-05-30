package bankingsystem.services;

import bankingsystem.domain.Cuenta;
import bankingsystem.domain.Person;
import bankingsystem.domain.TarjetaCredito;
import bankingsystem.Persistence.repository.CuentaRepository; // ✅ Importamos tu repositorio genérico
import bankingsystem.services.input.LoginService;
import bankingsystem.services.outputport.PersonaPersistencePort;
import bankingsystem.services.outputport.CuentaAhorrosPersistencePort; // ✅ Importamos los puertos necesarios
import bankingsystem.services.outputport.CuentaCorrientePersistencePort;
import bankingsystem.services.outputport.TarjetaCreditoPersistencePort;

import java.time.LocalDateTime;
import java.util.List;

public class LoginServiceImpl implements LoginService {

    private final PersonaPersistencePort personRepository;

    // ✅ NUEVOS ATRIBUTOS: Para jalar la información desde MySQL y sincronizar la lista genérica
    private final CuentaRepository cuentaRepository;
    private final CuentaAhorrosPersistencePort ahorrosPersistencePort;
    private final CuentaCorrientePersistencePort corrientePersistencePort;
    private final TarjetaCreditoPersistencePort tarjetaPersistencePort;

    private static final int MAX_ATTEMPTS = 3;

    // ✅ CONSTRUCTOR ACTUALIZADO: Inyectamos todas las dependencias requeridas
    public LoginServiceImpl(PersonaPersistencePort personRepository,
                            CuentaRepository cuentaRepository,
                            CuentaAhorrosPersistencePort ahorrosPersistencePort,
                            CuentaCorrientePersistencePort corrientePersistencePort,
                            TarjetaCreditoPersistencePort tarjetaPersistencePort) {
        this.personRepository = personRepository;
        this.cuentaRepository = cuentaRepository;
        this.ahorrosPersistencePort = ahorrosPersistencePort;
        this.corrientePersistencePort = corrientePersistencePort;
        this.tarjetaPersistencePort = tarjetaPersistencePort;
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

            // 🔄 ================== SINCRONIZACIÓN CON MYSQL ==================
            // 1. Limpiamos cualquier rastro viejo del usuario en memoria para evitar residuos duplicados
            cuentaRepository.findAllCuentas().removeIf(c -> c.getPropietario().equalsIgnoreCase(username));

            // 2. Cargamos la cuenta de ahorros real de MySQL a la RAM
            Cuenta ahorro = ahorrosPersistencePort.findByPropietario(username);
            if (ahorro != null) {
                cuentaRepository.saveCuenta(ahorro);
            }

            // 3. Cargamos la cuenta corriente real de MySQL a la RAM
            Cuenta corriente = corrientePersistencePort.findbypropietario(username); // Nota: Valida si en tu puerto se escribe findByPropietario o findbypropietario
            if (corriente != null) {
                cuentaRepository.saveCuenta(corriente);
            }

            // 4. Cargamos la tarjeta de crédito activa de MySQL a la RAM
            List<TarjetaCredito> tarjetas = tarjetaPersistencePort.findByPropietario(username);
            if (tarjetas != null && !tarjetas.isEmpty()) {
                cuentaRepository.saveCuenta(tarjetas.get(0));
            }
            // =================================================================

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