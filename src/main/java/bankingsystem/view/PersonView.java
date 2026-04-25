package bankingsystem.view;

import bankingsystem.services.PersonService;
import bankingsystem.utils.PersonFormValidation;

public class PersonView {
    private final PersonService personService;
    public PersonView(PersonService personService){
        this.personService = personService;
    }

    public void createPerson(){
        personService.createPerson();
    }

    public void updatePerson(){
        personService.updatePerson(PersonFormValidation.validateInt("Ingrese el ID"));
    }

        public void deletePerson(){
            personService.deletePerson(PersonFormValidation.validateInt("Ingrese el id de la persona a eliminar"));
        }

}
