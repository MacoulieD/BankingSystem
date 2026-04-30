package bankingsystem.services;

import bankingsystem.domain.Person;
import bankingsystem.domain.enums.TypoCuenta;
import bankingsystem.repository.PersonRepository;
import bankingsystem.utils.PersonFormValidation;


public class PersonServiceImpl implements PersonService {

    private final PersonRepository personRepository;
    private final CuentaServices cuentaService; // Inyectamos la interfaz general


    public PersonServiceImpl(PersonRepository personRepository, CuentaServices cuentaService) {
        this.personRepository = personRepository;
        this.cuentaService = cuentaService;
    }

    @Override
    public Person createPerson() {
        System.out.println("\n--- FORMULARIO DE REGISTRO ---");


        int id, telephone;
        String name, email, username, password;
        double initialBalance;


        id = PersonFormValidation.validateInt("Ingrese su identificación: ");
        name = PersonFormValidation.validateStringName("Ingrese su nombre completo: ");
        telephone = PersonFormValidation.validateInt("Ingrese su celular: ");
        email = PersonFormValidation.validateString("Ingrese su email: ");


        while (true) {
            username = PersonFormValidation.validateString("Ingrese su nombre de usuario: ");
            if (personRepository.existsByUsername(username)) {
                System.out.println("❌ El usuario ya existe. Intente con uno diferente.");
            } else {
                break;
            }
        }


        while (true) {
            password = PersonFormValidation.validateString("Ingrese su contraseña: ");
            String repeatPassword = PersonFormValidation.validateString("Confirme su contraseña: ");
            if (PersonFormValidation.validatePassword(password, repeatPassword)) {
                break;
            }
        }


        initialBalance = PersonFormValidation.validateDouble("Ingrese el saldo inicial para la apertura: ");


        System.out.println("\n¿Qué tipo de cuenta desea abrir?");
        System.out.println("1. Cuenta de Ahorros");
        System.out.println("2. Cuenta Corriente");
        int tipoCuenta = PersonFormValidation.validateInt("Seleccione una opción: ");


        if (tipoCuenta == 1 || tipoCuenta == 2) {

            cuentaService.crearCuenta(username, initialBalance, TypoCuenta.desdeId(tipoCuenta));

            String mensaje = (tipoCuenta == 1) ? "Ahorros" : "Corriente";
            System.out.println("✅ Solicitud de Cuenta de " + mensaje + " procesada.");
        } else {
            System.out.println("⚠️ Opción no válida. El perfil se creará sin cuenta activa.");
        }


        Person newPerson = new Person(id, name, telephone, email, username, initialBalance, password);
        Person saved = personRepository.save(newPerson);

        System.out.println("\n✅ ¡Registro de usuario y cuenta completado con éxito!");
        return saved;
    }

    @Override
    public Person updatePerson(int id) {
        return null;
    }

    @Override
    public void deletePerson(int id) {
        personRepository.deleteById(id);
    }
}