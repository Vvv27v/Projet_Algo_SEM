import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class InterfaceGraphique extends JFrame {
    private List<Batiment> batiments;
    private List<Consommation_Energie> consommations;
    private JTable tableauBatiments;
    private JTable tableauConsommation;
    private DefaultTableModel modelBatiments;
    private DefaultTableModel modelConsommation;

    public InterfaceGraphique() {
        batiments = new ArrayList<>();
        consommations = new ArrayList<>();
        
        // Configuration de la fenêtre
        setTitle("Gestion des Bâtiments et Consommation Énergétique");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setResizable(true);

        // Créer les panneaux
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel Bâtiments
        JPanel panelBatiments = creerPanelBatiments();
        
        // Panel Consommation
        JPanel panelConsommation = creerPanelConsommation();

        // Panel Onglets
        JTabbedPane onglets = new JTabbedPane();
        onglets.addTab("Bâtiments", panelBatiments);
        onglets.addTab("Consommation Énergétique", panelConsommation);

        panelPrincipal.add(onglets, BorderLayout.CENTER);

        // Panel boutons globaux
        JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton btnCharger = new JButton("Charger Données");
        JButton btnQuitter = new JButton("Quitter");

        btnCharger.addActionListener(e -> chargerDonnees());
        btnQuitter.addActionListener(e -> System.exit(0));

        panelBoutons.add(btnCharger);
        panelBoutons.add(btnQuitter);

        panelPrincipal.add(panelBoutons, BorderLayout.SOUTH);

        add(panelPrincipal);
        setVisible(true);
    }

    private JPanel creerPanelBatiments() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Gestion des Bâtiments"));

        // Créer le modèle et le tableau
        modelBatiments = new DefaultTableModel(
            new String[]{"Nom", "Type", "Étages", "Détails"}, 0
        );
        tableauBatiments = new JTable(modelBatiments);
        tableauBatiments.setFillsViewportHeight(true);
        tableauBatiments.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(tableauBatiments);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Panel boutons
        JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        
        JButton btnAjouter = new JButton("Ajouter");
        JButton btnModifier = new JButton("Modifier");
        JButton btnSupprimer = new JButton("Supprimer");
        JButton btnAffichageDetaille = new JButton("Détails");

        btnAjouter.addActionListener(e -> afficherDialogAjoutBatiment());
        btnModifier.addActionListener(e -> modifierBatimentSelectionne());
        btnSupprimer.addActionListener(e -> supprimerBatimentSelectionne());
        btnAffichageDetaille.addActionListener(e -> afficherDetailsBatiment());

        panelBoutons.add(btnAjouter);
        panelBoutons.add(btnModifier);
        panelBoutons.add(btnSupprimer);
        panelBoutons.add(btnAffichageDetaille);

        panel.add(panelBoutons, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel creerPanelConsommation() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Gestion de la Consommation Énergétique"));

        // Créer le modèle et le tableau
        modelConsommation = new DefaultTableModel(
            new String[]{"Type d'Énergie", "Quantité (kWh)"}, 0
        );
        tableauConsommation = new JTable(modelConsommation);
        tableauConsommation.setFillsViewportHeight(true);
        tableauConsommation.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(tableauConsommation);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Panel boutons
        JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        
        JButton btnAjouter = new JButton("Ajouter");
        JButton btnModifier = new JButton("Modifier");
        JButton btnSupprimer = new JButton("Supprimer");
        JButton btnStatistiques = new JButton("Statistiques");

        btnAjouter.addActionListener(e -> afficherDialogAjoutConsommation());
        btnModifier.addActionListener(e -> modifierConsommationSelectionnee());
        btnSupprimer.addActionListener(e -> supprimerConsommationSelectionnee());
        btnStatistiques.addActionListener(e -> afficherStatistiques());

        panelBoutons.add(btnAjouter);
        panelBoutons.add(btnModifier);
        panelBoutons.add(btnSupprimer);
        panelBoutons.add(btnStatistiques);

        panel.add(panelBoutons, BorderLayout.SOUTH);

        return panel;
    }

    private void chargerDonnees() {
        // Nettoyer les données existantes
        batiments.clear();
        consommations.clear();
        modelBatiments.setRowCount(0);
        modelConsommation.setRowCount(0);

        // Charger les bâtiments
        batiments.add(new Maison("Villa Moderne", 2, 4));
        batiments.add(new Appartement("Immeuble Centre-Ville", 8, 3));
        batiments.add(new Bureau("Bureau Tech", 5, 30));
        batiments.add(new Local_commercial("Centre Commercial", 3, 500));
        batiments.add(new Batiment_Universitaire("Université A", 6, 100));
        batiments.add(new Autre_Structure("Parking Souterrain", 3, "Parking"));

        // Charger les consommations
        consommations.add(new Consommation_Energie("Électricité", 5000));
        consommations.add(new Consommation_Energie("Chauffage Gaz", 3000));
        consommations.add(new Consommation_Energie("Eau", 1200));
        consommations.add(new Consommation_Energie("Électricité Éclairage", 800));

        // Actualiser les tableaux
        actualiserTableauBatiments();
        actualiserTableauConsommation();

        JOptionPane.showMessageDialog(this, "Données chargées avec succès!");
    }

    private void actualiserTableauBatiments() {
        modelBatiments.setRowCount(0);
        for (Batiment b : batiments) {
            String details = obtenirDetailsBatiment(b);
            modelBatiments.addRow(new Object[]{
                b.getNom(),
                b.getType(),
                b.getNombreEtages(),
                details
            });
        }
    }

    private void actualiserTableauConsommation() {
        modelConsommation.setRowCount(0);
        for (Consommation_Energie c : consommations) {
            modelConsommation.addRow(new Object[]{
                c.getNom(),
                c.getQuantité()
            });
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
                }

                if (b != null) {
                    batiments.add(b);
                    actualiserTableauBatiments();
                    dialog.dispose();
                    JOptionPane.showMessageDialog(this, "Bâtiment ajouté avec succès!");
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
        JDialog dialog = new JDialog(this, "Ajouter une Consommation", true);
        dialog.setSize(350, 150);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblNom = new JLabel("Type d'Énergie:");
        JTextField txtNom = new JTextField();
        JLabel lblQuantite = new JLabel("Quantité (kWh):");
        JTextField txtQuantite = new JTextField();

        panel.add(lblNom);
        panel.add(txtNom);
        panel.add(lblQuantite);
        panel.add(txtQuantite);

        JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnAjouter = new JButton("Ajouter");
        JButton btnAnnuler = new JButton("Annuler");

        btnAjouter.addActionListener(e -> {
            try {
                String nom = txtNom.getText();
                int quantite = Integer.parseInt(txtQuantite.getText());
                
                Consommation_Energie c = new Consommation_Energie(nom, quantite);
                consommations.add(c);
                actualiserTableauConsommation();
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Consommation ajoutée avec succès!");
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
            batiments.remove(selectedRow);
            actualiserTableauBatiments();
            JOptionPane.showMessageDialog(this, "Bâtiment supprimé!");
        } else {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un bâtiment!");
        }
    }

    private void supprimerConsommationSelectionnee() {
        int selectedRow = tableauConsommation.getSelectedRow();
        if (selectedRow >= 0) {
            consommations.remove(selectedRow);
            actualiserTableauConsommation();
            JOptionPane.showMessageDialog(this, "Consommation supprimée!");
        } else {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une consommation!");
        }
    }

    private void modifierBatimentSelectionne() {
        int selectedRow = tableauBatiments.getSelectedRow();
        if (selectedRow >= 0) {
            JOptionPane.showMessageDialog(this, "Modification non implémentée");
        } else {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un bâtiment!");
        }
    }

    private void modifierConsommationSelectionnee() {
        int selectedRow = tableauConsommation.getSelectedRow();
        if (selectedRow >= 0) {
            JOptionPane.showMessageDialog(this, "Modification non implémentée");
        } else {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une consommation!");
        }
    }

    private void afficherDetailsBatiment() {
        int selectedRow = tableauBatiments.getSelectedRow();
        if (selectedRow >= 0) {
            Batiment b = batiments.get(selectedRow);
            StringBuilder details = new StringBuilder();
            details.append("Nom: ").append(b.getNom()).append("\n");
            details.append("Type: ").append(b.getType()).append("\n");
            details.append("Nombre d'étages: ").append(b.getNombreEtages()).append("\n");

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
        if (consommations.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Aucune consommation enregistrée!");
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
                typeMax = c.getNom();
            }
            if (quantite < minConsommation) {
                minConsommation = quantite;
                typeMin = c.getNom();
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new InterfaceGraphique());
    }
}
