package Smart_Farm;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class PageCulture {

    // =========================================
    // PAGE CULTURE
    // =========================================
    public static ScrollPane culturePage() {

        BorderPane root = new BorderPane();

        VBox center = new VBox();
        center.setSpacing(30);
        center.setPadding(new Insets(20));
        center.setAlignment(Pos.TOP_CENTER);
        center.setFillWidth(true);

        // STATS + ADD
        HBox statsHeader = new HBox(20);
        statsHeader.setPadding(new Insets(20));
        statsHeader.setAlignment(Pos.CENTER);
        statsHeader.getChildren().addAll(
                UIFactory.createLiveNumberDisplay("Céréales", FarmState.nbrCerealProperty(), 220, 90),
                UIFactory.createLiveNumberDisplay("Légumes",  FarmState.nbrLegumeProperty(), 220, 90),
                UIFactory.createLiveNumberDisplay("Fruits",   FarmState.nbrFruitProperty(),  220, 90),
                UIFactory.createAddCard("Total", FarmState.nbrCulturesProperty(), "➕ Ajouter Culture",
                        () -> showAddCultureForm(), 220, 90)
        );
        UIFactory.expandToFill(statsHeader);
        center.getChildren().add(statsHeader);

        // TABLE
        center.getChildren().add(UIFactory.createAnimatedTitle("🌱 Liste des Cultures"));

        VBox tableCard = TableFactory.createSearchTableCard(
                List.of("Type", "Croissance", "Cultivation", "Recolte", "Ph", "Humidité"),
                FarmState::searchCultureByType,
                FarmState::mapCulture,
                culture -> FarmState.nextGrowthStage(culture),
                refresh -> FarmState.setCultureRefresh(
                        () -> refresh.accept(FarmState.searchCultureByType("")))
        );

        center.getChildren().add(tableCard);

        // CULTURES PAR ZONE (accordion)
        center.getChildren().add(UIFactory.createAnimatedTitle("📍 Cultures par Zone"));
        center.getChildren().add(createCultureByZoneCard());

        root.setCenter(center);

        ScrollPane pageScroll = new ScrollPane(root);
        pageScroll.setFitToWidth(true);
        pageScroll.setPannable(true);
        pageScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        pageScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        return pageScroll;
    }

    // =========================================
    // STATS CARDS
    // =========================================
    public static HBox cultureStatsCards() {

        HBox container = new HBox();
        container.setSpacing(20);
        container.setPadding(new Insets(20));
        container.setAlignment(Pos.CENTER);

        container.getChildren().addAll(
                UIFactory.createLiveNumberDisplay("Cereal",  FarmState.nbrCerealProperty(),  260, 90),
                UIFactory.createLiveNumberDisplay("Legumes", FarmState.nbrLegumeProperty(),  260, 90),
                UIFactory.createLiveNumberDisplay("Fruits",  FarmState.nbrFruitProperty(),   260, 90)
        );

        return container;
    }

    // =========================================
    // CARTE ZONES CULTURE (ACCORDION)
    // =========================================
    public static VBox createCultureByZoneCard() {

        VBox card = new VBox(15);
        card.setPadding(new Insets(18));
        card.getStyleClass().add("culture-list-card");

        VBox listContainer = new VBox(10);

        final String[] currentQuery = {""};

        Runnable rebuild = () -> {
            String q = currentQuery[0];
            List<Zone> filtered = ZoneState.getZones().stream()
                    .filter(z -> z instanceof ZoneCulture)
                    .filter(z -> q == null || q.isEmpty() ||
                            z.getNom().toLowerCase().contains(q.toLowerCase()))
                    .collect(Collectors.toList());
            listContainer.getChildren().setAll(buildAccordionZones(filtered).getChildren());
        };

        rebuild.run();

        // Rebuild lorsqu'une nouvelle zone est ajoutée
        ZoneState.nbrZonesProperty().addListener((obs, o, n) -> Platform.runLater(rebuild));

        TextField search = UIFactory.createSearchBar(query -> {
            currentQuery[0] = query == null ? "" : query;
            rebuild.run();
        });

        card.getChildren().addAll(search, listContainer);
        return card;
    }

    // =========================================
    // LISTE ACCORDION DES ZONES CULTURE
    // =========================================
    private static VBox buildAccordionZones(List<Zone> zones) {

        VBox list = new VBox(10);

        if (zones.isEmpty()) {
            Label none = new Label("Aucune zone de culture disponible");
            none.setStyle("-fx-text-fill: #888; -fx-font-size: 13px; -fx-padding: 8;");
            list.getChildren().add(none);
            return list;
        }

        for (Zone z : zones) {
            if (!(z instanceof ZoneCulture zc)) continue;

            // ---- Section conteneur ----
            VBox section = new VBox(0);
            section.setStyle(
                    "-fx-background-color: white;" +
                    "-fx-background-radius: 12;" +
                    "-fx-border-color: #d0e8d0;" +
                    "-fx-border-radius: 12;" +
                    "-fx-border-width: 1.5;"
            );

            // ---- Header cliquable ----
            HBox headerRow = new HBox(10);
            headerRow.setPadding(new Insets(13, 16, 13, 16));
            headerRow.setAlignment(Pos.CENTER_LEFT);
            headerRow.setStyle(
                    "-fx-background-color: #eaf5ea;" +
                    "-fx-background-radius: 12 12 0 0;" +
                    "-fx-cursor: hand;"
            );

            Label arrowLbl = new Label("▶");
            arrowLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #3B7249;");

            Label nameLbl = new Label("📍  " + z.getNom());
            nameLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2E5E3B;");

            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);

            List<Culture> cultures = zc.getCultures();
            int count = cultures.size();
            Label countBadge = new Label(count + " culture(s)");
            countBadge.setStyle(
                    "-fx-font-size: 11px; -fx-text-fill: #2E5E3B; -fx-font-weight: bold;" +
                    "-fx-background-color: #c8e6c9; -fx-padding: 3 10; -fx-background-radius: 12;"
            );

            headerRow.getChildren().addAll(arrowLbl, nameLbl, sp, countBadge);

            // ---- Contenu (cultures) — masqué par défaut ----
            VBox content = new VBox(6);
            content.setPadding(new Insets(10, 12, 12, 18));
            content.setManaged(false);
            content.setVisible(false);

            buildCultureRows(content, cultures);

            // ---- Toggle ----
            final boolean[] open = {false};
            headerRow.setOnMouseClicked(e -> {
                open[0] = !open[0];
                content.setManaged(open[0]);
                content.setVisible(open[0]);
                arrowLbl.setText(open[0] ? "▼" : "▶");
                String borderCol  = open[0] ? "#4A8A5C" : "#d0e8d0";
                String borderWidth = open[0] ? "2" : "1.5";
                section.setStyle(
                        "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: " + borderCol + ";" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-width: " + borderWidth + ";"
                );
            });

            section.getChildren().addAll(headerRow, content);
            list.getChildren().add(section);
        }

        return list;
    }

    // =========================================
    // LIGNES DE CULTURES DANS L'ACCORDION
    // =========================================
    private static void buildCultureRows(VBox content, List<Culture> cultures) {
        content.getChildren().clear();

        if (cultures.isEmpty()) {
            Label emptyLbl = new Label("Aucune culture dans cette zone");
            emptyLbl.setStyle("-fx-text-fill: #999; -fx-font-size: 12px; -fx-padding: 4;");
            content.getChildren().add(emptyLbl);
            return;
        }

        for (Culture c : cultures) {
            HBox row = new HBox(16);
            row.setPadding(new Insets(9, 14, 9, 14));
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle(
                    "-fx-background-color: #f6fbf6;" +
                    "-fx-background-radius: 9;" +
                    "-fx-border-color: #d0e8d0;" +
                    "-fx-border-radius: 9;" +
                    "-fx-border-width: 1;"
            );

            Label famLbl = new Label("🌱  " + c.getFamille());
            famLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2E5E3B;");
            famLbl.setPrefWidth(90);

            Label stadeLbl = new Label("📈  " + c.getStadeCroissance());
            stadeLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");
            stadeLbl.setPrefWidth(115);

            Label phLbl = new Label("pH " + String.format("%.1f", c.getPH()));
            phLbl.setStyle(
                    "-fx-font-size: 11px; -fx-text-fill: #2E5E3B;" +
                    "-fx-background-color: #e8f5e9; -fx-padding: 2 8;" +
                    "-fx-background-radius: 8;"
            );
            phLbl.setPrefWidth(70);

            Label humLbl = new Label("💧 " + String.format("%.0f", c.getHumidite()) + "%");
            humLbl.setStyle(
                    "-fx-font-size: 11px; -fx-text-fill: #1565C0;" +
                    "-fx-background-color: #e3f2fd; -fx-padding: 2 8;" +
                    "-fx-background-radius: 8;"
            );
            humLbl.setPrefWidth(75);

            Label dateLbl = new Label("📅  " + c.getDatePlantation() + "  →  " + c.getDateRecolte());
            dateLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");

            row.getChildren().addAll(famLbl, stadeLbl, phLbl, humLbl, dateLbl);
            content.getChildren().add(row);
        }
    }

    // =========================================
    // FORMULAIRE AJOUT CULTURE
    // =========================================
    public static Culture showAddCultureForm() {

        final Culture[] result = new Culture[1];

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Ajouter Culture");

        GridPane root = new GridPane();
        root.setPadding(new Insets(25));
        root.setHgap(15);
        root.setVgap(15);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("form-global");

        // TYPE CULTURE
        Label typeLabel = new Label("Type Culture");
        typeLabel.getStyleClass().add("top-title");

        RadioButton cerealBtn = new RadioButton("Céréale");
        RadioButton legumeBtn = new RadioButton("Légume");
        RadioButton fruitBtn  = new RadioButton("Fruit");

        ToggleGroup typeGroup = new ToggleGroup();
        cerealBtn.setToggleGroup(typeGroup);
        legumeBtn.setToggleGroup(typeGroup);
        fruitBtn.setToggleGroup(typeGroup);
        cerealBtn.setSelected(true);

        VBox typeBox = new VBox(10, cerealBtn, legumeBtn, fruitBtn);

        // DATE PLANTATION
        Label plantationLabel = new Label("Date Plantation");
        plantationLabel.getStyleClass().add("top-title");
        DatePicker plantationPicker = new DatePicker(LocalDate.now());
        plantationPicker.setDisable(true);

        // DATE RECOLTE
        Label recolteLabel = new Label("Date Récolte");
        recolteLabel.getStyleClass().add("top-title");
        DatePicker recoltePicker = new DatePicker();
        recoltePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date.isBefore(LocalDate.now().plusDays(1))) setDisable(true);
            }
        });

        // PH
        Label phMinLabel = new Label("PH Min");
        Label phMaxLabel = new Label("PH Max");
        Spinner<Integer> phMinSpinner = new Spinner<>(0, 14, 6);
        Spinner<Integer> phMaxSpinner = new Spinner<>(0, 14, 8);

        // HUMIDITE
        Label humMinLabel = new Label("Humidité Min");
        Label humMaxLabel = new Label("Humidité Max");
        Spinner<Integer> humMinSpinner = new Spinner<>(0, 100, 40);
        Spinner<Integer> humMaxSpinner = new Spinner<>(0, 100, 70);

        // BUTTON
        Button validateBtn = UIFactory.createActionButton("Valider", () -> {
            if (recoltePicker.getValue() == null) {
                new Alert(Alert.AlertType.ERROR, "Choisir une date de récolte", ButtonType.OK).showAndWait();
                return;
            }
            if (phMinSpinner.getValue() > phMaxSpinner.getValue()) {
                new Alert(Alert.AlertType.ERROR, "PH Min > PH Max", ButtonType.OK).showAndWait();
                return;
            }
            if (humMinSpinner.getValue() > humMaxSpinner.getValue()) {
                new Alert(Alert.AlertType.ERROR, "Humidité Min > Humidité Max", ButtonType.OK).showAndWait();
                return;
            }

            Seuil phSeuil  = new Seuil(phMinSpinner.getValue(),  phMaxSpinner.getValue());
            Seuil humSeuil = new Seuil(humMinSpinner.getValue(), humMaxSpinner.getValue());

            Culture c;
            if (cerealBtn.isSelected())
                c = new Cereal(FamilleCulture.Cereal, plantationPicker.getValue(), recoltePicker.getValue(), phSeuil, humSeuil);
            else if (legumeBtn.isSelected())
                c = new Legume(FamilleCulture.Legume, plantationPicker.getValue(), recoltePicker.getValue(), phSeuil, humSeuil);
            else
                c = new Fruit(FamilleCulture.Fruit,  plantationPicker.getValue(), recoltePicker.getValue(), phSeuil, humSeuil);

            FarmState.addCulture(c);
            stage.close();
        });

        // LAYOUT
        root.add(typeLabel,       0, 0); root.add(typeBox,         1, 0);
        root.add(plantationLabel, 0, 1); root.add(plantationPicker, 1, 1);
        root.add(recolteLabel,    0, 2); root.add(recoltePicker,   1, 2);
        root.add(phMinLabel,      0, 3); root.add(phMinSpinner,    1, 3);
        root.add(phMaxLabel,      0, 4); root.add(phMaxSpinner,    1, 4);
        root.add(humMinLabel,     0, 5); root.add(humMinSpinner,   1, 5);
        root.add(humMaxLabel,     0, 6); root.add(humMaxSpinner,   1, 6);
        root.add(validateBtn,     1, 7);

        Scene scene = new Scene(root, 520, 700);
        scene.getStylesheets().add(PageCulture.class.getResource("style.css").toExternalForm());
        stage.setScene(scene);
        stage.setResizable(false);
        stage.showAndWait();
        return result[0];
    }

    // =========================================
    // KEPT FOR COMPATIBILITY (appelé nulle part mais laissé)
    // =========================================
    public static VBox createCultureSearchCard(List<Zone> zones) {
        return createCultureByZoneCard();
    }
}
