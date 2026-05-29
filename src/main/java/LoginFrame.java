import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private DatabaseManager dbManager;
    private int currentUserId = -1;

    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private static final Color PRIMARY_COLOR = new Color(70, 130, 180);
    private static final Color ACCENT_COLOR = new Color(220, 220, 220);
    private static final Color TEXT_COLOR = new Color(60, 60, 60);

    public LoginFrame() {
        dbManager = new DatabaseManager();
        setupFrame();
        setVisible(true);
    }

    private void setupFrame() {
        setTitle("Smart Energy Manager - Authentification");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 800);
        setLocationRelativeTo(null);
        setResizable(false);
        setBackground(BACKGROUND_COLOR);

        setContentPane(createMainPanel());
    }

    private JPanel createMainPanel() {
        JPanel main = new JPanel(new CardLayout());
        main.setBackground(BACKGROUND_COLOR);

        JPanel loginPanel = createLoginPanel();
        JPanel registerPanel = createRegisterPanel();
        JPanel verifyPanel = createVerifyPanel();

        main.add(loginPanel, "LOGIN");
        main.add(registerPanel, "REGISTER");
        main.add(verifyPanel, "VERIFY");

        return main;
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND_COLOR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;

        JLabel titleLabel = new JLabel("⚡ Connexion");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        gbc.gridy = 0;
        panel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        emailLabel.setForeground(TEXT_COLOR);
        panel.add(emailLabel, gbc);

        gbc.gridy = 2;
        JTextField emailField = new JTextField();
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        emailField.setBorder(BorderFactory.createLineBorder(ACCENT_COLOR));
        panel.add(emailField, gbc);

        gbc.gridy = 3;
        JLabel pwdLabel = new JLabel("Mot de passe:");
        pwdLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pwdLabel.setForeground(TEXT_COLOR);
        panel.add(pwdLabel, gbc);

        gbc.gridy = 4;
        JPasswordField pwdField = new JPasswordField();
        pwdField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pwdField.setBorder(BorderFactory.createLineBorder(ACCENT_COLOR));
        panel.add(pwdField, gbc);

        gbc.gridy = 5;
        gbc.gridwidth = 2;
        JButton loginBtn = createButton("Connexion", PRIMARY_COLOR);
        loginBtn.addActionListener(e -> {
            String email = emailField.getText().trim();
            String pwd = new String(pwdField.getPassword());
            
            if (email.isEmpty() || pwd.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs!", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int userId = dbManager.loginUser(email, pwd);
            if (userId > 0) {
                openEnergyDashboard(userId);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Email ou mot de passe incorrect, ou compte non vérifié!", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(loginBtn, gbc);

        gbc.gridy = 6;
        JButton registerBtn = createButton("Créer un compte", ACCENT_COLOR);
        registerBtn.addActionListener(e -> {
            CardLayout cl = (CardLayout) getContentPane().getLayout();
            cl.show(getContentPane(), "REGISTER");
        });
        panel.add(registerBtn, gbc);

        return panel;
    }

    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND_COLOR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 20, 8, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;

        JLabel titleLabel = new JLabel("📝 Inscription");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        gbc.gridy = 0;
        panel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        JTextField nomField = addField(panel, "Nom:", gbc, 1);
        JTextField prenomField = addField(panel, "Prénom:", gbc, 2);
        JTextField emailField = addField(panel, "Email (@):", gbc, 3);
        JTextField telField = addField(panel, "Téléphone:", gbc, 4);
        JPasswordField pwdField = addPasswordField(panel, "Mot de passe:", gbc, 5);

        gbc.gridy = 6;
        gbc.gridwidth = 2;
        JButton registerBtn = createButton("S'inscrire", PRIMARY_COLOR);
        registerBtn.addActionListener(e -> {
            String nom = nomField.getText().trim();
            String prenom = prenomField.getText().trim();
            String email = emailField.getText().trim();
            String tel = telField.getText().trim();
            String pwd = new String(pwdField.getPassword());

            if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || tel.isEmpty() || pwd.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs!", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!email.contains("@")) {
                JOptionPane.showMessageDialog(this, "Email invalide! Doit contenir @", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (dbManager.emailExists(email)) {
                JOptionPane.showMessageDialog(this, "Cet email existe déjà!", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (dbManager.registerUser(email, nom, prenom, tel, pwd)) {
                JOptionPane.showMessageDialog(this, "✅ Inscription réussie! Vous pouvez maintenant vous connecter.", "Succès", JOptionPane.INFORMATION_MESSAGE);
                CardLayout cl = (CardLayout) getContentPane().getLayout();
                cl.show(getContentPane(), "LOGIN");
            } else {
                JOptionPane.showMessageDialog(this, "Erreur lors de l'inscription!", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(registerBtn, gbc);

        gbc.gridy = 7;
        JButton backBtn = createButton("Retour", ACCENT_COLOR);
        backBtn.addActionListener(e -> {
            CardLayout cl = (CardLayout) getContentPane().getLayout();
            cl.show(getContentPane(), "LOGIN");
        });
        panel.add(backBtn, gbc);

        return panel;
    }

    private JPanel createVerifyPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND_COLOR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;

        JLabel titleLabel = new JLabel("✓ Vérification");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        gbc.gridy = 0;
        panel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        JLabel codeLabel = new JLabel("Code (envoyé par email):");
        codeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        codeLabel.setForeground(TEXT_COLOR);
        panel.add(codeLabel, gbc);

        gbc.gridy = 2;
        JTextField codeField = new JTextField();
        codeField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        codeField.setBorder(BorderFactory.createLineBorder(ACCENT_COLOR));
        panel.add(codeField, gbc);

        gbc.gridy = 3;
        gbc.gridwidth = 2;
        JButton verifyBtn = createButton("Vérifier", PRIMARY_COLOR);
        verifyBtn.addActionListener(e -> {
            String code = codeField.getText().trim();
            if (code.equals("12345")) {
                JOptionPane.showMessageDialog(this, "Compte vérifié! Vous pouvez maintenant vous connecter.", "Succès", JOptionPane.INFORMATION_MESSAGE);
                CardLayout cl = (CardLayout) getContentPane().getLayout();
                cl.show(getContentPane(), "LOGIN");
            } else {
                JOptionPane.showMessageDialog(this, "Code incorrect!", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(verifyBtn, gbc);

        return panel;
    }

    private void showVerificationPanel(String email) {
        dbManager.verifyUser(email);
        CardLayout cl = (CardLayout) getContentPane().getLayout();
        cl.show(getContentPane(), "VERIFY");
    }

    private JTextField addField(JPanel panel, String label, GridBagConstraints gbc, int row) {
        gbc.gridy = row;
        gbc.gridwidth = 1;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(TEXT_COLOR);
        panel.add(lbl, gbc);

        gbc.gridy = row + 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JTextField field = new JTextField(25);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        field.setBorder(BorderFactory.createLineBorder(ACCENT_COLOR));
        panel.add(field, gbc);

        return field;
    }

    private JPasswordField addPasswordField(JPanel panel, String label, GridBagConstraints gbc, int row) {
        gbc.gridy = row;
        gbc.gridwidth = 1;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(TEXT_COLOR);
        panel.add(lbl, gbc);

        gbc.gridy = row + 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JPasswordField field = new JPasswordField(25);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        field.setBorder(BorderFactory.createLineBorder(ACCENT_COLOR));
        panel.add(field, gbc);

        return field;
    }

    private JButton createButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setBackground(bgColor);
        btn.setForeground(TEXT_COLOR);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void openEnergyDashboard(int userId) {
        SwingUtilities.invokeLater(() -> new InterfaceGraphique(userId));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame());
    }
}
