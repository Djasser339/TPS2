package Smart_Farm;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.*;
import java.util.stream.Collectors;

class CommercialState {

    // =========================================
    // STORAGE
    // =========================================
    private static final List<Client> clients = new ArrayList<>();
    private static final List<Vente>  ventes  = new ArrayList<>();

    // =========================================
    // PROPERTIES (UI binding)
    // =========================================
    private static final IntegerProperty nbrClients      = new SimpleIntegerProperty(0);
    private static final IntegerProperty nbrVentes        = new SimpleIntegerProperty(0);
    private static final DoubleProperty  chiffreAffaires  = new SimpleDoubleProperty(0.0);

    public static IntegerProperty nbrClientsProperty()     { return nbrClients; }
    public static IntegerProperty nbrVentesProperty()      { return nbrVentes; }
    public static DoubleProperty  chiffreAffairesProperty(){ return chiffreAffaires; }

    // =========================================
    // REFRESH HOOKS
    // =========================================
    private static Runnable clientRefresh;
    private static Runnable venteRefresh;
    private static Runnable dashRefresh;

    public static void setClientRefresh(Runnable r) { clientRefresh = r; }
    public static void setVenteRefresh(Runnable r)  { venteRefresh  = r; }
    public static void setDashRefresh(Runnable r)   { dashRefresh   = r; }

    public static void refreshClients() { if (clientRefresh != null) Platform.runLater(clientRefresh); }
    public static void refreshVentes()  { if (venteRefresh  != null) Platform.runLater(venteRefresh);  }
    public static void refreshDash()    { if (dashRefresh   != null) Platform.runLater(dashRefresh);   }

    // =========================================
    // CLIENT — CRUD
    // =========================================
    public static void addClient(Client c) {
        clients.add(c);
        nbrClients.set(clients.size());
        refreshClients();
        refreshDash();
    }

    public static void updateClient(Client updated) {
        // fields were mutated directly on the object — just refresh UI
        refreshClients();
    }

    /**
     * Deletes a client.
     * Returns false if the client has registered sales (prevent orphan ventes).
     */
    public static boolean deleteClient(String clientId) {
        boolean hasVentes = ventes.stream().anyMatch(v -> v.getClientId().equals(clientId));
        if (hasVentes) return false;
        clients.removeIf(c -> c.getId().equals(clientId));
        nbrClients.set(clients.size());
        refreshClients();
        refreshDash();
        return true;
    }

    public static List<Client> getClients()             { return Collections.unmodifiableList(clients); }
    public static List<Client> getClientsMutable()      { return clients; }

    public static Optional<Client> getClientById(String id) {
        return clients.stream().filter(c -> c.getId().equals(id)).findFirst();
    }

    public static List<Client> searchClient(String query) {
        if (query == null || query.isBlank()) return clients;
        String q = query.toLowerCase();
        return clients.stream()
                .filter(c -> c.getNomComplet().toLowerCase().contains(q)
                        || c.getEmail().toLowerCase().contains(q)
                        || c.getTelephone().toLowerCase().contains(q)
                        || c.getId().toLowerCase().contains(q)
                        || c.getAdresse().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    public static List<String> mapClient(Client c) {
        long nbVentes = ventes.stream().filter(v -> v.getClientId().equals(c.getId())).count();
        double ca     = ventes.stream()
                .filter(v -> v.getClientId().equals(c.getId()))
                .mapToDouble(Vente::getMontant).sum();
        return List.of(
                c.getId(),
                c.getNomComplet(),
                c.getEmail(),
                c.getTelephone(),
                c.getAdresse(),
                String.valueOf(nbVentes),
                String.format("%.2f DA", ca)
        );
    }

    // =========================================
    // VENTE — CRUD
    // =========================================

    /**
     * Registers a sale.
     * Returns false if the requested quantity exceeds available production stock.
     */
    public static boolean addVente(Vente v) {
        double available = getQuantiteDisponible(v.getZoneNom(), v.getTypeProduction());
        if (v.getQuantite() > available + 1e-9) return false;

        ventes.add(v);
        nbrVentes.set(ventes.size());
        updateChiffreAffaires();
        refreshVentes();
        refreshDash();
        return true;
    }

    public static void deleteVente(String venteId) {
        ventes.removeIf(v -> v.getId().equals(venteId));
        nbrVentes.set(ventes.size());
        updateChiffreAffaires();
        refreshVentes();
        refreshDash();
    }

    public static List<Vente> getVentes()        { return Collections.unmodifiableList(ventes); }

    public static List<Vente> searchVente(String query) {
        if (query == null || query.isBlank()) return ventes;
        String q = query.toLowerCase();
        return ventes.stream()
                .filter(v -> v.getId().toLowerCase().contains(q)
                        || v.getZoneNom().toLowerCase().contains(q)
                        || v.getTypeProduction().toLowerCase().contains(q)
                        || getClientById(v.getClientId())
                               .map(c -> c.getNomComplet().toLowerCase().contains(q))
                               .orElse(false))
                .collect(Collectors.toList());
    }

    public static List<String> mapVente(Vente v) {
        String clientName = getClientById(v.getClientId())
                .map(Client::getNomComplet).orElse("(inconnu)");
        return List.of(
                v.getId(),
                clientName,
                v.getZoneNom(),
                v.getTypeProduction(),
                String.format("%.2f", v.getQuantite()),
                String.format("%.2f DA", v.getPrixUnitaire()),
                String.format("%.2f DA", v.getMontant()),
                v.getDateVente().toString()
        );
    }

    /** All sales for a given client, most recent first. */
    public static List<Vente> getVentesForClient(String clientId) {
        return ventes.stream()
                .filter(v -> v.getClientId().equals(clientId))
                .sorted(Comparator.comparing(Vente::getDateVente).reversed())
                .collect(Collectors.toList());
    }

    // =========================================
    // STOCK — the core link to production
    // =========================================

    /**
     * Available quantity = total produced (in all production records for this
     * zone + typeProduction) minus total already sold (in registered ventes).
     *
     * This is the bridge between the ProductionRecord system and Commercial.
     * It reads live from ZoneState (no duplication of production data).
     */
    public static double getQuantiteDisponible(String zoneNom, String typeProduction) {
        double totalProduit = ZoneState.getZones().stream()
                .filter(z -> z.getNom().equals(zoneNom))
                .flatMap(z -> z.getProductions().stream())
                .filter(p -> p.getTypeProduction().equals(typeProduction))
                .mapToDouble(EnregistrementProduction::getQuantite)
                .sum();

        double totalVendu = ventes.stream()
                .filter(v -> v.getZoneNom().equals(zoneNom)
                          && v.getTypeProduction().equals(typeProduction))
                .mapToDouble(Vente::getQuantite)
                .sum();

        return Math.max(0.0, totalProduit - totalVendu);
    }

    /**
     * All (zoneNom, typeProduction) pairs that have at least some recorded production.
     * Used to populate the sale form's zone/product selectors.
     */
    public static List<String[]> getAvailableStockLines() {
        List<String[]> lines = new ArrayList<>();
        for (Zone z : ZoneState.getZones()) {
            Set<String> types = z.getProductions().stream()
                    .map(EnregistrementProduction::getTypeProduction)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            for (String type : types) {
                double qty = getQuantiteDisponible(z.getNom(), type);
                lines.add(new String[]{ z.getNom(), type, String.format("%.2f", qty) });
            }
        }
        return lines;
    }

    /** Unique production types for a specific zone (from its actual production records). */
    public static List<String> getTypesForZone(String zoneNom) {
        return ZoneState.getZones().stream()
                .filter(z -> z.getNom().equals(zoneNom))
                .flatMap(z -> z.getProductions().stream())
                .map(EnregistrementProduction::getTypeProduction)
                .distinct()
                .collect(Collectors.toList());
    }

    // =========================================
    // STATISTICS
    // =========================================

    public static double getChiffreAffairesTotal() {
        return ventes.stream().mapToDouble(Vente::getMontant).sum();
    }

    /** Top N clients ranked by total revenue. */
    public static List<Client> getTopClients(int n) {
        Map<String, Double> rev = new HashMap<>();
        ventes.forEach(v -> rev.merge(v.getClientId(), v.getMontant(), Double::sum));
        return clients.stream()
                .sorted(Comparator.comparingDouble(
                        (Client c) -> rev.getOrDefault(c.getId(), 0.0)).reversed())
                .limit(n)
                .collect(Collectors.toList());
    }

    /** Revenue grouped by typeProduction, sorted descending. */
    public static Map<String, Double> getRevenueByProduct() {
        return ventes.stream()
                .collect(Collectors.groupingBy(Vente::getTypeProduction,
                         Collectors.summingDouble(Vente::getMontant)))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> a, LinkedHashMap::new));
    }

    /** Quantity sold grouped by typeProduction, sorted descending. */
    public static Map<String, Double> getQuantityByProduct() {
        return ventes.stream()
                .collect(Collectors.groupingBy(Vente::getTypeProduction,
                         Collectors.summingDouble(Vente::getQuantite)))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> a, LinkedHashMap::new));
    }

    // =========================================
    // INTERNAL
    // =========================================
    private static void updateChiffreAffaires() {
        chiffreAffaires.set(getChiffreAffairesTotal());
    }

    // =========================================
    // PERSISTENCE
    // =========================================
    public static void restore(List<Client> cls, List<Vente> vnts) {
        clients.clear();
        ventes.clear();
        if (cls != null) clients.addAll(cls);
        if (vnts != null) ventes.addAll(vnts);
        nbrClients.set(clients.size());
        nbrVentes.set(ventes.size());
        updateChiffreAffaires();
        refreshClients();
        refreshVentes();
        refreshDash();
    }

    public static List<Client> getClientsForSave() { return new ArrayList<>(clients); }
    public static List<Vente>  getVentesForSave()  { return new ArrayList<>(ventes); }
}
