public class Consommation_Energie {
    private int id;
    private int batimentId;
    private TypeConsommation type;
    private int quantité;
    private static int nextId = 1;

    public Consommation_Energie(int batimentId, TypeConsommation type, int quantité) {
        this.id = nextId++;
        this.batimentId = batimentId;
        this.type = type;
        this.quantité = quantité;
    }

    public Consommation_Energie(int id, int batimentId, TypeConsommation type, int quantité) {
        this.id = id;
        this.batimentId = batimentId;
        this.type = type;
        this.quantité = quantité;
        if (id >= nextId) {
            nextId = id + 1;
        }
    }

    public int getId() {
        return id;
    }

    public int getBatimentId() {
        return batimentId;
    }

    public void setBatimentId(int batimentId) {
        this.batimentId = batimentId;
    }

    public TypeConsommation getType() {
        return type;
    }

    public void setType(TypeConsommation type) {
        this.type = type;
    }

    public int getQuantité() {
        return quantité;
    }

    public void setQuantité(int quantité) {
        this.quantité = quantité;
    }

    public String getNom() {
        return type.getLabel();
    }

    @Override
    public String toString() {
        return "Consommation_Energie{" +
                "id=" + id +
                ", batimentId=" + batimentId +
                ", type=" + type +
                ", quantité=" + quantité +
                '}';
    }
}
