package Smart_Farm;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
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

        // STATS
        center.getChildren().add(alerteStatsCards());

        // ALERTES ACTIVES
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

        // CHART — résumé des alertes (sous le tableau des alertes actives)
        center.getChildren().add(createAlerteChartCard());

        // HISTORIQUE & FILTRES
        center.getChildren().add(UIFactory.createAnimatedTitle("🔍 Historique & Filtres"));
        center.getChildren().add(createFiltreCard());

        // SCROLL GLOBAL
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
        container.setPadding(new Insets(10, 20, 10, 20));
        container.setAlignment(Pos.CENTER);

        container.getChildren().addAll(
                createAlertStatCard("Alertes Actives",  "🚨", "#1565C0", "#E3F2FD",
                        AlerteState.nbrAlertesProperty()),
                createAlertStatCard("Critiques",        "🔴", "#B71C1C", "#FFEBEE",
                        AlerteState.nbrCritiquesProperty()),
                createAlertStatCard("Avertissements",   "🟡", "#E65100", "#FFF3E0",
                        AlerteState.nbrAvertissementsProperty())
        );

        return container;
    }

    private static VBox createAlertStatCard(
            String title, String icon,
            String textColor, String bgColor,
            javafx.beans.value.ObservableNumberValue value
    ) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(18, 24, 18, 24));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPrefWidth(240);
        card.setMinWidth(200);
        card.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                "-fx-background-radius: 14;" +
                "-fx-border-radius: 14;" +
                "-fx-border-color: " + textColor + "44;" +
                "-fx-border-width: 1.5;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 10, 0.1, 0, 3);"
        );

        Label iconTitle = new Label(icon + "  " + title);
        iconTitle.setStyle(
                "-fx-font-size: 13px; -fx-font-weight: bold;" +
                "-fx-text-fill: " + textColor + ";"
        );
        iconTitle.setWrapText(true);

        Label valueLabel = new Label();
        valueLabel.setStyle(
                "-fx-font-size: 36px; -fx-font-weight: bold;" +
                "-fx-text-fill: " + textColor + ";"
        );
        valueLabel.textProperty().bind(
                Bindings.createStringBinding(
                        () -> String.valueOf(value.getValue()), value)
        );

        card.getChildren().addAll(iconTitle, valueLabel);
        UIFactory.applyHoverEffect(card);
        return card;
    }

    // =========================================
    // CHART — RÉSUMÉ DES ALERTES
    // =========================================
    private static VBox createAlerteChartCard() {

        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.getStyleClass().add("farm-graph-card");

        Label titleLbl = new Label("📊  Résumé des alertes");
        titleLbl.getStyleClass().add("farm-graph-title");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setAutoRanging(true);
        yAxis.setMinorTickVisible(false);
        yAxis.setLabel("Nombre");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.getStyleClass().add("farm-bar-chart");
        chart.setPrefHeight(230);
        chart.setCategoryGap(35);
        chart.setBarGap(8);

        Runnable refreshChart = () -> {
            chart.getData().clear();
            GestionnaireCapteursAlertes g = GestionnaireCapteursAlertes.getInstance();
            List<Alerte> all = g.filtrerAlertes(null, null, null, null, null);

            long critiques      = all.stream()
                    .filter(a -> a.getNiveau() == Gravite.critique).count();
            long avertissements = all.stream()
                    .filter(a -> a.getNiveau() == Gravite.avertissement).count();
            long acquittees     = all.stream()
                    .filter(Alerte::isAcquittee).count();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.getData().add(new XYChart.Data<>("Critique",      critiques));
            series.getData().add(new XYChart.Data<>("Avertissement", avertissements));
            series.getData().add(new XYChart.Data<>("Acquittées",    acquittees));

            chart.getData().add(series);

            Platform.runLater(() -> {
                for (XYChart.Data<String, Number> d : series.getData()) {
                    if (d.getNode() == null) continue;
                    String color = switch (d.getXValue()) {
                        case "Critique"      -> "#c62828";
                        case "Avertissement" -> "#e65100";
                        default              -> "#2e7d32";
                    };
                    d.getNode().setStyle(
                            "-fx-bar-fill: " + color + ";" +
                            "-fx-background-radius: 8 8 0 0;"
                    );
                }
            });
        };

        refreshChart.run();
        AlerteState.nbrAlertesProperty().addListener((obs, o, n) ->
                Platform.runLater(refreshChart));

        card.getChildren().addAll(titleLbl, chart);
        return card;
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

        GestionnaireCapteursAlertes g = GestionnaireCapteursAlertes.getInstance();
        Capteur c = g.getCapteurById(alerte.getReleve().getIdCapteur());
        String zone = (c != null) ? c.getZoneId() : "?";
        String type = (c != null) ? c.getTypeNom() : "?";

        Label infoLabel  = new Label(
                "Zone : " + zone + "   |   Capteur : " + alerte.getReleve().getIdCapteur()
                        + "  (" + type + ")");
        Label valLabel   = new Label("Valeur : " + alerte.getReleve().getValeurAsString());
        Label dateLabel  = new Label("Date : " + alerte.getDateCreation()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd  HH:mm:ss")));
        Label statutLabel = new Label("Statut : " + (alerte.isAcquittee() ? "Acquittée ✅" : "Active ⚠️"));

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

        if (alerte.isAcquittee()) acquitterBtn.setDisable(true);
        if (alerte.isSupprimee()) supprimerBtn.setDisable(true);

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
    // CARTE HISTORIQUE COMPLET + BOUTON FILTRE
    // =========================================
    private static VBox createFiltreCard() {

        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.getStyleClass().add("culture-list-card");

        Label title = new Label("Historique des alertes");
        title.getStyleClass().add("culture-zone-title");

        Button filtreBtn = new Button("🔍  Filtrer");
        filtreBtn.getStyleClass().add("primary-button");
        filtreBtn.setStyle("-fx-padding: 8 20; -fx-font-size: 13px;");

        Region hSpacer = new Region();
        HBox.setHgrow(hSpacer, Priority.ALWAYS);

        HBox headerRow = new HBox(10, title, hSpacer, filtreBtn);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        VBox resultArea = new VBox(6);
        resultArea.setPadding(new Insets(5, 0, 0, 0));

        loadHistory(resultArea, null, null, null, null, null);

        filtreBtn.setOnAction(e -> showFiltreModal(resultArea));

        // Pas de ScrollPane interne — la page entière scrolle
        card.getChildren().addAll(headerRow, resultArea);
        return card;
    }

    // =========================================
    // CHARGEMENT HISTORIQUE
    // =========================================
    private static void loadHistory(VBox resultArea,
                                    String zone, TypeMesure typeCapteur, Gravite niv,
                                    LocalDateTime debut, LocalDateTime fin) {

        List<Alerte> all = GestionnaireCapteursAlertes.getInstance()
                .filtrerAlertes(zone, typeCapteur, niv, debut, fin)
                .stream()
                .sorted((a, b) -> b.getDateCreation().compareTo(a.getDateCreation()))
                .collect(Collectors.toList());

        resultArea.getChildren().clear();

        if (all.isEmpty()) {
            Label none = new Label("Aucune alerte dans l'historique");
            none.setStyle("-fx-text-fill: #888; -fx-font-size: 13px; -fx-padding: 10 0;");
            resultArea.getChildren().add(none);
            return;
        }

        GestionnaireCapteursAlertes g = GestionnaireCapteursAlertes.getInstance();

        for (Alerte a : all) {

            String color = switch (a.getNiveau()) {
                case critique      -> "#c62828";
                case avertissement -> "#e65100";
                default            -> "#2e7d32";
            };
            String icon = switch (a.getNiveau()) {
                case critique      -> "🔴";
                case avertissement -> "🟡";
                default            -> "🟢";
            };

            Capteur cap = g.getCapteurById(a.getReleve().getIdCapteur());
            String z = (cap != null) ? cap.getZoneId() : "?";
            String capteurId   = (cap != null) ? cap.getId()      : "?";
            String capteurType = (cap != null) ? cap.getTypeNom() : "?";

            String statutText  = a.isAcquittee() ? "Acquittee" : a.isSupprimee() ? "Supprimee" : "Active";
            String statutColor = a.isAcquittee() ? "#2e7d32"   : a.isSupprimee() ? "#777777"   : "#e65100";

            HBox row = new HBox(12);
            row.setPadding(new Insets(10, 14, 10, 14));
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("alerte-history-item");

            Label iconLbl = new Label(icon);
            iconLbl.setStyle("-fx-font-size: 15px;");

            Label idLbl = new Label("#" + a.getId());
            idLbl.setStyle("-fx-text-fill: #777; -fx-font-size: 11px; -fx-font-weight: bold;");
            idLbl.setPrefWidth(42);

            Label zoneLbl = new Label(z);
            zoneLbl.setStyle("-fx-text-fill: #3B7249; -fx-font-size: 12px; -fx-font-weight: bold;");
            zoneLbl.setPrefWidth(100);

            Label capteurLbl = new Label(capteurId + " (" + capteurType + ")");
            capteurLbl.setStyle("-fx-text-fill: #1565C0; -fx-font-size: 11px;");
            capteurLbl.setPrefWidth(150);

            Label niveauLbl = new Label(a.getNiveau().name().toUpperCase());
            niveauLbl.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 12px; -fx-font-weight: bold;");
            niveauLbl.setPrefWidth(100);

            Label valLbl = new Label(a.getReleve().getValeurAsString());
            valLbl.setStyle("-fx-text-fill: #444; -fx-font-size: 12px;");
            valLbl.setPrefWidth(110);

            Label dateLbl = new Label(a.getDateCreation()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            dateLbl.setStyle("-fx-text-fill: #888; -fx-font-size: 11px;");
            dateLbl.setPrefWidth(120);

            Label statutLbl = new Label(statutText);
            statutLbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: white;" +
                    "-fx-background-color: " + statutColor + "; -fx-padding: 2 7;" +
                    "-fx-background-radius: 6;");

            row.getChildren().addAll(iconLbl, idLbl, zoneLbl, capteurLbl, niveauLbl, valLbl, dateLbl, statutLbl);
            row.setOnMouseClicked(e ->
                    showAlerteActionForm(a, () -> loadHistory(resultArea, null, null, null, null, null)));

            resultArea.getChildren().add(row);
        }
    }

    // =========================================
    // MODALE DE FILTRES
    // =========================================
    private static void showFiltreModal(VBox resultArea) {

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Filtrer l'historique");

        // --- Header ---
        HBox header = new HBox();
        header.setPadding(new Insets(20, 25, 20, 25));
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("form-header");
        Label headerLbl = new Label("🔍  Filtrer l'historique des alertes");
        headerLbl.getStyleClass().add("form-header-title");
        header.getChildren().add(headerLbl);

        // --- Grille ---
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(24, 32, 12, 32));
        grid.setHgap(16);
        grid.setVgap(16);
        ColumnConstraints c0 = new ColumnConstraints(130);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        c1.setFillWidth(true);
        grid.getColumnConstraints().addAll(c0, c1);

        // Zone — ComboBox dynamique (toutes les zones existantes)
        Label zoneLabel = new Label("Zone");
        zoneLabel.getStyleClass().add("form-label");
        ComboBox<String> zoneCombo = new ComboBox<>();
        zoneCombo.getItems().add("Toutes les zones");
        ZoneState.getZones().forEach(z -> zoneCombo.getItems().add(z.getNom()));
        zoneCombo.setValue("Toutes les zones");
        zoneCombo.setMaxWidth(Double.MAX_VALUE);

        // Type capteur
        Label typeCapteurLabel = new Label("Type capteur");
        typeCapteurLabel.getStyleClass().add("form-label");
        ComboBox<String> typeCapteurCombo = new ComboBox<>();
        typeCapteurCombo.getItems().add("Tous");
        for (TypeMesure tm : TypeMesure.values()) {
            typeCapteurCombo.getItems().add(tm.name());
        }
        typeCapteurCombo.setValue("Tous");
        typeCapteurCombo.setMaxWidth(Double.MAX_VALUE);

        // Niveau
        Label niveauLabel = new Label("Niveau");
        niveauLabel.getStyleClass().add("form-label");
        ComboBox<String> niveauCombo = new ComboBox<>();
        niveauCombo.getItems().addAll("Tous", "critique", "avertissement", "normal");
        niveauCombo.setValue("Tous");
        niveauCombo.setMaxWidth(Double.MAX_VALUE);

        // Dates
        Label dateDebutLabel = new Label("Date début");
        dateDebutLabel.getStyleClass().add("form-label");
        DatePicker dateDebut = new DatePicker();
        dateDebut.setMaxWidth(Double.MAX_VALUE);

        Label dateFinLabel = new Label("Date fin");
        dateFinLabel.getStyleClass().add("form-label");
        DatePicker dateFin = new DatePicker();
        dateFin.setMaxWidth(Double.MAX_VALUE);

        grid.add(zoneLabel,         0, 0); grid.add(zoneCombo,        1, 0);
        grid.add(typeCapteurLabel,  0, 1); grid.add(typeCapteurCombo, 1, 1);
        grid.add(niveauLabel,       0, 2); grid.add(niveauCombo,      1, 2);
        grid.add(dateDebutLabel,    0, 3); grid.add(dateDebut,        1, 3);
        grid.add(dateFinLabel,      0, 4); grid.add(dateFin,          1, 4);

        // --- Boutons ---
        Button applyBtn = UIFactory.createActionButton("✅  Appliquer", () -> {
            String zone = zoneCombo.getValue().equals("Toutes les zones")
                    ? null : zoneCombo.getValue();
            TypeMesure tc = typeCapteurCombo.getValue().equals("Tous")
                    ? null : TypeMesure.valueOf(typeCapteurCombo.getValue());
            Gravite niv = niveauCombo.getValue().equals("Tous")
                    ? null : Gravite.valueOf(niveauCombo.getValue());
            LocalDateTime debut = dateDebut.getValue() != null
                    ? dateDebut.getValue().atStartOfDay() : null;
            LocalDateTime fin = dateFin.getValue() != null
                    ? dateFin.getValue().atTime(23, 59, 59) : null;
            loadHistory(resultArea, zone, tc, niv, debut, fin);
            stage.close();
        });

        Button resetBtn = UIFactory.createActionButton("↺  Tout afficher", () -> {
            loadHistory(resultArea, null, null, null, null, null);
            stage.close();
        });
        resetBtn.setStyle(
                "-fx-background-color: #7a7a7a; -fx-text-fill: white;" +
                "-fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 10 25;");

        HBox buttons = new HBox(14, applyBtn, resetBtn);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(10, 32, 24, 32));

        VBox root = new VBox(header, grid, buttons);
        root.getStyleClass().add("form-global");

        Scene scene = new Scene(root, 490, 430);
        scene.getStylesheets().add(PageZone.class.getResource("style.css").toExternalForm());
        stage.setScene(scene);
        stage.setResizable(false);
        stage.showAndWait();
    }
}
