import java.sql.*;

public class Migration {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/nahsaz_library?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String pass = "";

        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Statement stmt = conn.createStatement()) {
            
            System.out.println("Checking for 'cover' column in 'books' table...");
            
            // Check if column exists
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getColumns(null, null, "books", "cover");
            
            if (!rs.next()) {
                System.out.println("Column 'cover' does not exist. Adding it...");
                stmt.execute("ALTER TABLE books ADD COLUMN cover VARCHAR(255) DEFAULT NULL");
                System.out.println("Column 'cover' added successfully!");
            } else {
                System.out.println("Column 'cover' already exists.");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
