package bankingsystem.Config;

import bankingsystem.Persistence.repository.*;
import bankingsystem.services.*;
import bankingsystem.services.input.CuentaServices;
import bankingsystem.services.input.LoginService;
import bankingsystem.services.input.PersonService;
import bankingsystem.services.input.TarjetaCreditoServices;
import bankingsystem.view.CuentaView;
import bankingsystem.view.LoginView;
import bankingsystem.view.PersonView;
import bankingsystem.userinterface.MenuApp;

public class Config {

    public static MenuApp createMenuApp() {
        // 1. REPOSITORIOS
        PersonRepository personRepo = new PersonRepository();
        CuentaRepository cuentaGeneralRepo = new CuentaRepository();
        CuentaAhorrosRepository ahorrosRepo = new CuentaAhorrosRepository();
        CuentaCorrienteRepository corrienteRepo = new CuentaCorrienteRepository();
        TarjetaCreditoRepository tarjetaRepo = new TarjetaCreditoRepository();
        MovimientoRepository movimientoRepo = new MovimientoRepository();

        // 2. SERVICIOS
        CuentaServices cuentaService = new CuentaServicesImpl(
                cuentaGeneralRepo,
                ahorrosRepo,
                corrienteRepo,
                tarjetaRepo,
                movimientoRepo
        );

        PersonService personService = new PersonServiceImpl(personRepo, cuentaService);

        LoginService loginService = new LoginServiceImpl(personRepo);
        TarjetaCreditoServices tarjetaService = new TarjetaCreditoServiceImpl(tarjetaRepo, movimientoRepo);

        PersonView personView = new PersonView(personService);
        LoginView loginView = new LoginView(loginService, personRepo);

        CuentaView cuentaView = new CuentaView(cuentaService, tarjetaService);

        return new MenuApp(personView, loginView, cuentaView, cuentaService);
    }
}