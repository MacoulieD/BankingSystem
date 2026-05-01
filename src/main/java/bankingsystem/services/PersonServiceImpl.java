package bankingsystem.services;

import bankingsystem.domain.Cuenta;
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

        TypoCuenta tipoSeleccionado = null;
        if (tipoCuenta == 1 || tipoCuenta == 2) {
            tipoSeleccionado = TypoCuenta.desdeId(tipoCuenta);
        } else {
            System.out.println("⚠️ Opción no válida. El perfil se creará sin cuenta activa.");
        }


        Person newPerson = new Person(id, name, telephone, email, username, initialBalance, password);
        Person saved = personRepository.save(newPerson);

        System.out.println("\n✅ Usuario registrado correctamente: " + saved.getUsername());

        if (tipoSeleccionado != null) {
            try {
                cuentaService.crearCuenta(username, initialBalance, tipoSeleccionado);
                System.out.println("✅ Cuenta inicial creada: " + tipoSeleccionado + ".");
            } catch (Exception e) {
                System.out.println("⚠️ Usuario creado, pero no se pudo aperturar la cuenta inicial: " + e.getMessage());
            }
        } else {
            System.out.println("ℹ️ Puedes aperturar tu primera cuenta desde el menú después de iniciar sesión.");
        }

        return saved;
    }

    @Override
    public Person updatePerson(int id) {
        Person person = personRepository.findById(id);
        if (person == null) {
            System.out.println("❌ No se encontró un usuario con esa identificación.");
            return null;
        }
        return editarDatosPersona(person);
    }

    @Override
    public Person updatePersonByUsername(String username) {
        Person person = personRepository.findByUsername(username);
        if (person == null) {
            System.out.println("❌ No se encontró el usuario autenticado.");
            return null;
        }
        return editarDatosPersona(person);
    }

    @Override
    public void deletePerson(int id) {
        personRepository.deleteById(id);
    }

    private Person editarDatosPersona(Person person) {
        System.out.println("\n--- ACTUALIZACIÓN DE DATOS ---");

        boolean huboCambios = false;
        String ultimoDatoActualizado = null;
        int opcion = -1;

        while (opcion != 0) {
            mostrarDatosActuales(person);
            System.out.println("\n¿Qué dato deseas modificar?");
            System.out.println("1. Identificación");
            System.out.println("2. Nombre completo");
            System.out.println("3. Celular");
            System.out.println("4. Email");
            System.out.println("5. Nombre de usuario");
            System.out.println("6. Contraseña");
            System.out.println("0. Finalizar edición");

            opcion = PersonFormValidation.validateInt("Seleccione una opción: ");

            switch (opcion) {
                case 1 -> {
                    int valorAnterior = person.getId();
                    int nuevoId;
                    while (true) {
                        nuevoId = PersonFormValidation.validateInt("Nueva identificación: ");
                        Person existente = personRepository.findById(nuevoId);
                        if (existente != null && existente != person) {
                            System.out.println("❌ La identificación ya está registrada por otro usuario.");
                            continue;
                        }
                        break;
                    }
                    person.setId(nuevoId);
                    huboCambios = true;
                    ultimoDatoActualizado = "Identificación";
                    System.out.println("✅ Identificación actualizada: " + valorAnterior + " -> " + nuevoId + ".");
                }
                case 2 -> {
                    String valorAnterior = person.getName();
                    String nuevoNombre = PersonFormValidation.validateStringName("Nuevo nombre completo: ");
                    person.setName(nuevoNombre);
                    huboCambios = true;
                    ultimoDatoActualizado = "Nombre completo";
                    System.out.println("✅ Nombre actualizado: " + valorAnterior + " -> " + nuevoNombre + ".");
                }
                case 3 -> {
                    int valorAnterior = person.getTelephone();
                    int nuevoTelefono = PersonFormValidation.validateInt("Nuevo celular: ");
                    person.setTelephone(nuevoTelefono);
                    huboCambios = true;
                    ultimoDatoActualizado = "Celular";
                    System.out.println("✅ Celular actualizado: " + valorAnterior + " -> " + nuevoTelefono + ".");
                }
                case 4 -> {
                    String valorAnterior = person.getEmail();
                    String nuevoEmail = PersonFormValidation.validateString("Nuevo email: ");
                    person.setEmail(nuevoEmail);
                    huboCambios = true;
                    ultimoDatoActualizado = "Email";
                    System.out.println("✅ Email actualizado: " + valorAnterior + " -> " + nuevoEmail + ".");
                }
                case 5 -> {
                    String usernameAnterior = person.getUsername();
                    String nuevoUsername;
                    while (true) {
                        nuevoUsername = PersonFormValidation.validateString("Nuevo nombre de usuario: ");
                        Person existente = personRepository.findByUsername(nuevoUsername);
                        if (existente != null && existente != person) {
                            System.out.println("❌ El nombre de usuario ya existe. Intente con uno diferente.");
                            continue;
                        }
                        break;
                    }
                    person.setUsername(nuevoUsername);
                    if (!usernameAnterior.equalsIgnoreCase(nuevoUsername)) {
                        sincronizarPropietarioEnCuentas(usernameAnterior, nuevoUsername);
                    }
                    huboCambios = true;
                    ultimoDatoActualizado = "Nombre de usuario";
                    System.out.println("✅ Nombre de usuario actualizado: " + usernameAnterior + " -> " + nuevoUsername + ".");
                }
                case 6 -> {
                    String nuevaPassword;
                    while (true) {
                        nuevaPassword = PersonFormValidation.validateString("Nueva contraseña: ");
                        String repetirPassword = PersonFormValidation.validateString("Confirme la nueva contraseña: ");
                        if (PersonFormValidation.validatePassword(nuevaPassword, repetirPassword)) {
                            break;
                        }
                    }
                    person.setPassword(nuevaPassword);
                    huboCambios = true;
                    ultimoDatoActualizado = "Contraseña";
                    System.out.println("✅ Contraseña actualizada correctamente.");
                }
                case 0 -> System.out.println("↩️ Finalizando edición...");
                default -> System.out.println("❌ Opción no válida.");
            }
        }

        if (huboCambios) {
            System.out.println("✅ Datos actualizados correctamente. Último dato modificado: " + ultimoDatoActualizado + ".");
        } else {
            System.out.println("ℹ️ No se realizaron cambios en el perfil.");
        }

        return person;
    }

    private void mostrarDatosActuales(Person person) {
        System.out.println("\n--- DATOS ACTUALES DEL USUARIO ---");
        System.out.println("Identificación: " + person.getId());
        System.out.println("Nombre completo: " + person.getName());
        System.out.println("Celular: " + person.getTelephone());
        System.out.println("Email: " + person.getEmail());
        System.out.println("Nombre de usuario: " + person.getUsername());
    }

    private void sincronizarPropietarioEnCuentas(String usernameAnterior, String nuevoUsername) {
        for (Cuenta cuenta : cuentaService.listarTodasLasCuentas()) {
            if (cuenta.getPropietario() != null && cuenta.getPropietario().equalsIgnoreCase(usernameAnterior)) {
                cuenta.setPropietario(nuevoUsername);
            }
        }
    }
}