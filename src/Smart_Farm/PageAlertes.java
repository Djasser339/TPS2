package Smart_Farm;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class PageAlertes {

    // =========================================
    // PAGE ALERTES
    // =========================================
    public static ScrollPane alertePage() {

        VBox center = new VBox();
        center.setSpacing(30);
        center.setPadding(new Insets(20));
        center.setAlignment(Pos.TOP_CENTER);
        center.setFillWidth(true);

        // =========================
        // STATS
        // =========================
        center.getChildren().add(alerteStatsCards());

        // =========================
        // ALERTES ACTIVES
        // =========================
        center.getChildren().add(
                UIFactory.createAnimatedTitle("🚨 Alertes Actives — Critiques en premier")
        );

        List<String> headers = List.of("ID", "Zone", "Type Capteur", "Niveau", "Date", "Valeur", "Statut");

        VBox tableCard = TableFactory.createSearchTableCard(
                headers,
                query -> {
                    List<Alerte> alertes = AlerteState.getAlertesActives();
                    if (query == null || query.isEmpty()) return alertes;
                    String q = query.toLowerCase();
                    return alertes.stream()
                            .filter(a -> AlerteState.mapAlerte(a).stream()
                                    .anyMatch(v -> v.toLowerCase().contains(q)))
                            .collect(Collectors.toList());
                },
                AlerteState::mapAlerte,
                alerte -> showAlerteActionForm(alerte, () -> AlerteState.refreshAlertes()),
                refresh -> AlerteState.setAlerteRefresh(() ->
                        refresh.accept(AlerteState.getAlertesActives()))
        );

        center.getChildren().add(tableCard);

        // =========================
        // HISTORIQUE & FILTRES
        // =========================
        center.getChildren().add(UIFactory.createAnimatedTitle("🔍 Historique & Filtres"));
        center.getChildren().add(createFiltreCard());

        // =========================
        // SCROLL GLOBAL
        // =========================
        ScrollPane pageScroll = new ScrollPane(center);
        pageScroll.setFitToWidth(true);
        pageScroll.setPannable(true);
        pageScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        pageScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        return pageScroll;
    }

    // =========================================
    // STATS CARDS
    // =========================================
    public static HBox alerteStatsCards() {

        HBox container = new HBox();
        container.setSpacing(20);
        container.setPadding(new Insets(20));
        container.setAlignment(Pos.CENTER);

        container.getChildren().addAll(
                UIFactory.createLiveNumberDisplay(
                        "Alertes Actives", AlerteState.nbrAlertesProperty(), 240, 90),
                UIFactory.createLiveNumberDisplay(
                        "Critiques", AlerteState.nbrCritiquesProperty(), 240, 90),
                UIFactory.createLiveNumberDisplay(
                        "Avertissements", AlerteState.nbrAvertissementsProperty(), 240, 90)
        );

        return container;
    }

    // =========================================
    // FORMULAIRE ACTION ALERTE
    // =========================================
    public static void showAlerteActionForm(Alerte alerte, Runnable onClose) {

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Alerte #" + alerte.getId());

        VBox root = new VBox(15);
        root.setPadding(new Insets(25));
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("form-global");

        // NIVEAU BADGE
        String niveauColor = switch (alerte.getNiveau()) {
            case critique      -> "#ff5252";
            case avertissement -> "#ffb300";
            default            -> "#4caf50";
        };
        String niveauIcon = switch (alerte.getNiveau()) {
            case critique      -> "🔴";
            case avertissement -> "🟡";
            default            -> "🟢";
        };

        Label niveauLabel = new Label(niveauIcon + " " + alerte.getNiveau().name().toUpperCase());
        niveauLabel.setStyle(
                "-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + niveauColor + ";"
        );

        // INFOS
        GestionnaireCapteursAlertes g = GestionnaireCapteursAlertes.getInstance();
        Capteur c = g.getCapteurById(alerte.getReleve().getIdCapteur());
        String zone = (c != null) ? c.getZoneId() : "?";
        String type = (c != null) ? c.getTypeNom() : "?";

        Label infoLabel = new Label(
                "Zone : " + zone + "   |   Capteur : " + alerte.getReleve().getIdCapteur()
                        + "  (" + type + ")");
        Label valLabel  = new Label("Valeur : " + alerte.getReleve().getValeurAsString());
        Label dateLabel = new Label("Date : " + alerte.getDateCreation()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd  HH:mm:ss")));
        Label statutLabel = new Label("Statut : " + (alerte.isAcquittee() ? "Acquittée ✅" : "Active ⚠️"));

        // ACTIONS
        Button acquitterBtn = UIFactory.createActionButton("✅ Acquitter", () -> {
            g.acquitterAlerte(alerte.getId());
            AlerteState.updateStats();
            if (onClose != null) onClose.run();
            stage.close();
        });

        Button supprimerBtn = UIFactory.createActionButton("🗑 Supprimer", () -> {
            g.supprimerAlerte(alerte.getId());
            AlerteState.updateStats();
            if (onClose != null) onClose.run();
            stage.close();
        });

        if (alerte.isAcquittee())  acquitterBtn.setDisable(true);
        if (alerte.isSupprimee())  supprimerBtn.setDisable(true);

        HBox buttons = new HBox(15, acquitterBtn, supprimerBtn);
        buttons.setAlignment(Pos.CENTER);

        root.getChildren().addAll(
                niveauLabel,
                new Separator(),
                infoLabel, valLabel, dateLabel, statutLabel,
                new Separator(),
                buttons
        );

        Scene scene = new Scene(root, 460, 320);
        scene.getStylesheets().add(PageZone.class.getResource("style.css").toExternalForm());
        stage.setScene(scene);
        stage.showAndWait();
    }

    // =========================================
    // CARTE FILTRE HISTORIQUE
    // =========================================
    private static VBox createFiltreCard() {

        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.getStyleClass().add("culture-list-card");

        Label title = new Label("Filtrer l'historique des alertes");
        title.getStyleClass().add("culture-zone-title");

        // ZONE
        TextField zoneField = new TextField();
        zoneField.setPromptText("Zone ID  (vide = toutes)");
        zoneField.setPrefWidth(250);

        // NIVEAU
        ComboBox<String> niveauCombo = new ComboBox<>();
        niveauCombo.getItems().addAll("Tous", "critique", "avertissement", "normal");
        niveauCombo.setValue("Tous");

        // DATES
        DatePicker dateDebut = new DatePicker();
        dateDebut.setPromptText("Date début");
        DatePicker dateFin = new DatePicker();
        dateFin.setPromptText("Date fin");

        HBox dateRow = new HBox(10,
                new Label("De :"), dateDebut,
                new Label("À :"), dateFin);
        dateRow.setAlignment(Pos.CENTER_LEFT);

        // RÉSULTATS
        VBox resultArea = new VBox(6);
        resultArea.setPadding(new Insets(5, 0, 0, 0));

        Button searchBtn = UIFactory.createActionButton("🔍 Filtrer", () -> {

            String zone   = zoneField.getText().trim().isEmpty() ? null : zoneField.getText().trim();
            Gravite niv   = niveauCombo.getValue().equals("Tous") ? null
                    : Gravite.valueOf(niveauCombo.getValue());
            LocalDateTime debut = dateDebut.getValue() != null
                    ? dateDebut.getValue().atStartOfDay() : null;
            LocalDateTime fin   = dateFin.getValue() != null
                    ? dateFin.getValue().atTime(23, 59, 59) : null;

            List<Alerte> filtered = GestionnaireCapteursAlertes.getInstance()
                    .filtrerAlertes(zone, null, niv, debut, fin);

            resultArea.getChildren().clear();

            if (filtered.isEmpty()) {
                Label none = new Label("Aucune alerte trouvée pour ces critères");
                none.getStyleClass().add("culture-text");
                resultArea.getChildren().add(none);
                return;
            }

            for (Alerte a : filtered) {

                HBox row = new HBox(10);
                row.setPadding(new Insets(8));
                row.setAlignment(Pos.CENTER_LEFT);
                row.getStyleClass().add("culture-item");
                row.setStyle(row.getStyle() + " -fx-cursor: hand;");

                String color = switch (a.getNiveau()) {
                    case critique      -> "#ff5252";
                    case avertissement -> "#ffb300";
                    default            -> "#4caf50";
                };

                GestionnaireCapteursAlertes g = GestionnaireCapteursAlertes.getInstance();
                Capteur cap = g.getCapteurById(a.getReleve().getIdCapteur());
                String z = (cap != null) ? cap.getZoneId() : "?";

                String icon = switch (a.getNiveau()) {
                    case critique      -> "🔴";
                    case avertissement -> "🟡";
                    default            -> "🟢";
                };

                String statut = a.isAcquittee() ? "✅" : a.isSupprimee() ? "🗑" : "⚠️";

                Label lbl = new Label(
                        icon + "  #" + a.getId()
                                + "   Zone: " + z
                                + "   " + a.getNiveau().name()
                                + "   " + a.getReleve().getValeurAsString()
                                + "   " + a.getDateCreation().format(DateTimeFormatter.ofPattern("MM-dd HH:mm"))
                                + "  " + statut
                );
                lbl.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 12px;");

                row.getChildren().add(lbl);

                // click to open action form
                row.setOnMouseClicked(e ->
                        showAlerteActionForm(a, () -> resultArea.getChildren().clear())
                );

                resultArea.getChildren().add(row);
            }
        });

        card.getChildren().addAll(title, zoneField, niveauCombo, dateRow, searchBtn, resultArea);
        return card;
    }
}
