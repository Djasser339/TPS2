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
        if (valeur < min - tolerance || valeur > max + tolerance)
            return Gravite.critique;
        if (valeur < min || valeur > max)
            return Gravite.avertissement;
        return Gravite.normal;
    }

    public double getMin() { return min; }
    public double getMax() { return max; }
}

// ==================== RELEVE ====================
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

    public String getValeurAsString() {
        return valeur + " " + unite;
    }
}

class ReleveGPS extends Releve {
    private final double latitude;
    private final double longitude;

    public ReleveGPS(String idCapteur, double latitude, double longitude) {
        super(idCapteur);
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    public String getValeurAsString() {
        return String.format("lat=%.4f, lon=%.4f", latitude, longitude);
    }
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

    public Capteur(String id, String zoneId) {
        this.id = id;
        this.zoneId = zoneId;
        this.statut = StatutCapteur.ACTIVE;
        this.historiqueReleves = new ArrayList<>();
    }

    public abstract void envoyerReleve();

    public void changerStatut(StatutCapteur nouveauStatut) {
        this.statut = nouveauStatut;
    }


    public void suspendre() {
        this.statut = StatutCapteur.SUSPENDU;
    }


    public void reactiver() {
        this.statut = StatutCapteur.ACTIVE;
    }


    public boolean estSuspendu() {
        return this.statut == StatutCapteur.SUSPENDU;
    }

    public void ajouterReleve(Releve releve) {
        historiqueReleves.add(releve);
    }

    public List<Releve> getHistoriqueReleves() {
        return Collections.unmodifiableList(historiqueReleves);
    }


    public List<Releve> filtrerRelevesParDate(LocalDateTime debut, LocalDateTime fin) {
        List<Releve> resultat = new ArrayList<>();
        for (Releve r : historiqueReleves) {
            if (!r.getTimestamp().isBefore(debut) && !r.getTimestamp().isAfter(fin)) {
                resultat.add(r);
            }
        }
        return resultat;
    }

    public String getId() { return id; }
    public String getZoneId() { return zoneId; }
    public StatutCapteur getStatut() { return statut; }
    public void setZoneId(String zoneId) { this.zoneId = zoneId; }
}

// ==================== CAPTEUR NUMÉRIQUE (ABSTRACT) ====================
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

    public void configurerSeuil(Seuil nouveauSeuil) {
        this.seuil = nouveauSeuil;
    }

    protected ReleveNumerique effectuerMesure(double valeur) {
        ReleveNumerique releve = new ReleveNumerique(this.id, valeur, this.unite, this.typeMesure);
        Gravite gravite = seuil.evaluerGravite(valeur);
        releve.setNiveau(gravite);
        this.ajouterReleve(releve);
        return releve;
    }

    public TypeMesure getTypeMesure() { return typeMesure; }
    public Seuil getSeuil() { return seuil; }
    public String getUnite() { return unite; }
}

// ==================== CAPTEURS CONCRETS ====================
class CapteurEnvironnemental extends CapteurNumerique {
    public CapteurEnvironnemental(String id, String zoneId, TypeMesure type, Seuil seuil) {
        super(id, zoneId, type, seuil,
                type == TypeMesure.TEMPERATURE ? "°C" :
                        type == TypeMesure.HUMIDITE ? "%" : "mm");
        if (type != TypeMesure.TEMPERATURE && type != TypeMesure.HUMIDITE && type != TypeMesure.PLUVIOMETRIE)
            throw new IllegalArgumentException("Type non valide pour capteur environnemental");
    }


    public void envoyerReleve() {
        if (this.statut != StatutCapteur.ACTIVE) {
            return;
        }
        Random rand = new Random();
        double valeur;
        switch (typeMesure) {
            case TEMPERATURE: valeur = 15 + rand.nextDouble() * 20; break;
            case HUMIDITE:    valeur = 40 + rand.nextDouble() * 60; break;
            default:          valeur = rand.nextDouble() * 50;
        }
        effectuerMesure(valeur);
    }
}

class CapteurSol extends CapteurNumerique {
    public CapteurSol(String id, String zoneId, TypeMesure type, Seuil seuil) {
        super(id, zoneId, type, seuil,
                type == TypeMesure.PH_SOL ? "pH" :
                        type == TypeMesure.HUMIDITE_SOL ? "%" : "mg/kg");
        if (type != TypeMesure.PH_SOL && type != TypeMesure.HUMIDITE_SOL && type != TypeMesure.AZOTE)
            throw new IllegalArgumentException("Type non valide pour capteur de sol");
    }


    public void envoyerReleve() {
        if (this.statut != StatutCapteur.ACTIVE) {
            return;
        }
        Random rand = new Random();
        double valeur;
        switch (typeMesure) {
            case PH_SOL:        valeur = 5.5 + rand.nextDouble() * 4; break;
            case HUMIDITE_SOL:  valeur = 10 + rand.nextDouble() * 70; break;
            default:            valeur = 20 + rand.nextDouble() * 180;
        }
        effectuerMesure(valeur);
    }
}

class CapteurEau extends CapteurNumerique {
    public CapteurEau(String id, String zoneId, TypeMesure type, Seuil seuil) {
        super(id, zoneId, type, seuil,
                type == TypeMesure.TEMPERATURE_EAU ? "°C" :
                        type == TypeMesure.OXYGENE_DISSOUS ? "mg/L" : "pH");
        if (type != TypeMesure.TEMPERATURE_EAU && type != TypeMesure.OXYGENE_DISSOUS && type != TypeMesure.PH_EAU)
            throw new IllegalArgumentException("Type non valide pour capteur aquacole");
    }


    public void envoyerReleve() {
        if (this.statut != StatutCapteur.ACTIVE) {
            return;
        }
        Random rand = new Random();
        double valeur;
        switch (typeMesure) {
            case TEMPERATURE_EAU:   valeur = 10 + rand.nextDouble() * 15; break;
            case OXYGENE_DISSOUS:   valeur = 4 + rand.nextDouble() * 8; break;
            default:                valeur = 6.5 + rand.nextDouble() * 2;
        }
        effectuerMesure(valeur);
    }
}

class CapteurBiometrique extends Capteur {
    private Seuil seuilTemperature;
    private Seuil seuilActivite;

    public CapteurBiometrique(String id, String zoneId, Seuil seuilTemperature, Seuil seuilActivite) {
        super(id, zoneId);
        this.seuilTemperature = seuilTemperature;
        this.seuilActivite = seuilActivite;
    }

    public void configurerSeuils(Seuil seuilTemperature, Seuil seuilActivite) {
        this.seuilTemperature = seuilTemperature;
        this.seuilActivite = seuilActivite;
    }


    public void envoyerReleve() {
        if (this.statut != StatutCapteur.ACTIVE) {
            return;
        }
        Random rand = new Random();
        double temperature = 37 + rand.nextDouble() * 3;
        double activite = 20 + rand.nextDouble() * 100;

        ReleveNumerique releveTemp = new ReleveNumerique(this.id, temperature, "°C", TypeMesure.TEMPERATURE_CORPORELLE);
        ReleveNumerique releveAct = new ReleveNumerique(this.id, activite, "pas/min", TypeMesure.ACTIVITE_PAS_PAR_MINUTE);

        Gravite gTemp = seuilTemperature.evaluerGravite(temperature);
        Gravite gAct = seuilActivite.evaluerGravite(activite);
        releveTemp.setNiveau(gTemp);
        releveAct.setNiveau(gAct);

        this.ajouterReleve(releveTemp);
        this.ajouterReleve(releveAct);
    }

    public Seuil getSeuilTemperature() { return seuilTemperature; }
    public Seuil getSeuilActivite() { return seuilActivite; }
}

class CapteurGPS extends Capteur {
    private double latitude;
    private double longitude;
    private List<double[]> historiquePositions = new ArrayList<>();

    public CapteurGPS(String id, String zoneId) {
        super(id, zoneId);
    }


    public void envoyerReleve() {
        if (this.statut != StatutCapteur.ACTIVE) {
            return;
        }
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
    public List<double[]> getHistoriquePositions() {
        return Collections.unmodifiableList(historiquePositions);
    }
}