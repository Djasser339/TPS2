package Smart_Farm;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

class Vente implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String    id;
    private final String    clientId;       // → Client.id
    private final String    zoneNom;        // → Zone.getNom()  (production key)
    private final String    typeProduction; // → EnregistrementProduction.typeProduction
    private final double    quantite;
    private final double    prixUnitaire;
    private final LocalDate dateVente;
    private final String    notes;

    public Vente(String clientId, String zoneNom, String typeProduction,
                 double quantite, double prixUnitaire, String notes) {
        this.id             = "VNT-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        this.clientId       = clientId;
        this.zoneNom        = zoneNom;
        this.typeProduction = typeProduction;
        this.quantite       = quantite;
        this.prixUnitaire   = prixUnitaire;
        this.dateVente      = LocalDate.now();
        this.notes          = notes == null ? "" : notes;
    }

    // ── getters ──────────────────────────────────────────────────────────
    public String    getId()             { return id; }
    public String    getClientId()       { return clientId; }
    public String    getZoneNom()        { return zoneNom; }
    public String    getTypeProduction() { return typeProduction; }
    public double    getQuantite()       { return quantite; }
    public double    getPrixUnitaire()   { return prixUnitaire; }
    public LocalDate getDateVente()      { return dateVente; }
    public String    getNotes()          { return notes; }

    /** Montant total = quantite × prixUnitaire */
    public double getMontant() { return quantite * prixUnitaire; }
}
