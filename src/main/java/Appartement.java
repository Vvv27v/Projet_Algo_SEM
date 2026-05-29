clpublic class Appartement extends Batiment {
    private int nombreChambres;

    public Appartement(String nom, int nombreEtages, int nombreChambres) {
        super(nom, "Appartement", nombreEtages);
        this.nombreChambres = nombreChambres;
    }

    public Appartement(int id, String nom, int nombreEtages, int nombreChambres) {
        super(id, nom, "Appartement", nombreEtages);
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
        return "Appartement{" +
                "nom='" + getNom() + '\'' +
                ", nombreEtages=" + getNombreEtages() +
                ", nombreChambres=" + nombreChambres +
                '}';
    }
}
