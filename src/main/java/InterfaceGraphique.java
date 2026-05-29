import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.*;

public class InterfaceGraphique extends JFrame {

    // ── Couleurs ──────────────────────────────────────────────────────────────
    private static final Color SIDEBAR  = new Color(30, 42, 58);
    private static final Color ACTIVE   = new Color(52, 152, 219);
    private static final Color PRIMARY  = new Color(52, 152, 219);
    private static final Color SUCCESS  = new Color(46, 204, 113);
    private static final Color WARNING  = new Color(243, 156, 18);
    private static final Color DANGER   = new Color(231, 76, 60);
    private static final Color BG       = new Color(245, 246, 250);
    private static final Color TEXT     = new Color(44, 62, 80);
    private static final Color MUTED    = new Color(127, 140, 141);

    // ── État ──────────────────────────────────────────────────────────────────
    private final int userId;
    private final List<Batiment> batiments = new ArrayList<>();
    private final DatabaseManager db;

    // ── Navigation ────────────────────────────────────────────────────────────
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private final Map<String, JButton> navBtns = new HashMap<>();
    private String activeNav = "dashboard";

    // ── Composants tableau de bord ────────────────────────────────────────────
    private JLabel lblBatiments, lblConsoTotal, lblCoutTotal, lblTopBat;
    private DefaultTableModel modelRecent;

    // ── Composants bâtiments ──────────────────────────────────────────────────
    private DefaultTableModel modelBat;
    private JTable tableBat;
    private JTextArea detailsArea;

    // ── Composants consommations ──────────────────────────────────────────────
    private DefaultTableModel modelConso;
    private JTable tableConso;
    private JComboBox<String> comboBatConso;

    // ── Composants graphiques ─────────────────────────────────────────────────
    private JComboBox<String> comboBatChart;
    private JComboBox<String> comboChartType;
    private JPanel chartHolder;

    // ── Composants statistiques ───────────────────────────────────────────────
    private JComboBox<String> comboBatStats;
    private JTextArea statsArea;

    // ─────────────────────────────────────────────────────────────────────────
    public InterfaceGraphique(int userId) {
        this.userId = userId;
        this.db = new DatabaseManager();
        batiments.addAll(db.getBatimentsByUser(userId));
        buildUI();
        if (batiments.isEmpty()) loadTestData();
        else refreshAll();
        setVisible(true);
    }

    // ── Construction de l'UI ──────────────────────────────────────────────────
    private void buildUI() {
        setTitle("Smart Energy Manager");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1400, 820);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1050, 650));

        JPanel root = new JPanel(new BorderLayout());
        root.add(buildSidebar(), BorderLayout.WEST);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BG);
        contentPanel.add(buildDashboard(),      "dashboard");
        contentPanel.add(buildBatiments(),      "batiments");
        contentPanel.add(buildConsommations(),  "consommations");
        contentPanel.add(buildGraphiques(),     "graphiques");
        contentPanel.add(buildStatistiques(),   "statistiques");

        root.add(contentPanel, BorderLayout.CENTER);
        setContentPane(root);
        setNav("dashboard");
    }

    private JPanel buildSidebar() {
        JPanel sb = new JPanel();
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));
        sb.setBackground(SIDEBAR);
        sb.setPreferredSize(new Dimension(210, 0));

        // Logo
        JPanel logo = new JPanel(new BorderLayout());
        logo.setBackground(new Color(20, 30, 44));
        logo.setMaximumSize(new Dimension(210, 65));
        logo.setBorder(BorderFactory.createEmptyBorder(15, 18, 15, 18));
        JLabel logoLbl = new JLabel("⚡ Smart Energy");
        logoLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        logoLbl.setForeground(Color.WHITE);
        logo.add(logoLbl);
        sb.add(logo);
        sb.add(Box.createVerticalStrut(12));

        String[][] items = {
            {"dashboard",     "🏠", "Tableau de bord"},
            {"batiments",     "🏢", "Bâtiments"},
            {"consommations", "⚡", "Consommations"},
            {"graphiques",    "📊", "Graphiques"},
            {"statistiques",  "📈", "Statistiques"},
        };
        for (String[] item : items) {
            JButton btn = navBtn(item[1] + "  " + item[2], item[0]);
            navBtns.put(item[0], btn);
            sb.add(btn);
            sb.add(Box.createVerticalStrut(3));
        }

        sb.add(Box.createVerticalGlue());

        JButton btnTest = actionBtn("📥 Données test", WARNING);
        btnTest.setMaximumSize(new Dimension(188, 34));
        btnTest.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnTest.addActionListener(e -> loadTestData());
        sb.add(btnTest);
        sb.add(Box.createVerticalStrut(8));

        JButton btnQuit = actionBtn("❌ Quitter", DANGER);
        btnQuit.setMaximumSize(new Dimension(188, 34));
        btnQuit.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnQuit.addActionListener(e -> System.exit(0));
        sb.add(btnQuit);
        sb.add(Box.createVerticalStrut(18));

        return sb;
    }

    private JButton navBtn(String text, String key) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(new Color(180, 200, 220));
        btn.setBackground(SIDEBAR);
        btn.setBorder(BorderFactory.createEmptyBorder(11, 18, 11, 18));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(210, 42));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { if (!key.equals(activeNav)) btn.setBackground(new Color(44,62,80)); }
            public void mouseExited(MouseEvent e)  { if (!key.equals(activeNav)) btn.setBackground(SIDEBAR); }
        });
        btn.addActionListener(e -> {
            setNav(key);
            cardLayout.show(contentPanel, key);
            switch (key) {
                case "consommations" -> { refreshCombos(); refreshConsoTable(); }
                case "graphiques"    -> { refreshCombos(); }
                case "statistiques"  -> { refreshCombos(); }
                case "dashboard"     -> refreshDashboard();
            }
        });
        return btn;
    }

    private void setNav(String key) {
        if (navBtns.containsKey(activeNav)) {
            navBtns.get(activeNav).setBackground(SIDEBAR);
            navBtns.get(activeNav).setForeground(new Color(180, 200, 220));
        }
        activeNav = key;
        if (navBtns.containsKey(key)) {
            navBtns.get(key).setBackground(ACTIVE);
            navBtns.get(key).setForeground(Color.WHITE);
        }
    }

    // ── Tableau de bord ───────────────────────────────────────────────────────
    private JPanel buildDashboard() {
        JPanel p = page("Tableau de bord", "Vue d'ensemble de votre consommation énergétique");

        JPanel cards = new JPanel(new GridLayout(1, 4, 15, 0));
        cards.setBackground(BG);
        lblBatiments  = new JLabel("0"); lblBatiments.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblConsoTotal = new JLabel("0"); lblConsoTotal.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblCoutTotal  = new JLabel("0.00 €"); lblCoutTotal.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTopBat     = new JLabel("-"); lblTopBat.setFont(new Font("Segoe UI", Font.BOLD, 20));
        cards.add(summaryCard("🏢 Bâtiments",    lblBatiments,  PRIMARY, "gérés"));
        cards.add(summaryCard("⚡ Consommation",  lblConsoTotal, SUCCESS, "relevés total"));
        cards.add(summaryCard("💶 Coût estimé",   lblCoutTotal,  WARNING, "euros"));
        cards.add(summaryCard("🔥 Plus actif",    lblTopBat,     DANGER,  "bâtiment"));

        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setBackground(BG);
        body.add(cards, BorderLayout.NORTH);

        JLabel t = new JLabel("Relevés récents");
        t.setFont(new Font("Segoe UI", Font.BOLD, 14)); t.setForeground(TEXT);
        modelRecent = new DefaultTableModel(new String[]{"Bâtiment","Type","Quantité","Unité","Coût (€)","Date"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tbl = new JTable(modelRecent); styleTable(tbl);
        JPanel tp = new JPanel(new BorderLayout(0, 8)); tp.setBackground(BG);
        tp.add(t, BorderLayout.NORTH); tp.add(new JScrollPane(tbl), BorderLayout.CENTER);
        body.add(tp, BorderLayout.CENTER);

        p.add(body, BorderLayout.CENTER);
        return p;
    }

    private JPanel summaryCard(String title, JLabel val, Color accent, String sub) {
        JPanel c = new JPanel(new BorderLayout(0, 4));
        c.setBackground(Color.WHITE);
        c.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(228,228,228)),
            BorderFactory.createEmptyBorder(16,16,16,16)));
        JLabel t = new JLabel(title); t.setFont(new Font("Segoe UI", Font.BOLD, 11)); t.setForeground(MUTED);
        val.setForeground(accent);
        JLabel s = new JLabel(sub); s.setFont(new Font("Segoe UI", Font.PLAIN, 11)); s.setForeground(MUTED);
        JPanel inner = new JPanel(new BorderLayout(0,2)); inner.setBackground(Color.WHITE);
        inner.add(t, BorderLayout.NORTH); inner.add(val, BorderLayout.CENTER); inner.add(s, BorderLayout.SOUTH);
        JPanel bar = new JPanel(); bar.setBackground(accent); bar.setPreferredSize(new Dimension(4,0));
        c.add(inner, BorderLayout.CENTER); c.add(bar, BorderLayout.WEST);
        return c;
    }

    private void refreshDashboard() {
        if (lblBatiments == null) return;
        lblBatiments.setText(String.valueOf(batiments.size()));
        double cout = 0; int total = 0;
        Map<String,Integer> bConso = new LinkedHashMap<>();
        if (modelRecent != null) modelRecent.setRowCount(0);
        for (Batiment b : batiments) {
            for (Consommation_Energie c : db.getConsommationsByBatiment(b.getId())) {
                total++; cout += c.getCout();
                bConso.merge(b.getNom(), c.getQuantité(), Integer::sum);
                if (modelRecent != null)
                    modelRecent.addRow(new Object[]{b.getNom(), c.getType().getLabel(),
                        c.getQuantité(), c.getUnit().getLabel(),
                        String.format("%.2f", c.getCout()), c.getDateHeure()});
            }
        }
        lblConsoTotal.setText(String.valueOf(total));
        lblCoutTotal.setText(String.format("%.2f €", cout));
        String top = bConso.entrySet().stream().max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse("-");
        lblTopBat.setText(top.length() > 14 ? top.substring(0, 14) + "…" : top);
    }

    // ── Bâtiments ─────────────────────────────────────────────────────────────
    private JPanel buildBatiments() {
        JPanel p = page("Gestion des Bâtiments", "Créez, modifiez et supprimez vos bâtiments");

        JPanel body = new JPanel(new BorderLayout(14, 0));
        body.setBackground(BG);

        // Table card
        JPanel tc = card(new BorderLayout(0, 10));
        JPanel th = new JPanel(new BorderLayout()); th.setBackground(Color.WHITE);
        JLabel tl = new JLabel("Liste"); tl.setFont(new Font("Segoe UI", Font.BOLD, 14)); tl.setForeground(TEXT);
        th.add(tl, BorderLayout.WEST);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); btns.setBackground(Color.WHITE);
        JButton bAdd = actionBtn("+ Ajouter", SUCCESS);   bAdd.addActionListener(e -> addBatimentDialog());
        JButton bEdt = actionBtn("✏ Modifier", PRIMARY);  bEdt.addActionListener(e -> editBatimentDialog());
        JButton bDel = actionBtn("🗑 Supprimer", DANGER); bDel.addActionListener(e -> deleteBatiment());
        JButton bClo = actionBtn("⎘ Cloner", WARNING);    bClo.addActionListener(e -> cloneBatiment());
        btns.add(bAdd); btns.add(bEdt); btns.add(bDel); btns.add(bClo);
        th.add(btns, BorderLayout.EAST);
        modelBat = new DefaultTableModel(new String[]{"ID","Nom","Type","Étages","Détail"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tableBat = new JTable(modelBat); styleTable(tableBat);
        tableBat.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tableBat.getSelectedRow() >= 0) {
                int idx = tableBat.getSelectedRow();
                if (idx < batiments.size() && detailsArea != null)
                    detailsArea.setText(batimentDetails(batiments.get(idx)));
            }
        });
        tc.add(th, BorderLayout.NORTH);
        tc.add(new JScrollPane(tableBat), BorderLayout.CENTER);
        body.add(tc, BorderLayout.CENTER);

        // Details card
        JPanel dc = card(new BorderLayout(0, 8));
        dc.setPreferredSize(new Dimension(270, 0));
        JLabel dl = new JLabel("Détails"); dl.setFont(new Font("Segoe UI", Font.BOLD, 14)); dl.setForeground(TEXT);
        detailsArea = new JTextArea("Sélectionnez un bâtiment.");
        detailsArea.setEditable(false); detailsArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        detailsArea.setForeground(TEXT); detailsArea.setBackground(Color.WHITE);
        detailsArea.setLineWrap(true); detailsArea.setWrapStyleWord(true);
        dc.add(dl, BorderLayout.NORTH); dc.add(new JScrollPane(detailsArea), BorderLayout.CENTER);
        body.add(dc, BorderLayout.EAST);

        p.add(body, BorderLayout.CENTER);
        refreshBatTable();
        return p;
    }

    private String batimentDetails(Batiment b) {
        StringBuilder sb = new StringBuilder();
        sb.append("ID : ").append(b.getId()).append("\nNom : ").append(b.getNom())
          .append("\nType : ").append(b.getType()).append("\nÉtages : ").append(b.getNombreEtages()).append("\n\n");
        if (b instanceof Maison m)          sb.append("Chambres : ").append(m.getNombreChambres());
        else if (b instanceof Appartement a) sb.append("Chambres : ").append(a.getNombreChambres());
        else if (b instanceof Bureau bu)    sb.append("Bureaux : ").append(bu.getNombreBureau());
        else if (b instanceof Local_commercial l) sb.append("Surface : ").append(l.getSurface()).append(" m²");
        else if (b instanceof Batiment_Universitaire u) sb.append("Salles : ").append(u.getNombreSalles());
        else if (b instanceof Autre_Structure a) sb.append("Desc : ").append(a.getDescription());
        List<Consommation_Energie> cs = db.getConsommationsByBatiment(b.getId());
        double cout = cs.stream().mapToDouble(Consommation_Energie::getCout).sum();
        sb.append("\n\n── Relevés : ").append(cs.size()).append("\n── Coût total : ").append(String.format("%.2f €", cout));
        return sb.toString();
    }

    private void addBatimentDialog() {
        JDialog d = dialog("Ajouter un Bâtiment", 420, 280);
        JPanel f = form(4);
        JTextField nom = new JTextField(); JComboBox<String> type = new JComboBox<>(
            new String[]{"Maison","Appartement","Bureau","Local_commercial","Batiment_Universitaire","Autre_Structure"});
        JTextField etages = new JTextField("1"); JTextField detail = new JTextField("0");
        row(f,"Nom :", nom); row(f,"Type :", type); row(f,"Étages :", etages); row(f,"Détail :", detail);
        JButton ok = actionBtn("Ajouter", SUCCESS);
        ok.addActionListener(e -> {
            try {
                if (nom.getText().trim().isEmpty()) { err(d,"Nom requis."); return; }
                String t = (String) type.getSelectedItem();
                int et = Integer.parseInt(etages.getText().trim());
                String det = detail.getText().trim();
                Batiment b = "Local_commercial".equals(t)
                    ? new Local_commercial(nom.getText().trim(), et, Double.parseDouble(det))
                    : "Autre_Structure".equals(t)
                    ? new Autre_Structure(nom.getText().trim(), et, det)
                    : makeBat(nom.getText().trim(), t, et, Integer.parseInt(det));
                if (b != null) { batiments.add(b); db.saveBatiment(b, det, userId); refreshAll(); d.dispose(); }
            } catch (Exception ex) { err(d, "Valeur invalide."); }
        });
        JButton cancel = actionBtn("Annuler", MUTED); cancel.addActionListener(e -> d.dispose());
        finishDialog(d, f, ok, cancel);
    }

    private void editBatimentDialog() {
        int idx = tableBat.getSelectedRow();
        if (idx < 0) { info("Sélectionnez un bâtiment."); return; }
        Batiment b = batiments.get(idx);
        JDialog d = dialog("Modifier — " + b.getNom(), 420, 240);
        JPanel f = form(3);
        JTextField nom = new JTextField(b.getNom());
        JTextField etages = new JTextField(String.valueOf(b.getNombreEtages()));
        JTextField det = new JTextField(detailStr(b));
        row(f,"Nom :", nom); row(f,"Étages :", etages); row(f,"Détail :", det);
        JButton ok = actionBtn("Enregistrer", SUCCESS);
        ok.addActionListener(e -> {
            try {
                b.setNom(nom.getText().trim());
                b.setNombreEtages(Integer.parseInt(etages.getText().trim()));
                db.saveBatiment(b, det.getText().trim(), userId);
                refreshAll(); d.dispose();
            } catch (Exception ex) { err(d,"Valeur invalide."); }
        });
        JButton cancel = actionBtn("Annuler", MUTED); cancel.addActionListener(e -> d.dispose());
        finishDialog(d, f, ok, cancel);
    }

    private void deleteBatiment() {
        int idx = tableBat.getSelectedRow();
        if (idx < 0) { info("Sélectionnez un bâtiment."); return; }
        Batiment b = batiments.get(idx);
        if (JOptionPane.showConfirmDialog(this,"Supprimer « "+b.getNom()+" » ?","Confirmation",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            db.deleteBatiment(b.getId()); batiments.remove(idx); refreshAll();
        }
    }

    private void cloneBatiment() {
        int idx = tableBat.getSelectedRow();
        if (idx < 0) { info("Sélectionnez un bâtiment."); return; }
        Batiment o = batiments.get(idx);
        String det = detailStr(o);
        Batiment clone;
        if      (o instanceof Maison m)         clone = new Maison("Copie – "+o.getNom(), o.getNombreEtages(), m.getNombreChambres());
        else if (o instanceof Appartement a)    clone = new Appartement("Copie – "+o.getNom(), o.getNombreEtages(), a.getNombreChambres());
        else if (o instanceof Bureau b)         clone = new Bureau("Copie – "+o.getNom(), o.getNombreEtages(), b.getNombreBureau());
        else if (o instanceof Local_commercial l) clone = new Local_commercial("Copie – "+o.getNom(), o.getNombreEtages(), l.getSurface());
        else if (o instanceof Batiment_Universitaire u) clone = new Batiment_Universitaire("Copie – "+o.getNom(), o.getNombreEtages(), u.getNombreSalles());
        else                                    clone = new Autre_Structure("Copie – "+o.getNom(), o.getNombreEtages(), o instanceof Autre_Structure a ? a.getDescription() : "");
        batiments.add(clone); db.saveBatiment(clone, det, userId); refreshAll();
    }

    // ── Consommations ─────────────────────────────────────────────────────────
    private JPanel buildConsommations() {
        JPanel p = page("Consommations Énergétiques", "Enregistrez et consultez vos relevés par bâtiment");
        JPanel body = new JPanel(new BorderLayout(0, 12)); body.setBackground(BG);

        // Filtre bâtiment
        JPanel filter = card(new FlowLayout(FlowLayout.LEFT, 14, 6));
        filter.add(lbl("Bâtiment :")); comboBatConso = new JComboBox<>();
        comboBatConso.setPreferredSize(new Dimension(230, 30));
        comboBatConso.addActionListener(e -> refreshConsoTable());
        filter.add(comboBatConso);
        JButton rBtn = actionBtn("↻ Actualiser", PRIMARY); rBtn.addActionListener(e -> refreshConsoTable());
        filter.add(rBtn);
        body.add(filter, BorderLayout.NORTH);

        // Tableau
        JPanel tc = card(new BorderLayout(0, 10));
        JPanel th = new JPanel(new BorderLayout()); th.setBackground(Color.WHITE);
        JLabel tl = new JLabel("Relevés"); tl.setFont(new Font("Segoe UI", Font.BOLD, 14)); tl.setForeground(TEXT);
        th.add(tl, BorderLayout.WEST);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); btns.setBackground(Color.WHITE);
        JButton bAdd = actionBtn("+ Ajouter", SUCCESS); bAdd.addActionListener(e -> addConsoDialog());
        JButton bDel = actionBtn("🗑 Supprimer", DANGER); bDel.addActionListener(e -> deleteConso());
        btns.add(bAdd); btns.add(bDel); th.add(btns, BorderLayout.EAST);
        modelConso = new DefaultTableModel(new String[]{"ID","Type","Quantité","Unité","Coût (€)","Date/Heure"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tableConso = new JTable(modelConso); styleTable(tableConso);
        tc.add(th, BorderLayout.NORTH); tc.add(new JScrollPane(tableConso), BorderLayout.CENTER);
        body.add(tc, BorderLayout.CENTER);
        p.add(body, BorderLayout.CENTER);
        return p;
    }

    private void refreshConsoTable() {
        if (modelConso == null || comboBatConso == null) return;
        modelConso.setRowCount(0);
        int idx = comboBatConso.getSelectedIndex();
        if (idx >= 0 && idx < batiments.size()) {
            for (Consommation_Energie c : db.getConsommationsByBatiment(batiments.get(idx).getId())) {
                modelConso.addRow(new Object[]{c.getId(), c.getType().getLabel(),
                    c.getQuantité(), c.getUnit().getLabel(),
                    String.format("%.2f", c.getCout()), c.getDateHeure()});
            }
        }
    }

    private void addConsoDialog() {
        if (batiments.isEmpty()) { info("Ajoutez d'abord un bâtiment."); return; }
        JDialog d = dialog("Ajouter une Consommation", 430, 310);
        JPanel f = form(5);
        String[] names = batiments.stream().map(b -> b.getNom()+" ("+b.getType()+")").toArray(String[]::new);
        JComboBox<String> bat = new JComboBox<>(names);
        int pre = comboBatConso != null ? comboBatConso.getSelectedIndex() : 0;
        if (pre >= 0 && pre < names.length) bat.setSelectedIndex(pre);
        JComboBox<TypeConsommation> type = new JComboBox<>(TypeConsommation.values());
        JLabel unitLbl = new JLabel("kWh"); unitLbl.setForeground(PRIMARY); unitLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        type.addActionListener(e -> { TypeConsommation t = (TypeConsommation)type.getSelectedItem();
            if (t!=null) unitLbl.setText(EnergyUnit.getUnitForType(t).getLabel()); });
        JTextField qty = new JTextField();
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        JTextField date = new JTextField(now);
        row(f,"Bâtiment :", bat); row(f,"Type :", type); row(f,"Unité :", unitLbl);
        row(f,"Quantité :", qty); row(f,"Date :", date);
        JButton ok = actionBtn("Ajouter", SUCCESS);
        ok.addActionListener(e -> {
            try {
                int q = Integer.parseInt(qty.getText().trim());
                if (q <= 0) { err(d,"Quantité doit être > 0."); return; }
                TypeConsommation t = (TypeConsommation) type.getSelectedItem();
                int batId = batiments.get(bat.getSelectedIndex()).getId();
                Consommation_Energie c = new Consommation_Energie(batId, t, q, date.getText().trim());
                int newId = db.saveConsommation(c); c.setId(newId);
                if (comboBatConso != null) comboBatConso.setSelectedIndex(bat.getSelectedIndex());
                refreshConsoTable(); refreshDashboard(); d.dispose();
                JOptionPane.showMessageDialog(this, "✔  Consommation ajoutée !", "Succès", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) { err(d,"Quantité invalide."); }
        });
        JButton cancel = actionBtn("Annuler", MUTED); cancel.addActionListener(e -> d.dispose());
        finishDialog(d, f, ok, cancel);
    }

    private void deleteConso() {
        int row = tableConso.getSelectedRow();
        if (row < 0) { info("Sélectionnez un relevé."); return; }
        int id = Integer.parseInt(modelConso.getValueAt(row, 0).toString());
        db.deleteConsommation(id); refreshConsoTable(); refreshDashboard();
    }

    // ── Graphiques ────────────────────────────────────────────────────────────
    private JPanel buildGraphiques() {
        JPanel p = page("Graphiques", "Visualisation de vos consommations énergétiques");
        JPanel body = new JPanel(new BorderLayout(0, 12)); body.setBackground(BG);

        JPanel ctrl = card(new FlowLayout(FlowLayout.LEFT, 14, 6));
        ctrl.add(lbl("Bâtiment :")); comboBatChart = new JComboBox<>(); comboBatChart.setPreferredSize(new Dimension(200, 30));
        ctrl.add(comboBatChart);
        ctrl.add(lbl("Graphique :")); comboChartType = new JComboBox<>(new String[]{"Barres par type","Camembert","Comparaison bâtiments"});
        comboChartType.setPreferredSize(new Dimension(200, 30)); ctrl.add(comboChartType);
        JButton show = actionBtn("📊 Afficher", PRIMARY); show.addActionListener(e -> drawChart()); ctrl.add(show);
        body.add(ctrl, BorderLayout.NORTH);

        chartHolder = new JPanel(new BorderLayout()); chartHolder.setBackground(Color.WHITE);
        chartHolder.setBorder(BorderFactory.createLineBorder(new Color(228,228,228)));
        JLabel ph = new JLabel("Sélectionnez un bâtiment et cliquez sur Afficher", SwingConstants.CENTER);
        ph.setForeground(MUTED); chartHolder.add(ph, BorderLayout.CENTER);
        body.add(chartHolder, BorderLayout.CENTER);
        p.add(body, BorderLayout.CENTER);
        return p;
    }

    private void drawChart() {
        if (chartHolder == null) return;
        chartHolder.removeAll();
        String ct = (String) comboChartType.getSelectedItem();
        int idx = comboBatChart.getSelectedIndex();
        JPanel chart;
        if ("Comparaison bâtiments".equals(ct)) {
            chart = chartComparaison();
        } else if (idx >= 0 && idx < batiments.size()) {
            List<Consommation_Energie> cs = db.getConsommationsByBatiment(batiments.get(idx).getId());
            chart = "Camembert".equals(ct) ? chartPie(cs, batiments.get(idx).getNom())
                                           : chartBars(cs, batiments.get(idx).getNom());
        } else { chart = new JPanel(); }
        chartHolder.add(chart, BorderLayout.CENTER);
        chartHolder.revalidate(); chartHolder.repaint();
    }

    private static final Color[] CHART_COLORS = {
        new Color(52,152,219), new Color(46,204,113), new Color(243,156,18),
        new Color(231,76,60),  new Color(155,89,182), new Color(22,160,133)
    };

    private JPanel chartBars(List<Consommation_Energie> cs, String titre) {
        Map<String,Double> data = new LinkedHashMap<>();
        for (TypeConsommation t : TypeConsommation.values()) {
            double s = cs.stream().filter(c -> c.getType()==t).mapToDouble(Consommation_Energie::getQuantité).sum();
            if (s > 0) data.put(t.getLabel(), s);
        }
        return new JPanel() {
            { setBackground(Color.WHITE); }
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int W=getWidth(), H=getHeight(), pL=70, pR=30, pT=50, pB=70;
                int cW=W-pL-pR, cH=H-pT-pB;
                g2.setColor(TEXT); g2.setFont(new Font("Segoe UI",Font.BOLD,13));
                FontMetrics fm=g2.getFontMetrics(); String tit="Consommation par type – "+titre;
                g2.drawString(tit, (W-fm.stringWidth(tit))/2, 32);
                if (data.isEmpty()) { g2.setColor(MUTED); g2.setFont(new Font("Segoe UI",Font.PLAIN,13)); g2.drawString("Aucune donnée",W/2-50,H/2); return; }
                double mx=data.values().stream().mapToDouble(Double::doubleValue).max().orElse(1);
                g2.setColor(new Color(200,200,200)); g2.drawLine(pL,pT,pL,pT+cH); g2.drawLine(pL,pT+cH,pL+cW,pT+cH);
                g2.setFont(new Font("Segoe UI",Font.PLAIN,10));
                for (int i=0;i<=4;i++) { int y=pT+cH-(int)(i*cH/4.0); g2.setColor(new Color(240,240,240)); g2.drawLine(pL,y,pL+cW,y); g2.setColor(MUTED); String v=String.format("%.0f",mx*i/4); g2.drawString(v,pL-g2.getFontMetrics().stringWidth(v)-4,y+4); }
                List<String> keys=new ArrayList<>(data.keySet()); int bGW=cW/Math.max(keys.size(),1), bW=(int)(bGW*.6);
                for (int i=0;i<keys.size();i++) {
                    double val=data.get(keys.get(i)); int bH=(int)(val/mx*cH);
                    int x=pL+i*bGW+(bGW-bW)/2, y=pT+cH-bH;
                    g2.setColor(CHART_COLORS[i%CHART_COLORS.length]); g2.fillRoundRect(x,y,bW,bH,6,6);
                    g2.setColor(TEXT); fm=g2.getFontMetrics(); String vs=String.format("%.0f",val);
                    g2.drawString(vs, x+(bW-fm.stringWidth(vs))/2, y-4);
                    String[] parts=keys.get(i).split(" "); int ly=pT+cH+14;
                    for (String part:parts) { g2.drawString(part, x+(bW-fm.stringWidth(part))/2, ly); ly+=12; }
                }
            }
        };
    }

    private JPanel chartPie(List<Consommation_Energie> cs, String titre) {
        Map<String,Double> data = new LinkedHashMap<>();
        for (TypeConsommation t : TypeConsommation.values()) {
            double s = cs.stream().filter(c -> c.getType()==t).mapToDouble(Consommation_Energie::getQuantité).sum();
            if (s > 0) data.put(t.getLabel(), s);
        }
        return new JPanel() {
            { setBackground(Color.WHITE); }
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int W=getWidth(), H=getHeight();
                g2.setColor(TEXT); g2.setFont(new Font("Segoe UI",Font.BOLD,13));
                String tit="Répartition – "+titre; FontMetrics fm=g2.getFontMetrics();
                g2.drawString(tit,(W-fm.stringWidth(tit))/2,32);
                if (data.isEmpty()) { g2.setColor(MUTED); g2.drawString("Aucune donnée",W/2-50,H/2); return; }
                double total=data.values().stream().mapToDouble(Double::doubleValue).sum();
                int sz=Math.min(W-220,H-80), px=40, py=50;
                double angle=0; List<String> keys=new ArrayList<>(data.keySet());
                for (int i=0;i<keys.size();i++) {
                    double arc=data.get(keys.get(i))/total*360;
                    g2.setColor(CHART_COLORS[i%CHART_COLORS.length]); g2.fillArc(px,py,sz,sz,(int)angle,(int)arc);
                    g2.setColor(Color.WHITE); g2.drawArc(px,py,sz,sz,(int)angle,(int)arc);
                    angle+=arc;
                }
                int lx=px+sz+20, ly=py+20; g2.setFont(new Font("Segoe UI",Font.PLAIN,11));
                for (int i=0;i<keys.size();i++) {
                    g2.setColor(CHART_COLORS[i%CHART_COLORS.length]); g2.fillRect(lx,ly,13,13);
                    g2.setColor(TEXT); g2.drawString(keys.get(i)+" ("+String.format("%.1f%%",data.get(keys.get(i))/total*100)+")", lx+18, ly+11);
                    ly+=22;
                }
            }
        };
    }

    private JPanel chartComparaison() {
        Map<String,Double> data = new LinkedHashMap<>();
        for (Batiment b : batiments) {
            double s = db.getConsommationsByBatiment(b.getId()).stream().mapToDouble(Consommation_Energie::getQuantité).sum();
            data.put(b.getNom(), s);
        }
        return new JPanel() {
            { setBackground(Color.WHITE); }
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int W=getWidth(),H=getHeight(),pL=160,pR=30,pT=50,pB=30;
                int cW=W-pL-pR, cH=H-pT-pB;
                g2.setColor(TEXT); g2.setFont(new Font("Segoe UI",Font.BOLD,13));
                String tit="Comparaison entre bâtiments"; FontMetrics fm=g2.getFontMetrics();
                g2.drawString(tit,(W-fm.stringWidth(tit))/2,32);
                if (data.isEmpty()) return;
                double mx=data.values().stream().mapToDouble(Double::doubleValue).max().orElse(1); if(mx==0)mx=1;
                List<String> keys=new ArrayList<>(data.keySet()); int bGH=cH/Math.max(keys.size(),1), bH=(int)(bGH*.55);
                g2.setFont(new Font("Segoe UI",Font.PLAIN,11));
                for (int i=0;i<keys.size();i++) {
                    double val=data.get(keys.get(i)); int bW=(int)(val/mx*cW);
                    int x=pL, y=pT+i*bGH+(bGH-bH)/2;
                    g2.setColor(new Color(235,235,235)); g2.fillRoundRect(x,y,cW,bH,4,4);
                    if (bW>0) { g2.setColor(CHART_COLORS[i%CHART_COLORS.length]); g2.fillRoundRect(x,y,bW,bH,4,4); }
                    g2.setColor(TEXT); fm=g2.getFontMetrics();
                    String lbl=keys.get(i).length()>20?keys.get(i).substring(0,20)+"…":keys.get(i);
                    g2.drawString(lbl, pL-fm.stringWidth(lbl)-8, y+bH/2+4);
                    String vs=String.format("%.0f",val);
                    if (bW>50) { g2.setColor(Color.WHITE); g2.drawString(vs, x+bW-fm.stringWidth(vs)-6, y+bH/2+4); }
                    else { g2.setColor(TEXT); g2.drawString(vs, x+bW+5, y+bH/2+4); }
                }
            }
        };
    }

    // ── Statistiques ──────────────────────────────────────────────────────────
    private JPanel buildStatistiques() {
        JPanel p = page("Statistiques & Analyses", "Analyse détaillée, coûts et détection d'anomalies");
        JPanel body = new JPanel(new BorderLayout(0, 12)); body.setBackground(BG);

        JPanel ctrl = card(new FlowLayout(FlowLayout.LEFT, 14, 6));
        ctrl.add(lbl("Bâtiment :")); comboBatStats = new JComboBox<>(); comboBatStats.setPreferredSize(new Dimension(220,30));
        ctrl.add(comboBatStats);
        JButton bOne = actionBtn("🔍 Analyser", PRIMARY); bOne.addActionListener(e -> statsOne());
        JButton bAll = actionBtn("📊 Tous", WARNING); bAll.addActionListener(e -> statsAll());
        ctrl.add(bOne); ctrl.add(bAll);
        body.add(ctrl, BorderLayout.NORTH);

        JPanel sc = card(new BorderLayout());
        statsArea = new JTextArea(); statsArea.setEditable(false);
        statsArea.setFont(new Font("Courier New", Font.PLAIN, 12)); statsArea.setForeground(TEXT);
        statsArea.setBackground(Color.WHITE); statsArea.setMargin(new Insets(12,12,12,12));
        sc.add(new JScrollPane(statsArea), BorderLayout.CENTER);
        body.add(sc, BorderLayout.CENTER);
        p.add(body, BorderLayout.CENTER);
        return p;
    }

    private void statsOne() {
        int idx = comboBatStats.getSelectedIndex();
        if (idx<0||idx>=batiments.size()) { statsArea.setText("Sélectionnez un bâtiment."); return; }
        Batiment b = batiments.get(idx);
        List<Consommation_Energie> cs = db.getConsommationsByBatiment(b.getId());
        if (cs.isEmpty()) { statsArea.setText("Aucune consommation enregistrée pour "+b.getNom()+"."); return; }
        double total=cs.stream().mapToDouble(Consommation_Energie::getQuantité).sum();
        double cout=cs.stream().mapToDouble(Consommation_Energie::getCout).sum();
        double avg=total/cs.size();
        double max=cs.stream().mapToDouble(Consommation_Energie::getQuantité).max().orElse(0);
        double min=cs.stream().mapToDouble(Consommation_Energie::getQuantité).min().orElse(0);
        String typeMax=cs.stream().max(Comparator.comparingInt(Consommation_Energie::getQuantité)).map(c->c.getType().getLabel()).orElse("-");
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════\n");
        sb.append("  RAPPORT – ").append(b.getNom().toUpperCase()).append("\n");
        sb.append("═══════════════════════════════════════\n\n");
        sb.append("📋 Bâtiment : ").append(b.getType()).append(", ").append(b.getNombreEtages()).append(" étage(s)\n\n");
        sb.append("📊 CONSOMMATION\n");
        sb.append(String.format("   Relevés    : %d%n", cs.size()));
        sb.append(String.format("   Total      : %.0f (unités mixtes)%n", total));
        sb.append(String.format("   Moyenne    : %.1f%n", avg));
        sb.append(String.format("   Maximum    : %.0f (%s)%n", max, typeMax));
        sb.append(String.format("   Minimum    : %.0f%n", min));
        sb.append("\n💶 COÛTS\n");
        sb.append(String.format("   Total      : %.2f €%n", cout));
        sb.append(String.format("   Mensuel    : %.2f € (estimé)%n", cout));
        sb.append(String.format("   Annuel     : %.2f € (estimé)%n", cout * 12));
        sb.append("\n📈 PAR TYPE\n");
        for (TypeConsommation t : TypeConsommation.values()) {
            List<Consommation_Energie> f=cs.stream().filter(c->c.getType()==t).collect(Collectors.toList());
            if (!f.isEmpty()) {
                double q=f.stream().mapToDouble(Consommation_Energie::getQuantité).sum();
                double c2=f.stream().mapToDouble(Consommation_Energie::getCout).sum();
                sb.append(String.format("   %-24s %5.0f %-4s  %6.2f €%n", t.getLabel(), q, EnergyUnit.getUnitForType(t).getLabel(), c2));
            }
        }
        sb.append("\n⚠️  ANOMALIES (> 2× la moyenne)\n");
        double seuil=avg*2; boolean found=false;
        for (Consommation_Energie c : cs) {
            if (c.getQuantité()>seuil) { sb.append(String.format("   → %s : %d (seuil %.0f)%n",c.getType().getLabel(),c.getQuantité(),seuil)); found=true; }
        }
        if (!found) sb.append("   Aucune anomalie détectée.\n");
        statsArea.setText(sb.toString());
    }

    private void statsAll() {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════\n");
        sb.append("  ANALYSE GLOBALE – TOUS LES BÂTIMENTS\n");
        sb.append("═══════════════════════════════════════════\n\n");
        sb.append(String.format("  %-22s  %7s  %9s%n", "Bâtiment", "Relevés", "Coût (€)"));
        sb.append("  ─────────────────────────────────────────\n");
        double gtCout=0;
        for (Batiment b : batiments) {
            List<Consommation_Energie> cs=db.getConsommationsByBatiment(b.getId());
            double c=cs.stream().mapToDouble(Consommation_Energie::getCout).sum();
            sb.append(String.format("  %-22s  %7d  %9.2f%n", b.getNom(), cs.size(), c));
            gtCout+=c;
        }
        sb.append("  ─────────────────────────────────────────\n");
        sb.append(String.format("  %-22s  %7s  %9.2f%n", "TOTAL", "", gtCout));
        statsArea.setText(sb.toString());
    }

    // ── Données test ──────────────────────────────────────────────────────────
    private void loadTestData() {
        db.clearDatabase(); batiments.clear();
        batiments.add(new Maison("Villa Moderna", 2, 4));
        batiments.add(new Appartement("Immeuble Centre", 8, 3));
        batiments.add(new Bureau("Tech Building", 5, 30));
        batiments.add(new Local_commercial("Mega Store", 3, 1500.0));
        batiments.add(new Batiment_Universitaire("Université Tech", 6, 120));
        batiments.add(new Autre_Structure("Station Solaire", 1, "Énergie Renouvelable"));
        for (Batiment b : batiments) db.saveBatiment(b, detailStr(b), userId);
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        Object[][] rows = {
            {0,TypeConsommation.ELECTRICITE,1200},{0,TypeConsommation.CHAUFFAGE_GAZ,80},{0,TypeConsommation.EAU,150},
            {1,TypeConsommation.ELECTRICITE,3500},{1,TypeConsommation.EAU,250},
            {2,TypeConsommation.CLIMATISATION,4000},{2,TypeConsommation.ELECTRICITE,2000},
            {3,TypeConsommation.ELECTRICITE,8000},{3,TypeConsommation.EAU,500},
            {4,TypeConsommation.ECLAIRAGE,1500},{4,TypeConsommation.ELECTRICITE,3000},
            {5,TypeConsommation.ENERGIES_RENOUVELABLES,2500},
        };
        for (Object[] r : rows) {
            Consommation_Energie c = new Consommation_Energie(batiments.get((int)r[0]).getId(),(TypeConsommation)r[1],(int)r[2],now);
            c.setId(db.saveConsommation(c));
        }
        refreshAll();
        JOptionPane.showMessageDialog(this, "✔  Données de test chargées !", "Succès", JOptionPane.INFORMATION_MESSAGE);
    }

    // ── Refresh global ────────────────────────────────────────────────────────
    private void refreshAll() {
        refreshBatTable();
        refreshCombos();
        refreshConsoTable();
        refreshDashboard();
    }

    private void refreshBatTable() {
        if (modelBat == null) return;
        modelBat.setRowCount(0);
        for (Batiment b : batiments)
            modelBat.addRow(new Object[]{b.getId(), b.getNom(), b.getType(), b.getNombreEtages(), detailStr(b)});
    }

    @SuppressWarnings("unchecked")
    private void refreshCombos() {
        JComboBox<?>[] combos = {comboBatConso, comboBatChart, comboBatStats};
        for (JComboBox<?> raw : combos) {
            if (raw == null) continue;
            JComboBox<String> cb = (JComboBox<String>) raw;
            cb.removeAllItems();
            for (Batiment b : batiments) cb.addItem(b.getNom() + " (" + b.getType() + ")");
        }
    }

    private String detailStr(Batiment b) {
        if (b instanceof Maison m)          return m.getNombreChambres() + " chambres";
        if (b instanceof Appartement a)     return a.getNombreChambres() + " chambres";
        if (b instanceof Bureau bu)         return bu.getNombreBureau() + " bureaux";
        if (b instanceof Local_commercial l) return l.getSurface() + " m²";
        if (b instanceof Batiment_Universitaire u) return u.getNombreSalles() + " salles";
        if (b instanceof Autre_Structure a) return a.getDescription();
        return "";
    }

    private Batiment makeBat(String nom, String type, int et, int det) {
        return switch (type) {
            case "Maison" -> new Maison(nom, et, det);
            case "Appartement" -> new Appartement(nom, et, det);
            case "Bureau" -> new Bureau(nom, et, det);
            case "Batiment_Universitaire" -> new Batiment_Universitaire(nom, et, det);
            default -> new Autre_Structure(nom, et, "");
        };
    }

    // ── Helpers UI ────────────────────────────────────────────────────────────
    private JPanel page(String title, String sub) {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setBackground(BG); p.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        JPanel h = new JPanel(new BorderLayout()); h.setBackground(BG);
        JLabel tl = new JLabel(title); tl.setFont(new Font("Segoe UI",Font.BOLD,22)); tl.setForeground(TEXT);
        JLabel sl = new JLabel(sub);   sl.setFont(new Font("Segoe UI",Font.PLAIN,13)); sl.setForeground(MUTED);
        h.add(tl, BorderLayout.NORTH); h.add(sl, BorderLayout.SOUTH);
        p.add(h, BorderLayout.NORTH);
        return p;
    }

    private JPanel card(LayoutManager lm) {
        JPanel c = new JPanel(lm); c.setBackground(Color.WHITE);
        c.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(228,228,228)),
            BorderFactory.createEmptyBorder(14,14,14,14)));
        return c;
    }

    private JPanel card(FlowLayout lm) { return card((LayoutManager) lm); }

    private JButton actionBtn(String txt, Color bg) {
        JButton b = new JButton(txt); b.setFont(new Font("Segoe UI",Font.BOLD,11));
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setBorder(BorderFactory.createEmptyBorder(7,13,7,13)); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(bg.darker()); }
            public void mouseExited(MouseEvent e)  { b.setBackground(bg); }
        });
        return b;
    }

    private void styleTable(JTable t) {
        t.setFont(new Font("Segoe UI",Font.PLAIN,12)); t.setRowHeight(30);
        t.setSelectionBackground(new Color(52,152,219,50)); t.setSelectionForeground(TEXT);
        t.setGridColor(new Color(242,242,242)); t.setShowVerticalLines(false);
        t.getTableHeader().setBackground(new Color(52,73,94)); t.getTableHeader().setForeground(Color.WHITE);
        t.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,12));
        t.getTableHeader().setPreferredSize(new Dimension(0,34));
        t.setIntercellSpacing(new Dimension(10,0));
    }

    private JPanel form(int rows) {
        JPanel f = new JPanel(new GridLayout(rows, 2, 10, 12));
        f.setBorder(BorderFactory.createEmptyBorder(18,18,8,18)); f.setBackground(Color.WHITE);
        return f;
    }

    private void row(JPanel f, String label, JComponent field) {
        JLabel l = new JLabel(label); l.setFont(new Font("Segoe UI",Font.PLAIN,12)); l.setForeground(TEXT);
        field.setFont(new Font("Segoe UI",Font.PLAIN,12)); f.add(l); f.add(field);
    }

    private JLabel lbl(String t) {
        JLabel l = new JLabel(t); l.setFont(new Font("Segoe UI",Font.PLAIN,12)); l.setForeground(TEXT); return l;
    }

    private JDialog dialog(String title, int w, int h) {
        JDialog d = new JDialog(this, title, true); d.setSize(w, h);
        d.setLocationRelativeTo(this); d.setLayout(new BorderLayout());
        d.getContentPane().setBackground(Color.WHITE); return d;
    }

    private void finishDialog(JDialog d, JPanel form, JButton ok, JButton cancel) {
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10)); btns.setBackground(Color.WHITE);
        btns.add(cancel); btns.add(ok);
        d.add(form, BorderLayout.CENTER); d.add(btns, BorderLayout.SOUTH); d.setVisible(true);
    }

    private void err(Component parent, String msg) { JOptionPane.showMessageDialog(parent, msg, "Erreur", JOptionPane.ERROR_MESSAGE); }
    private void info(String msg) { JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.INFORMATION_MESSAGE); }
}
