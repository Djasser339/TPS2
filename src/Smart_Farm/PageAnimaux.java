package Smart_Farm;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.*;

public class PageAnimaux {

    // =========================================
    // PAGE ANIMAUX
    // =========================================
    public static ScrollPane animauxPage() {

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
                UIFactory.createLiveNumberDisplay("Ruminants", AnimalState.nbrRuminantsProperty(), 220, 90),
                UIFactory.createLiveNumberDisplay("Volaille",  AnimalState.nbrVolailleProperty(),  220, 90),
                UIFactory.createLiveNumberDisplay("Aquacole",  AnimalState.nbrAquacoleProperty(),  220, 90),
                UIFactory.createAddCard("Total", AnimalState.nbrAnimalsProperty(), "➕ Ajouter Animal", () -> showAddAnimalForm(), 220, 90)
        );
        UIFactory.expandToFill(statsHeader);
        center.getChildren().add(statsHeader);

        // =========================
        // TABLE
        // =========================
        center.getChildren().add(UIFactory.createAnimatedTitle("🐄 Liste des Animaux"));

        List<String> headers = List.of(
                "ID",
                "Nom",
                "Type",
                "Age",
                "Poids"
        );

        VBox tableCard = TableFactory.createSearchTableCard(
                headers,

                query -> AnimalState.searchAnimal(query),

                AnimalState::mapAnimal,

                animal -> {
                    System.out.println("CLICK ANIMAL: " + animal);
                    showAnimalActionForm(animal);
                },

                refresh -> AnimalState.setAnimalRefresh(() ->
                        refresh.accept(AnimalState.searchAnimal(""))
                )
        );

        center.getChildren().add(tableCard);

        // =========================
        // DISTRIBUTION PAR TYPE
        // =========================
        center.getChildren().add(UIFactory.createAnimatedTitle("🐾 Distribution des Animaux par Type"));
        center.getChildren().add(createAnimalDistributionChart());

        center.getChildren().add(
                createAlimentationSearchCard(ZoneState.getZones())
        );

        VBox graphCard = FarmBarGraph.createBarGraphCard(
                "Production Live",
                FarmBarGraph.generateRandomFarmData(),
                900,
                500
        );

        BarChart<String, Number> chart =
                (BarChart<String, Number>)
                        graphCard.getChildren().get(1);

        FarmBarGraph.startLiveUpdate(
                chart,
                FarmBarGraph::generateRandomFarmData
        );

        center.getChildren().add(graphCard);




        // =========================
        // SCROLL GLOBAL (IMPORTANT FIX)
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
    public static HBox animauxStatsCards() {

        HBox container = new HBox();

        container.setSpacing(20);
        container.setPadding(new Insets(20));
        container.setAlignment(Pos.CENTER);

        container.getChildren().addAll(

                UIFactory.createLiveNumberDisplay(
                        "ruminants",
                        AnimalState.nbrRuminantsProperty(),
                        260,
                        90
                ),

                UIFactory.createLiveNumberDisplay(
                        "Vollaile",
                        AnimalState.nbrVolailleProperty(),
                        260,
                        90
                ),

                UIFactory.createLiveNumberDisplay(
                        "Aquacole",
                        AnimalState.nbrAquacoleProperty(),
                        260,
                        90
                )
        );

        return container;
    }

    public static Animal showAddAnimalForm() {

        final Animal[] result = new Animal[1];

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Ajouter Animal");

        // ---- Header ----
        HBox header = new HBox();
        header.setPadding(new Insets(18, 25, 18, 25));
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("form-header");
        Label headerLbl = new Label("🐄  Ajouter un Animal");
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

        // TYPE ANIMAL
        Label typeLabel = new Label("Type Animal");
        typeLabel.getStyleClass().add("form-label");

        RadioButton ruminantBtn = new RadioButton("🐄 Ruminant");
        RadioButton volailleBtn = new RadioButton("🐔 Volaille");
        RadioButton aquacoleBtn = new RadioButton("🐟 Aquacole");

        ToggleGroup group = new ToggleGroup();
        ruminantBtn.setToggleGroup(group);
        volailleBtn.setToggleGroup(group);
        aquacoleBtn.setToggleGroup(group);
        ruminantBtn.setSelected(true);

        HBox typeBox = new HBox(14, ruminantBtn, volailleBtn, aquacoleBtn);
        typeBox.setAlignment(Pos.CENTER_LEFT);

        // NOM
        Label nameLabel = new Label("Nom");
        nameLabel.getStyleClass().add("form-label");
        TextField nameField = new TextField();
        nameField.setPromptText("Nom de l'animal");
        nameField.setMaxWidth(Double.MAX_VALUE);

        // AGE / POIDS
        Label ageLabel = new Label("Âge (ans)");
        ageLabel.getStyleClass().add("form-label");
        Spinner<Integer> ageSpinner = new Spinner<>(0, 50, 1);
        ageSpinner.setEditable(true);
        ageSpinner.setMaxWidth(Double.MAX_VALUE);

        Label poidsLabel = new Label("Poids (kg)");
        poidsLabel.getStyleClass().add("form-label");
        Spinner<Integer> poidsSpinner = new Spinner<>(0, 500, 10);
        poidsSpinner.setEditable(true);
        poidsSpinner.setMaxWidth(Double.MAX_VALUE);

        Button validateBtn = new Button("✔  Valider");
        validateBtn.getStyleClass().add("form-button");
        validateBtn.setMaxWidth(Double.MAX_VALUE);

        validateBtn.setOnAction(e -> {
            String name = nameField.getText();
            if (name == null || name.isEmpty()) return;

            Animal a;
            if (ruminantBtn.isSelected()) {
                a = new Ruminant(TypeEspece.ruminant, name);
            } else if (volailleBtn.isSelected()) {
                a = new Volaille(TypeEspece.volaille, name);
            } else {
                a = new Aquacole(TypeEspece.aqua, name);
            }

            a.setAge(ageSpinner.getValue());
            a.setPoid(poidsSpinner.getValue());
            AnimalState.addAnimal(a);
            result[0] = a;
            stage.close();
        });

        grid.add(typeLabel,   0, 0); grid.add(typeBox,      1, 0);
        grid.add(nameLabel,   0, 1); grid.add(nameField,    1, 1);
        grid.add(ageLabel,    0, 2); grid.add(ageSpinner,   1, 2);
        grid.add(poidsLabel,  0, 3); grid.add(poidsSpinner, 1, 3);
        grid.add(validateBtn, 1, 4);

        VBox root = new VBox(header, grid);
        root.getStyleClass().add("form-global");

        Scene scene = new Scene(root, 480, 360);
        scene.getStylesheets().add(PageZone.class.getResource("style.css").toExternalForm());
        stage.setScene(scene);
        stage.showAndWait();

        return result[0];
    }

    public static void showAnimalActionForm(Animal a) {

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Action Animal");

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        root.getStyleClass().add("form-global");

        Label title = new Label("Modifier état de santé");
        title.getStyleClass().add("form-label");

        RadioButton sain = new RadioButton("Sain");
        RadioButton malade = new RadioButton("Malade");
        RadioButton surveillance = new RadioButton("Surveillance");

        ToggleGroup group = new ToggleGroup();
        sain.setToggleGroup(group);
        malade.setToggleGroup(group);
        surveillance.setToggleGroup(group);

        if (a.getEtatSante() == EtatSante.sain) {
            sain.setSelected(true);
        } else if (a.getEtatSante() == EtatSante.malade) {
            malade.setSelected(true);
        } else {
            surveillance.setSelected(true);
        }

        Button save = new Button("Valider");
        save.getStyleClass().add("form-button");

        save.setOnAction(e -> {

            if (sain.isSelected()) {
                a.setEtatSante(EtatSante.sain);
            } else if (malade.isSelected()) {
                a.setEtatSante(EtatSante.malade);
            } else {
                a.setEtatSante(EtatSante.quarantaine);
            }

            AnimalState.refreshAnimals();
            stage.close();
        });

        root.getChildren().addAll(
                title,
                sain,
                malade,
                surveillance,
                save
        );

        Scene scene = new Scene(root, 300, 250);

        //  FIX IMPORTANT ICI
        scene.getStylesheets().add(
                PageZone.class.getResource("style.css").toExternalForm()
        );

        stage.setScene(scene);
        stage.showAndWait();
    }


    // =========================================
    // BUILD ALIMENTATION LIST
    // =========================================

    // =========================
    // MAIN CARD WITH SEARCH
    // =========================
    public static VBox createAlimentationSearchCard(List<Zone> zones) {

        VBox card = new VBox(15);
        card.setPadding(new Insets(15));
        card.getStyleClass().add("culture-list-card");

        final String[] queryHolder = {""};

        TextField search = UIFactory.createSearchBar(query -> {

            queryHolder[0] = query;

            updateList(card, zones, queryHolder[0]);
        });

        card.getChildren().add(search);

        updateList(card, zones, "");

        return card;
    }

    // =========================
    // UPDATE LIST SAFE
    // =========================
    private static void updateList(VBox card, List<Zone> zones, String query) {

        List<Zone> filtered = ZoneState.searchAlimentationZones(query);

        Node searchNode = card.getChildren().get(0);

        card.getChildren().setAll(
                searchNode,
                buildAlimentationList(filtered)
        );
    }

    // =========================
    // BUILD LIST
    // =========================
    private static VBox buildAlimentationList(List<Zone> zones) {

        VBox list = new VBox(15);

        for (Zone z : zones) {

            VBox box = new VBox(10);
            box.setPadding(new Insets(15));
            box.getStyleClass().add("culture-zone-box");

            Label title = new Label("🐄 " + z.getNom() + " • " + z.getType());
            title.getStyleClass().add("culture-zone-title");

            box.getChildren().add(title);

            // =========================
            // PROGRAMMES
            // =========================
            List<ProgAlimentation> progs = new ArrayList<>();

            if (z instanceof ZoneElevage ze) {
                if (ze.getProgramme() != null)
                    progs = ze.getProgramme();
            }
            else if (z instanceof ZoneAquacole za) {
                if (za.getProgramme() != null)
                    progs = za.getProgramme();
            }

            // =========================
            // EMPTY STATE
            // =========================
            if (progs.isEmpty()) {

                Label empty = new Label("Aucun programme d'alimentation");
                empty.getStyleClass().add("culture-text");

                box.getChildren().add(empty);
            }

            // =========================
            // LIST PROGRAMS
            // =========================
            else {

                for (ProgAlimentation p : progs) {

                    VBox progBox = new VBox(5);
                    progBox.getStyleClass().add("culture-item");

                    Label text = new Label(
                            "🌾 " + p.getQuantite()
                                    + " kg de " + p.getTypeAliment()
                    );

                    text.getStyleClass().add("culture-text");

                    progBox.getChildren().add(text);
                    box.getChildren().add(progBox);
                }
            }

            // =========================
            // CLICK -> ADD PROGRAM
            // =========================
            box.setOnMouseClicked(e -> {

                showAddAlimentationForm(z);

                list.getChildren().setAll(
                        buildAlimentationList(
                                ZoneState.getAlimentationZones()
                        )
                );
            });

            list.getChildren().add(box);
        }

        return list;
    }

    // =========================================
    // PIE CHART — DISTRIBUTION DES ANIMAUX
    // =========================================
    private static VBox createAnimalDistributionChart() {

        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.getStyleClass().add("farm-graph-card");

        PieChart chart = new PieChart();
        chart.setLabelsVisible(true);
        chart.setLegendVisible(true);
        chart.setAnimated(false);
        chart.setPrefHeight(300);

        String[] COLORS = {"#ff6b35", "#4caf50", "#2196f3"};

        Runnable refreshChart = () -> {
            chart.getData().clear();
            int ruminants = AnimalState.nbrRuminantsProperty().get();
            int volailles = AnimalState.nbrVolailleProperty().get();
            int aquacoles = AnimalState.nbrAquacoleProperty().get();
            if (ruminants > 0)
                chart.getData().add(new PieChart.Data("Ruminants (" + ruminants + ")", ruminants));
            if (volailles > 0)
                chart.getData().add(new PieChart.Data("Volailles (" + volailles + ")", volailles));
            if (aquacoles > 0)
                chart.getData().add(new PieChart.Data("Aquacoles (" + aquacoles + ")", aquacoles));
            if (chart.getData().isEmpty())
                chart.getData().add(new PieChart.Data("Aucun animal", 1));
            Platform.runLater(() -> {
                for (int i = 0; i < chart.getData().size(); i++) {
                    PieChart.Data d = chart.getData().get(i);
                    if (d.getNode() != null)
                        d.getNode().setStyle("-fx-pie-color: " + COLORS[i % COLORS.length] + ";");
                }
            });
        };

        refreshChart.run();
        AnimalState.nbrAnimalsProperty().addListener((obs, o, n) -> Platform.runLater(refreshChart));

        card.getChildren().add(chart);
        return card;
    }

    // =========================
    // ADD PROGRAM FORM (SAFE)
    // =========================
    public static void showAddAlimentationForm(Zone z) {

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Programme Alimentation");

        GridPane root = new GridPane();

        root.setPadding(new Insets(25));
        root.setHgap(15);
        root.setVgap(15);
        root.setAlignment(Pos.CENTER);

        root.getStyleClass().add("form-global");

        // =========================
        // TYPE ALIMENT
        // =========================
        Label typeLabel = new Label("Type Aliment");
        typeLabel.getStyleClass().add("top-title");

        TextField typeField = new TextField();
        typeField.setPromptText("Ex: Maïs, Foin, Poisson...");

        // =========================
        // QUANTITE
        // =========================
        Label qLabel = new Label("Quantité (kg)");
        qLabel.getStyleClass().add("top-title");

        Spinner<Double> qSpinner = new Spinner<>(
                0.0, 10000.0, 10.0, 1.0
        );
        qSpinner.setEditable(true);

        // =========================
        // DESCRIPTION
        // =========================
        Label descLabel = new Label("Description");
        descLabel.getStyleClass().add("top-title");

        TextField descField = new TextField();
        descField.setPromptText("optionnel");

        // =========================
        // BUTTON
        // =========================
        Button validateBtn = UIFactory.createActionButton(
                "Valider",
                () -> {

                    // =========================
                    // VALIDATION TYPE
                    // =========================
                    if (typeField.getText() == null || typeField.getText().isBlank()) {

                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setHeaderText(null);
                        alert.setContentText("Type aliment obligatoire");
                        alert.showAndWait();
                        return;
                    }

                    // =========================
                    // SAFE QUANTITY
                    // =========================
                    double q;
                    try {
                        q = Double.parseDouble(qSpinner.getEditor().getText());
                    } catch (Exception e) {
                        q = qSpinner.getValue() != null ? qSpinner.getValue() : 0.0;
                    }

                    // =========================
                    // CREATE PROGRAM
                    // =========================
                    ProgAlimentation p = new ProgAlimentation(
                            typeField.getText(),
                            q
                    );

                    p.setDescription(descField.getText());

                    // =========================
                    // ADD TO ZONE (SAFE LIST)
                    // =========================
                    if (z instanceof ZoneElevage ze) {

                        if (ze.getProgramme() == null) {
                            ze.setProgramme(new ArrayList<>());
                        }

                        ze.getProgramme().add(p);
                    }

                    if (z instanceof ZoneAquacole za) {

                        if (za.getProgramme() == null) {
                            za.setProgramme(new ArrayList<>());
                        }

                        za.getProgramme().add(p);
                    }

                    // =========================
                    // REFRESH UI
                    // =========================
                    ZoneState.refreshAlimentation();

                    stage.close();
                }
        );

        // =========================
        // LAYOUT (GRID LIKE CULTURE)
        // =========================
        root.add(typeLabel, 0, 0);
        root.add(typeField, 1, 0);

        root.add(qLabel, 0, 1);
        root.add(qSpinner, 1, 1);

        root.add(descLabel, 0, 2);
        root.add(descField, 1, 2);

        root.add(validateBtn, 1, 3);

        // =========================
        // SCENE + CSS (SAME STYLE)
        // =========================
        Scene scene = new Scene(root, 520, 300);

        scene.getStylesheets().add(
                PageCulture.class.getResource("style.css").toExternalForm()
        );

        stage.setScene(scene);
        stage.setResizable(false);
        stage.showAndWait();
    }
}