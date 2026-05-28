public enum EnergyUnit {
    KILOWATT_HEURE("kWh"),
    LITRE("L"),
    METRE_CUBE("m³"),
    KILOGRAM("kg");

    private final String label;

    EnergyUnit(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static EnergyUnit getUnitForType(TypeConsommation type) {
        return switch (type) {
            case ELECTRICITE, ECLAIRAGE, CLIMATISATION, ENERGIES_RENOUVELABLES -> KILOWATT_HEURE;
            case CHAUFFAGE_GAZ -> METRE_CUBE;
            case EAU -> LITRE;
        };
    }

    @Override
    public String toString() {
        return label;
    }
}
