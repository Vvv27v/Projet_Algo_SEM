import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String DATABASE_URL = "jdbc:sqlite:projet_batiments.db";

    public DatabaseManager() {
        initializeDatabase();
    }

    public void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            if (conn != null) {
                createTables(conn);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTables(Connection conn) throws SQLException {
        String sqlBatiments = "CREATE TABLE IF NOT EXISTS batiments (" +
                "id INTEGER PRIMARY KEY," +
                "nom TEXT NOT NULL," +
                "type TEXT NOT NULL," +
                "nombre_etages INTEGER NOT NULL," +
                "details TEXT)";

        String sqlConsommation = "CREATE TABLE IF NOT EXISTS consommations (" +
                "id INTEGER PRIMARY KEY," +
                "batiment_id INTEGER NOT NULL," +
                "type TEXT NOT NULL," +
                "quantite INTEGER NOT NULL," +
                "FOREIGN KEY(batiment_id) REFERENCES batiments(id) ON DELETE CASCADE)";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sqlBatiments);
            stmt.execute(sqlConsommation);
        }
    }

    public void saveBatiment(Batiment batiment, String details) {
        String sql = "INSERT OR REPLACE INTO batiments (id, nom, type, nombre_etages, details) VALUES(?,?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, batiment.getId());
            pstmt.setString(2, batiment.getNom());
            pstmt.setString(3, batiment.getType());
            pstmt.setInt(4, batiment.getNombreEtages());
            pstmt.setString(5, details);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveConsommation(Consommation_Energie consommation) {
        String sql = "INSERT OR REPLACE INTO consommations (id, batiment_id, type, quantite) VALUES(?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, consommation.getId());
            pstmt.setInt(2, consommation.getBatimentId());
            pstmt.setString(3, consommation.getType().name());
            pstmt.setInt(4, consommation.getQuantité());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Consommation_Energie> getConsommationsByBatiment(int batimentId) {
        List<Consommation_Energie> consommations = new ArrayList<>();
        String sql = "SELECT id, batiment_id, type, quantite FROM consommations WHERE batiment_id = ?";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, batimentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Consommation_Energie c = new Consommation_Energie(
                            rs.getInt("id"),
                            rs.getInt("batiment_id"),
                            TypeConsommation.valueOf(rs.getString("type")),
                            rs.getInt("quantite")
                    );
                    consommations.add(c);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return consommations;
    }

    public void deleteConsommation(int consommationId) {
        String sql = "DELETE FROM consommations WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, consommationId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteBatiment(int batimentId) {
        String sql = "DELETE FROM batiments WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, batimentId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void clearDatabase() {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM consommations");
            stmt.execute("DELETE FROM batiments");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
