import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
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
        batiments = new ArrayList<>();
        dbManager = new DatabaseManager();

        setTitle("Gestion des Bâtiments et Consommation Énergétique");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750);
        setLocationRelativeTo(null);
        setResizable(true);

        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelMain = creerPanelBatimentsAvecConsommation();
        panelPrincipal.add(panelMain, BorderLayout.CENTER);

        JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton btnCharger = new JButton("Charger Données");
        JButton btnSauvegarder = new JButton("Sauvegarder");
        JButton btnClear = new JButton("Effacer Base");
        JButton btnQuitter = new JButton("Quitter");

        btnCharger.addActionListener(e -> chargerDonnees());
        btnSauvegarder.addActionListener(e -> sauvegarderDonnees());
        btnClear.addActionListener(e -> effacerBase());
        btnQuitter.addActionListener(e -> System.exit(0));

        panelBoutons.add(btnCharger);
        panelBoutons.add(btnSauvegarder);
        panelBoutons.add(btnClear);
        panelBoutons.add(btnQuitter);

        panelPrincipal.add(panelBoutons, BorderLayout.SOUTH);

        add(panelPrincipal);
        setVisible(true);

        chargerDonnees();
    }

    private JPanel creerPanelBatimentsAvecConsommation() {
        JPanel panelMain = new JPanel(new BorderLayout(10, 10));
        panelMain.setBorder(BorderFactory.createTitledBorder("Gestion Bâtiments et Consommation Énergétique"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(350);

        JPanel panelBatiments = new JPanel(new BorderLayout(10, 10));
        panelBatiments.setBorder(BorderFactory.createTitledBorder("Bâtiments"));

        modelBatiments = new DefaultTableModel(
            new String[]{"ID", "Nom", "Type", "Étages", "Détails"}, 0
        );
        tableauBatiments = new JTable(modelBatiments);
        tableauBatiments.setFillsViewportHeight(true);
        tableauBatiments.setRowHeight(25);
        tableauBatiments.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tableauBatiments.getSelectedRow();
                if (selectedRow >= 0) {
                    selectedBatimentId = (int) modelBatiments.getValueAt(selectedRow, 0);
                    actualiserTableauConsommation();
                }
            }
        });

        JScrollPane scrollBatiments = new JScrollPane(tableauBatiments);
        panelBatiments.add(scrollBatiments, BorderLayout.CENTER);

        JPanel panelBoutonsBatiments = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        JButton btnAjouter = new JButton("Ajouter Bâtiment");
        JButton btnSupprimer = new JButton("Supprimer Bâtiment");
        JButton btnAffichageDetaille = new JButton("Détails");

        btnAjouter.addActionListener(e -> afficherDialogAjoutBatiment());
        btnSupprimer.addActionListener(e -> supprimerBatimentSelectionne());
        btnAffichageDetaille.addActionListener(e -> afficherDetailsBatiment());

        panelBoutonsBatiments.add(btnAjouter);
        panelBoutonsBatiments.add(btnSupprimer);
        panelBoutonsBatiments.add(btnAffichageDetaille);

        panelBatiments.add(panelBoutonsBatiments, BorderLayout.SOUTH);

        JPanel panelConsommation = new JPanel(new BorderLayout(10, 10));
        panelConsommation.setBorder(BorderFactory.createTitledBorder("Consommation Énergétique (du bâtiment sélectionné)"));

        modelConsommation = new DefaultTableModel(
            new String[]{"ID", "Type", "Quantité (kWh)"}, 0
        );
        tableauConsommation = new JTable(modelConsommation);
        tableauConsommation.setFillsViewportHeight(true);
        tableauConsommation.setRowHeight(25);

        JScrollPane scrollConsommation = new JScrollPane(tableauConsommation);
        panelConsommation.add(scrollConsommation, BorderLayout.CENTER);

        JPanel panelBoutonsConsommation = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        JButton btnAjouterConsommation = new JButton("Ajouter Consommation");
        JButton btnSupprimerConsommation = new JButton("Supprimer Consommation");
        JButton btnStatistiques = new JButton("Statistiques");

        btnAjouterConsommation.addActionListener(e -> afficherDialogAjoutConsommation());
        btnSupprimerConsommation.addActionListener(e -> supprimerConsommationSelectionnee());
        btnStatistiques.addActionListener(e -> afficherStatistiques());

        panelBoutonsConsommation.add(btnAjouterConsommation);
        panelBoutonsConsommation.add(btnSupprimerConsommation);
        panelBoutonsConsommation.add(btnStatistiques);

        panelConsommation.add(panelBoutonsConsommation, BorderLayout.SOUTH);

        splitPane.setTopComponent(panelBatiments);
        splitPane.setBottomComponent(panelConsommation);

        panelMain.add(splitPane, BorderLayout.CENTER);

        return panelMain;
    }

    private void chargerDonnees() {
        dbManager.clearDatabase();
        batiments.clear();

        batiments.add(new Maison("Villa Moderne", 2, 4));
        batiments.add(new Appartement("Immeuble Centre-Ville", 8, 3));
        batiments.add(new Bureau("Bureau Tech", 5, 30));
        batiments.add(new Local_commercial("Centre Commercial", 3, 500));
        batiments.add(new Batiment_Universitaire("Université A", 6, 100));
        batiments.add(new Autre_Structure("Parking Souterrain", 3, "Parking"));

        for (Batiment b : batiments) {
            String details = obtenirDetailsBatiment(b);
            dbManager.saveBatiment(b, details);
        }

        Consommation_Energie cons1 = new Consommation_Energie(batiments.get(0).getId(), TypeConsommation.ELECTRICITE, 1000);
        Consommation_Energie cons2 = new Consommation_Energie(batiments.get(0).getId(), TypeConsommation.CHAUFFAGE_GAZ, 800);
        Consommation_Energie cons3 = new Consommation_Energie(batiments.get(1).getId(), TypeConsommation.ELECTRICITE, 3000);
        Consommation_Energie cons4 = new Consommation_Energie(batiments.get(1).getId(), TypeConsommation.EAU, 500);
        Consommation_Energie cons5 = new Consommation_Energie(batiments.get(2).getId(), TypeConsommation.CLIMATISATION, 2000);

        dbManager.saveConsommation(cons1);
        dbManager.saveConsommation(cons2);
        dbManager.saveConsommation(cons3);
        dbManager.saveConsommation(cons4);
        dbManager.saveConsommation(cons5);

        actualiserTableauBatiments();
        JOptionPane.showMessageDialog(this, "Données chargées avec succès!");
    }

    private void sauvegarderDonnees() {
        JOptionPane.showMessageDialog(this, "Données sauvegardées!");
    }

    private void actualiserTableauBatiments() {
        modelBatiments.setRowCount(0);
        for (Batiment b : batiments) {
            String details = obtenirDetailsBatiment(b);
            modelBatiments.addRow(new Object[]{
                b.getId(),
                b.getNom(),
                b.getType(),
                b.getNombreEtages(),
                details
            });
        }
    }

    private void actualiserTableauConsommation() {
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

    private void afficherDialogAjoutBatiment() {
        JDialog dialog = new JDialog(this, "Ajouter un Bâtiment", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblNom = new JLabel("Nom:");
        JTextField txtNom = new JTextField();
        JLabel lblType = new JLabel("Type:");
        JComboBox<String> cmbType = new JComboBox<>(
            new String[]{"Maison", "Appartement", "Bureau", "Local_commercial", "Batiment_Universitaire", "Autre_Structure"}
        );
        JLabel lblEtages = new JLabel("Étages:");
        JTextField txtEtages = new JTextField();
        JLabel lblDetail = new JLabel("Détail (Chambres/Surface):");
        JTextField txtDetail = new JTextField();

        panel.add(lblNom);
        panel.add(txtNom);
        panel.add(lblType);
        panel.add(cmbType);
        panel.add(lblEtages);
        panel.add(txtEtages);
        panel.add(lblDetail);
        panel.add(txtDetail);

        JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnAjouter = new JButton("Ajouter");
        JButton btnAnnuler = new JButton("Annuler");

        btnAjouter.addActionListener(e -> {
            try {
                String nom = txtNom.getText();
                String type = (String) cmbType.getSelectedItem();
                int etages = Integer.parseInt(txtEtages.getText());
                int detail = Integer.parseInt(txtDetail.getText());

                Batiment b = null;
                switch (type) {
                    case "Maison":
                        b = new Maison(nom, etages, detail);
                        break;
                    case "Appartement":
                        b = new Appartement(nom, etages, detail);
                        break;
                    case "Bureau":
                        b = new Bureau(nom, etages, detail);
                        break;
                    case "Local_commercial":
                        b = new Local_commercial(nom, etages, detail);
                        break;
                    case "Batiment_Universitaire":
                        b = new Batiment_Universitaire(nom, etages, detail);
                        break;
                    case "Autre_Structure":
                        b = new Autre_Structure(nom, etages, "Structure");
                        break;
                }

                if (b != null) {
                    batiments.add(b);
                    dbManager.saveBatiment(b, obtenirDetailsBatiment(b));
                    actualiserTableauBatiments();
                    dialog.dispose();
                    JOptionPane.showMessageDialog(InterfaceGraphique.this, "Bâtiment ajouté!");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Erreur: Veuillez entrer des nombres valides!");
            }
        });

        btnAnnuler.addActionListener(e -> dialog.dispose());

        panelBoutons.add(btnAjouter);
        panelBoutons.add(btnAnnuler);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(panelBoutons, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void afficherDialogAjoutConsommation() {
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

        JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnAjouter = new JButton("Ajouter");
        JButton btnAnnuler = new JButton("Annuler");

        btnAjouter.addActionListener(e -> {
            try {
                TypeConsommation type = (TypeConsommation) cmbType.getSelectedItem();
                int quantite = Integer.parseInt(txtQuantite.getText());

                Consommation_Energie c = new Consommation_Energie(selectedBatimentId, type, quantite);
                dbManager.saveConsommation(c);
                actualiserTableauConsommation();
                dialog.dispose();
                JOptionPane.showMessageDialog(InterfaceGraphique.this, "Consommation ajoutée!");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Erreur: Veuillez entrer une quantité valide!");
            }
        });

        btnAnnuler.addActionListener(e -> dialog.dispose());

        panelBoutons.add(btnAjouter);
        panelBoutons.add(btnAnnuler);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(panelBoutons, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void supprimerBatimentSelectionne() {
        int selectedRow = tableauBatiments.getSelectedRow();
        if (selectedRow >= 0) {
            int batimentId = (int) modelBatiments.getValueAt(selectedRow, 0);
            batiments.removeIf(b -> b.getId() == batimentId);
            dbManager.deleteBatiment(batimentId);
            actualiserTableauBatiments();
            modelConsommation.setRowCount(0);
            JOptionPane.showMessageDialog(this, "Bâtiment supprimé!");
        } else {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un bâtiment!");
        }
    }

    private void supprimerConsommationSelectionnee() {
        int selectedRow = tableauConsommation.getSelectedRow();
        if (selectedRow >= 0) {
            int consomationId = (int) modelConsommation.getValueAt(selectedRow, 0);
            dbManager.deleteConsommation(consomationId);
            actualiserTableauConsommation();
            JOptionPane.showMessageDialog(this, "Consommation supprimée!");
        } else {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une consommation!");
        }
    }

    private void afficherDetailsBatiment() {
        int selectedRow = tableauBatiments.getSelectedRow();
        if (selectedRow >= 0) {
            Batiment b = batiments.get(selectedRow);
            StringBuilder details = new StringBuilder();
            details.append("ID: ").append(b.getId()).append("\n");
            details.append("Nom: ").append(b.getNom()).append("\n");
            details.append("Type: ").append(b.getType()).append("\n");
            details.append("Nombre d'étages: ").append(b.getNombreEtages()).append("\n\n");

            if (b instanceof Maison) {
                details.append("Nombre de chambres: ").append(((Maison) b).getNombreChambres());
            } else if (b instanceof Appartement) {
                details.append("Nombre de chambres: ").append(((Appartement) b).getNombreChambres());
            } else if (b instanceof Bureau) {
                details.append("Nombre de bureaux: ").append(((Bureau) b).getNombreBureau());
            } else if (b instanceof Local_commercial) {
                details.append("Surface: ").append(((Local_commercial) b).getSurface()).append(" m²");
            } else if (b instanceof Batiment_Universitaire) {
                details.append("Nombre de salles: ").append(((Batiment_Universitaire) b).getNombreSalles());
            } else if (b instanceof Autre_Structure) {
                details.append("Description: ").append(((Autre_Structure) b).getDescription());
            }

            JOptionPane.showMessageDialog(this, details.toString(), "Détails du Bâtiment", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un bâtiment!");
        }
    }

    private void afficherStatistiques() {
        if (selectedBatimentId < 0) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un bâtiment!");
            return;
        }

        List<Consommation_Energie> consommations = dbManager.getConsommationsByBatiment(selectedBatimentId);

        if (consommations.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Aucune consommation enregistrée pour ce bâtiment!");
            return;
        }

        int totalConsommation = 0;
        int maxConsommation = Integer.MIN_VALUE;
        String typeMax = "";
        int minConsommation = Integer.MAX_VALUE;
        String typeMin = "";

        for (Consommation_Energie c : consommations) {
            int quantite = c.getQuantité();
            totalConsommation += quantite;
            if (quantite > maxConsommation) {
                maxConsommation = quantite;
                typeMax = c.getType().getLabel();
            }
            if (quantite < minConsommation) {
                minConsommation = quantite;
                typeMin = c.getType().getLabel();
            }
        }

        double moyenne = (double) totalConsommation / consommations.size();

        StringBuilder stats = new StringBuilder();
        stats.append("=== Statistiques de Consommation ===\n\n");
        stats.append("Total: ").append(totalConsommation).append(" kWh\n");
        stats.append("Moyenne: ").append(String.format("%.2f", moyenne)).append(" kWh\n");
        stats.append("Maximum: ").append(typeMax).append(" (").append(maxConsommation).append(" kWh)\n");
        stats.append("Minimum: ").append(typeMin).append(" (").append(minConsommation).append(" kWh)");

        JOptionPane.showMessageDialog(this, stats.toString(), "Statistiques", JOptionPane.INFORMATION_MESSAGE);
    }

    private void effacerBase() {
        dbManager.clearDatabase();
        batiments.clear();
        modelBatiments.setRowCount(0);
        modelConsommation.setRowCount(0);
        JOptionPane.showMessageDialog(this, "Base de données effacée!");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new InterfaceGraphique());
    }
}
