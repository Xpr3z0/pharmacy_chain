package bdclient;

import java.sql.ResultSet;
import java.util.List;

public interface JDBCClient {
    boolean accessToDB(String login, String password);
    String getLogin();
    List<String> getTableNames();
    ResultSet getTable(String selectedTable);
    boolean updateTable(String selectedTable, String columnChangeName, String newRecord, String columnSearchName, String columnSearch);
    boolean deleteRowTable(String selectedTable, String columnSearchName, String columnSearch);
    boolean simpleQuery(String selectedTable, String sql);
}
