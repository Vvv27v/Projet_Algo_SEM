public class CreateTestUser {
    public static void main(String[] args) {
        DatabaseManager db = new DatabaseManager();

        if (db.registerUser("test@test.com", "Test", "User", "0601020304", "123456")) {
            System.out.println("✅ Compte créé avec succès!");
            System.out.println("Email: test@test.com");
            System.out.println("Mot de passe: 123456");
        } else {
            System.out.println("❌ Erreur lors de la création");
        }
    }
}
