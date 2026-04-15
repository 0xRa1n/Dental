	package passanduser;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Dbconnection {

    private static final String URL = "jdbc:sqlite:Database.db";

    // 
    public static Connection getConnection() {
        try {
            Connection con = DriverManager.getConnection(URL);
            return con;
        } catch (SQLException e) {
            System.out.println("Connection failed!");
            e.printStackTrace();
            return null;
        }
    }
}	