package Smart_Farm;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.io.*;

// ==================== CAPTEUR (ABSTRACT) ====================
abstract class Capteur implements Suspendable, Serializable {
    private static final long serialVersionUID = 1L;

    protected final String id;
    protected String zoneId;
    protected StatutCapteur statut;
    protected List<Releve> historiqueReleves;
    protected transient GestionnaireCapteursAlertes gestionnaire;

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        this.gestionnaire = GestionnaireCapteursAlertes.getInstance();
    }

    public Capteur(String id, String zoneId) {
        this.id = id; this.zoneId = zoneId;
        this.statut = StatutCapteur.ACTIVE; this.historiqueReleves = new ArrayList<>();
        this.gestionnaire = GestionnaireCapteursAlertes.getInstance();
    }

    public void setStatut(StatutCapteur statut) {this.statut=statut;}
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
    private static final long serialVersionUID = 1L;
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
    private static final long serialVersionUID = 1L;
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
    private static final long serialVersionUID = 1L;
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
    private static final long serialVersionUID = 1L;
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
    private static final long serialVersionUID = 1L;
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
    private static final long serialVersionUID = 1L;
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
    public boolean estHorsLimites(GeographicalLimits limits) {
        return latitude < limits.getLatMin() || latitude > limits.getLatMax() || longitude < limits.getLonMin() || longitude > limits.getLonMax();
    }
    public double getLatitude()  { return latitude; }
    public double getLongitude() { return longitude; }
    public List<double[]> getHistoriquePositions() { return Collections.unmodifiableList(historiquePositions); }
    @Override public String getTypeNom() { return "GPS"; }
}