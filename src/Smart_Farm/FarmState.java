package Smart_Farm;

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
                String.valueOf(c.getPH()),
                String.valueOf(c.getHumidite())


                );
    }
}



class AnimalState {

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

    // =========================
    // REFRESH HOOK
    // =========================
    private static Runnable zoneRefresh;
    private static Runnable productionRefresh;

    public static void setZoneRefresh(Runnable r) {
        zoneRefresh = r;
    }

    public static void refreshZones() {
        if (zoneRefresh != null) zoneRefresh.run();
    }

    public static void refresh() {
        nbrZones.set(zones.size());
    }


    public static void notifyRefresh() {
        if (zoneRefresh != null) zoneRefresh.run();
    }

    // =========================
    // PRODUCTION REFRESH
    // =========================

    public static void setProductionRefresh(Runnable r) {
        productionRefresh = r;
    }

    public static void refreshProduction() {
        if (productionRefresh != null) {
            Platform.runLater(productionRefresh);
        }
    }

    // =========================
    // PROPERTIES
    // =========================
    public static IntegerProperty nbrZonesProperty() {
        return nbrZones;
    }

    public static IntegerProperty nbrZoneCultureProperty() {
        return nbrZoneCulture;
    }

    public static IntegerProperty nbrZoneElevageProperty() {
        return nbrZoneElevage;
    }

    public static IntegerProperty nbrZoneAquacoleProperty() {
        return nbrZoneAquacole;
    }

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

// =========================
// RANDOM + CONFIG
// =========================

    private static final Random random = new Random();

    /*
     *  MODIFIER ICI : intervale de production (secondes)
     */
    private static final int PRODUCTION_INTERVAL = 10;

    // évite double timeline par zone
    private static final Set<Zone> runningZones = new HashSet<>();

    // refresh UI hook


// =========================
// SIMULATION PRODUCTION
// =========================

    public static void startProductionSimulation(Zone zone) {

        System.out.println("[DEBUG] startProductionSimulation CALLED for: " + zone.getNom());

        if (zone == null) {
            System.out.println("[ERROR] zone = null");
            return;
        }

        if (runningZones.contains(zone)) {
            System.out.println("[DEBUG] Timeline already running for: " + zone.getNom());
            return;
        }

        runningZones.add(zone);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(PRODUCTION_INTERVAL), e -> {

                    System.out.println("[DEBUG] TIMER TICK -> " + zone.getNom());

                    if (zone.getNbrEntite() <= 0) {
                        System.out.println("[WARN] No entities in zone: " + zone.getNom());
                        return;
                    }

                    // =========================
                    // CALCUL
                    // =========================
                    double t = System.currentTimeMillis() / 1000.0;
                    double variation = 0.8 + Math.abs(Math.sin(t / 20));

                    double rendement =
                            zone.getNbrEntite()
                                    * (5 + random.nextDouble() * 10)
                                    * variation;

                    rendement = Math.round(rendement * 10.0) / 10.0;

                    // =========================
                    // TYPE
                    // =========================
                    String typeProduction;

                    if (zone instanceof ZoneCulture) {
                        typeProduction = "kg cultures";
                    }
                    else if (zone instanceof ZoneElevage ze) {

                        if (ze.getTypeZoneElevage() == TypeZoneElevage.Ruminant) {
                            typeProduction = "litres lait";
                        } else {
                            typeProduction = "unité oeufs";
                        }
                    }
                    else {
                        typeProduction = "kg aquacole";
                    }

                    System.out.println("[DEBUG] GENERATED PROD: " + rendement + " " + typeProduction);

                    // =========================
                    // SAVE
                    // =========================
                    zone.enregistrerProduction(rendement, typeProduction);

                    System.out.println("[DEBUG] PRODUCTIONS SIZE = " + zone.getProductions().size());

                    // =========================
                    // REFRESH UI
                    // =========================
                    if (productionRefresh == null) {
                        System.out.println("[ERROR] productionRefresh is NULL ");
                    } else {
                        System.out.println("[DEBUG] calling refreshProduction()");
                        refreshProduction();
                    }

                    System.out.println("[DEBUG] END TICK " + zone.getNom());
                })
        );

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        System.out.println("[DEBUG] Timeline STARTED for: " + zone.getNom());
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
            String[] split = type.split(" ", 2);
            unite = split[0];
            if (split.length > 1) description = split[1];
        }

        return List.of(
                z.getNom(),
                z.getType().toString(),
                String.valueOf(p.getDate()),
                description,
                String.valueOf(p.getQuantite()),
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

class CapteurState{

}

class AlerteState{

}