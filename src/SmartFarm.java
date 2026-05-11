import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.*;

// =============================================================
// ==================== ÉNUMÉRATIONS ==========================
// =============================================================

enum StatutCapteur   { ACTIVE, INACTIVE, SUSPENDU }
enum TypeMesure      {
    TEMPERATURE, HUMIDITE, PLUVIOMETRIE,
    PH_SOL, HUMIDITE_SOL, AZOTE,
    TEMPERATURE_EAU, OXYGENE_DISSOUS, PH_EAU,
    TEMPERATURE_CORPORELLE, ACTIVITE_PAS_PAR_MINUTE
}
enum EtatSante       { malade, sain, quarantaine }
enum Gravite         { normal, avertissement, critique }
enum TypeZone        { aquacole, elevage, culture }
enum StatutZone      { ACTIVE, INACTIVE, SUSPENDU }
enum TypeEspece      { ruminant, volaille, aqua }
enum StadeCroissance { semis, germination, croissance, maturite, recolte }
enum FamilleCulture  { Cereal, Legume, Fruit }
enum TypeZoneElevage { Ruminant, Volaille }
enum TypeEvenSante   { VACCIN, MALADIE, GAIN_POIDS }

// =============================================================
// ==================== INTERFACES ============================
// =============================================================

interface Suspendable {
    void suspendre();
    void reactiver();
    boolean estSuspendu();
}

interface Entite {
    void ajouterAnimal(Animal a);
}

// =============================================================
// ==================== SEUIL =================================
// =============================================================

class Seuil {
    private double min;
    private double max;

    public Seuil(double min, double max) {
        if (min >= max) throw new IllegalArgumentException("min doit être < max");
        this.min = min;
        this.max = max;
    }

    public boolean estHorsLimites(double valeur) {
        return valeur < min || valeur > max;
    }

    public Gravite evaluerGravite(double valeur) {
        double tolerance = (max - min) * 0.1;
        if (valeur < min - tolerance || valeur > max + tolerance) return Gravite.critique;
        if (valeur < min || valeur > max)                         return Gravite.avertissement;
        return Gravite.normal;
    }

    public double getMin() { return min; }
    public double getMax() { return max; }

    @Override
    public String toString() {
        return "[" + min + " , " + max + "]";
    }
}

// =============================================================
// ==================== LIMITES GÉOGRAPHIQUES =================
// =============================================================

class GeographicalLimits {
    private String description;
    private double latMin, latMax, lonMin, lonMax;

    public GeographicalLimits(String description, double latMin, double latMax,
                              double lonMin, double lonMax) {
        this.description = description;
        this.latMin = latMin;
        this.latMax = latMax;
        this.lonMin  = lonMin;
        this.lonMax  = lonMax;
    }

    /** Constructeur simplifié (utilisé quand les coordonnées ne sont pas encore connues) */
    public GeographicalLimits(String description) {
        this(description, 43.0, 44.0, 1.0, 2.0);
    }

    public boolean estHorsLimites(double lat, double lon) {
        return lat < latMin || lat > latMax || lon < lonMin || lon > lonMax;
    }

    public String getDescription() { return description; }
    public double getLatMin()      { return latMin; }
    public double getLatMax()      { return latMax; }
    public double getLonMin()      { return lonMin; }
    public double getLonMax()      { return lonMax; }
}

// =============================================================
// ==================== RELEVÉ ================================
// =============================================================

abstract class Releve {
    private static long compteur = 0;
    private final long id;
    private final String idCapteur;
    private final LocalDateTime timestamp;
    private Gravite niveau;

    public Releve(String idCapteur) {
        this.id        = ++compteur;
        this.idCapteur = idCapteur;
        this.timestamp = LocalDateTime.now();
        this.niveau    = Gravite.normal;
    }

    public long          getId()          { return id; }
    public String        getIdCapteur()   { return idCapteur; }
    public LocalDateTime getTimestamp()   { return timestamp; }
    public Gravite       getNiveau()      { return niveau; }
    public void          setNiveau(Gravite niveau) { this.niveau = niveau; }

    public abstract String getValeurAsString();
}

class ReleveNumerique extends Releve {
    private final double     valeur;
    private final String     unite;
    private final TypeMesure typeMesure;

    public ReleveNumerique(String idCapteur, double valeur, String unite, TypeMesure typeMesure) {
        super(idCapteur);
        this.valeur     = valeur;
        this.unite      = unite;
        this.typeMesure = typeMesure;
    }

    public double     getValeur()     { return valeur; }
    public String     getUnite()      { return unite; }
    public TypeMesure getTypeMesure() { return typeMesure; }

    @Override
    public String getValeurAsString() { return valeur + " " + unite; }
}

class ReleveGPS extends Releve {
    private final double latitude;
    private final double longitude;

    public ReleveGPS(String idCapteur, double latitude, double longitude) {
        super(idCapteur);
        this.latitude  = latitude;
        this.longitude = longitude;
    }

    public double getLatitude()  { return latitude; }
    public double getLongitude() { return longitude; }

    @Override
    public String getValeurAsString() {
        return String.format("lat=%.4f, lon=%.4f", latitude, longitude);
    }
}

// =============================================================
// ==================== ALERTE ================================
// =============================================================

class Alerte {
    private static long compteur = 0;
    private final long          id;
    private final Releve        releve;
    private final Gravite       niveau;
    private final LocalDateTime dateCreation;
    private final String        zoneId;
    private boolean acquittee;
    private boolean supprimee;

    public Alerte(Releve releve, Gravite niveau, String zoneId) {
        this.id           = ++compteur;
        this.releve       = releve;
        this.niveau       = niveau;
        this.dateCreation = LocalDateTime.now();
        this.zoneId       = zoneId;
        this.acquittee    = false;
        this.supprimee    = false;
    }

    public void acquitter() { this.acquittee = true; }
    public void supprimer() { this.supprimee = true; }

    public long          getId()          { return id; }
    public Releve        getReleve()      { return releve; }
    public Gravite       getNiveau()      { return niveau; }
    public LocalDateTime getDateCreation(){ return dateCreation; }
    public String        getZoneId()      { return zoneId; }
    public boolean       isAcquittee()   { return acquittee; }
    public boolean       isSupprimee()   { return supprimee; }

    @Override
    public String toString() {
        return "[ALERTE #" + id + "] Niveau=" + niveau
                + " | Zone=" + zoneId
                + " | Capteur=" + releve.getIdCapteur()
                + " | Valeur=" + releve.getValeurAsString()
                + " | " + dateCreation.toLocalDate()
                + " | Acquittée=" + acquittee;
    }
}

// =============================================================
// ==================== GESTIONNAIRE CAPTEURS & ALERTES =======
// =============================================================

class GestionnaireCapteursAlertes {
    private static GestionnaireCapteursAlertes instance;

    private final List<Capteur>              tousLesCapteurs  = new ArrayList<>();
    private final Map<String, List<Capteur>> capteursParZone  = new HashMap<>();
    private final Map<String, Capteur>       capteursParId    = new HashMap<>();
    private final List<Alerte>               alertes          = new ArrayList<>();

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
        if (niveau != Gravite.normal)
            alertes.add(new Alerte(releve, niveau, zoneId));
    }

    public List<Capteur> getCapteursParZone(String zoneId) {
        return capteursParZone.getOrDefault(zoneId, Collections.emptyList());
    }

    // ---- Affichage tableau de bord d'une zone ----
    public String afficherTableauBordZone(String zoneId) {
        List<Capteur> capteurs = getCapteursParZone(zoneId);
        if (capteurs.isEmpty()) return "Aucun capteur dans la zone " + zoneId;

        StringBuilder sb = new StringBuilder("\n--- TABLEAU DE BORD - ZONE " + zoneId + " ---\n");
        for (Capteur c : capteurs) {
            List<Releve> historique = c.getHistoriqueReleves();
            if (historique.isEmpty()) {
                sb.append("  Capteur ").append(c.getId()).append(" : aucun relevé\n");
                continue;
            }
            Releve dernier = historique.get(historique.size() - 1);
            String couleur, niveauStr;
            switch (dernier.getNiveau()) {
                case normal:        niveauStr = "NORMAL";         couleur = "\u001B[32m"; break;
                case avertissement: niveauStr = "AVERTISSEMENT";  couleur = "\u001B[33m"; break;
                default:            niveauStr = "CRITIQUE";       couleur = "\u001B[31m"; break;
            }
            sb.append("  Capteur ").append(c.getId())
                    .append(" (").append(c.getTypeNom()).append(") : ")
                    .append(couleur).append(niveauStr).append("\u001B[0m")
                    .append(" - Valeur : ").append(dernier.getValeurAsString()).append("\n");
        }
        return sb.toString();
    }

    // ---- Historique d'un capteur ----
    public String afficherEvolutionReleves(String idCapteur) {
        Capteur capteur = capteursParId.get(idCapteur);
        if (capteur == null) return "Capteur inconnu : " + idCapteur;

        List<Releve> historique = capteur.getHistoriqueReleves();
        if (historique.isEmpty()) return "Aucun relevé pour ce capteur.";

        StringBuilder sb = new StringBuilder("\n=== ÉVOLUTION DES RELEVÉS - CAPTEUR " + idCapteur + " ===\n");
        sb.append(String.format("%-30s %-20s %-15s\n", "Date", "Valeur", "Niveau"));
        sb.append("------------------------------------------------------------------\n");
        for (Releve r : historique)
            sb.append(String.format("%-30s %-20s %-15s\n",
                    r.getTimestamp(), r.getValeurAsString(), r.getNiveau()));
        return sb.toString();
    }

    // ---- Historique de tous les capteurs d'une zone ----
    public String afficherEvolutionRelevesZone(String zoneId) {
        List<Capteur> capteurs = getCapteursParZone(zoneId);
        if (capteurs.isEmpty()) return "Aucun capteur dans la zone " + zoneId;

        StringBuilder sb = new StringBuilder("\n=== ÉVOLUTION DES RELEVÉS - ZONE " + zoneId + " ===\n");
        for (Capteur c : capteurs) sb.append(afficherEvolutionReleves(c.getId())).append("\n");
        return sb.toString();
    }

    // ---- Alertes actives triées par gravité ----
    public String afficherAlertesActives() {
        List<Alerte> actives = alertes.stream()
                .filter(a -> !a.isAcquittee() && !a.isSupprimee())
                .sorted((a1, a2) -> a2.getNiveau().compareTo(a1.getNiveau()))
                .collect(Collectors.toList());

        if (actives.isEmpty()) return "Aucune alerte active.";

        StringBuilder sb = new StringBuilder("\n=== ALERTES ACTIVES ===\n");
        for (Alerte a : actives)
            sb.append("ID:").append(a.getId())
                    .append(" | Niveau:").append(a.getNiveau())
                    .append(" | Capteur:").append(a.getReleve().getIdCapteur())
                    .append(" | Date:").append(a.getDateCreation())
                    .append(" | Valeur:").append(a.getReleve().getValeurAsString())
                    .append("\n");
        return sb.toString();
    }

    // ---- Acquitter / Supprimer ----
    public boolean acquitterAlerte(long id) {
        for (Alerte a : alertes)
            if (a.getId() == id && !a.isSupprimee()) { a.acquitter(); return true; }
        return false;
    }

    public boolean supprimerAlerte(long id) {
        for (Alerte a : alertes)
            if (a.getId() == id) { a.supprimer(); return true; }
        return false;
    }

    // ---- Filtrer les alertes ----
    public List<Alerte> filtrerAlertes(String zoneId, TypeMesure typeCapteur,
                                       Gravite niveau,
                                       LocalDateTime debut, LocalDateTime fin) {
        return alertes.stream()
                .filter(a -> zoneId      == null || zoneId.equals(a.getZoneId()))
                .filter(a -> niveau      == null || a.getNiveau() == niveau)
                .filter(a -> debut       == null || !a.getDateCreation().isBefore(debut))
                .filter(a -> fin         == null || !a.getDateCreation().isAfter(fin))
                .filter(a -> {
                    if (typeCapteur == null) return true;
                    Releve r = a.getReleve();
                    if (!(r instanceof ReleveNumerique)) return false;
                    return ((ReleveNumerique) r).getTypeMesure() == typeCapteur;
                })
                .sorted((a1, a2) -> a2.getNiveau().compareTo(a1.getNiveau()))
                .collect(Collectors.toList());
    }

    // ---- Graphique évolution d'un capteur ----
    public void afficherGraphiqueCapteur(Capteur capteur) {
        List<Releve> releves = capteur.getHistoriqueReleves();
        System.out.println("=== Graphique : Capteur " + capteur.getId() + " ===");
        for (Releve r : releves) {
            String indicateur;
            switch (r.getNiveau()) {
                case critique:      indicateur = "[ROUGE  CRITIQUE     ]"; break;
                case avertissement: indicateur = "[ORANGE AVERTISSEMENT]"; break;
                default:            indicateur = "[VERT   NORMAL       ]"; break;
            }
            System.out.println(indicateur + " " + r.getTimestamp().toLocalTime()
                    + " -> " + r.getValeurAsString());
        }
        System.out.println("==========================================");
    }

    public List<Alerte>  getAllAlertes()     { return Collections.unmodifiableList(alertes); }
    public List<Capteur> getTousLesCapteurs(){ return Collections.unmodifiableList(tousLesCapteurs); }
}

// =============================================================
// ==================== CAPTEUR (ABSTRAIT) ====================
// =============================================================

abstract class Capteur implements Suspendable {
    protected final String        id;
    protected String              zoneId;
    protected StatutCapteur       statut;
    protected List<Releve>        historiqueReleves;
    protected GestionnaireCapteursAlertes gestionnaire = GestionnaireCapteursAlertes.getInstance();

    public Capteur(String id, String zoneId) {
        this.id               = id;
        this.zoneId           = zoneId;
        this.statut           = StatutCapteur.ACTIVE;
        this.historiqueReleves = new ArrayList<>();
    }

    public abstract void   envoyerReleve();
    public abstract String getTypeNom();

    public void changerStatut(StatutCapteur s) { this.statut = s; }
    public void suspendre()                    { this.statut = StatutCapteur.SUSPENDU; }
    public void reactiver()                    { this.statut = StatutCapteur.ACTIVE; }
    public void desactiver()                   { this.statut = StatutCapteur.INACTIVE; }
    public boolean estSuspendu()               { return this.statut == StatutCapteur.SUSPENDU; }

    public void ajouterReleve(Releve releve)   { historiqueReleves.add(releve); }

    public List<Releve> getHistoriqueReleves() {
        return Collections.unmodifiableList(historiqueReleves);
    }

    public List<Releve> filtrerRelevesParDate(LocalDateTime debut, LocalDateTime fin) {
        return historiqueReleves.stream()
                .filter(r -> !r.getTimestamp().isBefore(debut) && !r.getTimestamp().isAfter(fin))
                .collect(Collectors.toList());
    }

    public String        getId()    { return id; }
    public String        getZoneId(){ return zoneId; }
    public StatutCapteur getStatut(){ return statut; }
    public void          setZoneId(String zoneId) { this.zoneId = zoneId; }

    @Override
    public String toString() {
        return "Capteur{id='" + id + "', zone='" + zoneId + "', statut=" + statut + '}';
    }
}

// =============================================================
// ==================== CAPTEUR NUMÉRIQUE =====================
// =============================================================

abstract class CapteurNumerique extends Capteur {
    protected TypeMesure typeMesure;
    protected Seuil      seuil;
    protected String     unite;

    public CapteurNumerique(String id, String zoneId, TypeMesure typeMesure,
                            Seuil seuil, String unite) {
        super(id, zoneId);
        this.typeMesure = typeMesure;
        this.seuil      = seuil;
        this.unite      = unite;
    }

    public void configurerSeuil(Seuil nouveauSeuil) { this.seuil = nouveauSeuil; }

    protected ReleveNumerique effectuerMesure(double valeur) {
        ReleveNumerique releve = new ReleveNumerique(this.id, valeur, this.unite, this.typeMesure);
        Gravite gravite = seuil.evaluerGravite(valeur);
        releve.setNiveau(gravite);
        this.ajouterReleve(releve);
        if (gravite != Gravite.normal)
            gestionnaire.declencherAlerte(releve, gravite, this.zoneId);
        return releve;
    }

    public TypeMesure getTypeMesure() { return typeMesure; }
    public Seuil      getSeuil()      { return seuil; }
    public String     getUnite()      { return unite; }
}

// =============================================================
// ==================== CAPTEURS CONCRETS =====================
// =============================================================

class CapteurEnvironnemental extends CapteurNumerique {

    public CapteurEnvironnemental(String id, String zoneId, TypeMesure type, Seuil seuil) {
        super(id, zoneId, type, seuil,
                type == TypeMesure.TEMPERATURE ? "°C" :
                        type == TypeMesure.HUMIDITE    ? "%" : "mm");
        if (type != TypeMesure.TEMPERATURE && type != TypeMesure.HUMIDITE
                && type != TypeMesure.PLUVIOMETRIE)
            throw new IllegalArgumentException("Type non valide pour CapteurEnvironnemental");
    }

    @Override
    public void envoyerReleve() {
        if (statut != StatutCapteur.ACTIVE) return;
        Random rand = new Random();
        double valeur;
        switch (typeMesure) {
            case TEMPERATURE: valeur = 15 + rand.nextDouble() * 20; break;
            case HUMIDITE:    valeur = 40 + rand.nextDouble() * 60; break;
            default:          valeur = rand.nextDouble() * 50;
        }
        effectuerMesure(valeur);
    }

    @Override public String getTypeNom() { return "Environnemental"; }
}

class CapteurSol extends CapteurNumerique {

    public CapteurSol(String id, String zoneId, TypeMesure type, Seuil seuil) {
        super(id, zoneId, type, seuil,
                type == TypeMesure.PH_SOL       ? "pH" :
                        type == TypeMesure.HUMIDITE_SOL ? "%" : "mg/kg");
        if (type != TypeMesure.PH_SOL && type != TypeMesure.HUMIDITE_SOL
                && type != TypeMesure.AZOTE)
            throw new IllegalArgumentException("Type non valide pour CapteurSol");
    }

    @Override
    public void envoyerReleve() {
        if (statut != StatutCapteur.ACTIVE) return;
        Random rand = new Random();
        double valeur;
        switch (typeMesure) {
            case PH_SOL:       valeur = 5.5 + rand.nextDouble() * 4;   break;
            case HUMIDITE_SOL: valeur = 10  + rand.nextDouble() * 70;  break;
            default:           valeur = 20  + rand.nextDouble() * 180;
        }
        effectuerMesure(valeur);
    }

    @Override public String getTypeNom() { return "Sol"; }
}

class CapteurEau extends CapteurNumerique {

    public CapteurEau(String id, String zoneId, TypeMesure type, Seuil seuil) {
        super(id, zoneId, type, seuil,
                type == TypeMesure.TEMPERATURE_EAU  ? "°C" :
                        type == TypeMesure.OXYGENE_DISSOUS   ? "mg/L" : "pH");
        if (type != TypeMesure.TEMPERATURE_EAU && type != TypeMesure.OXYGENE_DISSOUS
                && type != TypeMesure.PH_EAU)
            throw new IllegalArgumentException("Type non valide pour CapteurEau");
    }

    @Override
    public void envoyerReleve() {
        if (statut != StatutCapteur.ACTIVE) return;
        Random rand = new Random();
        double valeur;
        switch (typeMesure) {
            case TEMPERATURE_EAU:  valeur = 10  + rand.nextDouble() * 15; break;
            case OXYGENE_DISSOUS:  valeur = 4   + rand.nextDouble() * 8;  break;
            default:               valeur = 6.5 + rand.nextDouble() * 2;
        }
        effectuerMesure(valeur);
    }

    @Override public String getTypeNom() { return "Eau"; }
}

class CapteurBiometrique extends Capteur {
    private Seuil seuilTemperature;
    private Seuil seuilActivite;

    public CapteurBiometrique(String id, String zoneId,
                              Seuil seuilTemperature, Seuil seuilActivite) {
        super(id, zoneId);
        this.seuilTemperature = seuilTemperature;
        this.seuilActivite    = seuilActivite;
    }

    public void configurerSeuils(Seuil seuilTemperature, Seuil seuilActivite) {
        this.seuilTemperature = seuilTemperature;
        this.seuilActivite    = seuilActivite;
    }

    @Override
    public void envoyerReleve() {
        if (statut != StatutCapteur.ACTIVE) return;
        Random rand = new Random();
        double temperature = 37 + rand.nextDouble() * 3;
        double activite    = 20 + rand.nextDouble() * 100;

        ReleveNumerique releveTemp = new ReleveNumerique(this.id, temperature, "°C",
                TypeMesure.TEMPERATURE_CORPORELLE);
        ReleveNumerique releveAct  = new ReleveNumerique(this.id, activite, "pas/min",
                TypeMesure.ACTIVITE_PAS_PAR_MINUTE);

        Gravite gTemp = seuilTemperature.evaluerGravite(temperature);
        Gravite gAct  = seuilActivite.evaluerGravite(activite);

        releveTemp.setNiveau(gTemp);
        releveAct.setNiveau(gAct);

        this.ajouterReleve(releveTemp);
        this.ajouterReleve(releveAct);

        if (gTemp != Gravite.normal) gestionnaire.declencherAlerte(releveTemp, gTemp, this.zoneId);
        if (gAct  != Gravite.normal) gestionnaire.declencherAlerte(releveAct,  gAct,  this.zoneId);
    }

    @Override public String getTypeNom() { return "Biométrique"; }

    public Seuil getSeuilTemperature() { return seuilTemperature; }
    public Seuil getSeuilActivite()    { return seuilActivite; }
}

class CapteurGPS extends Capteur {
    private double            latitude;
    private double            longitude;
    private List<double[]>    historiquePositions = new ArrayList<>();

    public CapteurGPS(String id, String zoneId) {
        super(id, zoneId);
    }

    @Override
    public void envoyerReleve() {
        if (statut != StatutCapteur.ACTIVE) return;
        Random rand = new Random();
        this.latitude  = 43.5 + (rand.nextDouble() - 0.5) * 0.1;
        this.longitude = 1.5  + (rand.nextDouble() - 0.5) * 0.1;
        historiquePositions.add(new double[]{latitude, longitude});
        ReleveGPS releveGPS = new ReleveGPS(this.id, latitude, longitude);
        this.ajouterReleve(releveGPS);
    }

    public boolean estHorsLimites(GeographicalLimits limites) {
        return limites.estHorsLimites(latitude, longitude);
    }

    public double        getLatitude()          { return latitude; }
    public double        getLongitude()          { return longitude; }
    public List<double[]>getHistoriquePositions(){ return Collections.unmodifiableList(historiquePositions); }

    @Override public String getTypeNom() { return "GPS"; }
}

// =============================================================
// ==================== HISTORIQUE PRODUCTION =================
// =============================================================

class HistoriqueProd {
    private LocalDate date;
    private double    quantite;
    private String    unite;
    private String    description;

    public HistoriqueProd(double quantite, String unite, String description) {
        this.date        = LocalDate.now();
        this.quantite    = quantite;
        this.unite       = unite;
        this.description = description;
    }

    public LocalDate getDate()        { return date; }
    public double    getQuantite()    { return quantite; }
    public String    getUnite()       { return unite; }
    public String    getDescription() { return description; }

    @Override
    public String toString() {
        return "[" + date + "] " + description + " : " + quantite + " " + unite;
    }
}

// =============================================================
// ==================== ENREGISTREMENT PRODUCTION =============
// =============================================================

class EnregistrementProduction {
    private LocalDate date;
    private double    quantite;
    private String    typeProduction;

    public EnregistrementProduction(double quantite, String typeProduction) {
        this.date          = LocalDate.now();
        this.quantite      = quantite;
        this.typeProduction = typeProduction;
    }

    public LocalDate getDate()           { return date; }
    public double    getQuantite()       { return quantite; }
    public String    getTypeProduction() { return typeProduction; }

    @Override
    public String toString() {
        return "[" + date + "] " + typeProduction + " : " + quantite;
    }
}

// =============================================================
// ==================== EVENEMENT SANTÉ =======================
// =============================================================

class EvenementSante {
    private TypeEvenSante type;
    private LocalDate     date;
    private String        description;

    public EvenementSante(TypeEvenSante type, String description) {
        this.type        = type;
        this.date        = LocalDate.now();
        this.description = description;
    }

    public TypeEvenSante getType()        { return type; }
    public LocalDate     getDate()        { return date; }
    public String        getDescription() { return description; }

    @Override
    public String toString() {
        return "[" + date + "] " + type + " : " + description;
    }
}

// =============================================================
// ==================== PROGRAMME ALIMENTATION ================
// =============================================================

class ProgAlimentation {
    private String typeAliment;
    private double quantite;
    private String description;

    public ProgAlimentation(String typeAliment, double quantite) {
        this.typeAliment = typeAliment;
        this.quantite    = quantite;
    }

    public String getTypeAliment()              { return typeAliment; }
    public double getQuantite()                 { return quantite; }
    public String getDescription()              { return description; }
    public void   setDescription(String d)      { this.description = d; }
    public void   setQuantite(double q)         { this.quantite = q; }
    public void   setTypeAliment(String t)      { this.typeAliment = t; }

    @Override
    public String toString() {
        return typeAliment + " : " + quantite + " kg";
    }
}

// =============================================================
// ==================== ANIMAL (ABSTRAIT) =====================
// =============================================================

abstract class Animal {
    private static int compteurId = 0;
    private final int  id;
    private String     nom;
    private int        temperature;
    private int        nivActivite;
    private TypeEspece espece;
    private int        age;
    private int        poid;
    private EtatSante  etatSante;
    private CapteurBiometrique capteurBio;
    private CapteurGPS         capteurGPS;
    private List<EvenementSante> evenementsSante = new ArrayList<>();
    private List<HistoriqueProd> historique      = new ArrayList<>();

    public Animal(TypeEspece espece, String nom) {
        this.id     = ++compteurId;
        this.espece = espece;
        this.nom    = nom;
        this.etatSante = EtatSante.sain;
    }

    public boolean estMalade() { return this.etatSante == EtatSante.malade; }

    public void ajouterEvenementSante(EvenementSante e) { evenementsSante.add(e); }
    public void supprimerEvenementSante(EvenementSante e){ evenementsSante.remove(e); }
    public List<EvenementSante> getEvenementsSante() {
        return Collections.unmodifiableList(evenementsSante);
    }

    public void ajouterHistorique(HistoriqueProd h) { historique.add(h); }
    public List<HistoriqueProd> getHistorique()     { return Collections.unmodifiableList(historique); }

    public int        getId()          { return id; }
    public String     getNom()         { return nom; }
    public int        getTemperature() { return temperature; }
    public int        getNivActivite() { return nivActivite; }
    public TypeEspece getEspece()      { return espece; }
    public int        getAge()         { return age; }
    public int        getPoid()        { return poid; }
    public EtatSante  getEtatSante()   { return etatSante; }
    public CapteurBiometrique getCapteurBio() { return capteurBio; }
    public CapteurGPS         getCapteurGPS() { return capteurGPS; }

    public void setTemperature(int temperature) { this.temperature = temperature; }
    public void setNivActivite(int nivActivite) { this.nivActivite = nivActivite; }
    public void setAge(int age)                 { this.age = age; }
    public void setPoid(int poid)               { this.poid = poid; }
    public void setEtatSante(EtatSante e)       { this.etatSante = e; }
    public void setCapteurBio(CapteurBiometrique c){ this.capteurBio = c; }
    public void setCapteurGPS(CapteurGPS c)      { this.capteurGPS = c; }

    @Override
    public String toString() {
        return "Animal{id=" + id + ", nom='" + nom + "', espece=" + espece
                + ", age=" + age + ", poid=" + poid
                + ", etatSante=" + etatSante
                + ", temperature=" + temperature + "}";
    }
}

// ---- Sous-classes d'Animal ----

class Ruminant extends Animal implements Entite {
    public Ruminant(TypeEspece espece, String nom) { super(espece, nom); }

    @Override
    public void ajouterAnimal(Animal a) {
        System.out.println("Ajout d'un animal associé à " + getNom());
    }
}

class Volaille extends Animal implements Entite {
    public Volaille(TypeEspece espece, String nom) { super(espece, nom); }

    @Override
    public void ajouterAnimal(Animal a) {
        System.out.println("Ajout d'un animal associé à " + getNom());
    }
}

class Aquacole extends Animal {
    public Aquacole(TypeEspece espece, String nom) { super(espece, nom); }
}

// =============================================================
// ==================== CULTURE (ABSTRAITE) ===================
// =============================================================

abstract class Culture {
    private FamilleCulture famille;
    private LocalDate      datePlantation;
    private LocalDate      dateRecolte;
    private StadeCroissance stadeCroissance;
    private Seuil          exigencePH;
    private Seuil          exigenceHumidite;
    private int temperature, humidite, pluviometrie, pH, teneurAzote;
    private List<HistoriqueProd> historique = new ArrayList<>();

    public Culture(FamilleCulture famille, LocalDate datePlantation, LocalDate dateRecolte,
                   Seuil exigencePH, Seuil exigenceHumidite) {
        this.famille          = famille;
        this.datePlantation   = datePlantation;
        this.dateRecolte      = dateRecolte;
        this.exigencePH       = exigencePH;
        this.exigenceHumidite = exigenceHumidite;
        this.stadeCroissance  = StadeCroissance.semis;
    }

    public abstract String getTypeCulture();

    public void ajouterHistorique(HistoriqueProd h) { historique.add(h); }
    public List<HistoriqueProd> getHistorique()     { return Collections.unmodifiableList(historique); }

    public FamilleCulture  getFamille()          { return famille; }
    public LocalDate       getDatePlantation()   { return datePlantation; }
    public LocalDate       getDateRecolte()      { return dateRecolte; }
    public StadeCroissance getStadeCroissance()  { return stadeCroissance; }
    public Seuil           getExigencePH()       { return exigencePH; }
    public Seuil           getExigenceHumidite() { return exigenceHumidite; }
    public int             getTemperature()      { return temperature; }
    public int             getHumidite()         { return humidite; }
    public int             getPluviometrie()     { return pluviometrie; }
    public int             getPH()               { return pH; }
    public int             getTeneurAzote()      { return teneurAzote; }

    public void setDateRecolte(LocalDate d)          { this.dateRecolte = d; }
    public void setStadeCroissance(StadeCroissance s){ this.stadeCroissance = s; }
    public void setTemperature(int t)                { this.temperature = t; }
    public void setHumidite(int h)                   { this.humidite = h; }
    public void setPluviometrie(int p)               { this.pluviometrie = p; }
    public void setPH(int pH)                        { this.pH = pH; }
    public void setTeneurAzote(int az)               { this.teneurAzote = az; }

    public String conditionCroissance() {
        return "PH : " + exigencePH + "\nHumidité : " + exigenceHumidite + "\n";
    }

    public String afficherStats() {
        return "PH : " + pH + "\nHumidité : " + humidite
                + "\nPluviométrie : " + pluviometrie
                + "\nTempérature : " + temperature + "\n";
    }

    @Override
    public String toString() {
        return "Culture{famille=" + famille
                + ", type=" + getTypeCulture()
                + ", datePlantation=" + datePlantation
                + ", dateRecolte=" + dateRecolte
                + ", stade=" + stadeCroissance + '}';
    }
}

class Cereal extends Culture {
    public Cereal(FamilleCulture famille, LocalDate datePlantation, LocalDate dateRecolte,
                  Seuil exigencePH, Seuil exigenceHumidite) {
        super(famille, datePlantation, dateRecolte, exigencePH, exigenceHumidite);
    }
    @Override public String getTypeCulture() { return "Céréale"; }
}

class Legume extends Culture {
    public Legume(FamilleCulture famille, LocalDate datePlantation, LocalDate dateRecolte,
                  Seuil exigencePH, Seuil exigenceHumidite) {
        super(famille, datePlantation, dateRecolte, exigencePH, exigenceHumidite);
    }
    @Override public String getTypeCulture() { return "Légume"; }
}

class Fruit extends Culture {
    public Fruit(FamilleCulture famille, LocalDate datePlantation, LocalDate dateRecolte,
                 Seuil exigencePH, Seuil exigenceHumidite) {
        super(famille, datePlantation, dateRecolte, exigencePH, exigenceHumidite);
    }
    @Override public String getTypeCulture() { return "Fruit"; }
}

// =============================================================
// ==================== ZONE (ABSTRAITE) ======================
// =============================================================

abstract class Zone implements Suspendable {
    protected int       code;
    protected String    nom;
    protected TypeZone  type;
    protected StatutZone statut;
    protected boolean   estSuspendu;
    protected List<Capteur>               capteurs    = new ArrayList<>();
    protected List<EnregistrementProduction> productions = new ArrayList<>();
    protected List<Alerte>                alerts      = new ArrayList<>();
    protected List<Releve>                relevances  = new ArrayList<>();

    public Zone(int code, String nom, TypeZone type) {
        this.code       = code;
        this.nom        = nom;
        this.type       = type;
        this.statut     = StatutZone.ACTIVE;
        this.estSuspendu = false;
    }

    @Override
    public void suspendre() {
        this.estSuspendu = true;
        this.statut = StatutZone.SUSPENDU;
        capteurs.forEach(Capteur::suspendre);
    }

    @Override
    public void reactiver() {
        this.estSuspendu = false;
        this.statut = StatutZone.ACTIVE;
        capteurs.forEach(Capteur::reactiver);
    }

    @Override
    public boolean estSuspendu() { return estSuspendu; }

    public void desactiver() { this.statut = StatutZone.INACTIVE; }

    public void ajouterCapteur(Capteur c)   { capteurs.add(c); }
    public void supprimerCapteur(Capteur c) { capteurs.remove(c); }

    public void ajouterAlerte(Alerte a)  { alerts.add(a); }
    public void supprimerAlerte(Alerte a){ alerts.remove(a); }

    public void ajouterReleve(Releve r)  { relevances.add(r); }
    public void supprimerReleve(Releve r){ relevances.remove(r); }

    public void enregistrerProduction(double quantite, String typeProduction) {
        productions.add(new EnregistrementProduction(quantite, typeProduction));
    }

    public int        getCode()      { return code; }
    public String     getNom()       { return nom; }
    public TypeZone   getType()      { return type; }
    public StatutZone getStatut()    { return statut; }
    public List<Capteur>               getCapteurs()   { return Collections.unmodifiableList(capteurs); }
    public List<EnregistrementProduction> getProductions(){ return Collections.unmodifiableList(productions); }
    public List<Alerte>                getAlerts()    { return Collections.unmodifiableList(alerts); }
    public List<Releve>                getRelevances(){ return Collections.unmodifiableList(relevances); }

    public int  getNbrEntite() { return 0; }

    public void setNom(String nom)         { this.nom = nom; }
    public void setStatut(StatutZone s)    { this.statut = s; }

    @Override
    public String toString() {
        return "Zone{code=" + code + ", nom='" + nom + "', type=" + type + ", statut=" + statut + '}';
    }
}

// ---- ZoneCulture ----
class ZoneCulture extends Zone {
    private List<Culture>              cultures               = new ArrayList<>();
    private List<CapteurSol>          capteurSols             = new ArrayList<>();
    private List<CapteurEnvironnemental> capteurEnvironnementals = new ArrayList<>();

    public ZoneCulture(int code, String nom, TypeZone type) { super(code, nom, type); }

    public void ajouterCulture(Culture c)  { cultures.add(c); }
    public void supprimerCulture(Culture c){ cultures.remove(c); }
    public List<Culture> getCultures()     { return Collections.unmodifiableList(cultures); }

    public void ajouterCapteurSol(CapteurSol c) {
        capteurSols.add(c); capteurs.add(c);
    }
    public void supprimerCapteurSol(CapteurSol c) {
        capteurSols.remove(c); capteurs.remove(c);
    }

    public void ajouterCapteurEnvironnemental(CapteurEnvironnemental c) {
        capteurEnvironnementals.add(c); capteurs.add(c);
    }
    public void supprimerCapteurEnvironnemental(CapteurEnvironnemental c) {
        capteurEnvironnementals.remove(c); capteurs.remove(c);
    }

    @Override public int getNbrEntite() { return cultures.size(); }
}

// ---- ZoneElevage ----
class ZoneElevage extends Zone {
    private List<Animal>         animals          = new ArrayList<>();
    private GeographicalLimits   limitZone;
    private List<ProgAlimentation> programme      = new ArrayList<>();
    private TypeZoneElevage      typeZoneElevage;

    public ZoneElevage(int code, String nom, TypeZone type,
                       TypeZoneElevage typeElevage, GeographicalLimits limitZone) {
        super(code, nom, type);
        this.typeZoneElevage = typeElevage;
        this.limitZone       = limitZone;
    }

    public void ajouterRuminant(Ruminant a) { animals.add(a); }
    public void ajouterVollaile(Volaille a) { animals.add(a); }
    public void supprimerAnimal(Animal a)   { animals.remove(a); }

    public void ajouterProgAlimentation(ProgAlimentation p) { programme.add(p); }

    public List<Animal>          getAnimals()         { return Collections.unmodifiableList(animals); }
    public List<ProgAlimentation>getProgramme()       { return Collections.unmodifiableList(programme); }
    public GeographicalLimits    getLimitZone()        { return limitZone; }
    public TypeZoneElevage       getTypeZoneElevage()  { return typeZoneElevage; }

    @Override public int getNbrEntite() { return animals.size(); }
}

// ---- ZoneAquacole ----
class ZoneAquacole extends Zone {
    private List<Aquacole>       aquacoles  = new ArrayList<>();
    private List<ProgAlimentation> programme = new ArrayList<>();
    private List<CapteurEau>     capteurEau  = new ArrayList<>();

    public ZoneAquacole(int code, String nom, TypeZone type) { super(code, nom, type); }

    public void ajouterAquacole(Aquacole a)  { aquacoles.add(a); }
    public void supprimerAquacole(Aquacole a){ aquacoles.remove(a); }

    public void ajouterProgAlimentation(ProgAlimentation p) { programme.add(p); }

    public void ajouterCapteurEau(CapteurEau c)  { capteurEau.add(c); capteurs.add(c); }
    public void supprimerCapteurEau(CapteurEau c){ capteurEau.remove(c); capteurs.remove(c); }

    public List<Aquacole>        getAquacoles() { return Collections.unmodifiableList(aquacoles); }
    public List<ProgAlimentation>getProgramme() { return Collections.unmodifiableList(programme); }
    public List<CapteurEau>      getCapteurEau(){ return Collections.unmodifiableList(capteurEau); }

    @Override public int getNbrEntite() { return aquacoles.size(); }
}

// =============================================================
// ==================== FERME =================================
// =============================================================

class Ferme {
    private String       nom;
    private List<Zone>   zones   = new ArrayList<>();
    private List<Alerte> alertes = new ArrayList<>();
    private List<Culture>cultures = new ArrayList<>();
    private List<Animal> animals  = new ArrayList<>();

    public Ferme(String nom) { this.nom = nom; }

    public String getNom() { return nom; }

    public List<Zone>   getZones()   { return Collections.unmodifiableList(zones); }
    public List<Alerte> getAlertes() { return Collections.unmodifiableList(alertes); }
    public List<Animal> getAnimals() { return Collections.unmodifiableList(animals); }

    public void ajouterZoneElevage(ZoneElevage zone)   { zones.add(zone); }
    public void ajouterZoneCulture(ZoneCulture zone)   { zones.add(zone); }
    public void ajouterZoneAquacole(ZoneAquacole zone) { zones.add(zone); }
    public void supprimerZone(Zone zone)               { zones.remove(zone); }

    public void ajouterAlerte(Alerte a)  { alertes.add(a); }
    public void ajouterCulture(Culture c){ cultures.add(c); }
    public void ajouterAnimal(Animal a)  { animals.add(a); }

    // Panneau d'alertes (non supprimées, triées par gravité)
    public void afficherPanneauAlertes() {
        System.out.println("========== PANNEAU DES ALERTES ==========");
        alertes.stream()
                .filter(a -> !a.isSupprimee())
                .sorted((a, b) -> b.getNiveau().compareTo(a.getNiveau()))
                .forEach(System.out::println);
        System.out.println("=========================================");
    }

    public List<Alerte> filtrerAlertes(String zoneId, Gravite niveau,
                                       LocalDateTime debut, LocalDateTime fin) {
        return alertes.stream()
                .filter(a -> zoneId == null || a.getZoneId().equals(zoneId))
                .filter(a -> niveau == null || a.getNiveau() == niveau)
                .filter(a -> debut  == null || !a.getDateCreation().isBefore(debut))
                .filter(a -> fin    == null || !a.getDateCreation().isAfter(fin))
                .collect(Collectors.toList());
    }
}

// =============================================================
// ==================== APP ===================================
// =============================================================

class App {
    private String nom;
    private Ferme  ferme;
    private GestionnaireCapteursAlertes gestionnaire = GestionnaireCapteursAlertes.getInstance();

    public App(String nom, Ferme ferme) {
        this.nom   = nom;
        this.ferme = ferme;
    }

    // ---- Gestion zones ----
    public void desactiverZone(Zone zone) { zone.suspendre(); }
    public void reactiverZone(Zone zone)  { zone.reactiver(); }

    // ---- Ajout entités ----
    public void ajouterCulture(Culture culture, ZoneCulture zone) {
        zone.ajouterCulture(culture);
    }

    public void ajouterRuminant(Ruminant ruminant, ZoneElevage zone) {
        if (zone.getTypeZoneElevage() == TypeZoneElevage.Ruminant)
            zone.ajouterRuminant(ruminant);
    }

    public void ajouterVolaille(Volaille volaille, ZoneElevage zone) {
        if (zone.getTypeZoneElevage() == TypeZoneElevage.Volaille)
            zone.ajouterVollaile(volaille);
    }

    public void ajouterAquacole(Aquacole aquacole, ZoneAquacole zone) {
        zone.ajouterAquacole(aquacole);
    }

    // ---- Traitement relevés ----
    public void traiterReleves(Capteur capteur, Zone zone) {
        capteur.envoyerReleve();
        for (Releve r : capteur.getHistoriqueReleves()) {
            if (r.getNiveau() != Gravite.normal) {
                Alerte alerte = new Alerte(r, r.getNiveau(), String.valueOf(zone.getCode()));
                ferme.ajouterAlerte(alerte);
            }
        }
    }

    public void traiterReleveGPS(CapteurGPS capteurGPS, Animal animal, ZoneElevage zone) {
        capteurGPS.envoyerReleve();
        if (capteurGPS.estHorsLimites(zone.getLimitZone())) {
            List<Releve> releves = capteurGPS.getHistoriqueReleves();
            if (!releves.isEmpty()) {
                Releve dernier = releves.get(releves.size() - 1);
                dernier.setNiveau(Gravite.critique);
                Alerte alerte = new Alerte(dernier, Gravite.critique, String.valueOf(zone.getCode()));
                ferme.ajouterAlerte(alerte);
            }
        }
    }

    // ---- Méthodes d'affichage ----
    public String afficherInfosBaseZone(Zone z) {
        return "Code : " + z.getCode() + "\n"
                + "Nom : "    + z.getNom()    + "\n"
                + "Type : "   + z.getType()   + "\n"
                + "Statut : " + z.getStatut() + "\n"
                + "Suspendu : " + z.estSuspendu() + "\n"
                + "Nombre d'entités : " + z.getNbrEntite() + "\n";
    }

    public String afficherCapteursZone(Zone z) {
        StringBuilder r = new StringBuilder("--- Capteurs ---\n");
        for (Capteur c : z.getCapteurs()) {
            r.append("  ").append(c.getId())
                    .append(" (").append(c.getTypeNom()).append(")")
                    .append(" | Statut : ").append(c.getStatut())
                    .append(" | Relevés : ").append(c.getHistoriqueReleves().size())
                    .append("\n");
        }
        return r.toString();
    }

    public String afficherProductionsZone(Zone z) {
        StringBuilder r = new StringBuilder("--- Productions ---\n");
        for (EnregistrementProduction ep : z.getProductions())
            r.append("  ").append(ep).append("\n");
        return r.toString();
    }

    public String afficherZoneCulture(ZoneCulture z) {
        StringBuilder r = new StringBuilder("--- Cultures ---\n");
        for (Culture c : z.getCultures()) {
            r.append("  Type : ").append(c.getTypeCulture()).append("\n");
            r.append("  Famille : ").append(c.getFamille()).append("\n");
            r.append("  Stade : ").append(c.getStadeCroissance()).append("\n");
            r.append("  ").append(c.conditionCroissance());
            r.append("  ").append(c.afficherStats());
        }
        return r.toString();
    }

    public String afficherZoneElevage(ZoneElevage z) {
        StringBuilder r = new StringBuilder("--- Animaux ---\n");
        for (Animal a : z.getAnimals())
            r.append("  ").append(a).append("\n");
        r.append("--- Programme alimentation ---\n");
        for (ProgAlimentation p : z.getProgramme())
            r.append("  ").append(p).append("\n");
        return r.toString();
    }

    public String afficherZoneAquacole(ZoneAquacole z) {
        StringBuilder r = new StringBuilder("--- Aquacoles ---\n");
        for (Aquacole a : z.getAquacoles())
            r.append("  ").append(a).append("\n");
        r.append("--- Programme alimentation ---\n");
        for (ProgAlimentation p : z.getProgramme())
            r.append("  ").append(p).append("\n");
        return r.toString();
    }

    public String afficherZoneComplete(Zone z) {
        StringBuilder r = new StringBuilder();
        r.append(afficherInfosBaseZone(z));
        r.append(afficherCapteursZone(z));
        r.append(afficherProductionsZone(z));
        if      (z instanceof ZoneCulture  c) r.append(afficherZoneCulture(c));
        else if (z instanceof ZoneElevage  e) r.append(afficherZoneElevage(e));
        else if (z instanceof ZoneAquacole a) r.append(afficherZoneAquacole(a));
        r.append("\n====================================\n\n");
        return r.toString();
    }

    public String afficherZones() {
        StringBuilder r = new StringBuilder("===== ZONES DE LA FERME : " + ferme.getNom() + " =====\n\n");
        for (Zone z : ferme.getZones())
            r.append(afficherZoneComplete(z));
        return r.toString();
    }
}

// =============================================================
// ==================== MAIN / MENU ===========================
// =============================================================

public class SmartFarm {

    private static final Scanner scanner     = new Scanner(System.in);
    private static App           app;
    private static Ferme         ferme;
    private static GestionnaireCapteursAlertes gestionnaire = GestionnaireCapteursAlertes.getInstance();

    public static void main(String[] args) {

        // ---- Initialisation de la ferme de démonstration ----
        ferme = new Ferme("Smart Farm");
        app   = new App("Farm Manager", ferme);

        // Zone culture – Blé
        ZoneCulture zoneCulture = new ZoneCulture(1, "Zone Ble", TypeZone.culture);
        Cereal ble = new Cereal(FamilleCulture.Cereal,
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 7, 1),
                new Seuil(6, 8), new Seuil(40, 70));
        ble.setPH(7); ble.setHumidite(55); ble.setTemperature(25); ble.setPluviometrie(120);
        app.ajouterCulture(ble, zoneCulture);
        ferme.ajouterZoneCulture(zoneCulture);

        // Zone élevage – Vaches (ruminants)
        GeographicalLimits limitVaches = new GeographicalLimits("Nord Ferme", 43.45, 43.55, 1.45, 1.55);
        ZoneElevage zoneElevage = new ZoneElevage(2, "Zone Vaches", TypeZone.elevage,
                TypeZoneElevage.Ruminant, limitVaches);
        Ruminant vache1 = new Ruminant(TypeEspece.ruminant, "Vache A");
        vache1.setAge(4); vache1.setPoid(450); vache1.setEtatSante(EtatSante.sain); vache1.setTemperature(38);
        vache1.ajouterEvenementSante(new EvenementSante(TypeEvenSante.VACCIN, "Vaccin FMD 2025"));
        app.ajouterRuminant(vache1, zoneElevage);
        zoneElevage.ajouterProgAlimentation(new ProgAlimentation("Herbe", 5.5));
        ferme.ajouterZoneElevage(zoneElevage);

        // Zone élevage – Poulets (volaille)
        ZoneElevage zonePoulet = new ZoneElevage(3, "Zone Poulets", TypeZone.elevage,
                TypeZoneElevage.Volaille, new GeographicalLimits("Sud Ferme", 43.40, 43.48, 1.40, 1.48));
        Volaille poulet = new Volaille(TypeEspece.volaille, "Poulet 1");
        poulet.setAge(1); poulet.setPoid(2); poulet.setEtatSante(EtatSante.sain);
        app.ajouterVolaille(poulet, zonePoulet);
        zonePoulet.ajouterProgAlimentation(new ProgAlimentation("Graines", 0.5));
        ferme.ajouterZoneElevage(zonePoulet);

        // Zone aquacole – Tilapia
        ZoneAquacole zoneAquacole = new ZoneAquacole(4, "Bassin Poissons", TypeZone.aquacole);
        Aquacole tilapia = new Aquacole(TypeEspece.aqua, "Tilapia");
        tilapia.setAge(2); tilapia.setEtatSante(EtatSante.sain);
        app.ajouterAquacole(tilapia, zoneAquacole);
        zoneAquacole.ajouterProgAlimentation(new ProgAlimentation("Granules", 2.3));
        ferme.ajouterZoneAquacole(zoneAquacole);

        // ---- Capteurs pré-configurés ----
        CapteurEnvironnemental capTemp = new CapteurEnvironnemental(
                "ENV-001", "Zone Ble", TypeMesure.TEMPERATURE, new Seuil(10, 35));
        zoneCulture.ajouterCapteurEnvironnemental(capTemp);
        gestionnaire.ajouterCapteur(capTemp);

        CapteurSol capSol = new CapteurSol("SOL-001", "Zone Ble", TypeMesure.PH_SOL, new Seuil(5.5, 7.5));
        zoneCulture.ajouterCapteurSol(capSol);
        gestionnaire.ajouterCapteur(capSol);

        CapteurBiometrique capBio = new CapteurBiometrique("BIO-001", "Zone Vaches",
                new Seuil(37, 39.5), new Seuil(10, 100));
        zoneElevage.ajouterCapteur(capBio);
        gestionnaire.ajouterCapteur(capBio);

        CapteurGPS capGPS = new CapteurGPS("GPS-001", "Zone Vaches");
        zoneElevage.ajouterCapteur(capGPS);
        gestionnaire.ajouterCapteur(capGPS);

        CapteurEau capEau = new CapteurEau("EAU-001", "Bassin Poissons",
                TypeMesure.OXYGENE_DISSOUS, new Seuil(5, 10));
        zoneAquacole.ajouterCapteurEau(capEau);
        gestionnaire.ajouterCapteur(capEau);

        // ---- Menu principal ----
        int choix;
        do {
            System.out.println("\n╔══════════════════════════════════════╗");
            System.out.println("║       MENU PRINCIPAL - SMART FARM    ║");
            System.out.println("╠══════════════════════════════════════╣");
            System.out.println("║  1.  Afficher toutes les zones        ║");
            System.out.println("║  2.  Ajouter un capteur à une zone    ║");
            System.out.println("║  3.  Envoyer un relevé (simulation)   ║");
            System.out.println("║  4.  Tableau de bord d'une zone       ║");
            System.out.println("║  5.  Historique d'un capteur          ║");
            System.out.println("║  6.  Évolution relevés d'une zone     ║");
            System.out.println("║  7.  Graphique d'un capteur           ║");
            System.out.println("║  8.  Changer statut d'un capteur      ║");
            System.out.println("║  9.  Afficher alertes actives         ║");
            System.out.println("║  10. Acquitter une alerte             ║");
            System.out.println("║  11. Supprimer une alerte             ║");
            System.out.println("║  12. Filtrer l'historique des alertes ║");
            System.out.println("║  13. Suspendre / Réactiver une zone   ║");
            System.out.println("║  14. Ajouter animal / culture         ║");
            System.out.println("║  0.  Quitter                          ║");
            System.out.println("╚══════════════════════════════════════╝");
            System.out.print("Votre choix : ");
            choix = lireInt();

            switch (choix) {
                case 1  -> System.out.println(app.afficherZones());
                case 2  -> menuAjouterCapteur();
                case 3  -> menuEnvoyerReleve();
                case 4  -> menuTableauBord();
                case 5  -> menuHistoriqueCapteur();
                case 6  -> menuEvolutionZone();
                case 7  -> menuGraphiqueCapteur();
                case 8  -> menuChangerStatutCapteur();
                case 9  -> System.out.println(gestionnaire.afficherAlertesActives());
                case 10 -> menuAcquitterAlerte();
                case 11 -> menuSupprimerAlerte();
                case 12 -> menuFiltrerAlertes();
                case 13 -> menuSuspendreZone();
                case 14 -> menuAjouterEntite();
                case 0  -> System.out.println("Au revoir !");
                default -> System.out.println("Choix invalide.");
            }
        } while (choix != 0);
        scanner.close();
    }

    // ==========================================================
    // ==================== SOUS-MENUS ==========================
    // ==========================================================

    private static void menuAjouterCapteur() {
        System.out.print("Nom de la zone : ");
        String nomZone = scanner.nextLine().trim();
        Zone zone = trouverZoneParNom(nomZone);
        if (zone == null) { System.out.println("Zone non trouvée."); return; }

        System.out.println("Type de capteur :");
        System.out.println("  1 = Environnemental  2 = Sol  3 = Eau  4 = Biométrique  5 = GPS");
        int type = lireInt();
        System.out.print("Identifiant du capteur : ");
        String id = scanner.nextLine().trim();

        Capteur capteur = null;
        try {
            switch (type) {
                case 1 -> {
                    System.out.print("Type mesure (TEMPERATURE / HUMIDITE / PLUVIOMETRIE) : ");
                    TypeMesure tm = TypeMesure.valueOf(scanner.nextLine().trim().toUpperCase());
                    double[] s = lireSeuil(); capteur = new CapteurEnvironnemental(id, zone.getNom(), tm, new Seuil(s[0], s[1]));
                }
                case 2 -> {
                    System.out.print("Type mesure (PH_SOL / HUMIDITE_SOL / AZOTE) : ");
                    TypeMesure tm = TypeMesure.valueOf(scanner.nextLine().trim().toUpperCase());
                    double[] s = lireSeuil(); capteur = new CapteurSol(id, zone.getNom(), tm, new Seuil(s[0], s[1]));
                }
                case 3 -> {
                    System.out.print("Type mesure (TEMPERATURE_EAU / OXYGENE_DISSOUS / PH_EAU) : ");
                    TypeMesure tm = TypeMesure.valueOf(scanner.nextLine().trim().toUpperCase());
                    double[] s = lireSeuil(); capteur = new CapteurEau(id, zone.getNom(), tm, new Seuil(s[0], s[1]));
                }
                case 4 -> {
                    System.out.print("Seuil température - min : "); double tMin = lireDouble();
                    System.out.print("Seuil température - max : "); double tMax = lireDouble();
                    System.out.print("Seuil activité    - min : "); double aMin = lireDouble();
                    System.out.print("Seuil activité    - max : "); double aMax = lireDouble();
                    capteur = new CapteurBiometrique(id, zone.getNom(), new Seuil(tMin, tMax), new Seuil(aMin, aMax));
                }
                case 5 -> capteur = new CapteurGPS(id, zone.getNom());
                default -> { System.out.println("Type invalide."); return; }
            }
            zone.ajouterCapteur(capteur);
            gestionnaire.ajouterCapteur(capteur);

            // Ajouter aux listes spécialisées si applicable
            if (zone instanceof ZoneCulture zc) {
                if (capteur instanceof CapteurSol cs) zc.ajouterCapteurSol(cs);
                else if (capteur instanceof CapteurEnvironnemental ce) zc.ajouterCapteurEnvironnemental(ce);
            } else if (zone instanceof ZoneAquacole za && capteur instanceof CapteurEau ce) {
                za.ajouterCapteurEau(ce);
            }
            System.out.println("✔ Capteur " + id + " ajouté à la zone « " + zone.getNom() + " ».");

        } catch (Exception e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    private static void menuEnvoyerReleve() {
        System.out.print("Identifiant du capteur : ");
        String id = scanner.nextLine().trim();
        Capteur c = trouverCapteurParId(id);
        if (c == null) { System.out.println("Capteur inconnu."); return; }
        c.envoyerReleve();
        List<Releve> releves = c.getHistoriqueReleves();
        if (!releves.isEmpty()) {
            Releve dernier = releves.get(releves.size() - 1);
            System.out.println("✔ Relevé envoyé : " + dernier.getValeurAsString()
                    + " [" + dernier.getNiveau() + "]");
        }
    }

    private static void menuTableauBord() {
        System.out.print("Nom de la zone : ");
        String nom = scanner.nextLine().trim();
        Zone zone = trouverZoneParNom(nom);
        if (zone == null) { System.out.println("Zone non trouvée."); return; }
        System.out.println(gestionnaire.afficherTableauBordZone(zone.getNom()));
    }

    private static void menuHistoriqueCapteur() {
        System.out.print("Identifiant du capteur : ");
        System.out.println(gestionnaire.afficherEvolutionReleves(scanner.nextLine().trim()));
    }

    private static void menuEvolutionZone() {
        System.out.print("Nom de la zone : ");
        String nom = scanner.nextLine().trim();
        Zone zone = trouverZoneParNom(nom);
        if (zone == null) { System.out.println("Zone non trouvée."); return; }
        System.out.println(gestionnaire.afficherEvolutionRelevesZone(zone.getNom()));
    }

    private static void menuGraphiqueCapteur() {
        System.out.print("Identifiant du capteur : ");
        String id = scanner.nextLine().trim();
        Capteur c = trouverCapteurParId(id);
        if (c == null) { System.out.println("Capteur inconnu."); return; }
        gestionnaire.afficherGraphiqueCapteur(c);
    }

    private static void menuChangerStatutCapteur() {
        System.out.print("Identifiant du capteur : ");
        String id = scanner.nextLine().trim();
        Capteur c = trouverCapteurParId(id);
        if (c == null) { System.out.println("Capteur inconnu."); return; }
        System.out.print("Nouveau statut (ACTIVE / INACTIVE / SUSPENDU) : ");
        try {
            StatutCapteur s = StatutCapteur.valueOf(scanner.nextLine().trim().toUpperCase());
            c.changerStatut(s);
            System.out.println("✔ Statut modifié.");
        } catch (Exception e) { System.out.println("Statut invalide."); }
    }

    private static void menuAcquitterAlerte() {
        System.out.print("ID de l'alerte : ");
        long id = lireLong();
        System.out.println(gestionnaire.acquitterAlerte(id) ? "✔ Alerte acquittée." : "Alerte introuvable.");
    }

    private static void menuSupprimerAlerte() {
        System.out.print("ID de l'alerte : ");
        long id = lireLong();
        System.out.println(gestionnaire.supprimerAlerte(id) ? "✔ Alerte supprimée." : "Alerte introuvable.");
    }

    private static void menuFiltrerAlertes() {
        System.out.print("Zone (nom ou vide) : ");
        String zNom = scanner.nextLine().trim();
        String zoneId = zNom.isEmpty() ? null : zNom;

        System.out.print("Type mesure (ex: TEMPERATURE ou vide) : ");
        String tmStr = scanner.nextLine().trim();
        TypeMesure tm = tmStr.isEmpty() ? null : TypeMesure.valueOf(tmStr.toUpperCase());

        System.out.print("Niveau (normal / avertissement / critique ou vide) : ");
        String nivStr = scanner.nextLine().trim();
        Gravite niveau = nivStr.isEmpty() ? null : Gravite.valueOf(nivStr);

        System.out.print("Date début (AAAA-MM-JJTHH:MM:SS ou vide) : ");
        String debutStr = scanner.nextLine().trim();
        LocalDateTime debut = debutStr.isEmpty() ? null : LocalDateTime.parse(debutStr);

        System.out.print("Date fin   (AAAA-MM-JJTHH:MM:SS ou vide) : ");
        String finStr = scanner.nextLine().trim();
        LocalDateTime fin = finStr.isEmpty() ? null : LocalDateTime.parse(finStr);

        List<Alerte> resultats = gestionnaire.filtrerAlertes(zoneId, tm, niveau, debut, fin);
        System.out.println("=== ALERTES FILTRÉES (" + resultats.size() + ") ===");
        for (Alerte a : resultats)
            System.out.printf("ID:%-5d | %-15s | Capteur:%-10s | %s | Valeur:%s%n",
                    a.getId(), a.getNiveau(), a.getReleve().getIdCapteur(),
                    a.getDateCreation(), a.getReleve().getValeurAsString());
    }

    private static void menuSuspendreZone() {
        System.out.print("Nom de la zone : ");
        String nom = scanner.nextLine().trim();
        Zone zone = trouverZoneParNom(nom);
        if (zone == null) { System.out.println("Zone non trouvée."); return; }
        System.out.print("Action (1=Suspendre / 2=Réactiver) : ");
        int action = lireInt();
        if (action == 1) { app.desactiverZone(zone); System.out.println("✔ Zone suspendue."); }
        else if (action == 2) { app.reactiverZone(zone); System.out.println("✔ Zone réactivée."); }
        else System.out.println("Action invalide.");
    }

    private static void menuAjouterEntite() {
        System.out.println("Que voulez-vous ajouter ?");
        System.out.println("  1 = Animal (ruminant / volaille / aquacole)");
        System.out.println("  2 = Culture");
        int choix = lireInt();
        if (choix == 1) menuAjouterAnimal();
        else if (choix == 2) menuAjouterCulture();
        else System.out.println("Choix invalide.");
    }

    private static void menuAjouterAnimal() {
        System.out.print("Nom de l'animal : "); String nom = scanner.nextLine().trim();
        System.out.print("Espèce (ruminant / volaille / aqua) : "); String especeStr = scanner.nextLine().trim().toLowerCase();
        System.out.print("Âge : "); int age = lireInt();
        System.out.print("Poids : "); int poid = lireInt();
        System.out.print("Nom de la zone cible : "); String nomZone = scanner.nextLine().trim();
        Zone zone = trouverZoneParNom(nomZone);
        if (zone == null) { System.out.println("Zone non trouvée."); return; }
        try {
            TypeEspece espece = TypeEspece.valueOf(especeStr);
            if (espece == TypeEspece.ruminant && zone instanceof ZoneElevage ze) {
                Ruminant r = new Ruminant(TypeEspece.ruminant, nom);
                r.setAge(age); r.setPoid(poid);
                app.ajouterRuminant(r, ze);
                System.out.println("✔ Ruminant ajouté.");
            } else if (espece == TypeEspece.volaille && zone instanceof ZoneElevage ze) {
                Volaille v = new Volaille(TypeEspece.volaille, nom);
                v.setAge(age); v.setPoid(poid);
                app.ajouterVolaille(v, ze);
                System.out.println("✔ Volaille ajoutée.");
            } else if (espece == TypeEspece.aqua && zone instanceof ZoneAquacole za) {
                Aquacole a = new Aquacole(TypeEspece.aqua, nom);
                a.setAge(age); a.setPoid(poid);
                app.ajouterAquacole(a, za);
                System.out.println("✔ Aquacole ajouté.");
            } else {
                System.out.println("Incompatibilité espèce/zone ou type de zone incorrect.");
            }
        } catch (Exception e) { System.out.println("Erreur : " + e.getMessage()); }
    }

    private static void menuAjouterCulture() {
        System.out.print("Famille (Cereal / Legume / Fruit) : "); String famStr = scanner.nextLine().trim();
        System.out.print("Date plantation (AAAA-MM-JJ) : "); LocalDate dp = LocalDate.parse(scanner.nextLine().trim());
        System.out.print("Date récolte    (AAAA-MM-JJ) : "); LocalDate dr = LocalDate.parse(scanner.nextLine().trim());
        System.out.print("pH min : "); double phMin = lireDouble();
        System.out.print("pH max : "); double phMax = lireDouble();
        System.out.print("Humidité min : "); double hMin = lireDouble();
        System.out.print("Humidité max : "); double hMax = lireDouble();
        System.out.print("Nom de la ZoneCulture cible : "); String nomZone = scanner.nextLine().trim();
        Zone zone = trouverZoneParNom(nomZone);
        if (!(zone instanceof ZoneCulture zc)) { System.out.println("Zone culture non trouvée."); return; }
        try {
            FamilleCulture fam = FamilleCulture.valueOf(famStr);
            Seuil seuilPH  = new Seuil(phMin, phMax);
            Seuil seuilHum = new Seuil(hMin, hMax);
            Culture culture;
            switch (fam) {
                case Cereal -> culture = new Cereal(fam, dp, dr, seuilPH, seuilHum);
                case Legume -> culture = new Legume(fam, dp, dr, seuilPH, seuilHum);
                default     -> culture = new Fruit(fam, dp, dr, seuilPH, seuilHum);
            }
            app.ajouterCulture(culture, zc);
            System.out.println("✔ Culture ajoutée.");
        } catch (Exception e) { System.out.println("Erreur : " + e.getMessage()); }
    }

    // ==========================================================
    // ==================== UTILITAIRES =========================
    // ==========================================================

    private static Zone trouverZoneParNom(String nom) {
        for (Zone z : ferme.getZones())
            if (z.getNom().equalsIgnoreCase(nom)) return z;
        return null;
    }

    private static Capteur trouverCapteurParId(String id) {
        for (Zone z : ferme.getZones())
            for (Capteur c : z.getCapteurs())
                if (c.getId().equals(id)) return c;
        return null;
    }

    private static double[] lireSeuil() {
        System.out.print("Seuil min : "); double min = lireDouble();
        System.out.print("Seuil max : "); double max = lireDouble();
        return new double[]{min, max};
    }

    private static int lireInt() {
        try {
            int v = Integer.parseInt(scanner.nextLine().trim());
            return v;
        } catch (Exception e) { return -1; }
    }

    private static long lireLong() {
        try { return Long.parseLong(scanner.nextLine().trim()); }
        catch (Exception e) { return -1; }
    }

    private static double lireDouble() {
        try { return Double.parseDouble(scanner.nextLine().trim()); }
        catch (Exception e) { return 0; }
    }
}
























