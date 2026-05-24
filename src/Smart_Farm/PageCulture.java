package Smart_Farm;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.*;

import java.time.LocalDate;

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

        // =========================
        // STATS
        // =========================
        center.getChildren().add(cultureStatsCards());

        // =========================
        // ACTION CARD
        // =========================
        center.getChildren().add(
                UIFactory.RajoutterCard(
                        "Cultures",
                        FarmState.nbrCulturesProperty(),
                        "Ajouter Culture",
                        () -> {
                            System.out.println("Ajouter Culture");
                            showAddCultureForm();
                        },
                        700,
                        100
                )
        );

        // =========================
        // TABLE
        // =========================
        VBox tableCard = TableFactory.createSearchTableCard(
                List.of("Type", "Croissance", "Cultivation", "Recolte", "Ph", "Humidité"),
                FarmState::searchCultureByType,
                FarmState::mapCulture,
                culture -> {
                    System.out.println("CLICK: " + culture);
                    FarmState.nextGrowthStage(culture);
                },
                refresh -> {
                    FarmState.setCultureRefresh(() -> {
                        refresh.accept(FarmState.searchCultureByType(""));
                    });
                }
        );

        center.getChildren().add(tableCard);

        center.getChildren().add(
                createCultureSearchCard(ZoneState.getZones())
        );

        // =========================
        // TEST CONTENT (tu peux enlever après)
        // =========================
        for (int i = 0; i < 10; i++) {
            center.getChildren().add(cultureStatsCards());
        }

        root.setCenter(center);

        // =========================
        // SCROLL FIX FINAL
        // =========================
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

                UIFactory.createLiveNumberDisplay(
                        "Cereal",
                        FarmState.nbrCerealProperty(),
                        260,
                        90
                ),

                UIFactory.createLiveNumberDisplay(
                        "Legumes",
                        FarmState.nbrLegumeProperty(),
                        260,
                        90
                ),

                UIFactory.createLiveNumberDisplay(
                        "Fruits",
                        FarmState.nbrFruitProperty(),
                        260,
                        90
                )
        );

        return container;
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
        // =========================
        // TYPE CULTURE
        // =========================
        Label typeLabel = new Label("Type Culture");
        typeLabel.getStyleClass().add("top-title");

        RadioButton cerealBtn = new RadioButton("Céréale");
        RadioButton legumeBtn = new RadioButton("Légume");
        RadioButton fruitBtn = new RadioButton("Fruit");

        ToggleGroup typeGroup = new ToggleGroup();

        cerealBtn.setToggleGroup(typeGroup);
        legumeBtn.setToggleGroup(typeGroup);
        fruitBtn.setToggleGroup(typeGroup);

        cerealBtn.setSelected(true);

        VBox typeBox = new VBox(10, cerealBtn, legumeBtn, fruitBtn);

        // =========================
        // DATE PLANTATION
        // =========================
        Label plantationLabel = new Label("Date Plantation");
        plantationLabel.getStyleClass().add("top-title");

        DatePicker plantationPicker = new DatePicker(LocalDate.now());
        plantationPicker.setDisable(true);

        // =========================
        // DATE RECOLTE
        // =========================
        Label recolteLabel = new Label("Date Récolte");
        recolteLabel.getStyleClass().add("top-title");

        DatePicker recoltePicker = new DatePicker();

        recoltePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);

                if (date.isBefore(LocalDate.now().plusDays(1))) {
                    setDisable(true);
                }
            }
        });

        // =========================
        // PH
        // =========================
        Label phMinLabel = new Label("PH Min");
        Label phMaxLabel = new Label("PH Max");

        Spinner<Integer> phMinSpinner = new Spinner<>(0, 14, 6);
        Spinner<Integer> phMaxSpinner = new Spinner<>(0, 14, 8);

        // =========================
        // HUMIDITE
        // =========================
        Label humMinLabel = new Label("Humidité Min");
        Label humMaxLabel = new Label("Humidité Max");

        Spinner<Integer> humMinSpinner = new Spinner<>(0, 100, 40);
        Spinner<Integer> humMaxSpinner = new Spinner<>(0, 100, 70);

        // =========================
        // BUTTON
        // =========================
        Button validateBtn = UIFactory.createActionButton(
                "Valider",
                () -> {

                    if (recoltePicker.getValue() == null) {

                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setHeaderText(null);
                        alert.setContentText("Choisir une date de récolte");
                        alert.showAndWait();
                        return;
                    }

                    if (phMinSpinner.getValue() > phMaxSpinner.getValue()) {

                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setHeaderText(null);
                        alert.setContentText("PH Min > PH Max");
                        alert.showAndWait();
                        return;
                    }

                    if (humMinSpinner.getValue() > humMaxSpinner.getValue()) {

                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setHeaderText(null);
                        alert.setContentText("Humidité Min > Humidité Max");
                        alert.showAndWait();
                        return;
                    }

                    Seuil phSeuil = new Seuil(
                            phMinSpinner.getValue(),
                            phMaxSpinner.getValue()
                    );

                    Seuil humSeuil = new Seuil(
                            humMinSpinner.getValue(),
                            humMaxSpinner.getValue()
                    );

                    Culture c;

                    if (cerealBtn.isSelected()) {

                        c = new Cereal(
                                FamilleCulture.Cereal,
                                plantationPicker.getValue(),
                                recoltePicker.getValue(),
                                phSeuil,
                                humSeuil
                        );


                    } else if (legumeBtn.isSelected()) {

                        c = new Legume(
                                FamilleCulture.Legume,
                                plantationPicker.getValue(),
                                recoltePicker.getValue(),
                                phSeuil,
                                humSeuil
                        );


                    } else {

                        c = new Fruit(
                                FamilleCulture.Fruit,
                                plantationPicker.getValue(),
                                recoltePicker.getValue(),
                                phSeuil,
                                humSeuil
                        );

                    }

                    FarmState.addCulture(c);

                    stage.close();
                }
        );

        // =========================
        // LAYOUT
        // =========================
        root.add(typeLabel, 0, 0);
        root.add(typeBox, 1, 0);

        root.add(plantationLabel, 0, 1);
        root.add(plantationPicker, 1, 1);

        root.add(recolteLabel, 0, 2);
        root.add(recoltePicker, 1, 2);

        root.add(phMinLabel, 0, 3);
        root.add(phMinSpinner, 1, 3);

        root.add(phMaxLabel, 0, 4);
        root.add(phMaxSpinner, 1, 4);

        root.add(humMinLabel, 0, 5);
        root.add(humMinSpinner, 1, 5);

        root.add(humMaxLabel, 0, 6);
        root.add(humMaxSpinner, 1, 6);

        root.add(validateBtn, 1, 7);

        Scene scene = new Scene(root, 520, 700);
        scene.getStylesheets().add(
                PageCulture.class.getResource("style.css").toExternalForm()
        );

        stage.setScene(scene);
        stage.showAndWait();
        stage.setResizable(false);

        return result[0];
    }

    private static VBox buildCultureList(List<Zone> zones) {

        VBox list = new VBox(12);

        for (Zone z : zones) {

            VBox zoneBox = new VBox(8);
            zoneBox.getStyleClass().add("culture-zone-box");

            Label zoneTitle = new Label("📍 " + z.getNom());
            zoneTitle.getStyleClass().add("culture-zone-title");

            zoneBox.getChildren().add(zoneTitle);

            if (z instanceof ZoneCulture zc) {

                List<Culture> cultures = zc.getCultures();

                if (cultures == null || cultures.isEmpty()) {

                    Label empty = new Label("Aucune culture");
                    empty.getStyleClass().add("culture-text");
                    zoneBox.getChildren().add(empty);

                } else {

                    for (Culture c : cultures) {

                        VBox item = new VBox(4);
                        item.getStyleClass().add("culture-item");

                        item.getChildren().addAll(
                                new Label("🌱 " + c.getFamille()),
                                new Label("📈 " + c.getStadeCroissance()),
                                new Label("📅 Plantation: " + c.getDatePlantation()),
                                new Label("📅 Récolte: " + c.getDateRecolte())
                        );

                        item.getChildren().forEach(n ->
                                n.getStyleClass().add("culture-text")
                        );

                        zoneBox.getChildren().add(item);
                    }
                }
            }

            list.getChildren().add(zoneBox);
        }

        return list;
    }

    public static VBox createCultureSearchCard(List<Zone> zones) {

        VBox card = new VBox(15);
        card.setPadding(new Insets(15));
        card.getStyleClass().add("culture-list-card");

        // =========================
        // LIST CONTAINER (IMPORTANT)
        // =========================
        VBox listContainer = new VBox(12);

        // =========================
        // SEARCH BAR
        // =========================
        TextField search = UIFactory.createSearchBar(query -> {

            List<Zone> filtered = ZoneState.searchCultureZones(query);

            listContainer.getChildren().setAll(
                    buildCultureList(filtered)
            );
        });

        // =========================
        // INIT LIST
        // =========================
        listContainer.getChildren().addAll(
                buildCultureList(zones)
        );

        // =========================
        // ADD TO CARD
        // =========================
        card.getChildren().addAll(
                search,
                listContainer
        );

        return card;
    }


}