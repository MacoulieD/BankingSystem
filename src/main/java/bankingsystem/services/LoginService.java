package bankingsystem.services;

import bankingsystem.domain.Person;

public interface LoginService {

    Person login(String username, String password);
}