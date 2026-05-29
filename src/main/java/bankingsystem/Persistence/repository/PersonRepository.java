package bankingsystem.Persistence.repository;

import bankingsystem.domain.Person;
import bankingsystem.services.outputport.PersonaPersistencePort;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PersonRepository implements PersonaPersistencePort {
    private List<Person> persons = new ArrayList<>();

    public Person savePersona(Person person) {

        persons.add(person);

        return person;
    }

    public List<Person> findAllPersonas() {
        for(Person person : persons) {
            System.out.println("ID: " + person.getId() + ", Name: " + person.getName() + ", Telephone: " + person.getTelephone() + ", Email: " + person.getEmail() + ", Username: " + person.getUsername() + ", Initial Balance: " + person.getInitialBalance());
        }
        return persons;
    }

    public Optional<Person> findPersonaByIdOptional(int id) {
        return Optional.empty();
    }

    public Optional<Person> findPersonaById(int id) {
        System.out.println("repositorio" + id  );
        try {
            for (Person person : persons) {
                if (person.getId() == id) {
                    System.out.println("ID: " + person.getId() + ", Name: " + person.getName() + ", Telephone: " + person.getTelephone() + ", Email: " + person.getEmail() + ", Username: " + person.getUsername() + ", Initial Balance: " + person.getInitialBalance());

                }
                return Optional.of(person);
            }
        } catch (Exception e) {
            System.out.println("Person not found");
        }
        return Optional.empty();
    }

    public Person findPersonaByUsername(String username) {
        return null;
    }

    public Person findByUsername(String username) {
        return null;
    }

    public Person updatePerson(int id, String name, String telephone, String email, String username, double initialBalance, String password) {
        return null;
    }


    public Person updatePerson(Person person) {
        for(Person person1 : persons) {
            if(person1.getId() == person.getId()) {
                return person;
            }
        }
        return person;
    }


    public void deletePersona(int id) {
        for(Person person : persons) {
            if(id == person.getId()) {
                persons.remove(id);
            }
        }
    }

}