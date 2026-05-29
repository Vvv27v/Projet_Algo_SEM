import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class InterfaceGraphique extends JFrame {
    private List<Batiment> batiments;
    private DatabaseManager dbManager;
    private JTable tableauBatiments;
    private JTable tableauConsommation;
    private DefaultTableModel modelBatiments;
    private DefaultTableModel modelConsommation;
    private int selectedBatimentId = -1;
    private int userId;

    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SECONDARY_COLOR = new Color(22, 160, 133);
    private static final Color ACCENT_COLOR = new Color(52, 211, 153);
    private static final Color BACKGROUND_COLOR = new Color(245, 248, 250);
    private static final Color TEXT_COLOR = new Color(44, 62, 80);
    private static final Color BORDER_COLOR = new Color(189, 195, 199);

    public InterfaceGraphique(int userId) {
        this.userId = userId;
        dbManager = new DatabaseManager();
        batiments = dbManager.getBatimentsByUser(userId);
        setupFrame();
        setVisible(true);
    }

    private void setupFrame() {
        setTitle("⚡ Smart Energy Manager - Gestion Énergétique");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1600, 900);
        setLocationRelativeTo(null);
        setResizable(true);
        setBackground(BACKGROUND_COLOR);

        setContentPane(createMainPanel());
    }

    private JPanel createMainPanel() {
        JPanel main = new JPanel(new BorderLayout(15, 15));
        main.setBackground(BACKGROUND_COLOR);
        main.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(850);
        splitPane.setResizeWeight(0.55);
        splitPane.setBackground(BACKGROUND_COLOR);

        splitPane.setLeftComponent(createBatimentsPanel());
        splitPane.setRightComponent(createRightPanel());

        main.add(splitPane, BorderLayout.CENTER);
        main.add(createBottomPanel(), BorderLayout.SOUTH);

        return main;
    }

    private JPanel createBatimentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(createStyledBorder("🏢 Gestion des Bâtiments"));

        modelBatiments = new DefaultTableModel(
            new String[]{"ID", "Nom", "Type", "Étages", "Détails"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableauBatiments = new JTable(modelBatiments);
        styleTable(tableauBatiments);
        tableauBatiments.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tableauBatiments.getSelectedRow() >= 0) {
                selectedBatimentId = (int) modelBatiments.getValueAt(tableauBatiments.getSelectedRow(), 0);
                refreshConsommationTable();
            }
        });

        JScrollPane scroll = new JScrollPane(tableauBatiments);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(createBatimentsButtonPanel(), BorderLayout.SOUTH);

        refreshBatimentsTable();
        return panel;
    }

    private JPanel createRightPanel() {
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBackground(BACKGROUND_COLOR);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setDividerLocation(400);
        split.setResizeWeight(0.5);
        split.setBackground(BACKGROUND_COLOR);

        split.setTopComponent(createConsommationPanel());
        split.setBottomComponent(createStatisticsPanel());

        main.add(split, BorderLayout.CENTER);
        return main;
    }

    private JPanel createConsommationPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(createStyledBorder("⚡ Consommations Énergétiques"));

        modelConsommation = new DefaultTableModel(
            new String[]{"ID", "Type", "Quantité", "Unité"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableauConsommation = new JTable(modelConsommation);
        styleTable(tableauConsommation);

        JScrollPane scroll = new JScrollPane(tableauConsommation);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(createConsommationButtonPanel(), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(createStyledBorder("📊 Statistiques"));

        JTextArea statsArea = new JTextArea();
        statsArea.setEditable(false);
        statsArea.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statsArea.setBackground(new Color(255, 255, 255));
        statsArea.setForeground(TEXT_COLOR);
        statsArea.setMargin(new Insets(10, 10, 10, 10));
        statsArea.setLineWrap(true);
        statsArea.setWrapStyleWord(true);

        JScrollPane scroll = new JScrollPane(statsArea);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        panel.add(scroll, BorderLayout.CENTER);

        JButton btnStats = createStyledButton("🔍 Calculer", ACCENT_COLOR);
        btnStats.addActionListener(e -> showStatistics(statsArea));

        panel.add(btnStats, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createBatimentsButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        panel.setBackground(BACKGROUND_COLOR);

        JButton btn1 = createStyledButton("➕ Ajouter", SECONDARY_COLOR);
        btn1.addActionListener(e -> showAddBatimentDialog());
        panel.add(btn1);

        JButton btn2 = createStyledButton("🗑️ Supprimer", new Color(231, 76, 60));
        btn2.addActionListener(e -> deleteBatiment());
        panel.add(btn2);

        JButton btn3 = createStyledButton("ℹ️ Détails", PRIMARY_COLOR);
        btn3.addActionListener(e -> showBatimentDetails());
        panel.add(btn3);

        JButton btn4 = createStyledButton("📥 Test", ACCENT_COLOR);
        btn4.addActionListener(e -> loadTestData());
        panel.add(btn4);

        return panel;
    }

    private JPanel createConsommationButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        panel.setBackground(BACKGROUND_COLOR);

        JButton btn1 = createStyledButton("➕ Ajouter", SECONDARY_COLOR);
        btn1.addActionListener(e -> showAddConsommationDialog());
        panel.add(btn1);

        JButton btn2 = createStyledButton("🗑️ Supprimer", new Color(231, 76, 60));
        btn2.addActionListener(e -> deleteConsommation());
        panel.add(btn2);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton btn1 = createStyledButton("💾 Sauvegarder", PRIMARY_COLOR);
        btn1.addActionListener(e -> JOptionPane.showMessageDialog(this, "✅ Données sauvegardées!", "Succès", JOptionPane.INFORMATION_MESSAGE));
        panel.add(btn1);

        JButton btn2 = createStyledButton("🗑️ Effacer BD", new Color(231, 76, 60));
        btn2.addActionListener(e -> clearDatabase());
        panel.add(btn2);

        JButton btn3 = createStyledButton("❌ Quitter", new Color(108, 117, 125));
        btn3.addActionListener(e -> System.exit(0));
        panel.add(btn3);

        return panel;
    }

    private void showAddBatimentDialog() {
        JDialog dialog = createStyledDialog("➕ Ajouter un Bâtiment", 450, 350);

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JTextField txtNom = new JTextField();
        JComboBox<String> cmbType = new JComboBox<>(
            new String[]{"Maison", "Appartement", "Bureau", "Local_commercial", "Batiment_Universitaire", "Autre_Structure"}
        );
        JTextField txtEtages = new JTextField();
        JTextField txtDetail = new JTextField();

        addLabelAndField(panel, "Nom du bâtiment:", txtNom);
        addLabelAndField(panel, "Type:", cmbType);
        addLabelAndField(panel, "Nombre d'étages:", txtEtages);
        addLabelAndField(panel, "Détail (Chambres/Surface):", txtDetail);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        JButton btnOK = createStyledButton("✓ Ajouter", SECONDARY_COLOR);
        JButton btnCancel = createStyledButton("✗ Annuler", new Color(189, 195, 199));

        btnOK.addActionListener(e -> {
            try {
                String nom = txtNom.getText().trim();
                String type = (String) cmbType.getSelectedItem();
                int etages = Integer.parseInt(txtEtages.getText());
                String detailStr = txtDetail.getText();

                if (nom.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Le nom ne peut pas être vide!", "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Batiment b = null;
                if ("Local_commercial".equals(type)) {
                    double surface = Double.parseDouble(detailStr);
                    b = new Local_commercial(nom, etages, surface);
                } else {
                    int detail = Integer.parseInt(detailStr);
                    b = createBatiment(nom, type, etages, detail);
                }

                if (b != null) {
                    batiments.add(b);
                    dbManager.saveBatiment(b, detailStr, userId);
                    refreshBatimentsTable();
                    dialog.dispose();
                    JOptionPane.showMessageDialog(InterfaceGraphique.this, "✅ Bâtiment ajouté avec succès!", "Succès", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Erreur: Les champs doivent être des nombres valides!", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        buttonPanel.add(btnOK);
        buttonPanel.add(btnCancel);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void showAddConsommationDialog() {
        if (batiments.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez d'abord ajouter un bâtiment!", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dialog = createStyledDialog("⚡ Ajouter une Consommation", 450, 350);

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[] batimentNames = batiments.stream().map(b -> b.getNom() + " (" + b.getType() + ")").toArray(String[]::new);
        JComboBox<String> cmbBatiment = new JComboBox<>(batimentNames);
        JComboBox<TypeConsommation> cmbType = new JComboBox<>(TypeConsommation.values());
        JLabel lblUnit = new JLabel("Unité: kWh");
        lblUnit.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUnit.setForeground(SECONDARY_COLOR);
        JTextField txtQuantite = new JTextField();

        cmbType.addActionListener(e -> {
            TypeConsommation type = (TypeConsommation) cmbType.getSelectedItem();
            EnergyUnit unit = EnergyUnit.getUnitForType(type);
            lblUnit.setText("Unité: " + unit.getLabel());
        });

        addLabelAndField(panel, "Bâtiment:", cmbBatiment);
        addLabelAndField(panel, "Type d'énergie:", cmbType);
        panel.add(new JLabel(""));
        panel.add(lblUnit);
        addLabelAndField(panel, "Quantité:", txtQuantite);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        JButton btnOK = createStyledButton("✓ Ajouter", SECONDARY_COLOR);
        JButton btnCancel = createStyledButton("✗ Annuler", new Color(189, 195, 199));

        btnOK.addActionListener(e -> {
            try {
                int batimentIdx = cmbBatiment.getSelectedIndex();
                TypeConsommation type = (TypeConsommation) cmbType.getSelectedItem();
                int quantite = Integer.parseInt(txtQuantite.getText());
                EnergyUnit unit = EnergyUnit.getUnitForType(type);

                if (batimentIdx < 0 || quantite <= 0) {
                    JOptionPane.showMessageDialog(dialog, "Veuillez entrer des valeurs valides!", "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int batimentId = batiments.get(batimentIdx).getId();
                Consommation_Energie c = new Consommation_Energie(batimentId, type, quantite);
                c.setUnit(unit);
                dbManager.saveConsommation(c);
                selectedBatimentId = batimentId;
                refreshConsommationTable();
                dialog.dispose();
                JOptionPane.showMessageDialog(InterfaceGraphique.this, "✅ Consommation ajoutée!", "Succès", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Erreur: Quantité invalide!", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        buttonPanel.add(btnOK);
        buttonPanel.add(btnCancel);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void deleteBatiment() {
        int selectedRow = tableauBatiments.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un bâtiment!", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int batimentId = (int) modelBatiments.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Êtes-vous sûr de supprimer ce bâtiment?", "Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            batiments.removeIf(b -> b.getId() == batimentId);
            dbManager.deleteBatiment(batimentId);
            refreshBatimentsTable();
            modelConsommation.setRowCount(0);
            selectedBatimentId = -1;
            JOptionPane.showMessageDialog(this, "✅ Bâtiment supprimé!", "Succès", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deleteConsommation() {
        int selectedRow = tableauConsommation.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une consommation!", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Object idObj = modelConsommation.getValueAt(selectedRow, 0);
        if (idObj != null) {
            try {
                int consomationId = Integer.parseInt(idObj.toString());
                dbManager.deleteConsommation(consomationId);
                refreshConsommationTable();
                JOptionPane.showMessageDialog(this, "✅ Consommation supprimée!", "Succès", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Erreur en supprimant la consommation!", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showBatimentDetails() {
        int selectedRow = tableauBatiments.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un bâtiment!", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Batiment b = batiments.get(selectedRow);
        StringBuilder details = new StringBuilder();
        details.append("🏢 DÉTAILS DU BÂTIMENT\n\n");
        details.append("━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        details.append("ID: ").append(b.getId()).append("\n");
        details.append("Nom: ").append(b.getNom()).append("\n");
        details.append("Type: ").append(b.getType()).append("\n");
        details.append("Étages: ").append(b.getNombreEtages()).append("\n\n");

        if (b instanceof Maison) {
            details.append("🛏️  Chambres: ").append(((Maison) b).getNombreChambres());
        } else if (b instanceof Appartement) {
            details.append("🛏️  Chambres: ").append(((Appartement) b).getNombreChambres());
        } else if (b instanceof Bureau) {
            details.append("💼 Bureaux: ").append(((Bureau) b).getNombreBureau());
        } else if (b instanceof Local_commercial) {
            details.append("📦 Surface: ").append(String.format("%.2f", ((Local_commercial) b).getSurface())).append(" m²");
        } else if (b instanceof Batiment_Universitaire) {
            details.append("🎓 Salles: ").append(((Batiment_Universitaire) b).getNombreSalles());
        } else if (b instanceof Autre_Structure) {
            details.append("📋 Description: ").append(((Autre_Structure) b).getDescription());
        }

        JOptionPane.showMessageDialog(this, details.toString(), "Détails", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showStatistics(JTextArea statsArea) {
        if (selectedBatimentId < 0) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un bâtiment!", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        List<Consommation_Energie> consommations = dbManager.getConsommationsByBatiment(selectedBatimentId);
        if (consommations.isEmpty()) {
            statsArea.setText("📊 Aucune consommation enregistrée pour ce bâtiment.");
            return;
        }

        int total = 0;
        int max = Integer.MIN_VALUE;
        String typeMax = "";
        int min = Integer.MAX_VALUE;
        String typeMin = "";

        for (Consommation_Energie c : consommations) {
            int quantite = c.getQuantité();
            total += quantite;
            if (quantite > max) {
                max = quantite;
                typeMax = c.getType().getLabel();
            }
            if (quantite < min) {
                min = quantite;
                typeMin = c.getType().getLabel();
            }
        }

        double moyenne = (double) total / consommations.size();

        StringBuilder stats = new StringBuilder();
        stats.append("📊 STATISTIQUES DE CONSOMMATION\n");
        stats.append("═════════════════════════════════\n\n");
        stats.append(String.format("Total: %d\n", total));
        stats.append(String.format("Moyenne: %.2f\n", moyenne));
        stats.append(String.format("Maximum: %s (%d)\n", typeMax, max));
        stats.append(String.format("Minimum: %s (%d)\n", typeMin, min));
        stats.append(String.format("\n📈 Nombre de relevés: %d", consommations.size()));

        statsArea.setText(stats.toString());
    }

    private void loadTestData() {
        dbManager.clearDatabase();
        batiments.clear();
        modelBatiments.setRowCount(0);
        modelConsommation.setRowCount(0);

        batiments.add(new Maison("Villa Moderna", 2, 4));
        batiments.add(new Appartement("Immeuble Centre", 8, 3));
        batiments.add(new Bureau("Tech Building", 5, 30));
        batiments.add(new Local_commercial("Mega Store", 3, 1500));
        batiments.add(new Batiment_Universitaire("Université Tech", 6, 120));
        batiments.add(new Autre_Structure("Station Solaire", 1, "Énergie Renouvelable"));

        for (Batiment b : batiments) {
            dbManager.saveBatiment(b, obtenirDetailsBatiment(b), userId);
        }

        Consommation_Energie[] consumptions = {
            new Consommation_Energie(batiments.get(0).getId(), TypeConsommation.ELECTRICITE, 1200),
            new Consommation_Energie(batiments.get(0).getId(), TypeConsommation.CHAUFFAGE_GAZ, 80),
            new Consommation_Energie(batiments.get(0).getId(), TypeConsommation.EAU, 150),
            new Consommation_Energie(batiments.get(1).getId(), TypeConsommation.ELECTRICITE, 3500),
            new Consommation_Energie(batiments.get(1).getId(), TypeConsommation.EAU, 250),
            new Consommation_Energie(batiments.get(2).getId(), TypeConsommation.CLIMATISATION, 4000),
            new Consommation_Energie(batiments.get(3).getId(), TypeConsommation.ELECTRICITE, 8000),
        };

        for (Consommation_Energie c : consumptions) {
            c.setUnit(EnergyUnit.getUnitForType(c.getType()));
            dbManager.saveConsommation(c);
        }

        refreshBatimentsTable();
        JOptionPane.showMessageDialog(this, "✅ Données de test chargées avec succès!", "Succès", JOptionPane.INFORMATION_MESSAGE);
    }

    private void clearDatabase() {
        int confirm = JOptionPane.showConfirmDialog(this, "⚠️  Êtes-vous sûr de vouloir effacer tous les données?", "Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dbManager.clearDatabase();
            batiments.clear();
            modelBatiments.setRowCount(0);
            modelConsommation.setRowCount(0);
            selectedBatimentId = -1;
            JOptionPane.showMessageDialog(this, "✅ Base de données effacée!", "Succès", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private String obtenirDetailsBatiment(Batiment b) {
        if (b instanceof Maison) {
            return String.valueOf(((Maison) b).getNombreChambres());
        } else if (b instanceof Appartement) {
            return String.valueOf(((Appartement) b).getNombreChambres());
        } else if (b instanceof Bureau) {
            return String.valueOf(((Bureau) b).getNombreBureau());
        } else if (b instanceof Local_commercial) {
            return String.valueOf(((Local_commercial) b).getSurface());
        } else if (b instanceof Batiment_Universitaire) {
            return String.valueOf(((Batiment_Universitaire) b).getNombreSalles());
        } else if (b instanceof Autre_Structure) {
            return ((Autre_Structure) b).getDescription();
        }
        return "N/A";
    }

    private Batiment createBatiment(String nom, String type, int etages, int detail) {
        return switch (type) {
            case "Maison" -> new Maison(nom, etages, detail);
            case "Appartement" -> new Appartement(nom, etages, detail);
            case "Bureau" -> new Bureau(nom, etages, detail);
            case "Batiment_Universitaire" -> new Batiment_Universitaire(nom, etages, detail);
            case "Autre_Structure" -> new Autre_Structure(nom, etages, "Structure " + nom);
            default -> null;
        };
    }

    private void refreshBatimentsTable() {
        modelBatiments.setRowCount(0);
        for (Batiment b : batiments) {
            modelBatiments.addRow(new Object[]{
                b.getId(),
                b.getNom(),
                b.getType(),
                b.getNombreEtages(),
                obtenirDetailsBatiment(b)
            });
        }
    }

    private void refreshConsommationTable() {
        modelConsommation.setRowCount(0);
        if (selectedBatimentId >= 0) {
            List<Consommation_Energie> consommations = dbManager.getConsommationsByBatiment(selectedBatimentId);
            for (Consommation_Energie c : consommations) {
                modelConsommation.addRow(new Object[]{
                    c.getId(),
                    c.getType().getLabel(),
                    c.getQuantité(),
                    c.getUnit().getLabel()
                });
            }
        }
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        table.setRowHeight(28);
        table.setSelectionBackground(ACCENT_COLOR);
        table.setSelectionForeground(Color.WHITE);
        table.setGridColor(BORDER_COLOR);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.getTableHeader().setBackground(PRIMARY_COLOR);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(bgColor.darker());
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(bgColor);
            }
        });
        return btn;
    }

    private javax.swing.border.TitledBorder createStyledBorder(String title) {
        javax.swing.border.TitledBorder border = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            title
        );
        border.setTitleFont(new Font("Segoe UI", Font.BOLD, 13));
        border.setTitleColor(PRIMARY_COLOR);
        return border;
    }

    private JDialog createStyledDialog(String title, int width, int height) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setSize(width, height);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(Color.WHITE);
        return dialog;
    }

    private void addLabelAndField(JPanel panel, String label, JComponent field) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(TEXT_COLOR);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        if (field instanceof JTextField) {
            ((JTextField) field).setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        }
        panel.add(lbl);
        panel.add(field);
    }
}
