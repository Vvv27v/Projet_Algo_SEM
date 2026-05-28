public abstract class Batiment {
    private String nom;
    private String type;
    private int nombreEtages;

    public Batiment(String nom, String type, int nombreEtages) {
        this.nom = nom;
        this.type = type;
        this.nombreEtages = nombreEtages;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getNombreEtages() {
        return nombreEtages;
    }

    public void setNombreEtages(int nombreEtages) {
        this.nombreEtages = nombreEtages;
    }

    @Override
    public String toString() {
        return "Batiment{" +
                "nom='" + nom + '\'' +
                ", type='" + type + '\'' +
                ", nombreEtages=" + nombreEtages +
                '}';
    }
}
