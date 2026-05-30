package bankingsystem.Persistence.repository;

import bankingsystem.Persistence.mapper.RowMapper; // 👈 Importas la interfaz genérica
import bankingsystem.domain.Person;
import bankingsystem.services.outputport.PersonaPersistencePort;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PersonRepositoryAdapterMySql implements PersonaPersistencePort {
    private final Connection dbconnection;
    private final RowMapper<Person> rowmapper;

    public PersonRepositoryAdapterMySql(Connection dbconnection, RowMapper<Person> rowmapper) {
        this.dbconnection = dbconnection;
        this.rowmapper = rowmapper;
    }

    @Override
    public Person savePersona(Person person) {
        String sql = "INSERT INTO person (id_person, name, telephone, email, userName, userpassword, is_blocked, failed_attempts) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = dbconnection.prepareStatement(sql)) {
            setPersonParams(ps, person);
            ps.setTimestamp(7, null);
            ps.setInt(8, 0);
            ps.executeUpdate();
            System.out.println("✅ ¡Usuario registrado exitosamente!");
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar la persona: " + e.getMessage(), e);
        }
        return person;
    }

    @Override
    public List<Person> findAllPersonas() {
        List<Person> persons = new ArrayList<>();
        String sql = "SELECT id_person, name, telephone, email, userName, userpassword, is_blocked, failed_attempts FROM person";
        try (PreparedStatement ps = dbconnection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                // 🛠️ AQUÍ SE USA: Ya no hay código duplicado a mano
                persons.add(rowmapper.mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar personas: " + e.getMessage(), e);
        }
        return persons;
    }

    @Override
    public Optional<Person> findPersonaByIdOptional(int id) {
        String sql = "SELECT id_person, name, telephone, email, userName, userpassword, is_blocked, failed_attempts FROM person WHERE id_person = ?";
        try (PreparedStatement ps = dbconnection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // 🛠️ AQUÍ SE USA: Retorna el mapeo de la interfaz directamente
                    return Optional.of(rowmapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar persona por id: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Person> findPersonaById(int id) {
        return findPersonaByIdOptional(id);
    }

    @Override
    public Person findPersonaByUsername(String username) {
        return findByUsername(username);
    }

    @Override
    public Person findByUsername(String username) {
        String sql = "SELECT id_person, name, telephone, email, userName, userpassword, is_blocked, failed_attempts FROM person WHERE LOWER(TRIM(userName)) = LOWER(TRIM(?))";
        try (PreparedStatement ps = dbconnection.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // 🛠️ AQUÍ SE USA: El login ahora pasa directamente por el traductor genérico
                    return rowmapper.mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar el usuario por userName: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public Person updatePerson(int id, String name, String telephone, String email, String username, double initialBalance, String password) {
        Person p = new Person();
        p.setId(id);
        p.setName(name);
        p.setTelephone(telephone);
        p.setEmail(email);
        p.setUsername(username);
        p.setInitialBalance(initialBalance);
        p.setPassword(password);
        return updatePerson(p);
    }

    @Override
    public Person updatePerson(Person person) {
        // Busca el username actual en BD para detectar si cambió
        String usernameActualEnBD = null;
        try {
            Optional<Person> actual = findPersonaByIdOptional(person.getId());
            if (actual.isPresent()) {
                usernameActualEnBD = actual.get().getUsername();
            }
        } catch (Exception ignored) {}

        boolean usernameChanged = usernameActualEnBD != null
                && !usernameActualEnBD.equalsIgnoreCase(person.getUsername());

        String sql = """
                UPDATE person
                SET id_person = ?, name = ?, telephone = ?, email = ?, userName = ?, userpassword = ?, failed_attempts = ?, is_blocked = ?
                WHERE id_person = ?""";
        try {
            // ✅ Si el username cambia, desactiva FK para evitar constraint violation
            if (usernameChanged) {
                dbconnection.createStatement().execute("SET FOREIGN_KEY_CHECKS = 0");
            }

            try (PreparedStatement ps = dbconnection.prepareStatement(sql)) {
                setPersonParams(ps, person);
                ps.setInt(7, person.getFailedLoginAttempts());
                if (person.getBlockedUntil() != null) {
                    ps.setTimestamp(8, java.sql.Timestamp.valueOf(person.getBlockedUntil()));
                } else {
                    ps.setTimestamp(8, null);
                }
                ps.setInt(9, person.getId());
                ps.executeUpdate();
            }

            // ✅ Actualiza propietario en todas las tablas de cuentas
            if (usernameChanged) {
                String[] tablas = {"cuentas", "cuenta_ahorros", "cuenta_corriente", "tarjetas_credito"};
                for (String tabla : tablas) {
                    String sqlCuenta = "UPDATE " + tabla + " SET propietario = ? WHERE propietario = ?";
                    try (PreparedStatement ps = dbconnection.prepareStatement(sqlCuenta)) {
                        ps.setString(1, person.getUsername().trim().toLowerCase());
                        ps.setString(2, usernameActualEnBD.trim().toLowerCase());
                        ps.executeUpdate();
                    }
                }
                dbconnection.createStatement().execute("SET FOREIGN_KEY_CHECKS = 1");
            }

            System.out.println("✅ ¡Usuario actualizado correctamente!");
        } catch (SQLException e) {
            try { dbconnection.createStatement().execute("SET FOREIGN_KEY_CHECKS = 1"); } catch (SQLException ignored) {}
            throw new RuntimeException("Error al actualizar la persona: " + e.getMessage(), e);
        }
        return person;
    }

    // ── Criterio 2: actualiza propietario en todas las tablas de cuentas ──────
    @Override
    public void updatePropietarioEnCuentas(String usernameAnterior, String usernameNuevo) {
        try {
            // 1. Desactiva FK temporalmente para poder actualizar sin conflicto
            dbconnection.createStatement().execute("SET FOREIGN_KEY_CHECKS = 0");

            String[] tablas = {"cuentas", "cuenta_ahorros", "cuenta_corriente", "tarjetas_credito"};
            for (String tabla : tablas) {
                String sql = "UPDATE " + tabla + " SET propietario = ? WHERE propietario = ?";
                try (PreparedStatement ps = dbconnection.prepareStatement(sql)) {
                    ps.setString(1, usernameNuevo.trim().toLowerCase());
                    ps.setString(2, usernameAnterior.trim().toLowerCase());
                    ps.executeUpdate();
                }
            }

            // 2. Reactiva FK checks
            dbconnection.createStatement().execute("SET FOREIGN_KEY_CHECKS = 1");

        } catch (SQLException e) {
            try { dbconnection.createStatement().execute("SET FOREIGN_KEY_CHECKS = 1"); } catch (SQLException ignored) {}
            throw new RuntimeException("Error al actualizar propietario en cuentas: " + e.getMessage(), e);
        }
    }

    @Override
    public void deletePersona(int id) {
        String sql = "DELETE FROM person WHERE id_person = ?";
        try (PreparedStatement ps = dbconnection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar la persona: " + e.getMessage(), e);
        }
    }

    private void setPersonParams(PreparedStatement ps, Person person) throws SQLException {
        ps.setInt(1, person.getId());
        ps.setString(2, person.getName().trim());
        ps.setString(3, person.getTelephone().trim());
        ps.setString(4, person.getEmail().trim());
        ps.setString(5, person.getUsername().trim().toLowerCase());
        ps.setString(6, person.getPassword().trim());
    }
}
