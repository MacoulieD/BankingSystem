package bankingsystem.Config;

import bankingsystem.Persistence.database.DataBaseConnectionMySql;
import bankingsystem.Persistence.mapper.*;
import bankingsystem.Persistence.repository.*;
import bankingsystem.services.*;
import bankingsystem.services.input.CuentaServices;
import bankingsystem.services.input.LoginService;
import bankingsystem.services.input.PersonService;
import bankingsystem.services.input.TarjetaCreditoServices;
import bankingsystem.services.outputport.*;
import bankingsystem.view.CuentaView;
import bankingsystem.view.LoginView;
import bankingsystem.view.PersonView;
import bankingsystem.userinterface.MenuApp;

import java.sql.Connection;

public class Config {

    public static MenuApp createMenuApp() {

        // 1. CONEXIÓN
        Connection dbConnection = DataBaseConnectionMySql.getInstance().getConnection();

        // 2. MAPPERS
        PersonRowmapper          personRowmapper       = new PersonRowmapper();
        CuentaAhorrosRowMapper   ahorrosRowMapper      = new CuentaAhorrosRowMapper();
        CuentaCorrienteRowMapper corrienteRowMapper    = new CuentaCorrienteRowMapper();
        TarjetaCreditoRowMapper  tarjetaRowMapper      = new TarjetaCreditoRowMapper();
        MovimientoRowMapper      movimientoRowMapper   = new MovimientoRowMapper();

        // 3. REPOSITORIOS — todos en MySQL
        PersonaPersistencePort         personaRepo               = new PersonRepositoryAdapterMySql(dbConnection, personRowmapper);
        CuentaAhorrosPersistencePort   ahorrosRepo               = new CuentaAhorrosRepositoryAdapterMySql(dbConnection, ahorrosRowMapper);
        CuentaCorrientePersistencePort corrienteRepo             = new CuentaCorrienteRepositoryAdapterMySql(dbConnection, corrienteRowMapper);
        TarjetaCreditoPersistencePort  tarjetaRepo               = new TarjetaCreditoRepositoryAdapterMySql(dbConnection, tarjetaRowMapper);
        MovimientoPersistencePort      movimientoPersistencePort = new MovimientoRepositoryAdapterMySql(dbConnection, movimientoRowMapper);

        // 4. Repositorios en memoria
        CuentaRepository     cuentaGeneralRepo = new CuentaRepository();
        MovimientoRepository movimientoRepo    = new MovimientoRepository();

        // 5. SERVICIOS
        CuentaServices cuentaService = new CuentaServicesImpl(
                cuentaGeneralRepo,
                ahorrosRepo,
                corrienteRepo,
                tarjetaRepo,
                movimientoRepo,
                movimientoPersistencePort
        );

        PersonService personService = new PersonServiceImpl(personaRepo, cuentaService);

        // ← LoginServiceImpl recibe 5 argumentos — sincroniza BD con memoria al hacer login
        LoginService loginService = new LoginServiceImpl(
                personaRepo,
                cuentaGeneralRepo,
                ahorrosRepo,
                corrienteRepo,
                tarjetaRepo
        );

        // ← TarjetaCreditoServiceImpl recibe 3 argumentos
        TarjetaCreditoServices tarjetaService = new TarjetaCreditoServiceImpl(
                tarjetaRepo,
                movimientoRepo,
                movimientoPersistencePort
        );

        // 6. VISTAS
        PersonView personView = new PersonView(personService);
        LoginView  loginView  = new LoginView(loginService, personaRepo);
        CuentaView cuentaView = new CuentaView(cuentaService, tarjetaService);

        return new MenuApp(personView, loginView, cuentaView, cuentaService);
    }
}