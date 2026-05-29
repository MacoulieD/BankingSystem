package bankingsystem.services;

import bankingsystem.domain.Person;
import bankingsystem.domain.enums.TypoCuenta;
import bankingsystem.services.input.CuentaServices;
import bankingsystem.services.input.PersonService;
import bankingsystem.services.outputport.PersonaPersistencePort;
import bankingsystem.utils.FormValidation;

import java.util.Optional;

public class PersonServiceImpl implements PersonService {

    private final PersonaPersistencePort personRepository;
    private CuentaServices cuentaService;

    public PersonServiceImpl(PersonaPersistencePort personRepository, CuentaServices cuentaService) {
        this.personRepository = personRepository;
        this.cuentaService = cuentaService;
    }

    public void setCuentaService(CuentaServices cuentaService) {
        this.cuentaService = cuentaService;
    }

    @Override
    public Person createPerson(int id, String name, String telephone, String email, String username, double initialBalance, String password, String confirmPassword, TypoCuenta tipoCuenta) {
        if (personRepository.findPersonaByIdOptional(id).isPresent()) {
            System.out.println("❌ Ya existe un usuario con el ID " + id + ". Ingrese un ID diferente.");
            return null;
        }
        Person person = new Person(id, name, telephone, email, username, initialBalance, password, confirmPassword, 0, null);
        Person saved;
        try {
            saved = personRepository.savePersona(person);
        } catch (RuntimeException e) {
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("email")) {
                System.out.println("❌ El correo electrónico ingresado ya está registrado. Ingrese uno diferente.");
            } else {
                System.out.println("❌ No se pudo registrar el usuario: " + e.getMessage());
            }
            return null;
        }
        try {
            cuentaService.crearCuenta(username, initialBalance, tipoCuenta);
        } catch (RuntimeException e) {
            System.out.println("❌ Usuario registrado pero no se pudo crear la cuenta: " + e.getMessage());
            return saved;
        }
        System.out.println("✅ Cuenta inicial creada: " + tipoCuenta + ".");
        return saved;
    }

    @Override
    public Optional<Person> getPersonById(int id) {
        return personRepository.findPersonaByIdOptional(id);
    }

    @Override
    public Optional<Person> getPersonByEmail(String email) {
        return Optional.empty();
    }

    @Override
    public Person updatePerson(int id, String name, String telephone, String email, String username, double initialBalance, String password, String confirmPassword) {
        Optional<Person> personOpt = personRepository.findPersonaByIdOptional(id);
        if (personOpt.isPresent()) {
            Person person = personOpt.get();
            System.out.println("\n--- ACTUALIZACIÓN DE DATOS ---");
            System.out.println("\n¿Qué dato deseas modificar?");
            System.out.println("1. Identificación 2. Nombre completo 3. Celular 4. Email 5. Nombre de usuario 6. Contraseña");
            int opcion = FormValidation.validateInt("Seleccione una opción: ");
            switch (opcion) {
                case 1:
                    person.setId(FormValidation.validateInt("Nueva identificación: "));
                    personRepository.updatePerson(person);
                    System.out.println("🎉 ¡Identificación actualizada con éxito!");
                    break;
                case 2:
                    person.setName(FormValidation.validateStringName("Nuevo nombre completo: "));
                    personRepository.updatePerson(person);
                    System.out.println("🎉 ¡Nombre completo actualizado con éxito!");
                    break;
                case 3:
                    person.setTelephone(FormValidation.validateintPhone("Nuevo celular: "));
                    personRepository.updatePerson(person);
                    System.out.println("🎉 ¡Número de celular actualizado con éxito!");
                    break;
                case 4:
                    person.setEmail(FormValidation.validateString("Nuevo email: "));
                    personRepository.updatePerson(person);
                    System.out.println("🎉 ¡Correo electrónico actualizado con éxito!");
                    break;
                case 5:
                    String usernameActual = person.getUsername();
                    String nuevoUsername;

                    while (true) {
                        nuevoUsername = FormValidation.validateString("Nuevo nombre de usuario: ");

                        if (nuevoUsername.equalsIgnoreCase(usernameActual)) {
                            System.out.println("ℹ️ El nuevo username es igual al actual. No se realizaron cambios.");
                            break;
                        }

                        Person existente = personRepository.findPersonaByUsername(nuevoUsername);
                        if (existente != null && existente.getId() != person.getId()) {
                            System.out.println("❌ El nombre de usuario '" + nuevoUsername + "' ya existe. Intente con uno diferente.");
                        } else {
                            person.setUsername(nuevoUsername);
                            personRepository.updatePerson(person);
                            System.out.println("🎉 ¡Cambio exitoso! Tu nombre de usuario ahora es: " + nuevoUsername);
                            break;
                        }
                    }
                    break;
                case 6:
                    String passwordActual = person.getPassword();
                    String nuevaPassword;
                    String confirmacionPassword;

                    while (true) {
                        nuevaPassword = FormValidation.validateString("Nueva contraseña: ");

                        if (nuevaPassword.equals(passwordActual)) {
                            System.out.println("❌ La nueva contraseña no puede ser igual a la contraseña actual. Elige una diferente.");
                            continue;
                        }

                        confirmacionPassword = FormValidation.validateString("Confirme su nueva contraseña: ");

                        if (!nuevaPassword.equals(confirmacionPassword)) {
                            System.out.println("❌ Las contraseñas no coinciden. Inténtalo de nuevo desde el principio.");
                            continue;
                        }

                        person.setPassword(nuevaPassword);
                        personRepository.updatePerson(person);
                        System.out.println("🎉 ¡Cambio de contraseña exitoso! Tu clave ha sido actualizada.");
                        break;
                    }
                    break;
                default:
                    System.out.println("❌ Opción no válida. No se realizaron cambios.");
                    break;
            }
            return person;
        } else {
            System.out.println("❌ No se encontró un usuario con esa identificación.");
            return null;
        }
    }

    @Override
    public void deletePerson(int id) {
        // Implementar lógica de borrado físico o lógico si es necesario
    }
}