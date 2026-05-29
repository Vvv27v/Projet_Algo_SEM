public class Consommation_Energie {
    private int id;
    private int batimentId;
    private TypeConsommation type;
    private int quantite;
    private EnergyUnit unit;
    private double cout;
    private String dateHeure;

    // Nouveau relevé (l'ID sera assigné par la base de données)
    public Consommation_Energie(int batimentId, TypeConsommation type, int quantite, String dateHeure) {
        this.id = -1;
        this.batimentId = batimentId;
        this.type = type;
        this.quantite = quantite;
        this.unit = EnergyUnit.getUnitForType(type);
        this.cout = calculerCout(type, quantite);
        this.dateHeure = dateHeure;
    }

    // Chargement depuis la base de données
    public Consommation_Energie(int id, int batimentId, TypeConsommation type, int quantite,
                                EnergyUnit unit, double cout, String dateHeure) {
        this.id = id;
        this.batimentId = batimentId;
        this.type = type;
        this.quantite = quantite;
        this.unit = unit;
        this.cout = cout;
        this.dateHeure = dateHeure;
    }

    public static double calculerCout(TypeConsommation type, int quantite) {
        double tarif = switch (type) {
            case ELECTRICITE         -> 0.18;
            case CHAUFFAGE_GAZ       -> 1.20;
            case EAU                 -> 0.003;
            case ECLAIRAGE           -> 0.18;
            case CLIMATISATION       -> 0.20;
            case ENERGIES_RENOUVELABLES -> 0.05;
        };
        return tarif * quantite;
    }

    public int getId()                   { return id; }
    public void setId(int id)            { this.id = id; }
    public int getBatimentId()           { return batimentId; }
    public void setBatimentId(int v)     { this.batimentId = v; }
    public TypeConsommation getType()    { return type; }
    public void setType(TypeConsommation t) { this.type = t; }
    public int getQuantité()             { return quantite; }
    public void setQuantité(int v)       { this.quantite = v; this.cout = calculerCout(type, v); }
    public EnergyUnit getUnit()          { return unit; }
    public void setUnit(EnergyUnit u)    { this.unit = u; }
    public double getCout()              { return cout; }
    public void setCout(double v)        { this.cout = v; }
    public String getDateHeure()         { return dateHeure != null ? dateHeure : ""; }
    public void setDateHeure(String v)   { this.dateHeure = v; }
    public String getNom()               { return type.getLabel(); }

    @Override
    public String toString() {
        return "Consommation{id=" + id + ", batimentId=" + batimentId
                + ", type=" + type + ", quantite=" + quantite + "}";
    }
}
