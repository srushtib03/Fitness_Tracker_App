package fitness;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnector {
    private static final String URL = "jdbc:sqlite:fitness.db";
    private static Connection connection;

    public static Connection connect() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL);
                System.out.println("Connected to SQLite database!");
                initializeDatabase();
            }
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
        }
        return connection;
    }

    private static void initializeDatabase() {
        try {
            String usersTable = "CREATE TABLE IF NOT EXISTS users ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "username TEXT UNIQUE,"
                    + "password TEXT"
                    + ");";
            String activitiesTable = "CREATE TABLE IF NOT EXISTS activities ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "user_id INTEGER,"
                    + "date TEXT,"
                    + "steps INTEGER DEFAULT 0,"
                    + "calories INTEGER DEFAULT 0,"
                    + "duration INTEGER DEFAULT 0,"
                    + "bmi REAL,"
                    + "bmi_category TEXT,"
                    + "FOREIGN KEY(user_id) REFERENCES users(id)"
                    + ");";
            String profileTable = "CREATE TABLE IF NOT EXISTS user_profiles ("
                    + "user_id INTEGER PRIMARY KEY,"
                    + "age INTEGER,"
                    + "gender TEXT,"
                    + "height REAL,"
                    + "weight REAL,"
                    + "goal_steps INTEGER,"
                    + "goal_calories INTEGER,"
                    + "goal_bmi REAL,"
                    + "FOREIGN KEY(user_id) REFERENCES users(id));";
            String waterTable = "CREATE TABLE IF NOT EXISTS water_log ("
                    + "user_id INTEGER,"
                    + "date TEXT,"
                    + "water INTEGER,"
                    + "PRIMARY KEY(user_id, date),"
                    + "FOREIGN KEY(user_id) REFERENCES users(id));";
            String sleepTable = "CREATE TABLE IF NOT EXISTS sleep_log (" +
                    "user_id INTEGER," +
                    "date TEXT," +
                    "sleep_time TEXT," +
                    "wake_time TEXT," +
                    "PRIMARY KEY(user_id, date)," +
                    "FOREIGN KEY(user_id) REFERENCES users(id));";
            connection.createStatement().execute(sleepTable);
        
            // (Add more CREATE TABLE statements for Sleep/Nutrition/Reminders here if new tabs are added later)

            connection.createStatement().execute(usersTable);
            connection.createStatement().execute(activitiesTable);
            connection.createStatement().execute(profileTable);
            connection.createStatement().execute(waterTable);
        } catch (SQLException e) {
            System.out.println("Failed initializing database: " + e.getMessage());
        }
    }
}