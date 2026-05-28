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
                // AJOUT CRUCIAL : Force SQLite à activer et respecter les liaisons d'ID entre les tables
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("PRAGMA foreign_keys = ON;");
                }
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
                "unit TEXT DEFAULT 'kWh'," +
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
        // On retire l'id des colonnes pour laisser SQLite l'auto-incrémenter
        String sql = "INSERT INTO consommations (batiment_id, type, quantite, unit) VALUES(?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, consommation.getBatimentId());
            pstmt.setString(2, consommation.getType().name());
            pstmt.setInt(3, consommation.getQuantité());
            pstmt.setString(4, consommation.getUnit().name());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de la sauvegarde de la consommation : " + e.getMessage());
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
                    // Sécurité : Récupération propre des Enums sans planter au valueOf
                    TypeConsommation type = TypeConsommation.ELECTRICITE;
                    try {
                        type = TypeConsommation.valueOf(rs.getString("type").toUpperCase());
                    } catch (Exception e) { /* Valeur par défaut */ }

                    EnergyUnit unit = EnergyUnit.getUnitForType(type);

                    Consommation_Energie c = new Consommation_Energie(
                            rs.getInt("id"),
                            rs.getInt("batiment_id"),
                            type,
                            rs.getInt("quantite"),
                            unit
                    );
                    consommations.add(c);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des consommations : " + e.getMessage());
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
