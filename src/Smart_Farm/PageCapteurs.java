package Smart_Farm;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class PageCapteurs {

    private static final String[] TYPE_NAMES  = {"Environnemental", "Sol", "Eau", "Biométrique", "GPS"};
    private static final String[] TYPE_COLORS = {"#4CAF50", "#FF9800", "#2196F3", "#9C27B0", "#F44336"};

    private static Runnable evolutionRefreshCallback;
    public static void notifyEvolutionRefresh() {
        if (evolutionRefreshCallback != null) evolutionRefreshCallback.run();
    }

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
        // STATS + ADD
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
        // DONUT CHART — TYPES
        // =========================
        center.getChildren().add(UIFactory.createAnimatedTitle("📊 Répartition des Capteurs par Type"));
        center.getChildren().add(createCapteurTypeDonut());

        // =========================
        // DASHBOARD PAR ZONE
        // =========================
        center.getChildren().add(UIFactory.createAnimatedTitle("📡 Tableau de Bord par Zone"));

        VBox[] dashboardHolder = {createZoneDashboard()};
        center.getChildren().add(dashboardHolder[0]);

        Timeline dashboardRefresh = new Timeline(
                new KeyFrame(Duration.seconds(2), e -> {
                    int idx = center.getChildren().indexOf(dashboardHolder[0]);
                    dashboardHolder[0] = createZoneDashboard();
                    if (idx >= 0) center.getChildren().set(idx, dashboardHolder[0]);
                })
        );
        dashboardRefresh.setCycleCount(Timeline.INDEFINITE);
        dashboardRefresh.play();

        // =========================
        // GRAPHIQUES ÉVOLUTION
        // =========================
        center.getChildren().add(UIFactory.createAnimatedTitle("📈 Évolution par Zone / Capteur"));
        center.getChildren().add(createEvolutionSearchCard());

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
                UIFactory.createLiveNumberDisplay("Total Capteurs", CapteurState.nbrCapteursProperty(), 200, 90),
                UIFactory.createLiveNumberDisplay("Actifs",         CapteurState.nbrActifsProperty(),   200, 90),
                UIFactory.createLiveNumberDisplay("Suspendus",      CapteurState.nbrSuspendusProp(),    200, 90),
                UIFactory.createLiveNumberDisplay("Défaillants",    CapteurState.nbrDefaillantsProperty(), 200, 90)
        );

        return container;
    }

    // =========================================
    // DONUT CHART — CAPTEURS PAR TYPE
    // =========================================
    private static VBox createCapteurTypeDonut() {

        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.TOP_CENTER);
        card.getStyleClass().add("farm-graph-card");

        List<Capteur> all = CapteurState.getCapteurs();
        if (all.isEmpty()) {
            Label empty = new Label("Aucun capteur — ajoutez des capteurs pour voir les statistiques");
            empty.getStyleClass().add("culture-text");
            card.getChildren().add(empty);
            return card;
        }

        Map<String, Long> byType = all.stream()
                .collect(Collectors.groupingBy(Capteur::getTypeNom, Collectors.counting()));

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        List<String> usedColors = new ArrayList<>();

        for (int i = 0; i < TYPE_NAMES.length; i++) {
            long count = byType.getOrDefault(TYPE_NAMES[i], 0L);
            if (count > 0) {
                pieData.add(new PieChart.Data(TYPE_NAMES[i] + "  (" + count + ")", count));
                usedColors.add(TYPE_COLORS[i]);
            }
        }

        PieChart chart = new PieChart(pieData);
        chart.setLegendVisible(false);
        chart.setLabelsVisible(true);
        chart.setAnimated(false);
        chart.setPrefSize(380, 300);

        // Apply colors after scene renders (nodes are created on layout pass)
        new Timeline(new KeyFrame(Duration.millis(150), e -> {
            for (int i = 0; i < chart.getData().size() && i < usedColors.size(); i++) {
                PieChart.Data d = chart.getData().get(i);
                if (d.getNode() != null) {
                    d.getNode().setStyle("-fx-pie-color: " + usedColors.get(i) + ";");
                }
            }
        })).play();

        // Donut hole overlay
        StackPane donutPane = new StackPane(chart);
        Circle hole = new Circle(75, Color.web("#f4f4f4"));
        hole.setMouseTransparent(true);
        donutPane.getChildren().add(hole);

        // Color legend
        HBox legend = new HBox(20);
        legend.setAlignment(Pos.CENTER);
        for (int i = 0; i < TYPE_NAMES.length; i++) {
            long count = byType.getOrDefault(TYPE_NAMES[i], 0L);
            if (count > 0) {
                Rectangle rect = new Rectangle(14, 14, Color.web(TYPE_COLORS[i]));
                rect.setArcWidth(4);
                rect.setArcHeight(4);
                Label lbl = new Label(TYPE_NAMES[i] + " : " + count);
                lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #333;");
                HBox item = new HBox(6, rect, lbl);
                item.setAlignment(Pos.CENTER_LEFT);
                legend.getChildren().add(item);
            }
        }

        card.getChildren().addAll(donutPane, legend);
        return card;
    }

    // =========================================
    // ÉVOLUTION PAR ZONE / CAPTEUR
    // =========================================
    private static VBox createEvolutionSearchCard() {

        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.TOP_LEFT);
        card.getStyleClass().add("farm-graph-card");

        // --- Search controls ---
        Label zoneLabel = new Label("Zone :");
        zoneLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #3B7249; -fx-font-size: 13px;");

        ComboBox<String> zoneCombo = new ComboBox<>();
        zoneCombo.setPromptText("Sélectionner une zone (optionnel)");
        zoneCombo.setEditable(true);
        zoneCombo.setPrefWidth(240);
        ZoneState.getZones().forEach(z -> zoneCombo.getItems().add(z.getNom()));
        zoneCombo.showingProperty().addListener((obs, was, showing) -> {
            if (showing) {
                String cur = zoneCombo.getEditor().getText();
                zoneCombo.getItems().setAll(
                        ZoneState.getZones().stream().map(z -> z.getNom()).collect(Collectors.toList()));
                zoneCombo.getEditor().setText(cur);
            }
        });

        Label capteurLabel = new Label("Capteur :");
        capteurLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #3B7249; -fx-font-size: 13px;");

        ComboBox<String> capteurCombo = new ComboBox<>();
        capteurCombo.setPromptText("Sélectionner un capteur (optionnel)");
        capteurCombo.setEditable(true);
        capteurCombo.setPrefWidth(240);
        CapteurState.getCapteurs().forEach(c -> capteurCombo.getItems().add(c.getId()));
        capteurCombo.showingProperty().addListener((obs, was, showing) -> {
            if (showing) {
                String cur = capteurCombo.getEditor().getText();
                capteurCombo.getItems().setAll(
                        CapteurState.getCapteurs().stream().map(c -> c.getId()).collect(Collectors.toList()));
                capteurCombo.getEditor().setText(cur);
            }
        });

        Button showBtn = new Button("  Afficher  ");
        showBtn.getStyleClass().add("form-button");

        HBox searchRow = new HBox(12, zoneLabel, zoneCombo, capteurLabel, capteurCombo, showBtn);
        searchRow.setAlignment(Pos.CENTER_LEFT);
        searchRow.setPadding(new Insets(0, 0, 5, 0));

        // --- Hint ---
        Label hint = new Label("Sélectionnez une zone et/ou un capteur, puis cliquez Afficher");
        hint.setStyle("-fx-text-fill: #888; -fx-font-size: 12px;");

        // --- Zone chart ---
        CategoryAxis xZ = new CategoryAxis();
        NumberAxis   yZ = new NumberAxis();
        yZ.setAutoRanging(true);
        LineChart<String, Number> zoneChart = new LineChart<>(xZ, yZ);
        zoneChart.setTitle("Évolution par Zone");
        zoneChart.setLegendVisible(true);
        zoneChart.setAnimated(false);
        zoneChart.setPrefSize(860, 310);
        zoneChart.getStyleClass().add("farm-bar-chart");
        zoneChart.setVisible(false);
        zoneChart.setManaged(false);

        // --- Capteur chart ---
        CategoryAxis xC = new CategoryAxis();
        NumberAxis   yC = new NumberAxis();
        yC.setAutoRanging(true);
        LineChart<String, Number> capteurChart = new LineChart<>(xC, yC);
        capteurChart.setTitle("Évolution par Capteur");
        capteurChart.setLegendVisible(true);
        capteurChart.setAnimated(false);
        capteurChart.setPrefSize(860, 310);
        capteurChart.getStyleClass().add("farm-bar-chart");
        capteurChart.setVisible(false);
        capteurChart.setManaged(false);

        Runnable updateCharts = () -> {
            String zoneName  = zoneCombo.getEditor().getText().trim();
            String capteurId = capteurCombo.getEditor().getText().trim();

            // only refresh if at least one filter is active
            if (zoneName.isEmpty() && capteurId.isEmpty()) return;

            zoneChart.getData().clear();
            capteurChart.getData().clear();
            hint.setVisible(false);
            hint.setManaged(false);

            // -- Zone chart --
            if (!zoneName.isEmpty()) {
                List<Capteur> inZone = CapteurState.getCapteurs().stream()
                        .filter(c -> c.getZoneId().equalsIgnoreCase(zoneName))
                        .collect(Collectors.toList());

                for (Capteur c : inZone) {
                    XYChart.Series<String, Number> s = new XYChart.Series<>();
                    s.setName(c.getId() + " (" + c.getTypeNom() + ")");
                    for (Releve r : c.getHistoriqueReleves()) {
                        if (r instanceof ReleveNumerique rn) {
                            s.getData().add(new XYChart.Data<>(
                                    r.getTimestamp().toLocalTime().toString().substring(0, 8),
                                    rn.getValeur()));
                        }
                    }
                    if (!s.getData().isEmpty()) zoneChart.getData().add(s);
                }
                zoneChart.setTitle("Évolution — Zone : " + zoneName);
                boolean hasZone = !zoneChart.getData().isEmpty();
                zoneChart.setVisible(hasZone);
                zoneChart.setManaged(hasZone);
            } else {
                zoneChart.setVisible(false);
                zoneChart.setManaged(false);
            }

            // -- Capteur chart --
            if (!capteurId.isEmpty()) {
                Optional<Capteur> found = CapteurState.getCapteurs().stream()
                        .filter(c -> c.getId().equalsIgnoreCase(capteurId))
                        .findFirst();

                found.ifPresent(c -> {
                    XYChart.Series<String, Number> s = new XYChart.Series<>();
                    s.setName(c.getId() + " (" + c.getTypeNom() + ")");
                    for (Releve r : c.getHistoriqueReleves()) {
                        if (r instanceof ReleveNumerique rn) {
                            s.getData().add(new XYChart.Data<>(
                                    r.getTimestamp().toLocalTime().toString().substring(0, 8),
                                    rn.getValeur()));
                        }
                    }
                    if (!s.getData().isEmpty()) capteurChart.getData().add(s);
                });

                capteurChart.setTitle("Évolution — Capteur : " + capteurId);
                boolean hasCap = !capteurChart.getData().isEmpty();
                capteurChart.setVisible(hasCap);
                capteurChart.setManaged(hasCap);
            } else {
                capteurChart.setVisible(false);
                capteurChart.setManaged(false);
            }

            if (!zoneChart.isVisible() && !capteurChart.isVisible()) {
                hint.setText("Aucune donnée de relevé disponible pour ce filtre");
                hint.setVisible(true);
                hint.setManaged(true);
            }
        };

        showBtn.setOnAction(e -> {
            // first click with empty combos: show hint
            if (zoneCombo.getEditor().getText().trim().isEmpty()
                    && capteurCombo.getEditor().getText().trim().isEmpty()) {
                hint.setText("Sélectionnez une zone et/ou un capteur, puis cliquez Afficher");
                hint.setVisible(true);
                hint.setManaged(true);
                return;
            }
            updateCharts.run();
        });

        evolutionRefreshCallback = updateCharts;

        card.getChildren().addAll(searchRow, hint, zoneChart, capteurChart);
        return card;
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
    // FORMULAIRE ACTION CAPTEUR — clic sur tableau
    // =========================================
    public static void showCapteurActionForm(Capteur capteur) {

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Capteur : " + capteur.getId());

        VBox root = new VBox(12);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER_LEFT);
        root.getStyleClass().add("form-global");

        Label title = new Label("📡 " + capteur.getId() + "  —  " + capteur.getTypeNom());
        title.getStyleClass().add("form-label");

        Label zoneInfo   = new Label("Zone : " + capteur.getZoneId());
        Label statutInfo = new Label("Statut : " + capteur.getStatut().name());

        // --- Changer statut ---
        Label changeLabel = new Label("Changer Statut :");

        RadioButton actifBtn   = new RadioButton("Actif");
        RadioButton suspendBtn = new RadioButton("Suspendu");
        RadioButton inactifBtn = new RadioButton("Défaillant");

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

        // --- Envoyer relevé ---
        Button sendBtn = UIFactory.createActionButton("📤 Envoyer Relevé", () -> {
            capteur.envoyerReleve();
            CapteurState.updateStats();
            AlerteState.updateStats();
            AlerteState.refreshAlertes();
            notifyEvolutionRefresh();
            stage.close();
        });

        // --- Historique ---
        Label histTitle = new Label("Historique des relevés :");
        histTitle.getStyleClass().add("form-label");

        // Date filter controls
        DatePicker fromPicker = new DatePicker();
        fromPicker.setPromptText("Date début");
        fromPicker.setPrefWidth(148);

        DatePicker toPicker = new DatePicker();
        toPicker.setPromptText("Date fin");
        toPicker.setPrefWidth(148);

        Button filterBtn = new Button("🔍");
        filterBtn.getStyleClass().add("form-button");

        Button resetBtn = new Button("Tout afficher");
        resetBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #3B7249;" +
                          " -fx-font-size: 11px; -fx-cursor: hand;");
        resetBtn.setMinWidth(Region.USE_PREF_SIZE);

        Label arrowLbl = new Label("→");
        arrowLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");

        HBox filterRow = new HBox(8, fromPicker, arrowLbl, toPicker, filterBtn, resetBtn);
        filterRow.setAlignment(Pos.CENTER_LEFT);

        VBox histBox = new VBox(4);
        histBox.setPadding(new Insets(5));

        ScrollPane histScroll = new ScrollPane(histBox);
        histScroll.setFitToWidth(true);
        histScroll.setPrefHeight(260);
        histScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        buildHistContent(histBox, capteur.getHistoriqueReleves(), null, null);

        filterBtn.setOnAction(e -> {
            LocalDateTime from = fromPicker.getValue() != null
                    ? fromPicker.getValue().atStartOfDay() : null;
            LocalDateTime to = toPicker.getValue() != null
                    ? toPicker.getValue().plusDays(1).atStartOfDay() : null;
            buildHistContent(histBox, capteur.getHistoriqueReleves(), from, to);
        });

        resetBtn.setOnAction(e -> {
            fromPicker.setValue(null);
            toPicker.setValue(null);
            buildHistContent(histBox, capteur.getHistoriqueReleves(), null, null);
        });

        root.getChildren().addAll(
                title, zoneInfo, statutInfo,
                new Separator(),
                changeLabel, actifBtn, suspendBtn, inactifBtn, saveStatut,
                new Separator(),
                sendBtn,
                new Separator(),
                histTitle, filterRow, histScroll
        );

        Scene scene = new Scene(root, 490, 680);
        scene.getStylesheets().add(PageZone.class.getResource("style.css").toExternalForm());
        stage.setScene(scene);
        stage.showAndWait();
    }

    // =========================================
    // HELPER — construction du contenu historique
    // =========================================
    private static void buildHistContent(VBox histBox, List<Releve> releves,
                                         LocalDateTime from, LocalDateTime to) {
        histBox.getChildren().clear();

        List<Releve> filtered = releves.stream()
                .filter(r -> (from == null || !r.getTimestamp().isBefore(from))
                          && (to   == null ||  r.getTimestamp().isBefore(to)))
                .sorted(Comparator.comparing(Releve::getTimestamp).reversed())
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            Label none = new Label(from != null || to != null
                    ? "Aucun relevé pour cette période"
                    : "Aucun relevé enregistré");
            none.getStyleClass().add("culture-text");
            histBox.getChildren().add(none);
            return;
        }

        for (Releve r : filtered) {
            String color = switch (r.getNiveau()) {
                case critique      -> "#ff5252";
                case avertissement -> "#ffb300";
                default            -> "#4caf50";
            };
            String time = r.getTimestamp()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            Label rl = new Label("• " + time + "  →  " + r.getValeurAsString()
                    + "  [" + r.getNiveau().name() + "]");
            rl.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 12px;");
            histBox.getChildren().add(rl);
        }
    }
}
