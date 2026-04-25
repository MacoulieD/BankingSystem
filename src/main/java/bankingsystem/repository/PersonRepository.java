package bankingsystem.repository;

import bankingsystem.domain.Person;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PersonRepository {
    // Lista en memoria para simular la base de datos
    private List<Person> persons = new ArrayList<>();

    // Guarda una nueva persona en la lista
    public Person save(Person person) {
        persons.add(person);
        return person;
    }

    // Verifica si el nombre de usuario ya está tomado (Usado en Registro)
    public boolean existsByUsername(String username) {
        return persons.stream()
                .anyMatch(p -> p.getUsername().equalsIgnoreCase(username));
    }

    // BUSCADOR CLAVE: Encuentra a la persona por su usuario (Usado en Login)
    public Person findByUsername(String username) {
        return persons.stream()
                .filter(p -> p.getUsername().equalsIgnoreCase(username))
                .findFirst()
                .orElse(null);
    }

    // Retorna todos los registros
    public List<Person> findAll() {
        return new ArrayList<>(persons);
    }

    // Busca por ID (Identificación)
    public Person findById(int id) {
        return persons.stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElse(null);
    }

    // Elimina un registro por ID
    public boolean deleteById(int id) {
        return persons.removeIf(p -> p.getId() == id);
    }
}