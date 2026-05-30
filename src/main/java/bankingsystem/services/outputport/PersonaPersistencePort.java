package bankingsystem.services.outputport;

import bankingsystem.domain.Person;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para persistencia de personas.
 * Define el contrato que cualquier adaptador (BD, memoria) debe cumplir.
 */
public interface PersonaPersistencePort {

    Person savePersona(Person person);
    List<Person> findAllPersonas();
    Optional<Person> findPersonaByIdOptional(int id);
    Optional<Person> findPersonaById(int id);
    Person findPersonaByUsername(String username);
    Person findByUsername(String username);

    /**
     * Actualiza los datos del cliente en la BD.
     * Si el username cambia, propaga el nuevo propietario a todas las cuentas.
     */
    Person updatePerson(int id, String name, String telephone, String email,
                        String username, double initialBalance, String password);

    Person updatePerson(Person person);

    // ── Criterio 2: propaga el nuevo username a todas las cuentas del cliente ──
    void updatePropietarioEnCuentas(String usernameAnterior, String usernameNuevo);

    void deletePersona(int id);
}