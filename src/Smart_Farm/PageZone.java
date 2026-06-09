package Smart_Farm;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
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
        // STATS + ADD  (une seule ligne coh\u00E9rente)
        // =========================
        HBox statsHeader = new HBox(20);
        statsHeader.setPadding(new Insets(20));
        statsHeader.setAlignment(Pos.CENTER);
        statsHeader.getChildren().addAll(
                UIFactory.createLiveNumberDisplay("Zone Culture",  ZoneState.nbrZoneCultureProperty(),  220, 90),
                UIFactory.createLiveNumberDisplay("Zone Elevage",  ZoneState.nbrZoneElevageProperty(),  220, 90),
                UIFactory.createLiveNumberDisplay("Zone Aquacole", ZoneState.nbrZoneAquacoleProperty(), 220, 90),
                UIFactory.createAddCard("Total", ZoneState.nbrZonesProperty(), "\u2795 Ajouter Zone", () -> showAddZoneForm(), 220, 90)
        );
        UIFactory.expandToFill(statsHeader);
        center.getChildren().add(statsHeader);

        // =========================
        // AFFECTATION CARDS
        // =========================
        center.getChildren().add(
                UIFactory.createAnimatedTitle("\uD83C\uDF04 Affecter une culture ou un animal \u00E0 une zone")
        );

        center.getChildren().add(
                createDoubleActionCard(
                        "Nbr Cultures",
                        FarmState.nbrCulturesProperty(),
                        "Nbr Animaux",
                        AnimalState.nbrAnimalsProperty(),
                        700,
                        110
                )
        );

        // =========================
        // TABLE ZONES
        // =========================

        center.getChildren().add(
                UIFactory.createAnimatedTitle("Vue d'ensemble des Zones ( cliquer pour modifier ou d\u00E9sactiver )")
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
        // CARTE VISUELLE DES ZONES
        // =========================
        HBox mapTitleRow = UIFactory.createAnimatedTitle("Carte Visuelle des Zones");
        Button fsBtnTitle = new Button("⛶");
        fsBtnTitle.setStyle(
                "-fx-background-color: transparent; -fx-border-color: #3B7249;" +
                "-fx-border-radius: 6; -fx-border-width: 1.5; -fx-font-size: 15px;" +
                "-fx-text-fill: #3B7249; -fx-cursor: hand; -fx-padding: 2 9;"
        );
        fsBtnTitle.setTooltip(new Tooltip("Plein écran"));
        Region mapTitleSpacer = new Region();
        HBox.setHgrow(mapTitleSpacer, Priority.ALWAYS);
        mapTitleRow.getChildren().addAll(mapTitleSpacer, fsBtnTitle);
        fsBtnTitle.setOnAction(e -> showFullscreenMap());
        center.getChildren().add(mapTitleRow);
        center.getChildren().add(createZoneMapCard());


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
        HBox container = new HBox(20);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(0));

        // ---- Card 1: Cultures ----
        VBox card1 = new VBox(8);
        card1.setAlignment(Pos.CENTER);
        card1.setPadding(new Insets(22, 40, 22, 40));
        card1.getStyleClass().add("widget-card");
        HBox.setHgrow(card1, Priority.ALWAYS);

        Label icon1 = new Label("🌱");
        icon1.setStyle("-fx-font-size: 28px;");

        Label lbl1 = new Label(title1);
        lbl1.setStyle("-fx-font-size: 11px; -fx-text-fill: #888888; -fx-font-weight: bold;");

        Label val1 = new Label();
        val1.textProperty().bind(value1.asString());
        val1.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #2E5E3B;");

        Button btn1 = createActionButton("+ Affecter Culture", () -> {});
        btn1.setMaxWidth(Double.MAX_VALUE);
        btn1.setOnAction(e -> showAssignToZoneForm(
                "Affecter Culture",
                List.of("Type", "Croissance", "Cultivation", "Recolte", "Ph", "Humidité"),
                FarmState::searchCultureByType,
                FarmState::mapCulture,
                (zone, culture) -> {
                    if (!(zone instanceof ZoneCulture zc))
                        throw new IllegalArgumentException("Cette zone n'accepte pas les cultures");
                    zc.ajouterCulture((Culture) culture);
                },
                () -> {}
        ));

        card1.getChildren().addAll(icon1, lbl1, val1, btn1);

        // ---- Card 2: Animaux ----
        VBox card2 = new VBox(8);
        card2.setAlignment(Pos.CENTER);
        card2.setPadding(new Insets(22, 40, 22, 40));
        card2.getStyleClass().add("widget-card");
        HBox.setHgrow(card2, Priority.ALWAYS);

        Label icon2 = new Label("🐄");
        icon2.setStyle("-fx-font-size: 28px;");

        Label lbl2 = new Label(title2);
        lbl2.setStyle("-fx-font-size: 11px; -fx-text-fill: #888888; -fx-font-weight: bold;");

        Label val2 = new Label();
        val2.textProperty().bind(value2.asString());
        val2.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #2E5E3B;");

        Button btn2 = createActionButton("+ Affecter Animal", () -> {});
        btn2.setMaxWidth(Double.MAX_VALUE);
        btn2.setOnAction(e -> showAssignToZoneForm(
                "Affecter Animal",
                List.of("ID", "Nom", "Type", "Age", "Poids"),
                AnimalState::searchAnimal,
                AnimalState::mapAnimal,
                (zone, animalObj) -> {
                    Animal animal = (Animal) animalObj;
                    if (animal instanceof Aquacole aqua) {
                        if (!(zone instanceof ZoneAquacole za))
                            throw new IllegalArgumentException("Un animal aquacole doit être dans une zone aquacole");
                        za.ajouterAquacole(aqua);
                        return;
                    }
                    if (!(zone instanceof ZoneElevage ze))
                        throw new IllegalArgumentException("Cet animal doit être dans une zone d'élevage");
                    if (animal instanceof Ruminant r) ze.ajouterRuminant(r);
                    else if (animal instanceof Volaille v) ze.ajouterVollaile(v);
                },
                () -> {}
        ));

        card2.getChildren().addAll(icon2, lbl2, val2, btn2);

        container.getChildren().addAll(card1, card2);
        return container;
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

        // ---- Header ----
        HBox header = new HBox();
        header.setPadding(new Insets(18, 25, 18, 25));
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("form-header");
        Label headerLbl = new Label("Ajouter une Zone");
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

        // NOM ZONE
        Label nameLabel = new Label("Nom Zone");
        nameLabel.getStyleClass().add("form-label");
        TextField nameField = new TextField();
        nameField.setPromptText("Ex: Zone Nord");
        nameField.setMaxWidth(Double.MAX_VALUE);

        // TYPE ZONE
        Label typeLabel = new Label("Type Zone");
        typeLabel.getStyleClass().add("form-label");

        RadioButton cultureBtn = new RadioButton("Culture");
        RadioButton elevageBtn = new RadioButton("Elevage");
        RadioButton aquaBtn    = new RadioButton("Aquacole");

        ToggleGroup group = new ToggleGroup();
        cultureBtn.setToggleGroup(group);
        elevageBtn.setToggleGroup(group);
        aquaBtn.setToggleGroup(group);
        cultureBtn.setSelected(true);

        HBox typeBox = new HBox(14, cultureBtn, elevageBtn, aquaBtn);
        typeBox.setAlignment(Pos.CENTER_LEFT);

        // DYNAMIC AREA
        VBox dynamicBox = new VBox(15);

        Runnable updateUI = () -> {
            dynamicBox.getChildren().clear();

            if (cultureBtn.isSelected()) {
                Button ok = UIFactory.createActionButton("Valider", () -> {
                    String name = nameField.getText();
                    if (name == null || name.isEmpty()) return;
                    ZoneCulture z = new ZoneCulture(name.hashCode(), name, TypeZone.culture);
                    ZoneState.addZone(z);
                    result[0] = z;
                    stage.close();
                });
                ok.setMaxWidth(Double.MAX_VALUE);
                dynamicBox.getChildren().add(ok);
            }

            else if (elevageBtn.isSelected()) {
                ComboBox<TypeZoneElevage> typeElevage = new ComboBox<>();
                typeElevage.getItems().addAll(TypeZoneElevage.values());
                typeElevage.setValue(TypeZoneElevage.values()[0]);
                typeElevage.setMaxWidth(Double.MAX_VALUE);

                SimpleDoubleProperty latMinProp = new SimpleDoubleProperty(-45);
                SimpleDoubleProperty latMaxProp = new SimpleDoubleProperty(45);
                SimpleDoubleProperty lonMinProp = new SimpleDoubleProperty(-90);
                SimpleDoubleProperty lonMaxProp = new SimpleDoubleProperty(90);

                Pane geoPane = createGeoLimitsPane(latMinProp, latMaxProp, lonMinProp, lonMaxProp);

                Label coordLabel = new Label();
                coordLabel.textProperty().bind(javafx.beans.binding.Bindings.createStringBinding(
                        () -> String.format("Lat [%d, %d]   Lon [%d, %d]",
                                (int) latMinProp.get(), (int) latMaxProp.get(),
                                (int) lonMinProp.get(), (int) lonMaxProp.get()),
                        latMinProp, latMaxProp, lonMinProp, lonMaxProp));
                coordLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #3B7249; -fx-font-size: 13px;");

                Button ok = UIFactory.createActionButton("Valider", () -> {
                    String name = nameField.getText();
                    if (name == null || name.isEmpty()) return;
                    GeographicalLimits limits = new GeographicalLimits(
                            "Zone Elevage",
                            latMinProp.get(), latMaxProp.get(),
                            lonMinProp.get(), lonMaxProp.get());
                    ZoneElevage z = new ZoneElevage(
                            name.hashCode(), name, TypeZone.elevage,
                            typeElevage.getValue(), limits);
                    ZoneState.addZone(z);
                    result[0] = z;
                    stage.close();
                });
                ok.setMaxWidth(Double.MAX_VALUE);

                Label hint = new Label("Glisser les bords verts pour délimiter la zone :");
                hint.setStyle("-fx-text-fill: #555555; -fx-font-size: 12px;");

                Label elevTypeLabel = new Label("Type Elevage");
                elevTypeLabel.getStyleClass().add("form-label");
                elevTypeLabel.setMinWidth(130);
                HBox typeElevRow = new HBox(15, elevTypeLabel, typeElevage);
                typeElevRow.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(typeElevage, Priority.ALWAYS);

                VBox elevageBox = new VBox(12, typeElevRow, hint, geoPane, coordLabel, ok);
                elevageBox.setAlignment(Pos.CENTER_LEFT);
                dynamicBox.getChildren().add(elevageBox);
            }

            else if (aquaBtn.isSelected()) {
                Button ok = UIFactory.createActionButton("Valider", () -> {
                    String name = nameField.getText();
                    if (name == null || name.isEmpty()) return;
                    ZoneAquacole z = new ZoneAquacole(name.hashCode(), name, TypeZone.aquacole);
                    ZoneState.addZone(z);
                    result[0] = z;
                    stage.close();
                });
                ok.setMaxWidth(Double.MAX_VALUE);
                dynamicBox.getChildren().add(ok);
            }
        };

        cultureBtn.setOnAction(e -> updateUI.run());
        elevageBtn.setOnAction(e -> updateUI.run());
        aquaBtn.setOnAction(e -> updateUI.run());
        updateUI.run();

        grid.add(nameLabel,  0, 0); grid.add(nameField, 1, 0);
        grid.add(typeLabel,  0, 1); grid.add(typeBox,   1, 1);
        grid.add(dynamicBox, 0, 2, 2, 1);

        VBox root = new VBox(header, grid);
        root.getStyleClass().add("form-global");

        Scene scene = new Scene(root, 680, 640);
        scene.getStylesheets().add(PageZone.class.getResource("style.css").toExternalForm());
        stage.setScene(scene);
        stage.setResizable(false);
        stage.showAndWait();

        return result[0];
    }

    // =========================================
    // GEO LIMITS VISUAL PANE
    // =========================================
    private static Pane createGeoLimitsPane(
            SimpleDoubleProperty latMin,
            SimpleDoubleProperty latMax,
            SimpleDoubleProperty lonMin,
            SimpleDoubleProperty lonMax
    ) {
        final double W = 520;
        final double H = 270;
        final double HANDLE = 10;

        Pane pane = new Pane();
        pane.setPrefSize(W, H);
        pane.setMinSize(W, H);
        pane.setMaxSize(W, H);
        pane.setStyle(
            "-fx-background-color: #e8f5e9;" +
            "-fx-border-color: #3D7A4E;" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;"
        );

        // Grid lines
        for (int lon = -180; lon <= 180; lon += 45) {
            double x = (lon + 180) / 360.0 * W;
            Line v = new Line(x, 0, x, H);
            v.setStroke(Color.color(0.3, 0.55, 0.3, 0.22));
            v.setStrokeWidth(1);
            pane.getChildren().add(v);
            Label lbl = new Label(lon + "°");
            lbl.setStyle("-fx-font-size: 9px; -fx-text-fill: #6a9a6a;");
            lbl.setLayoutX(x + 2); lbl.setLayoutY(H - 14);
            pane.getChildren().add(lbl);
        }
        for (int lat = -90; lat <= 90; lat += 30) {
            double y = (90 - lat) / 180.0 * H;
            Line h = new Line(0, y, W, y);
            h.setStroke(Color.color(0.3, 0.55, 0.3, 0.22));
            h.setStrokeWidth(1);
            pane.getChildren().add(h);
            Label lbl = new Label(lat + "°");
            lbl.setStyle("-fx-font-size: 9px; -fx-text-fill: #6a9a6a;");
            lbl.setLayoutX(2); lbl.setLayoutY(y - 12);
            pane.getChildren().add(lbl);
        }

        // Zone fill
        Rectangle zone = new Rectangle();
        zone.setFill(Color.color(0.18, 0.47, 0.18, 0.28));
        zone.setStroke(Color.web("#2E5E3B"));
        zone.setStrokeWidth(2.5);
        pane.getChildren().add(zone);

        // Edge drag handles
        Rectangle topH    = new Rectangle();
        Rectangle bottomH = new Rectangle();
        Rectangle leftH   = new Rectangle();
        Rectangle rightH  = new Rectangle();

        Color handleColor = Color.color(0.18, 0.47, 0.18, 0.75);
        topH.setFill(handleColor);    topH.setArcWidth(4);    topH.setArcHeight(4);
        bottomH.setFill(handleColor); bottomH.setArcWidth(4); bottomH.setArcHeight(4);
        leftH.setFill(handleColor);   leftH.setArcWidth(4);   leftH.setArcHeight(4);
        rightH.setFill(handleColor);  rightH.setArcWidth(4);  rightH.setArcHeight(4);

        topH.setCursor(javafx.scene.Cursor.N_RESIZE);
        bottomH.setCursor(javafx.scene.Cursor.S_RESIZE);
        leftH.setCursor(javafx.scene.Cursor.W_RESIZE);
        rightH.setCursor(javafx.scene.Cursor.E_RESIZE);

        pane.getChildren().addAll(topH, bottomH, leftH, rightH);

        // Value labels on each edge
        String lblStyle = "-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: white;" +
                          "-fx-background-color: #2E5E3B; -fx-padding: 2 6;" +
                          "-fx-background-radius: 4;";
        Label lblN = new Label(); lblN.setStyle(lblStyle);
        Label lblS = new Label(); lblS.setStyle(lblStyle);
        Label lblW = new Label(); lblW.setStyle(lblStyle);
        Label lblE = new Label(); lblE.setStyle(lblStyle);
        pane.getChildren().addAll(lblN, lblS, lblW, lblE);

        // Layout updater
        Runnable update = () -> {
            double x1 = Math.max(0,      Math.min(W, (lonMin.get() + 180) / 360.0 * W));
            double x2 = Math.max(x1 + 16, Math.min(W, (lonMax.get() + 180) / 360.0 * W));
            double y1 = Math.max(0,      Math.min(H, (90 - latMax.get()) / 180.0 * H));
            double y2 = Math.max(y1 + 16, Math.min(H, (90 - latMin.get()) / 180.0 * H));

            zone.setX(x1); zone.setY(y1);
            zone.setWidth(x2 - x1); zone.setHeight(y2 - y1);

            topH.setX(x1);           topH.setY(y1 - HANDLE / 2); topH.setWidth(x2 - x1); topH.setHeight(HANDLE);
            bottomH.setX(x1);        bottomH.setY(y2 - HANDLE / 2); bottomH.setWidth(x2 - x1); bottomH.setHeight(HANDLE);
            leftH.setX(x1 - HANDLE / 2); leftH.setY(y1); leftH.setWidth(HANDLE); leftH.setHeight(y2 - y1);
            rightH.setX(x2 - HANDLE / 2); rightH.setY(y1); rightH.setWidth(HANDLE); rightH.setHeight(y2 - y1);

            double cx = (x1 + x2) / 2;
            double cy = (y1 + y2) / 2;
            lblN.setText(String.format("N %d°", (int) latMax.get()));
            lblS.setText(String.format("S %d°", (int) latMin.get()));
            lblW.setText(String.format("W %d°", (int) lonMin.get()));
            lblE.setText(String.format("E %d°", (int) lonMax.get()));
            lblN.setLayoutX(cx - 22); lblN.setLayoutY(y1 + 2);
            lblS.setLayoutX(cx - 22); lblS.setLayoutY(y2 - 20);
            lblW.setLayoutX(x1 + 3);  lblW.setLayoutY(cy - 10);
            lblE.setLayoutX(x2 - 50); lblE.setLayoutY(cy - 10);
        };

        latMin.addListener((o, v, n) -> update.run());
        latMax.addListener((o, v, n) -> update.run());
        lonMin.addListener((o, v, n) -> update.run());
        lonMax.addListener((o, v, n) -> update.run());

        // Drag logic
        double[] drag = {0};

        topH.setOnMousePressed(e -> drag[0] = e.getSceneY());
        topH.setOnMouseDragged(e -> {
            double dy = e.getSceneY() - drag[0]; drag[0] = e.getSceneY();
            double newY = (90 - latMax.get()) / 180.0 * H + dy;
            newY = Math.max(0, Math.min((90 - latMin.get()) / 180.0 * H - 16, newY));
            latMax.set(90 - newY / H * 180);
        });

        bottomH.setOnMousePressed(e -> drag[0] = e.getSceneY());
        bottomH.setOnMouseDragged(e -> {
            double dy = e.getSceneY() - drag[0]; drag[0] = e.getSceneY();
            double newY = (90 - latMin.get()) / 180.0 * H + dy;
            newY = Math.max((90 - latMax.get()) / 180.0 * H + 16, Math.min(H, newY));
            latMin.set(90 - newY / H * 180);
        });

        leftH.setOnMousePressed(e -> drag[0] = e.getSceneX());
        leftH.setOnMouseDragged(e -> {
            double dx = e.getSceneX() - drag[0]; drag[0] = e.getSceneX();
            double newX = (lonMin.get() + 180) / 360.0 * W + dx;
            newX = Math.max(0, Math.min((lonMax.get() + 180) / 360.0 * W - 16, newX));
            lonMin.set(newX / W * 360 - 180);
        });

        rightH.setOnMousePressed(e -> drag[0] = e.getSceneX());
        rightH.setOnMouseDragged(e -> {
            double dx = e.getSceneX() - drag[0]; drag[0] = e.getSceneX();
            double newX = (lonMax.get() + 180) / 360.0 * W + dx;
            newX = Math.max((lonMin.get() + 180) / 360.0 * W + 16, Math.min(W, newX));
            lonMax.set(newX / W * 360 - 180);
        });

        update.run();
        return pane;
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
        RadioButton suspendedBtn = new RadioButton("Suspendu");

        ToggleGroup group = new ToggleGroup();

        activeBtn.setToggleGroup(group);
        inactiveBtn.setToggleGroup(group);
        suspendedBtn.setToggleGroup(group);

// =========================
// INIT VALUE
// =========================
        if (z.getStatut() == StatutZone.ACTIVE) {
            activeBtn.setSelected(true);
        }
        else if (z.getStatut() == StatutZone.INACTIVE) {
            inactiveBtn.setSelected(true);
        }
        else {
            suspendedBtn.setSelected(true);
        }

        VBox statusBox = new VBox(10, activeBtn, inactiveBtn, suspendedBtn);

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
                    }
                    else if (inactiveBtn.isSelected()) {
                        z.setStatut(StatutZone.INACTIVE);
                    }
                    else {
                        ZoneState.setZoneStatut(z,
                                activeBtn.isSelected()
                                        ? StatutZone.ACTIVE
                                        : StatutZone.SUSPENDU
                        );                    }

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
    // CARTE VISUELLE DES ZONES
    // =========================================
    private static VBox createZoneMapCard() {

        final double W = 920;
        final double H = 480;

        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.getStyleClass().add("culture-list-card");

        Pane mapPane = new Pane();
        mapPane.setPrefSize(W, H);
        mapPane.setMinSize(W, H);
        mapPane.setMaxSize(W, H);
        mapPane.setStyle(
                "-fx-background-color: #e8f5e9;" +
                "-fx-border-color: #3D7A4E;" +
                "-fx-border-width: 1.5;" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;"
        );
        mapPane.setClip(new Rectangle(W, H));

        Runnable buildMap = buildMapContent(mapPane, W, H);
        buildMap.run();
        ZoneState.nbrZonesProperty().addListener((obs, o, n) -> Platform.runLater(buildMap));
        ZoneState.setMapRefresh(buildMap);

        card.getChildren().add(mapPane);
        return card;
    }

    // =========================================
    // CONTENU DE LA CARTE (partagé carte + plein écran)
    // =========================================
    private static Runnable buildMapContent(Pane mapPane, double W, double H) {
        String[] ZONE_COLORS = {"#e65100","#1565C0","#6a1b9a","#00695c","#c62828","#f57f17"};
        String[] ZONE_BG     = {"#fff8e1","#e3f2fd","#f3e5f5","#e0f2f1","#ffebee","#fffde7"};

        return () -> {
            mapPane.getChildren().clear();

            List<ZoneElevage> elevZones = ZoneState.getZones().stream()
                    .filter(z -> z instanceof ZoneElevage)
                    .map(z -> (ZoneElevage) z)
                    .filter(z -> z.getLimitZone() != null)
                    .filter(z -> z.getStatut() == StatutZone.ACTIVE)
                    .collect(java.util.stream.Collectors.toList());

            if (elevZones.isEmpty()) {
                Label none = new Label("Aucune zone d'élevage avec coordonnées.\nAjoutez une zone Elevage pour la voir apparaître ici.");
                none.setStyle("-fx-text-fill: #888; -fx-font-size: 13px;");
                none.setWrapText(true);
                none.setLayoutX(W / 2 - 200);
                none.setLayoutY(H / 2 - 20);
                mapPane.getChildren().add(none);
                return;
            }

            // Viewport basé sur l'étendue réelle de toutes les zones (min/max lat/lon)
            double minLat = elevZones.stream().mapToDouble(z -> z.getLimitZone().getLatMin()).min().orElse(40);
            double maxLat = elevZones.stream().mapToDouble(z -> z.getLimitZone().getLatMax()).max().orElse(46);
            double minLon = elevZones.stream().mapToDouble(z -> z.getLimitZone().getLonMin()).min().orElse(-5);
            double maxLon = elevZones.stream().mapToDouble(z -> z.getLimitZone().getLonMax()).max().orElse(10);

            double latSpread = Math.max(maxLat - minLat, 1.0);
            double lonSpread = Math.max(maxLon - minLon, 1.0);
            final double vLatMin = minLat - latSpread * 0.12;
            final double vLatMax = maxLat + latSpread * 0.12;
            final double vLonMin = minLon - lonSpread * 0.12;
            final double vLonMax = maxLon + lonSpread * 0.12;
            final double totalLat = vLatMax - vLatMin;
            final double totalLon = vLonMax - vLonMin;

            int nX = 6, nY = 5;
            for (int i = 0; i <= nX; i++) {
                double lon = vLonMin + i * totalLon / nX;
                double x = (lon - vLonMin) / totalLon * W;
                Line vl = new Line(x, 0, x, H);
                vl.setStroke(Color.color(0.3, 0.55, 0.3, 0.22));
                vl.setStrokeWidth(1);
                mapPane.getChildren().add(vl);
                Label gl = new Label(String.format("%.1f°", lon));
                gl.setStyle("-fx-font-size: 9px; -fx-text-fill: #6a9a6a;");
                gl.setLayoutX(x + 2); gl.setLayoutY(H - 14);
                mapPane.getChildren().add(gl);
            }
            for (int i = 0; i <= nY; i++) {
                double lat = vLatMax - i * totalLat / nY;
                double y = (vLatMax - lat) / totalLat * H;
                Line hl = new Line(0, y, W, y);
                hl.setStroke(Color.color(0.3, 0.55, 0.3, 0.22));
                hl.setStrokeWidth(1);
                mapPane.getChildren().add(hl);
                Label gl = new Label(String.format("%.1f°", lat));
                gl.setStyle("-fx-font-size: 9px; -fx-text-fill: #6a9a6a;");
                gl.setLayoutX(2); gl.setLayoutY(y - 12);
                mapPane.getChildren().add(gl);
            }

            Label compass = new Label("N");
            compass.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #3B7249;");
            compass.setLayoutX(W - 22); compass.setLayoutY(4);
            mapPane.getChildren().add(compass);

            for (int idx = 0; idx < elevZones.size(); idx++) {
                ZoneElevage ze = elevZones.get(idx);
                GeographicalLimits lim = ze.getLimitZone();

                double x1 = (lim.getLonMin() - vLonMin) / totalLon * W;
                double x2 = (lim.getLonMax() - vLonMin) / totalLon * W;
                double y1 = (vLatMax - lim.getLatMax()) / totalLat * H;
                double y2 = (vLatMax - lim.getLatMin()) / totalLat * H;

                double cx = (x1 + x2) / 2.0;
                double cy = (y1 + y2) / 2.0;

                // Taille proportionnelle avec plancher minimal pour la visibilité/clic
                double drawW = Math.max(x2 - x1, 16.0);
                double drawH = Math.max(y2 - y1, 10.0);
                double rx1 = cx - drawW / 2.0;
                double ry1 = cy - drawH / 2.0;

                String col = ZONE_COLORS[idx % ZONE_COLORS.length];
                String bgC = ZONE_BG[idx % ZONE_BG.length];

                Rectangle rect = new Rectangle(rx1, ry1, drawW, drawH);
                rect.setFill(Color.web(bgC, 0.65));
                rect.setStroke(Color.web(col));
                rect.setStrokeWidth(2.5);
                rect.setArcWidth(4); rect.setArcHeight(4);
                rect.setCursor(javafx.scene.Cursor.HAND);
                rect.setOnMouseClicked(e -> showZoneEditForm(ze));
                mapPane.getChildren().add(rect);

                Label nameLbl = new Label(ze.getNom());
                nameLbl.setStyle(
                        "-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: white;" +
                        "-fx-background-color: " + col + "; -fx-padding: 3 8;" +
                        "-fx-background-radius: 5; -fx-cursor: hand;"
                );
                nameLbl.setMaxWidth(120);
                nameLbl.setWrapText(true);
                nameLbl.setLayoutX(cx - 55);
                nameLbl.setLayoutY(cy - 12);
                nameLbl.setOnMouseClicked(e -> showZoneEditForm(ze));
                mapPane.getChildren().add(nameLbl);
            }
        };
    }

    // =========================================
    // PLEIN ÉCRAN CARTE
    // =========================================
    private static void showFullscreenMap() {
        Stage stage = new Stage();
        stage.setTitle("Carte Visuelle des Zones — Plein écran");
        stage.initModality(Modality.APPLICATION_MODAL);

        final double MAP_W = 2000;
        final double MAP_H = 1100;

        Pane mapPane = new Pane();
        mapPane.setPrefSize(MAP_W, MAP_H);
        mapPane.setMinSize(MAP_W, MAP_H);
        mapPane.setMaxSize(MAP_W, MAP_H);
        mapPane.setStyle("-fx-background-color: #e8f5e9;");

        buildMapContent(mapPane, MAP_W, MAP_H).run();

        Group zoomGroup = new Group(mapPane);

        StackPane viewport = new StackPane(zoomGroup);
        viewport.setStyle("-fx-background-color: #d4e8d4;");

        // Zoom vers le curseur au scroll
        viewport.setOnScroll(e -> {
            double oldScale = zoomGroup.getScaleX();
            double factor   = e.getDeltaY() > 0 ? 1.12 : 1.0 / 1.12;
            double newScale = Math.max(0.1, Math.min(6.0, oldScale * factor));
            double mx = e.getX() - viewport.getWidth()  / 2.0;
            double my = e.getY() - viewport.getHeight() / 2.0;
            zoomGroup.setScaleX(newScale);
            zoomGroup.setScaleY(newScale);
            zoomGroup.setTranslateX(mx - (mx - zoomGroup.getTranslateX()) * newScale / oldScale);
            zoomGroup.setTranslateY(my - (my - zoomGroup.getTranslateY()) * newScale / oldScale);
            e.consume();
        });

        // Déplacement par glisser-déposer
        double[] drag = {0, 0, 0, 0};
        viewport.setOnMousePressed(e -> {
            drag[0] = e.getSceneX(); drag[1] = e.getSceneY();
            drag[2] = zoomGroup.getTranslateX(); drag[3] = zoomGroup.getTranslateY();
            viewport.setCursor(javafx.scene.Cursor.CLOSED_HAND);
        });
        viewport.setOnMouseDragged(e -> {
            zoomGroup.setTranslateX(drag[2] + e.getSceneX() - drag[0]);
            zoomGroup.setTranslateY(drag[3] + e.getSceneY() - drag[1]);
        });
        viewport.setOnMouseReleased(e -> viewport.setCursor(javafx.scene.Cursor.OPEN_HAND));
        viewport.setCursor(javafx.scene.Cursor.OPEN_HAND);

        // Barre inférieure
        Button closeBtn = new Button("✕  Fermer");
        closeBtn.setStyle(
                "-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold;" +
                "-fx-background-radius: 8; -fx-padding: 8 24; -fx-font-size: 13px; -fx-cursor: hand;"
        );
        closeBtn.setOnAction(e -> stage.close());

        Label hint = new Label("Scroll pour zoomer  ·  Glisser pour déplacer  ·  Échap pour fermer");
        hint.setStyle("-fx-text-fill: #666; -fx-font-size: 11px; -fx-font-style: italic;");

        Region toolbarSpacer = new Region();
        HBox.setHgrow(toolbarSpacer, Priority.ALWAYS);

        HBox toolbar = new HBox(14, hint, toolbarSpacer, closeBtn);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(8, 16, 8, 16));
        toolbar.setStyle("-fx-background-color: #f8fdf8; -fx-border-color: #c8e0c8; -fx-border-width: 1 0 0 0;");

        BorderPane root = new BorderPane(viewport);
        root.setBottom(toolbar);

        Scene scene = new Scene(root);
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) stage.close();
        });

        stage.setScene(scene);
        stage.setMaximized(true);

        // Ajuster l'échelle initiale pour que la carte remplisse la fenêtre
        // Platform.runLater ensures the StackPane layout pass has completed so
        // layoutX/Y are set correctly before scale is applied — otherwise the
        // pivot would be off-center and the map appears shifted to the right.
        stage.setOnShown(e -> Platform.runLater(() -> {
            double vpW = viewport.getWidth();
            double vpH = viewport.getHeight();
            if (vpW > 0 && vpH > 0) {
                double scale = Math.min(vpW / MAP_W, vpH / MAP_H) * 0.92;
                zoomGroup.setScaleX(scale);
                zoomGroup.setScaleY(scale);
                zoomGroup.setTranslateX(0);
                zoomGroup.setTranslateY(0);
            }
        }));

        stage.show();
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