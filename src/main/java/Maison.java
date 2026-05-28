public class Maison extends Batiment {
    private int nombreChambres;

    public Maison(String nom, int nombreEtages, int nombreChambres) {
        super(nom, "Maison", nombreEtages);
        this.nombreChambres = nombreChambres;
    }

    public int getNombreChambres() {
        return nombreChambres;
    }

    public void setNombreChambres(int nombreChambres) {
        this.nombreChambres = nombreChambres;
    }

    @Override
    public String toString() {
        return "Maison{" +
                "nom='" + getNom() + '\'' +
                ", nombreEtages=" + getNombreEtages() +
                ", nombreChambres=" + nombreChambres +
                '}';
    }
}
