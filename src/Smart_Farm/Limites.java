package Smart_Farm;

import java.io.Serializable;

// ==================== SEUIL ====================
class Seuil implements Serializable {
    private static final long serialVersionUID = 1L;
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


// ==================== LIMITES GEOGRAPHIQUES ====================

class GeographicalLimits implements Serializable {
    private static final long serialVersionUID = 1L;
    private String description;
    private double latMin;
    private double latMax;
    private double lonMin;
    private double lonMax;

    public GeographicalLimits(String description, double latMin, double latMax, double lonMin, double lonMax) {
        this.description = description;
        this.latMin = latMin;
        this.latMax = latMax;
        this.lonMin = lonMin;
        this.lonMax = lonMax;
    }

    public boolean estHorsLimites(double lat, double lon) {
        return lat < latMin || lat > latMax || lon < lonMin || lon > lonMax;
    }

    public String getDescription() { return description; }
    public double getLatMin() { return latMin; }
    public double getLatMax() { return latMax; }
    public double getLonMin() { return lonMin; }
    public double getLonMax() { return lonMax; }
}