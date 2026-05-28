package bankingsystem.Persistence.mapper;

import bankingsystem.domain.Person;

public class PersonRowmapper implements RowMapper<Person> {

    /// Este método se encarga de mapear una fila del ResultSet a un objeto Person.
    @Override
    public Person mapRow(java.sql.ResultSet rs) throws java.sql.SQLException {

        Person person = new Person();

        person.setId(rs.getInt("id_person"));
        person.setName(rs.getString("name"));
        person.setTelephone(rs.getString("last_name"));
        person.setEmail(rs.getString("email"));
        person.setInitialBalance(rs.getDouble("initial_balance"));
        person.setPassword(rs.getString("password"));

        // Aquí se asume que el campo "status" en la base de datos es un booleano que indica si la persona está activa o no.
        return person;

    }
}
