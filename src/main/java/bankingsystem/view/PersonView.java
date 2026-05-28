package bankingsystem.view;

import bankingsystem.domain.Person;
import bankingsystem.services.input.PersonService;
import bankingsystem.utils.FormValidation;

import javax.swing.*;

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
        double initialBalance = FormValidation.validateDouble("Ingrese el saldo inicial: ");

        String password;
        String confirmPassword;
        while (true) {
            password = FormValidation.validateString("Ingrese la contraseña: ");
            confirmPassword = FormValidation.validateString("Confirme la contraseña: ");
            if (FormValidation.validatePassword(password, confirmPassword)) {
                break;
            }
        }

        personService.createPerson(idPerson, name, telephone, email, username, initialBalance, password, confirmPassword);
    }

    public void updatePerson(){
        personService.updatePerson(FormValidation.validateInt("Ingrese el ID"));
    }

    public Person updateLoggedPerson(String username){
        return personService.updatePersonByUsername(username);
    }

        public void deletePerson(){
            personService.deletePerson(FormValidation.validateInt("Ingrese el id de la persona a eliminar"));
        }

}
