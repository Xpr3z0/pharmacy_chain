package bdclient;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ClientPostgreSQL implements JDBCClient {
    private static ClientPostgreSQL instance;
    private Properties dbProperties;
    private String login = null;
    private String password = null;
    private String dbUrl = null;
    private String dbSchema = null;
    Connection externalConnection = null;

    public static ClientPostgreSQL getInstance() {
        return instance == null ? instance = new ClientPostgreSQL() : instance;
    }

    private ClientPostgreSQL() {
        try {
            dbProperties = getDbProperties(getClass().getResource("/properties/config.properties").openStream());
            if (dbProperties != null) {
                Class.forName(dbProperties.getProperty("db.driver"));
                dbUrl = dbProperties.getProperty("db.url");
                dbSchema = dbProperties.getProperty("db.schema");
            }
        } catch (FileNotFoundException e) {
            System.out.println("Не найден файл настроек");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Properties getDbProperties(InputStream configFileInput) {
        Properties property = new Properties();
        try {
            property.load(configFileInput);
            return property;
        } catch (FileNotFoundException e) {
            System.out.println("Не найден файл настроек");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean accessToDB(String login, String password) {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(dbUrl, login, password);
            connection.close();
            this.login = login;
            this.password = password;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        createTablesIfNeeded();
        return true;
    }

    @Override
    public String getLogin() {
        return login;
    }

    public Connection getConnection() throws SQLException {
        externalConnection = null;
        externalConnection = DriverManager.getConnection(dbUrl, login, password);
        return externalConnection;
    }

    public void closeConnection() throws SQLException {
        if (externalConnection != null) {
            externalConnection.close();
        }
    }

    @Override
    public List<String> getTableNames() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(dbUrl, login, password);
            PreparedStatement statement = connection.prepareStatement(dbProperties.getProperty("tableNamesSql"));
            statement.setString(1, dbSchema);
            ResultSet resultSet = statement.executeQuery();
            ArrayList<String> arrayList = new ArrayList<>();
            while (resultSet.next()) {
                arrayList.add(resultSet.getString(1));
            }
            return arrayList;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public ArrayList<String> stringListQuery(String query, String nameOfColToGet) {
        Connection connection = null;
        ResultSet resultSet;
        ArrayList<String> finalList = new ArrayList<>();
        try {

            connection = DriverManager.getConnection(dbUrl, login, password);
            PreparedStatement statement = connection.prepareStatement(query.trim());
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                finalList.add(resultSet.getString(nameOfColToGet));
            }
//            System.out.println(finalList);
            resultSet.close();
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return finalList;
    }


    public String getInfoByColoumnQuery(String query, String colName) {
        Connection connection = null;
        ResultSet resultSet;
        String result = null;

        try {
            connection = DriverManager.getConnection(dbUrl, login, password);
            PreparedStatement statement = connection.prepareStatement(query.trim());
            statement.setString(1, colName); // Подставьте значение id в запрос

            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                result = resultSet.getString(1); // Используйте соответствующий индекс или имя столбца
            }

//            System.out.println(result);

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return result;
    }


    @Override
    public ResultSet getTable(String selectedTable) {
        Connection connection = null;
        String orderBy = "";
        switch (selectedTable) {
            case "аптеки": orderBy = "order by id ASC"; break;
            case "препараты": orderBy = "order by id ASC"; break;
            case "учет": {
//                orderBy = "order by id_магазина ASC";
                break;
            }
            default:
                System.out.println("orderBy detection error");
        }
        try {
            String query = String.format(dbProperties.getProperty("getTableSql"), dbSchema, selectedTable, orderBy);
            connection = DriverManager.getConnection(dbUrl, login, password);
            PreparedStatement statement = connection.prepareStatement(query.toString());
            return statement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean updateTable(String selectedTable, String columnChangeName, String newRecord, String columnSearchName, String columnSearch) {
        Connection connection = null;
        try {
            String query = String.format(dbProperties.getProperty("updateTable"), dbSchema, selectedTable, columnChangeName, columnSearchName, columnSearch);
            connection = DriverManager.getConnection(dbUrl, login, password);
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setObject(1, convertStringToInteger(newRecord));
            return statement.executeUpdate() != -1 ? true : false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean deleteRowTable(String selectedTable, String columnSearchName, String columnSearch) {
        Connection connection = null;
        try {
            String query = String.format(dbProperties.getProperty("deleteRowTable"), dbSchema, selectedTable, columnSearchName, columnSearch);
            connection = DriverManager.getConnection(dbUrl, login, password);
            PreparedStatement statement = connection.prepareStatement(query);
            return statement.executeUpdate() != -1 ? true : false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean simpleQuery(String selectedTable, String sql) {
        Connection connection = null;
        try {
            String query = String.format(sql, dbSchema, selectedTable);
            connection = DriverManager.getConnection(dbUrl, login, password);
            PreparedStatement statement = connection.prepareStatement(query);
            return statement.executeUpdate() != -1 ? true : false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Object convertStringToInteger(String str) {
        try {
            return new Integer(str);
        } catch (NumberFormatException e) {
            return str;
        }
    }

// TODO: УДАЛИТЬ sout
    public void createTablesIfNeeded() {
        List<String> tableNames = getTableNames();
        Connection connection = null;

        if (tableNames == null || !tableNames.contains("аптеки")) {

            boolean created = simpleQuery("public", createPharmaciesTable);
            if (created) {
                System.out.println("Table 'аптеки' created.");
                boolean inserted = simpleQuery("public", insertIntoPharmacies);
                if (inserted) {
                    System.out.println("Initial data inserted into table 'аптеки'.");
                } else {
                    System.out.println("Error inserting initial data into table 'аптеки'.");
                }
            } else {
                System.out.println("Error creating table 'аптеки'.");
            }
        }

        if (tableNames == null || !tableNames.contains("препараты")) {
            boolean created = simpleQuery("public", createMedicinesTable);
            if (created) {
                System.out.println("Table 'препараты' created.");
                boolean inserted = simpleQuery("public", insertIntoMedicines);
                if (inserted) {
                    System.out.println("Initial data inserted into table 'препараты'.");
                } else {
                    System.out.println("Error inserting initial data into table 'препараты'.");
                }

            } else {
                System.out.println("Error creating table 'препараты'.");
            }
        }

        if (tableNames == null || !tableNames.contains("учет")) {
            boolean created = simpleQuery("public", createInventoryTable);
//            simpleQuery("public", "CREATE UNIQUE INDEX idx_название_адрес ON учет (название_книги, адрес_магазина)");
            if (created) {
                System.out.println("Table 'employees' created.");
                boolean inserted = simpleQuery("public", insertIntoInventory);
                if (inserted) {
                    System.out.println("Initial data inserted into table 'employees'.");
                } else {
                    System.out.println("Error inserting initial data into table 'employees'.");
                }

            } else {
                System.out.println("Error creating table 'employees'.");
            }
        }
    }


    /**
     * SQL запросы
     **/

    private final String createPharmaciesTable =
            "CREATE TABLE аптеки (" +
                    "id SERIAL PRIMARY KEY, " +
                    "адрес VARCHAR(255) UNIQUE, " +
                    "телефон VARCHAR(50), " +
                    "электронная_почта VARCHAR(100), " +
                    "рабочие_часы VARCHAR(100));";

    private final String createMedicinesTable =
            "CREATE TABLE препараты (" +
                    "id SERIAL PRIMARY KEY, " +
                    "название VARCHAR(255) UNIQUE, " +
                    "производитель VARCHAR(255), " +
                    "тип VARCHAR(100), " +
                    "цена DECIMAL(10, 2), " +
                    "форма_выпуска VARCHAR(100), " +
                    "рецептурный BOOLEAN);";


    private final String createInventoryTable =
            "CREATE TABLE учет (" +
                    "название_препарата VARCHAR(255), " +
                    "адрес_аптеки VARCHAR(255), " +
                    "количество_препаратов INT, " +
                    "PRIMARY KEY (название_препарата, адрес_аптеки), " +
                    "FOREIGN KEY (название_препарата) REFERENCES препараты(название), " +
                    "FOREIGN KEY (адрес_аптеки) REFERENCES аптеки(адрес));";


    private final String insertIntoPharmacies =
            "INSERT INTO аптеки (адрес, телефон, электронная_почта, рабочие_часы) VALUES " +
                    "('ул. Пушкина, 10', '123-456-7890', 'pushkin@pharmacy.com', '8:00 - 22:00'), " +
                    "('пр. Ленина, 20', '234-567-8901', 'lenin@pharmacy.com', '7:00 - 23:00'), " +
                    "('ул. Гоголя, 30', '345-678-9012', 'gogol@pharmacy.com', '8:00 - 22:00'), " +
                    "('ул. Чехова, 40', '456-789-0123', 'chekhov@pharmacy.com', '7:00 - 23:00'), " +
                    "('пр. Мира, 50', '567-890-1234', 'mir@pharmacy.com', '8:00 - 22:00'), " +
                    "('ул. Садовая, 60', '678-901-2345', 'sad@pharmacy.com', '7:00 - 23:00'), " +
                    "('ул. Лесная, 70', '789-012-3456', 'les@pharmacy.com', '8:00 - 22:00'), " +
                    "('ул. Озерная, 80', '890-123-4567', 'lake@pharmacy.com', '7:00 - 23:00'), " +
                    "('ул. Горная, 90', '901-234-5678', 'mount@pharmacy.com', '8:00 - 22:00'), " +
                    "('ул. Полевая, 100', '912-345-6789', 'field@pharmacy.com', '7:00 - 23:00'), " +
                    "('пр. Весенний, 110', '923-456-7890', 'spring@pharmacy.com', '8:00 - 22:00'), " +
                    "('ул. Зимняя, 120', '934-567-8901', 'winter@pharmacy.com', '7:00 - 23:00'), " +
                    "('пр. Осенний, 130', '945-678-9012', 'autumn@pharmacy.com', '8:00 - 22:00'), " +
                    "('ул. Летняя, 140', '956-789-0123', 'summer@pharmacy.com', '7:00 - 23:00'), " +
                    "('ул. Заречная, 150', '967-890-1234', 'river@pharmacy.com', '8:00 - 22:00'), " +
                    "('ул. Южная, 160', '978-901-2345', 'south@pharmacy.com', '7:00 - 23:00'), " +
                    "('ул. Северная, 170', '989-012-3456', 'north@pharmacy.com', '8:00 - 22:00'), " +
                    "('ул. Восточная, 180', '990-123-4567', 'east@pharmacy.com', '7:00 - 23:00'), " +
                    "('ул. Западная, 190', '991-234-5678', 'west@pharmacy.com', '8:00 - 22:00'), " +
                    "('ул. Центральная, 200', '992-345-6789', 'center@pharmacy.com', '7:00 - 23:00');";



    private final String insertIntoMedicines =
            "INSERT INTO препараты (название, производитель, тип, цена, форма_выпуска, рецептурный) VALUES " +
                    "('Аспирин', 'Bayer', 'Болеутоляющее', 50.00, 'Таблетки', FALSE), " +
                    "('Парацетамол', 'GSK', 'Жаропонижающее', 30.00, 'Таблетки', FALSE), " +
                    "('Амоксициллин', 'Sandoz', 'Антибиотик', 70.00, 'Капсулы', TRUE), " +
                    "('Ибупрофен', 'Advil', 'Противовоспалительное', 45.00, 'Таблетки', FALSE), " +
                    "('Омепразол', 'Prilosec', 'Противоязвенное', 100.00, 'Капсулы', TRUE), " +
                    "('Лоратадин', 'Claritin', 'Противоаллергическое', 35.00, 'Таблетки', FALSE), " +
                    "('Метформин', 'Glucophage', 'Противодиабетическое', 50.00, 'Таблетки', TRUE), " +
                    "('Атенолол', 'Tenormin', 'Противогипертензивное', 60.00, 'Таблетки', TRUE), " +
                    "('Фуросемид', 'Lasix', 'Мочегонное', 40.00, 'Таблетки', TRUE), " +
                    "('Варфарин', 'Coumadin', 'Антикоагулянт', 80.00, 'Таблетки', TRUE), " +
                    "('Глибенкламид', 'Diabeta', 'Противодиабетическое', 55.00, 'Таблетки', TRUE), " +
                    "('Лизиноприл', 'Prinivil', 'Противогипертензивное', 60.00, 'Таблетки', TRUE), " +
                    "('Левотироксин', 'Synthroid', 'Гормональное', 120.00, 'Таблетки', TRUE), " +
                    "('Симвастатин', 'Zocor', 'Гиполипидемическое', 90.00, 'Таблетки', TRUE), " +
                    "('Азитромицин', 'Zithromax', 'Антибиотик', 80.00, 'Капсулы', TRUE), " +
                    "('Алипразолам', 'Xanax', 'Транквилизатор', 110.00, 'Таблетки', TRUE), " +
                    "('Гидрохлортиазид', 'Microzide', 'Мочегонное', 40.00, 'Таблетки', TRUE), " +
                    "('Цетиризин', 'Zyrtec', 'Противоаллергическое', 35.00, 'Таблетки', FALSE), " +
                    "('Напроксен', 'Aleve', 'Противовоспалительное', 47.00, 'Таблетки', FALSE), " +
                    "('Дексаметазон', 'Decadron', 'Гормональное', 130.00, 'Таблетки', TRUE);";

    private final String insertIntoInventory =
            "INSERT INTO учет (название_препарата, адрес_аптеки, количество_препаратов) VALUES " +
                    "('Аспирин', 'ул. Пушкина, 10', 20), " +
                    "('Парацетамол', 'пр. Ленина, 20', 15), " +
                    "('Амоксициллин', 'ул. Гоголя, 30', 10), " +
                    "('Ибупрофен', 'ул. Чехова, 40', 25), " +
                    "('Омепразол', 'пр. Мира, 50', 30), " +
                    "('Лоратадин', 'ул. Садовая, 60', 18), " +
                    "('Метформин', 'ул. Лесная, 70', 22), " +
                    "('Атенолол', 'ул. Озерная, 80', 12), " +
                    "('Фуросемид', 'ул. Горная, 90', 8), " +
                    "('Варфарин', 'ул. Полевая, 100', 16), " +
                    "('Глибенкламид', 'пр. Весенний, 110', 14), " +
                    "('Лизиноприл', 'ул. Зимняя, 120', 20), " +
                    "('Левотироксин', 'пр. Осенний, 130', 9), " +
                    "('Симвастатин', 'ул. Летняя, 140', 30), " +
                    "('Азитромицин', 'ул. Заречная, 150', 15), " +
                    "('Алипразолам', 'ул. Южная, 160', 10), " +
                    "('Гидрохлортиазид', 'ул. Северная, 170', 20), " +
                    "('Цетиризин', 'ул. Восточная, 180', 25), " +
                    "('Напроксен', 'ул. Западная, 190', 12), " +
                    "('Дексаметазон', 'ул. Центральная, 200', 14);";

}