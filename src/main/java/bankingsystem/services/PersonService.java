package bankingsystem.services;

import bankingsystem.domain.Person;

public interface PersonService {
    public Person createPerson();
    public Person getPersonById(int id);
    public Person getPersonByEmail(String email);
    public Person updatePerson(int id);
    public void deletePerson(int id);
}
