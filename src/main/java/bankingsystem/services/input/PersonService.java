package bankingsystem.services.input;

import bankingsystem.domain.Person;

import java.util.Optional;

public interface PersonService {
    public Person createPerson(int id, String name, String telephone, String email, String username, double initialBalance, String password, String confirmPassword);
    public Optional<Person> getPersonById(int id);
    public Optional<Person> getPersonByEmail(String email);
    public Person updatePerson(int id);
    public Person updatePersonByUsername(String username);
    public void deletePerson(int id);
}
