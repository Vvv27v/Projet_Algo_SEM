public abstract class Batiment {
    private int id;
    private String nom;
    private String type;
    private int nombreEtages;
    private static int nextId = 1;

    public Batiment(String nom, String type, int nombreEtages) {
        this.id = nextId++;
        this.nom = nom;
        this.type = type;
        this.nombreEtages = nombreEtages;
    }

    public Batiment(int id, String nom, String type, int nombreEtages) {
        this.id = id;
        this.nom = nom;
        this.type = type;
        this.nombreEtages = nombreEtages;
        if (id >= nextId) {
            nextId = id + 1;
        }
    }

    public int getId() {
        return id;
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
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", type='" + type + '\'' +
                ", nombreEtages=" + nombreEtages +
                '}';
    }
}
