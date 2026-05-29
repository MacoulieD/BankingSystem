package bankingsystem.services;

import bankingsystem.domain.Person;
import bankingsystem.domain.enums.TypoCuenta;
import bankingsystem.services.input.CuentaServices;
import bankingsystem.services.input.PersonService;
import bankingsystem.services.outputport.PersonaPersistencePort;
import bankingsystem.utils.FormValidation;

import java.util.Optional;


public class    PersonServiceImpl implements PersonService {
//    private final PersonRepository personRepository;
    private final PersonaPersistencePort personRepository;
    private final CuentaServices cuentaService; // Inyectamos la interfaz general


    public PersonServiceImpl(PersonaPersistencePort personRepository, CuentaServices cuentaService) {
        this.personRepository = personRepository;
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
                    break;
                case 2:
                    person.setName(FormValidation.validateStringName("Nuevo nombre completo: "));
                    break;
                case 3:
                    person.setTelephone(FormValidation.validateintPhone("Nuevo celular: "));
                    break;
                case 4:
                    person.setEmail(FormValidation.validateString("Nuevo email: "));
                    break;
                case 5:
                    String usernameActual = person.getUsername();
                    String nuevoUsername;

                    while (true) {
                        nuevoUsername = FormValidation.validateString("Nuevo nombre de usuario: ");

                        if (nuevoUsername.equalsIgnoreCase(usernameActual)) {
                            System.out.println("ℹ️ El nuevo username es igual al actual. No se realizaron cambios.");
                            break; //
                        }

                        Person existente = personRepository.findPersonaByUsername(nuevoUsername);
                        if (existente != null && existente.getId() != person.getId()) {
                            System.out.println("❌ El nombre de usuario '" + nuevoUsername + "' ya existe. Intente con uno diferente.");
                        }
                        else {
                            person.setUsername(nuevoUsername);

                            personRepository.updatePerson(person);

                            System.out.println("🎉 ¡Cambio exitoso! Tu nombre de usuario ahora es: " + nuevoUsername);
                            break; // Salimos del bucle ya que todo salió bien
                        }
                    }
                case 6:
                    String passwordActual = person.getPassword();
                    String nuevaPassword;
                    String confirmacionPassword;

                    while (true) {
                        nuevaPassword = FormValidation.validateString("Nueva contraseña: ");

                        if (nuevaPassword.equals(passwordActual)) {
                            System.out.println("❌ La nueva contraseña no puede ser igual a la contraseña actual. Elige una diferente.");
                            continue; // Vuelve a pedir la contraseña
                        }

                        confirmacionPassword = FormValidation.validateString("Confirme su nueva contraseña: ");

                        if (!nuevaPassword.equals(confirmacionPassword)) {
                            System.out.println("❌ Las contraseñas no coinciden. Inténtalo de nuevo desde el principio.");
                            continue; // Vuelve a empezar el ciclo
                        }

                        person.setPassword(nuevaPassword);

                        personRepository.updatePerson(person);

                        System.out.println("🎉 ¡Cambio de contraseña exitoso! Tu clave ha sido actualizada.");
                        break;
                    }
                    default:
                        System.out.println("❌ Opción no válida. No se realizaron cambios.");
                        break;
            }
            return person;
        }else {
            System.out.println("❌ No se encontró un usuario con esa identificación.");
            return null;
        }
    }

    @Override
    public void deletePerson(int id) {

    }
}


//        id = FormValidation.validateInt("Ingrese su identificación: ");
//        name = FormValidation.validateStringName("Ingrese su nombre completo: ");
//        telephone = FormValidation.validateintPhone("Ingrese su número de teléfono: ");
//        email = FormValidation.validateString("Ingrese su email: ");
//
//
//        while (true) {
//            username = FormValidation.validateString("Ingrese su nombre de usuario: ");
//            if (personRepository.existsByUsername(username)) {
//                System.out.println("❌ El usuario ya existe. Intente con uno diferente.");
//            } else {
//                break;
//            }
//        }
//
//
//        while (true) {
//            password = FormValidation.validateString("Ingrese su contraseña: ");
//            String repeatPassword = FormValidation.validateString("Confirme su contraseña: ");
//            if (FormValidation.validatePassword(password, repeatPassword)) {
//                break;
//            }
//        }
//
//
//        initialBalance = FormValidation.validateDouble("Ingrese el saldo inicial para la apertura: ");
//
//
//        System.out.println("\n¿Qué tipo de cuenta desea abrir?");
//        System.out.println("1. Cuenta de Ahorros");
//        System.out.println("2. Cuenta Corriente");
//        int tipoCuenta = FormValidation.validateInt("Seleccione una opción: ");
//
//        TypoCuenta tipoSeleccionado = null;
//        if (tipoCuenta == 1 || tipoCuenta == 2) {
//            tipoSeleccionado = TypoCuenta.desdeId(tipoCuenta);
//        } else {
//            System.out.println("⚠️ Opción no válida. El perfil se creará sin cuenta activa.");
//        }
//
//
//        Person newPerson = new Person(id, name, telephone, email, username, initialBalance, password);
//        Person saved = personRepository.save(newPerson);
//
//        System.out.println("\n✅ Usuario registrado correctamente: " + saved.getUsername());
//
//        if (tipoSeleccionado != null) {
//            try {
//                cuentaService.crearCuenta(username, initialBalance, tipoSeleccionado);
//                System.out.println("✅ Cuenta inicial creada: " + tipoSeleccionado + ".");
//            } catch (Exception e) {
//                System.out.println("⚠️ Usuario creado, pero no se pudo aperturar la cuenta inicial: " + e.getMessage());
//            }
//        } else {
//            System.out.println("ℹ️ Puedes aperturar tu primera cuenta desde el menú después de iniciar sesión.");
//        }
//
//        return saved;
//    }
//
//    @Override
//    public Person updatePerson(int id) {
//        Person person = personRepository.findById(id);
//        if (person == null) {
//            System.out.println("❌ No se encontró un usuario con esa identificación.");
//            return null;
//        }
//        return editarDatosPersona(person);
//    }
//
//    @Override
//    public Person updatePersonByUsername(String username) {
//        Person person = personRepository.findByUsername(username);
//        if (person == null) {
//            System.out.println("❌ No se encontró el usuario autenticado.");
//            return null;
//        }
//        return editarDatosPersona(person);
//    }
//
//    @Override
//    public void deletePerson(int id) {
//        personRepository.deleteById(id);
//    }
//
//    private Person editarDatosPersona(Person person) {
//        System.out.println("\n--- ACTUALIZACIÓN DE DATOS ---");
//
//        boolean huboCambios = false;
//        String ultimoDatoActualizado = null;
//        int opcion = -1;
//
//        while (opcion != 0) {
//            mostrarDatosActuales(person);
//            System.out.println("\n¿Qué dato deseas modificar?");
//            System.out.println("1. Identificación");
//            System.out.println("2. Nombre completo");
//            System.out.println("3. Celular");
//            System.out.println("4. Email");
//            System.out.println("5. Nombre de usuario");
//            System.out.println("6. Contraseña");
//            System.out.println("0. Finalizar edición");
//
//            opcion = FormValidation.validateInt("Seleccione una opción: ");
//
//            switch (opcion) {
//                case 1 -> {
//                    int valorAnterior = person.getId();
//                    int nuevoId;
//                    while (true) {
//                        nuevoId = FormValidation.validateInt("Nueva identificación: ");
//                        Person existente = personRepository.findById(nuevoId);
//                        if (existente != null && existente != person) {
//                            System.out.println("❌ La identificación ya está registrada por otro usuario.");
//                            continue;
//                        }
//                        break;
//                    }
//                    person.setId(nuevoId);
//                    huboCambios = true;
//                    ultimoDatoActualizado = "Identificación";
//                    System.out.println("✅ Identificación actualizada: " + valorAnterior + " -> " + nuevoId + ".");
//                }
//                case 2 -> {
//                    String valorAnterior = person.getName();
//                    String nuevoNombre = FormValidation.validateStringName("Nuevo nombre completo: ");
//                    person.setName(nuevoNombre);
//                    huboCambios = true;
//                    ultimoDatoActualizado = "Nombre completo";
//                    System.out.println("✅ Nombre actualizado: " + valorAnterior + " -> " + nuevoNombre + ".");
//                }
//                case 3 -> {
//                    String valorAnterior = person.getTelephone();
//                    String nuevoTelefono = FormValidation.validateintPhone("Nuevo celular: ");
//                    person.setTelephone(nuevoTelefono);
//                    huboCambios = true;
//                    ultimoDatoActualizado = "Celular";
//                    System.out.println("✅ Celular actualizado: " + valorAnterior + " -> " + nuevoTelefono + ".");
//                }
//                case 4 -> {
//                    String valorAnterior = person.getEmail();
//                    String nuevoEmail = FormValidation.validateString("Nuevo email: ");
//                    person.setEmail(nuevoEmail);
//                    huboCambios = true;
//                    ultimoDatoActualizado = "Email";
//                    System.out.println("✅ Email actualizado: " + valorAnterior + " -> " + nuevoEmail + ".");
//                }
//                case 5 -> {
//                    String usernameAnterior = person.getUsername();
//                    String nuevoUsername;
//                    while (true) {
//                        nuevoUsername = FormValidation.validateString("Nuevo nombre de usuario: ");
//                        Person existente = personRepository.findByUsername(nuevoUsername);
//                        if (existente != null && existente != person) {
//                            System.out.println("❌ El nombre de usuario ya existe. Intente con uno diferente.");
//                            continue;
//                        }
//                        break;
//                    }
//                    person.setUsername(nuevoUsername);
//                    if (!usernameAnterior.equalsIgnoreCase(nuevoUsername)) {
//                        sincronizarPropietarioEnCuentas(usernameAnterior, nuevoUsername);
//                    }
//                    huboCambios = true;
//                    ultimoDatoActualizado = "Nombre de usuario";
//                    System.out.println("✅ Nombre de usuario actualizado: " + usernameAnterior + " -> " + nuevoUsername + ".");
//                }
//                case 6 -> {
//                    String nuevaPassword;
//                    while (true) {
//                        nuevaPassword = FormValidation.validateString("Nueva contraseña: ");
//                        String repetirPassword = FormValidation.validateString("Confirme la nueva contraseña: ");
//                        if (FormValidation.validatePassword(nuevaPassword, repetirPassword)) {
//                            break;
//                        }
//                    }
//                    person.setPassword(nuevaPassword);
//                    huboCambios = true;
//                    ultimoDatoActualizado = "Contraseña";
//                    System.out.println("✅ Contraseña actualizada correctamente.");
//                }
//                case 0 -> System.out.println("↩️ Finalizando edición...");
//                default -> System.out.println("❌ Opción no válida.");
//            }
//        }
//
//        if (huboCambios) {
//            System.out.println("✅ Datos actualizados correctamente. Último dato modificado: " + ultimoDatoActualizado + ".");
//        } else {
//            System.out.println("ℹ️ No se realizaron cambios en el perfil.");
//        }
//
//        return person;
//    }
//
//    private void mostrarDatosActuales(Person person) {
//        System.out.println("\n--- DATOS ACTUALES DEL USUARIO ---");
//        System.out.println("Identificación: " + person.getId());
//        System.out.println("Nombre completo: " + person.getName());
//        System.out.println("Celular: " + person.getTelephone());
//        System.out.println("Email: " + person.getEmail());
//        System.out.println("Nombre de usuario: " + person.getUsername());
//    }
//
//    private void sincronizarPropietarioEnCuentas(String usernameAnterior, String nuevoUsername) {
//        for (Cuenta cuenta : cuentaService.listarTodasLasCuentas()) {
//            if (cuenta.getPropietario() != null && cuenta.getPropietario().equalsIgnoreCase(usernameAnterior)) {
//                cuenta.setPropietario(nuevoUsername);
//            }
//        }
//    }
//}