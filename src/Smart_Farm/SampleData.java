package Smart_Farm;

/**
 * Demo data for testing capteurs and alertes.
 *
 * To remove all sample data: set ENABLED = false and relaunch.
 * The rest of the app works identically with only real data.
 */
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

        ZoneState.addZone(zoneBle);
        ZoneState.addZone(zoneBovins);
        ZoneState.addZone(zonePoisson);

        // ── Capteurs — Zone-Ble ────────────────────────────────────────
        // Narrow thresholds so random readings reliably produce alerts
        CapteurState.addCapteur(new CapteurEnvironnemental(
                "ENV-TEMP-01", "Zone-Ble", TypeMesure.TEMPERATURE, new Seuil(20, 27)));
        CapteurState.addCapteur(new CapteurEnvironnemental(
                "ENV-HUM-01",  "Zone-Ble", TypeMesure.HUMIDITE,    new Seuil(52, 68)));
        CapteurState.addCapteur(new CapteurSol(
                "SOL-PH-01",   "Zone-Ble", TypeMesure.PH_SOL,      new Seuil(6.2, 7.0)));

        // ── Capteurs — Zone-Bovins ─────────────────────────────────────
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
