package Smart_Farm;

import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Modality;
import javafx.stage.Stage;


import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static Smart_Farm.UIFactory.createActionButton;

public class PageZone {

    public static ScrollPane zonePage() {

        VBox center = new VBox();
        center.setSpacing(30);
        center.setPadding(new Insets(20));
        center.setAlignment(Pos.TOP_CENTER);
        center.setFillWidth(true);



        // =========================
        // STATS
        // =========================
        center.getChildren().add(zoneStatsCards());

        // =========================
        // ACTION CARD
        // =========================
        center.getChildren().add(
                UIFactory.createAnimatedTitle("\uD83C\uDF0D Ajouter une zone")
        );

        center.getChildren().add(
                zoneActionCard(
                        ZoneState.nbrZonesProperty(),
                        () -> showAddZoneForm(),
                        700,
                        100
                )
        );

        // =========================
        // DOUBLE ACTION CARD
        // =========================
        center.getChildren().add(
                UIFactory.createAnimatedTitle("\uD83C\uDF04 Affecter une culture ou un animal a une zone")
        );

        center.getChildren().add(
                createDoubleActionCard(
                        "Nbr Cultures",
                        FarmState.nbrCulturesProperty(),
                        "Nbr animaux",
                        AnimalState.nbrAnimalsProperty(),
                        300,
                        200
                )
        );

        // =========================
        // TABLE ZONES
        // =========================

        center.getChildren().add(
                UIFactory.createAnimatedTitle("\uD83C\uDFDE\uFE0F Vue d'ensemble des Zones ( clicker pour modifier ou desactiver )")
        );

        List<String> headers = List.of(
                "Code",
                "Nom",
                "Type",
                "Statut",
                "Entités"
        );

        VBox tableCard = TableFactory.createSearchTableCard(
                headers,

                query -> ZoneState.searchZone(query),

                ZoneState::mapZone,

                zone -> {
                    System.out.println("CLICK ZONE: " + zone);
                    showZoneEditForm(zone);
                },

                refresh -> ZoneState.setZoneRefresh(() ->
                        refresh.accept(ZoneState.searchZone(""))
                )
        );

        center.getChildren().add(tableCard);

        // =========================
        // TABLE HISTORIQUE PRODUCTION
        // =========================

        center.getChildren().add(
                UIFactory.createAnimatedTitle("\uD83D\uDCC8 Historique de production")
        );

        List<String> headers2 = List.of(
                "Zone",
                "Type Zone",
                "Date",
                "Description",
                "Valeur",
                "Unité"
        );

        VBox productionTable = TableFactory.createSearchTableCard(

                headers2,

                // =========================
                // SEARCH
                // =========================
                query -> ZoneState.searchProduction(query),

                // =========================
                // MAPPER
                // =========================
                ZoneState::mapProduction,

                // =========================
                // CLICK ROW
                // =========================
                prod -> {

                    System.out.println(
                            "Production : " + prod
                    );
                },

                // =========================
                // REFRESH
                // =========================
                refresh -> ZoneState.setProductionRefresh(() ->

                        refresh.accept(
                                ZoneState.searchProduction("")
                        )
                )
        );

        center.getChildren().add(productionTable);


        // =========================
        // SCROLL FIX FINAL
        // =========================
        ScrollPane pageScroll = new ScrollPane();

        pageScroll.setContent(center);

        //  FIX IMPORTANT (anti scroll horizontal)
        pageScroll.setFitToWidth(true);
        pageScroll.setFitToHeight(false);

        pageScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        pageScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        pageScroll.setPannable(false);

        return pageScroll;
    }

    public static HBox zoneStatsCards() {

        HBox container = new HBox();
        container.setSpacing(20);
        container.setPadding(new Insets(20));
        container.setAlignment(Pos.CENTER);

        container.getChildren().addAll(

                UIFactory.createLiveNumberDisplay(
                        "Zone Culture",
                        ZoneState.nbrZoneCultureProperty(),
                        260,
                        90
                ),

                UIFactory.createLiveNumberDisplay(
                        "Zone Elevage",
                        ZoneState.nbrZoneElevageProperty(),
                        260,
                        90
                ),

                UIFactory.createLiveNumberDisplay(
                        "Zone Aquacole",
                        ZoneState.nbrZoneAquacoleProperty(),
                        260,
                        90
                )
        );

        return container;
    }

    public static HBox createDoubleActionCard(
            String title1,
            IntegerProperty value1,
            String title2,
            IntegerProperty value2,
            double width,
            double height
    ) {

        HBox card = new HBox();
        card.setSpacing(20);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER);

        card.getStyleClass().add("widget-card");

        card.setPrefSize(width, height);

        VBox block1 = new VBox();
        block1.setAlignment(Pos.CENTER);

        Label label1 = new Label(title1+ " : " +value1.get());
        label1.getStyleClass().add("top-title");


        Button btn1 = createActionButton("\uD83C\uDFF7\uFE0F Affecter Culture", () -> {
            System.out.println("Action 1");
        });

        btn1.setOnAction(e -> {

            showAssignToZoneForm(

                    "Affecter Culture",

                    List.of(
                            "Type",
                            "Croissance",
                            "Cultivation",
                            "Recolte",
                            "Ph",
                            "Humidité"
                    ),

                    FarmState::searchCultureByType,

                    FarmState::mapCulture,

                    (zone, culture) -> {

                        if (!(zone instanceof ZoneCulture zc)) {

                            throw new IllegalArgumentException(
                                    "Cette zone n'accepte pas les cultures"
                            );
                        }

                        zc.ajouterCulture((Culture) culture);
                    },

                    () -> {}
            );
        });

        block1.getChildren().addAll(label1,  btn1);

        VBox block2 = new VBox();
        block2.setAlignment(Pos.CENTER);

        Label label2 = new Label(title2+" : " +value2.get());
        label2.getStyleClass().add("top-title");



        Button btn2 = createActionButton("\uD83C\uDFF7\uFE0F Affecter Animal", () -> {
            System.out.println("Action 2");
        });

        btn2.setOnAction(e -> {

            showAssignToZoneForm(

                    "Affecter Animal",

                    List.of(
                            "ID",
                            "Nom",
                            "Type",
                            "Age",
                            "Poids"
                    ),

                    AnimalState::searchAnimal,

                    AnimalState::mapAnimal,

                    (zone, animalObj) -> {

                        Animal animal = (Animal) animalObj;

                        // =========================
                        // AQUACOLE
                        // =========================
                        if (animal instanceof Aquacole aqua) {

                            if (!(zone instanceof ZoneAquacole za)) {

                                throw new IllegalArgumentException(
                                        "Un animal aquacole doit être dans une zone aquacole"
                                );
                            }

                            za.ajouterAquacole(aqua);

                            return;
                        }

                        // =========================
                        // ELEVAGE
                        // =========================
                        if (!(zone instanceof ZoneElevage ze)) {

                            throw new IllegalArgumentException(
                                    "Cet animal doit être dans une zone d'élevage"
                            );
                        }

                        if (animal instanceof Ruminant r) {
                            ze.ajouterRuminant(r);
                        }

                        else if (animal instanceof Volaille v) {
                            ze.ajouterVollaile(v);
                        }
                    },

                    () -> {}
            );
        });

        block2.getChildren().addAll(label2,  btn2);

        card.getChildren().addAll(block1, block2);

        return card;
    }

    public static HBox zoneActionCard(
            IntegerProperty totalZones,
            Runnable onAddZone,
            double width,
            double height
    ) {

        HBox card = new HBox();
        card.setSpacing(20);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("widget-card");
        card.setPrefSize(width, height);

        Label title = new Label("Zones");
        title.getStyleClass().add("top-title");

        Label value = new Label();
        value.textProperty().bind(totalZones.asString());
        value.getStyleClass().add("top-date");

        Button addBtn = createActionButton("➕ Ajouter Zone", onAddZone);

        VBox box = new VBox(10, title, value, addBtn);
        box.setAlignment(Pos.CENTER);

        card.getChildren().add(box);

        return card;
    }

    public static Zone showAddZoneForm() {

        final Zone[] result = new Zone[1];

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Ajouter Zone");

        GridPane root = new GridPane();

        root.setPadding(new Insets(25));
        root.setHgap(15);
        root.setVgap(15);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("form-global");

        // =========================
        // NOM ZONE
        // =========================
        Label nameLabel = new Label("Nom Zone");
        TextField nameField = new TextField();

        // =========================
        // TYPE ZONE
        // =========================
        Label typeLabel = new Label("Type Zone");

        RadioButton cultureBtn = new RadioButton("Culture");
        RadioButton elevageBtn = new RadioButton("Élevage");
        RadioButton aquaBtn = new RadioButton("Aquacole");

        ToggleGroup group = new ToggleGroup();
        cultureBtn.setToggleGroup(group);
        elevageBtn.setToggleGroup(group);
        aquaBtn.setToggleGroup(group);

        cultureBtn.setSelected(true);

        // ✔ horizontal
        HBox typeBox = new HBox(15, cultureBtn, elevageBtn, aquaBtn);

        // =========================
        // DYNAMIC AREA
        // =========================
        VBox dynamicBox = new VBox(15);

        Runnable updateUI = () -> {

            dynamicBox.getChildren().clear();

            // =====================================================
            // CULTURE
            // =====================================================
            if (cultureBtn.isSelected()) {

                Button ok = UIFactory.createActionButton("Valider", () -> {

                    String name = nameField.getText();
                    if (name == null || name.isEmpty()) return;

                    ZoneCulture z = new ZoneCulture(
                            name.hashCode(),
                            name,
                            TypeZone.culture
                    );

                    ZoneState.addZone(z);
                    result[0] = z;
                    stage.close();
                });

                dynamicBox.getChildren().add(ok);
            }

            // =====================================================
            // ÉLEVAGE
            // =====================================================
            else if (elevageBtn.isSelected()) {

                // =========================
                // TYPE ELEVAGE
                // =========================
                ComboBox<TypeZoneElevage> typeElevage = new ComboBox<>();
                typeElevage.getItems().addAll(TypeZoneElevage.values());

                // =========================
                // LATITUDE RANGE
                // =========================
                Slider latMin = new Slider(-90, 90, -10);
                Slider latMax = new Slider(-90, 90, 10);

                Label latLabel = new Label();

                latLabel.textProperty().bind(
                        Bindings.createStringBinding(
                                () -> "Latitude : [" +
                                        (int) latMin.getValue() + " , " +
                                        (int) latMax.getValue() + "]",
                                latMin.valueProperty(),
                                latMax.valueProperty()
                        )
                );

                latMin.valueProperty().addListener((o, oldV, newV) -> {
                    if (newV.doubleValue() > latMax.getValue()) {
                        latMax.setValue(newV.doubleValue());
                    }
                });

                latMax.valueProperty().addListener((o, oldV, newV) -> {
                    if (newV.doubleValue() < latMin.getValue()) {
                        latMin.setValue(newV.doubleValue());
                    }
                });

                // =========================
                // LONGITUDE RANGE
                // =========================
                Slider lonMin = new Slider(-180, 180, -10);
                Slider lonMax = new Slider(-180, 180, 10);

                Label lonLabel = new Label();

                lonLabel.textProperty().bind(
                        Bindings.createStringBinding(
                                () -> "Longitude : [" +
                                        (int) lonMin.getValue() + " , " +
                                        (int) lonMax.getValue() + "]",
                                lonMin.valueProperty(),
                                lonMax.valueProperty()
                        )
                );

                lonMin.valueProperty().addListener((o, oldV, newV) -> {
                    if (newV.doubleValue() > lonMax.getValue()) {
                        lonMax.setValue(newV.doubleValue());
                    }
                });

                lonMax.valueProperty().addListener((o, oldV, newV) -> {
                    if (newV.doubleValue() < lonMin.getValue()) {
                        lonMin.setValue(newV.doubleValue());
                    }
                });

                // =========================
                // VALIDATION
                // =========================
                Button ok = UIFactory.createActionButton("Valider", () -> {

                    String name = nameField.getText();
                    if (name == null || name.isEmpty()) return;

                    GeographicalLimits limits = new GeographicalLimits(
                            "Zone Elevage",
                            (int) latMin.getValue(),
                            (int) latMax.getValue(),
                            (int) lonMin.getValue(),
                            (int) lonMax.getValue()
                    );

                    ZoneElevage z = new ZoneElevage(
                            name.hashCode(),
                            name,
                            TypeZone.elevage,
                            typeElevage.getValue(),
                            limits
                    );

                    ZoneState.addZone(z);
                    result[0] = z;
                    stage.close();
                });

                VBox elevageBox = new VBox(10,
                        new Label("Type Élevage"),
                        typeElevage,
                        latLabel,
                        latMin,
                        latMax,
                        lonLabel,
                        lonMin,
                        lonMax,
                        ok
                );

                dynamicBox.getChildren().add(elevageBox);
            }

            // =====================================================
            // AQUACOLE
            // =====================================================
            else if (aquaBtn.isSelected()) {

                Button ok = UIFactory.createActionButton("Valider", () -> {

                    String name = nameField.getText();
                    if (name == null || name.isEmpty()) return;

                    ZoneAquacole z = new ZoneAquacole(
                            name.hashCode(),
                            name,
                            TypeZone.aquacole
                    );

                    ZoneState.addZone(z);
                    result[0] = z;
                    stage.close();
                });

                dynamicBox.getChildren().add(ok);
            }
        };

        // =========================
        // LISTENERS
        // =========================
        cultureBtn.setOnAction(e -> updateUI.run());
        elevageBtn.setOnAction(e -> updateUI.run());
        aquaBtn.setOnAction(e -> updateUI.run());

        updateUI.run();

        // =========================
        // LAYOUT
        // =========================
        root.add(nameLabel, 0, 0);
        root.add(nameField, 1, 0);

        root.add(typeLabel, 0, 1);
        root.add(typeBox, 1, 1);

        root.add(dynamicBox, 1, 2);

        Scene scene = new Scene(root, 650, 450);
        scene.getStylesheets().add(
                PageZone.class.getResource("style.css").toExternalForm()
        );

        stage.setScene(scene);
        stage.showAndWait();
        stage.setResizable(false);

        return result[0];
    }

    public static void showZoneEditForm(Zone z) {

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Modifier Zone");

        GridPane root = new GridPane();
        root.setPadding(new Insets(25));
        root.setHgap(15);
        root.setVgap(15);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("form-global");

        // =========================
        // NOM
        // =========================
        Label nameLabel = new Label("Nom Zone");
        TextField nameField = new TextField(z.getNom());

        // =========================
        // STATUT
        // =========================
        Label statusLabel = new Label("Statut");

        RadioButton activeBtn = new RadioButton("Active");
        RadioButton inactiveBtn = new RadioButton("Inactive");

        ToggleGroup group = new ToggleGroup();
        activeBtn.setToggleGroup(group);
        inactiveBtn.setToggleGroup(group);

        if (z.getStatut() == StatutZone.ACTIVE) {
            activeBtn.setSelected(true);
        } else {
            inactiveBtn.setSelected(true);
        }

        VBox statusBox = new VBox(10, activeBtn, inactiveBtn);

        // =========================
        // BUTTON
        // =========================
        Button validateBtn = UIFactory.createActionButton(
                "Valider",
                () -> {

                    // update name
                    z.setNom(nameField.getText());

                    // update status
                    if (activeBtn.isSelected()) {
                        z.setStatut(StatutZone.ACTIVE);
                    } else {
                        z.setStatut(StatutZone.INACTIVE);
                    }

                    ZoneState.notifyRefresh();

                    stage.close();
                }
        );

        // =========================
        // LAYOUT
        // =========================
        root.add(nameLabel, 0, 0);
        root.add(nameField, 1, 0);

        root.add(statusLabel, 0, 1);
        root.add(statusBox, 1, 1);

        root.add(validateBtn, 1, 2);

        Scene scene = new Scene(root, 450, 250);
        scene.getStylesheets().add(
                PageZone.class.getResource("style.css").toExternalForm()
        );

        stage.setScene(scene);
        stage.showAndWait();
    }

    // =========================================
// FORMULAIRE AFFECTATION ENTITE -> ZONE
// =========================================
    public static <T> void showAssignToZoneForm(

            String title,

            List<String> entityHeaders,

            Function<String, List<T>> entitySearch,

            Function<T, List<String>> entityMapper,

            BiConsumer<Zone, T> affectationLogic,

            Runnable entityRefreshRegister
    ) {

        Stage stage = new Stage();

        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(title);

        BorderPane root = new BorderPane();

        root.setPadding(new Insets(20));

        // =========================================
        // ETAPE 1 : TABLE ENTITES
        // =========================================
        VBox entityTable = TableFactory.createSearchTableCard(

                entityHeaders,

                entitySearch,

                entityMapper,

                selectedEntity -> {

                    // =========================================
                    // ETAPE 2 : TABLE ZONES
                    // =========================================
                    VBox zoneTable = TableFactory.createSearchTableCard(

                            List.of(
                                    "Code",
                                    "Nom",
                                    "Type",
                                    "Statut",
                                    "Nbr Entité"
                            ),

                            ZoneState::searchZone,

                            ZoneState::mapZone,

                            selectedZone -> {

                                try {

                                    // =========================================
                                    // VERIFICATION METIER (ELEVAGE UNIQUEMENT)
                                    // =========================================

                                    if (selectedZone instanceof ZoneElevage zoneElevage) {

                                        if (selectedEntity instanceof Ruminant
                                                && zoneElevage.getTypeZoneElevage() == TypeZoneElevage.Volaille) {
                                            throw new IllegalArgumentException(
                                                    "Impossible d'affecter un Ruminant à une zone de Volaille"
                                            );
                                        }

                                        if (selectedEntity instanceof Volaille
                                                && zoneElevage.getTypeZoneElevage() == TypeZoneElevage.Ruminant) {
                                            throw new IllegalArgumentException(
                                                    "Impossible d'affecter une Volaille à une zone de Ruminant"
                                            );
                                        }
                                    }

                                    // =========================
                                    // LOGIQUE AFFECTATION
                                    // =========================
                                    affectationLogic.accept(selectedZone, selectedEntity);

                                    ZoneState.refresh();

                                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                    alert.setHeaderText(null);
                                    alert.setContentText("Affectation réussie");
                                    alert.showAndWait();

                                    stage.close();

                                }

                                catch (IllegalArgumentException ex) {

                                    Alert alert = new Alert(Alert.AlertType.ERROR);
                                    alert.setHeaderText(null);
                                    alert.setContentText(ex.getMessage());
                                    alert.showAndWait();
                                }
                            },



                            refresh -> {
                                ZoneState.setZoneRefresh(() ->
                                        refresh.accept(ZoneState.searchZone(""))
                                );
                            }
                    );

                    root.setCenter(zoneTable);
                },

                refresh -> {
                    entityRefreshRegister.run();
                }
        );

        root.setCenter(entityTable);

        Scene scene = new Scene(root, 900, 600);

        scene.getStylesheets().add(
                PageZone.class.getResource("style.css").toExternalForm()
        );

        stage.setScene(scene);

        stage.showAndWait();
    }
}