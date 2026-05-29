package bankingsystem.Persistence.repository;

import bankingsystem.Persistence.database.DataBaseConnectionMySql;
import bankingsystem.Persistence.mapper.PersonRowmapper;
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
    private final PersonRowmapper rowmapper;

    public PersonRepositoryAdapterMySql(Connection dbconnection, PersonRowmapper rowmapper) {
        this.dbconnection = dbconnection;
        this.rowmapper = rowmapper;
    }

    @Override
    public Person savePersona(Person person) {
        String sql = "INSERT INTO person (id_person, name, telephone, email, userName, initialBalance, userpassword, is_blocked, failed_attempts) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = dbconnection.prepareStatement(sql)) {

            setPersonParams(ps, person);

            ps.setTimestamp(8, null);
            ps.setInt(9, 0);

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
        // 🛠️ Especificamos las columnas exactas en lugar de usar '*'
        String sql = "SELECT id_person, name, telephone, email, userName, userpassword, initialBalance, is_blocked, failed_attempts FROM person";
        try (PreparedStatement ps = dbconnection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // 🛠️ SOLUCIÓN DEFINITIVA: Mapeamos a mano para que el login no falle jamás por culpa del Rowmapper
                Person person = new Person();
                person.setId(rs.getInt("id_person"));
                person.setName(rs.getString("name"));
                person.setTelephone(rs.getString("telephone"));
                person.setEmail(rs.getString("email"));
                person.setUsername(rs.getString("userName"));
                person.setInitialBalance(rs.getDouble("initialBalance"));
                person.setPassword(rs.getString("userpassword"));
                person.setFailedLoginAttempts(rs.getInt("failed_attempts"));

                java.sql.Timestamp timestamp = rs.getTimestamp("is_blocked");
                if (timestamp != null) {
                    person.setBlockedUntil(timestamp.toLocalDateTime());
                }
                persons.add(person);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar personas: " + e.getMessage(), e);
        }
        return persons;
    }

    @Override
    public Optional<Person> findPersonaByIdOptional(int id) {
        String sql = "SELECT id_person, name, telephone, email, userName, userpassword, initialBalance, is_blocked, failed_attempts FROM person WHERE id_person = ?";
        try (PreparedStatement ps = dbconnection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Person person = new Person();
                    person.setId(rs.getInt("id_person"));
                    person.setName(rs.getString("name"));
                    person.setTelephone(rs.getString("telephone"));
                    person.setEmail(rs.getString("email"));
                    person.setUsername(rs.getString("userName"));
                    person.setInitialBalance(rs.getDouble("initialBalance"));
                    person.setPassword(rs.getString("userpassword"));
                    person.setFailedLoginAttempts(rs.getInt("failed_attempts"));

                    java.sql.Timestamp timestamp = rs.getTimestamp("is_blocked");
                    if (timestamp != null) {
                        person.setBlockedUntil(timestamp.toLocalDateTime());
                    }
                    return Optional.of(person);
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
        String sql = "SELECT id_person, name, telephone, email, userName, userpassword, initialBalance, is_blocked, failed_attempts FROM person WHERE userName = ?";

        try (PreparedStatement ps = dbconnection.prepareStatement(sql)) {
            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Person person = new Person();

                    person.setId(rs.getInt("id_person"));
                    person.setName(rs.getString("name"));
                    person.setTelephone(rs.getString("telephone"));
                    person.setEmail(rs.getString("email"));
                    person.setUsername(rs.getString("userName"));
                    person.setInitialBalance(rs.getDouble("initialBalance"));
                    person.setPassword(rs.getString("userpassword"));

                    // Seguridad
                    person.setFailedLoginAttempts(rs.getInt("failed_attempts"));
                    java.sql.Timestamp timestamp = rs.getTimestamp("is_blocked");
                    if (timestamp != null) {
                        person.setBlockedUntil(timestamp.toLocalDateTime());
                    } else {
                        person.setBlockedUntil(null);
                    }

                    return person;
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
        String sql = """
                UPDATE person
                SET id_person = ?, name = ?, telephone = ?, email = ?, userName = ?, initialBalance = ?, userpassword = ?, failed_attempts = ?, is_blocked = ?
                WHERE id_person = ?""";

        try (PreparedStatement ps = dbconnection.prepareStatement(sql)) {
            setPersonParams(ps, person);

            ps.setInt(8, person.getFailedLoginAttempts());

            if (person.getBlockedUntil() != null) {
                ps.setTimestamp(9, java.sql.Timestamp.valueOf(person.getBlockedUntil()));
            } else {
                ps.setTimestamp(9, null);
            }

            ps.setInt(10, person.getId());

            ps.executeUpdate();
            System.out.println("✅ ¡Usuario actualizado correctamente usando setPersonParams!");
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar la persona: " + e.getMessage(), e);
        }
        return person;
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
        ps.setString(2, person.getName());
        ps.setString(3, person.getTelephone());
        ps.setString(4, person.getEmail());
        ps.setString(5, person.getUsername());
        ps.setDouble(6, person.getInitialBalance());
        ps.setString(7, person.getPassword());
    }
}