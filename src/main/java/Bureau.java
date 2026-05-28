public class Bureau extends Batiment {
    private int nombreBureau;

    public Bureau(String nom, int nombreEtages, int nombreBureau) {
        super(nom, "Bureau", nombreEtages);
        this.nombreBureau = nombreBureau;
    }

    public Bureau(int id, String nom, int nombreEtages, int nombreBureau) {
        super(id, nom, "Bureau", nombreEtages);
        this.nombreBureau = nombreBureau;
    }

    public int getNombreBureau() {
        return nombreBureau;
    }

    public void setNombreBureau(int nombreBureau) {
        this.nombreBureau = nombreBureau;
    }

    @Override
    public String toString() {
        return "Bureau{" +
                "nom='" + getNom() + '\'' +
                ", nombreEtages=" + getNombreEtages() +
                ", nombreBureau=" + nombreBureau +
                '}';
    }
}
