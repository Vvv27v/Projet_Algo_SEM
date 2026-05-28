public class Local_commercial extends Batiment {
    private double surface;

    public Local_commercial(String nom, int nombreEtages, double surface) {
        super(nom, "Local_commercial", nombreEtages);
        this.surface = surface;
    }

    public Local_commercial(int id, String nom, int nombreEtages, double surface) {
        super(id, nom, "Local_commercial", nombreEtages);
        this.surface = surface;
    }

    public double getSurface() {
        return surface;
    }

    public void setSurface(double surface) {
        this.surface = surface;
    }

    @Override
    public String toString() {
        return "Local_commercial{" +
                "nom='" + getNom() + '\'' +
                ", nombreEtages=" + getNombreEtages() +
                ", surface=" + surface +
                '}';
    }
}
