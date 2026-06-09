package Smart_Farm;


// ==================== ZONE (ABSTRACT) ====================

import javafx.beans.property.DoubleProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

abstract class Zone implements Suspendable , Serializable {
    protected int code;
    protected String nom;
    protected TypeZone type;
    protected StatutZone statut;
    protected boolean estSuspendu;
    protected List<Capteur> capteurs = new ArrayList<>();
    protected List<EnregistrementProduction> productions = new ArrayList<>();
    protected List<Alerte>  alerts = new ArrayList<>();
    protected List<Releve> relevances = new ArrayList<>();

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

    public void ajouterAlerte(Alerte a) { alerts.add(a); }
    public void supprimerAlerte(Alerte a) { alerts.remove(a); }

    public void ajouterReleve(Releve r) { relevances.add(r); }
    public void supprimerReleve(Releve r) { relevances.remove(r); }

    public void enregistrerProduction(double quantite, String typeProduction) {
        productions.add(new EnregistrementProduction(quantite, typeProduction));
    }

    public int getCode() { return code; }
    public String getNom() { return nom; }
    public TypeZone getType() { return type; }
    public StatutZone getStatut() { return statut; }
    public List<Capteur> getCapteurs() { return Collections.unmodifiableList(capteurs); }
    public List<EnregistrementProduction> getProductions() { return Collections.unmodifiableList(productions); }
    public List<Alerte> getAlerts() { return Collections.unmodifiableList(alerts); }
    public List<Releve> getRelevances() { return Collections.unmodifiableList(relevances); }
    public int getNbrEntite() { return 0; }

    public String afficherProduction() {
        StringBuilder res = new StringBuilder();

        for (EnregistrementProduction e : productions) {
            res.append(e.toString());
        }

        return res.toString();
    }



    public void setNom(String nom) { this.nom = nom; }
    public void setStatut(StatutZone statut) { this.statut = statut; }

    @Override
    public String toString() {
        return "Zone{" +
                "code=" + code +
                ", nom='" + nom + '\'' +
                ", type=" + type +
                ", statut=" + statut +
                '}';
    }
}


// ==================== ZONES ====================

class ZoneCulture extends Zone implements Serializable  {
    private List<Culture> cultures = new ArrayList<>();
    private List<CapteurSol> capteurSols = new ArrayList<>();
    private List<CapteurEnvironnemental> capteurEnvironnementals = new ArrayList<>();
    private static int Nbr;



    public ZoneCulture(int code, String nom, TypeZone type) {
        super(code, nom, type);
        Nbr++;
    }

    public static int getNbr() { return Nbr; }


    public void ajouterCulture(Culture c) { cultures.add(c); }
    public void supprimerCulture(Culture c) { cultures.remove(c); }
    public List<Culture> getCultures() { return Collections.unmodifiableList(cultures); }

    public int getNbrCultures() { return Nbr; }

    public void ajoutterCapteurSol(CapteurSol c) {
        capteurSols.add(c);
        capteurs.add(c);
    }

    public void ajoutterCapteurEnvironnementals(CapteurEnvironnemental c) {
        capteurEnvironnementals.add(c);
        capteurs.add(c);
    }

    public void SuprimerCapteurSol(CapteurSol c) {
        capteurSols.remove(c);
        capteurSols.remove(c);
    }

    public void SuprimerCapteurEnvironnementals(CapteurEnvironnemental c) {
        capteurEnvironnementals.remove(c);
        capteurs.remove(c);
    }



    @Override
    public int getNbrEntite() { return cultures.size(); }
}

class ZoneElevage extends Zone implements Serializable  {
    private List<Animal> animals = new ArrayList<>();
    private GeographicalLimits limitZone;
    private List<ProgAlimentation> programme = new ArrayList<>();
    private TypeZoneElevage typeZoneElevage;
    private static int Nbr;

    public ZoneElevage(int code, String nom, TypeZone type, TypeZoneElevage typeElevage, GeographicalLimits limitZone) {
        super(code, nom, type);
        this.typeZoneElevage = typeElevage;
        this.limitZone = limitZone;
        Nbr++;
    }

    public static int getNbr() { return Nbr; }


    public void ajouterRuminant(Ruminant a) { animals.add(a); }
    public void ajouterVollaile(Volaille a) { animals.add(a); }

    public void supprimerAnimal(Animal a) { animals.remove(a); }
    public void ajouterProgAlimentation(ProgAlimentation p) { programme.add(p); }

    public List<Animal> getAnimals() { return Collections.unmodifiableList(animals); }
    public List<ProgAlimentation> getProgramme() { return programme; }
    public GeographicalLimits getLimitZone() { return limitZone; }
    public TypeZoneElevage getTypeZoneElevage() { return typeZoneElevage; }
    public void setProgramme(List<ProgAlimentation> programme){ this.programme = Collections.unmodifiableList(programme); }

    @Override
    public int getNbrEntite() { return animals.size(); }

    public String afficherProgAlimentation(){
        StringBuilder res = new StringBuilder();

        for (ProgAlimentation a : programme){
            res.append(a.toString());
        }

        return res.toString();
    }
}

class ZoneAquacole extends Zone implements Serializable  {
    private List<Aquacole> aquacoles = new ArrayList<>();
    private List<ProgAlimentation> programme = new ArrayList<>();
    private List<CapteurEau> capteurEau = new ArrayList<>();
    private static int Nbr;


    public ZoneAquacole(int code, String nom, TypeZone type) {
        super(code, nom, type);
        Nbr++;
    }

    public static int getNbr() { return Nbr; }


    public void ajouterAquacole(Aquacole a) { aquacoles.add(a); }
    public void supprimerAquacole(Aquacole a) { aquacoles.remove(a); }
    public void ajouterProgAlimentation(ProgAlimentation p) { programme.add(p); }

    public void ajoutterCapteurEau(CapteurEau c) {
        capteurEau.add(c);
        capteurs.add(c);
    }
    public void SuprimerCapteurEau(CapteurEau c) {
        capteurEau.remove(c);
        capteurs.remove(c);
    }



    public List<Aquacole> getAquacoles() { return Collections.unmodifiableList(aquacoles); }
    public List<ProgAlimentation> getProgramme() { return programme; }
    public void setProgramme(List<ProgAlimentation> programme){ this.programme = Collections.unmodifiableList(programme); }

    public String afficherProgAlimentation(){
        StringBuilder res = new StringBuilder();
        for (Aquacole a : aquacoles){
            res.append(a.toString());
        }

        return res.toString();
    }

    @Override
    public int getNbrEntite() { return aquacoles.size(); }
}
