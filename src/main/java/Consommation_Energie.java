public class Consommation_Energie {
    private String nom;
    private int quantité;

    public Consommation_Energie(String nom, int quantité) {
        this.nom = nom;
        this.quantité = quantité;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getQuantité() {
        return quantité;
    }

    public void setQuantité(int quantité) {
        this.quantité = quantité;
    }

    @Override
    public String toString() {
        return "Consommation_Energie{" +
                "nom='" + nom + '\'' +
                ", quantité=" + quantité +
                '}';
    }
}
