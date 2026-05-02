package Smart_Farm;


// ==================== ZONE (ABSTRACT) ====================

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

abstract class Zone implements Suspendable {
    private int code;
    private String nom;
    private TypeZone type;
    private StatutZone statut;
    private boolean estSuspendu;
    private List<Capteur> capteurs = new ArrayList<>();
    private List<EnregistrementProduction> productions = new ArrayList<>();

    public Zone(int code, String nom, TypeZone type) {
        this.code = code;
        this.nom = nom;
        this.type = type;
        this.statut = StatutZone.ACTIVE;
        this.estSuspendu = false;
    }

    public void suspendre() {
        this.estSuspendu = true;
        this.statut = StatutZone.SUSPENDU;
        for (Capteur c : capteurs) {
            c.suspendre();
        }
    }

    public void reactiver() {
        this.estSuspendu = false;
        this.statut = StatutZone.ACTIVE;
        for (Capteur c : capteurs) {
            c.reactiver();
        }
    }

    public boolean estSuspendu() { return estSuspendu; }

    public void desactiver() { this.statut = StatutZone.INACTIVE; }

    public void ajouterCapteur(Capteur c) { capteurs.add(c); }
    public void supprimerCapteur(Capteur c) { capteurs.remove(c); }

    public void enregistrerProduction(double quantite, String typeProduction) {
        productions.add(new EnregistrementProduction(quantite, typeProduction));
    }

    public int getCode() { return code; }
    public String getNom() { return nom; }
    public TypeZone getType() { return type; }
    public StatutZone getStatut() { return statut; }
    public List<Capteur> getCapteurs() { return Collections.unmodifiableList(capteurs); }
    public List<EnregistrementProduction> getProductions() { return Collections.unmodifiableList(productions); }
    public int getNbrEntite() { return 0; }

    public void setNom(String nom) { this.nom = nom; }
    public void setStatut(StatutZone statut) { this.statut = statut; }
}


// ==================== ZONES ====================

class ZoneCulture extends Zone {
    private List<Culture> cultures = new ArrayList<>();

    public ZoneCulture(int code, String nom, TypeZone type) {
        super(code, nom, type);
    }

    public void ajouterCulture(Culture c) { cultures.add(c); }
    public void supprimerCulture(Culture c) { cultures.remove(c); }
    public List<Culture> getCultures() { return Collections.unmodifiableList(cultures); }

    @Override
    public int getNbrEntite() { return cultures.size(); }
}

class ZoneElevage extends Zone {
    private List<Animal> animals = new ArrayList<>();
    private GeographicalLimits limitZone;
    private List<ProgAlimentation> programme = new ArrayList<>();
    private TypeZoneElevage typeZoneElevage;

    public ZoneElevage(int code, String nom, TypeZone type, TypeZoneElevage typeElevage, GeographicalLimits limitZone) {
        super(code, nom, type);
        this.typeZoneElevage = typeElevage;
        this.limitZone = limitZone;
    }

    public void ajouterAnimal(Animal a) { animals.add(a); }
    public void supprimerAnimal(Animal a) { animals.remove(a); }
    public void ajouterProgAlimentation(ProgAlimentation p) { programme.add(p); }

    public List<Animal> getAnimals() { return Collections.unmodifiableList(animals); }
    public List<ProgAlimentation> getProgramme() { return Collections.unmodifiableList(programme); }
    public GeographicalLimits getLimitZone() { return limitZone; }
    public TypeZoneElevage getTypeZoneElevage() { return typeZoneElevage; }

    @Override
    public int getNbrEntite() { return animals.size(); }
}

class ZoneAquacole extends Zone {
    private List<Aquacole> aquacoles = new ArrayList<>();
    private List<ProgAlimentation> programme = new ArrayList<>();

    public ZoneAquacole(int code, String nom, TypeZone type) {
        super(code, nom, type);
    }

    public void ajouterAquacole(Aquacole a) { aquacoles.add(a); }
    public void supprimerAquacole(Aquacole a) { aquacoles.remove(a); }
    public void ajouterProgAlimentation(ProgAlimentation p) { programme.add(p); }

    public List<Aquacole> getAquacoles() { return Collections.unmodifiableList(aquacoles); }
    public List<ProgAlimentation> getProgramme() { return Collections.unmodifiableList(programme); }

    @Override
    public int getNbrEntite() { return aquacoles.size(); }
}
