package Smart_Farm;

// ==================== PROGRAMME ALIMENTATION ====================

import java.io.Serializable;

class ProgAlimentation implements Serializable {
    private String typeAliment;
    private double quantite;
    private String description;

    public ProgAlimentation(String typeAliment, double quantite) {
        this.typeAliment = typeAliment;
        this.quantite = quantite;
    }

    public String getTypeAliment() { return typeAliment; }
    public double getQuantite() { return quantite; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public void setQuantite(double quantite) { this.quantite = quantite; }
    public void setTypeAliment(String typeAliment) { this.typeAliment = typeAliment; }

    @Override
    public String toString() {
        return typeAliment + " : " + quantite + " kg";
    }
}