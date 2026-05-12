package Smart_Farm;
// ==================== RELEVE ====================


import java.time.LocalDateTime;

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