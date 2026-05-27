package bankingsystem.services.input;

import bankingsystem.domain.Person;

public interface LoginService {

    Person login(String username, String password);
}