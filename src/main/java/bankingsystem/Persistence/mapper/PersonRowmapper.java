package bankingsystem.Persistence.mapper;

import bankingsystem.domain.Person;
import java.sql.ResultSet;
import java.sql.SQLException;


public class PersonRowmapper implements RowMapper<Person> {

    @Override
    public Person mapRow(ResultSet rs) throws SQLException {

        Person person = new Person();


        person.setId(rs.getInt("id_person"));
        person.setName(rs.getString("name"));
        person.setTelephone(rs.getString("telephone"));
        person.setEmail(rs.getString("email"));
        person.setUsername(rs.getString("userName"));
        person.setPassword(rs.getString("userpassword"));
        person.setInitialBalance(rs.getDouble("initialBalance"));


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