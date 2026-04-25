package bankingsystem.services;

import bankingsystem.domain.Person;

public interface LoginService {
    // Retorna el objeto Person si las credenciales son correctas
    Person login(String username, String password);
}