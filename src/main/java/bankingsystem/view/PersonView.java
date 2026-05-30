package bankingsystem.view;

import bankingsystem.domain.Person;
import bankingsystem.domain.enums.TypoCuenta;
import bankingsystem.services.input.PersonService;
import bankingsystem.utils.FormValidation;

import javax.swing.*;
import java.util.Optional;

public class PersonView {

    private final PersonService personService;

    public PersonView(PersonService personService){
        this.personService = personService;
    }

    public void createPerson() {
        int idPerson = FormValidation.validateInt("Ingrese el ID: ");
        String name = FormValidation.validateStringName("Ingrese el nombre: ");
        String telephone = FormValidation.validateintPhone("Ingrese el teléfono: ");
        String email = FormValidation.validateString("Ingrese el correo electrónico: ");
        String username = FormValidation.validateString("Ingrese el nombre de usuario: ");

        String password;
        String confirmPassword;
        while (true) {
            password = FormValidation.validateString("Ingrese la contraseña: ");
            confirmPassword = FormValidation.validateString("Confirme la contraseña: ");
            if (FormValidation.validatePassword(password, confirmPassword)) {
                break;
            }
        }

        System.out.println("\n¿Qué tipo de cuenta desea abrir?");
        System.out.println("1. Cuenta de Ahorros");
        System.out.println("2. Cuenta Corriente");
        TypoCuenta tipoCuenta;
        while (true) {
            int opcion = FormValidation.validateInt("Seleccione una opción: ");
            tipoCuenta = TypoCuenta.desdeId(opcion);
            if (tipoCuenta == TypoCuenta.AHORROS || tipoCuenta == TypoCuenta.CORRIENTE) break;
            System.out.println("❌ Opción no válida. Seleccione 1 (Ahorros) o 2 (Corriente).");
        }

        double initialBalance = FormValidation.validateDouble("Ingrese el saldo inicial: ");

        personService.createPerson(idPerson, name, telephone, email, username, initialBalance, password, confirmPassword, tipoCuenta);
    }

    public void getPersonById() {
        int idPerson = FormValidation.validateInt("Ingrese el ID de la persona: ");
        Optional<Person> personOptional = personService.getPersonById(idPerson);

        if (personOptional.isEmpty()) {
            System.out.println("❌ No se encontró ninguna persona con el ID ingresado.");
            return;
        }

        Person person = personOptional.get();
        System.out.println("ID: " + person.getId() + ", Name: " + person.getName()
                + ", Telephone: " + person.getTelephone()
                + ", Email: " + person.getEmail()
                + ", Username: " + person.getUsername()
                + ", Initial Balance: " + person.getInitialBalance());
    }

    public void updatePerson(){
        System.out.println("Estoy en la Vista");
        int id = FormValidation.validateInt("Ingrese el ID de la persona: ");

        Optional<Person> personOptional = personService.getPersonById(id);
        if (personOptional.isEmpty()) {
            System.out.println("❌ No se encontró ninguna persona con el ID ingresado.");
            return;
        }

        Person person = personOptional.get();
        String name = FormValidation.validateStringName("Ingrese el nombre: ");
        String telephone = FormValidation.validateintPhone("Ingrese el teléfono: ");
        String email = FormValidation.validateString("Ingrese el correo electrónico: ");
        String username = FormValidation.validateString("Ingrese el nombre de usuario: ");
        double initialBalance = FormValidation.validateDouble("Ingrese el saldo inicial: ");

        String password;
        String confirmPassword;
        String passwordActual = person.getPassword();

        while (true) {
            password = FormValidation.validateString("Ingrese la nueva contraseña: ");

            if (password.equals(passwordActual)) {
                System.out.println("❌ La nueva contraseña no puede ser igual a la anterior. Elige otra.");
                continue;
            }

            confirmPassword = FormValidation.validateString("Confirme la contraseña: ");

            if (!password.equals(confirmPassword)) {
                System.out.println("❌ Las contraseñas no coinciden. Inténtalo de nuevo.");
                continue;
            }

            break;
        }

        personService.updatePerson(id, name, telephone, email, username, initialBalance, password, confirmPassword);
        System.out.println("🎉 ¡Persona actualizada con éxito!");
    }

    public void deletePerson(){
        personService.deletePerson(FormValidation.validateInt("Ingrese el ID de la persona a eliminar: "));
    }

    public Person updateLoggedPerson(String username) {
        // Busca la persona en BD por username
        Optional<Person> personOpt = personService.getPersonByUsername(username);
        if (personOpt.isEmpty()) {
            System.out.println("❌ No se encontró el usuario en la base de datos.");
            return null;
        }

        Person person = personOpt.get();
        // Delega al servicio que ya tiene el menú y la lógica de actualización
        return personService.updatePerson(
                person.getId(),
                person.getName(),
                person.getTelephone(),
                person.getEmail(),
                person.getUsername(),
                person.getInitialBalance(),
                person.getPassword(),
                person.getPassword()
        );
    }
}