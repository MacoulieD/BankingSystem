package bankingsystem.Persistence.repository;

import bankingsystem.domain.Person;
import bankingsystem.services.outputport.PersonaPersistencePort;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación en memoria del puerto PersonaPersistencePort.
 * Usada en pruebas o cuando no hay conexión a BD.
 */
public class PersonRepository implements PersonaPersistencePort {

    private final List<Person> persons = new ArrayList<>();

    @Override
    public Person savePersona(Person person) {
        persons.add(person);
        return person;
    }

    @Override
    public List<Person> findAllPersonas() {
        persons.forEach(p -> System.out.println("ID: " + p.getId() + ", Name: " + p.getName()));
        return persons;
    }

    @Override
    public Optional<Person> findPersonaByIdOptional(int id) {
        return persons.stream().filter(p -> p.getId() == id).findFirst();
    }

    @Override
    public Optional<Person> findPersonaById(int id) {
        return findPersonaByIdOptional(id);
    }

    @Override
    public Person findPersonaByUsername(String username) {
        return persons.stream()
                .filter(p -> p.getUsername() != null && p.getUsername().equalsIgnoreCase(username))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Person findByUsername(String username) {
        return findPersonaByUsername(username);
    }

    @Override
    public Person updatePerson(int id, String name, String telephone, String email,
                               String username, double initialBalance, String password) {
        return findPersonaByIdOptional(id).map(p -> {
            p.setName(name); p.setTelephone(telephone); p.setEmail(email);
            p.setUsername(username); p.setInitialBalance(initialBalance); p.setPassword(password);
            return p;
        }).orElse(null);
    }

    @Override
    public Person updatePerson(Person person) {
        return persons.stream()
                .filter(p -> p.getId() == person.getId())
                .findFirst()
                .map(p -> {
                    p.setName(person.getName());
                    p.setTelephone(person.getTelephone());
                    p.setEmail(person.getEmail());
                    p.setUsername(person.getUsername());
                    p.setInitialBalance(person.getInitialBalance());
                    p.setPassword(person.getPassword());
                    return p;
                }).orElse(null);
    }

    @Override
    public void deletePersona(int id) {
        persons.removeIf(p -> p.getId() == id);
    }
}