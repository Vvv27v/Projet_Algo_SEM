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
                syncConsommationNextId(conn);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void syncConsommationNextId(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT MAX(id) FROM consommations")) {
            if (rs.next() && rs.getObject(1) != null) {
                Consommation_Energie.initNextId(rs.getInt(1) + 1);
            }
        }
    }

    private void createTables(Connection conn) throws SQLException {
        String sqlUsers = "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY," +
                "email TEXT NOT NULL UNIQUE," +
                "nom TEXT NOT NULL," +
                "prenom TEXT NOT NULL," +
                "telephone TEXT NOT NULL," +
                "password TEXT NOT NULL," +
                "verified INTEGER DEFAULT 0)";

        String sqlBatiments = "CREATE TABLE IF NOT EXISTS batiments (" +
                "id INTEGER PRIMARY KEY," +
                "user_id INTEGER NOT NULL," +
                "nom TEXT NOT NULL," +
                "type TEXT NOT NULL," +
                "nombre_etages INTEGER NOT NULL," +
                "details TEXT," +
                "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)";

        String sqlConsommation = "CREATE TABLE IF NOT EXISTS consommations (" +
                "id INTEGER PRIMARY KEY," +
                "batiment_id INTEGER NOT NULL," +
                "type TEXT NOT NULL," +
                "quantite INTEGER NOT NULL," +
                "unit TEXT DEFAULT 'kWh'," +
                "date_heure TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY(batiment_id) REFERENCES batiments(id) ON DELETE CASCADE)";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sqlUsers);
            stmt.execute(sqlBatiments);
            stmt.execute(sqlConsommation);
        }
    }

    public void saveBatiment(Batiment batiment, String details, int userId) {
        String sql = "INSERT OR REPLACE INTO batiments (id, user_id, nom, type, nombre_etages, details) VALUES(?,?,?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, batiment.getId());
            pstmt.setInt(2, userId);
            pstmt.setString(3, batiment.getNom());
            pstmt.setString(4, batiment.getType());
            pstmt.setInt(5, batiment.getNombreEtages());
            pstmt.setString(6, details);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveConsommation(Consommation_Energie consommation) {
        String sql = "INSERT OR REPLACE INTO consommations (id, batiment_id, type, quantite, unit) VALUES(?,?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, consommation.getId());
            pstmt.setInt(2, consommation.getBatimentId());
            pstmt.setString(3, consommation.getType().name());
            pstmt.setInt(4, consommation.getQuantité());
            pstmt.setString(5, consommation.getUnit().name());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Consommation_Energie> getConsommationsByBatiment(int batimentId) {
        List<Consommation_Energie> consommations = new ArrayList<>();
        String sql = "SELECT id, batiment_id, type, quantite, unit FROM consommations WHERE batiment_id = ?";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, batimentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    EnergyUnit unit = EnergyUnit.valueOf(rs.getString("unit"));
                    Consommation_Energie c = new Consommation_Energie(
                            rs.getInt("id"),
                            rs.getInt("batiment_id"),
                            TypeConsommation.valueOf(rs.getString("type")),
                            rs.getInt("quantite"),
                            unit
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

    public List<Batiment> getAllBatiments() {
        List<Batiment> batiments = new ArrayList<>();
        String sql = "SELECT * FROM batiments";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String nom = rs.getString("nom");
                String type = rs.getString("type");
                int etages = rs.getInt("nombre_etages");
                String details = rs.getString("details");

                Batiment b = createBatimentFromDatabase(id, nom, type, etages, details);
                if (b != null) {
                    batiments.add(b);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return batiments;
    }

    public List<Batiment> getBatimentsByUser(int userId) {
        List<Batiment> batiments = new ArrayList<>();
        String sql = "SELECT * FROM batiments WHERE user_id = ?";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String nom = rs.getString("nom");
                    String type = rs.getString("type");
                    int etages = rs.getInt("nombre_etages");
                    String details = rs.getString("details");

                    Batiment b = createBatimentFromDatabase(id, nom, type, etages, details);
                    if (b != null) {
                        batiments.add(b);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return batiments;
    }

    public boolean registerUser(String email, String nom, String prenom, String telephone, String password) {
        String sql = "INSERT INTO users (email, nom, prenom, telephone, password, verified) VALUES(?,?,?,?,?,1)";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, nom);
            pstmt.setString(3, prenom);
            pstmt.setString(4, telephone);
            pstmt.setString(5, password);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int loginUser(String email, String password) {
        String sql = "SELECT id, verified FROM users WHERE email = ? AND password = ?";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    if (rs.getInt("verified") == 1) {
                        return rs.getInt("id");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean verifyUser(String email) {
        String sql = "UPDATE users SET verified = 1 WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean emailExists(String email) {
        String sql = "SELECT id FROM users WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Batiment createBatimentFromDatabase(int id, String nom, String type, int etages, String details) {
        return switch (type) {
            case "Maison" -> {
                int detail = details != null ? Integer.parseInt(details) : 0;
                yield new Maison(id, nom, etages, detail);
            }
            case "Appartement" -> {
                int detail = details != null ? Integer.parseInt(details) : 0;
                yield new Appartement(id, nom, etages, detail);
            }
            case "Bureau" -> {
                int detail = details != null ? Integer.parseInt(details) : 0;
                yield new Bureau(id, nom, etages, detail);
            }
            case "Local_commercial" -> {
                double detail = details != null ? Double.parseDouble(details) : 0;
                yield new Local_commercial(id, nom, etages, detail);
            }
            case "Batiment_Universitaire" -> {
                int detail = details != null ? Integer.parseInt(details) : 0;
                yield new Batiment_Universitaire(id, nom, etages, detail);
            }
            case "Autre_Structure" -> new Autre_Structure(id, nom, etages, details);
            default -> null;
        };
    }
}
