package Smart_Farm;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

class Client implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String adresse;
    private final LocalDate dateCreation;

    public Client(String nom, String prenom, String email, String telephone, String adresse) {
        this.id          = "CLI-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        this.nom         = nom;
        this.prenom      = prenom;
        this.email       = email;
        this.telephone   = telephone;
        this.adresse     = adresse;
        this.dateCreation = LocalDate.now();
    }

    // ── getters ──────────────────────────────────────────────────────────
    public String    getId()           { return id; }
    public String    getNom()          { return nom; }
    public String    getPrenom()       { return prenom; }
    public String    getEmail()        { return email; }
    public String    getTelephone()    { return telephone; }
    public String    getAdresse()      { return adresse; }
    public LocalDate getDateCreation() { return dateCreation; }
    public String    getNomComplet()   { return nom + " " + prenom; }

    // ── setters ──────────────────────────────────────────────────────────
    public void setNom(String nom)             { this.nom = nom; }
    public void setPrenom(String prenom)       { this.prenom = prenom; }
    public void setEmail(String email)         { this.email = email; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public void setAdresse(String adresse)     { this.adresse = adresse; }

    @Override
    public String toString() { return getNomComplet() + " (" + id + ")"; }
}
