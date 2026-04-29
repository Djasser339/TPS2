
import java.util.*;

// Enums


enum EtatSante{malade,sain,quarentaine}
enum Gravite{normal,avertissement,critique}
enum TypeZone{aquacole,elevage,culture}
enum StatutZone{ACTIVE,INACTIVE}
enum TypeGps{Bio,Eau,Sol,Gps}
enum TypeEspece{ruminant,volaille,aqua}
enum StadeCroissance{semis,germination,croissance,maturite,recolte}
enum FamilleCulture{Cereal,Legume,Fruit}
enum StatutCapteur{ACTIVE,INACTIVE}
enum TypeZoneElevage{Ruminant,Volaille}


// Interfaces

interface suspendable{
    void suspendre();
    void desactiver();
    boolean estSuspendu();

}

interface Entite{
    void AjoutterAnimal(String a);
    //void AjoutterHistorique(HistoriqueProd p);
}

// Class Zones

abstract class Zone implements suspendable {

    private int code;
    private String nom;
    private TypeZone Type;
    private  StatutZone Statut;
    private boolean estSuspendu;
    // ArrayList de Capteurs
    //ArrayList de Historique
    private int NbrEntite;

    public Zone(int code, String nom, TypeZone type) {
        this.code = code;
        this.nom = nom;
        Type = type;
        Statut = StatutZone.ACTIVE;
    }

    public int getCode() {
        return code;
    }

    public String getNom() {
        return nom;
    }

    public TypeZone getType() {
        return Type;
    }

    public StatutZone getStatut() {
        return Statut;
    }

    public boolean estSuspendu() {
        return estSuspendu;
    }

    public int getNbrEntite() {
        return NbrEntite;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setStatut(StatutZone statut) {
        Statut = statut;
    }

    public void setEstSuspendu(boolean estSuspendu) {
        this.estSuspendu = estSuspendu;
    }

    public void suspendre(){
        this.estSuspendu = true;

        // supendre Tout les capteurs ==> for each loop
    }
    public void desactiver(){
        this.setStatut(StatutZone.INACTIVE);
    }

    public void reactiver(){
        this.setStatut(StatutZone.ACTIVE);
        this.estSuspendu =false;

    }

    // ajoutter suprimer modifier capteur
    // ajoutter suprimer modifier capteur Historique
}



abstract class Culture{
    private FamilleCulture famille;
    private String DatePlantation;
    private String DateRecolte;
    private StadeCroissance StadeCroissance;

    private Interval ExigencePH;
    private Interval ExigenceHumidite;

    private int Temperature;
    private int Humidite;
    private int Pleuviometrie;
    private int PH;
    private int TeneurAzote;

    // ArrayList de HistoriqueProd


    public Culture(FamilleCulture famille, String datePlantation, String dateRecolte, Interval exigencePH, Interval exigenceHumidite) {
        this.famille = famille;
        DatePlantation = datePlantation;
        DateRecolte = dateRecolte;
        ExigencePH = exigencePH;
        ExigenceHumidite = exigenceHumidite;
    }

    public FamilleCulture getFamille() {
        return famille;
    }

    public String getDatePlantation() {
        return DatePlantation;
    }

    public String getDateRecolte() {
        return DateRecolte;
    }

    public StadeCroissance getStadeCroissance() {
        return StadeCroissance;
    }

    public Interval getExigencePH() {
        return ExigencePH;
    }

    public Interval getExigenceHumidite() {
        return ExigenceHumidite;
    }

    public int getTemperature() {
        return Temperature;
    }

    public int getHumidite() {
        return Humidite;
    }

    public int getPleuviometrie() {
        return Pleuviometrie;
    }

    public int getPH() {
        return PH;
    }

    public int getTeneurAzote() {
        return TeneurAzote;
    }

    public void setDateRecolte(String dateRecolte) {
        DateRecolte = dateRecolte;
    }

    public void setStadeCroissance(StadeCroissance stadeCroissance) {
        StadeCroissance = stadeCroissance;
    }

    public void setTemperature(int temperature) {
        Temperature = temperature;
    }

    public void setHumidite(int humidite) {
        Humidite = humidite;
    }

    public void setPleuviometrie(int pleuviometrie) {
        Pleuviometrie = pleuviometrie;
    }

    public void setPH(int PH) {
        this.PH = PH;
    }

    public void setTeneurAzote(int teneurAzote) {
        TeneurAzote = teneurAzote;
    }

    public String ConditionCroissance(){
        return "PH : [ " + ExigencePH.getMin() + " , " + ExigencePH.getMax() + " ] \n" +
               "Humidité : [ " + ExigenceHumidite.getMin() + " , " + ExigenceHumidite.getMax() + " ] \n";
    }

    public String AfficherStats(){
        return  "PH : " + this.PH+"\n"+
                "Humidité : " + this.Humidite+"\n"+
                "Pleuviometrie : " + this.Pleuviometrie+"\n"+
                "Temperature : " + this.Temperature;


    }

}

// Rajoutter Attribut String + Vérifier qu il existe dans le ARRAYLIST DE sTRING

class Cereal extends Culture{
    List<String> cultures = new ArrayList<>();


    public Cereal(FamilleCulture famille, String datePlantation, String dateRecolte, Interval exigencePH, Interval exigenceHumidite) {
        super(famille, datePlantation, dateRecolte, exigencePH, exigenceHumidite);
    }

    public List<String> getCultures() {
        return cultures;
    }

    public void addCultures(String culture) {
        cultures.add(culture);
    }

    public void removeCultures(String culture) {
        cultures.remove(culture);
    }
}

class Legume extends Culture{
    List<String> cultures = new ArrayList<>();

    public Legume(FamilleCulture famille, String datePlantation, String dateRecolte, Interval exigencePH, Interval exigenceHumidite) {
        super(famille, datePlantation, dateRecolte, exigencePH, exigenceHumidite);
    }

    public List<String> getCultures() {
        return cultures;
    }

    public void addCultures(String culture) {
        cultures.add(culture);
    }

    public void removeCultures(String culture) {
        cultures.remove(culture);
    }
}

class Fruit extends Culture{
    List<String> cultures = new ArrayList<>();

    public Fruit(FamilleCulture famille, String datePlantation, String dateRecolte, Interval exigencePH, Interval exigenceHumidite) {
        super(famille, datePlantation, dateRecolte, exigencePH, exigenceHumidite);
    }

    public List<String> getCultures() {
        return cultures;
    }

    public void addCultures(String culture) {
        cultures.add(culture);
    }

    public void removeCultures(String culture) {
        cultures.remove(culture);
    }
}

class ZoneCulture extends Zone{

    // ArrayList de Capteur Sol et Environement

    List<Culture>  cultures = new ArrayList<>();

    public ZoneCulture(int code, String nom, TypeZone type) {
        super(code, nom, type);
    }

    public void ajouterCulture(Culture c) {
        cultures.add(c);
    }

    // methodes ajouter suprimer capteur

}


class ZoneElevage extends Zone{
    private List<Animal> animals = new ArrayList<>();
    private GeographicalLimits LimitZone;
    private List<ProgAlimentation> programme = new ArrayList<>();
    private TypeZoneElevage typeZoneElevage;

    public ZoneElevage(int code, String nom, TypeZone type,TypeZoneElevage typeElevage , GeographicalLimits limitZone) {
        super(code, nom, type);
        typeZoneElevage = typeElevage;
        LimitZone = limitZone;
    }

    public List<Animal> getAnimals() {
        return animals;
    }

    public List<ProgAlimentation> getProgramme() {
        return programme;
    }

    public void ajouterAnimal(Animal a) {
        animals.add(a);
    }



    public void ajouterProgAlimentation(ProgAlimentation a) {
        programme.add(a);
    }

    public GeographicalLimits getLimitZone() {
        return LimitZone;
    }

    public TypeZoneElevage getTypeZoneElevage() {
        return typeZoneElevage;
    }
}

class ZoneAquacole extends Zone{
    private List<Aquacole> aquacoles = new ArrayList<>();
    private List<ProgAlimentation> programme = new ArrayList<>();
    //capteur eau


    public ZoneAquacole(int code, String nom, TypeZone type) {
        super(code, nom, type);
    }

    public void ajouterAquacole(Aquacole a) {
        aquacoles.add(a);
    }

    public void ajouterProgAlimentation(ProgAlimentation a) {
        programme.add(a);
    }

    //methode ajouter et supprimer capteurs eau


    public List<Aquacole> getAquacoles() {
        return aquacoles;
    }

    public List<ProgAlimentation> getProgramme() {
        return programme;
    }


}

// Class Animal

abstract class Animal {
    private int id;
    private String nom;
    private int Temperature;
    private int NivActivité;
    private TypeEspece Espece;
    private int age ;
    private int poid;
    private EtatSante etatSante;

    // private PosGeo GPS;
    //private ArrayList de Capteurs Bio
    // verifier Healthevents

    public Animal(int id, TypeEspece espece, String nom) {
        this.id = id;
        Espece = espece;
        this.nom = nom;
    }

    public boolean estMalade() {return (this.etatSante == EtatSante.malade);}

    public int getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public int getTemperature() {
        return Temperature;
    }

    public int getNivActivité() {
        return NivActivité;
    }

    public TypeEspece getEspece() {
        return Espece;
    }

    public int getAge() {
        return age;
    }

    public int getPoid() {
        return poid;
    }

    public EtatSante getEtatSante() {
        return etatSante;
    }

    public void setTemperature(int temperature) {
        Temperature = temperature;
    }

    public void setNivActivité(int nivActivité) {
        NivActivité = nivActivité;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setPoid(int poid) {
        this.poid = poid;
    }

    public void setEtatSante(EtatSante etatSante) {
        this.etatSante = etatSante;
    }

    // methode Alerte  ( dans Ruminant + vollaile )

    @Override
    public String toString() {
        return "animal{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", Temperature=" + Temperature +
                ", NivActivité=" + NivActivité +
                ", Espece=" + Espece +
                ", age=" + age +
                ", poid=" + poid +
                ", etatSante=" + etatSante +
                '}';
    }
}

class Ruminant extends Animal implements Entite{
    List<String> animals= new ArrayList<>();
    //List<HistriqueProd> Historique = new ArrayList<>();


    public Ruminant(int id, TypeEspece espece, String nom) {
        super(id, espece, nom);
    }

    @Override
    public void AjoutterAnimal(String a) {
        animals.add(a);
    }

    //public void AjoutterHistorique(HistoriqueProd p){}
}

class Volaille extends Animal implements Entite{
    List<String> animals= new ArrayList<>();
    //List<HistriqueProd> Historique = new ArrayList<>();


    public Volaille(int id, TypeEspece espece, String nom) {
        super(id, espece, nom);
    }

    @Override
    public void AjoutterAnimal(String a) {
        animals.add(a);
    }

    //public void AjoutterHistorique(HistoriqueProd p){}
}

class Aquacole extends Animal implements Entite{
    List<String> animals= new ArrayList<>();
    //List<HistriqueProd> Historique = new ArrayList<>();


    public Aquacole(int id, TypeEspece espece, String nom) {
        super(id, espece, nom);
    }

    @Override
    public void AjoutterAnimal(String a) {
        animals.add(a);
    }

    //public void AjoutterHistorique(HistoriqueProd p){}
}

// Historique

// Programme

class ProgAlimentation{
    private String TypeAliment;
    private double quantite;
    private String description;

    public ProgAlimentation(String typeAliment, double quantite) {
        TypeAliment = typeAliment;
        this.quantite = quantite;
    }

    public String getTypeAliment() {
        return TypeAliment;
    }

    public double getQuantite() {
        return quantite;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setQuantite(double quantite) {
        this.quantite = quantite;
    }

    public void setTypeAliment(String typeAliment) {
        TypeAliment = typeAliment;
    }
}



// class

class Interval{
    private  int min;
    private int max;

    public Interval(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public int getMin() {
        return min;
    }
    public int getMax() {
        return max;
    }
}

class GeographicalLimits{
    private String description;

    public GeographicalLimits(String description) {
        this.description = description;
    }

}

// App et Ferme

class Ferme{
    private String nom;
    private List<Zone> zones= new ArrayList<>();
    // ArrayList de Alert

    public Ferme(String nom) {
        this.nom = nom;
    }

    public String getNom() {
        return nom;
    }

    public List<Zone> getZones() {
        return zones;
    }



    public void ajouterZoneElevage(ZoneElevage zone){
        zones.add(zone);
    }

    public void ajouterZoneCulure(ZoneCulture zone){
        zones.add(zone);
    }

    public void ajouterZoneAquacole(ZoneAquacole zone){
        zones.add(zone);
    }

    public void suprimerZone(Zone zone){
        zones.remove(zone);
    }

    // methode ajoutter suprimer afficher alerte

}

class App{
    private String nom;
    private Ferme ferme;

    public App(String nom, Ferme ferme) {
        this.nom = nom;
        this.ferme = ferme;
    }

    public void desactiverZone(Zone zone){
        zone.desactiver();
        zone.suspendre();
    }

    public void ReactiverZone(Zone zone){
        zone.reactiver();
    }

    // modifier zone ?

    public void ajouterCulture(Culture culture,ZoneCulture zone){
        zone.ajouterCulture(culture);
    }

    public void ajouterRuminant(Ruminant ruminant,ZoneElevage zone){

        if(zone.getTypeZoneElevage() == TypeZoneElevage.Ruminant){
            zone.ajouterAnimal(ruminant);
        }else{
            System.out.println(ruminant.getNom() + ruminant.getId() + "n'est pas un Ruminant");
        }

    }

    public void ajouterVolaille(Volaille volaille,ZoneElevage zone){

        if(zone.getTypeZoneElevage() == TypeZoneElevage.Volaille){
            zone.ajouterAnimal(volaille);
        }else{
            System.out.println(volaille.getNom() + volaille.getId() + "n'est pas un Ruminant");
        }

    }

    public void ajouterAquacole(Aquacole aquacole,ZoneAquacole zone){
        zone.ajouterAquacole(aquacole);
    }

    public String afficherZones() {

        StringBuilder res = new StringBuilder();

        res.append("===== LISTE DES ZONES DE LA FERME =====\n\n");

        for (Zone z : ferme.getZones()) {

            res.append("Code : ").append(z.getCode()).append("\n");
            res.append("Nom : ").append(z.getNom()).append("\n");
            res.append("Type : ").append(z.getType()).append("\n");
            res.append("Statut : ").append(z.getStatut()).append("\n");
            res.append("Suspendu : ").append(z.estSuspendu()).append("\n");

            // ===============================
            // ZONE CULTURE
            // ===============================
            if (z instanceof ZoneCulture) {

                ZoneCulture zone = (ZoneCulture) z;

                res.append("--- Cultures ---\n");

                for (Culture c : zone.cultures) {

                    res.append("Famille : ")
                            .append(c.getFamille()).append("\n");

                    res.append("Date Plantation : ")
                            .append(c.getDatePlantation()).append("\n");

                    res.append("Date Récolte : ")
                            .append(c.getDateRecolte()).append("\n");

                    res.append(c.ConditionCroissance());
                    res.append(c.AfficherStats());
                    res.append("\n----------------\n");
                }
            }

            // ===============================
            // ZONE ELEVAGE
            // ===============================
            else if (z instanceof ZoneElevage) {

                ZoneElevage zone = (ZoneElevage) z;

                res.append("Type Elevage : ")
                        .append(zone.getTypeZoneElevage()).append("\n");

                res.append("--- Animaux ---\n");

                for (Animal a : zone.getAnimals()) {
                    res.append(a.toString()).append("\n");
                }

                res.append("--- Programme Alimentation ---\n");

                for (ProgAlimentation p : zone.getProgramme()) {

                    res.append("Aliment : ")
                            .append(p.getTypeAliment())
                            .append(" | Quantite : ")
                            .append(p.getQuantite())
                            .append("\n");
                }
            }

            // ===============================
            // ZONE AQUACOLE
            // ===============================
            else if (z instanceof ZoneAquacole) {

                ZoneAquacole zone = (ZoneAquacole) z;

                res.append("--- Aquacoles ---\n");

                for (Aquacole a : zone.getAquacoles()) {
                    res.append(a.toString()).append("\n");
                }

                res.append("--- Programme Alimentation ---\n");

                for (ProgAlimentation p : zone.getProgramme()) {

                    res.append("Aliment : ")
                            .append(p.getTypeAliment())
                            .append(" | Quantite : ")
                            .append(p.getQuantite())
                            .append("\n");
                }
            }

            res.append("\n====================================\n\n");
        }

        return res.toString();
    }

    // Enregistrer + afficher HistoriqueProd


}

// POSGEO = capteurGPS


public class Main {

    public static void main(String[] args) {

        // =========================
        // CREATION FERME + APP
        // =========================
        Ferme ferme = new Ferme("Smart Farm");
        App app = new App("Farm Manager", ferme);


        // =========================
        // ZONE CULTURE
        // =========================
        ZoneCulture zoneCulture =
                new ZoneCulture(1, "Zone Ble", TypeZone.culture);

        Interval ph = new Interval(6,8);
        Interval hum = new Interval(40,70);

        Cereal ble = new Cereal(
                FamilleCulture.Cereal,
                "01/02/2026",
                "01/07/2026",
                ph,
                hum
        );

        ble.setPH(7);
        ble.setHumidite(55);
        ble.setTemperature(25);
        ble.setPleuviometrie(120);

        app.ajouterCulture(ble, zoneCulture);

        ferme.ajouterZoneCulure(zoneCulture);


        // =========================
        // ZONE ELEVAGE RUMINANT
        // =========================
        GeographicalLimits limit =
                new GeographicalLimits("Nord Ferme");

        ZoneElevage zoneElevage =
                new ZoneElevage(
                        2,
                        "Zone Vaches",
                        TypeZone.elevage,
                        TypeZoneElevage.Ruminant,
                        limit
                );

        Ruminant vache1 =
                new Ruminant(101, TypeEspece.ruminant, "Vache A");

        vache1.setAge(4);
        vache1.setPoid(450);
        vache1.setEtatSante(EtatSante.sain);
        vache1.setTemperature(38);

        app.ajouterRuminant(vache1, zoneElevage);

        ProgAlimentation prog1 =
                new ProgAlimentation("Herbe", 5.5);

        zoneElevage.ajouterProgAlimentation(prog1);

        ferme.ajouterZoneElevage(zoneElevage);


        // =========================
        // ZONE VOLAILLE
        // =========================
        ZoneElevage zonePoulet =
                new ZoneElevage(
                        3,
                        "Zone Poulets",
                        TypeZone.elevage,
                        TypeZoneElevage.Volaille,
                        new GeographicalLimits("Sud Ferme")
                );

        Volaille poulet =
                new Volaille(201, TypeEspece.volaille, "Poulet 1");

        poulet.setAge(1);
        poulet.setPoid(2);
        poulet.setEtatSante(EtatSante.sain);

        app.ajouterVolaille(poulet, zonePoulet);

        ferme.ajouterZoneElevage(zonePoulet);


        // =========================
        // ZONE AQUACOLE
        // =========================
        ZoneAquacole zoneAquacole =
                new ZoneAquacole(
                        4,
                        "Bassin Poissons",
                        TypeZone.aquacole
                );

        Aquacole poisson =
                new Aquacole(301, TypeEspece.aqua, "Tilapia");

        poisson.setAge(2);
        poisson.setEtatSante(EtatSante.sain);

        app.ajouterAquacole(poisson, zoneAquacole);

        ProgAlimentation progPoisson =
                new ProgAlimentation("Granules", 2.3);

        zoneAquacole.ajouterProgAlimentation(progPoisson);

        ferme.ajouterZoneAquacole(zoneAquacole);


        // =========================
        // TEST DESACTIVATION ZONE
        // =========================
        app.desactiverZone(zonePoulet);


        // =========================
        // AFFICHAGE FINAL
        // =========================
        System.out.println(app.afficherZones());
    }
}
