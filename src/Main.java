import java.time.LocalDateTime;
import java.util.*;

// ========== ÉNUMÉRATIONS ==========
enum EtatSante { malade, sain, quarentaine }
enum Gravite { normal, avertissement, critique }
enum TypeZone { aquacole, elevage, culture }
enum StatutZone { ACTIVE, INACTIVE }
enum TypeEspece { ruminant, volaille, aqua }
enum StadeCroissance { semis, germination, croissance, maturite, recolte }
enum FamilleCulture { Cereal, Legume, Fruit }
enum StatutCapteur { ACTIVE, INACTIVE, SUSPENDU }
enum TypeZoneElevage { Ruminant, Volaille }
enum TypeMesure {
    TEMPERATURE, HUMIDITE, PLUVIOMETRIE,
    PH_SOL, HUMIDITE_SOL, AZOTE,
    TEMPERATURE_EAU, OXYGENE_DISSOUS, PH_EAU,
    TEMPERATURE_CORPORELLE, ACTIVITE_PAS_PAR_MINUTE
}

// ========== INTERFACES ==========
interface Suspendable {
    void suspendre();
    void reactiver();
    boolean estSuspendu();
    void desactiver();
}
interface Entite {
    void AjoutterAnimal(String a);
}

// ========== CLASSES DE BASE (SEUIL, RELEVE, ALERTE, GESTIONNAIRE) ==========
class Seuil {
    private double min, max;
    public Seuil(double min, double max) {
        if (min >= max) throw new IllegalArgumentException("min < max");
        this.min = min; this.max = max;
    }
    public boolean estHorsLimites(double valeur) { return valeur < min || valeur > max; }
    public Gravite evaluerGravite(double valeur) {
        double tolerance = (max - min) * 0.1;
        if (valeur < min - tolerance || valeur > max + tolerance) return Gravite.critique;
        if (valeur < min || valeur > max) return Gravite.avertissement;
        return Gravite.normal;
    }
    public double getMin() { return min; }
    public double getMax() { return max; }
}

abstract class Releve {
    private static long compteur = 0;
    private final long id;
    private final String idCapteur;
    private final LocalDateTime timestamp;
    private Gravite niveau;
    public Releve(String idCapteur) {
        this.id = ++compteur;
        this.idCapteur = idCapteur;
        this.timestamp = LocalDateTime.now();
        this.niveau = Gravite.normal;
    }
    public long getId() { return id; }
    public String getIdCapteur() { return idCapteur; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public Gravite getNiveau() { return niveau; }
    public void setNiveau(Gravite niveau) { this.niveau = niveau; }
    public abstract String getValeurAsString();
}

class ReleveNumerique extends Releve {
    private final double valeur;
    private final String unite;
    private final TypeMesure typeMesure;
    public ReleveNumerique(String idCapteur, double valeur, String unite, TypeMesure typeMesure) {
        super(idCapteur);
        this.valeur = valeur;
        this.unite = unite;
        this.typeMesure = typeMesure;
    }
    public double getValeur() { return valeur; }
    public String getUnite() { return unite; }
    public TypeMesure getTypeMesure() { return typeMesure; }
    public String getValeurAsString() { return valeur + " " + unite; }
}

class ReleveGPS extends Releve {
    private final double latitude, longitude;
    public ReleveGPS(String idCapteur, double latitude, double longitude) {
        super(idCapteur);
        this.latitude = latitude;
        this.longitude = longitude;
    }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getValeurAsString() { return String.format("lat=%.4f, lon=%.4f", latitude, longitude); }
}

class Alerte {
    private static long compteur = 0;
    private final long id;
    private final Releve releve;
    private final Gravite niveau;
    private final LocalDateTime dateCreation;
    private boolean acquittee, supprimee;
    private String zoneId;
    public Alerte(Releve releve, Gravite niveau, String zoneId) {
        this.id = ++compteur;
        this.releve = releve;
        this.niveau = niveau;
        this.dateCreation = LocalDateTime.now();
        this.zoneId = zoneId;
        this.acquittee = false;
        this.supprimee = false;
    }
    public void acquitter() { this.acquittee = true; }
    public void supprimer() { this.supprimee = true; }
    public long getId() { return id; }
    public Releve getReleve() { return releve; }
    public Gravite getNiveau() { return niveau; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public boolean isAcquittee() { return acquittee; }
    public boolean isSupprimee() { return supprimee; }
    public String getZoneId() { return zoneId; }
}

class GestionnaireCapteursAlertes {
    private static GestionnaireCapteursAlertes instance;
    private List<Capteur> tousLesCapteurs = new ArrayList<>();
    private Map<String, List<Capteur>> capteursParZone = new HashMap<>();
    private Map<String, Capteur> capteursParId = new HashMap<>();
    private List<Alerte> alertes = new ArrayList<>();

    private GestionnaireCapteursAlertes() {}
    public static GestionnaireCapteursAlertes getInstance() {
        if (instance == null) instance = new GestionnaireCapteursAlertes();
        return instance;
    }
    public void ajouterCapteur(Capteur capteur) {
        tousLesCapteurs.add(capteur);
        capteursParZone.computeIfAbsent(capteur.getZoneId(), k -> new ArrayList<>()).add(capteur);
        capteursParId.put(capteur.getId(), capteur);
    }
    public void declencherAlerte(Releve releve, Gravite niveau, String zoneId) {
        if (niveau != Gravite.normal) alertes.add(new Alerte(releve, niveau, zoneId));
    }
    public List<Capteur> getCapteursParZone(String zoneId) {
        return capteursParZone.getOrDefault(zoneId, Collections.emptyList());
    }
    public String afficherTableauBordZone(String zoneId) {
        List<Capteur> capteurs = getCapteursParZone(zoneId);
        if (capteurs.isEmpty()) return "Aucun capteur dans la zone " + zoneId;
        StringBuilder sb = new StringBuilder("\n--- TABLEAU DE BORD - ZONE " + zoneId + " ---\n");
        for (Capteur c : capteurs) {
            List<Releve> historique = c.getHistoriqueReleves();
            if (historique.isEmpty()) {
                sb.append("Capteur ").append(c.getId()).append(" : pas de relevé\n");
                continue;
            }
            Releve dernier = historique.get(historique.size()-1);
            String niveauStr, couleur;
            switch (dernier.getNiveau()) {
                case normal: niveauStr="NORMAL"; couleur="\u001B[32m"; break;
                case avertissement: niveauStr="AVERTISSEMENT"; couleur="\u001B[33m"; break;
                case critique: niveauStr="CRITIQUE"; couleur="\u001B[31m"; break;
                default: niveauStr="INCONNU"; couleur="";
            }
            sb.append("Capteur ").append(c.getId()).append(" (").append(c.getTypeNom()).append(") : ")
                    .append(couleur).append(niveauStr).append("\u001B[0m")
                    .append(" - Valeur : ").append(dernier.getValeurAsString()).append("\n");
        }
        return sb.toString();
    }
    public String afficherEvolutionReleves(String idCapteur) {
        Capteur capteur = capteursParId.get(idCapteur);
        if (capteur == null) return "Capteur inconnu : " + idCapteur;
        List<Releve> historique = capteur.getHistoriqueReleves();
        if (historique.isEmpty()) return "Aucun relevé pour ce capteur.";
        StringBuilder sb = new StringBuilder("\n=== ÉVOLUTION DES RELEVÉS - CAPTEUR " + idCapteur + " ===\n");
        sb.append(String.format("%-20s %-15s %-10s\n", "Date", "Valeur", "Niveau"));
        sb.append("------------------------------------------------\n");
        for (Releve r : historique)
            sb.append(String.format("%-20s %-15s %-10s\n", r.getTimestamp().toString(), r.getValeurAsString(), r.getNiveau().toString()));
        return sb.toString();
    }
    public String afficherEvolutionRelevesZone(String zoneId) {
        List<Capteur> capteurs = getCapteursParZone(zoneId);
        if (capteurs.isEmpty()) return "Aucun capteur dans la zone " + zoneId;
        StringBuilder sb = new StringBuilder("\n=== ÉVOLUTION DES RELEVÉS - ZONE " + zoneId + " ===\n");
        for (Capteur c : capteurs) sb.append(afficherEvolutionReleves(c.getId())).append("\n");
        return sb.toString();
    }
    public String afficherAlertesActives() {
        List<Alerte> actives = new ArrayList<>();
        for (Alerte a : alertes) if (!a.isAcquittee() && !a.isSupprimee()) actives.add(a);
        Collections.sort(actives, (a1,a2) -> a2.getNiveau().compareTo(a1.getNiveau()));
        if (actives.isEmpty()) return "Aucune alerte active.";
        StringBuilder sb = new StringBuilder("\n=== ALERTES ACTIVES ===\n");
        for (Alerte a : actives)
            sb.append("ID:").append(a.getId()).append(" | Niveau:").append(a.getNiveau())
                    .append(" | Capteur:").append(a.getReleve().getIdCapteur())
                    .append(" | Date:").append(a.getDateCreation())
                    .append(" | Valeur:").append(a.getReleve().getValeurAsString()).append("\n");
        return sb.toString();
    }
    public boolean acquitterAlerte(long id) {
        for (Alerte a : alertes) if (a.getId() == id && !a.isSupprimee()) { a.acquitter(); return true; }
        return false;
    }
    public boolean supprimerAlerte(long id) {
        for (Alerte a : alertes) if (a.getId() == id) { a.supprimer(); return true; }
        return false;
    }
    public List<Alerte> filtrerAlertes(String zoneId, TypeMesure typeCapteur, Gravite niveau,
                                       LocalDateTime debut, LocalDateTime fin) {
        List<Alerte> resultat = new ArrayList<>();
        for (Alerte a : alertes) {
            if (zoneId != null && !zoneId.equals(a.getZoneId())) continue;
            if (niveau != null && a.getNiveau() != niveau) continue;
            if (debut != null && a.getDateCreation().isBefore(debut)) continue;
            if (fin != null && a.getDateCreation().isAfter(fin)) continue;
            if (typeCapteur != null) {
                Releve r = a.getReleve();
                if (!(r instanceof ReleveNumerique)) continue;
                if (((ReleveNumerique) r).getTypeMesure() != typeCapteur) continue;
            }
            resultat.add(a);
        }
        Collections.sort(resultat, (a1,a2) -> a2.getNiveau().compareTo(a1.getNiveau()));
        return resultat;
    }
}

// ========== CAPTEURS ==========
abstract class Capteur implements Suspendable {
    protected final String id;
    protected String zoneId;
    protected StatutCapteur statut;
    protected List<Releve> historiqueReleves;
    protected GestionnaireCapteursAlertes gestionnaire = GestionnaireCapteursAlertes.getInstance();
    public Capteur(String id, String zoneId) {
        this.id = id;
        this.zoneId = zoneId;
        this.statut = StatutCapteur.ACTIVE;
        this.historiqueReleves = new ArrayList<>();
    }
    public abstract void envoyerReleve();
    public void changerStatut(StatutCapteur nouveauStatut) { this.statut = nouveauStatut; }
    @Override public void suspendre() { this.statut = StatutCapteur.SUSPENDU; }
    @Override public void reactiver() { this.statut = StatutCapteur.ACTIVE; }
    @Override public boolean estSuspendu() { return this.statut == StatutCapteur.SUSPENDU; }
    @Override public void desactiver() {
        this.statut = StatutCapteur.INACTIVE;
    }
    public void ajouterReleve(Releve releve) { historiqueReleves.add(releve); }
    public List<Releve> getHistoriqueReleves() { return Collections.unmodifiableList(historiqueReleves); }
    public List<Releve> filtrerRelevesParDate(LocalDateTime debut, LocalDateTime fin) {
        List<Releve> resultat = new ArrayList<>();
        for (Releve r : historiqueReleves)
            if (!r.getTimestamp().isBefore(debut) && !r.getTimestamp().isAfter(fin))
                resultat.add(r);
        return resultat;
    }
    public String getId() { return id; }
    public String getZoneId() { return zoneId; }
    public StatutCapteur getStatut() { return statut; }
    public void setZoneId(String zoneId) { this.zoneId = zoneId; }
    public abstract String getTypeNom();
}

abstract class CapteurNumerique extends Capteur {
    protected TypeMesure typeMesure;
    protected Seuil seuil;
    protected String unite;
    public CapteurNumerique(String id, String zoneId, TypeMesure typeMesure, Seuil seuil, String unite) {
        super(id, zoneId);
        this.typeMesure = typeMesure;
        this.seuil = seuil;
        this.unite = unite;
    }
    public void configurerSeuil(Seuil nouveauSeuil) { this.seuil = nouveauSeuil; }
    protected ReleveNumerique effectuerMesure(double valeur) {
        ReleveNumerique releve = new ReleveNumerique(this.id, valeur, this.unite, this.typeMesure);
        Gravite gravite = seuil.evaluerGravite(valeur);
        releve.setNiveau(gravite);
        this.ajouterReleve(releve);
        if (gravite != Gravite.normal) gestionnaire.declencherAlerte(releve, gravite, this.zoneId);
        return releve;
    }
    public TypeMesure getTypeMesure() { return typeMesure; }
    public Seuil getSeuil() { return seuil; }
    public String getUnite() { return unite; }
}

class CapteurEnvironnemental extends CapteurNumerique {
    public CapteurEnvironnemental(String id, String zoneId, TypeMesure type, Seuil seuil) {
        super(id, zoneId, type, seuil, type == TypeMesure.TEMPERATURE ? "°C" : type == TypeMesure.HUMIDITE ? "%" : "mm");
        if (type != TypeMesure.TEMPERATURE && type != TypeMesure.HUMIDITE && type != TypeMesure.PLUVIOMETRIE)
            throw new IllegalArgumentException("Type non valide");
    }
    @Override public void envoyerReleve() {
        if (statut != StatutCapteur.ACTIVE) return;
        Random rand = new Random();
        double v;
        switch (typeMesure) {
            case TEMPERATURE: v = 15 + rand.nextDouble() * 20; break;
            case HUMIDITE: v = 40 + rand.nextDouble() * 60; break;
            default: v = rand.nextDouble() * 50;
        }
        effectuerMesure(v);
    }
    @Override public String getTypeNom() { return "Environnemental"; }
}

class CapteurSol extends CapteurNumerique {
    public CapteurSol(String id, String zoneId, TypeMesure type, Seuil seuil) {
        super(id, zoneId, type, seuil, type == TypeMesure.PH_SOL ? "pH" : type == TypeMesure.HUMIDITE_SOL ? "%" : "mg/kg");
        if (type != TypeMesure.PH_SOL && type != TypeMesure.HUMIDITE_SOL && type != TypeMesure.AZOTE)
            throw new IllegalArgumentException("Type non valide");
    }
    @Override public void envoyerReleve() {
        if (statut != StatutCapteur.ACTIVE) return;
        Random rand = new Random();
        double v;
        switch (typeMesure) {
            case PH_SOL: v = 5.5 + rand.nextDouble() * 4; break;
            case HUMIDITE_SOL: v = 10 + rand.nextDouble() * 70; break;
            default: v = 20 + rand.nextDouble() * 180;
        }
        effectuerMesure(v);
    }
    @Override public String getTypeNom() { return "Sol"; }
}

class CapteurEau extends CapteurNumerique {
    public CapteurEau(String id, String zoneId, TypeMesure type, Seuil seuil) {
        super(id, zoneId, type, seuil, type == TypeMesure.TEMPERATURE_EAU ? "°C" : type == TypeMesure.OXYGENE_DISSOUS ? "mg/L" : "pH");
        if (type != TypeMesure.TEMPERATURE_EAU && type != TypeMesure.OXYGENE_DISSOUS && type != TypeMesure.PH_EAU)
            throw new IllegalArgumentException("Type non valide");
    }
    @Override public void envoyerReleve() {
        if (statut != StatutCapteur.ACTIVE) return;
        Random rand = new Random();
        double v;
        switch (typeMesure) {
            case TEMPERATURE_EAU: v = 10 + rand.nextDouble() * 15; break;
            case OXYGENE_DISSOUS: v = 4 + rand.nextDouble() * 8; break;
            default: v = 6.5 + rand.nextDouble() * 2;
        }
        effectuerMesure(v);
    }
    @Override public String getTypeNom() { return "Eau"; }
}

class CapteurBiometrique extends Capteur {
    private Seuil seuilTemperature, seuilActivite;
    public CapteurBiometrique(String id, String zoneId, Seuil seuilTemperature, Seuil seuilActivite) {
        super(id, zoneId);
        this.seuilTemperature = seuilTemperature;
        this.seuilActivite = seuilActivite;
    }
    public void configurerSeuils(Seuil temp, Seuil act) { this.seuilTemperature = temp; this.seuilActivite = act; }
    @Override public void envoyerReleve() {
        if (statut != StatutCapteur.ACTIVE) return;
        Random rand = new Random();
        double temp = 37 + rand.nextDouble() * 3;
        double act = 20 + rand.nextDouble() * 100;
        ReleveNumerique rTemp = new ReleveNumerique(this.id, temp, "°C", TypeMesure.TEMPERATURE_CORPORELLE);
        ReleveNumerique rAct = new ReleveNumerique(this.id, act, "pas/min", TypeMesure.ACTIVITE_PAS_PAR_MINUTE);
        Gravite gTemp = seuilTemperature.evaluerGravite(temp);
        Gravite gAct = seuilActivite.evaluerGravite(act);
        rTemp.setNiveau(gTemp);
        rAct.setNiveau(gAct);
        this.ajouterReleve(rTemp);
        this.ajouterReleve(rAct);
        if (gTemp != Gravite.normal) gestionnaire.declencherAlerte(rTemp, gTemp, this.zoneId);
        if (gAct != Gravite.normal) gestionnaire.declencherAlerte(rAct, gAct, this.zoneId);
    }
    @Override public String getTypeNom() { return "Biométrique"; }
    public Seuil getSeuilTemperature() { return seuilTemperature; }
    public Seuil getSeuilActivite() { return seuilActivite; }
}

class CapteurGPS extends Capteur {
    private double latitude, longitude;
    private List<double[]> historiquePositions = new ArrayList<>();
    public CapteurGPS(String id, String zoneId) { super(id, zoneId); }
    @Override public void envoyerReleve() {
        if (statut != StatutCapteur.ACTIVE) return;
        Random rand = new Random();
        this.latitude = 43.5 + (rand.nextDouble() - 0.5) * 0.1;
        this.longitude = 1.5 + (rand.nextDouble() - 0.5) * 0.1;
        historiquePositions.add(new double[]{latitude, longitude});
        ReleveGPS releveGPS = new ReleveGPS(this.id, latitude, longitude);
        this.ajouterReleve(releveGPS);
    }
    public boolean estHorsLimites(double latMin, double latMax, double lonMin, double lonMax) {
        return latitude < latMin || latitude > latMax || longitude < lonMin || longitude > lonMax;
    }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public List<double[]> getHistoriquePositions() { return Collections.unmodifiableList(historiquePositions); }
    @Override public String getTypeNom() { return "GPS"; }
}

// ========== CLASSES DU BINÔME (ZONES, CULTURES, ANIMAUX, FERME, APP) ==========
class Interval {
    private int min, max;
    public Interval(int min, int max) { this.min = min; this.max = max; }
    public int getMin() { return min; }
    public int getMax() { return max; }
}
class GeographicalLimits {
    private String description;
    public GeographicalLimits(String description) { this.description = description; }
    public String getDescription() { return description; }
}
class EnregistrementProduction {
    private String type; private double quantite; private LocalDateTime date;
    public EnregistrementProduction(String type, double quantite) { this.type = type; this.quantite = quantite; this.date = LocalDateTime.now(); }
    @Override public String toString() { return String.format("%s : %.2f le %s", type, quantite, date); }
}

abstract class Zone implements Suspendable {
    private int code; private String nom; private TypeZone type; private StatutZone statut; private boolean estSuspendu; private int nbrEntite;
    private List<Capteur> capteurs = new ArrayList<>();
    private List<EnregistrementProduction> productions = new ArrayList<>();
    public Zone(int code, String nom, TypeZone type) {
        this.code = code; this.nom = nom; this.type = type; this.statut = StatutZone.ACTIVE; this.estSuspendu = false;
    }
    public int getCode() { return code; }
    public String getNom() { return nom; }
    public TypeZone getType() { return type; }
    public StatutZone getStatut() { return statut; }
    @Override public boolean estSuspendu() { return estSuspendu; }
    public int getNbrEntite() { return nbrEntite; }
    public void setNbrEntite(int n) { nbrEntite = n; }
    public void setNom(String nom) { this.nom = nom; }
    public void setStatut(StatutZone statut) { this.statut = statut; }
    public void setEstSuspendu(boolean estSuspendu) { this.estSuspendu = estSuspendu; }
    public List<Capteur> getCapteurs() { return capteurs; }
    public void ajouterCapteur(Capteur c) { capteurs.add(c); }
    public List<EnregistrementProduction> getProductions() { return productions; }
    public void ajouterProduction(EnregistrementProduction p) { productions.add(p); }
    @Override public void suspendre() { this.estSuspendu = true; for (Capteur c : capteurs) c.suspendre(); }
    @Override public void desactiver() { this.statut = StatutZone.INACTIVE; }
    @Override public void reactiver() { this.statut = StatutZone.ACTIVE; this.estSuspendu = false; for (Capteur c : capteurs) c.reactiver(); }
}

// Cultures
abstract class Culture {
    private FamilleCulture famille; private String datePlantation; private String dateRecolte; private StadeCroissance stadeCroissance;
    private Interval exigencePH, exigenceHumidite; private int temperature, humidite, pleuviometrie, pH, teneurAzote;
    public Culture(FamilleCulture f, String dp, String dr, Interval ph, Interval h) {
        famille = f; datePlantation = dp; dateRecolte = dr; exigencePH = ph; exigenceHumidite = h; stadeCroissance = StadeCroissance.semis;
    }
    public FamilleCulture getFamille() { return famille; }
    public String getDatePlantation() { return datePlantation; }
    public String getDateRecolte() { return dateRecolte; }
    public StadeCroissance getStadeCroissance() { return stadeCroissance; }
    public void setStadeCroissance(StadeCroissance s) { stadeCroissance = s; }
    public Interval getExigencePH() { return exigencePH; }
    public Interval getExigenceHumidite() { return exigenceHumidite; }
    public int getTemperature() { return temperature; }
    public void setTemperature(int t) { temperature = t; }
    public int getHumidite() { return humidite; }
    public void setHumidite(int h) { humidite = h; }
    public int getPleuviometrie() { return pleuviometrie; }
    public void setPleuviometrie(int p) { pleuviometrie = p; }
    public int getPH() { return pH; }
    public void setPH(int pH) { this.pH = pH; }
    public int getTeneurAzote() { return teneurAzote; }
    public void setTeneurAzote(int az) { teneurAzote = az; }
    public String conditionCroissance() { return "PH : ["+exigencePH.getMin()+","+exigencePH.getMax()+"]\nHumidité : ["+exigenceHumidite.getMin()+","+exigenceHumidite.getMax()+"]\n"; }
    public String afficherStats() { return "PH : "+pH+"\nHumidité : "+humidite+"\nPluviométrie : "+pleuviometrie+"\nTempérature : "+temperature+"\n"; }
}
class Cereal extends Culture { public Cereal(FamilleCulture f, String dp, String dr, Interval ph, Interval h) { super(f,dp,dr,ph,h); } }
class Legume extends Culture { public Legume(FamilleCulture f, String dp, String dr, Interval ph, Interval h) { super(f,dp,dr,ph,h); } }
class Fruit extends Culture { public Fruit(FamilleCulture f, String dp, String dr, Interval ph, Interval h) { super(f,dp,dr,ph,h); } }

class ZoneCulture extends Zone {
    private List<Culture> cultures = new ArrayList<>();
    public ZoneCulture(int code, String nom, TypeZone type) { super(code, nom, type); }
    public void ajouterCulture(Culture c) { cultures.add(c); }
    public List<Culture> getCultures() { return cultures; }
}
class ZoneElevage extends Zone {
    private List<Animal> animals = new ArrayList<>();
    private GeographicalLimits limitZone;
    private List<ProgAlimentation> programme = new ArrayList<>();
    private TypeZoneElevage typeZoneElevage;
    public ZoneElevage(int code, String nom, TypeZone type, TypeZoneElevage te, GeographicalLimits limit) {
        super(code, nom, type);
        typeZoneElevage = te;
        limitZone = limit;
    }
    public TypeZoneElevage getTypeZoneElevage() { return typeZoneElevage; }
    public GeographicalLimits getLimitZone() { return limitZone; }
    public List<Animal> getAnimals() { return animals; }
    public void ajouterAnimal(Animal a) { animals.add(a); }
    public void ajouterRuminant(Ruminant r) { animals.add(r); }
    public void ajouterVollaile(Volaille v) { animals.add(v); }
    public List<ProgAlimentation> getProgramme() { return programme; }
    public void ajouterProgAlimentation(ProgAlimentation p) { programme.add(p); }
}
class ZoneAquacole extends Zone {
    private List<Aquacole> aquacoles = new ArrayList<>();
    private List<ProgAlimentation> programme = new ArrayList<>();
    public ZoneAquacole(int code, String nom, TypeZone type) { super(code, nom, type); }
    public void ajouterAquacole(Aquacole a) { aquacoles.add(a); }
    public List<Aquacole> getAquacoles() { return aquacoles; }
    public List<ProgAlimentation> getProgramme() { return programme; }
    public void ajouterProgAlimentation(ProgAlimentation p) { programme.add(p); }
}

abstract class Animal {
    private int id; private String nom; private int temperature; private int nivActivite; private TypeEspece espece; private int age, poid; private EtatSante etatSante;
    public Animal(int id, TypeEspece espece, String nom) { this.id = id; this.espece = espece; this.nom = nom; this.etatSante = EtatSante.sain; }
    public int getId() { return id; }
    public String getNom() { return nom; }
    public int getTemperature() { return temperature; }
    public void setTemperature(int t) { temperature = t; }
    public int getNivActivite() { return nivActivite; }
    public void setNivActivite(int na) { nivActivite = na; }
    public TypeEspece getEspece() { return espece; }
    public int getAge() { return age; }
    public void setAge(int a) { age = a; }
    public int getPoid() { return poid; }
    public void setPoid(int p) { poid = p; }
    public EtatSante getEtatSante() { return etatSante; }
    public void setEtatSante(EtatSante e) { etatSante = e; }
    @Override public String toString() { return "Animal{id="+id+", nom='"+nom+"', espece="+espece+", age="+age+", poids="+poid+", état="+etatSante+'}'; }
}
class Ruminant extends Animal implements Entite { public Ruminant(int id, TypeEspece e, String nom) { super(id,e,nom); } @Override public void AjoutterAnimal(String a) {} }
class Volaille extends Animal implements Entite { public Volaille(int id, TypeEspece e, String nom) { super(id,e,nom); } @Override public void AjoutterAnimal(String a) {} }
class Aquacole extends Animal implements Entite { public Aquacole(int id, TypeEspece e, String nom) { super(id,e,nom); } @Override public void AjoutterAnimal(String a) {} }

class ProgAlimentation {
    private String typeAliment; private double quantite;
    public ProgAlimentation(String type, double q) { typeAliment = type; quantite = q; }
    public String getTypeAliment() { return typeAliment; }
    public double getQuantite() { return quantite; }
}

class Ferme {
    private String nom; private List<Zone> zones = new ArrayList<>();
    public Ferme(String nom) { this.nom = nom; }
    public String getNom() { return nom; }
    public List<Zone> getZones() { return zones; }
    public void ajouterZoneElevage(ZoneElevage z) { zones.add(z); }
    public void ajouterZoneCulture(ZoneCulture z) { zones.add(z); }
    public void ajouterZoneAquacole(ZoneAquacole z) { zones.add(z); }
    public void supprimerZone(Zone z) { zones.remove(z); }
}

class App {
    private String nom; private Ferme ferme; private GestionnaireCapteursAlertes gestionnaire = GestionnaireCapteursAlertes.getInstance();
    public App(String nom, Ferme ferme) { this.nom = nom; this.ferme = ferme; }
    public void desactiverZone(Zone zone) { zone.suspendre(); }
    public void reactiverZone(Zone zone) { zone.reactiver(); }
    public void ajouterCulture(Culture culture, ZoneCulture zone) { zone.ajouterCulture(culture); }
    public void ajouterRuminant(Ruminant r, ZoneElevage zone) { if (zone.getTypeZoneElevage()==TypeZoneElevage.Ruminant) zone.ajouterAnimal(r); }
    public void ajouterVolaille(Volaille v, ZoneElevage zone) { if (zone.getTypeZoneElevage()==TypeZoneElevage.Volaille) zone.ajouterAnimal(v); }
    public void ajouterAquacole(Aquacole a, ZoneAquacole zone) { zone.ajouterAquacole(a); }
    public String afficherZones() {
        StringBuilder sb = new StringBuilder("===== ZONES DE LA FERME : "+ferme.getNom()+" =====\n\n");
        for (Zone z : ferme.getZones()) {
            sb.append("Code : ").append(z.getCode()).append("\nNom : ").append(z.getNom()).append("\nType : ").append(z.getType())
                    .append("\nStatut : ").append(z.getStatut()).append("\nSuspendu : ").append(z.estSuspendu()).append("\nNb entités : ").append(z.getNbrEntite()).append("\n--- Capteurs ---\n");
            for (Capteur c : z.getCapteurs()) sb.append("  ").append(c.getId()).append(" (").append(c.getTypeNom()).append(") - ").append(c.getStatut()).append("\n");
            if (z instanceof ZoneCulture zc) {
                sb.append("--- Cultures ---\n");
                for (Culture c : zc.getCultures()) sb.append("Famille : ").append(c.getFamille()).append("\n").append(c.conditionCroissance()).append(c.afficherStats()).append("\n");
            } else if (z instanceof ZoneElevage ze) {
                sb.append("--- Animaux ---\n");
                for (Animal a : ze.getAnimals()) sb.append(a).append("\n");
                sb.append("--- Programme alimentation ---\n");
                for (ProgAlimentation p : ze.getProgramme()) sb.append(p.getTypeAliment()).append(" : ").append(p.getQuantite()).append("\n");
            } else if (z instanceof ZoneAquacole za) {
                sb.append("--- Aquacoles ---\n");
                for (Aquacole a : za.getAquacoles()) sb.append(a).append("\n");
                sb.append("--- Programme alimentation ---\n");
                for (ProgAlimentation p : za.getProgramme()) sb.append(p.getTypeAliment()).append(" : ").append(p.getQuantite()).append("\n");
            }
            sb.append("\n====================================\n\n");
        }
        return sb.toString();
    }
}

// ========== CLASSE PRINCIPALE AVEC MENU ==========
public class Main {
    private static Scanner scanner = new Scanner(System.in);
    private static App app;
    private static Ferme ferme;
    private static GestionnaireCapteursAlertes gestionnaire = GestionnaireCapteursAlertes.getInstance();

    public static void main(String[] args) {
        ferme = new Ferme("Smart Farm");
        app = new App("Farm Manager", ferme);

        // Zone culture
        ZoneCulture zoneCulture = new ZoneCulture(1, "Zone Ble", TypeZone.culture);
        Interval ph = new Interval(6,8); Interval hum = new Interval(40,70);
        Cereal ble = new Cereal(FamilleCulture.Cereal, "01/02/2026", "01/07/2026", ph, hum);
        ble.setPH(7); ble.setHumidite(55); ble.setTemperature(25); ble.setPleuviometrie(120);
        app.ajouterCulture(ble, zoneCulture);
        ferme.ajouterZoneCulture(zoneCulture);

        // Zone élevage ruminant
        GeographicalLimits limit = new GeographicalLimits("Nord Ferme");
        ZoneElevage zoneElevage = new ZoneElevage(2, "Zone Vaches", TypeZone.elevage, TypeZoneElevage.Ruminant, limit);
        Ruminant vache1 = new Ruminant(101, TypeEspece.ruminant, "Vache A");
        vache1.setAge(4); vache1.setPoid(450); vache1.setEtatSante(EtatSante.sain); vache1.setTemperature(38);
        app.ajouterRuminant(vache1, zoneElevage);
        zoneElevage.ajouterProgAlimentation(new ProgAlimentation("Herbe", 5.5));
        ferme.ajouterZoneElevage(zoneElevage);

        // Zone volaille
        ZoneElevage zonePoulet = new ZoneElevage(3, "Zone Poulets", TypeZone.elevage, TypeZoneElevage.Volaille, new GeographicalLimits("Sud Ferme"));
        Volaille poulet = new Volaille(201, TypeEspece.volaille, "Poulet 1");
        poulet.setAge(1); poulet.setPoid(2); poulet.setEtatSante(EtatSante.sain);
        app.ajouterVolaille(poulet, zonePoulet);
        zonePoulet.ajouterProgAlimentation(new ProgAlimentation("Graines", 0.5));
        ferme.ajouterZoneElevage(zonePoulet);

        // Zone aquacole
        ZoneAquacole zoneAquacole = new ZoneAquacole(4, "Bassin Poissons", TypeZone.aquacole);
        Aquacole poisson = new Aquacole(301, TypeEspece.aqua, "Tilapia");
        poisson.setAge(2); poisson.setEtatSante(EtatSante.sain);
        app.ajouterAquacole(poisson, zoneAquacole);
        zoneAquacole.ajouterProgAlimentation(new ProgAlimentation("Granules", 2.3));
        ferme.ajouterZoneAquacole(zoneAquacole);

        int choix;
        do {
            System.out.println("\n========== MENU PRINCIPAL ==========");
            System.out.println("1. Afficher toutes les zones");
            System.out.println("2. Ajouter un capteur à une zone");
            System.out.println("3. Envoyer un relevé (simulation) pour un capteur");
            System.out.println("4. Afficher tableau de bord d'une zone (dernier relevé avec couleur)");
            System.out.println("5. Afficher l'historique complet d'un capteur");
            System.out.println("6. Afficher l'évolution des relevés de tous les capteurs d'une zone");
            System.out.println("7. Changer le statut d'un capteur");
            System.out.println("8. Afficher les alertes actives (triées)");
            System.out.println("9. Acquitter une alerte");
            System.out.println("10. Supprimer une alerte");
            System.out.println("11. Filtrer l'historique des alertes");
            System.out.println("0. Quitter");
            System.out.print("Votre choix : ");
            choix = scanner.nextInt();
            scanner.nextLine();

            switch (choix) {
                case 1 -> System.out.println(app.afficherZones());
                case 2 -> ajouterCapteur();
                case 3 -> envoyerReleve();
                case 4 -> afficherTableauBord();
                case 5 -> afficherHistoriqueCapteur();
                case 6 -> afficherEvolutionZone();
                case 7 -> changerStatutCapteur();
                case 8 -> System.out.println(gestionnaire.afficherAlertesActives());
                case 9 -> acquitterAlerte();
                case 10 -> supprimerAlerte();
                case 11 -> filtrerAlertes();
                case 0 -> System.out.println("Au revoir !");
                default -> System.out.println("Choix invalide.");
            }
        } while (choix != 0);
        scanner.close();
    }

    private static void ajouterCapteur() {
        System.out.print("Nom de la zone (Zone Ble, Zone Vaches, Zone Poulets, Bassin Poissons) : ");
        String zoneNom = scanner.nextLine();
        Zone zone = trouverZoneParNom(zoneNom);
        if (zone == null) { System.out.println("Zone non trouvée."); return; }
        System.out.print("Type de capteur (1=Environnemental,2=Sol,3=Eau,4=Biométrique,5=GPS) : ");
        int type = scanner.nextInt(); scanner.nextLine();
        System.out.print("Identifiant du capteur : ");
        String id = scanner.nextLine();
        System.out.print("Seuil min : "); double min = scanner.nextDouble();
        System.out.print("Seuil max : "); double max = scanner.nextDouble(); scanner.nextLine();
        Seuil seuil = new Seuil(min, max);
        Capteur capteur = null;
        try {
            switch (type) {
                case 1 -> { System.out.print("Type mesure (TEMPERATURE, HUMIDITE, PLUVIOMETRIE) : ");
                    TypeMesure tm = TypeMesure.valueOf(scanner.nextLine().toUpperCase());
                    capteur = new CapteurEnvironnemental(id, zone.getNom(), tm, seuil); }
                case 2 -> { System.out.print("Type mesure (PH_SOL, HUMIDITE_SOL, AZOTE) : ");
                    TypeMesure tm = TypeMesure.valueOf(scanner.nextLine().toUpperCase());
                    capteur = new CapteurSol(id, zone.getNom(), tm, seuil); }
                case 3 -> { System.out.print("Type mesure (TEMPERATURE_EAU, OXYGENE_DISSOUS, PH_EAU) : ");
                    TypeMesure tm = TypeMesure.valueOf(scanner.nextLine().toUpperCase());
                    capteur = new CapteurEau(id, zone.getNom(), tm, seuil); }
                case 4 -> { System.out.print("Seuil température (min max) : ");
                    double tMin = scanner.nextDouble(); double tMax = scanner.nextDouble();
                    System.out.print("Seuil activité (min max) : ");
                    double aMin = scanner.nextDouble(); double aMax = scanner.nextDouble(); scanner.nextLine();
                    capteur = new CapteurBiometrique(id, zone.getNom(), new Seuil(tMin, tMax), new Seuil(aMin, aMax)); }
                case 5 -> capteur = new CapteurGPS(id, zone.getNom());
                default -> { System.out.println("Type invalide"); return; }
            }
            gestionnaire.ajouterCapteur(capteur);
            zone.ajouterCapteur(capteur);
            System.out.println("Capteur ajouté.");
        } catch(Exception e) { System.out.println("Erreur : "+e.getMessage()); }
    }

    private static void envoyerReleve() {
        System.out.print("Identifiant du capteur : ");
        String id = scanner.nextLine();
        Capteur c = trouverCapteurParId(id);
        if (c == null) { System.out.println("Capteur inconnu."); return; }
        c.envoyerReleve();
        System.out.println("Relevé envoyé.");
    }

    private static void afficherTableauBord() {
        System.out.print("Nom de la zone : ");
        String zoneNom = scanner.nextLine();
        Zone zone = trouverZoneParNom(zoneNom);
        if (zone == null) { System.out.println("Zone non trouvée."); return; }
        System.out.println(gestionnaire.afficherTableauBordZone(zone.getNom()));
    }

    private static void afficherHistoriqueCapteur() {
        System.out.print("Identifiant du capteur : ");
        String id = scanner.nextLine();
        System.out.println(gestionnaire.afficherEvolutionReleves(id));
    }

    private static void afficherEvolutionZone() {
        System.out.print("Nom de la zone : ");
        String zoneNom = scanner.nextLine();
        Zone zone = trouverZoneParNom(zoneNom);
        if (zone == null) { System.out.println("Zone non trouvée."); return; }
        System.out.println(gestionnaire.afficherEvolutionRelevesZone(zone.getNom()));
    }

    private static void changerStatutCapteur() {
        System.out.print("Identifiant du capteur : ");
        String id = scanner.nextLine();
        Capteur c = trouverCapteurParId(id);
        if (c == null) { System.out.println("Capteur inconnu."); return; }
        System.out.print("Nouveau statut (ACTIVE, INACTIVE, SUSPENDU) : ");
        StatutCapteur statut = StatutCapteur.valueOf(scanner.nextLine().toUpperCase());
        c.changerStatut(statut);
        System.out.println("Statut modifié.");
    }

    private static void acquitterAlerte() {
        System.out.print("ID de l'alerte : ");
        long id = scanner.nextLong();
        if (gestionnaire.acquitterAlerte(id)) System.out.println("Alerte acquittée.");
        else System.out.println("Échec.");
    }

    private static void supprimerAlerte() {
        System.out.print("ID de l'alerte : ");
        long id = scanner.nextLong();
        if (gestionnaire.supprimerAlerte(id)) System.out.println("Alerte supprimée.");
        else System.out.println("Échec.");
    }

    private static void filtrerAlertes() {
        System.out.print("Zone (nom ou vide) : ");
        String zoneNom = scanner.nextLine();
        String zoneId = zoneNom.isEmpty() ? null : zoneNom;
        System.out.print("Type mesure (ou vide) : ");
        String tmStr = scanner.nextLine();
        TypeMesure type = tmStr.isEmpty() ? null : TypeMesure.valueOf(tmStr.toUpperCase());
        System.out.print("Niveau (normal, avertissement, critique ou vide) : ");
        String nivStr = scanner.nextLine();
        Gravite niveau = nivStr.isEmpty() ? null : Gravite.valueOf(nivStr);
        System.out.print("Date début (AAAA-MM-JJTHH:MM:SS ou vide) : ");
        String debutStr = scanner.nextLine();
        LocalDateTime debut = debutStr.isEmpty() ? null : LocalDateTime.parse(debutStr);
        System.out.print("Date fin (AAAA-MM-JJTHH:MM:SS ou vide) : ");
        String finStr = scanner.nextLine();
        LocalDateTime fin = finStr.isEmpty() ? null : LocalDateTime.parse(finStr);
        List<Alerte> resultats = gestionnaire.filtrerAlertes(zoneId, type, niveau, debut, fin);
        System.out.println("=== ALERTES FILTRÉES ===");
        for (Alerte a : resultats)
            System.out.printf("ID:%d | Niveau:%s | Capteur:%s | Date:%s | Valeur:%s%n",
                    a.getId(), a.getNiveau(), a.getReleve().getIdCapteur(), a.getDateCreation(), a.getReleve().getValeurAsString());
    }

    private static Zone trouverZoneParNom(String nom) {
        for (Zone z : ferme.getZones()) if (z.getNom().equalsIgnoreCase(nom)) return z;
        return null;
    }
    private static Capteur trouverCapteurParId(String id) {
        for (Zone z : ferme.getZones()) for (Capteur c : z.getCapteurs()) if (c.getId().equals(id)) return c;
        return null;
    }
}