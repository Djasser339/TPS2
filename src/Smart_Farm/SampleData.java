package Smart_Farm;

import java.time.LocalDate;

public class SampleData {

    // ── Toggle ─────────────────────────────────────────────────────────
    // false  →  no sample data, app starts empty (real data only)
    // true   →  loads demo zones, capteurs and generates alert history
    // ───────────────────────────────────────────────────────────────────
    public static final boolean ENABLED = true;

    public static void load() {
        if (!ENABLED) return;

        // ── Zones ──────────────────────────────────────────────────────
        ZoneCulture  zoneBle     = new ZoneCulture(1001, "Zone-Ble",     TypeZone.culture);
        ZoneElevage  zoneBovins  = new ZoneElevage(1002, "Zone-Bovins",  TypeZone.elevage,
                TypeZoneElevage.Ruminant,
                new GeographicalLimits("Zone-Bovins", 43, 44, 1, 2));
        ZoneAquacole zonePoisson = new ZoneAquacole(1003, "Zone-Poissons", TypeZone.aquacole);

        ZoneElevage zoneMoutons = new ZoneElevage(1004, "Zone-Moutons", TypeZone.elevage,
                TypeZoneElevage.Ruminant,
                new GeographicalLimits("Zone-Moutons", 46, 48, 3, 6));
        ZoneElevage zonePoules = new ZoneElevage(1005, "Zone-Poules", TypeZone.elevage,
                TypeZoneElevage.Volaille,
                new GeographicalLimits("Zone-Poules", 40, 42, -2, 1));

        ZoneState.addZone(zoneBle);
        ZoneState.addZone(zoneBovins);
        ZoneState.addZone(zonePoisson);
        ZoneState.addZone(zoneMoutons);
        ZoneState.addZone(zonePoules);

        // ── Cultures ───────────────────────────────────────────────────
        Cereal ble = new Cereal(FamilleCulture.Cereal,
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 7, 15),
                new Seuil(6.0, 7.5), new Seuil(50, 70));
        Legume tomate = new Legume(FamilleCulture.Legume,
                LocalDate.of(2026, 4, 10), LocalDate.of(2026, 8, 20),
                new Seuil(6.0, 7.0), new Seuil(60, 80));
        FarmState.addCulture(ble);
        FarmState.addCulture(tomate);

        // ── Animaux ────────────────────────────────────────────────────
        Ruminant bovin = new Ruminant(TypeEspece.ruminant, "Bessie");
        bovin.setAge(4); bovin.setPoid(520);
        Volaille poule = new Volaille(TypeEspece.volaille, "Coco");
        poule.setAge(1); poule.setPoid(3);
        AnimalState.addAnimal(bovin);
        AnimalState.addAnimal(poule);

        // ── Capteurs — Zone-Ble ────────────────────────────────────────
        // Narrow thresholds so random readings reliably produce alerts
        CapteurState.addCapteur(new CapteurEnvironnemental(
                "ENV-TEMP-01", "Zone-Ble", TypeMesure.TEMPERATURE, new Seuil(20, 27)));
        CapteurState.addCapteur(new CapteurEnvironnemental(
                "ENV-HUM-01",  "Zone-Ble", TypeMesure.HUMIDITE,    new Seuil(52, 68)));
        CapteurState.addCapteur(new CapteurSol(
                "SOL-PH-01",   "Zone-Ble", TypeMesure.PH_SOL,      new Seuil(6.2, 7.0)));

        // ── Capteurs — Zone-Bovins ─────────────────────────────────────
        CapteurState.addCapteur(new CapteurEnvironnemental(
                "ENV-BOV-01", "Zone-Bovins", TypeMesure.TEMPERATURE, new Seuil(15, 28)));
        CapteurState.addCapteur(new CapteurBiometrique(
                "BIO-BOV-01", "Zone-Bovins", new Seuil(38.2, 39.0), new Seuil(35, 85)));
        CapteurState.addCapteur(new CapteurGPS("GPS-BOV-01", "Zone-Bovins"));

        // ── Capteurs — Zone-Poissons ───────────────────────────────────
        CapteurState.addCapteur(new CapteurEau(
                "EAU-TEMP-01", "Zone-Poissons", TypeMesure.TEMPERATURE_EAU, new Seuil(16, 22)));
        CapteurState.addCapteur(new CapteurEau(
                "EAU-OXY-01",  "Zone-Poissons", TypeMesure.OXYGENE_DISSOUS, new Seuil(6, 9)));

        // ── Generate reading history (automatically triggers alerts) ───
        for (Capteur c : CapteurState.getCapteurs()) {
            for (int i = 0; i < 20; i++) c.envoyerReleve();
        }

        AlerteState.updateStats();
    }
}
