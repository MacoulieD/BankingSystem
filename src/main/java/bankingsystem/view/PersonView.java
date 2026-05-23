package bankingsystem.view;

import bankingsystem.domain.Person;
import bankingsystem.services.PersonService;
import bankingsystem.utils.FormValidation;

public class PersonView {
    private final PersonService personService;
    public PersonView(PersonService personService){
        this.personService = personService;
    }

    public void createPerson(){
        personService.createPerson();
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
