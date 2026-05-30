package bankingsystem.services.input;

import bankingsystem.domain.Person;
import bankingsystem.domain.enums.TypoCuenta;

import java.util.Optional;

public interface PersonService {
    public Person createPerson(int id, String name, String telephone, String email, String username, double initialBalance, String password, String confirmPassword, TypoCuenta tipoCuenta);
    public Optional<Person> getPersonById(int id);
    public Optional<Person> getPersonByEmail(String email);
    Optional<Person> getPersonByUsername(String username);
    Person updatePerson(int id, String name, String telephone, String email, String username, double initialBalance, String password, String confirmPassword);
    public void deletePerson(int id);
}