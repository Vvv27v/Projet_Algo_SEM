public class Autre_Structure extends Batiment {
    private String description;

    public Autre_Structure(String nom, int nombreEtages, String description) {
        super(nom, "Autre_Structure", nombreEtages);
        this.description = description;
    }

    public Autre_Structure(int id, String nom, int nombreEtages, String description) {
        super(id, nom, "Autre_Structure", nombreEtages);
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Autre_Structure{" +
                "nom='" + getNom() + '\'' +
                ", nombreEtages=" + getNombreEtages() +
                ", description='" + description + '\'' +
                '}';
    }
}
