package bankingsystem.services;

import bankingsystem.domain.Person;

public interface PersonService {
    public Person createPerson();
    public Person updatePerson(int id);
    public void deletePerson(int id);
}
