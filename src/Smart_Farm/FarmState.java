package Smart_Farm;

import java.io.*;


import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;


import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.Random;

import static Smart_Farm.StadeCroissance.*;

public class FarmState {

    public static void restore(List<Culture> data) {

        cultures.clear();
        cultures.addAll(data);

        updateStats();

        if (cultureRefresh != null) {
            cultureRefresh.run();
        }
    }

    private static Runnable cultureRefresh;

    public static void setCultureRefresh(Runnable refresh) {
        cultureRefresh = refresh;
    }

    // =========================================
    // DATA SOURCE UNIQUE
    // =========================================
    private static final ObservableList<Culture> cultures =
            FXCollections.observableArrayList();

    // =========================================
    // STATS
    // =========================================
    private static final IntegerProperty nbrCultures = new SimpleIntegerProperty(0);
    private static final IntegerProperty nbrCereal = new SimpleIntegerProperty(0);
    private static final IntegerProperty nbrLegume = new SimpleIntegerProperty(0);
    private static final IntegerProperty nbrFruit = new SimpleIntegerProperty(0);

    public static ObservableList<Culture> getCultures() {
        return cultures;
    }

    public static IntegerProperty nbrCulturesProperty() { return nbrCultures; }
    public static IntegerProperty nbrCerealProperty() { return nbrCereal; }
    public static IntegerProperty nbrLegumeProperty() { return nbrLegume; }
    public static IntegerProperty nbrFruitProperty() { return nbrFruit; }

    // =========================================
    // ADD CULTURE (AUTO REFRESH)
    // =========================================
    public static void addCulture(Culture c) {

        cultures.add(c);
        updateStats();

        //  AUTO REFRESH TABLE
        if (cultureRefresh != null) {
            cultureRefresh.run();
        }
    }

    // =========================================
    // UPDATE STATS CENTRALISE
    // =========================================
    private static void updateStats() {

        nbrCultures.set(cultures.size());

        nbrCereal.set((int) cultures.stream()
                .filter(c -> c instanceof Cereal)
                .count());

        nbrLegume.set((int) cultures.stream()
                .filter(c -> c instanceof Legume)
                .count());

        nbrFruit.set((int) cultures.stream()
                .filter(c -> c instanceof Fruit)
                .count());
    }

    // =========================================
    // GROWTH LOGIC
    // =========================================
    public static void nextGrowthStage(Culture c) {

        if (c == null) return;

        switch (c.getStadeCroissance()) {

            case semis -> c.setStadeCroissance(StadeCroissance.germination);
            case germination -> c.setStadeCroissance(StadeCroissance.croissance);
            case croissance -> c.setStadeCroissance(StadeCroissance.maturite);
            case maturite -> c.setStadeCroissance(StadeCroissance.recolte);
            case recolte -> c.setStadeCroissance(StadeCroissance.semis);
        }

        //  AUTO REFRESH TABLE
        if (cultureRefresh != null) {
            cultureRefresh.run();
        }
    }

    // =========================================
    // SEARCH (CLEAN + SIMPLE)
    // =========================================
    public static List<Culture> searchCultureByType(String query) {

        if (query == null || query.isEmpty()) {
            return cultures;
        }

        String q = query.toLowerCase();

        return cultures.stream()
                .filter(c ->
                        c.getClass().getSimpleName().toLowerCase().contains(q)
                )
                .collect(Collectors.toList());
    }

    // =========================================
    // TABLE MAPPER (IMPORTANT CLEAN FIX)
    // =========================================
    public static List<String> mapCulture(Culture c) {
        return List.of(
                String.valueOf(c.getFamille()),
                String.valueOf(c.getStadeCroissance()),
                String.valueOf(c.getDatePlantation()),
                String.valueOf(c.getDateRecolte()),
                (int)c.getExigencePH().getMin() + " – " + (int)c.getExigencePH().getMax(),
                (int)c.getExigenceHumidite().getMin() + "% – " + (int)c.getExigenceHumidite().getMax() + "%"
        );
    }
}



class AnimalState {

    public static void restore(List<Animal> data) {

        animals.clear();
        animals.addAll(data);

        nbrAnimals.set(animals.size());
        nbrRuminants.set((int) animals.stream().filter(a -> a instanceof Ruminant).count());
        nbrVolaille.set((int) animals.stream().filter(a -> a instanceof Volaille).count());
        nbrAquacole.set((int) animals.stream().filter(a -> a instanceof Aquacole).count());

        refreshAnimals();
    }

    // =========================
    // LISTE PRINCIPALE
    // =========================
    private static final List<Animal> animals = new ArrayList<>();

    // =========================
    // STATS GLOBAL
    // =========================
    private static final IntegerProperty nbrAnimals = new SimpleIntegerProperty(0);

    private static final IntegerProperty nbrRuminants = new SimpleIntegerProperty(0);
    private static final IntegerProperty nbrVolaille = new SimpleIntegerProperty(0);
    private static final IntegerProperty nbrAquacole = new SimpleIntegerProperty(0);

    // =========================
    // REFRESH TABLE (LIKE CULTURE)
    // =========================
    private static Runnable animalRefresh;

    public static void setAnimalRefresh(Runnable refresh) {
        animalRefresh = refresh;
    }

    public static void refreshAnimals() {
        if (animalRefresh != null) {
            animalRefresh.run();
        }
    }

    // =========================
    // PROPERTIES (UI BIND)
    // =========================
    public static IntegerProperty nbrAnimalsProperty() {
        return nbrAnimals;
    }

    public static IntegerProperty nbrRuminantsProperty() {
        return nbrRuminants;
    }

    public static IntegerProperty nbrVolailleProperty() {
        return nbrVolaille;
    }

    public static IntegerProperty nbrAquacoleProperty() {
        return nbrAquacole;
    }

    // =========================
    // GET LIST
    // =========================
    public static List<Animal> getAnimals() {
        return animals;
    }

    // =========================
    // ADD ANIMAL (AUTO REFRESH + STATS)
    // =========================
    public static void addAnimal(Animal a) {

        animals.add(a);

        nbrAnimals.set(animals.size());

        if (a instanceof Ruminant) {
            nbrRuminants.set(nbrRuminants.get() + 1);
        }
        else if (a instanceof Volaille) {
            nbrVolaille.set(nbrVolaille.get() + 1);
        }
        else if (a instanceof Aquacole) {
            nbrAquacole.set(nbrAquacole.get() + 1);
        }

        // AUTO REFRESH TABLE
        refreshAnimals();
    }

    // =========================
    // SEARCH (TABLE FILTER)
    // =========================
    public static List<Animal> searchAnimal(String query) {

        return animals.stream()
                .filter(a -> query == null || query.isEmpty()
                        || a.getNom().toLowerCase().contains(query.toLowerCase())
                        || a.getEspece().name().toLowerCase().contains(query.toLowerCase())
                )
                .collect(Collectors.toList());
    }

    // =========================
    // MAP (TABLE DISPLAY)
    // =========================
    public static List<String> mapAnimal(Animal a) {

        return List.of(
                String.valueOf(a.getId()),
                a.getNom(),
                a.getEspece().name(),
                String.valueOf(a.getAge()),
                String.valueOf(a.getPoid())
        );
    }
}



class ZoneState {

    public static void restore(List<Zone> data) {

        zones.clear();

        // IMPORTANT
        timelines.values().forEach(Timeline::stop);
        timelines.clear();

        zones.addAll(data);

        // recalcul des stats
        nbrZones.set(zones.size());

        nbrZoneCulture.set(
                (int) zones.stream()
                        .filter(z -> z instanceof ZoneCulture)
                        .count()
        );

        nbrZoneElevage.set(
                (int) zones.stream()
                        .filter(z -> z instanceof ZoneElevage)
                        .count()
        );

        nbrZoneAquacole.set(
                (int) zones.stream()
                        .filter(z -> z instanceof ZoneAquacole)
                        .count()
        );

        // REDÉMARRER LES PRODUCTIONS
        for (Zone z : zones) {
            startProductionSimulation(z);
        }

        // refresh UI
        notifyRefresh();
    }



    // =========================
    // STORAGE
    // =========================
    private static final List<Zone> zones = new ArrayList<>();

    // =========================
    // COUNTERS (UI BIND)
    // =========================
    private static final IntegerProperty nbrZones =
            new SimpleIntegerProperty(0);

    private static final IntegerProperty nbrZoneCulture =
            new SimpleIntegerProperty(0);

    private static final IntegerProperty nbrZoneElevage =
            new SimpleIntegerProperty(0);

    private static final IntegerProperty nbrZoneAquacole =
            new SimpleIntegerProperty(0);

    // Incrémenté à chaque appel de refresh() pour notifier les charts même
    // quand le nombre de zones ne change pas (ex : affectation d'entités)
    private static final IntegerProperty zoneEntityRevision =
            new SimpleIntegerProperty(0);

    // Incrémenté à chaque tick de production (toutes les 60 s par zone)
    private static final IntegerProperty productionRevision =
            new SimpleIntegerProperty(0);

    // =========================
    // REFRESH HOOK
    // =========================
    private static Runnable zoneRefresh;
    private static Runnable productionRefresh;
    private static Runnable mapRefresh;

    public static void setZoneRefresh(Runnable r) {
        zoneRefresh = r;
    }

    public static void setMapRefresh(Runnable r) {
        mapRefresh = r;
    }

    public static void refreshMap() {
        if (mapRefresh != null) Platform.runLater(mapRefresh);
    }

    public static void refreshZones() {
        if (zoneRefresh != null) zoneRefresh.run();
    }

    public static void refresh() {
        nbrZones.set(zones.size());
        zoneEntityRevision.set(zoneEntityRevision.get() + 1);
    }


    public static void notifyRefresh() {
        if (zoneRefresh != null) zoneRefresh.run();
        refreshMap();
    }

    // =========================
    // PRODUCTION REFRESH
    // =========================

    public static void setProductionRefresh(Runnable r) {
        productionRefresh = r;
    }

    public static void refreshProduction() {
        productionRevision.set(productionRevision.get() + 1);
        if (productionRefresh != null) {
            Platform.runLater(productionRefresh);
        }
    }

    public static IntegerProperty productionRevisionProperty() { return productionRevision; }

    // =========================
    // PROPERTIES
    // =========================
    public static IntegerProperty nbrZonesProperty()        { return nbrZones; }
    public static IntegerProperty nbrZoneCultureProperty()  { return nbrZoneCulture; }
    public static IntegerProperty nbrZoneElevageProperty()  { return nbrZoneElevage; }
    public static IntegerProperty nbrZoneAquacoleProperty() { return nbrZoneAquacole; }
    public static IntegerProperty zoneEntityRevisionProperty() { return zoneEntityRevision; }

    // =========================
    // ADD ZONE
    // =========================
    public static void addZone(Zone z) {

        zones.add(z);

        nbrZones.set(zones.size());

        if (z instanceof ZoneCulture) {
            nbrZoneCulture.set(nbrZoneCulture.get() + 1);
        }
        else if (z instanceof ZoneElevage) {
            nbrZoneElevage.set(nbrZoneElevage.get() + 1);
        }
        else if (z instanceof ZoneAquacole) {
            nbrZoneAquacole.set(nbrZoneAquacole.get() + 1);
        }

        refreshZones();

        startProductionSimulation(z);
    }

    // =========================
    // GET ALL
    // =========================
    public static List<Zone> getZones() {
        return zones;
    }

    // =========================
    // SEARCH (GENERAL)
    // =========================
    public static List<Zone> searchZone(String query) {

        if (query == null || query.isEmpty()) {
            return zones;
        }

        String q = query.toLowerCase();

        return zones.stream()
                .filter(z ->
                        String.valueOf(z.getCode()).contains(q) ||
                                z.getNom().toLowerCase().contains(q) ||
                                z.getType().toString().toLowerCase().contains(q) ||
                                z.getStatut().toString().toLowerCase().contains(q)
                )
                .collect(Collectors.toList());
    }

    // =========================
    // FILTER BY TYPE (OPTIONAL)
    // =========================
    public static List<Zone> filterZone(Predicate<Zone> predicate) {
        return zones.stream().filter(predicate).collect(Collectors.toList());
    }

    // =========================
    // MAPPER (TABLE UI)
    // =========================
    public static List<String> mapZone(Zone z) {

        return List.of(
                String.valueOf(z.getCode()),
                z.getNom(),
                z.getType().toString(),
                z.getStatut().toString(),
                String.valueOf(z.getNbrEntite())
        );
    }

    // =========================
    // CLEAR (OPTION DEBUG)
    // =========================
    public static void clear() {
        zones.clear();

        nbrZones.set(0);
        nbrZoneCulture.set(0);
        nbrZoneElevage.set(0);
        nbrZoneAquacole.set(0);

        refreshZones();
    }

    public static int getZoneSize(Zone z) {
        return z.getNbrEntite();
    }

    public static void setZoneStatut(Zone z, StatutZone nouveauStatut) {

        if (z == null) return;

        z.setStatut(nouveauStatut);

        // propagation automatique vers capteurs
        if (nouveauStatut == StatutZone.SUSPENDU) {
            CapteurState.suspendAllFromZone(z);
        }
        else if (nouveauStatut == StatutZone.ACTIVE) {
            CapteurState.activateAllFromZone(z);
        }

        notifyRefresh();
        CapteurState.refreshCapteurs();
    }

// =========================
// RANDOM + CONFIG
// =========================

    private static final Random random = new Random();

    /*
     *  MODIFIER ICI : intervale de production (secondes)
     */
    private static final int PRODUCTION_INTERVAL = 60;

    // évite double timeline par zone
    private static final Set<Zone> runningZones = new HashSet<>();

    // refresh UI hook


// =========================
// SIMULATION PRODUCTION
// =========================
    private static final Map<Zone, Timeline> timelines = new HashMap<>();

    public static void startProductionSimulation(Zone zone) {

        if (zone == null) return;

        // si déjà une timeline -> stop ancien
        if (timelines.containsKey(zone)) {
            timelines.get(zone).stop();
        }

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(PRODUCTION_INTERVAL), e -> {

                    // 🔥 CHECK STATUT À CHAQUE TICK
                    if (zone.getStatut() != StatutZone.ACTIVE) {
                        return; // ou ignore production
                    }

                    if (zone.getNbrEntite() <= 0) return;

                    double t = System.currentTimeMillis() / 1000.0;
                    double variation = 0.8 + Math.abs(Math.sin(t / 20));

                    double rendement =
                            zone.getNbrEntite()
                                    * (5 + random.nextDouble() * 10)
                                    * variation;

                    rendement = Math.round(rendement * 10.0) / 10.0;

                    String typeProduction;

                    if (zone instanceof ZoneCulture) {
                        typeProduction = "kg cultures";
                    }
                    else if (zone instanceof ZoneElevage ze) {
                        typeProduction = (ze.getTypeZoneElevage() == TypeZoneElevage.Ruminant)
                                ? "litres lait"
                                : "unité oeufs";
                    }
                    else {
                        typeProduction = "kg aquacole";
                    }

                    System.out.println(
                            "PRODUCTION -> "
                                    + zone.getNom()
                                    + " | "
                                    + rendement
                                    + " "
                                    + typeProduction
                    );

                    zone.enregistrerProduction(rendement, typeProduction);

                    refreshProduction();
                })
        );

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        timelines.put(zone, timeline);
    }

// =========================
// ROW WRAPPER
// =========================

    public static class ProductionRow {

        private final Zone zone;
        private final EnregistrementProduction production;

        public ProductionRow(Zone zone, EnregistrementProduction production) {
            this.zone = zone;
            this.production = production;
        }

        public Zone getZone() {
            return zone;
        }

        public EnregistrementProduction getProduction() {
            return production;
        }
    }

// =========================
// SEARCH PRODUCTION
// =========================

    public static List<ProductionRow> searchProduction(String query) {

        List<ProductionRow> rows = new ArrayList<>();

        for (Zone z : zones) {

            if (z.getProductions() == null) continue;

            for (EnregistrementProduction p : z.getProductions()) {
                rows.add(new ProductionRow(z, p));
            }
        }

        if (query == null || query.isBlank()) {
            return rows;
        }

        String q = query.toLowerCase();

        return rows.stream()
                .filter(r ->
                        r.getZone().getNom().toLowerCase().contains(q)
                                ||
                                r.getZone().getType().toString().toLowerCase().contains(q)
                )
                .collect(java.util.stream.Collectors.toList());
    }

// =========================
// MAP PRODUCTION (TABLE UI)
// =========================

    public static List<String> mapProduction(ProductionRow row) {

        Zone z = row.getZone();
        EnregistrementProduction p = row.getProduction();

        String type = p.getTypeProduction();

        String unite = "";
        String description = "";

        if (type != null) {

            if (type.contains("kg cultures")) {
                unite = "kg";
                description = "cultures";
            }
            else if (type.contains("litres lait")) {
                unite = "litres";
                description = "lait";
            }
            else if (type.contains("unité oeufs")) {
                unite = "unité";
                description = "oeufs";
            }
            else if (type.contains("kg aquacole")) {
                unite = "kg";
                description = "aquacole";
            }
        }

        return List.of(
                z.getNom(),
                z.getType().toString(),
                p.getDate().toString(),
                description,

                // ===== VALEUR =====
                String.valueOf(p.getQuantite()),

                // ===== UNITÉ =====
                unite
        );
    }

    public static List<Zone> searchCultureZones(String query) {

        if (query == null || query.isEmpty()) {
            return zones;
        }

        String q = query.toLowerCase();

        return zones.stream()
                .filter(z ->
                        z.getType().toString().toLowerCase().contains(q)
                                || z.getNom().toLowerCase().contains(q)
                )
                .collect(Collectors.toList());
    }

    // =========================================
// ZONES ALIMENTATION
// uniquement Elevage + Aquacole
// =========================================

    public static List<Zone> getZonesAlimentation() {

        return zones.stream()

                .filter(z ->

                        z instanceof ZoneElevage
                                ||
                                z instanceof ZoneAquacole
                )

                .collect(Collectors.toList());
    }

// =========================================
// SEARCH ALIMENTATION ZONES
// =========================================

    private static Runnable alimentationRefresh;

    // =========================
    // GET ALIMENTATION ZONES
    // =========================
    public static List<Zone> getAlimentationZones() {
        if (zones == null) return List.of();

        return zones.stream()
                .filter(z -> z instanceof ZoneElevage
                        || z instanceof ZoneAquacole)
                .toList();
    }

    // =========================
    // SEARCH
    // =========================
    public static List<Zone> searchAlimentationZones(String query) {

        List<Zone> base = getAlimentationZones();

        if (query == null || query.isBlank()) {
            return base;
        }

        String q = query.toLowerCase();

        return base.stream()
                .filter(z ->
                        z.getNom().toLowerCase().contains(q)
                                || z.getType().toString().toLowerCase().contains(q)
                )
                .toList();
    }

    // =========================
    // REFRESH HANDLER
    // =========================
    public static void setAlimentationRefresh(Runnable r) {
        alimentationRefresh = r;
    }

    public static void refreshAlimentation() {
        if (alimentationRefresh != null) {
            alimentationRefresh.run();
        }
    }





}

class CapteurState {

    public static void restore(List<Capteur> data) {

        capteurs.clear();
        capteurs.addAll(data);

        updateStats();

        // important : réenregistrer dans gestionnaire alertes
        GestionnaireCapteursAlertes instance = GestionnaireCapteursAlertes.getInstance();
        for (Capteur c : capteurs) {
            instance.ajouterCapteur(c);
        }

        refreshCapteurs();
    }

    private static final List<Capteur> capteurs = new ArrayList<>();

    private static final IntegerProperty nbrCapteurs    = new SimpleIntegerProperty(0);
    private static final IntegerProperty nbrActifs      = new SimpleIntegerProperty(0);
    private static final IntegerProperty nbrSuspendus   = new SimpleIntegerProperty(0);
    private static final IntegerProperty nbrDefaillants = new SimpleIntegerProperty(0);

    private static Runnable capteurRefresh;

    public static void setCapteurRefresh(Runnable r) {
        capteurRefresh = r;
    }

    public static void refreshCapteurs() {
        if (capteurRefresh != null) capteurRefresh.run();
    }

    public static IntegerProperty nbrCapteursProperty()    { return nbrCapteurs; }
    public static IntegerProperty nbrActifsProperty()      { return nbrActifs; }
    public static IntegerProperty nbrSuspendusProp()       { return nbrSuspendus; }
    public static IntegerProperty nbrDefaillantsProperty() { return nbrDefaillants; }

    public static List<Capteur> getCapteurs() { return capteurs; }

    public static void addCapteur(Capteur c) {
        capteurs.add(c);
        GestionnaireCapteursAlertes.getInstance().ajouterCapteur(c);
        updateStats();
        refreshCapteurs();
    }

    public static void updateStats() {
        nbrCapteurs.set(capteurs.size());
        nbrActifs.set((int) capteurs.stream()
                .filter(c -> c.getStatut() == StatutCapteur.ACTIVE).count());
        nbrSuspendus.set((int) capteurs.stream()
                .filter(c -> c.getStatut() == StatutCapteur.SUSPENDU).count());
        nbrDefaillants.set((int) capteurs.stream()
                .filter(c -> c.getStatut() == StatutCapteur.INACTIVE).count());
    }

    public static List<Capteur> searchCapteur(String query) {
        if (query == null || query.isEmpty()) return capteurs;

        String q = query.toLowerCase();

        return capteurs.stream()
                .filter(c -> c.getId().toLowerCase().contains(q)
                        || c.getTypeNom().toLowerCase().contains(q)
                        || c.getZoneId().toLowerCase().contains(q)
                        || c.getStatut().name().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    public static List<String> mapCapteur(Capteur c) {

        List<Releve> hist = c.getHistoriqueReleves();

        String dernierReleve = hist.isEmpty()
                ? "—"
                : hist.get(hist.size() - 1).getValeurAsString();

        String niveau = hist.isEmpty()
                ? "—"
                : hist.get(hist.size() - 1).getNiveau().name();

        String statut = switch (c.getStatut()) {
            case ACTIVE   -> "Actif";
            case SUSPENDU -> "Suspendu";
            case INACTIVE -> "Défaillant";
        };

        return List.of(
                c.getId(),
                c.getTypeNom(),
                c.getZoneId(),
                statut,
                dernierReleve,
                niveau
        );
    }

    // =========================
    // SUSPENDRE
    // =========================
    public static void suspendAllFromZone(Zone z) {

        String zoneName = z.getNom(); //  clé commune avec capteur

        for (Capteur c : capteurs) {

            if (c.getZoneId().equalsIgnoreCase(zoneName)) {
                c.setStatut(StatutCapteur.SUSPENDU);
            }
        }

        updateStats();
        refreshCapteurs();
    }

    // =========================
    // ACTIVER
    // =========================
    public static void activateAllFromZone(Zone z) {

        String zoneName = z.getNom();

        for (Capteur c : capteurs) {

            if (c.getZoneId().equalsIgnoreCase(zoneName)) {
                c.setStatut(StatutCapteur.ACTIVE);
            }
        }

        updateStats();
        refreshCapteurs();
    }
}

class AlerteState {

    public static void restore(List<Alerte> data) {

        alertes.clear();
        alertes.addAll(data);

        updateStats();
        refreshAlertes();
    }

    private static final List<Alerte> alertes = new ArrayList<>();


    private static final IntegerProperty nbrAlertes        = new SimpleIntegerProperty(0);
    private static final IntegerProperty nbrCritiques      = new SimpleIntegerProperty(0);
    private static final IntegerProperty nbrAvertissements = new SimpleIntegerProperty(0);
    private static final IntegerProperty nbrSupprimees     = new SimpleIntegerProperty(0);
    private static final IntegerProperty nbrAcquittees     = new SimpleIntegerProperty(0);

    private static Runnable alerteRefresh;
    private static Runnable historyRefresh;

    public static void setAlerteRefresh(Runnable r) { alerteRefresh = r; }
    public static void refreshAlertes() { if (alerteRefresh != null) alerteRefresh.run(); }

    public static void setHistoryRefresh(Runnable r) { historyRefresh = r; }
    public static void refreshHistory() { if (historyRefresh != null) Platform.runLater(historyRefresh); }

    public static IntegerProperty nbrAlertesProperty()        { return nbrAlertes; }
    public static IntegerProperty nbrCritiquesProperty()      { return nbrCritiques; }
    public static IntegerProperty nbrAvertissementsProperty() { return nbrAvertissements; }
    public static IntegerProperty nbrSupprimeesProp()         { return nbrSupprimees; }
    public static IntegerProperty nbrAcquitteesProperty()     { return nbrAcquittees; }

    public static List<Alerte> getAlertesActives() {
        return GestionnaireCapteursAlertes.getInstance()
                .filtrerAlertes(null, null, null, null, null)
                .stream()
                .filter(a -> !a.isAcquittee() && !a.isSupprimee())
                .sorted((a1, a2) -> a2.getNiveau().compareTo(a1.getNiveau()))
                .collect(Collectors.toList());
    }

    public static void updateStats() {
        List<Alerte> actives = getAlertesActives();
        nbrAlertes.set(actives.size());
        nbrCritiques.set((int) actives.stream()
                .filter(a -> a.getNiveau() == Gravite.critique).count());
        nbrAvertissements.set((int) actives.stream()
                .filter(a -> a.getNiveau() == Gravite.avertissement).count());

        List<Alerte> all = GestionnaireCapteursAlertes.getInstance()
                .filtrerAlertes(null, null, null, null, null);
        nbrSupprimees.set((int) all.stream().filter(Alerte::isSupprimee).count());
        nbrAcquittees.set((int) all.stream().filter(Alerte::isAcquittee).count());

        refreshHistory();
    }

    public static List<String> mapAlerte(Alerte a) {
        GestionnaireCapteursAlertes g = GestionnaireCapteursAlertes.getInstance();
        Capteur c = g.getCapteurById(a.getReleve().getIdCapteur());
        String zone = (c != null) ? c.getZoneId() : "?";
        String type = (c != null) ? c.getTypeNom() : "?";
        return List.of(
                String.valueOf(a.getId()),
                zone,
                type,
                a.getNiveau().name(),
                a.getDateCreation().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                a.getReleve().getValeurAsString(),
                a.isAcquittee() ? "Acquittée" : "Active"
        );
    }
}




class SmartFarmPersistence {

    private static final String FILE = "smartfarm.dat";

    // =========================
    // SAVE
    // =========================
    public static void save() {
        try (ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(FILE))) {

            out.writeObject(ZoneState.getZones());
            out.writeObject(CapteurState.getCapteurs());
            out.writeObject(FarmState.getCultures());
            out.writeObject(AnimalState.getAnimals());
            out.writeObject(AlerteState.getAlertesActives());
            // Commercial data — written last for backward compatibility
            out.writeObject(CommercialState.getClientsForSave());
            out.writeObject(CommercialState.getVentesForSave());

            System.out.println("✔ SAVE OK");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================
    // LOAD
    // =========================
    public static void load() {
        File f = new File(FILE);
        if (!f.exists()) {
            System.out.println("⚠ Aucun fichier de sauvegarde");
            return;
        }

        try (ObjectInputStream in = new ObjectInputStream(
                new FileInputStream(f))) {

            List<Zone> zones = (List<Zone>) in.readObject();
            List<Capteur> capteurs = (List<Capteur>) in.readObject();
            List<Culture> cultures = (List<Culture>) in.readObject();
            List<Animal> animals = (List<Animal>) in.readObject();
            List<Alerte> alertes = (List<Alerte>) in.readObject();

            // =========================
            // RESTORE ORDER IMPORTANT
            // =========================
            ZoneState.restore(zones);
            CapteurState.restore(capteurs);
            FarmState.restore(cultures);
            AnimalState.restore(animals);
            AlerteState.restore(alertes);

            // Commercial data — old save files silently skip this block
            try {
                List<Client> clients = (List<Client>) in.readObject();
                List<Vente>  ventes  = (List<Vente>)  in.readObject();
                CommercialState.restore(clients, ventes);
            } catch (Exception ignored) {
                // Pre-commercial save file — start with empty commercial data
            }

            System.out.println("✔ LOAD OK");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================
    // START APP
    // =========================
    public static void init() {
        load();
    }

    // =========================
    // STOP APP
    // =========================
    public static void shutdown() {
        save();
    }
}