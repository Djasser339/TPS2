import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

// ==================== ÉNUMÉRATIONS ====================
enum Gravite { normal, avertissement, critique }
enum StatutCapteur { ACTIVE, INACTIVE, SUSPENDU }
enum TypeMesure {
    TEMPERATURE, HUMIDITE, PLUVIOMETRIE,
    PH_SOL, HUMIDITE_SOL, AZOTE,
    TEMPERATURE_EAU, OXYGENE_DISSOUS, PH_EAU,
    TEMPERATURE_CORPORELLE, ACTIVITE_PAS_PAR_MINUTE
}

// ==================== SEUIL ====================
class Seuil {
    private double min, max;
    public Seuil(double min, double max) {
        if (min >= max) throw new IllegalArgumentException("min doit être < max");
        this.min = min; this.max = max;
    }
    public boolean estHorsLimites(double v) { return v < min || v > max; }
    public Gravite evaluerGravite(double valeur) {
        double tolerance = (max - min) * 0.1;
        if (valeur < min - tolerance || valeur > max + tolerance) return Gravite.critique;
        if (valeur < min || valeur > max) return Gravite.avertissement;
        return Gravite.normal;
    }
    public double getMin() { return min; }
    public double getMax() { return max; }
}

// ==================== RELEVES ====================
abstract class Releve {
    private static long compteur = 0;
    private final long id;
    private final String idCapteur;
    private final LocalDateTime timestamp;
    private Gravite niveau;
    public Releve(String idCapteur) {
        this.id = ++compteur; this.idCapteur = idCapteur;
        this.timestamp = LocalDateTime.now(); this.niveau = Gravite.normal;
    }
    public long getId()                 { return id; }
    public String getIdCapteur()        { return idCapteur; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public Gravite getNiveau()          { return niveau; }
    public void setNiveau(Gravite n)    { this.niveau = n; }
    public abstract String getValeurAsString();
}

class ReleveNumerique extends Releve {
    private final double valeur;
    private final String unite;
    private final TypeMesure typeMesure;
    public ReleveNumerique(String idCapteur, double valeur, String unite, TypeMesure typeMesure) {
        super(idCapteur); this.valeur = valeur; this.unite = unite; this.typeMesure = typeMesure;
    }
    public double getValeur()         { return valeur; }
    public String getUnite()          { return unite; }
    public TypeMesure getTypeMesure() { return typeMesure; }
    public String getValeurAsString() { return valeur + " " + unite; }
}

class ReleveGPS extends Releve {
    private final double latitude, longitude;
    public ReleveGPS(String idCapteur, double latitude, double longitude) {
        super(idCapteur); this.latitude = latitude; this.longitude = longitude;
    }
    public double getLatitude()       { return latitude; }
    public double getLongitude()      { return longitude; }
    public String getValeurAsString() { return String.format("lat=%.4f, lon=%.4f", latitude, longitude); }
}

// ==================== ALERTE ====================
class Alerte {
    private static long compteur = 0;
    private final long id;
    private final Releve releve;
    private final Gravite niveau;
    private final LocalDateTime dateCreation;
    private boolean acquittee, supprimee;
    public Alerte(Releve releve, Gravite niveau) {
        this.id = ++compteur; this.releve = releve; this.niveau = niveau;
        this.dateCreation = LocalDateTime.now(); this.acquittee = false; this.supprimee = false;
    }
    public void acquitter()              { this.acquittee = true; }
    public void supprimer()              { this.supprimee = true; }
    public long getId()                  { return id; }
    public Releve getReleve()            { return releve; }
    public Gravite getNiveau()           { return niveau; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public boolean isAcquittee()         { return acquittee; }
    public boolean isSupprimee()         { return supprimee; }
}

// ==================== GESTIONNAIRE GLOBAL ====================
class GestionnaireCapteursAlertes {
    private static GestionnaireCapteursAlertes instance;
    private List<Capteur>              tousLesCapteurs = new ArrayList<>();
    private Map<String, List<Capteur>> capteursParZone = new HashMap<>();
    private Map<String, Capteur>       capteursParId   = new HashMap<>();
    private List<Alerte>               alertes         = new ArrayList<>();

    private GestionnaireCapteursAlertes() {}
    public static GestionnaireCapteursAlertes getInstance() {
        if (instance == null) instance = new GestionnaireCapteursAlertes();
        return instance;
    }

    public void ajouterCapteur(Capteur c) {
        tousLesCapteurs.add(c);
        capteursParZone.computeIfAbsent(c.getZoneId(), k -> new ArrayList<>()).add(c);
        capteursParId.put(c.getId(), c);
    }

    // MÉTHODE AJOUTÉE — accès par ID
    public Capteur getCapteurById(String id) { return capteursParId.get(id); }

    // MÉTHODE AJOUTÉE — liste complète
    public List<Capteur> getTousCapteurs() { return Collections.unmodifiableList(tousLesCapteurs); }

    // MÉTHODE AJOUTÉE — liaison avec la Zone du binôme
    public void suspendreZone(String zoneId) {
        for (Capteur c : capteursParZone.getOrDefault(zoneId, Collections.emptyList())) c.suspendre();
    }
    public void reactiverZone(String zoneId) {
        for (Capteur c : capteursParZone.getOrDefault(zoneId, Collections.emptyList())) c.reactiver();
    }

    public void declencherAlerte(Releve releve, Gravite niveau) {
        if (niveau != Gravite.normal) alertes.add(new Alerte(releve, niveau));
    }
    public List<Capteur> getCapteursParZone(String zoneId) {
        return capteursParZone.getOrDefault(zoneId, Collections.emptyList());
    }

    public String afficherTableauBordZone(String zoneId) {
        List<Capteur> capteurs = getCapteursParZone(zoneId);
        if (capteurs.isEmpty()) return "Aucun capteur dans la zone " + zoneId;
        StringBuilder sb = new StringBuilder("\n--- TABLEAU DE BORD - ZONE " + zoneId + " ---\n");
        for (Capteur c : capteurs) {
            List<Releve> hist = c.getHistoriqueReleves();
            if (hist.isEmpty()) { sb.append("Capteur ").append(c.getId()).append(" : pas de relevé\n"); continue; }
            Releve dernier = hist.get(hist.size() - 1);
            String niveauStr, couleur;
            switch (dernier.getNiveau()) {
                case normal:        niveauStr = "NORMAL";        couleur = "\u001B[32m"; break;
                case avertissement: niveauStr = "AVERTISSEMENT"; couleur = "\u001B[33m"; break;
                case critique:      niveauStr = "CRITIQUE";      couleur = "\u001B[31m"; break;
                default:            niveauStr = "INCONNU";       couleur = "";
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
        List<Releve> hist = capteur.getHistoriqueReleves();
        if (hist.isEmpty()) return "Aucun relevé pour ce capteur.";
        StringBuilder sb = new StringBuilder("\n=== ÉVOLUTION – CAPTEUR " + idCapteur + " ===\n");
        sb.append(String.format("%-25s %-18s %-12s%n", "Date", "Valeur", "Niveau"));
        sb.append("─".repeat(56)).append("\n");
        for (Releve r : hist)
            sb.append(String.format("%-25s %-18s %-12s%n", r.getTimestamp(), r.getValeurAsString(), r.getNiveau()));
        return sb.toString();
    }

    public String afficherEvolutionRelevesZone(String zoneId) {
        List<Capteur> capteurs = getCapteursParZone(zoneId);
        if (capteurs.isEmpty()) return "Aucun capteur dans la zone " + zoneId;
        StringBuilder sb = new StringBuilder("\n=== ÉVOLUTION – ZONE " + zoneId + " ===\n");
        for (Capteur c : capteurs) sb.append(afficherEvolutionReleves(c.getId())).append("\n");
        return sb.toString();
    }

    public String afficherAlertesActives() {
        List<Alerte> actives = new ArrayList<>();
        for (Alerte a : alertes) if (!a.isAcquittee() && !a.isSupprimee()) actives.add(a);
        actives.sort((a1, a2) -> a2.getNiveau().compareTo(a1.getNiveau()));
        if (actives.isEmpty()) return "Aucune alerte active.";
        StringBuilder sb = new StringBuilder("\n=== ALERTES ACTIVES ===\n");
        for (Alerte a : actives)
            sb.append("ID: ").append(a.getId()).append(" | Niveau: ").append(a.getNiveau())
                    .append(" | Capteur: ").append(a.getReleve().getIdCapteur())
                    .append(" | Date: ").append(a.getDateCreation())
                    .append(" | Valeur: ").append(a.getReleve().getValeurAsString()).append("\n");
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

    public List<Alerte> filtrerAlertes(String zoneId, TypeMesure typeCapteur,
                                       Gravite niveau, LocalDateTime debut, LocalDateTime fin) {
        List<Alerte> res = new ArrayList<>();
        for (Alerte a : alertes) {
            Capteur c = capteursParId.get(a.getReleve().getIdCapteur());
            if (c == null) continue;
            if (zoneId      != null && !c.getZoneId().equals(zoneId)) continue;
            if (typeCapteur != null) {
                Releve r = a.getReleve();
                if (!(r instanceof ReleveNumerique)) continue;
                if (((ReleveNumerique) r).getTypeMesure() != typeCapteur) continue;
            }
            if (niveau != null && a.getNiveau() != niveau) continue;
            if (debut  != null && a.getDateCreation().isBefore(debut)) continue;
            if (fin    != null && a.getDateCreation().isAfter(fin))    continue;
            res.add(a);
        }
        res.sort((a1, a2) -> a2.getNiveau().compareTo(a1.getNiveau()));
        return res;
    }
}

// ==================== INTERFACE SUSPENDABLE ====================
interface Suspendable {
    void suspendre();
    void reactiver();
    boolean estSuspendu();
}

// ==================== CAPTEUR (ABSTRACT) ====================
abstract class Capteur implements Suspendable {
    protected final String id;
    protected String zoneId;
    protected StatutCapteur statut;
    protected List<Releve> historiqueReleves;
    protected GestionnaireCapteursAlertes gestionnaire = GestionnaireCapteursAlertes.getInstance();

    public Capteur(String id, String zoneId) {
        this.id = id; this.zoneId = zoneId;
        this.statut = StatutCapteur.ACTIVE; this.historiqueReleves = new ArrayList<>();
    }
    public abstract void envoyerReleve();
    public void changerStatut(StatutCapteur s) { this.statut = s; }
    public void suspendre()   { this.statut = StatutCapteur.SUSPENDU; }
    public void reactiver()   { this.statut = StatutCapteur.ACTIVE; }
    public boolean estSuspendu() { return this.statut == StatutCapteur.SUSPENDU; }
    public void ajouterReleve(Releve r) { historiqueReleves.add(r); }
    public List<Releve> getHistoriqueReleves() { return Collections.unmodifiableList(historiqueReleves); }
    public List<Releve> filtrerRelevesParDate(LocalDateTime debut, LocalDateTime fin) {
        List<Releve> res = new ArrayList<>();
        for (Releve r : historiqueReleves)
            if (!r.getTimestamp().isBefore(debut) && !r.getTimestamp().isAfter(fin)) res.add(r);
        return res;
    }
    public String getId()            { return id; }
    public String getZoneId()        { return zoneId; }
    public StatutCapteur getStatut() { return statut; }
    public void setZoneId(String z)  { this.zoneId = z; }
    public abstract String getTypeNom();
}

// ==================== CAPTEUR NUMERIQUE ====================
abstract class CapteurNumerique extends Capteur {
    protected TypeMesure typeMesure;
    protected Seuil seuil;
    protected String unite;
    public CapteurNumerique(String id, String zoneId, TypeMesure type, Seuil seuil, String unite) {
        super(id, zoneId); this.typeMesure = type; this.seuil = seuil; this.unite = unite;
    }
    public void configurerSeuil(Seuil s) { this.seuil = s; }
    protected ReleveNumerique effectuerMesure(double valeur) {
        ReleveNumerique r = new ReleveNumerique(this.id, valeur, this.unite, this.typeMesure);
        Gravite g = seuil.evaluerGravite(valeur);
        r.setNiveau(g); this.ajouterReleve(r);
        if (g != Gravite.normal) gestionnaire.declencherAlerte(r, g);
        return r;
    }
    public TypeMesure getTypeMesure() { return typeMesure; }
    public Seuil getSeuil()           { return seuil; }
    public String getUnite()          { return unite; }
}

// ==================== CAPTEURS CONCRETS ====================
class CapteurEnvironnemental extends CapteurNumerique {
    public CapteurEnvironnemental(String id, String zoneId, TypeMesure type, Seuil seuil) {
        super(id, zoneId, type, seuil,
                type == TypeMesure.TEMPERATURE ? "°C" : type == TypeMesure.HUMIDITE ? "%" : "mm");
        if (type != TypeMesure.TEMPERATURE && type != TypeMesure.HUMIDITE && type != TypeMesure.PLUVIOMETRIE)
            throw new IllegalArgumentException("Type non valide pour CapteurEnvironnemental");
    }
    @Override public void envoyerReleve() {
        if (statut != StatutCapteur.ACTIVE) return;
        Random r = new Random(); double v;
        switch (typeMesure) {
            case TEMPERATURE: v = 15 + r.nextDouble() * 20; break;
            case HUMIDITE:    v = 40 + r.nextDouble() * 60; break;
            default:          v = r.nextDouble() * 50;
        }
        effectuerMesure(v);
    }
    @Override public String getTypeNom() { return "Environnemental"; }
}

class CapteurSol extends CapteurNumerique {
    public CapteurSol(String id, String zoneId, TypeMesure type, Seuil seuil) {
        super(id, zoneId, type, seuil,
                type == TypeMesure.PH_SOL ? "pH" : type == TypeMesure.HUMIDITE_SOL ? "%" : "mg/kg");
        if (type != TypeMesure.PH_SOL && type != TypeMesure.HUMIDITE_SOL && type != TypeMesure.AZOTE)
            throw new IllegalArgumentException("Type non valide pour CapteurSol");
    }
    @Override public void envoyerReleve() {
        if (statut != StatutCapteur.ACTIVE) return;
        Random r = new Random(); double v;
        switch (typeMesure) {
            case PH_SOL:       v = 5.5 + r.nextDouble() * 4;  break;
            case HUMIDITE_SOL: v = 10  + r.nextDouble() * 70; break;
            default:           v = 20  + r.nextDouble() * 180;
        }
        effectuerMesure(v);
    }
    @Override public String getTypeNom() { return "Sol"; }
}

class CapteurEau extends CapteurNumerique {
    public CapteurEau(String id, String zoneId, TypeMesure type, Seuil seuil) {
        super(id, zoneId, type, seuil,
                type == TypeMesure.TEMPERATURE_EAU ? "°C" : type == TypeMesure.OXYGENE_DISSOUS ? "mg/L" : "pH");
        if (type != TypeMesure.TEMPERATURE_EAU && type != TypeMesure.OXYGENE_DISSOUS && type != TypeMesure.PH_EAU)
            throw new IllegalArgumentException("Type non valide pour CapteurEau");
    }
    @Override public void envoyerReleve() {
        if (statut != StatutCapteur.ACTIVE) return;
        Random r = new Random(); double v;
        switch (typeMesure) {
            case TEMPERATURE_EAU: v = 10  + r.nextDouble() * 15; break;
            case OXYGENE_DISSOUS: v = 4   + r.nextDouble() * 8;  break;
            default:              v = 6.5 + r.nextDouble() * 2;
        }
        effectuerMesure(v);
    }
    @Override public String getTypeNom() { return "Eau"; }
}

class CapteurBiometrique extends Capteur {
    private Seuil seuilTemperature, seuilActivite;
    public CapteurBiometrique(String id, String zoneId, Seuil seuilTemp, Seuil seuilAct) {
        super(id, zoneId); this.seuilTemperature = seuilTemp; this.seuilActivite = seuilAct;
    }
    public void configurerSeuils(Seuil t, Seuil a) { seuilTemperature = t; seuilActivite = a; }
    @Override public void envoyerReleve() {
        if (statut != StatutCapteur.ACTIVE) return;
        Random r = new Random();
        double temp = 37 + r.nextDouble() * 3;
        double act  = 20 + r.nextDouble() * 100;
        ReleveNumerique rT = new ReleveNumerique(id, temp, "°C",      TypeMesure.TEMPERATURE_CORPORELLE);
        ReleveNumerique rA = new ReleveNumerique(id, act,  "pas/min", TypeMesure.ACTIVITE_PAS_PAR_MINUTE);
        Gravite gT = seuilTemperature.evaluerGravite(temp);
        Gravite gA = seuilActivite.evaluerGravite(act);
        rT.setNiveau(gT); rA.setNiveau(gA);
        ajouterReleve(rT); ajouterReleve(rA);
        if (gT != Gravite.normal) gestionnaire.declencherAlerte(rT, gT);
        if (gA != Gravite.normal) gestionnaire.declencherAlerte(rA, gA);
    }
    @Override public String getTypeNom()    { return "Biométrique"; }
    public Seuil getSeuilTemperature()      { return seuilTemperature; }
    public Seuil getSeuilActivite()         { return seuilActivite; }
}

class CapteurGPS extends Capteur {
    private double latitude, longitude;
    private List<double[]> historiquePositions = new ArrayList<>();
    public CapteurGPS(String id, String zoneId) { super(id, zoneId); }
    @Override public void envoyerReleve() {
        if (statut != StatutCapteur.ACTIVE) return;
        Random r = new Random();
        this.latitude  = 43.5 + (r.nextDouble() - 0.5) * 0.1;
        this.longitude = 1.5  + (r.nextDouble() - 0.5) * 0.1;
        historiquePositions.add(new double[]{latitude, longitude});
        ajouterReleve(new ReleveGPS(id, latitude, longitude));
    }
    public boolean estHorsLimites(double latMin, double latMax, double lonMin, double lonMax) {
        return latitude < latMin || latitude > latMax || longitude < lonMin || longitude > lonMax;
    }
    public double getLatitude()  { return latitude; }
    public double getLongitude() { return longitude; }
    public List<double[]> getHistoriquePositions() { return Collections.unmodifiableList(historiquePositions); }
    @Override public String getTypeNom() { return "GPS"; }
}

// ============================================================
//  MENU — : Capteurs & Alertes
// ============================================================
public class Main {

    private static final Scanner sc = new Scanner(System.in);
    private static final GestionnaireCapteursAlertes gestion = GestionnaireCapteursAlertes.getInstance();
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // ── MAIN ─────────────────────────────────────────────────────
    public static void main(String[] args) {
        initialiserDemo();
        boolean actif = true;
        while (actif) {
            afficherMenuPrincipal();
            switch (lireInt()) {
                case 1: menuCapteurs(); break;
                case 2: menuAlertes();  break;
                case 0: actif = false; System.out.println("\n[INFO] Au revoir !\n"); break;
                default: System.out.println("[!] Choix invalide.");
            }
        }
    }

    // ── DEMO ─────────────────────────────────────────────────────
    private static void initialiserDemo() {
        System.out.println("\n[INFO] Chargement des données de démonstration...");
        CapteurEnvironnemental cTemp = new CapteurEnvironnemental("C-ENV-01","Z1",TypeMesure.TEMPERATURE,new Seuil(15,35));
        CapteurEnvironnemental cHum  = new CapteurEnvironnemental("C-ENV-02","Z1",TypeMesure.HUMIDITE,   new Seuil(40,80));
        CapteurSol             cPh   = new CapteurSol("C-SOL-01","Z1",TypeMesure.PH_SOL,           new Seuil(5.5,7.5));
        CapteurBiometrique     cBio  = new CapteurBiometrique("C-BIO-01","Z2",new Seuil(37.0,39.5),new Seuil(20,120));
        CapteurGPS             cGps  = new CapteurGPS("C-GPS-01","Z2");
        CapteurEau             cEau  = new CapteurEau("C-EAU-01","Z3",TypeMesure.PH_EAU,           new Seuil(6.5,8.5));
        CapteurEau             cO2   = new CapteurEau("C-EAU-02","Z3",TypeMesure.OXYGENE_DISSOUS,  new Seuil(4.0,12.0));
        for (Capteur c : new Capteur[]{cTemp,cHum,cPh,cBio,cGps,cEau,cO2}) gestion.ajouterCapteur(c);
        for (int i = 0; i < 6; i++) {
            cTemp.envoyerReleve(); cHum.envoyerReleve(); cPh.envoyerReleve();
            cBio.envoyerReleve();  cGps.envoyerReleve(); cEau.envoyerReleve(); cO2.envoyerReleve();
        }
        System.out.println("[INFO] 7 capteurs (Z1=Culture, Z2=Élevage, Z3=Aquacole) chargés.\n");
    }

    // ── MENUS ─────────────────────────────────────────────────────
    private static void afficherMenuPrincipal() {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║      SMART FARMING – CAPTEURS & ALERTES      ║");
        System.out.println("╠══════════════════════════════════════════════╣");
        System.out.println("║   1. Gérer les capteurs                      ║");
        System.out.println("║   2. Gérer les alertes                       ║");
        System.out.println("║   0. Quitter                                 ║");
        System.out.println("╚══════════════════════════════════════════════╝");
        System.out.print("Votre choix : ");
    }

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
            System.out.println("│  6. Graphique évolution par capteur          │");
            System.out.println("│  7. Graphique évolution par zone             │");
            System.out.println("│  0. Retour                                   │");
            System.out.println("└──────────────────────────────────────────────┘");
            System.out.print("Choix : ");
            switch (lireInt()) {
                case 1: ajouterCapteur();    break;
                case 2: tableauBordZone();   break;
                case 3: historiqueCapteur(); break;
                case 4: changerStatut();     break;
                case 5: simulerReleves();    break;
                case 6: graphiqueCapteur();  break;
                case 7: graphiqueZone();     break;
                case 0: actif = false;       break;
                default: System.out.println("[!] Choix invalide.");
            }
        }
    }

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
                case 1: System.out.println(gestion.afficherAlertesActives()); break;
                case 2: acquitterAlerte();   break;
                case 3: supprimerAlerte();   break;
                case 4: historiqueAlertes(); break;
                case 0: actif = false;       break;
                default: System.out.println("[!] Choix invalide.");
            }
        }
    }

    // ── 1. AJOUTER UN CAPTEUR ─────────────────────────────────────
    private static void ajouterCapteur() {
        System.out.println("\n--- NOUVEAU CAPTEUR ---");
        System.out.print("ID du capteur : "); String id = lireString();
        if (gestion.getCapteurById(id) != null) { System.out.println("[!] ID déjà utilisé."); return; }
        System.out.print("Zone ID       : "); String zone = lireString();
        System.out.println("\nType : 1=Environnemental  2=Sol  3=Eau  4=Biométrique  5=GPS");
        System.out.print("Choix : ");
        Capteur capteur = null;
        try {
            switch (lireInt()) {
                case 1: capteur = construireEnvironnemental(id, zone); break;
                case 2: capteur = construireSol(id, zone);             break;
                case 3: capteur = construireEau(id, zone);             break;
                case 4: capteur = construireBiometrique(id, zone);     break;
                case 5: capteur = new CapteurGPS(id, zone); System.out.println("[OK] GPS créé."); break;
                default: System.out.println("[!] Type invalide."); return;
            }
        } catch (IllegalArgumentException e) { System.out.println("[ERREUR] " + e.getMessage()); return; }
        gestion.ajouterCapteur(capteur);
        System.out.println("[OK] Capteur « " + id + " » ajouté à la zone " + zone + ".");
    }

    private static CapteurEnvironnemental construireEnvironnemental(String id, String zone) {
        TypeMesure t = choisirParmi(new String[]{"TEMPERATURE","HUMIDITE","PLUVIOMETRIE"},
                new TypeMesure[]{TypeMesure.TEMPERATURE,TypeMesure.HUMIDITE,TypeMesure.PLUVIOMETRIE});
        System.out.println("Seuils pour " + t + " :"); return new CapteurEnvironnemental(id,zone,t,lireSeuil());
    }
    private static CapteurSol construireSol(String id, String zone) {
        TypeMesure t = choisirParmi(new String[]{"PH_SOL","HUMIDITE_SOL","AZOTE"},
                new TypeMesure[]{TypeMesure.PH_SOL,TypeMesure.HUMIDITE_SOL,TypeMesure.AZOTE});
        System.out.println("Seuils pour " + t + " :"); return new CapteurSol(id,zone,t,lireSeuil());
    }
    private static CapteurEau construireEau(String id, String zone) {
        TypeMesure t = choisirParmi(new String[]{"TEMPERATURE_EAU","OXYGENE_DISSOUS","PH_EAU"},
                new TypeMesure[]{TypeMesure.TEMPERATURE_EAU,TypeMesure.OXYGENE_DISSOUS,TypeMesure.PH_EAU});
        System.out.println("Seuils pour " + t + " :"); return new CapteurEau(id,zone,t,lireSeuil());
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
    private static Seuil lireSeuil() {
        System.out.print("  Min : "); double min = lireDouble();
        System.out.print("  Max : "); double max = lireDouble();
        return new Seuil(min, max);
    }

    // ── 2. TABLEAU DE BORD ────────────────────────────────────────
    private static void tableauBordZone() {
        System.out.print("Zone ID : ");
        System.out.println(gestion.afficherTableauBordZone(lireString()));
    }

    // ── 3. HISTORIQUE ─────────────────────────────────────────────
    private static void historiqueCapteur() {
        System.out.print("ID du capteur : ");
        Capteur capteur = gestion.getCapteurById(lireString());
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
                System.out.printf("%-26s %-18s %-12s%n", r.getTimestamp().format(DTF), r.getValeurAsString(), r.getNiveau());
        } else {
            System.out.println(gestion.afficherEvolutionReleves(capteur.getId()));
        }
    }

    // ── 4. CHANGER STATUT ─────────────────────────────────────────
    private static void changerStatut() {
        System.out.print("ID du capteur : ");
        Capteur capteur = gestion.getCapteurById(lireString());
        if (capteur == null) { System.out.println("[!] Capteur inconnu."); return; }
        System.out.println("Statut actuel : " + capteur.getStatut());
        System.out.println("1=ACTIVE  2=INACTIVE(défaillant)  3=SUSPENDU");
        System.out.print("Choix : ");
        switch (lireInt()) {
            case 1: capteur.reactiver();                           System.out.println("[OK] → ACTIVE.");    break;
            case 2: capteur.changerStatut(StatutCapteur.INACTIVE); System.out.println("[OK] → INACTIVE.");  break;
            case 3: capteur.suspendre();                           System.out.println("[OK] → SUSPENDU.");  break;
            default: System.out.println("[!] Choix invalide.");
        }
    }

    // ── 5. SIMULER RELEVES ────────────────────────────────────────
    private static void simulerReleves() {
        System.out.println("1=Un capteur  2=Tous les capteurs d'une zone  3=Tous les capteurs actifs");
        System.out.print("Choix : ");
        switch (lireInt()) {
            case 1: {
                System.out.print("ID du capteur : ");
                Capteur c = gestion.getCapteurById(lireString());
                if (c == null) { System.out.println("[!] Capteur inconnu."); return; }
                System.out.print("Nombre de relevés : "); int n = Math.max(1, lireInt());
                for (int i = 0; i < n; i++) c.envoyerReleve();
                System.out.println("[OK] " + n + " relevé(s) pour " + c.getId() + ".");
                break;
            }
            case 2: {
                System.out.print("Zone ID : "); String zone = lireString();
                List<Capteur> liste = gestion.getCapteursParZone(zone);
                if (liste.isEmpty()) { System.out.println("[!] Zone inconnue ou vide."); return; }
                System.out.print("Nombre de relevés par capteur : "); int n = Math.max(1, lireInt());
                for (Capteur c : liste) for (int i = 0; i < n; i++) c.envoyerReleve();
                System.out.println("[OK] " + n + " × " + liste.size() + " capteur(s).");
                break;
            }
            case 3: {
                System.out.print("Nombre de relevés par capteur : "); int n = Math.max(1, lireInt());
                int count = 0;
                for (Capteur c : gestion.getTousCapteurs())
                    if (c.getStatut() == StatutCapteur.ACTIVE) {
                        for (int i = 0; i < n; i++) c.envoyerReleve(); count++;
                    }
                System.out.println("[OK] " + n + " × " + count + " capteur(s) actif(s).");
                break;
            }
            default: System.out.println("[!] Choix invalide.");
        }
    }

    // ── 6. GRAPHIQUE PAR CAPTEUR ──────────────────────────────────
    private static void graphiqueCapteur() {
        System.out.print("ID du capteur : ");
        Capteur capteur = gestion.getCapteurById(lireString());
        if (capteur == null) { System.out.println("[!] Capteur inconnu."); return; }
        List<ReleveNumerique> nums = extraireNumeriques(capteur.getHistoriqueReleves());
        if (nums.isEmpty()) { System.out.println("[INFO] Aucun relevé numérique."); return; }
        afficherGraphiqueASCII("CAPTEUR " + capteur.getId() + " (" + capteur.getTypeNom() + ")", nums);
    }

    // ── 7. GRAPHIQUE PAR ZONE ─────────────────────────────────────
    private static void graphiqueZone() {
        System.out.print("Zone ID : "); String zone = lireString();
        List<Capteur> capteurs = gestion.getCapteursParZone(zone);
        if (capteurs.isEmpty()) { System.out.println("[!] Aucun capteur dans cette zone."); return; }
        System.out.println(gestion.afficherEvolutionRelevesZone(zone));
        for (Capteur c : capteurs) {
            List<ReleveNumerique> nums = extraireNumeriques(c.getHistoriqueReleves());
            if (!nums.isEmpty())
                afficherGraphiqueASCII("ZONE " + zone + " | " + c.getId() + " (" + c.getTypeNom() + ")", nums);
        }
    }

    // ── GRAPHIQUE ASCII ───────────────────────────────────────────
    private static List<ReleveNumerique> extraireNumeriques(List<Releve> releves) {
        List<ReleveNumerique> res = new ArrayList<>();
        for (Releve r : releves) if (r instanceof ReleveNumerique) res.add((ReleveNumerique) r);
        return res;
    }

    private static void afficherGraphiqueASCII(String titre, List<ReleveNumerique> releves) {
        int debut = Math.max(0, releves.size() - 20);
        List<ReleveNumerique> s = releves.subList(debut, releves.size());
        double min = Double.MAX_VALUE, max = -Double.MAX_VALUE;
        for (ReleveNumerique r : s) { if (r.getValeur() < min) min = r.getValeur(); if (r.getValeur() > max) max = r.getValeur(); }
        double range = (max - min == 0) ? 1 : max - min;
        int hauteur = 10;
        System.out.println("\n╔══ GRAPHIQUE : " + titre);
        System.out.printf("║  Unité=%-8s  Min=%.2f  Max=%.2f  Relevés=%d%n", s.get(0).getUnite(), min, max, s.size());
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
        System.out.print("║         └"); for (int i = 0; i < s.size(); i++) System.out.print("──"); System.out.println("║");
        System.out.print("║          "); for (int i = 1; i <= s.size(); i++) System.out.printf("%-2s", (i%5==0)?String.valueOf(i):"."); System.out.println("║");
        System.out.println("╠══════════════════════════════════════════════════════╣");
        System.out.println("║  Légende :  ░ Normal   ▒ Avertissement   █ Critique ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
    }

    // ── ALERTES ───────────────────────────────────────────────────
    private static void acquitterAlerte() {
        System.out.println(gestion.afficherAlertesActives());
        System.out.print("ID de l'alerte à acquitter : "); long id = lireLong();
        System.out.println(gestion.acquitterAlerte(id) ? "[OK] Alerte #"+id+" acquittée." : "[!] Introuvable.");
    }

    private static void supprimerAlerte() {
        System.out.println(gestion.afficherAlertesActives());
        System.out.print("ID de l'alerte à supprimer : "); long id = lireLong();
        System.out.println(gestion.supprimerAlerte(id) ? "[OK] Alerte #"+id+" supprimée." : "[!] Introuvable.");
    }

    private static void historiqueAlertes() {
        System.out.println("\n--- FILTRES (Entrée = ignorer) ---");
        System.out.print("Zone ID (vide = toutes) : "); String zone = lireString();
        TypeMesure[] types = TypeMesure.values();
        System.out.println("Type de mesure :"); System.out.println("  0. Tous");
        for (int i = 0; i < types.length; i++) System.out.println("  " + (i+1) + ". " + types[i]);
        System.out.print("Choix : "); int tm = lireInt();
        TypeMesure typeMesure = (tm >= 1 && tm <= types.length) ? types[tm-1] : null;
        System.out.println("Niveau : 0=Tous  1=normal  2=avertissement  3=critique");
        System.out.print("Choix : ");
        Gravite niveau = null;
        switch (lireInt()) { case 1: niveau=Gravite.normal; break; case 2: niveau=Gravite.avertissement; break; case 3: niveau=Gravite.critique; break; }
        LocalDateTime debut = null, fin = null;
        System.out.print("Filtrer par période ? (o/n) : ");
        if (lireString().equalsIgnoreCase("o")) {
            debut = lireDateTime("Date début (yyyy-MM-dd HH:mm) : ");
            fin   = lireDateTime("Date fin   (yyyy-MM-dd HH:mm) : ");
        }
        List<Alerte> alertes = gestion.filtrerAlertes(zone.isEmpty() ? null : zone, typeMesure, niveau, debut, fin);
        if (alertes.isEmpty()) { System.out.println("[INFO] Aucune alerte ne correspond."); return; }
        System.out.println("\n=== HISTORIQUE (" + alertes.size() + " résultat(s)) ===");
        System.out.printf("%-6s  %-15s  %-15s  %-20s  %-12s  %s%n","ID","Capteur","Niveau","Date","Valeur","Acquittée");
        System.out.println("─".repeat(82));
        for (Alerte a : alertes)
            System.out.printf("%-6d  %-15s  %-15s  %-20s  %-12s  %s%n",
                    a.getId(), a.getReleve().getIdCapteur(), a.getNiveau(),
                    a.getDateCreation().format(DTF), a.getReleve().getValeurAsString(), a.isAcquittee()?"Oui":"Non");
    }

    // ── UTILITAIRES ───────────────────────────────────────────────
    private static int    lireInt()    { try { return Integer.parseInt(sc.nextLine().trim()); } catch (NumberFormatException e) { return -1; } }
    private static long   lireLong()   { try { return Long.parseLong(sc.nextLine().trim()); }  catch (NumberFormatException e) { return -1L; } }
    private static double lireDouble() { try { return Double.parseDouble(sc.nextLine().trim().replace(',','.')); } catch (NumberFormatException e) { System.out.println("[!] 0 utilisé."); return 0.0; } }
    private static String lireString() { return sc.nextLine().trim(); }
    private static LocalDateTime lireDateTime(String prompt) {
        while (true) { System.out.print(prompt);
            try { return LocalDateTime.parse(sc.nextLine().trim(), DTF); }
            catch (DateTimeParseException e) { System.out.println("[!] Format : yyyy-MM-dd HH:mm"); } }
    }
}