package Smart_Farm;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;

public class PageCapteurs {

    // =========================================
    // PAGE CAPTEURS
    // =========================================
    public static ScrollPane capteurPage() {

        VBox center = new VBox();
        center.setSpacing(30);
        center.setPadding(new Insets(20));
        center.setAlignment(Pos.TOP_CENTER);
        center.setFillWidth(true);

        // =========================
        // STATS + ADD  (une seule ligne cohérente)
        // =========================
        HBox statsHeader = new HBox(20);
        statsHeader.setPadding(new Insets(20));
        statsHeader.setAlignment(Pos.CENTER);
        statsHeader.getChildren().addAll(
                UIFactory.createLiveNumberDisplay("Actifs",      CapteurState.nbrActifsProperty(),      200, 90),
                UIFactory.createLiveNumberDisplay("Suspendus",   CapteurState.nbrSuspendusProp(),       200, 90),
                UIFactory.createLiveNumberDisplay("Défaillants", CapteurState.nbrDefaillantsProperty(), 200, 90),
                UIFactory.createAddCard("Total", CapteurState.nbrCapteursProperty(), "➕ Ajouter Capteur", () -> showAddCapteurForm(), 240, 90)
        );
        UIFactory.expandToFill(statsHeader);
        center.getChildren().add(statsHeader);

        // =========================
        // TABLE
        // =========================
        center.getChildren().add(UIFactory.createAnimatedTitle("📡 Liste des Capteurs"));

        List<String> headers = List.of("ID", "Type", "Zone", "Statut", "Dernier Relevé", "Niveau");

        VBox tableCard = TableFactory.createSearchTableCard(
                headers,
                query -> CapteurState.searchCapteur(query),
                CapteurState::mapCapteur,
                capteur -> showCapteurActionForm(capteur),
                refresh -> CapteurState.setCapteurRefresh(() ->
                        refresh.accept(CapteurState.searchCapteur("")))
        );

        center.getChildren().add(tableCard);

        // =========================
        // DASHBOARD PAR ZONE
        // =========================
        center.getChildren().add(UIFactory.createAnimatedTitle("📡 Tableau de Bord par Zone"));

        VBox[] dashboardHolder = {createZoneDashboard()};
        center.getChildren().add(dashboardHolder[0]);

        // auto-refresh dashboard every 10s
        Timeline dashboardRefresh = new Timeline(
                new KeyFrame(Duration.seconds(10), e -> {
                    int idx = center.getChildren().indexOf(dashboardHolder[0]);
                    dashboardHolder[0] = createZoneDashboard();
                    if (idx >= 0) center.getChildren().set(idx, dashboardHolder[0]);
                })
        );
        dashboardRefresh.setCycleCount(Timeline.INDEFINITE);
        dashboardRefresh.play();

        // =========================
        // GRAPHIQUE ÉVOLUTION
        // =========================
        center.getChildren().add(createReleveLineGraph());

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
    public static HBox capteurStatsCards() {

        HBox container = new HBox();
        container.setSpacing(20);
        container.setPadding(new Insets(20));
        container.setAlignment(Pos.CENTER);

        container.getChildren().addAll(
                UIFactory.createLiveNumberDisplay(
                        "Total Capteurs", CapteurState.nbrCapteursProperty(), 200, 90),
                UIFactory.createLiveNumberDisplay(
                        "Actifs", CapteurState.nbrActifsProperty(), 200, 90),
                UIFactory.createLiveNumberDisplay(
                        "Suspendus", CapteurState.nbrSuspendusProp(), 200, 90),
                UIFactory.createLiveNumberDisplay(
                        "Défaillants", CapteurState.nbrDefaillantsProperty(), 200, 90)
        );

        return container;
    }

    // =========================================
    // DASHBOARD PAR ZONE
    // =========================================
    private static VBox createZoneDashboard() {

        VBox dashboard = new VBox(15);
        dashboard.setPadding(new Insets(15));
        dashboard.getStyleClass().add("culture-list-card");

        List<Capteur> capteurs = CapteurState.getCapteurs();

        Map<String, List<Capteur>> byZone = new LinkedHashMap<>();
        for (Capteur c : capteurs) {
            byZone.computeIfAbsent(c.getZoneId(), k -> new ArrayList<>()).add(c);
        }

        if (byZone.isEmpty()) {
            Label empty = new Label("Aucun capteur configuré — ajoutez un capteur pour voir le tableau de bord");
            empty.getStyleClass().add("culture-text");
            dashboard.getChildren().add(empty);
            return dashboard;
        }

        for (Map.Entry<String, List<Capteur>> entry : byZone.entrySet()) {

            VBox zoneBox = new VBox(8);
            zoneBox.setPadding(new Insets(12));
            zoneBox.getStyleClass().add("culture-zone-box");

            Label zoneTitle = new Label("📍 Zone : " + entry.getKey());
            zoneTitle.getStyleClass().add("culture-zone-title");
            zoneBox.getChildren().add(zoneTitle);

            for (Capteur c : entry.getValue()) {

                HBox row = new HBox(12);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(6));
                row.getStyleClass().add("culture-item");

                List<Releve> hist = c.getHistoriqueReleves();
                String valeur = hist.isEmpty() ? "—" : hist.get(hist.size() - 1).getValeurAsString();
                Gravite niv   = hist.isEmpty() ? Gravite.normal : hist.get(hist.size() - 1).getNiveau();

                String indicateur, color;
                switch (niv) {
                    case critique      -> { indicateur = "🔴 CRITIQUE";      color = "#ff5252"; }
                    case avertissement -> { indicateur = "🟡 AVERTISSEMENT"; color = "#ffb300"; }
                    default            -> { indicateur = "🟢 NORMAL";        color = "#4caf50"; }
                }

                Label idLabel  = new Label("📡 " + c.getId() + "  (" + c.getTypeNom() + ")");
                idLabel.getStyleClass().add("culture-text");
                idLabel.setPrefWidth(200);

                Label valLabel = new Label(valeur);
                valLabel.getStyleClass().add("culture-text");
                valLabel.setPrefWidth(120);

                Label nivLabel = new Label(indicateur);
                nivLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 12px;");

                Label statutLabel = new Label("[" + c.getStatut().name() + "]");
                statutLabel.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 11px;");

                row.getChildren().addAll(idLabel, valLabel, nivLabel, statutLabel);
                zoneBox.getChildren().add(row);
            }

            dashboard.getChildren().add(zoneBox);
        }

        return dashboard;
    }

    // =========================================
    // GRAPHIQUE LIGNE (ÉVOLUTION RELEVÉS)
    // =========================================
    private static VBox createReleveLineGraph() {

        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefSize(900, 420);
        card.getStyleClass().add("farm-graph-card");

        Label title = new Label("📈 Évolution des Relevés");
        title.getStyleClass().add("farm-graph-title");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setAutoRanging(true);
        yAxis.setMinorTickVisible(false);

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setLegendVisible(true);
        chart.setAnimated(false);
        chart.setPrefSize(860, 340);
        chart.getStyleClass().add("farm-bar-chart");

        refreshLineChart(chart);

        Timeline autoRefresh = new Timeline(
                new KeyFrame(Duration.seconds(10), e -> refreshLineChart(chart))
        );
        autoRefresh.setCycleCount(Timeline.INDEFINITE);
        autoRefresh.play();

        card.getChildren().addAll(title, chart);
        return card;
    }

    private static void refreshLineChart(LineChart<String, Number> chart) {

        chart.getData().clear();

        List<Capteur> capteurs = CapteurState.getCapteurs();
        int maxShown = Math.min(capteurs.size(), 4);

        for (int i = 0; i < maxShown; i++) {

            Capteur c = capteurs.get(i);
            if (!(c instanceof CapteurNumerique)) continue;

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(c.getId());

            List<Releve> hist = c.getHistoriqueReleves();
            int start = Math.max(0, hist.size() - 10);

            for (int j = start; j < hist.size(); j++) {
                Releve r = hist.get(j);
                if (r instanceof ReleveNumerique rn) {
                    String timeLabel = r.getTimestamp().toLocalTime().toString().substring(0, 8);
                    series.getData().add(new XYChart.Data<>(timeLabel, rn.getValeur()));
                }
            }

            if (!series.getData().isEmpty()) {
                chart.getData().add(series);
            }
        }
    }

    // =========================================
    // FORMULAIRE AJOUT CAPTEUR
    // =========================================
    public static void showAddCapteurForm() {

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Ajouter Capteur");

        // ---- Header ----
        HBox header = new HBox();
        header.setPadding(new Insets(18, 25, 18, 25));
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("form-header");
        Label headerLbl = new Label("📡  Ajouter un Capteur");
        headerLbl.getStyleClass().add("form-header-title");
        header.getChildren().add(headerLbl);

        // ---- Grid ----
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(22, 25, 22, 25));
        grid.setHgap(15);
        grid.setVgap(14);
        ColumnConstraints c0 = new ColumnConstraints(130);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(javafx.scene.layout.Priority.ALWAYS);
        c1.setFillWidth(true);
        grid.getColumnConstraints().addAll(c0, c1);

        // ID
        Label idLabel = new Label("ID Capteur");
        idLabel.getStyleClass().add("form-label");
        TextField idField = new TextField();
        idField.setPromptText("Ex: TEMP-001");
        idField.setMaxWidth(Double.MAX_VALUE);

        // ZONE
        Label zoneLabel = new Label("Zone");
        zoneLabel.getStyleClass().add("form-label");
        ComboBox<String> zoneCombo = new ComboBox<>();
        ZoneState.getZones().forEach(z -> zoneCombo.getItems().add(z.getNom()));
        zoneCombo.setEditable(true);
        zoneCombo.setPromptText("Ex: ZONE-A");
        zoneCombo.setMaxWidth(Double.MAX_VALUE);

        // TYPE CAPTEUR
        Label typeLabel = new Label("Type Capteur");
        typeLabel.getStyleClass().add("form-label");
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Environnemental", "Sol", "Eau", "Biométrique", "GPS");
        typeCombo.setValue("Environnemental");
        typeCombo.setMaxWidth(Double.MAX_VALUE);

        // TYPE MESURE
        Label mesureLabel = new Label("Type Mesure");
        mesureLabel.getStyleClass().add("form-label");
        ComboBox<TypeMesure> mesureCombo = new ComboBox<>();
        mesureCombo.getItems().addAll(TypeMesure.TEMPERATURE, TypeMesure.HUMIDITE, TypeMesure.PLUVIOMETRIE);
        mesureCombo.setValue(TypeMesure.TEMPERATURE);
        mesureCombo.setMaxWidth(Double.MAX_VALUE);

        // SEUILS
        Label seuilMinLabel = new Label("Seuil Min");
        seuilMinLabel.getStyleClass().add("form-label");
        Spinner<Double> seuilMin = new Spinner<>(0.0, 9999.0, 10.0, 1.0);
        seuilMin.setEditable(true);
        seuilMin.setMaxWidth(Double.MAX_VALUE);

        Label seuilMaxLabel = new Label("Seuil Max");
        seuilMaxLabel.getStyleClass().add("form-label");
        Spinner<Double> seuilMax = new Spinner<>(0.0, 9999.0, 40.0, 1.0);
        seuilMax.setEditable(true);
        seuilMax.setMaxWidth(Double.MAX_VALUE);

        typeCombo.setOnAction(e -> {
            mesureCombo.getItems().clear();
            boolean notNumeric = false;
            switch (typeCombo.getValue()) {
                case "Environnemental" -> mesureCombo.getItems().addAll(
                        TypeMesure.TEMPERATURE, TypeMesure.HUMIDITE, TypeMesure.PLUVIOMETRIE);
                case "Sol" -> mesureCombo.getItems().addAll(
                        TypeMesure.PH_SOL, TypeMesure.HUMIDITE_SOL, TypeMesure.AZOTE);
                case "Eau" -> mesureCombo.getItems().addAll(
                        TypeMesure.TEMPERATURE_EAU, TypeMesure.OXYGENE_DISSOUS, TypeMesure.PH_EAU);
                case "Biométrique" -> { notNumeric = true; }
                case "GPS"         -> { notNumeric = true; }
            }
            mesureCombo.setDisable(notNumeric);
            seuilMin.setDisable(notNumeric);
            seuilMax.setDisable(notNumeric);
            if (!notNumeric && !mesureCombo.getItems().isEmpty())
                mesureCombo.setValue(mesureCombo.getItems().get(0));
        });

        Button validateBtn = new Button("✔  Valider");
        validateBtn.getStyleClass().add("form-button");
        validateBtn.setMaxWidth(Double.MAX_VALUE);

        validateBtn.setOnAction(e -> {
            String id   = idField.getText().trim();
            String zone = zoneCombo.getEditor().getText().trim();

            if (id.isEmpty() || zone.isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "ID et Zone sont obligatoires").showAndWait();
                return;
            }

            double min = seuilMin.getValue();
            double max = seuilMax.getValue();
            if (!seuilMin.isDisabled() && min >= max) {
                new Alert(Alert.AlertType.ERROR, "Seuil Min doit être inférieur au Seuil Max").showAndWait();
                return;
            }

            try {
                Capteur capteur;
                Seuil seuil = seuilMin.isDisabled() ? new Seuil(0, 1) : new Seuil(min, max);
                switch (typeCombo.getValue()) {
                    case "Environnemental" ->
                            capteur = new CapteurEnvironnemental(id, zone, mesureCombo.getValue(), seuil);
                    case "Sol" ->
                            capteur = new CapteurSol(id, zone, mesureCombo.getValue(), seuil);
                    case "Eau" ->
                            capteur = new CapteurEau(id, zone, mesureCombo.getValue(), seuil);
                    case "Biométrique" ->
                            capteur = new CapteurBiometrique(id, zone, new Seuil(36, 40), new Seuil(20, 100));
                    default ->
                            capteur = new CapteurGPS(id, zone);
                }
                CapteurState.addCapteur(capteur);
                stage.close();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Erreur : " + ex.getMessage()).showAndWait();
            }
        });

        grid.add(idLabel,       0, 0); grid.add(idField,    1, 0);
        grid.add(zoneLabel,     0, 1); grid.add(zoneCombo,  1, 1);
        grid.add(typeLabel,     0, 2); grid.add(typeCombo,  1, 2);
        grid.add(mesureLabel,   0, 3); grid.add(mesureCombo,1, 3);
        grid.add(seuilMinLabel, 0, 4); grid.add(seuilMin,   1, 4);
        grid.add(seuilMaxLabel, 0, 5); grid.add(seuilMax,   1, 5);
        grid.add(validateBtn,   1, 6);

        VBox root = new VBox(header, grid);
        root.getStyleClass().add("form-global");

        Scene scene = new Scene(root, 480, 470);
        scene.getStylesheets().add(PageZone.class.getResource("style.css").toExternalForm());
        stage.setScene(scene);
        stage.setResizable(false);
        stage.showAndWait();
    }

    // =========================================
    // FORMULAIRE ACTION CAPTEUR
    // =========================================
    public static void showCapteurActionForm(Capteur capteur) {

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Capteur : " + capteur.getId());

        VBox root = new VBox(12);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER_LEFT);
        root.getStyleClass().add("form-global");

        // TITLE
        Label title = new Label("📡 " + capteur.getId() + "  —  " + capteur.getTypeNom());
        title.getStyleClass().add("form-label");

        Label zoneInfo   = new Label("Zone : " + capteur.getZoneId());
        Label statutInfo = new Label("Statut : " + capteur.getStatut().name());

        // CHANGE STATUT
        Label changeLabel = new Label("Changer Statut :");

        RadioButton actifBtn    = new RadioButton("Actif");
        RadioButton suspendBtn  = new RadioButton("Suspendu");
        RadioButton inactifBtn  = new RadioButton("Défaillant");

        ToggleGroup group = new ToggleGroup();
        actifBtn.setToggleGroup(group);
        suspendBtn.setToggleGroup(group);
        inactifBtn.setToggleGroup(group);

        switch (capteur.getStatut()) {
            case ACTIVE   -> actifBtn.setSelected(true);
            case SUSPENDU -> suspendBtn.setSelected(true);
            default       -> inactifBtn.setSelected(true);
        }

        Button saveStatut = new Button("Valider Statut");
        saveStatut.getStyleClass().add("form-button");
        saveStatut.setOnAction(e -> {
            if (actifBtn.isSelected())       capteur.changerStatut(StatutCapteur.ACTIVE);
            else if (suspendBtn.isSelected()) capteur.changerStatut(StatutCapteur.SUSPENDU);
            else                             capteur.changerStatut(StatutCapteur.INACTIVE);
            CapteurState.updateStats();
            CapteurState.refreshCapteurs();
            stage.close();
        });

        // ENVOYER RELEVE
        Button sendBtn = UIFactory.createActionButton("📤 Envoyer Relevé", () -> {
            capteur.envoyerReleve();
            CapteurState.updateStats();
            AlerteState.updateStats();
            AlerteState.refreshAlertes();
            stage.close();
        });

        // HISTORIQUE
        Label histTitle = new Label("Historique (10 derniers relevés) :");
        histTitle.getStyleClass().add("form-label");

        VBox histBox = new VBox(4);
        histBox.setPadding(new Insets(5));

        List<Releve> hist = capteur.getHistoriqueReleves();
        int start = Math.max(0, hist.size() - 10);
        for (int i = start; i < hist.size(); i++) {
            Releve r = hist.get(i);
            String color = switch (r.getNiveau()) {
                case critique      -> "#ff5252";
                case avertissement -> "#ffb300";
                default            -> "#4caf50";
            };
            String time = r.getTimestamp().toLocalTime().toString().substring(0, 8);
            Label rl = new Label("• " + time + "  →  " + r.getValeurAsString()
                    + "  [" + r.getNiveau().name() + "]");
            rl.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 12px;");
            histBox.getChildren().add(rl);
        }
        if (hist.isEmpty()) {
            Label none = new Label("Aucun relevé enregistré");
            none.getStyleClass().add("culture-text");
            histBox.getChildren().add(none);
        }

        ScrollPane histScroll = new ScrollPane(histBox);
        histScroll.setFitToWidth(true);
        histScroll.setPrefHeight(180);
        histScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        root.getChildren().addAll(
                title, zoneInfo, statutInfo,
                new Separator(),
                changeLabel, actifBtn, suspendBtn, inactifBtn, saveStatut,
                new Separator(),
                sendBtn,
                new Separator(),
                histTitle, histScroll
        );

        Scene scene = new Scene(root, 400, 580);
        scene.getStylesheets().add(PageZone.class.getResource("style.css").toExternalForm());
        stage.setScene(scene);
        stage.showAndWait();
    }
}