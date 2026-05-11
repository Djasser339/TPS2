import java.time.LocalDateTime;
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

// ==================== RELEVES ====================
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

// ==================== ALERTE ====================
class Alerte {
    private static long compteur = 0;
    private final long id;
    private final Releve releve;
    private final Gravite niveau;
    private final LocalDateTime dateCreation;
    private boolean acquittee;
    private boolean supprimee;

    public Alerte(Releve releve, Gravite niveau) {
        this.id = ++compteur;
        this.releve = releve;
        this.niveau = niveau;
        this.dateCreation = LocalDateTime.now();
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
}

// ==================== GESTIONNAIRE GLOBAL ====================
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

    // Ajouter un capteur
    public void ajouterCapteur(Capteur capteur) {
        tousLesCapteurs.add(capteur);
        capteursParZone.computeIfAbsent(capteur.getZoneId(), k -> new ArrayList<>()).add(capteur);
        capteursParId.put(capteur.getId(), capteur);
    }

    // Déclencher une alerte
    public void declencherAlerte(Releve releve, Gravite niveau) {
        if (niveau != Gravite.normal) {
            alertes.add(new Alerte(releve, niveau));
        }
    }

    // Récupérer les capteurs d'une zone
    public List<Capteur> getCapteursParZone(String zoneId) {
        return capteursParZone.getOrDefault(zoneId, Collections.emptyList());
    }

    // ========== TABLEAU DE BORD PAR ZONE (dernier relevé) ==========
    public String afficherTableauBordZone(String zoneId) {
        List<Capteur> capteurs = getCapteursParZone(zoneId);
        if (capteurs.isEmpty()) return "Aucun capteur dans la zone " + zoneId;
        StringBuilder sb = new StringBuilder();
        sb.append("\n--- TABLEAU DE BORD - ZONE ").append(zoneId).append(" ---\n");
        for (Capteur c : capteurs) {
            List<Releve> historique = c.getHistoriqueReleves();
            if (historique.isEmpty()) {
                sb.append("Capteur ").append(c.getId()).append(" : pas de relevé\n");
                continue;
            }
            Releve dernier = historique.get(historique.size() - 1);
            String niveauStr;
            String couleur;
            switch (dernier.getNiveau()) {
                case normal: niveauStr = "NORMAL"; couleur = "\u001B[32m"; break;
                case avertissement: niveauStr = "AVERTISSEMENT"; couleur = "\u001B[33m"; break;
                case critique: niveauStr = "CRITIQUE"; couleur = "\u001B[31m"; break;
                default: niveauStr = "INCONNU"; couleur = "";
            }
            sb.append("Capteur ").append(c.getId()).append(" (").append(c.getTypeNom()).append(") : ")
                    .append(couleur).append(niveauStr).append("\u001B[0m")
                    .append(" - Valeur : ").append(dernier.getValeurAsString()).append("\n");
        }
        return sb.toString();
    }

    // ========== ÉVOLUTION DES RELEVÉS (tableau textuel) ==========
    public String afficherEvolutionReleves(String idCapteur) {
        Capteur capteur = capteursParId.get(idCapteur);
        if (capteur == null) return "Capteur inconnu : " + idCapteur;
        List<Releve> historique = capteur.getHistoriqueReleves();
        if (historique.isEmpty()) return "Aucun relevé pour ce capteur.";
        StringBuilder sb = new StringBuilder();
        sb.append("\n=== ÉVOLUTION DES RELEVÉS - CAPTEUR ").append(idCapteur).append(" ===\n");
        sb.append(String.format("%-20s %-15s %-10s\n", "Date", "Valeur", "Niveau"));
        sb.append("------------------------------------------------\n");
        for (Releve r : historique) {
            String niveau = r.getNiveau().toString();
            sb.append(String.format("%-20s %-15s %-10s\n",
                    r.getTimestamp().toString(),
                    r.getValeurAsString(),
                    niveau));
        }
        return sb.toString();
    }

    public String afficherEvolutionRelevesZone(String zoneId) {
        List<Capteur> capteurs = getCapteursParZone(zoneId);
        if (capteurs.isEmpty()) return "Aucun capteur dans la zone " + zoneId;
        StringBuilder sb = new StringBuilder();
        sb.append("\n=== ÉVOLUTION DES RELEVÉS - ZONE ").append(zoneId).append(" ===\n");
        for (Capteur c : capteurs) {
            sb.append(afficherEvolutionReleves(c.getId())).append("\n");
        }
        return sb.toString();
    }

    // ========== PANNEAU DES ALERTES ACTIVES ==========
    public String afficherAlertesActives() {
        List<Alerte> actives = new ArrayList<>();
        for (Alerte a : alertes) {
            if (!a.isAcquittee() && !a.isSupprimee()) actives.add(a);
        }
        Collections.sort(actives, new Comparator<Alerte>() {
            public int compare(Alerte a1, Alerte a2) {
                return a2.getNiveau().compareTo(a1.getNiveau()); // critique en premier
            }
        });
        if (actives.isEmpty()) return "Aucune alerte active.";
        StringBuilder sb = new StringBuilder("\n=== ALERTES ACTIVES ===\n");
        for (Alerte a : actives) {
            sb.append("ID: ").append(a.getId())
                    .append(" | Niveau: ").append(a.getNiveau())
                    .append(" | Capteur: ").append(a.getReleve().getIdCapteur())
                    .append(" | Date: ").append(a.getDateCreation())
                    .append(" | Valeur: ").append(a.getReleve().getValeurAsString())
                    .append("\n");
        }
        return sb.toString();
    }

    // Acquitter / supprimer
    public boolean acquitterAlerte(long id) {
        for (Alerte a : alertes) {
            if (a.getId() == id && !a.isSupprimee()) {
                a.acquitter();
                return true;
            }
        }
        return false;
    }

    public boolean supprimerAlerte(long id) {
        for (Alerte a : alertes) {
            if (a.getId() == id) {
                a.supprimer();
                return true;
            }
        }
        return false;
    }

    // Filtrage des alertes
    public List<Alerte> filtrerAlertes(String zoneId, TypeMesure typeCapteur, Gravite niveau, LocalDateTime debut, LocalDateTime fin) {
        List<Alerte> resultat = new ArrayList<>();
        for (Alerte a : alertes) {
            Capteur capteur = capteursParId.get(a.getReleve().getIdCapteur());
            if (capteur == null) continue;
            if (zoneId != null && !capteur.getZoneId().equals(zoneId)) continue;
            if (typeCapteur != null) {
                Releve r = a.getReleve();
                if (!(r instanceof ReleveNumerique)) continue;
                if (((ReleveNumerique) r).getTypeMesure() != typeCapteur) continue;
            }
            if (niveau != null && a.getNiveau() != niveau) continue;
            if (debut != null && a.getDateCreation().isBefore(debut)) continue;
            if (fin != null && a.getDateCreation().isAfter(fin)) continue;
            resultat.add(a);
        }
        // Tri par gravité (critique en premier)
        Collections.sort(resultat, new Comparator<Alerte>() {
            public int compare(Alerte a1, Alerte a2) {
                return a2.getNiveau().compareTo(a1.getNiveau());
            }
        });
        return resultat;
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
        this.id = id;
        this.zoneId = zoneId;
        this.statut = StatutCapteur.ACTIVE;
        this.historiqueReleves = new ArrayList<>();
    }
    public abstract void envoyerReleve();
    public void changerStatut(StatutCapteur nouveauStatut) { this.statut = nouveauStatut; }
    public void suspendre() { this.statut = StatutCapteur.SUSPENDU; }
    public void reactiver() { this.statut = StatutCapteur.ACTIVE; }
    public boolean estSuspendu() { return this.statut == StatutCapteur.SUSPENDU; }
    public void ajouterReleve(Releve releve) { historiqueReleves.add(releve); }
    public List<Releve> getHistoriqueReleves() { return Collections.unmodifiableList(historiqueReleves); }
    public List<Releve> filtrerRelevesParDate(LocalDateTime debut, LocalDateTime fin) {
        List<Releve> resultat = new ArrayList<>();
        for (Releve r : historiqueReleves) {
            if (!r.getTimestamp().isBefore(debut) && !r.getTimestamp().isAfter(fin))
                resultat.add(r);
        }
        return resultat;
    }
    public String getId() { return id; }
    public String getZoneId() { return zoneId; }
    public StatutCapteur getStatut() { return statut; }
    public void setZoneId(String zoneId) { this.zoneId = zoneId; }
    public abstract String getTypeNom();
}

// ==================== CAPTEUR NUMÉRIQUE ====================
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
        if (gravite != Gravite.normal) gestionnaire.declencherAlerte(releve, gravite);
        return releve;
    }
    public TypeMesure getTypeMesure() { return typeMesure; }
    public Seuil getSeuil() { return seuil; }
    public String getUnite() { return unite; }
}

// ==================== CAPTEURS CONCRETS ====================
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
        if (gTemp != Gravite.normal) gestionnaire.declencherAlerte(rTemp, gTemp);
        if (gAct != Gravite.normal) gestionnaire.declencherAlerte(rAct, gAct);
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