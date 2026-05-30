package bankingsystem.Config;

import bankingsystem.Persistence.database.DataBaseConnectionMySql;
import bankingsystem.Persistence.mapper.PersonRowmapper;
import bankingsystem.Persistence.mapper.CuentaAhorrosRowMapper;
import bankingsystem.Persistence.mapper.CuentaCorrienteRowMapper;
import bankingsystem.Persistence.mapper.TarjetaCreditoRowMapper; // ✅ INTERCAMBIO/ADICIÓN: Nuevo Mapper
import bankingsystem.Persistence.mapper.MovimientoRowMapper;     // ✅ Mapper movimientos
import bankingsystem.Persistence.repository.*;
import bankingsystem.services.*;
import bankingsystem.services.input.CuentaServices;
import bankingsystem.services.input.LoginService;
import bankingsystem.services.input.PersonService;
import bankingsystem.services.input.TarjetaCreditoServices;
import bankingsystem.services.outputport.PersonaPersistencePort;
import bankingsystem.services.outputport.CuentaAhorrosPersistencePort;
import bankingsystem.services.outputport.CuentaCorrientePersistencePort;
import bankingsystem.services.outputport.TarjetaCreditoPersistencePort; // ✅ INTERCAMBIO/ADICIÓN: Nuevo Puerto
import bankingsystem.services.outputport.MovimientoPersistencePort;     // ✅ Puerto movimientos BD
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
        CuentaCorrienteRowMapper cuentaCorrienteRowMapper = new CuentaCorrienteRowMapper();
        TarjetaCreditoRowMapper tarjetaCreditoRowMapper = new TarjetaCreditoRowMapper(); // ✅ Mantenemos orden de mappers

        PersonaPersistencePort personaPersistencePort = new PersonRepository();
        PersonaPersistencePort personaPersistencePortDB = new PersonRepositoryAdapterMySql(dbConnection, personRowmapper);

        PersonServiceImpl personService = new PersonServiceImpl(personaPersistencePortDB, null);
        PersonView personView = new PersonView(personService);

        LoginService loginService = new LoginServiceImpl(personaPersistencePortDB);

        // 1. REPOSITORIOS
        PersonRepository personRepo = new PersonRepository();
        CuentaRepository cuentaGeneralRepo = new CuentaRepository();
        CuentaAhorrosPersistencePort ahorrosRepo = new CuentaAhorrosRepositoryAdapterMySql(dbConnection, cuentaAhorrosRowMapper);
        CuentaCorrientePersistencePort corrienteRepo = new CuentaCorrienteRepositoryAdapterMySql(dbConnection, cuentaCorrienteRowMapper);

        // 🔥 CORREGIDO: Reemplazamos la persistencia en memoria por el adaptador MySQL mediante su puerto
        TarjetaCreditoPersistencePort tarjetaRepo = new TarjetaCreditoRepositoryAdapterMySql(dbConnection, tarjetaCreditoRowMapper);

        MovimientoRepository movimientoRepo = new MovimientoRepository();

        // ✅ Adaptador MySQL para movimientos (puerto de salida → BD)
        MovimientoRowMapper movimientoRowMapper         = new MovimientoRowMapper();
        MovimientoPersistencePort movimientoPersistencePort =
                new MovimientoRepositoryAdapterMySql(dbConnection, movimientoRowMapper);

        // 2. SERVICIOS
        CuentaServices cuentaService = new CuentaServicesImpl(
                cuentaGeneralRepo,
                ahorrosRepo,
                corrienteRepo,
                tarjetaRepo, // ✅ Pasará limpio ya que CuentaServicesImpl ahora espera el puerto
                movimientoRepo,
                movimientoPersistencePort  // ✅ Inyectamos el puerto de movimientos
        );

        // Si tu TarjetaCreditoServiceImpl ya fue migrado para recibir TarjetaCreditoPersistencePort, compilará perfecto aquí:
        TarjetaCreditoServices tarjetaService = new TarjetaCreditoServiceImpl(tarjetaRepo, movimientoRepo);

        personService.setCuentaService(cuentaService);

        LoginView loginView = new LoginView(loginService, personaPersistencePortDB);

        CuentaView cuentaView = new CuentaView(cuentaService, tarjetaService);

        return new MenuApp(personView, loginView, cuentaView, cuentaService);
    }
}