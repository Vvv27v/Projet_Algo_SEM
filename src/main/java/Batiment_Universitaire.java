public class Batiment_Universitaire extends Batiment {
    private int nombreSalles;

    public Batiment_Universitaire(String nom, int nombreEtages, int nombreSalles) {
        super(nom, "Batiment_Universitaire", nombreEtages);
        this.nombreSalles = nombreSalles;
    }

    public Batiment_Universitaire(int id, String nom, int nombreEtages, int nombreSalles) {
        super(id, nom, "Batiment_Universitaire", nombreEtages);
        this.nombreSalles = nombreSalles;
    }

    public int getNombreSalles() {
        return nombreSalles;
    }

    public void setNombreSalles(int nombreSalles) {
        this.nombreSalles = nombreSalles;
    }

    @Override
    public String toString() {
        return "Batiment_Universitaire{" +
                "nom='" + getNom() + '\'' +
                ", nombreEtages=" + getNombreEtages() +
                ", nombreSalles=" + nombreSalles +
                '}';
    }
}
