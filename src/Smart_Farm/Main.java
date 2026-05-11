package Smart_Farm;

import java.time.LocalDate;
import java.util.*;

public class Main {

    public static void main(String[] args) {

        Ferme ferme = new Ferme("Smart Farm");
        App app = new App("Farm Manager", ferme);
        Scanner sc = new Scanner(System.in);

        int choix;

        do {
            System.out.println("\n====================================");
            System.out.println("        SMART FARM SYSTEM");
            System.out.println("====================================");

            System.out.println("1. Gérer les zones");
            System.out.println("2. Gérer les cultures");
            System.out.println("3. Gérer les animaux");
            System.out.println("4. Afficher toutes les zones");
            System.out.println("5. Afficher alertes");
            System.out.println("0. Quitter");

            System.out.print("\nChoix : ");
            choix = sc.nextInt();

            switch (choix) {

                case 1 -> menuZones(app, ferme, sc);
                case 2 -> menuCultures(app, ferme, sc);
                case 3 -> menuAnimaux(app, ferme, sc);
                case 4 -> System.out.println(app.afficherZones());
                case 5 -> ferme.afficherPanneauAlertes();
                case 0 -> System.out.println("Fermeture...");
                default -> System.out.println("Choix invalide !");
            }

        } while (choix != 0);
    }

    // =========================================================
    // ===================== ZONES =============================
    // =========================================================
    static void menuZones(App app, Ferme ferme, Scanner sc) {

        int choix;

        do {
            System.out.println("\n----- GESTION ZONES -----");
            System.out.println("1. Ajouter zone");
            System.out.println("2. Supprimer zone");
            System.out.println("3. Désactiver zone");
            System.out.println("4. Réactiver zone");
            System.out.println("0. Retour");

            choix = sc.nextInt();

            switch (choix) {

                case 1 -> creerZone(app, ferme, sc);

                case 2 -> {
                    System.out.print("Code zone : ");
                    Zone z = trouverZone(ferme, sc.nextInt());
                    if (z != null) ferme.supprimerZone(z);
                }

                case 3 -> {
                    System.out.print("Code zone : ");
                    Zone z = trouverZone(ferme, sc.nextInt());
                    if (z != null) app.desactiverZone(z);
                }

                case 4 -> {
                    System.out.print("Code zone : ");
                    Zone z = trouverZone(ferme, sc.nextInt());
                    if (z != null) app.reactiverZone(z);
                }
            }

        } while (choix != 0);
    }

    // =========================================================
    // ===================== CULTURES ==========================
    // =========================================================
    static void menuCultures(App app, Ferme ferme, Scanner sc) {

        int choix;

        do {
            System.out.println("\n----- GESTION CULTURES -----");
            System.out.println("1. Créer culture");
            System.out.println("2. Affecter culture à zone");
            System.out.println("0. Retour");

            choix = sc.nextInt();

            switch (choix) {

                case 1 -> creerCulture();

                case 2 -> {
                    System.out.print("Code zone : ");
                    Zone z = trouverZone(ferme, sc.nextInt());

                    System.out.print("Nom culture (famille simple exemple) : ");
                    sc.nextLine();
                    String nom = sc.nextLine();

                    Culture c = new Cereal(
                            FamilleCulture.Cereal,
                            LocalDate.now(),
                            LocalDate.now().plusMonths(3),
                            new Seuil(6, 8),
                            new Seuil(40, 70)
                    );

                    if (z instanceof ZoneCulture zc) {
                        app.ajouterCulture(c, zc);
                        System.out.println("Culture affectée !");
                    }
                }
            }

        } while (choix != 0);
    }

    // =========================================================
    // ===================== ANIMAUX ===========================
    // =========================================================
    static void menuAnimaux(App app, Ferme ferme, Scanner sc) {

        int choix;

        do {
            System.out.println("\n----- GESTION ANIMAUX -----");
            System.out.println("1. Ajouter Ruminant");
            System.out.println("2. Ajouter Volaille");
            System.out.println("3. Affecter animal à zone");
            System.out.println("0. Retour");

            choix = sc.nextInt();

            switch (choix) {

                case 1 -> {
                    Ruminant r = creerRuminant(sc);


                    System.out.print("Code zone : ");
                    Zone z = trouverZone(ferme, sc.nextInt());

                    if (z instanceof ZoneElevage ze)
                        app.ajouterRuminant(r, ze);
                }

                case 2 -> {
                    Volaille v = creerVolaille(sc);

                    System.out.print("Code zone : ");
                    Zone z = trouverZone(ferme, sc.nextInt());

                    if (z instanceof ZoneElevage ze)
                        app.ajouterVolaille(v, ze);
                }

                case 3 -> System.out.println("Animal déjà affecté à la création !");
            }

        } while (choix != 0);
    }

    // =========================================================
    // ================= CREATE ZONE ===========================
    // =========================================================
    static void creerZone(App app, Ferme ferme, Scanner sc) {

        System.out.println("\n===== CREATION ZONE =====");

        System.out.print("Code : ");
        int code = sc.nextInt();
        sc.nextLine();

        System.out.print("Nom : ");
        String nom = sc.nextLine();

        System.out.println("Type : 1.Culture 2.Elevage 3.Aquacole");
        int type = sc.nextInt();

        Zone zone = null;

        switch (type) {

            case 1 -> {
                zone = new ZoneCulture(code, nom, TypeZone.culture);
                ferme.ajouterZoneCulture((ZoneCulture) zone);
            }

            case 2 -> {
                System.out.println("1.Ruminant 2.Volaille");
                int t = sc.nextInt();

                TypeZoneElevage tz = (t == 1)
                        ? TypeZoneElevage.Ruminant
                        : TypeZoneElevage.Volaille;

                zone = new ZoneElevage(code, nom, TypeZone.elevage, tz, null);
                ferme.ajouterZoneElevage((ZoneElevage) zone);
            }

            case 3 -> {
                zone = new ZoneAquacole(code, nom, TypeZone.aquacole);
                ferme.ajouterZoneAquacole((ZoneAquacole) zone);
            }
        }

        System.out.println("Zone ajoutée !");
    }

    // =========================================================
    // ================= HELPERS CREATION ======================
    // =========================================================
    static Culture creerCulture() {
        return new Cereal(
                FamilleCulture.Cereal,
                LocalDate.now(),
                LocalDate.now().plusMonths(4),
                new Seuil(6, 8),
                new Seuil(40, 70)
        );
    }

    static Ruminant creerRuminant(Scanner sc) {

        System.out.print("Nom : ");
        sc.nextLine();
        String nom = sc.nextLine();

        Ruminant r = new Ruminant(TypeEspece.ruminant, nom);
        r.setAge(3);
        r.setPoid(400);
        return r;
    }

    static Volaille creerVolaille(Scanner sc) {

        System.out.print("Nom : ");
        sc.nextLine();
        String nom = sc.nextLine();

        Volaille v = new Volaille(TypeEspece.volaille, nom);
        v.setAge(1);
        v.setPoid(2);
        return v;
    }

    // =========================================================
    // ================= FIND ZONE =============================
    // =========================================================
    static Zone trouverZone(Ferme ferme, int code) {

        for (Zone z : ferme.getZones()) {
            if (z.getCode() == code)
                return z;
        }

        System.out.println("Zone introuvable !");
        return null;
    }
}