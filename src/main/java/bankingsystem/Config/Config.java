package bankingsystem.Config;

import bankingsystem.repository.CuentaAhorrosRepository;
import bankingsystem.repository.PersonRepository;
import bankingsystem.services.*;
import bankingsystem.services.CuentaAhorrosServices; // Ajustado a Service (singular)
import bankingsystem.view.CuentaAhorrosView;
import bankingsystem.view.LoginView;
import bankingsystem.view.PersonView;
import bankingsystem.userinterface.MenuApp;

/**
 * Clase de configuración encargada de la Inyección de Dependencias.
 * Conecta los repositorios con los servicios, y estos con las vistas.
 */
public class Config {

    public static MenuApp createMenuApp() {
        // 1. REPOSITORIOS (Persistencia única en memoria)
        PersonRepository personRepo = new PersonRepository();
        CuentaAhorrosRepository cuentaRepo = new CuentaAhorrosRepository();

        // 2. SERVICIOS (Lógica de negocio e inyección de repositorios)
        PersonService personService = new PersonServiceImpl(personRepo);
        LoginService loginService = new LoginServiceImpl(personRepo);

        // Inyectamos el repositorio específico de ahorros al servicio
        CuentaAhorrosServices cuentaService = new CuentaAhorrosServiceImpl(cuentaRepo);

        // 3. VISTAS (Capa de presentación e inyección de servicios)
        PersonView personView = new PersonView(personService);

        // LoginView requiere acceso al personRepo para validar existencia de usuarios
        LoginView loginView = new LoginView(loginService, personRepo);

        // Vista de transacciones bancarias
        CuentaAhorrosView cuentaView = new CuentaAhorrosView(cuentaService);

        // 4. APP PRINCIPAL
        // Ensamblamos el MenuApp con todas sus dependencias listas para usar
        return new MenuApp(personView, loginView, cuentaView);
    }
}