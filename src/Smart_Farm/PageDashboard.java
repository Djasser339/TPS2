package Smart_Farm;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;

import java.time.format.DateTimeFormatter;
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

        center.getChildren().add(UIFactory.createAnimatedTitle("🍩 Répartition par Catégorie"));
        center.getChildren().add(buildDonutChartsRow());

        center.getChildren().add(UIFactory.createAnimatedTitle("📈 Analyses Détaillées"));
        center.getChildren().add(buildBarChartsRow());

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
    // TOP STATS ROW — farm-wide counts
    // =========================================
    private static HBox buildTopStatsRow() {
        HBox row = new HBox(14);
        row.setPadding(new Insets(0, 10, 0, 10));
        row.setAlignment(Pos.CENTER);

        row.getChildren().addAll(
                buildStatCard("🌍 Zones",    ZoneState.nbrZonesProperty(),       "#1565C0", "#E3F2FD"),
                buildStatCard("🐄 Animaux",  AnimalState.nbrAnimalsProperty(),    "#2E7D32", "#E8F5E9"),
                buildStatCard("🌱 Cultures", FarmState.nbrCulturesProperty(),     "#6A1B9A", "#F3E5F5"),
                buildStatCard("📡 Capteurs", CapteurState.nbrCapteursProperty(),  "#E65100", "#FFF3E0"),
                buildStatCard("🚨 Alertes",  AlerteState.nbrAlertesProperty(),    "#B71C1C", "#FFEBEE")
        );

        UIFactory.expandToFill(row);
        return row;
    }

    // =========================================
    // ALERT STATS ROW — all statuses
    // =========================================
    private static HBox buildAlertStatsRow() {
        HBox row = new HBox(14);
        row.setPadding(new Insets(0, 10, 0, 10));
        row.setAlignment(Pos.CENTER);

        row.getChildren().addAll(
                buildStatCard("⚠️ Actives",         AlerteState.nbrAlertesProperty(),        "#1565C0", "#E3F2FD"),
                buildStatCard("🔴 Critiques",        AlerteState.nbrCritiquesProperty(),      "#B71C1C", "#FFEBEE"),
                buildStatCard("🟡 Avertissements",   AlerteState.nbrAvertissementsProperty(), "#E65100", "#FFF3E0"),
                buildStatCard("✅ Acquittées (total)", AlerteState.nbrAcquitteesProperty(),   "#2E7D32", "#E8F5E9"),
                buildStatCard("🗑 Supprimées (total)", AlerteState.nbrSupprimeesProp(),       "#757575", "#F5F5F5")
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
                "-fx-border-color: " + textColor + "44;" +
                "-fx-border-width: 1.5;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 10, 0.1, 0, 3);"
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

        VBox culturesCard  = buildChartCard("🌱 Cultures par Famille", buildCulturesBar());
        VBox capteursCard  = buildChartCard("📡 Capteurs par Statut",  buildCapteursBar());
        VBox alertesCard   = buildChartCard("📊 Résumé Alertes",        buildAlertesBar());

        HBox.setHgrow(culturesCard, Priority.ALWAYS);
        HBox.setHgrow(capteursCard, Priority.ALWAYS);
        HBox.setHgrow(alertesCard,  Priority.ALWAYS);

        row.getChildren().addAll(culturesCard, capteursCard, alertesCard);
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
    // ZONES DONUT
    // =========================================
    private static StackPane buildZonesDonut() {
        PieChart chart = new PieChart();
        chart.setLegendVisible(true);
        chart.setAnimated(true);
        chart.setPrefSize(270, 230);
        chart.setLabelsVisible(false);

        Runnable refresh = () -> {
            chart.getData().clear();
            int culture  = ZoneState.nbrZoneCultureProperty().get();
            int elevage  = ZoneState.nbrZoneElevageProperty().get();
            int aquacole = ZoneState.nbrZoneAquacoleProperty().get();
            if (culture  > 0) chart.getData().add(new PieChart.Data("Culture ("  + culture  + ")", culture));
            if (elevage  > 0) chart.getData().add(new PieChart.Data("Elevage ("  + elevage  + ")", elevage));
            if (aquacole > 0) chart.getData().add(new PieChart.Data("Aquacole (" + aquacole + ")", aquacole));
            if (chart.getData().isEmpty()) chart.getData().add(new PieChart.Data("Aucune zone", 1));
        };

        refresh.run();
        ZoneState.nbrZonesProperty().addListener((o, v, n) -> Platform.runLater(refresh));

        return wrapDonut(chart);
    }

    // =========================================
    // ANIMAUX DONUT
    // =========================================
    private static StackPane buildAnimauxDonut() {
        PieChart chart = new PieChart();
        chart.setLegendVisible(true);
        chart.setAnimated(true);
        chart.setPrefSize(270, 230);
        chart.setLabelsVisible(false);

        Runnable refresh = () -> {
            chart.getData().clear();
            int ruminants = AnimalState.nbrRuminantsProperty().get();
            int volaille  = AnimalState.nbrVolailleProperty().get();
            int aquacole  = AnimalState.nbrAquacoleProperty().get();
            if (ruminants > 0) chart.getData().add(new PieChart.Data("Ruminants (" + ruminants + ")", ruminants));
            if (volaille  > 0) chart.getData().add(new PieChart.Data("Volaille ("  + volaille  + ")", volaille));
            if (aquacole  > 0) chart.getData().add(new PieChart.Data("Aquacole ("  + aquacole  + ")", aquacole));
            if (chart.getData().isEmpty()) chart.getData().add(new PieChart.Data("Aucun animal", 1));
        };

        refresh.run();
        AnimalState.nbrAnimalsProperty().addListener((o, v, n) -> Platform.runLater(refresh));

        return wrapDonut(chart);
    }

    // =========================================
    // ALERTES DONUT — all statuses
    // =========================================
    private static StackPane buildAlerteDonut() {
        PieChart chart = new PieChart();
        chart.setLegendVisible(true);
        chart.setAnimated(true);
        chart.setPrefSize(270, 230);
        chart.setLabelsVisible(false);

        Runnable refresh = () -> {
            chart.getData().clear();
            List<Alerte> all = GestionnaireCapteursAlertes.getInstance()
                    .filtrerAlertes(null, null, null, null, null);
            long critiques      = all.stream().filter(a -> !a.isAcquittee() && !a.isSupprimee() && a.getNiveau() == Gravite.critique).count();
            long avertissements = all.stream().filter(a -> !a.isAcquittee() && !a.isSupprimee() && a.getNiveau() == Gravite.avertissement).count();
            long acquittees     = all.stream().filter(Alerte::isAcquittee).count();
            long supprimees     = all.stream().filter(Alerte::isSupprimee).count();

            if (critiques      > 0) chart.getData().add(new PieChart.Data("Critiques ("       + critiques      + ")", critiques));
            if (avertissements > 0) chart.getData().add(new PieChart.Data("Avertissements ("  + avertissements + ")", avertissements));
            if (acquittees     > 0) chart.getData().add(new PieChart.Data("Acquittées ("      + acquittees     + ")", acquittees));
            if (supprimees     > 0) chart.getData().add(new PieChart.Data("Supprimées ("      + supprimees     + ")", supprimees));
            if (chart.getData().isEmpty()) chart.getData().add(new PieChart.Data("Aucune alerte", 1));

            Platform.runLater(() -> {
                for (PieChart.Data d : chart.getData()) {
                    if (d.getNode() == null) continue;
                    String color = d.getName().startsWith("Critiques")      ? "#c62828"
                                 : d.getName().startsWith("Avertissement")  ? "#e65100"
                                 : d.getName().startsWith("Acquittées")     ? "#2e7d32"
                                 : "#9e9e9e";
                    d.getNode().setStyle("-fx-pie-color: " + color + ";");
                }
            });
        };

        refresh.run();
        AlerteState.nbrAlertesProperty().addListener((o, v, n)    -> Platform.runLater(refresh));
        AlerteState.nbrAcquitteesProperty().addListener((o, v, n) -> Platform.runLater(refresh));
        AlerteState.nbrSupprimeesProp().addListener((o, v, n)     -> Platform.runLater(refresh));

        return wrapDonut(chart);
    }

    // =========================================
    // DONUT HOLE OVERLAY
    // =========================================
    private static StackPane wrapDonut(PieChart chart) {
        Circle hole = new Circle(58);
        hole.setStyle("-fx-fill: white;");
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
            s.getData().add(new XYChart.Data<>("Actives",       AlerteState.nbrAlertesProperty().get()));
            s.getData().add(new XYChart.Data<>("Critiques",     AlerteState.nbrCritiquesProperty().get()));
            s.getData().add(new XYChart.Data<>("Avert.",        AlerteState.nbrAvertissementsProperty().get()));
            s.getData().add(new XYChart.Data<>("Acquittées",    AlerteState.nbrAcquitteesProperty().get()));
            s.getData().add(new XYChart.Data<>("Supprimées",    AlerteState.nbrSupprimeesProp().get()));
            chart.getData().add(s);
            Platform.runLater(() -> {
                for (XYChart.Data<String, Number> d : s.getData()) {
                    if (d.getNode() == null) continue;
                    String color = switch (d.getXValue()) {
                        case "Critiques"  -> "#c62828";
                        case "Avert."     -> "#e65100";
                        case "Acquittées" -> "#2e7d32";
                        case "Supprimées" -> "#9e9e9e";
                        default           -> "#1565c0";
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
                    .stream().limit(6).collect(Collectors.toList());

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
