package bankingsystem.Persistence.mapper;
// Interfaz genérica para mapear filas de un ResultSet a objetos de dominio
public interface RowMapper<T> {

    T mapRow(java.sql.ResultSet rs) throws java.sql.SQLException;

}
