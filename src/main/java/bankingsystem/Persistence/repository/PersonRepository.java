package bankingsystem.Persistence.repository;

import bankingsystem.Persistence.database.DataBaseConnectionMySql;
import bankingsystem.domain.Person;
import bankingsystem.services.outputport.PersonaPersistencePort;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PersonRepository implements PersonaPersistencePort {
    // Lista en memoria para simular la base de datos
    private List<Person> persons = new ArrayList<>();

    public Person save(Person person) {
        persons.add(person);
        return person;
    }

    // Verifica si el nombre de usuario ya está tomado (Usado en Registro)
    public boolean existsByUsername(String username) {
        return persons.stream()
                .anyMatch(p -> p.getUsername().equalsIgnoreCase(username));
    }

    // BUSCADOR CLAVE: Encuentra a la persona por su usuario (Usado en Login)
    public Person findByUsername(String username) {
        return persons.stream()
                .filter(p -> p.getUsername().equalsIgnoreCase(username))
                .findFirst()
                .orElse(null);
    }

    // Retorna todos los registros
    public List<Person> findAll() {
        return new ArrayList<>(persons);
    }

    // Busca por ID (Identificación)
    public Person findById(int id) {
        return persons.stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElse(null);
    }

    // Elimina un registro por ID
    public boolean deleteById(int id) {
        return persons.removeIf(p -> p.getId() == id);
    }

    @Override
    public Person savePersona(Person person) {
        String sql = "INSERT INTO users (id, name, telephone, email, username, initialbalance, password) VALUES (?, ?, ?, ?, ?, ?, ?)";

        // 1. Obtenemos la conexión del Singleton SIN meterla en el try() para que no se cierre
        Connection conn = DataBaseConnectionMySql.getInstance().getConnection();

        // 2. Solo metemos el PreparedStatement en el try para liberar la consulta al terminar
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, person.getId());
            stmt.setString(2, person.getName());
            stmt.setString(3, person.getTelephone());
            stmt.setString(4, person.getEmail());
            stmt.setString(5, person.getUsername());
            stmt.setDouble(6, person.getInitialBalance());
            stmt.setString(7, person.getPassword());

            int rowsInserted = stmt.executeUpdate();

            if (rowsInserted > 0) {
                System.out.println("✅ [BD] ¡Usuario '" + person.getUsername() + "' registrado con éxito en MySQL!");
                return person;
            }

        } catch (SQLException e) {
            System.err.println("❌ [BD] Error crítico al intentar registrar al usuario: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Person findPersonaByUsername(String username) {
        String sql = "SELECT id_person, name, telephone, email, username, balance, password FROM users WHERE username = ?";

        // 1. Obtenemos la conexión sin destruirla al salir del método
        Connection conn = DataBaseConnectionMySql.getInstance().getConnection();

        // 2. Manejamos el ciclo de vida de la consulta y los resultados de forma segura
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Retornamos usando el constructor de 7 parámetros que añadimos a Person
                    return new Person(
                            rs.getInt("id_person"),
                            rs.getString("name"),
                            rs.getString("telephone"),
                            rs.getString("email"),
                            rs.getString("username"),
                            rs.getDouble("balance"),
                            rs.getString("password")
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ [BD] Error al buscar el usuario '" + username + "': " + e.getMessage());
            e.printStackTrace();
        }

        return null; // Si no lo encuentra o falla

    }

    @Override
    public List<Person> findAllPersonas() {
        return List.of();
    }

    @Override
    public Optional<Person> findPersonaByIdOptional(int id) {
        return Optional.empty();
    }

    @Override
    public Person findPersonaById(int id) {
        return null;
    }

    @Override
    public void deletePersona(int id) {

    }
}