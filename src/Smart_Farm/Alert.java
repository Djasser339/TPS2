package Smart_Farm;


// ==================== ALERTE ====================

import java.time.LocalDate;
import java.time.LocalDateTime;

class Alerte {
    private static long compteur = 0;
    private final long id;
    private final Releve releve;
    private final Gravite niveau;
    private final LocalDateTime dateCreation;
    private final String zoneId;
    private boolean acquittee;
    private boolean supprimee;

    public Alerte(Releve releve, Gravite niveau, String zoneId) {
        this.id = ++compteur;
        this.releve = releve;
        this.niveau = niveau;
        this.dateCreation = LocalDateTime.now();
        this.zoneId = zoneId;
        this.acquittee = false;
        this.supprimee = false;
    }

    public void acquitter() { this.acquittee = true; }
    public void supprimer() { this.supprimee = true; }

    public long getId() { return id; }
    public Releve getReleve() { return releve; }
    public Gravite getNiveau() { return niveau; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public String getZoneId() { return zoneId; }
    public boolean isAcquittee() { return acquittee; }
    public boolean isSupprimee() { return supprimee; }

    @Override
    public String toString() {
        return "[ALERTE #" + id + "] Niveau=" + niveau
                + " | Zone=" + zoneId
                + " | Capteur=" + releve.getIdCapteur()
                + " | Valeur=" + releve.getValeurAsString()
                + " | " + dateCreation.toLocalDate()
                + " | Acquittée=" + acquittee;
    }
}

// ==================== EVENEMENT SANTE ====================

class EvenementSante {
    private TypeEvenSante type;
    private LocalDate date;
    private String description;

    public EvenementSante(TypeEvenSante type, String description) {
        this.type = type;
        this.date = LocalDate.now();
        this.description = description;
    }

    public TypeEvenSante getType() { return type; }
    public LocalDate getDate() { return date; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return "[" + date + "] " + type + " : " + description;
    }
}