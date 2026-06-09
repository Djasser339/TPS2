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
        zoneBle.ajouterCulture(ble);
        zoneBle.ajouterCulture(tomate);

        // ── Animaux ────────────────────────────────────────────────────
        Ruminant bovin = new Ruminant(TypeEspece.ruminant, "Bessie");
        bovin.setAge(4); bovin.setPoid(520);
        Volaille poule = new Volaille(TypeEspece.volaille, "Coco");
        poule.setAge(1); poule.setPoid(3);
        AnimalState.addAnimal(bovin);
        AnimalState.addAnimal(poule);
        zoneBovins.ajouterRuminant(bovin);
        zonePoules.ajouterVollaile(poule);

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

        ZoneState.refresh();
        AlerteState.updateStats();

        // ── Initial production stock (so commercial demo works on first launch) ──
        zoneBle.enregistrerProduction(620.0, "kg cultures");
        zoneBle.enregistrerProduction(380.5, "kg cultures");
        zoneBovins.enregistrerProduction(210.0, "litres lait");
        zoneBovins.enregistrerProduction(175.5, "litres lait");
        zoneMoutons.enregistrerProduction(148.0, "litres lait");
        zonePoules.enregistrerProduction(450.0, "unité oeufs");
        zonePoules.enregistrerProduction(310.0, "unité oeufs");
        zonePoisson.enregistrerProduction(95.0,  "kg aquacole");

        // ── Sample clients ──────────────────────────────────────────────
        Client c1 = new Client("Benali",    "Karim",   "k.benali@email.dz",    "0550 123 456", "Alger, Bab El Oued");
        Client c2 = new Client("Meziane",   "Fatima",  "f.meziane@email.dz",   "0661 789 012", "Oran, Bir El Djir");
        Client c3 = new Client("Hadji",     "Youcef",  "y.hadji@email.dz",     "0770 345 678", "Constantine, El Khroub");
        Client c4 = new Client("Boudaoud",  "Samira",  "s.boudaoud@email.dz",  "0555 901 234", "Blida, Ouled Yaich");
        CommercialState.addClient(c1);
        CommercialState.addClient(c2);
        CommercialState.addClient(c3);
        CommercialState.addClient(c4);

        // ── Sample ventes (all within available stock) ──────────────────
        CommercialState.addVente(new Vente(c1.getId(), "Zone-Ble",     "kg cultures",  200.0, 45.0,  "Livraison directe"));
        CommercialState.addVente(new Vente(c2.getId(), "Zone-Bovins",  "litres lait",   80.0, 120.0, "Marché local Oran"));
        CommercialState.addVente(new Vente(c3.getId(), "Zone-Poules",  "unité oeufs",  150.0,  18.0, "Restaurant Constantine"));
        CommercialState.addVente(new Vente(c1.getId(), "Zone-Ble",     "kg cultures",  100.0, 47.0,  "2ème commande"));
        CommercialState.addVente(new Vente(c4.getId(), "Zone-Moutons", "litres lait",   60.0, 130.0, ""));
        CommercialState.addVente(new Vente(c2.getId(), "Zone-Poissons","kg aquacole",   30.0, 850.0, "Poissonnerie Oran"));
        CommercialState.addVente(new Vente(c3.getId(), "Zone-Bovins",  "litres lait",   40.0, 125.0, ""));
    }
}
