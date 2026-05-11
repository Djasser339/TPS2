package Smart_Farm;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

// ==================== FERME ====================

class Ferme {
    private String nom;
    private List<Zone> zones = new ArrayList<>();
    private List<Alerte> alertes = new ArrayList<>();
    private List<Culture> cultures = new ArrayList<>();
    private List<Animal>  animals = new ArrayList<>();

    public Ferme(String nom) {
        this.nom = nom;
    }

    public String getNom() { return nom; }

    public List<Zone> getZones() {
        return Collections.unmodifiableList(zones);
    }

    public List<Alerte> getAlertes() {
        return Collections.unmodifiableList(alertes);
    }

    public List<Animal> getAnimals() {
        return animals;
    }

    public void ajouterZoneElevage(ZoneElevage zone) { zones.add(zone); }
    public void ajouterZoneCulture(ZoneCulture zone) { zones.add(zone); }
    public void ajouterZoneAquacole(ZoneAquacole zone) { zones.add(zone); }
    public void supprimerZone(Zone zone) { zones.remove(zone); }

    public void ajouterAlerte(Alerte a) { alertes.add(a); }
    public void ajouterCulture(Culture c) { cultures.add(c); }

    public void ajouterAnimal(Animal a) { animals.add(a); }


    public void afficherPanneauAlertes() {
        System.out.println("========== PANNEAU DES ALERTES ==========");
        alertes.stream()
                .filter(a -> !a.isSupprimee())
                .sorted((a, b) -> b.getNiveau().compareTo(a.getNiveau()))
                .forEach(System.out::println);
        System.out.println("=========================================");
    }

    public List<Alerte> filtrerAlertes(String zoneId, Gravite niveau,
                                       LocalDateTime debut, LocalDateTime fin) {
        return alertes.stream()
                .filter(a -> (zoneId == null || a.getZoneId().equals(zoneId)))
                .filter(a -> (niveau == null || a.getNiveau() == niveau))
                .filter(a -> (debut == null || !a.getDateCreation().isBefore(debut)))
                .filter(a -> (fin == null || !a.getDateCreation().isAfter(fin)))
                .collect(Collectors.toList());
    }
}

// ==================== APP ====================

class App {
    private String nom;
    private Ferme ferme;

    public App(String nom, Ferme ferme) {
        this.nom = nom;
        this.ferme = ferme;
    }

    public void desactiverZone(Zone zone) {
        zone.suspendre();
    }

    public void reactiverZone(Zone zone) {
        zone.reactiver();
    }

    public void ajouterCulture(Culture culture, ZoneCulture zone) {
        zone.ajouterCulture(culture);
    }

    public void ajouterRuminant(Ruminant ruminant, ZoneElevage zone) {
        if (zone.getTypeZoneElevage() == TypeZoneElevage.Ruminant) {
            zone.ajouterRuminant(ruminant);
        }
    }

    public void ajouterVolaille(Volaille volaille, ZoneElevage zone) {
        if (zone.getTypeZoneElevage() == TypeZoneElevage.Volaille) {
            zone.ajouterVollaile(volaille);
        }
    }

    public void ajouterAquacole(Aquacole aquacole, ZoneAquacole zone) {
        zone.ajouterAquacole(aquacole);
    }

    public void traiterReleves(Capteur capteur, Zone zone) {
        capteur.envoyerReleve();
        for (Releve r : capteur.getHistoriqueReleves()) {
            if (r.getNiveau() != Gravite.normal) {
                Alerte alerte = new Alerte(r, r.getNiveau(), String.valueOf(zone.getCode()));
                ferme.ajouterAlerte(alerte);
            }
        }
    }

    public void traiterReleveGPS(CapteurGPS capteurGPS, Animal animal, ZoneElevage zone) {
        capteurGPS.envoyerReleve();
        if (capteurGPS.estHorsLimites(zone.getLimitZone())) {
            List<Releve> releves = capteurGPS.getHistoriqueReleves();
            if (!releves.isEmpty()) {
                Releve dernier = releves.get(releves.size() - 1);
                dernier.setNiveau(Gravite.critique);

                Alerte alerte = new Alerte(dernier, Gravite.critique, String.valueOf(zone.getCode()));
                ferme.ajouterAlerte(alerte);
            }
        }
    }

    // =========================================================
    // =============== AFFICHAGE REFACTORISE ===================
    // =========================================================

    public String afficherInfosBaseZone(Zone z) {
        StringBuilder r = new StringBuilder();

        r.append("Code : ").append(z.getCode()).append("\n");
        r.append("Nom : ").append(z.getNom()).append("\n");
        r.append("Type : ").append(z.getType()).append("\n");
        r.append("Statut : ").append(z.getStatut()).append("\n");
        r.append("Suspendu : ").append(z.estSuspendu()).append("\n");
        r.append("Nombre d'entités : ").append(z.getNbrEntite()).append("\n");

        return r.toString();
    }

    public String afficherCapteursZone(Zone z) {
        StringBuilder r = new StringBuilder();

        r.append("--- Capteurs ---\n");
        for (Capteur c : z.getCapteurs()) {
            r.append("  Capteur ").append(c.getId())
                    .append(" | Statut : ").append(c.getStatut())
                    .append(" | Relevés : ").append(c.getHistoriqueReleves().size())
                    .append("\n");
        }

        return r.toString();
    }

    public String afficherProductionsZone(Zone z) {
        StringBuilder r = new StringBuilder();

        r.append("--- Productions ---\n");
        for (EnregistrementProduction ep : z.getProductions()) {
            r.append("  ").append(ep).append("\n");
        }

        return r.toString();
    }

    public String afficherZoneCulture(ZoneCulture z) {
        StringBuilder r = new StringBuilder();

        r.append("--- Cultures ---\n");
        for (Culture c : z.getCultures()) {
            r.append("  Famille : ").append(c.getFamille()).append("\n");
            r.append("  ").append(c.conditionCroissance());
            r.append("  ").append(c.afficherStats());
        }

        return r.toString();
    }

    public String afficherZoneElevage(ZoneElevage z) {
        StringBuilder r = new StringBuilder();

        r.append("--- Animaux ---\n");
        for (Animal a : z.getAnimals()) {
            r.append("  ").append(a).append("\n");
        }

        return r.toString();
    }

    public String afficherZoneAquacole(ZoneAquacole z) {
        StringBuilder r = new StringBuilder();

        r.append("--- Aquacoles ---\n");
        for (Aquacole a : z.getAquacoles()) {
            r.append("  ").append(a).append("\n");
        }

        return r.toString();
    }

    public String afficherZoneComplete(Zone z) {
        StringBuilder r = new StringBuilder();

        r.append(afficherInfosBaseZone(z));
        r.append(afficherCapteursZone(z));
        r.append(afficherProductionsZone(z));

        if (z instanceof ZoneCulture c)
            r.append(afficherZoneCulture(c));
        else if (z instanceof ZoneElevage e)
            r.append(afficherZoneElevage(e));
        else if (z instanceof ZoneAquacole a)
            r.append(afficherZoneAquacole(a));

        r.append("\n====================================\n\n");

        return r.toString();
    }

    public String afficherToutesZones() {
        StringBuilder r = new StringBuilder();

        r.append("===== ZONES DE LA FERME : ")
                .append(ferme.getNom())
                .append(" =====\n\n");

        for (Zone z : ferme.getZones()) {
            r.append(afficherZoneComplete(z));
        }

        return r.toString();
    }

    public String afficherZones() {
        return afficherToutesZones();
    }
}