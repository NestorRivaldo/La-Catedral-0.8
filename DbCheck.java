import java.sql.*;

public class DbCheck {
    public static void main(String[] args) {
        String url = "jdbc:sqlite:data/sysventas.db";
        String query = "SELECT id_producto, nombre, pu, stock FROM upeu_producto";

        try (Connection conn = DriverManager.getConnection(url);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            System.out.println("--- PRODUCTOS EN BASE DE DATOS ---");
            System.out.println(String.format("%-5s %-30s %-10s %-5s", "ID", "NOMBRE", "PRECIO", "STOCK"));
            System.out.println("-------------------------------------------------------");

            while (rs.next()) {
                System.out.println(String.format("%-5d %-30s %-10.2f %-5.0f",
                        rs.getInt("id_producto"),
                        rs.getString("nombre"),
                        rs.getDouble("pu"),
                        rs.getDouble("stock")));
            }
            System.out.println("-------------------------------------------------------");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
