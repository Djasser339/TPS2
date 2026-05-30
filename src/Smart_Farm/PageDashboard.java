package Smart_Farm;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PageDashboard {

    public static ScrollPane dashboardPage() {
        VBox center = new VBox();
        center.setSpacing(28);
        center.setPadding(new Insets(20));
        center.setAlignment(Pos.TOP_CENTER);
        center.setFillWidth(true);

        center.getChildren().add(UIFactory.createAnimatedTitle("📊 Vue d'ensemble de la Ferme"));
        center.getChildren().add(buildTopStatsRow());

        center.getChildren().add(UIFactory.createAnimatedTitle("🚨 Statistiques des Alertes"));
        center.getChildren().add(buildAlertStatsRow());

        center.getChildren().add(UIFactory.createAnimatedTitle("🎯 Répartition par Catégorie"));
        center.getChildren().add(buildDonutChartsRow());

        center.getChildren().add(UIFactory.createAnimatedTitle("📈 Analyses Détaillées"));
        center.getChildren().add(buildBarChartsRow());

        center.getChildren().add(UIFactory.createAnimatedTitle("🌾 Statistiques Ferme Supplémentaires"));
        center.getChildren().add(buildFarmExtraStatsRow());
        center.getChildren().add(buildFarmExtraBarRow());

        center.getChildren().add(UIFactory.createAnimatedTitle("🔔 Dernières Alertes Actives"));
        center.getChildren().add(buildRecentAlertsCard());

        ScrollPane scroll = new ScrollPane(center);
        scroll.setFitToWidth(true);
        scroll.setPannable(true);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return scroll;
    }

    // =========================================
    // TOP STATS ROW — muted palette
    // =========================================
    private static HBox buildTopStatsRow() {
        HBox row = new HBox(14);
        row.setPadding(new Insets(0, 10, 0, 10));
        row.setAlignment(Pos.CENTER);
        row.getChildren().addAll(
            buildStatCard("🌍 Zones",    ZoneState.nbrZonesProperty(),       "#455A64", "#ECEFF1"),
            buildStatCard("🐄 Animaux",  AnimalState.nbrAnimalsProperty(),    "#388E3C", "#F1F8E9"),
            buildStatCard("🌱 Cultures", FarmState.nbrCulturesProperty(),     "#5D4037", "#EFEBE9"),
            buildStatCard("📡 Capteurs", CapteurState.nbrCapteursProperty(),  "#0277BD", "#E1F5FE"),
            buildStatCard("🚨 Alertes",  AlerteState.nbrAlertesProperty(),    "#C62828", "#FFEBEE")
        );
        UIFactory.expandToFill(row);
        return row;
    }

    // =========================================
    // ALERT STATS ROW — fixed emojis
    // =========================================
    private static HBox buildAlertStatsRow() {
        HBox row = new HBox(14);
        row.setPadding(new Insets(0, 10, 0, 10));
        row.setAlignment(Pos.CENTER);
        row.getChildren().addAll(
            buildStatCard("🔔 Actives",           AlerteState.nbrAlertesProperty(),        "#546E7A", "#ECEFF1"),
            buildStatCard("🔴 Critiques",          AlerteState.nbrCritiquesProperty(),      "#C62828", "#FFEBEE"),
            buildStatCard("🟡 Avertissements",     AlerteState.nbrAvertissementsProperty(), "#F57F17", "#FFF8E1"),
            buildStatCard("✅ Acquittées (total)", AlerteState.nbrAcquitteesProperty(),     "#2E7D32", "#E8F5E9"),
            buildStatCard("❌ Supprimées (total)", AlerteState.nbrSupprimeesProp(),         "#616161", "#F5F5F5")
        );
        UIFactory.expandToFill(row);
        return row;
    }

    // =========================================
    // STAT CARD
    // =========================================
    private static VBox buildStatCard(String title,
                                      javafx.beans.value.ObservableNumberValue value,
                                      String textColor, String bgColor) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(16, 18, 16, 18));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                "-fx-background-radius: 14;" +
                "-fx-border-radius: 14;" +
                "-fx-border-color: " + textColor + "33;" +
                "-fx-border-width: 1.5;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0.1, 0, 3);"
        );
        HBox.setHgrow(card, Priority.ALWAYS);

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + textColor + ";");
        titleLbl.setWrapText(true);

        Label valueLbl = new Label();
        valueLbl.setStyle("-fx-font-size: 34px; -fx-font-weight: bold; -fx-text-fill: " + textColor + ";");
        valueLbl.textProperty().bind(
                Bindings.createStringBinding(() -> String.valueOf(value.getValue()), value)
        );

        card.getChildren().addAll(titleLbl, valueLbl);
        UIFactory.applyHoverEffect(card);
        return card;
    }

    // =========================================
    // DONUT CHARTS ROW
    // =========================================
    private static HBox buildDonutChartsRow() {
        HBox row = new HBox(18);
        row.setPadding(new Insets(0, 10, 0, 10));
        row.setAlignment(Pos.CENTER);

        VBox zonesCard   = buildChartCard("🌍 Zones par Type",     buildZonesDonut());
        VBox animauxCard = buildChartCard("🐄 Animaux par Type",   buildAnimauxDonut());
        VBox alerteCard  = buildChartCard("🚨 Alertes par Statut", buildAlerteDonut());

        HBox.setHgrow(zonesCard,   Priority.ALWAYS);
        HBox.setHgrow(animauxCard, Priority.ALWAYS);
        HBox.setHgrow(alerteCard,  Priority.ALWAYS);

        row.getChildren().addAll(zonesCard, animauxCard, alerteCard);
        return row;
    }

    // =========================================
    // BAR CHARTS ROW
    // =========================================
    private static HBox buildBarChartsRow() {
        HBox row = new HBox(18);
        row.setPadding(new Insets(0, 10, 0, 10));
        row.setAlignment(Pos.CENTER);

        VBox culturesCard = buildChartCard("🌱 Cultures par Famille", buildCulturesBar());
        VBox capteursCard = buildChartCard("📡 Capteurs par Statut",  buildCapteursBar());
        VBox alertesCard  = buildChartCard("📊 Résumé Alertes",       buildAlertesBar());

        HBox.setHgrow(culturesCard, Priority.ALWAYS);
        HBox.setHgrow(capteursCard, Priority.ALWAYS);
        HBox.setHgrow(alertesCard,  Priority.ALWAYS);

        row.getChildren().addAll(culturesCard, capteursCard, alertesCard);
        return row;
    }

    // =========================================
    // EXTRA FARM STATS ROW
    // =========================================
    private static HBox buildFarmExtraStatsRow() {
        HBox row = new HBox(14);
        row.setPadding(new Insets(0, 10, 0, 10));
        row.setAlignment(Pos.CENTER);
        row.getChildren().addAll(
            buildStatCard("🌿 Zones Culture",  ZoneState.nbrZoneCultureProperty(),  "#388E3C", "#F1F8E9"),
            buildStatCard("🐑 Zones Elevage",  ZoneState.nbrZoneElevageProperty(),  "#6D4C41", "#EFEBE9"),
            buildStatCard("🐟 Zones Aquacole", ZoneState.nbrZoneAquacoleProperty(), "#0277BD", "#E1F5FE"),
            buildStatCard("🐄 Ruminants",      AnimalState.nbrRuminantsProperty(),  "#795548", "#EFEBE9"),
            buildStatCard("🐔 Volaille",       AnimalState.nbrVolailleProperty(),   "#F57F17", "#FFF8E1"),
            buildStatCard("🐠 Aquacole",       AnimalState.nbrAquacoleProperty(),   "#0288D1", "#E1F5FE")
        );
        UIFactory.expandToFill(row);
        return row;
    }

    // =========================================
    // EXTRA BAR CHARTS ROW
    // =========================================
    private static HBox buildFarmExtraBarRow() {
        HBox row = new HBox(18);
        row.setPadding(new Insets(0, 10, 0, 10));
        row.setAlignment(Pos.CENTER);

        VBox stagesCard = buildChartCard("🌿 Cultures par Stade de Croissance", buildCultureStagesBar());
        VBox zonesCard  = buildChartCard("📍 Entités par Type de Zone",         buildZoneEntitiesBar());

        HBox.setHgrow(stagesCard, Priority.ALWAYS);
        HBox.setHgrow(zonesCard,  Priority.ALWAYS);

        row.getChildren().addAll(stagesCard, zonesCard);
        return row;
    }

    // =========================================
    // CARD WRAPPER
    // =========================================
    private static VBox buildChartCard(String title, javafx.scene.Node chart) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(18));
        card.setAlignment(Pos.TOP_CENTER);
        card.getStyleClass().add("farm-graph-card");

        Label titleLbl = new Label(title);
        titleLbl.getStyleClass().add("farm-graph-title");

        card.getChildren().addAll(titleLbl, chart);
        return card;
    }

    // =========================================
    // ZONES DONUT — colored slices + circular legend
    // =========================================
    private static VBox buildZonesDonut() {
        String[] allColors = {"#4CAF50", "#FF9800", "#2196F3"};
        String[] labels    = {"Culture", "Elevage", "Aquacole"};

        Label[] countLabels = new Label[labels.length];

        PieChart chart = new PieChart();
        chart.setLegendVisible(false);
        chart.setAnimated(true);
        chart.setPrefSize(260, 220);
        chart.setLabelsVisible(false);

        List<String> appliedColors = new ArrayList<>();

        Runnable refresh = () -> {
            chart.getData().clear();
            appliedColors.clear();
            int[] counts = {
                ZoneState.nbrZoneCultureProperty().get(),
                ZoneState.nbrZoneElevageProperty().get(),
                ZoneState.nbrZoneAquacoleProperty().get()
            };
            for (int i = 0; i < labels.length; i++) {
                if (countLabels[i] != null) countLabels[i].setText(labels[i] + "  " + counts[i]);
                if (counts[i] > 0) {
                    chart.getData().add(new PieChart.Data(labels[i], counts[i]));
                    appliedColors.add(allColors[i]);
                }
            }
            if (chart.getData().isEmpty()) chart.getData().add(new PieChart.Data("Aucune zone", 1));
            new Timeline(new KeyFrame(Duration.millis(150), e -> {
                for (int i = 0; i < chart.getData().size() && i < appliedColors.size(); i++) {
                    PieChart.Data d = chart.getData().get(i);
                    if (d.getNode() != null)
                        d.getNode().setStyle("-fx-pie-color: " + appliedColors.get(i) + ";");
                }
            })).play();
        };

        HBox legend = buildCircleLegend(labels, allColors, countLabels);
        refresh.run();
        ZoneState.nbrZonesProperty().addListener((o, v, n) -> Platform.runLater(refresh));

        VBox box = new VBox(6, wrapDonut(chart), legend);
        box.setAlignment(Pos.TOP_CENTER);
        return box;
    }

    // =========================================
    // ANIMAUX DONUT — colored slices + circular legend
    // =========================================
    private static VBox buildAnimauxDonut() {
        String[] allColors = {"#8D6E63", "#FFCA28", "#42A5F5"};
        String[] labels    = {"Ruminants", "Volaille", "Aquacole"};

        Label[] countLabels = new Label[labels.length];

        PieChart chart = new PieChart();
        chart.setLegendVisible(false);
        chart.setAnimated(true);
        chart.setPrefSize(260, 220);
        chart.setLabelsVisible(false);

        List<String> appliedColors = new ArrayList<>();

        Runnable refresh = () -> {
            chart.getData().clear();
            appliedColors.clear();
            int[] counts = {
                AnimalState.nbrRuminantsProperty().get(),
                AnimalState.nbrVolailleProperty().get(),
                AnimalState.nbrAquacoleProperty().get()
            };
            for (int i = 0; i < labels.length; i++) {
                if (countLabels[i] != null) countLabels[i].setText(labels[i] + "  " + counts[i]);
                if (counts[i] > 0) {
                    chart.getData().add(new PieChart.Data(labels[i], counts[i]));
                    appliedColors.add(allColors[i]);
                }
            }
            if (chart.getData().isEmpty()) chart.getData().add(new PieChart.Data("Aucun animal", 1));
            new Timeline(new KeyFrame(Duration.millis(150), e -> {
                for (int i = 0; i < chart.getData().size() && i < appliedColors.size(); i++) {
                    PieChart.Data d = chart.getData().get(i);
                    if (d.getNode() != null)
                        d.getNode().setStyle("-fx-pie-color: " + appliedColors.get(i) + ";");
                }
            })).play();
        };

        HBox legend = buildCircleLegend(labels, allColors, countLabels);
        refresh.run();
        AnimalState.nbrAnimalsProperty().addListener((o, v, n) -> Platform.runLater(refresh));

        VBox box = new VBox(6, wrapDonut(chart), legend);
        box.setAlignment(Pos.TOP_CENTER);
        return box;
    }

    // =========================================
    // ALERTES DONUT — colored slices + circular legend
    // =========================================
    private static VBox buildAlerteDonut() {
        String[] allColors = {"#c62828", "#e65100", "#2e7d32", "#9e9e9e"};
        String[] labels    = {"Critiques", "Avertissements", "Acquittées", "Supprimées"};

        Label[] countLabels = new Label[labels.length];

        PieChart chart = new PieChart();
        chart.setLegendVisible(false);
        chart.setAnimated(true);
        chart.setPrefSize(260, 220);
        chart.setLabelsVisible(false);

        List<String> appliedColors = new ArrayList<>();

        Runnable refresh = () -> {
            chart.getData().clear();
            appliedColors.clear();
            List<Alerte> all = GestionnaireCapteursAlertes.getInstance()
                    .filtrerAlertes(null, null, null, null, null);
            long[] counts = {
                all.stream().filter(a -> !a.isAcquittee() && !a.isSupprimee() && a.getNiveau() == Gravite.critique).count(),
                all.stream().filter(a -> !a.isAcquittee() && !a.isSupprimee() && a.getNiveau() == Gravite.avertissement).count(),
                all.stream().filter(Alerte::isAcquittee).count(),
                all.stream().filter(Alerte::isSupprimee).count()
            };
            for (int i = 0; i < labels.length; i++) {
                if (countLabels[i] != null) countLabels[i].setText(labels[i] + "  " + counts[i]);
                if (counts[i] > 0) {
                    chart.getData().add(new PieChart.Data(labels[i], counts[i]));
                    appliedColors.add(allColors[i]);
                }
            }
            if (chart.getData().isEmpty()) chart.getData().add(new PieChart.Data("Aucune alerte", 1));
            new Timeline(new KeyFrame(Duration.millis(150), e -> {
                for (int i = 0; i < chart.getData().size() && i < appliedColors.size(); i++) {
                    PieChart.Data d = chart.getData().get(i);
                    if (d.getNode() != null)
                        d.getNode().setStyle("-fx-pie-color: " + appliedColors.get(i) + ";");
                }
            })).play();
        };

        HBox legend = buildCircleLegend(labels, allColors, countLabels);
        refresh.run();
        AlerteState.nbrAlertesProperty().addListener((o, v, n)    -> Platform.runLater(refresh));
        AlerteState.nbrAcquitteesProperty().addListener((o, v, n) -> Platform.runLater(refresh));
        AlerteState.nbrSupprimeesProp().addListener((o, v, n)     -> Platform.runLater(refresh));

        VBox box = new VBox(6, wrapDonut(chart), legend);
        box.setAlignment(Pos.TOP_CENTER);
        return box;
    }

    // =========================================
    // CIRCULAR LEGEND (with live count labels)
    // =========================================
    private static HBox buildCircleLegend(String[] labels, String[] colors, Label[] outCountLabels) {
        HBox legend = new HBox(14);
        legend.setAlignment(Pos.CENTER);
        for (int i = 0; i < labels.length; i++) {
            Circle dot = new Circle(6, Color.web(colors[i]));
            Label lbl = new Label(labels[i] + "  0");
            lbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #555;");
            outCountLabels[i] = lbl;
            HBox item = new HBox(5, dot, lbl);
            item.setAlignment(Pos.CENTER_LEFT);
            legend.getChildren().add(item);
        }
        return legend;
    }

    // =========================================
    // DONUT HOLE OVERLAY
    // =========================================
    private static StackPane wrapDonut(PieChart chart) {
        Circle hole = new Circle(65, Color.web("#f4f7f2"));
        hole.setMouseTransparent(true);
        return new StackPane(chart, hole);
    }

    // =========================================
    // CULTURES BAR CHART
    // =========================================
    private static BarChart<String, Number> buildCulturesBar() {
        BarChart<String, Number> chart = makeBarChart();
        chart.setPrefHeight(200);

        Runnable refresh = () -> {
            chart.getData().clear();
            XYChart.Series<String, Number> s = new XYChart.Series<>();
            s.getData().add(new XYChart.Data<>("Céréale", FarmState.nbrCerealProperty().get()));
            s.getData().add(new XYChart.Data<>("Légume",  FarmState.nbrLegumeProperty().get()));
            s.getData().add(new XYChart.Data<>("Fruit",   FarmState.nbrFruitProperty().get()));
            chart.getData().add(s);
            Platform.runLater(() -> colorBars(s, "Céréale", "#f9a825", "Légume", "#388e3c", "#e53935"));
        };

        refresh.run();
        FarmState.nbrCulturesProperty().addListener((o, v, n) -> Platform.runLater(refresh));
        return chart;
    }

    // =========================================
    // CAPTEURS BAR CHART
    // =========================================
    private static BarChart<String, Number> buildCapteursBar() {
        BarChart<String, Number> chart = makeBarChart();
        chart.setPrefHeight(200);

        Runnable refresh = () -> {
            chart.getData().clear();
            XYChart.Series<String, Number> s = new XYChart.Series<>();
            s.getData().add(new XYChart.Data<>("Actifs",      CapteurState.nbrActifsProperty().get()));
            s.getData().add(new XYChart.Data<>("Suspendus",   CapteurState.nbrSuspendusProp().get()));
            s.getData().add(new XYChart.Data<>("Défaillants", CapteurState.nbrDefaillantsProperty().get()));
            chart.getData().add(s);
            Platform.runLater(() -> colorBars(s, "Actifs", "#1565c0", "Suspendus", "#f57f17", "#c62828"));
        };

        refresh.run();
        CapteurState.nbrCapteursProperty().addListener((o, v, n) -> Platform.runLater(refresh));
        return chart;
    }

    // =========================================
    // ALERTES SUMMARY BAR CHART
    // =========================================
    private static BarChart<String, Number> buildAlertesBar() {
        BarChart<String, Number> chart = makeBarChart();
        chart.setPrefHeight(200);

        Runnable refresh = () -> {
            chart.getData().clear();
            XYChart.Series<String, Number> s = new XYChart.Series<>();
            s.getData().add(new XYChart.Data<>("Actives",    AlerteState.nbrAlertesProperty().get()));
            s.getData().add(new XYChart.Data<>("Critiques",  AlerteState.nbrCritiquesProperty().get()));
            s.getData().add(new XYChart.Data<>("Avert.",     AlerteState.nbrAvertissementsProperty().get()));
            s.getData().add(new XYChart.Data<>("Acquittées", AlerteState.nbrAcquitteesProperty().get()));
            s.getData().add(new XYChart.Data<>("Supprimées", AlerteState.nbrSupprimeesProp().get()));
            chart.getData().add(s);
            Platform.runLater(() -> {
                for (XYChart.Data<String, Number> d : s.getData()) {
                    if (d.getNode() == null) continue;
                    String color = switch (d.getXValue()) {
                        case "Critiques"  -> "#c62828";
                        case "Avert."     -> "#e65100";
                        case "Acquittées" -> "#2e7d32";
                        case "Supprimées" -> "#9e9e9e";
                        default           -> "#546e7a";
                    };
                    d.getNode().setStyle("-fx-bar-fill: " + color + "; -fx-background-radius: 8 8 0 0;");
                }
            });
        };

        refresh.run();
        AlerteState.nbrAlertesProperty().addListener((o, v, n)    -> Platform.runLater(refresh));
        AlerteState.nbrAcquitteesProperty().addListener((o, v, n) -> Platform.runLater(refresh));
        AlerteState.nbrSupprimeesProp().addListener((o, v, n)     -> Platform.runLater(refresh));
        return chart;
    }

    // =========================================
    // CULTURE STAGES BAR
    // =========================================
    private static BarChart<String, Number> buildCultureStagesBar() {
        BarChart<String, Number> chart = makeBarChart();
        chart.setPrefHeight(200);

        String[] stageColors = {"#81C784", "#AED581", "#FFF176", "#FFD54F", "#BCAAA4"};

        Runnable refresh = () -> {
            chart.getData().clear();
            List<Culture> cultures = FarmState.getCultures();
            XYChart.Series<String, Number> s = new XYChart.Series<>();
            for (StadeCroissance stage : StadeCroissance.values()) {
                long count = cultures.stream()
                        .filter(c -> c.getStadeCroissance() == stage)
                        .count();
                s.getData().add(new XYChart.Data<>(stage.name(), (int) count));
            }
            chart.getData().add(s);
            Platform.runLater(() -> {
                List<XYChart.Data<String, Number>> data = s.getData();
                for (int i = 0; i < data.size() && i < stageColors.length; i++) {
                    if (data.get(i).getNode() != null)
                        data.get(i).getNode().setStyle(
                            "-fx-bar-fill: " + stageColors[i] + "; -fx-background-radius: 8 8 0 0;");
                }
            });
        };

        refresh.run();
        FarmState.nbrCulturesProperty().addListener((o, v, n) -> Platform.runLater(refresh));
        return chart;
    }

    // =========================================
    // ZONE ENTITIES BAR
    // =========================================
    private static BarChart<String, Number> buildZoneEntitiesBar() {
        BarChart<String, Number> chart = makeBarChart();
        chart.setPrefHeight(200);

        Runnable refresh = () -> {
            chart.getData().clear();
            XYChart.Series<String, Number> s = new XYChart.Series<>();
            int totalCulture = ZoneState.getZones().stream()
                    .filter(z -> z instanceof ZoneCulture)
                    .mapToInt(z -> z.getNbrEntite()).sum();
            int totalElevage = ZoneState.getZones().stream()
                    .filter(z -> z instanceof ZoneElevage)
                    .mapToInt(z -> z.getNbrEntite()).sum();
            int totalAquacole = ZoneState.getZones().stream()
                    .filter(z -> z instanceof ZoneAquacole)
                    .mapToInt(z -> z.getNbrEntite()).sum();
            s.getData().add(new XYChart.Data<>("Culture",  totalCulture));
            s.getData().add(new XYChart.Data<>("Elevage",  totalElevage));
            s.getData().add(new XYChart.Data<>("Aquacole", totalAquacole));
            chart.getData().add(s);
            Platform.runLater(() -> colorBars(s, "Culture", "#4CAF50", "Elevage", "#FF9800", "#2196F3"));
        };

        refresh.run();
        ZoneState.nbrZonesProperty().addListener((o, v, n) -> Platform.runLater(refresh));
        return chart;
    }

    // =========================================
    // RECENT ALERTS MINI LIST
    // =========================================
    private static VBox buildRecentAlertsCard() {
        VBox card = new VBox(8);
        card.setPadding(new Insets(18));
        card.getStyleClass().add("culture-list-card");

        VBox listBox = new VBox(6);

        Runnable refresh = () -> {
            listBox.getChildren().clear();
            List<Alerte> recent = AlerteState.getAlertesActives()
                    .stream()
                    .sorted((a1, a2) -> a2.getDateCreation().compareTo(a1.getDateCreation()))
                    .limit(6).collect(Collectors.toList());

            if (recent.isEmpty()) {
                Label none = new Label("✅ Aucune alerte active");
                none.setStyle("-fx-text-fill: #4caf50; -fx-font-size: 13px; -fx-padding: 10 0;");
                listBox.getChildren().add(none);
                return;
            }

            GestionnaireCapteursAlertes g = GestionnaireCapteursAlertes.getInstance();
            for (Alerte a : recent) {
                Capteur cap = g.getCapteurById(a.getReleve().getIdCapteur());
                String zone = cap != null ? cap.getZoneId() : "?";
                String type = cap != null ? cap.getTypeNom() : "?";

                String color = switch (a.getNiveau()) {
                    case critique      -> "#c62828";
                    case avertissement -> "#e65100";
                    default            -> "#2e7d32";
                };
                String bg = switch (a.getNiveau()) {
                    case critique      -> "#ffebee";
                    case avertissement -> "#fff3e0";
                    default            -> "#e8f5e9";
                };
                String icon = switch (a.getNiveau()) {
                    case critique      -> "🔴";
                    case avertissement -> "🟡";
                    default            -> "🟢";
                };

                HBox row = new HBox(12);
                row.setPadding(new Insets(9, 14, 9, 14));
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 10;");

                Label iconLbl = new Label(icon);
                iconLbl.setStyle("-fx-font-size: 15px;");

                Label infoLbl = new Label(
                        a.getNiveau().name().toUpperCase()
                        + "  —  Zone: " + zone
                        + "  |  " + type
                        + "  |  " + a.getReleve().getValeurAsString()
                        + "  |  " + a.getDateCreation()
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd  HH:mm"))
                );
                infoLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

                row.getChildren().addAll(iconLbl, infoLbl);
                listBox.getChildren().add(row);
            }
        };

        refresh.run();
        AlerteState.nbrAlertesProperty().addListener((o, v, n) -> Platform.runLater(refresh));

        card.getChildren().add(listBox);
        return card;
    }

    // =========================================
    // HELPERS
    // =========================================
    private static BarChart<String, Number> makeBarChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setAutoRanging(true);
        yAxis.setMinorTickVisible(false);
        yAxis.setLabel("Nombre");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.getStyleClass().add("farm-bar-chart");
        chart.setCategoryGap(28);
        chart.setBarGap(5);
        return chart;
    }

    private static void colorBars(XYChart.Series<String, Number> series,
                                   String key1, String color1,
                                   String key2, String color2,
                                   String defaultColor) {
        for (XYChart.Data<String, Number> d : series.getData()) {
            if (d.getNode() == null) continue;
            String color = d.getXValue().equals(key1) ? color1
                         : d.getXValue().equals(key2) ? color2
                         : defaultColor;
            d.getNode().setStyle("-fx-bar-fill: " + color + "; -fx-background-radius: 8 8 0 0;");
        }
    }
}
