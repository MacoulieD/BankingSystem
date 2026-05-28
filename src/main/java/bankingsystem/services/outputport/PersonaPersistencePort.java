package bankingsystem.services.outputport;


import bankingsystem.domain.Person;

import java.util.List;
import java.util.Optional;

// Este es el puerto de salida para la persistencia de datos de personas. Define las operaciones que se pueden realizar con los datos de las personas, como guardar, actualizar, eliminar o consultar información. Las implementaciones de este puerto se encargarán de interactuar con la base de datos u otros mecanismos de almacenamiento para gestionar los datos de las personas.
public interface PersonaPersistencePort {

    Person savePersona(Person person);
    List<Person> findAllPersonas();
    Optional<Person> findPersonaByIdOptional(int id);
    Person findPersonaById(int id);
    Person findPersonaByUsername(String username);
    Person findByUsername(String username);
    void deletePersona(int id);

}
