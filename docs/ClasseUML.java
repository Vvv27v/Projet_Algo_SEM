public class Batiment {
    private String nom;


    public Batiment(String nom, String type) {
        this.nom = nom;
        this.type = (Maison, Appartement, Bureau, Local_commercial, Batiment_Universitaire, Autre_Structure);
    }

    public String getNom() {
        return nom;
    }

    public int getType() {
        return type;
    }
} 

public class Maison extends Batiment {
    private int nombreChambres;

    public Maison(String nom, int nombreEtages, int nombreChambres) {
        super(nom, nombreEtages);
        this.nombreChambres = nombreChambres;
    }

    public int getNombreChambres() {
        return nombreChambres;
    }
}
public class Appartement extends Batiment {
    private int nombreChambres;

    public Appartement(String nom, int nombreEtages, int nombreChambres) {
        super(nom, nombreEtages);
        this.nombreChambres = nombreChambres;
    }

    public int getNombreChambres() {
        return nombreChambres;
    }
}
public class Bureau extends Batiment {
    private int nombreChambres;

    public Bureau(String nom, int nombreEtages, int nombreChambres) {
        super(nom, nombreEtages);
        this.nombreChambres = nombreChambres;
    }

    public int getNombreChambres() {
        return nombreChambres;
    }
}
public class Local_commercial extends Batiment {
    private int nombreChambres;

    public Local_commercial(String nom, int nombreEtages, int nombreChambres) {
        super(nom, nombreEtages);
        this.nombreChambres = nombreChambres;
    }

    public int getNombreChambres() {
        return nombreChambres;
    }
}

public class Batiment_Universitaire extends Batiment {
    private int nombreChambres;

    public Batiment_Universitaire(String nom, int nombreEtages, int nombreChambres) {
        super(nom, nombreEtages);
        this.nombreChambres = nombreChambres;
    }

    public int getNombreChambres() {
        return nombreChambres;
    }
}

public class Autre_Structure extends Batiment {
    private int nombreChambres;

    public Autre_Structure(String nom, int nombreEtages, int nombreChambres) {
        super(nom, nombreEtages);
        this.nombreChambres = nombreChambres;
    }

    public int getNombreChambres() {
        return nombreChambres;
    }
}

public class Consommation_Energie {
    private String nom;
    private int quantité;

    public Consommation_Energie(String nom, int quantité) {
        this.nom = nom;
        this.quantité = quantité;
    }
