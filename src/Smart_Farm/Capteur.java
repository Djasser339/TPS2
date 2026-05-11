package Smart_Farm;

// ==================== CAPTEUR (ABSTRACT) ====================

import Smart_Farm.StatutCapteur;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;


// Suspendable !!

abstract class Capteur  {
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

    public void suspendre() { this.statut = StatutCapteur.SUSPENDU; }
    public void reactiver() { this.statut = StatutCapteur.ACTIVE; }

    public void ajouterReleve(Releve releve) {
        historiqueReleves.add(releve);
    }

    public List<Releve> getHistoriqueReleves() {
        return Collections.unmodifiableList(historiqueReleves);
    }

    public List<Releve> filtrerRelevesParDate(LocalDateTime debut, LocalDateTime fin) {
        return historiqueReleves.stream()
                .filter(r -> !r.getTimestamp().isBefore(debut) && !r.getTimestamp().isAfter(fin))
                .collect(Collectors.toList());
    }

    public String getId() { return id; }
    public String getZoneId() { return zoneId; }
    public StatutCapteur getStatut() { return statut; }
    public void setZoneId(String zoneId) { this.zoneId = zoneId; }

    @Override
    public String toString() {
        return "Capteur{" +
                "id='" + id + '\'' +
                ", zoneId='" + zoneId + '\'' +
                ", statut=" + statut +
                ", historiqueReleves=" + historiqueReleves +
                '}';
    }
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

    @Override
    public void envoyerReleve() {
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

    @Override
    public void envoyerReleve() {
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

    @Override
    public void envoyerReleve() {
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

    @Override
    public void envoyerReleve() {
        Random rand = new Random();
        double temperature = 37 + rand.nextDouble() * 3;
        double activite = 20 + rand.nextDouble() * 100;

        ReleveNumerique releveTemp = new ReleveNumerique(this.id, temperature, "°C", TypeMesure.TEMPERATURE_CORPORELLE);
        ReleveNumerique releveAct = new ReleveNumerique(this.id, activite, "pas/min", TypeMesure.ACTIVITE_PAS_PAR_MINUTE);

        releveTemp.setNiveau(seuilTemperature.evaluerGravite(temperature));
        releveAct.setNiveau(seuilActivite.evaluerGravite(activite));

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

    @Override
    public void envoyerReleve() {
        Random rand = new Random();
        this.latitude = 43.5 + (rand.nextDouble() - 0.5) * 0.1;
        this.longitude = 1.5 + (rand.nextDouble() - 0.5) * 0.1;
        historiquePositions.add(new double[]{latitude, longitude});

        ReleveGPS releveGPS = new ReleveGPS(this.id, latitude, longitude);
        this.ajouterReleve(releveGPS);
    }

    public boolean estHorsLimites(GeographicalLimits limites) {
        return limites.estHorsLimites(latitude, longitude);
    }

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public List<double[]> getHistoriquePositions() {
        return Collections.unmodifiableList(historiquePositions);
    }
}