package Smart_Farm;


// ==================== ALERTE ====================

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

// ==================== ALERTE ====================
class Alerte {
    private static long compteur = 0;
    private final long id;
    private final Releve releve;
    private final Gravite niveau;
    private final LocalDateTime dateCreation;
    private boolean acquittee, supprimee;
    public Alerte(Releve releve, Gravite niveau) {
        this.id = ++compteur; this.releve = releve; this.niveau = niveau;
        this.dateCreation = LocalDateTime.now(); this.acquittee = false; this.supprimee = false;
    }
    public void acquitter()              { this.acquittee = true; }
    public void supprimer()              { this.supprimee = true; }
    public long getId()                  { return id; }
    public Releve getReleve()            { return releve; }
    public Gravite getNiveau()           { return niveau; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public boolean isAcquittee()         { return acquittee; }
    public boolean isSupprimee()         { return supprimee; }
}

// ==================== GESTIONNAIRE GLOBAL ====================
class GestionnaireCapteursAlertes {
    private static GestionnaireCapteursAlertes instance;
    private List<Capteur> tousLesCapteurs = new ArrayList<>();
    private Map<String, List<Capteur>> capteursParZone = new HashMap<>();
    private Map<String, Capteur>       capteursParId   = new HashMap<>();
    private List<Alerte>               alertes         = new ArrayList<>();

    private GestionnaireCapteursAlertes() {}
    public static GestionnaireCapteursAlertes getInstance() {
        if (instance == null) instance = new GestionnaireCapteursAlertes();
        return instance;
    }

    public void ajouterCapteur(Capteur c) {
        tousLesCapteurs.add(c);
        capteursParZone.computeIfAbsent(c.getZoneId(), k -> new ArrayList<>()).add(c);
        capteursParId.put(c.getId(), c);
    }

    // MÉTHODE AJOUTÉE — accès par ID
    public Capteur getCapteurById(String id) { return capteursParId.get(id); }

    // MÉTHODE AJOUTÉE — liste complète
    public List<Capteur> getTousCapteurs() { return Collections.unmodifiableList(tousLesCapteurs); }

    // MÉTHODE AJOUTÉE — liaison avec la Zone du binôme
    public void suspendreZone(String zoneId) {
        for (Capteur c : capteursParZone.getOrDefault(zoneId, Collections.emptyList())) c.suspendre();
    }
    public void reactiverZone(String zoneId) {
        for (Capteur c : capteursParZone.getOrDefault(zoneId, Collections.emptyList())) c.reactiver();
    }

    public void declencherAlerte(Releve releve, Gravite niveau) {
        if (niveau != Gravite.normal) alertes.add(new Alerte(releve, niveau));
    }
    public List<Capteur> getCapteursParZone(String zoneId) {
        return capteursParZone.getOrDefault(zoneId, Collections.emptyList());
    }

    public String afficherTableauBordZone(String zoneId) {
        List<Capteur> capteurs = getCapteursParZone(zoneId);
        if (capteurs.isEmpty()) return "Aucun capteur dans la zone " + zoneId;
        StringBuilder sb = new StringBuilder("\n--- TABLEAU DE BORD - ZONE " + zoneId + " ---\n");
        for (Capteur c : capteurs) {
            List<Releve> hist = c.getHistoriqueReleves();
            if (hist.isEmpty()) { sb.append("Capteur ").append(c.getId()).append(" : pas de relevé\n"); continue; }
            Releve dernier = hist.get(hist.size() - 1);
            String niveauStr, couleur;
            switch (dernier.getNiveau()) {
                case normal:        niveauStr = "NORMAL";        couleur = "\u001B[32m"; break;
                case avertissement: niveauStr = "AVERTISSEMENT"; couleur = "\u001B[33m"; break;
                case critique:      niveauStr = "CRITIQUE";      couleur = "\u001B[31m"; break;
                default:            niveauStr = "INCONNU";       couleur = "";
            }
            sb.append("Capteur ").append(c.getId()).append(" (").append(c.getTypeNom()).append(") : ")
                    .append(couleur).append(niveauStr).append("\u001B[0m")
                    .append(" - Valeur : ").append(dernier.getValeurAsString()).append("\n");
        }
        return sb.toString();
    }

    public String afficherEvolutionReleves(String idCapteur) {
        Capteur capteur = capteursParId.get(idCapteur);
        if (capteur == null) return "Capteur inconnu : " + idCapteur;
        List<Releve> hist = capteur.getHistoriqueReleves();
        if (hist.isEmpty()) return "Aucun relevé pour ce capteur.";
        StringBuilder sb = new StringBuilder("\n=== ÉVOLUTION – CAPTEUR " + idCapteur + " ===\n");
        sb.append(String.format("%-25s %-18s %-12s%n", "Date", "Valeur", "Niveau"));
        sb.append("─".repeat(56)).append("\n");
        for (Releve r : hist)
            sb.append(String.format("%-25s %-18s %-12s%n", r.getTimestamp(), r.getValeurAsString(), r.getNiveau()));
        return sb.toString();
    }

    public String afficherEvolutionRelevesZone(String zoneId) {
        List<Capteur> capteurs = getCapteursParZone(zoneId);
        if (capteurs.isEmpty()) return "Aucun capteur dans la zone " + zoneId;
        StringBuilder sb = new StringBuilder("\n=== ÉVOLUTION – ZONE " + zoneId + " ===\n");
        for (Capteur c : capteurs) sb.append(afficherEvolutionReleves(c.getId())).append("\n");
        return sb.toString();
    }

    public String afficherAlertesActives() {
        List<Alerte> actives = new ArrayList<>();
        for (Alerte a : alertes) if (!a.isAcquittee() && !a.isSupprimee()) actives.add(a);
        actives.sort((a1, a2) -> a2.getNiveau().compareTo(a1.getNiveau()));
        if (actives.isEmpty()) return "Aucune alerte active.";
        StringBuilder sb = new StringBuilder("\n=== ALERTES ACTIVES ===\n");
        for (Alerte a : actives)
            sb.append("ID: ").append(a.getId()).append(" | Niveau: ").append(a.getNiveau())
                    .append(" | Capteur: ").append(a.getReleve().getIdCapteur())
                    .append(" | Date: ").append(a.getDateCreation())
                    .append(" | Valeur: ").append(a.getReleve().getValeurAsString()).append("\n");
        return sb.toString();
    }

    public boolean acquitterAlerte(long id) {
        for (Alerte a : alertes) if (a.getId() == id && !a.isSupprimee()) { a.acquitter(); return true; }
        return false;
    }
    public boolean supprimerAlerte(long id) {
        for (Alerte a : alertes) if (a.getId() == id) { a.supprimer(); return true; }
        return false;
    }

    public List<Alerte> filtrerAlertes(String zoneId, TypeMesure typeCapteur,
                                       Gravite niveau, LocalDateTime debut, LocalDateTime fin) {
        List<Alerte> res = new ArrayList<>();
        for (Alerte a : alertes) {
            Capteur c = capteursParId.get(a.getReleve().getIdCapteur());
            if (c == null) continue;
            if (zoneId      != null && !c.getZoneId().equals(zoneId)) continue;
            if (typeCapteur != null) {
                Releve r = a.getReleve();
                if (!(r instanceof ReleveNumerique)) continue;
                if (((ReleveNumerique) r).getTypeMesure() != typeCapteur) continue;
            }
            if (niveau != null && a.getNiveau() != niveau) continue;
            if (debut  != null && a.getDateCreation().isBefore(debut)) continue;
            if (fin    != null && a.getDateCreation().isAfter(fin))    continue;
            res.add(a);
        }
        res.sort((a1, a2) -> a2.getNiveau().compareTo(a1.getNiveau()));
        return res;
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