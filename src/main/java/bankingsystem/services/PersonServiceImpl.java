package bankingsystem.services;

import bankingsystem.domain.Person;
import bankingsystem.repository.PersonRepository;
import bankingsystem.utils.PersonFormValidation;

public class PersonServiceImpl implements PersonService {
    private final PersonRepository personRepository;

    public PersonServiceImpl(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @Override
    public Person createPerson() {
        System.out.println("\n--- FORMULARIO DE REGISTRO ---");

        int id = PersonFormValidation.validateInt("Ingrese su identificación: ");
        String name = PersonFormValidation.validateStringName("Ingrese su nombre completo: ");
        String phone = PersonFormValidation.validateStringPhone("Ingrese su celular: ");
        String email = PersonFormValidation.validateString("Ingrese su email: ");

        String username;
        while (true) {
            username = PersonFormValidation.validateString("Ingrese su nombre de usuario: ");
            if (personRepository.existsByUsername(username)) {
                System.out.println("❌ El usuario ya existe. Intente con uno diferente.");
            } else {
                break;
            }
        }

        double balance = PersonFormValidation.validateDouble("Ingrese su saldo inicial: ");

        String password, repeatPassword;
        while (true) {
            password = PersonFormValidation.validateString("Ingrese su contraseña: ");
            repeatPassword = PersonFormValidation.validateString("Confirme su contraseña: ");
            if (PersonFormValidation.validatePassword(password, repeatPassword)) {
                break;
            }
        }

        Person newPerson = new Person(id, name, phone, email, username, balance, password);
        Person saved = personRepository.save(newPerson);

        System.out.println("\n✅ ¡Registro exitoso!");
        return saved;
    }

    @Override public Person getPersonById(int id) { return personRepository.findById(id); }
    @Override public Person getPersonByEmail(String email) { return null; }
    @Override public Person updatePerson(int id) { return null; }
    @Override public void deletePerson(int id) { personRepository.deleteById(id); }
}