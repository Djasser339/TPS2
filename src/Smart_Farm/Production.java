package Smart_Farm;// ==================== HISTORIQUE PRODUCTION ====================

import java.time.LocalDate;

class HistoriqueProd {
    private LocalDate date;
    private double quantite;
    private String unite;
    private String description;

    public HistoriqueProd(double quantite, String unite, String description) {
        this.date = LocalDate.now();
        this.quantite = quantite;
        this.unite = unite;
        this.description = description;
    }

    public LocalDate getDate() { return date; }
    public double getQuantite() { return quantite; }
    public String getUnite() { return unite; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return "[" + date + "] " + description + " : " + quantite + " " + unite;
    }
}

// ==================== ENREGISTREMENT PRODUCTION ====================

class EnregistrementProduction {
    private LocalDate date;
    private double quantite;
    private String typeProduction;

    public EnregistrementProduction(double quantite, String typeProduction) {
        this.date = LocalDate.now();
        this.quantite = quantite;
        this.typeProduction = typeProduction;
    }

    public LocalDate getDate() {
        return date;
    }

    public double getQuantite() {
        return quantite;
    }

    public String getTypeProduction() {
        return typeProduction;
    }

    @Override
    public String toString() {
        return "[" + date + "] " + typeProduction + " : " + quantite;
    }

}