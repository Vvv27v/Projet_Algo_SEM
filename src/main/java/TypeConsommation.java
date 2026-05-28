public enum TypeConsommation {
    ELECTRICITE("Électricité"),
    CHAUFFAGE_GAZ("Chauffage Gaz"),
    EAU("Eau"),
    ECLAIRAGE("Éclairage"),
    CLIMATISATION("Climatisation"),
    ENERGIES_RENOUVELABLES("Énergies Renouvelables");

    private final String label;

    TypeConsommation(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
