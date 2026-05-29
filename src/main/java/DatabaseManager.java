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
                ensureConsommationSchema(conn);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void ensureConsommationSchema(Connection conn) throws SQLException {
        boolean hasCout = false;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA table_info(consommations)")) {
            while (rs.next()) {
                if ("cout".equalsIgnoreCase(rs.getString("name"))) {
                    hasCout = true;
                    break;
                }
            }
        } catch (Exception ignored) {}

        if (!hasCout) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DROP TABLE IF EXISTS consommations");
                stmt.execute("CREATE TABLE consommations (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "batiment_id INTEGER NOT NULL," +
                    "type TEXT NOT NULL," +
                    "quantite INTEGER NOT NULL," +
                    "unit TEXT DEFAULT 'KILOWATT_HEURE'," +
                    "cout REAL DEFAULT 0," +
                    "date_heure TEXT DEFAULT CURRENT_TIMESTAMP)");
            }
        }
    }

    private void createTables(Connection conn) throws SQLException {
        String sqlUsers = "CREATE TABLE IF NOT EXISTS users ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "email TEXT NOT NULL UNIQUE,"
                + "nom TEXT NOT NULL,"
                + "prenom TEXT NOT NULL,"
                + "telephone TEXT NOT NULL,"
                + "password TEXT NOT NULL,"
                + "verified INTEGER DEFAULT 0)";

        String sqlBatiments = "CREATE TABLE IF NOT EXISTS batiments ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "user_id INTEGER NOT NULL,"
                + "nom TEXT NOT NULL,"
                + "type TEXT NOT NULL,"
                + "nombre_etages INTEGER NOT NULL,"
                + "details TEXT,"
                + "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)";

        String sqlConsommation = "CREATE TABLE IF NOT EXISTS consommations ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "batiment_id INTEGER NOT NULL,"
                + "type TEXT NOT NULL,"
                + "quantite INTEGER NOT NULL,"
                + "unit TEXT DEFAULT 'KILOWATT_HEURE',"
                + "cout REAL DEFAULT 0,"
                + "date_heure TEXT DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY(batiment_id) REFERENCES batiments(id) ON DELETE CASCADE)";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sqlUsers);
            stmt.execute(sqlBatiments);
            stmt.execute(sqlConsommation);
        }
    }


    // Returns the auto-generated ID
    public int saveConsommation(Consommation_Energie c) {
        String sql = "INSERT INTO consommations (batiment_id, type, quantite, unit, cout, date_heure) VALUES(?,?,?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, c.getBatimentId());
                pstmt.setString(2, c.getType().name());
                pstmt.setInt(3, c.getQuantité());
                pstmt.setString(4, c.getUnit().name());
                pstmt.setDouble(5, c.getCout());
                pstmt.setString(6, c.getDateHeure());
                pstmt.executeUpdate();
            }
            // last_insert_rowid() est fiable avec SQLite
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("saveConsommation error: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    public List<Consommation_Energie> getConsommationsByBatiment(int batimentId) {
        List<Consommation_Energie> list = new ArrayList<>();
        String sql = "SELECT id, batiment_id, type, quantite, unit, cout, date_heure FROM consommations WHERE batiment_id = ? ORDER BY id DESC";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, batimentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    EnergyUnit unit;
                    try { unit = EnergyUnit.valueOf(rs.getString("unit")); }
                    catch (Exception e) { unit = EnergyUnit.KILOWATT_HEURE; }
                    TypeConsommation type;
                    try { type = TypeConsommation.valueOf(rs.getString("type")); }
                    catch (Exception e) { continue; }
                    list.add(new Consommation_Energie(
                            rs.getInt("id"),
                            rs.getInt("batiment_id"),
                            type,
                            rs.getInt("quantite"),
                            unit,
                            rs.getDouble("cout"),
                            rs.getString("date_heure")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void deleteConsommation(int id) {
        String sql = "DELETE FROM consommations WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
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

    public void deleteBatiment(int batimentId) {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM batiments WHERE id = ?")) {
            pstmt.setInt(1, batimentId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM consommations WHERE batiment_id = ?")) {
            pstmt.setInt(1, batimentId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Batiment> getBatimentsByUser(int userId) {
        List<Batiment> list = new ArrayList<>();
        String sql = "SELECT * FROM batiments WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Batiment b = createBatimentFromDatabase(
                            rs.getInt("id"), rs.getString("nom"),
                            rs.getString("type"), rs.getInt("nombre_etages"),
                            rs.getString("details"));
                    if (b != null) list.add(b);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void clearDatabase() {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM consommations");
            stmt.execute("DELETE FROM batiments");
            stmt.execute("DELETE FROM sqlite_sequence WHERE name='batiments'");
            stmt.execute("DELETE FROM sqlite_sequence WHERE name='consommations'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
                if (rs.next() && rs.getInt("verified") == 1) return rs.getInt("id");
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
            try (ResultSet rs = pstmt.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Batiment createBatimentFromDatabase(int id, String nom, String type, int etages, String details) {
        return switch (type) {
            case "Maison" -> new Maison(id, nom, etages, details != null ? parseIntSafe(details) : 0);
            case "Appartement" -> new Appartement(id, nom, etages, details != null ? parseIntSafe(details) : 0);
            case "Bureau" -> new Bureau(id, nom, etages, details != null ? parseIntSafe(details) : 0);
            case "Local_commercial" -> {
                double surf = 0;
                if (details != null) try { surf = Double.parseDouble(details.replace(" m²", "")); } catch (Exception ignored) {}
                yield new Local_commercial(id, nom, etages, surf);
            }
            case "Batiment_Universitaire" -> new Batiment_Universitaire(id, nom, etages, details != null ? parseIntSafe(details) : 0);
            case "Autre_Structure" -> new Autre_Structure(id, nom, etages, details != null ? details : "");
            default -> null;
        };
    }

    private int parseIntSafe(String s) {
        try { return Integer.parseInt(s.replaceAll("[^0-9]", "")); } catch (Exception e) { return 0; }
    }
}
