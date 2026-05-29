package bankingsystem.Config;

import bankingsystem.Persistence.database.DataBaseConnectionMySql;
import bankingsystem.Persistence.mapper.CuentaAhorrosRowMapper;
import bankingsystem.Persistence.mapper.PersonRowmapper;
import bankingsystem.Persistence.repository.*;
import bankingsystem.services.*;
import bankingsystem.services.input.CuentaServices;
import bankingsystem.services.input.LoginService;
import bankingsystem.services.input.PersonService;
import bankingsystem.services.input.TarjetaCreditoServices;
import bankingsystem.services.outputport.CuentaAhorrosPersistencePort;
import bankingsystem.services.outputport.PersonaPersistencePort;
import bankingsystem.view.CuentaView;
import bankingsystem.view.LoginView;
import bankingsystem.view.PersonView;
import bankingsystem.userinterface.MenuApp;

import java.sql.Connection;

public class Config {

    public static MenuApp createMenuApp() {

        Connection dbConnection = DataBaseConnectionMySql.getInstance().getConnection();

        PersonRowmapper personRowmapper = new PersonRowmapper();
        CuentaAhorrosRowMapper cuentaAhorrosRowMapper = new CuentaAhorrosRowMapper();

        // 1. REPOSITORIOS
        PersonRepository personRepo = new PersonRepository();
        CuentaRepository cuentaGeneralRepo = new CuentaRepository();
        CuentaCorrienteRepository corrienteRepo = new CuentaCorrienteRepository();
        TarjetaCreditoRepository tarjetaRepo = new TarjetaCreditoRepository();
        MovimientoRepository movimientoRepo = new MovimientoRepository();

        // 2. ADAPTADOR MYSQL CUENTA AHORROS
        CuentaAhorrosPersistencePort ahorrosRepo = new CuentaAhorrosRepositoryAdapterMySql(dbConnection, cuentaAhorrosRowMapper);

        // 3. SERVICIOS (cuentaService antes que personService)
        CuentaServices cuentaService = new CuentaServicesImpl(
                cuentaGeneralRepo,
                ahorrosRepo,
                corrienteRepo,
                tarjetaRepo,
                movimientoRepo
        );

        PersonaPersistencePort personaPersistencePortDB = new PersonRepositoryAdapterMySql(dbConnection, personRowmapper);
        PersonService personService = new PersonServiceImpl(personaPersistencePortDB, cuentaService);
        PersonView personView = new PersonView(personService);

        LoginService loginService = new LoginServiceImpl(personRepo);
        TarjetaCreditoServices tarjetaService = new TarjetaCreditoServiceImpl(tarjetaRepo, movimientoRepo);

        LoginView loginView = new LoginView(loginService, personRepo);
        CuentaView cuentaView = new CuentaView(cuentaService, tarjetaService);

        return new MenuApp(personView, loginView, cuentaView, cuentaService);
    }
}
