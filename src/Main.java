import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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

    public boolean estHorsLimites(double valeur) { return valeur < min || valeur > max; }

    public Gravite evaluerGravite(double valeur) {
        double tolerance = (max - min) * 0.1;
        if (valeur < min - tolerance || valeur > max + tolerance) return Gravite.critique;
        if (valeur < min || valeur > max) return Gravite.avertissement;
        return Gravite.normal;
    }

    public double getMin() { return min; }
    public double getMax() { return max; }

    @Override
    public String toString() { return "[" + min + " , " + max + "]"; }
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
        this.latMin = latMin; this.latMax = latMax;
        this.lonMin = lonMin; this.lonMax = lonMax;
    }

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

    public long          getId()        { return id; }
    public String        getIdCapteur() { return idCapteur; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public Gravite       getNiveau()    { return niveau; }
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

    public long          getId()           { return id; }
    public Releve        getReleve()       { return releve; }
    public Gravite       getNiveau()       { return niveau; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public String        getZoneId()       { return zoneId; }
    public boolean       isAcquittee()    { return acquittee; }
    public boolean       isSupprimee()    { return supprimee; }

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

    private final List<Capteur>              tousLesCapteurs = new ArrayList<>();
    private final Map<String, List<Capteur>> capteursParZone = new HashMap<>();
    private final Map<String, Capteur>       capteursParId   = new HashMap<>();
    private final List<Alerte>               alertes         = new ArrayList<>();

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

    public Capteur getCapteurById(String id) { return capteursParId.get(id); }
    public List<Capteur> getTousCapteurs()   { return Collections.unmodifiableList(tousLesCapteurs); }

    public void suspendreZone(String zoneId) {
        for (Capteur c : capteursParZone.getOrDefault(zoneId, Collections.emptyList())) c.suspendre();
    }
    public void reactiverZone(String zoneId) {
        for (Capteur c : capteursParZone.getOrDefault(zoneId, Collections.emptyList())) c.reactiver();
    }

    public void declencherAlerte(Releve releve, Gravite niveau, String zoneId) {
        if (niveau != Gravite.normal)
            alertes.add(new Alerte(releve, niveau, zoneId));
    }

    public List<Capteur> getCapteursParZone(String zoneId) {
        return capteursParZone.getOrDefault(zoneId, Collections.emptyList());
    }

    // ---- Tableau de bord zone ----
    public String afficherTableauBordZone(String zoneId) {
        List<Capteur> capteurs = getCapteursParZone(zoneId);
        if (capteurs.isEmpty()) return "Aucun capteur dans la zone " + zoneId;
        StringBuilder sb = new StringBuilder("\n--- TABLEAU DE BORD - ZONE " + zoneId + " ---\n");
        for (Capteur c : capteurs) {
            List<Releve> hist = c.getHistoriqueReleves();
            if (hist.isEmpty()) { sb.append("  Capteur ").append(c.getId()).append(" : aucun relevé\n"); continue; }
            Releve dernier = hist.get(hist.size() - 1);
            String niveauStr, couleur;
            switch (dernier.getNiveau()) {
                case normal:        niveauStr = "NORMAL";        couleur = "\u001B[32m"; break;
                case avertissement: niveauStr = "AVERTISSEMENT"; couleur = "\u001B[33m"; break;
                default:            niveauStr = "CRITIQUE";      couleur = "\u001B[31m"; break;
            }
            sb.append("  Capteur ").append(c.getId())
                    .append(" (").append(c.getTypeNom()).append(") : ")
                    .append(couleur).append(niveauStr).append("\u001B[0m")
                    .append(" - Valeur : ").append(dernier.getValeurAsString()).append("\n");
        }
        return sb.toString();
    }

    // ---- Historique capteur ----
    public String afficherEvolutionReleves(String idCapteur) {
        Capteur capteur = capteursParId.get(idCapteur);
        if (capteur == null) return "Capteur inconnu : " + idCapteur;
        List<Releve> hist = capteur.getHistoriqueReleves();
        if (hist.isEmpty()) return "Aucun relevé pour ce capteur.";
        StringBuilder sb = new StringBuilder("\n=== ÉVOLUTION – CAPTEUR " + idCapteur + " ===\n");
        sb.append(String.format("%-25s %-18s %-12s%n", "Date", "Valeur", "Niveau"));
        sb.append("─".repeat(56)).append("\n");
        for (Releve r : hist)
            sb.append(String.format("%-25s %-18s %-12s%n",
                    r.getTimestamp(), r.getValeurAsString(), r.getNiveau()));
        return sb.toString();
    }

    // ---- Historique zone ----
    public String afficherEvolutionRelevesZone(String zoneId) {
        List<Capteur> capteurs = getCapteursParZone(zoneId);
        if (capteurs.isEmpty()) return "Aucun capteur dans la zone " + zoneId;
        StringBuilder sb = new StringBuilder("\n=== ÉVOLUTION – ZONE " + zoneId + " ===\n");
        for (Capteur c : capteurs) sb.append(afficherEvolutionReleves(c.getId())).append("\n");
        return sb.toString();
    }

    // ---- Alertes actives ----
    public String afficherAlertesActives() {
        List<Alerte> actives = alertes.stream()
                .filter(a -> !a.isAcquittee() && !a.isSupprimee())
                .sorted((a1, a2) -> a2.getNiveau().compareTo(a1.getNiveau()))
                .collect(Collectors.toList());
        if (actives.isEmpty()) return "Aucune alerte active.";
        StringBuilder sb = new StringBuilder("\n=== ALERTES ACTIVES ===\n");
        for (Alerte a : actives)
            sb.append("ID: ").append(a.getId())
                    .append(" | Niveau: ").append(a.getNiveau())
                    .append(" | Capteur: ").append(a.getReleve().getIdCapteur())
                    .append(" | Date: ").append(a.getDateCreation())
                    .append(" | Valeur: ").append(a.getReleve().getValeurAsString()).append("\n");
        return sb.toString();
    }

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

    // ---- Graphique couleur ----
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

    public List<Alerte>  getAllAlertes()      { return Collections.unmodifiableList(alertes); }
    public List<Capteur> getTousLesCapteurs() { return Collections.unmodifiableList(tousLesCapteurs); }
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
        this.id                = id;
        this.zoneId            = zoneId;
        this.statut            = StatutCapteur.ACTIVE;
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
        List<Releve> res = new ArrayList<>();
        for (Releve r : historiqueReleves)
            if (!r.getTimestamp().isBefore(debut) && !r.getTimestamp().isAfter(fin)) res.add(r);
        return res;
    }

    public String        getId()     { return id; }
    public String        getZoneId() { return zoneId; }
    public StatutCapteur getStatut() { return statut; }
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

    public CapteurNumerique(String id, String zoneId, TypeMesure typeMesure, Seuil seuil, String unite) {
        super(id, zoneId);
        this.typeMesure = typeMesure;
        this.seuil      = seuil;
        this.unite      = unite;
    }

    public void configurerSeuil(Seuil s) { this.seuil = s; }

    protected ReleveNumerique effectuerMesure(double valeur) {
        ReleveNumerique r = new ReleveNumerique(this.id, valeur, this.unite, this.typeMesure);
        Gravite g = seuil.evaluerGravite(valeur);
        r.setNiveau(g);
        this.ajouterReleve(r);
        if (g != Gravite.normal) gestionnaire.declencherAlerte(r, g, this.zoneId);
        return r;
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
                        type == TypeMesure.HUMIDITE ? "%" : "mm");
        if (type != TypeMesure.TEMPERATURE && type != TypeMesure.HUMIDITE && type != TypeMesure.PLUVIOMETRIE)
            throw new IllegalArgumentException("Type non valide pour CapteurEnvironnemental");
    }

    @Override
    public void envoyerReleve() {
        if (statut != StatutCapteur.ACTIVE) return;
        Random rand = new Random(); double v;
        switch (typeMesure) {
            case TEMPERATURE: v = 15 + rand.nextDouble() * 20; break;
            case HUMIDITE:    v = 40 + rand.nextDouble() * 60; break;
            default:          v = rand.nextDouble() * 50;
        }
        effectuerMesure(v);
    }

    @Override public String getTypeNom() { return "Environnemental"; }
}

class CapteurSol extends CapteurNumerique {
    public CapteurSol(String id, String zoneId, TypeMesure type, Seuil seuil) {
        super(id, zoneId, type, seuil,
                type == TypeMesure.PH_SOL ? "pH" :
                        type == TypeMesure.HUMIDITE_SOL ? "%" : "mg/kg");
        if (type != TypeMesure.PH_SOL && type != TypeMesure.HUMIDITE_SOL && type != TypeMesure.AZOTE)
            throw new IllegalArgumentException("Type non valide pour CapteurSol");
    }

    @Override
    public void envoyerReleve() {
        if (statut != StatutCapteur.ACTIVE) return;
        Random rand = new Random(); double v;
        switch (typeMesure) {
            case PH_SOL:       v = 5.5 + rand.nextDouble() * 4;   break;
            case HUMIDITE_SOL: v = 10  + rand.nextDouble() * 70;  break;
            default:           v = 20  + rand.nextDouble() * 180;
        }
        effectuerMesure(v);
    }

    @Override public String getTypeNom() { return "Sol"; }
}

class CapteurEau extends CapteurNumerique {
    public CapteurEau(String id, String zoneId, TypeMesure type, Seuil seuil) {
        super(id, zoneId, type, seuil,
                type == TypeMesure.TEMPERATURE_EAU ? "°C" :
                        type == TypeMesure.OXYGENE_DISSOUS ? "mg/L" : "pH");
        if (type != TypeMesure.TEMPERATURE_EAU && type != TypeMesure.OXYGENE_DISSOUS && type != TypeMesure.PH_EAU)
            throw new IllegalArgumentException("Type non valide pour CapteurEau");
    }

    @Override
    public void envoyerReleve() {
        if (statut != StatutCapteur.ACTIVE) return;
        Random rand = new Random(); double v;
        switch (typeMesure) {
            case TEMPERATURE_EAU: v = 10  + rand.nextDouble() * 15; break;
            case OXYGENE_DISSOUS: v = 4   + rand.nextDouble() * 8;  break;
            default:              v = 6.5 + rand.nextDouble() * 2;
        }
        effectuerMesure(v);
    }

    @Override public String getTypeNom() { return "Eau"; }
}

class CapteurBiometrique extends Capteur {
    private Seuil seuilTemperature, seuilActivite;

    public CapteurBiometrique(String id, String zoneId, Seuil seuilTemp, Seuil seuilAct) {
        super(id, zoneId);
        this.seuilTemperature = seuilTemp;
        this.seuilActivite    = seuilAct;
    }

    public void configurerSeuils(Seuil t, Seuil a) { seuilTemperature = t; seuilActivite = a; }

    @Override
    public void envoyerReleve() {
        if (statut != StatutCapteur.ACTIVE) return;
        Random rand = new Random();
        double temp = 37 + rand.nextDouble() * 3;
        double act  = 20 + rand.nextDouble() * 100;
        ReleveNumerique rT = new ReleveNumerique(id, temp, "°C",      TypeMesure.TEMPERATURE_CORPORELLE);
        ReleveNumerique rA = new ReleveNumerique(id, act,  "pas/min", TypeMesure.ACTIVITE_PAS_PAR_MINUTE);
        Gravite gT = seuilTemperature.evaluerGravite(temp);
        Gravite gA = seuilActivite.evaluerGravite(act);
        rT.setNiveau(gT); rA.setNiveau(gA);
        ajouterReleve(rT); ajouterReleve(rA);
        if (gT != Gravite.normal) gestionnaire.declencherAlerte(rT, gT, this.zoneId);
        if (gA != Gravite.normal) gestionnaire.declencherAlerte(rA, gA, this.zoneId);
    }

    @Override public String getTypeNom()       { return "Biométrique"; }
    public Seuil getSeuilTemperature()         { return seuilTemperature; }
    public Seuil getSeuilActivite()            { return seuilActivite; }
}

class CapteurGPS extends Capteur {
    private double         latitude, longitude;
    private List<double[]> historiquePositions = new ArrayList<>();

    public CapteurGPS(String id, String zoneId) { super(id, zoneId); }

    @Override
    public void envoyerReleve() {
        if (statut != StatutCapteur.ACTIVE) return;
        Random rand = new Random();
        this.latitude  = 43.5 + (rand.nextDouble() - 0.5) * 0.1;
        this.longitude = 1.5  + (rand.nextDouble() - 0.5) * 0.1;
        historiquePositions.add(new double[]{latitude, longitude});
        ajouterReleve(new ReleveGPS(id, latitude, longitude));
    }

    public boolean estHorsLimites(GeographicalLimits limites) {
        return limites.estHorsLimites(latitude, longitude);
    }

    public double         getLatitude()           { return latitude; }
    public double         getLongitude()           { return longitude; }
    public List<double[]> getHistoriquePositions() { return Collections.unmodifiableList(historiquePositions); }

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
    public String toString() { return "[" + date + "] " + typeProduction + " : " + quantite; }
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
    public String toString() { return "[" + date + "] " + type + " : " + description; }
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

    public String getTypeAliment()         { return typeAliment; }
    public double getQuantite()            { return quantite; }
    public String getDescription()         { return description; }
    public void   setDescription(String d) { this.description = d; }
    public void   setQuantite(double q)    { this.quantite = q; }
    public void   setTypeAliment(String t) { this.typeAliment = t; }

    @Override
    public String toString() { return typeAliment + " : " + quantite + " kg"; }
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
    public void supprimerEvenementSante(EvenementSante e) { evenementsSante.remove(e); }
    public List<EvenementSante> getEvenementsSante() { return Collections.unmodifiableList(evenementsSante); }

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

    public void setTemperature(int t)          { this.temperature = t; }
    public void setNivActivite(int n)          { this.nivActivite = n; }
    public void setAge(int age)                { this.age = age; }
    public void setPoid(int poid)              { this.poid = poid; }
    public void setEtatSante(EtatSante e)      { this.etatSante = e; }
    public void setCapteurBio(CapteurBiometrique c) { this.capteurBio = c; }
    public void setCapteurGPS(CapteurGPS c)    { this.capteurGPS = c; }

    @Override
    public String toString() {
        return "Animal{id=" + id + ", nom='" + nom + "', espece=" + espece
                + ", age=" + age + ", poid=" + poid
                + ", etatSante=" + etatSante + ", temperature=" + temperature + "}";
    }
}

class Ruminant extends Animal implements Entite {
    public Ruminant(TypeEspece espece, String nom) { super(espece, nom); }
    @Override public void ajouterAnimal(Animal a) {
        System.out.println("Ajout d'un animal associé à " + getNom());
    }
}

class Volaille extends Animal implements Entite {
    public Volaille(TypeEspece espece, String nom) { super(espece, nom); }
    @Override public void ajouterAnimal(Animal a) {
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
    private FamilleCulture  famille;
    private LocalDate       datePlantation;
    private LocalDate       dateRecolte;
    private StadeCroissance stadeCroissance;
    private Seuil           exigencePH;
    private Seuil           exigenceHumidite;
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

    public void setDateRecolte(LocalDate d)           { this.dateRecolte = d; }
    public void setStadeCroissance(StadeCroissance s) { this.stadeCroissance = s; }
    public void setTemperature(int t)                 { this.temperature = t; }
    public void setHumidite(int h)                    { this.humidite = h; }
    public void setPluviometrie(int p)                { this.pluviometrie = p; }
    public void setPH(int pH)                         { this.pH = pH; }
    public void setTeneurAzote(int az)                { this.teneurAzote = az; }

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
        return "Culture{famille=" + famille + ", type=" + getTypeCulture()
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
    protected int        code;
    protected String     nom;
    protected TypeZone   type;
    protected StatutZone statut;
    protected boolean    estSuspendu;
    protected List<Capteur>                 capteurs    = new ArrayList<>();
    protected List<EnregistrementProduction> productions = new ArrayList<>();
    protected List<Alerte>                  alerts      = new ArrayList<>();
    protected List<Releve>                  relevances  = new ArrayList<>();

    public Zone(int code, String nom, TypeZone type) {
        this.code        = code;
        this.nom         = nom;
        this.type        = type;
        this.statut      = StatutZone.ACTIVE;
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

    @Override public boolean estSuspendu() { return estSuspendu; }

    public void desactiver() { this.statut = StatutZone.INACTIVE; }

    public void ajouterCapteur(Capteur c)   { capteurs.add(c); }
    public void supprimerCapteur(Capteur c) { capteurs.remove(c); }

    public void ajouterAlerte(Alerte a)   { alerts.add(a); }
    public void supprimerAlerte(Alerte a) { alerts.remove(a); }

    public void ajouterReleve(Releve r)   { relevances.add(r); }
    public void supprimerReleve(Releve r) { relevances.remove(r); }

    public void enregistrerProduction(double quantite, String typeProduction) {
        productions.add(new EnregistrementProduction(quantite, typeProduction));
    }

    public int        getCode()      { return code; }
    public String     getNom()       { return nom; }
    public TypeZone   getType()      { return type; }
    public StatutZone getStatut()    { return statut; }
    public List<Capteur>                getCapteurs()    { return Collections.unmodifiableList(capteurs); }
    public List<EnregistrementProduction> getProductions() { return Collections.unmodifiableList(productions); }
    public List<Alerte>                 getAlerts()     { return Collections.unmodifiableList(alerts); }
    public List<Releve>                 getRelevances() { return Collections.unmodifiableList(relevances); }

    public int  getNbrEntite() { return 0; }
    public void setNom(String nom)      { this.nom = nom; }
    public void setStatut(StatutZone s) { this.statut = s; }

    @Override
    public String toString() {
        return "Zone{code=" + code + ", nom='" + nom + "', type=" + type + ", statut=" + statut + '}';
    }
}

// ---- ZoneCulture ----
class ZoneCulture extends Zone {
    private List<Culture>               cultures                = new ArrayList<>();
    private List<CapteurSol>            capteurSols             = new ArrayList<>();
    private List<CapteurEnvironnemental> capteurEnvironnementals = new ArrayList<>();

    public ZoneCulture(int code, String nom, TypeZone type) { super(code, nom, type); }

    public void ajouterCulture(Culture c)   { cultures.add(c); }
    public void supprimerCulture(Culture c) { cultures.remove(c); }
    public List<Culture> getCultures()      { return Collections.unmodifiableList(cultures); }

    public void ajouterCapteurSol(CapteurSol c) { capteurSols.add(c); capteurs.add(c); }
    public void supprimerCapteurSol(CapteurSol c) { capteurSols.remove(c); capteurs.remove(c); }

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
    private List<Animal>          animals         = new ArrayList<>();
    private GeographicalLimits    limitZone;
    private List<ProgAlimentation> programme      = new ArrayList<>();
    private TypeZoneElevage       typeZoneElevage;

    public ZoneElevage(int code, String nom, TypeZone type,
                       TypeZoneElevage typeElevage, GeographicalLimits limitZone) {
        super(code, nom, type);
        this.typeZoneElevage = typeElevage;
        this.limitZone       = limitZone;
    }

    public void ajouterRuminant(Ruminant a)        { animals.add(a); }
    public void ajouterVollaile(Volaille a)         { animals.add(a); }
    public void supprimerAnimal(Animal a)           { animals.remove(a); }
    public void ajouterProgAlimentation(ProgAlimentation p) { programme.add(p); }

    public List<Animal>           getAnimals()        { return Collections.unmodifiableList(animals); }
    public List<ProgAlimentation> getProgramme()      { return Collections.unmodifiableList(programme); }
    public GeographicalLimits     getLimitZone()       { return limitZone; }
    public TypeZoneElevage        getTypeZoneElevage() { return typeZoneElevage; }

    @Override public int getNbrEntite() { return animals.size(); }
}

// ---- ZoneAquacole ----
class ZoneAquacole extends Zone {
    private List<Aquacole>        aquacoles  = new ArrayList<>();
    private List<ProgAlimentation> programme = new ArrayList<>();
    private List<CapteurEau>      capteurEau = new ArrayList<>();

    public ZoneAquacole(int code, String nom, TypeZone type) { super(code, nom, type); }

    public void ajouterAquacole(Aquacole a)   { aquacoles.add(a); }
    public void supprimerAquacole(Aquacole a) { aquacoles.remove(a); }
    public void ajouterProgAlimentation(ProgAlimentation p) { programme.add(p); }

    public void ajouterCapteurEau(CapteurEau c)   { capteurEau.add(c); capteurs.add(c); }
    public void supprimerCapteurEau(CapteurEau c) { capteurEau.remove(c); capteurs.remove(c); }

    public List<Aquacole>         getAquacoles() { return Collections.unmodifiableList(aquacoles); }
    public List<ProgAlimentation> getProgramme() { return Collections.unmodifiableList(programme); }
    public List<CapteurEau>       getCapteurEau(){ return Collections.unmodifiableList(capteurEau); }

    @Override public int getNbrEntite() { return aquacoles.size(); }
}

// =============================================================
// ==================== FERME =================================
// =============================================================

class Ferme {
    private String       nom;
    private List<Zone>   zones    = new ArrayList<>();
    private List<Alerte> alertes  = new ArrayList<>();
    private List<Culture> cultures = new ArrayList<>();
    private List<Animal>  animals  = new ArrayList<>();

    public Ferme(String nom) { this.nom = nom; }

    public String     getNom()     { return nom; }
    public List<Zone> getZones()   { return Collections.unmodifiableList(zones); }
    public List<Alerte> getAlertes() { return Collections.unmodifiableList(alertes); }
    public List<Animal> getAnimals() { return Collections.unmodifiableList(animals); }

    public void ajouterZoneElevage(ZoneElevage zone)   { zones.add(zone); }
    public void ajouterZoneCulture(ZoneCulture zone)   { zones.add(zone); }
    public void ajouterZoneAquacole(ZoneAquacole zone) { zones.add(zone); }
    public void supprimerZone(Zone zone)               { zones.remove(zone); }

    public void ajouterAlerte(Alerte a)  { alertes.add(a); }
    public void ajouterCulture(Culture c){ cultures.add(c); }
    public void ajouterAnimal(Animal a)  { animals.add(a); }

    public void afficherPanneauAlertes() {
        System.out.println("========== PANNEAU DES ALERTES ==========");
        alertes.stream()
                .filter(a -> !a.isSupprimee())
                .sorted((a, b) -> b.getNiveau().compareTo(a.getNiveau()))
                .forEach(System.out::println);
        System.out.println("=========================================");
    }
}

// =============================================================
// ==================== APP ===================================
// =============================================================

class App {
    private String nom;
    private Ferme  ferme;

    public App(String nom, Ferme ferme) { this.nom = nom; this.ferme = ferme; }

    public void desactiverZone(Zone zone) { zone.suspendre(); }
    public void reactiverZone(Zone zone)  { zone.reactiver(); }

    public void ajouterCulture(Culture culture, ZoneCulture zone) { zone.ajouterCulture(culture); }

    public void ajouterRuminant(Ruminant ruminant, ZoneElevage zone) {
        if (zone.getTypeZoneElevage() == TypeZoneElevage.Ruminant) zone.ajouterRuminant(ruminant);
    }

    public void ajouterVolaille(Volaille volaille, ZoneElevage zone) {
        if (zone.getTypeZoneElevage() == TypeZoneElevage.Volaille) zone.ajouterVollaile(volaille);
    }

    public void ajouterAquacole(Aquacole aquacole, ZoneAquacole zone) { zone.ajouterAquacole(aquacole); }

    public void traiterReleves(Capteur capteur, Zone zone) {
        capteur.envoyerReleve();
        for (Releve r : capteur.getHistoriqueReleves()) {
            if (r.getNiveau() != Gravite.normal) {
                ferme.ajouterAlerte(new Alerte(r, r.getNiveau(), String.valueOf(zone.getCode())));
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
                ferme.ajouterAlerte(new Alerte(dernier, Gravite.critique, String.valueOf(zone.getCode())));
            }
        }
    }

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
        for (Animal a : z.getAnimals()) r.append("  ").append(a).append("\n");
        r.append("--- Programme alimentation ---\n");
        for (ProgAlimentation p : z.getProgramme()) r.append("  ").append(p).append("\n");
        return r.toString();
    }

    public String afficherZoneAquacole(ZoneAquacole z) {
        StringBuilder r = new StringBuilder("--- Aquacoles ---\n");
        for (Aquacole a : z.getAquacoles()) r.append("  ").append(a).append("\n");
        r.append("--- Programme alimentation ---\n");
        for (ProgAlimentation p : z.getProgramme()) r.append("  ").append(p).append("\n");
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
        for (Zone z : ferme.getZones()) r.append(afficherZoneComplete(z));
        return r.toString();
    }

    public Ferme getFerme() { return ferme; }
}

// =============================================================
// ==================== MAIN / MENU PRINCIPAL =================
// =============================================================

public class Main {

    private static final Scanner sc          = new Scanner(System.in);
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final GestionnaireCapteursAlertes gestionnaire = GestionnaireCapteursAlertes.getInstance();

    private static App   app;
    private static Ferme ferme;

    // ==========================================================
    // ==================== MAIN ================================
    // ==========================================================

    public static void main(String[] args) {
        initialiserDemo();
        boolean actif = true;
        while (actif) {
            afficherMenuPrincipal();
            switch (lireInt()) {
                case 1: menuZones();    break;
                case 2: menuCapteurs(); break;
                case 3: menuAlertes();  break;
                case 0: actif = false; System.out.println("\n[INFO] Au revoir !\n"); break;
                default: System.out.println("[!] Choix invalide.");
            }
        }
    }

    // ==========================================================
    // ==================== DÉMO ================================
    // ==========================================================

    private static void initialiserDemo() {
        System.out.println("\n[INFO] Chargement des données de démonstration...");

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

        // Zone élevage – Vaches
        GeographicalLimits limitVaches = new GeographicalLimits("Nord Ferme", 43.45, 43.55, 1.45, 1.55);
        ZoneElevage zoneElevage = new ZoneElevage(2, "Zone Vaches", TypeZone.elevage,
                TypeZoneElevage.Ruminant, limitVaches);
        Ruminant vache1 = new Ruminant(TypeEspece.ruminant, "Vache A");
        vache1.setAge(4); vache1.setPoid(450); vache1.setEtatSante(EtatSante.sain); vache1.setTemperature(38);
        vache1.ajouterEvenementSante(new EvenementSante(TypeEvenSante.VACCIN, "Vaccin FMD 2025"));
        app.ajouterRuminant(vache1, zoneElevage);
        zoneElevage.ajouterProgAlimentation(new ProgAlimentation("Herbe", 5.5));
        ferme.ajouterZoneElevage(zoneElevage);

        // Zone élevage – Poulets
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

        // Capteurs pré-configurés
        CapteurEnvironnemental capTemp = new CapteurEnvironnemental(
                "ENV-001", "Zone Ble", TypeMesure.TEMPERATURE, new Seuil(10, 35));
        zoneCulture.ajouterCapteurEnvironnemental(capTemp);
        gestionnaire.ajouterCapteur(capTemp);

        CapteurEnvironnemental capHum = new CapteurEnvironnemental(
                "ENV-002", "Zone Ble", TypeMesure.HUMIDITE, new Seuil(40, 80));
        zoneCulture.ajouterCapteurEnvironnemental(capHum);
        gestionnaire.ajouterCapteur(capHum);

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

        // Générer des relevés initiaux
        for (int i = 0; i < 6; i++) {
            capTemp.envoyerReleve(); capHum.envoyerReleve(); capSol.envoyerReleve();
            capBio.envoyerReleve();  capGPS.envoyerReleve(); capEau.envoyerReleve();
        }

        System.out.println("[INFO] Ferme chargée : 4 zones, 6 capteurs, données initiales prêtes.\n");
    }

    // ==========================================================
    // ==================== MENUS PRINCIPAUX ====================
    // ==========================================================

    private static void afficherMenuPrincipal() {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║         SMART FARMING – MENU PRINCIPAL       ║");
        System.out.println("╠══════════════════════════════════════════════╣");
        System.out.println("║   1. Gérer les zones et entités              ║");
        System.out.println("║   2. Gérer les capteurs                      ║");
        System.out.println("║   3. Gérer les alertes                       ║");
        System.out.println("║   0. Quitter                                 ║");
        System.out.println("╚══════════════════════════════════════════════╝");
        System.out.print("Votre choix : ");
    }

    // ==========================================================
    // ==================== MENU ZONES ==========================
    // ==========================================================

    private static void menuZones() {
        boolean actif = true;
        while (actif) {
            System.out.println();
            System.out.println("┌──────────────────────────────────────────────┐");
            System.out.println("│         GESTION DES ZONES & ENTITÉS          │");
            System.out.println("├──────────────────────────────────────────────┤");
            System.out.println("│  1. Afficher toutes les zones                │");
            System.out.println("│  2. Ajouter une zone                         │");
            System.out.println("│  3. Suspendre / Réactiver une zone           │");
            System.out.println("│  4. Enregistrer une production               │");
            System.out.println("│  5. Ajouter animal / culture / aquacole      │");
            System.out.println("│  6. Afficher animaux d'une zone d'élevage    │");
            System.out.println("│  7. Afficher cultures d'une zone de culture  │");
            System.out.println("│  8. Afficher programme alimentation          │");
            System.out.println("│  0. Retour                                   │");
            System.out.println("└──────────────────────────────────────────────┘");
            System.out.print("Choix : ");
            switch (lireInt()) {
                case 1: System.out.println(app.afficherZones());   break;
                case 2: ajouterZone();                             break;
                case 3: suspendreOuReactiverZone();                break;
                case 4: enregistrerProduction();                   break;
                case 5: menuAjouterEntite();                       break;
                case 6: afficherAnimauxZone();                     break;
                case 7: afficherCulturesZone();                    break;
                case 8: afficherProgAlimentation();                break;
                case 0: actif = false;                             break;
                default: System.out.println("[!] Choix invalide.");
            }
        }
    }

    // ==========================================================
    // ==================== MENU CAPTEURS =======================
    // ==========================================================

    private static void menuCapteurs() {
        boolean actif = true;
        while (actif) {
            System.out.println();
            System.out.println("┌──────────────────────────────────────────────┐");
            System.out.println("│           GESTION DES CAPTEURS               │");
            System.out.println("├──────────────────────────────────────────────┤");
            System.out.println("│  1. Ajouter / configurer un capteur          │");
            System.out.println("│  2. Tableau de bord par zone                 │");
            System.out.println("│  3. Historique d'un capteur (filtre dates)   │");
            System.out.println("│  4. Changer le statut d'un capteur           │");
            System.out.println("│  5. Simuler l'envoi de relevés               │");
            System.out.println("│  6. Graphique ASCII évolution par capteur    │");
            System.out.println("│  7. Graphique ASCII évolution par zone       │");
            System.out.println("│  8. Graphique couleur d'un capteur           │");
            System.out.println("│  9. Évolution complète d'une zone            │");
            System.out.println("│  0. Retour                                   │");
            System.out.println("└──────────────────────────────────────────────┘");
            System.out.print("Choix : ");
            switch (lireInt()) {
                case 1: ajouterCapteur();         break;
                case 2: tableauBordZone();        break;
                case 3: historiqueCapteur();      break;
                case 4: changerStatutCapteur();   break;
                case 5: simulerReleves();         break;
                case 6: graphiqueASCIICapteur();  break;
                case 7: graphiqueASCIIZone();     break;
                case 8: graphiqueCouleurCapteur();break;
                case 9: evolutionZone();          break;
                case 0: actif = false;            break;
                default: System.out.println("[!] Choix invalide.");
            }
        }
    }

    // ==========================================================
    // ==================== MENU ALERTES ========================
    // ==========================================================

    private static void menuAlertes() {
        boolean actif = true;
        while (actif) {
            System.out.println();
            System.out.println("┌──────────────────────────────────────────────┐");
            System.out.println("│            GESTION DES ALERTES               │");
            System.out.println("├──────────────────────────────────────────────┤");
            System.out.println("│  1. Afficher les alertes actives             │");
            System.out.println("│  2. Acquitter une alerte                     │");
            System.out.println("│  3. Supprimer une alerte                     │");
            System.out.println("│  4. Historique des alertes (filtrable)       │");
            System.out.println("│  0. Retour                                   │");
            System.out.println("└──────────────────────────────────────────────┘");
            System.out.print("Choix : ");
            switch (lireInt()) {
                case 1: System.out.println(gestionnaire.afficherAlertesActives()); break;
                case 2: acquitterAlerte();   break;
                case 3: supprimerAlerte();   break;
                case 4: historiqueAlertes(); break;
                case 0: actif = false;       break;
                default: System.out.println("[!] Choix invalide.");
            }
        }
    }

    // ==========================================================
    // ==================== ACTIONS ZONES =======================
    // ==========================================================

    private static void ajouterZone() {
        System.out.println("\n--- NOUVELLE ZONE ---");
        System.out.print("Nom : "); String nom = lireString();
        System.out.print("Code (numéro) : "); int code = lireInt();
        System.out.println("Type : 1=Culture  2=Elevage Ruminant  3=Elevage Volaille  4=Aquacole");
        System.out.print("Choix : ");
        switch (lireInt()) {
            case 1: {
                ZoneCulture z = new ZoneCulture(code, nom, TypeZone.culture);
                ferme.ajouterZoneCulture(z);
                System.out.println("[OK] Zone culture « " + nom + " » créée.");
                break;
            }
            case 2: {
                GeographicalLimits limites = lireGeographicalLimits();
                ZoneElevage z = new ZoneElevage(code, nom, TypeZone.elevage, TypeZoneElevage.Ruminant, limites);
                ferme.ajouterZoneElevage(z);
                System.out.println("[OK] Zone élevage ruminant « " + nom + " » créée.");
                break;
            }
            case 3: {
                GeographicalLimits limites = lireGeographicalLimits();
                ZoneElevage z = new ZoneElevage(code, nom, TypeZone.elevage, TypeZoneElevage.Volaille, limites);
                ferme.ajouterZoneElevage(z);
                System.out.println("[OK] Zone élevage volaille « " + nom + " » créée.");
                break;
            }
            case 4: {
                ZoneAquacole z = new ZoneAquacole(code, nom, TypeZone.aquacole);
                ferme.ajouterZoneAquacole(z);
                System.out.println("[OK] Zone aquacole « " + nom + " » créée.");
                break;
            }
            default: System.out.println("[!] Type invalide.");
        }
    }

    private static GeographicalLimits lireGeographicalLimits() {
        System.out.print("Description des limites : "); String desc = lireString();
        System.out.print("Latitude min  : "); double latMin = lireDouble();
        System.out.print("Latitude max  : "); double latMax = lireDouble();
        System.out.print("Longitude min : "); double lonMin = lireDouble();
        System.out.print("Longitude max : "); double lonMax = lireDouble();
        return new GeographicalLimits(desc, latMin, latMax, lonMin, lonMax);
    }

    private static void suspendreOuReactiverZone() {
        System.out.print("Nom de la zone : ");
        Zone zone = trouverZoneParNom(lireString());
        if (zone == null) { System.out.println("[!] Zone non trouvée."); return; }
        System.out.println("Statut actuel : " + zone.getStatut());
        System.out.print("Action (1=Suspendre / 2=Réactiver) : ");
        int action = lireInt();
        if (action == 1)      { app.desactiverZone(zone); System.out.println("[OK] Zone suspendue."); }
        else if (action == 2) { app.reactiverZone(zone);  System.out.println("[OK] Zone réactivée."); }
        else System.out.println("[!] Action invalide.");
    }

    private static void enregistrerProduction() {
        System.out.print("Nom de la zone : ");
        Zone zone = trouverZoneParNom(lireString());
        if (zone == null) { System.out.println("[!] Zone non trouvée."); return; }
        System.out.print("Type de production : "); String type = lireString();
        System.out.print("Quantité : "); double q = lireDouble();
        zone.enregistrerProduction(q, type);
        System.out.println("[OK] Production enregistrée.");
    }

    private static void afficherAnimauxZone() {
        System.out.print("Nom de la zone d'élevage : ");
        Zone zone = trouverZoneParNom(lireString());
        if (!(zone instanceof ZoneElevage)) { System.out.println("[!] Zone élevage non trouvée."); return; }
        ZoneElevage ze = (ZoneElevage) zone;
        System.out.println("\n--- Animaux de « " + ze.getNom() + " » ---");
        for (Animal a : ze.getAnimals()) System.out.println("  " + a);
        System.out.println("--- Programme alimentation ---");
        for (ProgAlimentation p : ze.getProgramme()) System.out.println("  " + p);
    }

    private static void afficherCulturesZone() {
        System.out.print("Nom de la zone de culture : ");
        Zone zone = trouverZoneParNom(lireString());
        if (!(zone instanceof ZoneCulture)) { System.out.println("[!] Zone culture non trouvée."); return; }
        ZoneCulture zc = (ZoneCulture) zone;
        System.out.println("\n--- Cultures de « " + zc.getNom() + " » ---");
        for (Culture c : zc.getCultures()) {
            System.out.println("  " + c);
            System.out.println("  " + c.conditionCroissance());
            System.out.println("  " + c.afficherStats());
        }
    }

    private static void afficherProgAlimentation() {
        System.out.print("Nom de la zone (élevage ou aquacole) : ");
        Zone zone = trouverZoneParNom(lireString());
        if (zone == null) { System.out.println("[!] Zone non trouvée."); return; }
        if (zone instanceof ZoneElevage ze) {
            System.out.println("\n--- Programme alimentation « " + ze.getNom() + " » ---");
            for (ProgAlimentation p : ze.getProgramme()) System.out.println("  " + p);
        } else if (zone instanceof ZoneAquacole za) {
            System.out.println("\n--- Programme alimentation « " + za.getNom() + " » ---");
            for (ProgAlimentation p : za.getProgramme()) System.out.println("  " + p);
        } else {
            System.out.println("[!] Cette zone n'a pas de programme d'alimentation.");
        }
    }

    // ==========================================================
    // ==================== AJOUTER ENTITÉ =====================
    // ==========================================================

    private static void menuAjouterEntite() {
        System.out.println("Que voulez-vous ajouter ?");
        System.out.println("  1 = Animal (ruminant / volaille / aquacole)");
        System.out.println("  2 = Culture");
        System.out.println("  3 = Programme d'alimentation");
        System.out.print("Choix : ");
        int choix = lireInt();
        if      (choix == 1) menuAjouterAnimal();
        else if (choix == 2) menuAjouterCulture();
        else if (choix == 3) menuAjouterProgAlimentation();
        else System.out.println("[!] Choix invalide.");
    }

    private static void menuAjouterAnimal() {
        System.out.print("Nom de l'animal : "); String nom = lireString();
        System.out.print("Espèce (ruminant / volaille / aqua) : "); String especeStr = lireString().toLowerCase();
        System.out.print("Âge : "); int age = lireInt();
        System.out.print("Poids : "); int poid = lireInt();
        System.out.print("Nom de la zone cible : ");
        Zone zone = trouverZoneParNom(lireString());
        if (zone == null) { System.out.println("[!] Zone non trouvée."); return; }
        try {
            TypeEspece espece = TypeEspece.valueOf(especeStr);
            if (espece == TypeEspece.ruminant && zone instanceof ZoneElevage ze) {
                Ruminant r = new Ruminant(TypeEspece.ruminant, nom); r.setAge(age); r.setPoid(poid);
                app.ajouterRuminant(r, ze); System.out.println("[OK] Ruminant ajouté.");
            } else if (espece == TypeEspece.volaille && zone instanceof ZoneElevage ze) {
                Volaille v = new Volaille(TypeEspece.volaille, nom); v.setAge(age); v.setPoid(poid);
                app.ajouterVolaille(v, ze); System.out.println("[OK] Volaille ajoutée.");
            } else if (espece == TypeEspece.aqua && zone instanceof ZoneAquacole za) {
                Aquacole a = new Aquacole(TypeEspece.aqua, nom); a.setAge(age); a.setPoid(poid);
                app.ajouterAquacole(a, za); System.out.println("[OK] Aquacole ajouté.");
            } else {
                System.out.println("[!] Incompatibilité espèce/zone.");
            }
        } catch (Exception e) { System.out.println("[ERREUR] " + e.getMessage()); }
    }

    private static void menuAjouterCulture() {
        System.out.print("Famille (Cereal / Legume / Fruit) : "); String famStr = lireString();
        System.out.print("Date plantation (yyyy-MM-dd) : ");
        LocalDate dp = LocalDate.parse(lireString());
        System.out.print("Date récolte    (yyyy-MM-dd) : ");
        LocalDate dr = LocalDate.parse(lireString());
        System.out.print("pH min : "); double phMin = lireDouble();
        System.out.print("pH max : "); double phMax = lireDouble();
        System.out.print("Humidité min : "); double hMin = lireDouble();
        System.out.print("Humidité max : "); double hMax = lireDouble();
        System.out.print("Nom de la ZoneCulture cible : ");
        Zone zone = trouverZoneParNom(lireString());
        if (!(zone instanceof ZoneCulture)) { System.out.println("[!] Zone culture non trouvée."); return; }
        try {
            FamilleCulture fam = FamilleCulture.valueOf(famStr);
            Culture culture;
            switch (fam) {
                case Cereal: culture = new Cereal(fam, dp, dr, new Seuil(phMin, phMax), new Seuil(hMin, hMax)); break;
                case Legume: culture = new Legume(fam, dp, dr, new Seuil(phMin, phMax), new Seuil(hMin, hMax)); break;
                default:     culture = new Fruit(fam, dp, dr, new Seuil(phMin, phMax), new Seuil(hMin, hMax));
            }
            app.ajouterCulture(culture, (ZoneCulture) zone);
            System.out.println("[OK] Culture ajoutée.");
        } catch (Exception e) { System.out.println("[ERREUR] " + e.getMessage()); }
    }

    private static void menuAjouterProgAlimentation() {
        System.out.print("Nom de la zone (élevage ou aquacole) : ");
        Zone zone = trouverZoneParNom(lireString());
        if (zone == null) { System.out.println("[!] Zone non trouvée."); return; }
        System.out.print("Type d'aliment : "); String typeAliment = lireString();
        System.out.print("Quantité (kg) : "); double quantite = lireDouble();
        ProgAlimentation p = new ProgAlimentation(typeAliment, quantite);
        if      (zone instanceof ZoneElevage  ze) ze.ajouterProgAlimentation(p);
        else if (zone instanceof ZoneAquacole za) za.ajouterProgAlimentation(p);
        else { System.out.println("[!] Cette zone n'accepte pas de programme d'alimentation."); return; }
        System.out.println("[OK] Programme d'alimentation ajouté.");
    }

    // ==========================================================
    // ==================== ACTIONS CAPTEURS ====================
    // ==========================================================

    private static void ajouterCapteur() {
        System.out.println("\n--- NOUVEAU CAPTEUR ---");
        System.out.print("ID du capteur : "); String id = lireString();
        if (gestionnaire.getCapteurById(id) != null) { System.out.println("[!] ID déjà utilisé."); return; }
        System.out.print("Nom de la zone : ");
        Zone zone = trouverZoneParNom(lireString());
        if (zone == null) { System.out.println("[!] Zone non trouvée."); return; }
        System.out.println("\nType : 1=Environnemental  2=Sol  3=Eau  4=Biométrique  5=GPS");
        System.out.print("Choix : ");
        Capteur capteur = null;
        try {
            switch (lireInt()) {
                case 1: capteur = construireEnvironnemental(id, zone.getNom()); break;
                case 2: capteur = construireSol(id, zone.getNom());             break;
                case 3: capteur = construireEau(id, zone.getNom());             break;
                case 4: capteur = construireBiometrique(id, zone.getNom());     break;
                case 5: capteur = new CapteurGPS(id, zone.getNom()); System.out.println("[OK] GPS créé."); break;
                default: System.out.println("[!] Type invalide."); return;
            }
        } catch (IllegalArgumentException e) { System.out.println("[ERREUR] " + e.getMessage()); return; }

        zone.ajouterCapteur(capteur);
        gestionnaire.ajouterCapteur(capteur);

        if (zone instanceof ZoneCulture zc) {
            if (capteur instanceof CapteurSol cs) zc.ajouterCapteurSol(cs);
            else if (capteur instanceof CapteurEnvironnemental ce) zc.ajouterCapteurEnvironnemental(ce);
        } else if (zone instanceof ZoneAquacole za && capteur instanceof CapteurEau ce) {
            za.ajouterCapteurEau(ce);
        }
        System.out.println("[OK] Capteur « " + id + " » ajouté à la zone « " + zone.getNom() + " ».");
    }

    private static CapteurEnvironnemental construireEnvironnemental(String id, String zone) {
        TypeMesure t = choisirParmi(new String[]{"TEMPERATURE","HUMIDITE","PLUVIOMETRIE"},
                new TypeMesure[]{TypeMesure.TEMPERATURE, TypeMesure.HUMIDITE, TypeMesure.PLUVIOMETRIE});
        System.out.println("Seuils pour " + t + " :"); return new CapteurEnvironnemental(id, zone, t, lireSeuil());
    }
    private static CapteurSol construireSol(String id, String zone) {
        TypeMesure t = choisirParmi(new String[]{"PH_SOL","HUMIDITE_SOL","AZOTE"},
                new TypeMesure[]{TypeMesure.PH_SOL, TypeMesure.HUMIDITE_SOL, TypeMesure.AZOTE});
        System.out.println("Seuils pour " + t + " :"); return new CapteurSol(id, zone, t, lireSeuil());
    }
    private static CapteurEau construireEau(String id, String zone) {
        TypeMesure t = choisirParmi(new String[]{"TEMPERATURE_EAU","OXYGENE_DISSOUS","PH_EAU"},
                new TypeMesure[]{TypeMesure.TEMPERATURE_EAU, TypeMesure.OXYGENE_DISSOUS, TypeMesure.PH_EAU});
        System.out.println("Seuils pour " + t + " :"); return new CapteurEau(id, zone, t, lireSeuil());
    }
    private static CapteurBiometrique construireBiometrique(String id, String zone) {
        System.out.println("Seuil température corporelle (°C) :"); Seuil st = lireSeuil();
        System.out.println("Seuil activité (pas/min) :");          Seuil sa = lireSeuil();
        return new CapteurBiometrique(id, zone, st, sa);
    }
    private static TypeMesure choisirParmi(String[] labels, TypeMesure[] options) {
        for (int i = 0; i < options.length; i++) System.out.println("  " + (i+1) + ". " + labels[i]);
        System.out.print("Choix : ");
        int idx = lireInt() - 1;
        if (idx < 0 || idx >= options.length) throw new IllegalArgumentException("Type invalide.");
        return options[idx];
    }

    private static void tableauBordZone() {
        System.out.print("Nom de la zone : ");
        Zone zone = trouverZoneParNom(lireString());
        if (zone == null) { System.out.println("[!] Zone non trouvée."); return; }
        System.out.println(gestionnaire.afficherTableauBordZone(zone.getNom()));
    }

    private static void historiqueCapteur() {
        System.out.print("ID du capteur : ");
        Capteur capteur = gestionnaire.getCapteurById(lireString());
        if (capteur == null) { System.out.println("[!] Capteur inconnu."); return; }
        System.out.print("Filtrer par période ? (o/n) : ");
        if (lireString().equalsIgnoreCase("o")) {
            LocalDateTime debut = lireDateTime("Date début (yyyy-MM-dd HH:mm) : ");
            LocalDateTime fin   = lireDateTime("Date fin   (yyyy-MM-dd HH:mm) : ");
            List<Releve> releves = capteur.filtrerRelevesParDate(debut, fin);
            if (releves.isEmpty()) { System.out.println("[INFO] Aucun relevé dans cette période."); return; }
            System.out.println("\n=== HISTORIQUE FILTRÉ – " + capteur.getId() + " ===");
            System.out.printf("%-26s %-18s %-12s%n", "Timestamp", "Valeur", "Niveau");
            System.out.println("─".repeat(58));
            for (Releve r : releves)
                System.out.printf("%-26s %-18s %-12s%n",
                        r.getTimestamp().format(DTF), r.getValeurAsString(), r.getNiveau());
        } else {
            System.out.println(gestionnaire.afficherEvolutionReleves(capteur.getId()));
        }
    }

    private static void changerStatutCapteur() {
        System.out.print("ID du capteur : ");
        Capteur capteur = gestionnaire.getCapteurById(lireString());
        if (capteur == null) { System.out.println("[!] Capteur inconnu."); return; }
        System.out.println("Statut actuel : " + capteur.getStatut());
        System.out.println("1=ACTIVE  2=INACTIVE (défaillant)  3=SUSPENDU");
        System.out.print("Choix : ");
        switch (lireInt()) {
            case 1: capteur.reactiver();                           System.out.println("[OK] → ACTIVE.");   break;
            case 2: capteur.changerStatut(StatutCapteur.INACTIVE); System.out.println("[OK] → INACTIVE."); break;
            case 3: capteur.suspendre();                           System.out.println("[OK] → SUSPENDU."); break;
            default: System.out.println("[!] Choix invalide.");
        }
    }

    private static void simulerReleves() {
        System.out.println("1=Un capteur  2=Tous les capteurs d'une zone  3=Tous les capteurs actifs");
        System.out.print("Choix : ");
        switch (lireInt()) {
            case 1: {
                System.out.print("ID du capteur : ");
                Capteur c = gestionnaire.getCapteurById(lireString());
                if (c == null) { System.out.println("[!] Capteur inconnu."); return; }
                System.out.print("Nombre de relevés : "); int n = Math.max(1, lireInt());
                for (int i = 0; i < n; i++) c.envoyerReleve();
                System.out.println("[OK] " + n + " relevé(s) pour " + c.getId() + ".");
                break;
            }
            case 2: {
                System.out.print("Nom de la zone : ");
                Zone zone = trouverZoneParNom(lireString());
                if (zone == null) { System.out.println("[!] Zone non trouvée."); return; }
                List<Capteur> liste = gestionnaire.getCapteursParZone(zone.getNom());
                if (liste.isEmpty()) { System.out.println("[!] Aucun capteur dans cette zone."); return; }
                System.out.print("Nombre de relevés par capteur : "); int n = Math.max(1, lireInt());
                for (Capteur c : liste) for (int i = 0; i < n; i++) c.envoyerReleve();
                System.out.println("[OK] " + n + " × " + liste.size() + " capteur(s).");
                break;
            }
            case 3: {
                System.out.print("Nombre de relevés par capteur : "); int n = Math.max(1, lireInt());
                int count = 0;
                for (Capteur c : gestionnaire.getTousCapteurs())
                    if (c.getStatut() == StatutCapteur.ACTIVE) {
                        for (int i = 0; i < n; i++) c.envoyerReleve(); count++;
                    }
                System.out.println("[OK] " + n + " × " + count + " capteur(s) actif(s).");
                break;
            }
            default: System.out.println("[!] Choix invalide.");
        }
    }

    private static void graphiqueASCIICapteur() {
        System.out.print("ID du capteur : ");
        Capteur capteur = gestionnaire.getCapteurById(lireString());
        if (capteur == null) { System.out.println("[!] Capteur inconnu."); return; }
        List<ReleveNumerique> nums = extraireNumeriques(capteur.getHistoriqueReleves());
        if (nums.isEmpty()) { System.out.println("[INFO] Aucun relevé numérique."); return; }
        afficherGraphiqueASCII("CAPTEUR " + capteur.getId() + " (" + capteur.getTypeNom() + ")", nums);
    }

    private static void graphiqueASCIIZone() {
        System.out.print("Nom de la zone : ");
        Zone zone = trouverZoneParNom(lireString());
        if (zone == null) { System.out.println("[!] Zone non trouvée."); return; }
        List<Capteur> capteurs = gestionnaire.getCapteursParZone(zone.getNom());
        if (capteurs.isEmpty()) { System.out.println("[!] Aucun capteur dans cette zone."); return; }
        System.out.println(gestionnaire.afficherEvolutionRelevesZone(zone.getNom()));
        for (Capteur c : capteurs) {
            List<ReleveNumerique> nums = extraireNumeriques(c.getHistoriqueReleves());
            if (!nums.isEmpty())
                afficherGraphiqueASCII("ZONE " + zone.getNom() + " | " + c.getId()
                        + " (" + c.getTypeNom() + ")", nums);
        }
    }

    private static void graphiqueCouleurCapteur() {
        System.out.print("ID du capteur : ");
        Capteur capteur = gestionnaire.getCapteurById(lireString());
        if (capteur == null) { System.out.println("[!] Capteur inconnu."); return; }
        gestionnaire.afficherGraphiqueCapteur(capteur);
    }

    private static void evolutionZone() {
        System.out.print("Nom de la zone : ");
        Zone zone = trouverZoneParNom(lireString());
        if (zone == null) { System.out.println("[!] Zone non trouvée."); return; }
        System.out.println(gestionnaire.afficherEvolutionRelevesZone(zone.getNom()));
    }

    // ==========================================================
    // ==================== GRAPHIQUE ASCII =====================
    // ==========================================================

    private static List<ReleveNumerique> extraireNumeriques(List<Releve> releves) {
        List<ReleveNumerique> res = new ArrayList<>();
        for (Releve r : releves) if (r instanceof ReleveNumerique) res.add((ReleveNumerique) r);
        return res;
    }

    private static void afficherGraphiqueASCII(String titre, List<ReleveNumerique> releves) {
        int debut = Math.max(0, releves.size() - 20);
        List<ReleveNumerique> s = releves.subList(debut, releves.size());
        double min = Double.MAX_VALUE, max = -Double.MAX_VALUE;
        for (ReleveNumerique r : s) {
            if (r.getValeur() < min) min = r.getValeur();
            if (r.getValeur() > max) max = r.getValeur();
        }
        double range   = (max - min == 0) ? 1 : max - min;
        int    hauteur = 10;
        System.out.println("\n╔══ GRAPHIQUE : " + titre);
        System.out.printf("║  Unité=%-8s  Min=%.2f  Max=%.2f  Relevés=%d%n",
                s.get(0).getUnite(), min, max, s.size());
        System.out.println("╠══════════════════════════════════════════════════════╣");
        for (int row = hauteur; row >= 0; row--) {
            System.out.printf("║ %7.2f │", min + (range * row / hauteur));
            for (ReleveNumerique r : s) {
                double norm = (r.getValeur() - min) / range * hauteur;
                if (norm >= row - 0.5) {
                    switch (r.getNiveau()) {
                        case critique:      System.out.print("█ "); break;
                        case avertissement: System.out.print("▒ "); break;
                        default:            System.out.print("░ "); break;
                    }
                } else System.out.print("  ");
            }
            System.out.println("║");
        }
        System.out.print("║         └");
        for (int i = 0; i < s.size(); i++) System.out.print("──");
        System.out.println("║");
        System.out.print("║          ");
        for (int i = 1; i <= s.size(); i++) System.out.printf("%-2s", (i % 5 == 0) ? String.valueOf(i) : ".");
        System.out.println("║");
        System.out.println("╠══════════════════════════════════════════════════════╣");
        System.out.println("║  Légende :  ░ Normal   ▒ Avertissement   █ Critique ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
    }

    // ==========================================================
    // ==================== ACTIONS ALERTES =====================
    // ==========================================================

    private static void acquitterAlerte() {
        System.out.println(gestionnaire.afficherAlertesActives());
        System.out.print("ID de l'alerte à acquitter : ");
        long id = lireLong();
        System.out.println(gestionnaire.acquitterAlerte(id)
                ? "[OK] Alerte #" + id + " acquittée."
                : "[!] Introuvable ou déjà supprimée.");
    }

    private static void supprimerAlerte() {
        System.out.println(gestionnaire.afficherAlertesActives());
        System.out.print("ID de l'alerte à supprimer : ");
        long id = lireLong();
        System.out.println(gestionnaire.supprimerAlerte(id)
                ? "[OK] Alerte #" + id + " supprimée."
                : "[!] Introuvable.");
    }

    private static void historiqueAlertes() {
        System.out.println("\n--- FILTRES (Entrée = ignorer) ---");
        System.out.print("Nom de zone (vide = toutes) : "); String zNom = lireString();
        String zoneId = zNom.isEmpty() ? null : trouverZoneParNom(zNom) != null
                ? trouverZoneParNom(zNom).getNom() : zNom;

        TypeMesure[] types = TypeMesure.values();
        System.out.println("Type de mesure :"); System.out.println("  0. Tous");
        for (int i = 0; i < types.length; i++) System.out.println("  " + (i+1) + ". " + types[i]);
        System.out.print("Choix : "); int tm = lireInt();
        TypeMesure typeMesure = (tm >= 1 && tm <= types.length) ? types[tm-1] : null;

        System.out.println("Niveau : 0=Tous  1=normal  2=avertissement  3=critique");
        System.out.print("Choix : ");
        Gravite niveau = null;
        switch (lireInt()) {
            case 1: niveau = Gravite.normal;        break;
            case 2: niveau = Gravite.avertissement; break;
            case 3: niveau = Gravite.critique;      break;
        }

        LocalDateTime debut = null, fin = null;
        System.out.print("Filtrer par période ? (o/n) : ");
        if (lireString().equalsIgnoreCase("o")) {
            debut = lireDateTime("Date début (yyyy-MM-dd HH:mm) : ");
            fin   = lireDateTime("Date fin   (yyyy-MM-dd HH:mm) : ");
        }

        List<Alerte> alertes = gestionnaire.filtrerAlertes(zoneId, typeMesure, niveau, debut, fin);
        if (alertes.isEmpty()) { System.out.println("[INFO] Aucune alerte ne correspond."); return; }

        System.out.println("\n=== HISTORIQUE (" + alertes.size() + " résultat(s)) ===");
        System.out.printf("%-6s  %-15s  %-15s  %-20s  %-12s  %s%n",
                "ID", "Capteur", "Niveau", "Date", "Valeur", "Acquittée");
        System.out.println("─".repeat(82));
        for (Alerte a : alertes)
            System.out.printf("%-6d  %-15s  %-15s  %-20s  %-12s  %s%n",
                    a.getId(), a.getReleve().getIdCapteur(), a.getNiveau(),
                    a.getDateCreation().format(DTF), a.getReleve().getValeurAsString(),
                    a.isAcquittee() ? "Oui" : "Non");
    }

    // ==========================================================
    // ==================== UTILITAIRES =========================
    // ==========================================================

    private static Zone trouverZoneParNom(String nom) {
        for (Zone z : ferme.getZones())
            if (z.getNom().equalsIgnoreCase(nom)) return z;
        return null;
    }

    private static Seuil lireSeuil() {
        System.out.print("  Min : "); double min = lireDouble();
        System.out.print("  Max : "); double max = lireDouble();
        return new Seuil(min, max);
    }

    private static int lireInt() {
        try { return Integer.parseInt(sc.nextLine().trim()); }
        catch (NumberFormatException e) { return -1; }
    }

    private static long lireLong() {
        try { return Long.parseLong(sc.nextLine().trim()); }
        catch (NumberFormatException e) { return -1L; }
    }

    private static double lireDouble() {
        try { return Double.parseDouble(sc.nextLine().trim().replace(',', '.')); }
        catch (NumberFormatException e) { System.out.println("[!] 0 utilisé."); return 0.0; }
    }

    private static String lireString() { return sc.nextLine().trim(); }

    private static LocalDateTime lireDateTime(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return LocalDateTime.parse(sc.nextLine().trim(), DTF); }
            catch (DateTimeParseException e) { System.out.println("[!] Format : yyyy-MM-dd HH:mm"); }
        }
    }
}