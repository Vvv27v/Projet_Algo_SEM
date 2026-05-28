import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class InterfaceGraphique extends JFrame {
    private List<Batiment> batiments;
    private DatabaseManager dbManager;
    private JTable tableauBatiments;
    private JTable tableauConsommation;
    private DefaultTableModel modelBatiments;
    private DefaultTableModel modelConsommation;
    private int selectedBatimentId = -1;

    public InterfaceGraphique() {
        dbManager = new DatabaseManager();
        batiments = dbManager.getAllBatiments();

        setupFrame();
        setVisible(true);
    }

    private void setupFrame() {
        setTitle("Smart Energy Manager - Gestion Énergétique");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 800);
        setLocationRelativeTo(null);
        setResizable(true);

        JPanel mainPanel = createMainPanel();
        setContentPane(mainPanel);
    }

    private JPanel createMainPanel() {
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(400);
        splitPane.setResizeWeight(0.5);

        splitPane.setTopComponent(createBatimentsPanel());
        splitPane.setBottomComponent(createConsommationPanel());

        main.add(splitPane, BorderLayout.CENTER);
        main.add(createBottomPanel(), BorderLayout.SOUTH);

        return main;
    }

    private JPanel createBatimentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Gestion des Bâtiments"));

        modelBatiments = new DefaultTableModel(
            new String[]{"ID", "Nom", "Type", "Étages", "Détails"}, 0
        );
        tableauBatiments = new JTable(modelBatiments);
        tableauBatiments.setRowHeight(25);
        tableauBatiments.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tableauBatiments.getSelectedRow() >= 0) {
                selectedBatimentId = (int) modelBatiments.getValueAt(tableauBatiments.getSelectedRow(), 0);
                refreshConsommationTable();
            }
        });

        JScrollPane scroll = new JScrollPane(tableauBatiments);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(createBatimentsButtonPanel(), BorderLayout.SOUTH);

        refreshBatimentsTable();
        return panel;
    }

    private JPanel createBatimentsButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        JButton btnAdd = new JButton("Ajouter");
        JButton btnDelete = new JButton("Supprimer");
        JButton btnDetails = new JButton("Détails");
        JButton btnTest = new JButton("Charger Test");

        btnAdd.addActionListener(e -> showAddBatimentDialog());
        btnDelete.addActionListener(e -> deleteBatiment());
        btnDetails.addActionListener(e -> showBatimentDetails());
        btnTest.addActionListener(e -> loadTestData());

        panel.add(btnAdd);
        panel.add(btnDelete);
        panel.add(btnDetails);
        panel.add(btnTest);

        return panel;
    }

    private JPanel createConsommationPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Consommation Énergétique"));

        modelConsommation = new DefaultTableModel(
            new String[]{"ID", "Type", "Quantité (kWh)"}, 0
        );
        tableauConsommation = new JTable(modelConsommation);
        tableauConsommation.setRowHeight(25);

        JScrollPane scroll = new JScrollPane(tableauConsommation);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(createConsommationButtonPanel(), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createConsommationButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        JButton btnAdd = new JButton("Ajouter");
        JButton btnDelete = new JButton("Supprimer");
        JButton btnStats = new JButton("Statistiques");

        btnAdd.addActionListener(e -> showAddConsommationDialog());
        btnDelete.addActionListener(e -> deleteConsommation());
        btnStats.addActionListener(e -> showStatistics());

        panel.add(btnAdd);
        panel.add(btnDelete);
        panel.add(btnStats);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

        JButton btnSave = new JButton("Sauvegarder");
        JButton btnClear = new JButton("Effacer BD");
        JButton btnQuit = new JButton("Quitter");

        btnSave.addActionListener(e -> JOptionPane.showMessageDialog(this, "Données sauvegardées!"));
        btnClear.addActionListener(e -> clearDatabase());
        btnQuit.addActionListener(e -> System.exit(0));

        panel.add(btnSave);
        panel.add(btnClear);
        panel.add(btnQuit);

        return panel;
    }

    private void showAddBatimentDialog() {
        JDialog dialog = new JDialog(this, "Ajouter un Bâtiment", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(5, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblNom = new JLabel("Nom:");
        JTextField txtNom = new JTextField();
        JLabel lblType = new JLabel("Type:");
        JComboBox<String> cmbType = new JComboBox<>(
            new String[]{"Maison", "Appartement", "Bureau", "Local_commercial", "Batiment_Universitaire", "Autre_Structure"}
        );
        JLabel lblEtages = new JLabel("Étages:");
        JTextField txtEtages = new JTextField();
        JLabel lblDetail = new JLabel("Détail:");
        JTextField txtDetail = new JTextField();

        panel.add(lblNom);
        panel.add(txtNom);
        panel.add(lblType);
        panel.add(cmbType);
        panel.add(lblEtages);
        panel.add(txtEtages);
        panel.add(lblDetail);
        panel.add(txtDetail);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnOK = new JButton("OK");
        JButton btnCancel = new JButton("Annuler");

        btnOK.addActionListener(e -> {
            try {
                String nom = txtNom.getText();
                String type = (String) cmbType.getSelectedItem();
                int etages = Integer.parseInt(txtEtages.getText());
                String detailStr = txtDetail.getText();

                Batiment b = null;
                if ("Local_commercial".equals(type)) {
                    double surface = Double.parseDouble(detailStr);
                    b = new Local_commercial(nom, etages, surface);
                    dbManager.saveBatiment(b, detailStr);
                } else {
                    int detail = Integer.parseInt(detailStr);
                    b = createBatiment(nom, type, etages, detail);
                    dbManager.saveBatiment(b, detailStr);
                }

                if (b != null) {
                    batiments.add(b);
                    refreshBatimentsTable();
                    dialog.dispose();
                    JOptionPane.showMessageDialog(InterfaceGraphique.this, "Bâtiment ajouté!");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Erreur: Les champs doivent être des nombres valides!");
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        buttonPanel.add(btnOK);
        buttonPanel.add(btnCancel);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private Batiment createBatiment(String nom, String type, int etages, int detail) {
        return switch (type) {
            case "Maison" -> new Maison(nom, etages, detail);
            case "Appartement" -> new Appartement(nom, etages, detail);
            case "Bureau" -> new Bureau(nom, etages, detail);
            case "Local_commercial" -> new Local_commercial(nom, etages, detail);
            case "Batiment_Universitaire" -> new Batiment_Universitaire(nom, etages, detail);
            case "Autre_Structure" -> new Autre_Structure(nom, etages, "Structure " + nom);
            default -> null;
        };
    }

    private void showAddConsommationDialog() {
        if (selectedBatimentId < 0) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un bâtiment!");
            return;
        }

        JDialog dialog = new JDialog(this, "Ajouter une Consommation", true);
        dialog.setSize(350, 150);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblType = new JLabel("Type d'Énergie:");
        JComboBox<TypeConsommation> cmbType = new JComboBox<>(TypeConsommation.values());
        JLabel lblQuantite = new JLabel("Quantité (kWh):");
        JTextField txtQuantite = new JTextField();

        panel.add(lblType);
        panel.add(cmbType);
        panel.add(lblQuantite);
        panel.add(txtQuantite);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnOK = new JButton("OK");
        JButton btnCancel = new JButton("Annuler");

        btnOK.addActionListener(e -> {
            try {
                TypeConsommation type = (TypeConsommation) cmbType.getSelectedItem();
                int quantite = Integer.parseInt(txtQuantite.getText());

                Consommation_Energie c = new Consommation_Energie(selectedBatimentId, type, quantite);
                dbManager.saveConsommation(c);
                refreshConsommationTable();
                dialog.dispose();
                JOptionPane.showMessageDialog(InterfaceGraphique.this, "Consommation ajoutée!");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Erreur: Veuillez entrer une quantité valide!");
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
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un bâtiment!");
            return;
        }

        int batimentId = (int) modelBatiments.getValueAt(selectedRow, 0);
        batiments.removeIf(b -> b.getId() == batimentId);
        dbManager.deleteBatiment(batimentId);
        refreshBatimentsTable();
        modelConsommation.setRowCount(0);
        selectedBatimentId = -1;
        JOptionPane.showMessageDialog(this, "Bâtiment supprimé!");
    }

    private void deleteConsommation() {
        int selectedRow = tableauConsommation.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une consommation!");
            return;
        }

        int consomationId = (int) modelConsommation.getValueAt(selectedRow, 0);
        dbManager.deleteConsommation(consomationId);
        refreshConsommationTable();
        JOptionPane.showMessageDialog(this, "Consommation supprimée!");
    }

    private void showBatimentDetails() {
        int selectedRow = tableauBatiments.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un bâtiment!");
            return;
        }

        Batiment b = batiments.get(selectedRow);
        StringBuilder details = new StringBuilder();
        details.append("ID: ").append(b.getId()).append("\n");
        details.append("Nom: ").append(b.getNom()).append("\n");
        details.append("Type: ").append(b.getType()).append("\n");
        details.append("Étages: ").append(b.getNombreEtages()).append("\n\n");

        if (b instanceof Maison) {
            details.append("Chambres: ").append(((Maison) b).getNombreChambres());
        } else if (b instanceof Appartement) {
            details.append("Chambres: ").append(((Appartement) b).getNombreChambres());
        } else if (b instanceof Bureau) {
            details.append("Bureaux: ").append(((Bureau) b).getNombreBureau());
        } else if (b instanceof Local_commercial) {
            details.append("Surface: ").append(((Local_commercial) b).getSurface()).append(" m²");
        } else if (b instanceof Batiment_Universitaire) {
            details.append("Salles: ").append(((Batiment_Universitaire) b).getNombreSalles());
        } else if (b instanceof Autre_Structure) {
            details.append("Description: ").append(((Autre_Structure) b).getDescription());
        }

        JOptionPane.showMessageDialog(this, details.toString(), "Détails du Bâtiment", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showStatistics() {
        if (selectedBatimentId < 0) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un bâtiment!");
            return;
        }

        List<Consommation_Energie> consommations = dbManager.getConsommationsByBatiment(selectedBatimentId);
        if (consommations.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Aucune consommation pour ce bâtiment!");
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
        stats.append("=== Statistiques ===\n\n");
        stats.append("Total: ").append(total).append(" kWh\n");
        stats.append("Moyenne: ").append(String.format("%.2f", moyenne)).append(" kWh\n");
        stats.append("Maximum: ").append(typeMax).append(" (").append(max).append(" kWh)\n");
        stats.append("Minimum: ").append(typeMin).append(" (").append(min).append(" kWh)");

        JOptionPane.showMessageDialog(this, stats.toString(), "Statistiques", JOptionPane.INFORMATION_MESSAGE);
    }

    private void loadTestData() {
        dbManager.clearDatabase();
        batiments.clear();
        modelBatiments.setRowCount(0);
        modelConsommation.setRowCount(0);

        batiments.add(new Maison("Villa Moderne", 2, 4));
        batiments.add(new Appartement("Immeuble Centre-Ville", 8, 3));
        batiments.add(new Bureau("Bureau Tech", 5, 30));
        batiments.add(new Local_commercial("Centre Commercial", 3, 500));
        batiments.add(new Batiment_Universitaire("Université A", 6, 100));
        batiments.add(new Autre_Structure("Parking Souterrain", 3, "Parking"));

        for (Batiment b : batiments) {
            dbManager.saveBatiment(b, obtenirDetailsBatiment(b));
        }

        Consommation_Energie[] consumptions = {
            new Consommation_Energie(batiments.get(0).getId(), TypeConsommation.ELECTRICITE, 1000),
            new Consommation_Energie(batiments.get(0).getId(), TypeConsommation.CHAUFFAGE_GAZ, 800),
            new Consommation_Energie(batiments.get(1).getId(), TypeConsommation.ELECTRICITE, 3000),
            new Consommation_Energie(batiments.get(1).getId(), TypeConsommation.EAU, 500),
            new Consommation_Energie(batiments.get(2).getId(), TypeConsommation.CLIMATISATION, 2000)
        };

        for (Consommation_Energie c : consumptions) {
            dbManager.saveConsommation(c);
        }

        refreshBatimentsTable();
        JOptionPane.showMessageDialog(this, "Données de test chargées!");
    }

    private void clearDatabase() {
        int confirm = JOptionPane.showConfirmDialog(this, "Êtes-vous sûr de vouloir effacer la BD?");
        if (confirm == JOptionPane.YES_OPTION) {
            dbManager.clearDatabase();
            batiments.clear();
            modelBatiments.setRowCount(0);
            modelConsommation.setRowCount(0);
            selectedBatimentId = -1;
            JOptionPane.showMessageDialog(this, "Base de données effacée!");
        }
    }

    private String obtenirDetailsBatiment(Batiment b) {
        if (b instanceof Maison) {
            return "Chambres: " + ((Maison) b).getNombreChambres();
        } else if (b instanceof Appartement) {
            return "Chambres: " + ((Appartement) b).getNombreChambres();
        } else if (b instanceof Bureau) {
            return "Bureaux: " + ((Bureau) b).getNombreBureau();
        } else if (b instanceof Local_commercial) {
            return "Surface: " + ((Local_commercial) b).getSurface() + " m²";
        } else if (b instanceof Batiment_Universitaire) {
            return "Salles: " + ((Batiment_Universitaire) b).getNombreSalles();
        } else if (b instanceof Autre_Structure) {
            return ((Autre_Structure) b).getDescription();
        }
        return "N/A";
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
                    c.getQuantité()
                });
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new InterfaceGraphique());
    }
}
