package Smart_Farm;

import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ObservableNumberValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.util.function.Consumer;
import java.util.*;


import java.time.LocalDate;
import java.util.function.Function;

class UIFactory {

    // =========================================
    // TOP BAR
    // =========================================
    public static HBox createTopBar(String pageName, String farmName, javafx.scene.Node bellNode) {

        HBox topBar = new HBox();
        topBar.setPadding(new Insets(15));
        topBar.setSpacing(20);
        topBar.setAlignment(Pos.CENTER_LEFT);

        topBar.getStyleClass().add("top-bar");

        // PAGE NAME
        Label pageLabel = new Label(pageName);
        pageLabel.getStyleClass().add("top-title");

        // DATE
        Label dateLabel = new Label(LocalDate.now().toString());
        dateLabel.getStyleClass().add("top-date");

        // FARM NAME
        Label farmLabel = new Label("🌱 " + farmName);
        farmLabel.getStyleClass().add("farm-name");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(
                pageLabel,
                spacer,
                dateLabel,
                farmLabel,
                bellNode
        );

        return topBar;
    }

    // =========================================
    // NAV BAR
    // =========================================
    public static VBox createNavBar(BorderPane root, javafx.scene.Node bellNode) {

        VBox navBar = new VBox();
        navBar.setPadding(new Insets(20));
        navBar.setSpacing(15);

        navBar.getStyleClass().add("nav-bar");

        // =================================
        // BUTTONS
        // =================================

        Button zoneBtn = createNavButton("🌍 Zones");
        Button cultureBtn = createNavButton("🌱 Cultures");
        Button animalBtn = createNavButton("🐄 Animaux");
        Button capteurBtn = createNavButton("📡 Capteurs");
        Button alertBtn = createNavButton("🚨 Alertes");

        // =================================
        // ACTIONS
        // =================================

        zoneBtn.setOnAction(e -> {
            root.setCenter(Pages.zonePage());
            root.setTop(createTopBar(
                    "Gestion des Zones",
                    "Smart Farm",
                    bellNode
            ));
        });

        cultureBtn.setOnAction(e -> {
            root.setCenter(Pages.culturePage());
            root.setTop(createTopBar(
                    "Gestion des Cultures",
                    "Smart Farm",
                    bellNode
            ));
        });

        animalBtn.setOnAction(e -> {
            root.setCenter(Pages.animalPage());
            root.setTop(createTopBar(
                    "Gestion des Animaux",
                    "Smart Farm",
                    bellNode
            ));
        });

        capteurBtn.setOnAction(e -> {
            root.setCenter(Pages.capteurPage());
            root.setTop(createTopBar(
                    "Gestion des Capteurs",
                    "Smart Farm",
                    bellNode
            ));
        });

        alertBtn.setOnAction(e -> {
            root.setCenter(Pages.alertPage());
            root.setTop(createTopBar(
                    "Gestion des Alertes",
                    "Smart Farm",
                    bellNode
            ));
        });

        navBar.getChildren().addAll(
                zoneBtn,
                cultureBtn,
                animalBtn,
                capteurBtn,
                alertBtn
        );

        return navBar;
    }

    // =========================================
    // NAV BUTTON STYLE
    // =========================================
    private static Button createNavButton(String text) {

        Button button = new Button(text);

        button.setPrefWidth(220);
        button.setPrefHeight(45);

        button.getStyleClass().add("nav-button");

        return button;
    }

    // =========================================
    // BUTTON ACTION
    // =========================================

    public static Button createActionButton(String text, Runnable action) {

        Button button = new Button(text);

        // =========================
        // SIZE (comme navbar)
        // =========================
        button.setPrefWidth(220);
        button.setPrefHeight(45);

        // =========================
        // STYLE NAVBAR (IMPORTANT)
        // =========================
        button.getStyleClass().add("primary-button");

        // =========================
        // ACTION
        // =========================
        button.setOnAction(e -> action.run());

        return button;
    }

    // =========================================
    // SEARCH BAR
    // =========================================

    public static TextField createSearchBar(Consumer<String> onEnter) {

        TextField searchField = new TextField();

        searchField.setPromptText("🔎 Rechercher...");

        // =========================
        // SIZE CONTROL
        // =========================
        searchField.setPrefWidth(250);
        searchField.setMaxWidth(250);

        searchField.setPrefHeight(35);

        searchField.getStyleClass().add("search-field");

        // ENTER
        searchField.setOnAction(e -> {
            onEnter.accept(searchField.getText());
        });

        return searchField;
    }

    // =========================================
    // STAT CARD
    // =========================================



    public static HBox createLiveNumberDisplay(
            String labelText,
            ObservableNumberValue value,
            double width,
            double height
    ) {

        HBox box = new HBox();
        box.setSpacing(10);
        box.setPadding(new Insets(10));
        box.setAlignment(Pos.CENTER_LEFT);

        box.getStyleClass().add("widget-card");

        box.setPrefSize(width, height);
        box.setMinSize(width, height);
        box.setMaxSize(width, height);

        Label label = new Label(labelText + " : ");
        label.getStyleClass().add("top-title");

        Label valueLabel = new Label();
        valueLabel.getStyleClass().add("top-date");

        valueLabel.textProperty().bind(
                Bindings.createStringBinding(
                        () -> String.valueOf(value.getValue()),
                        value
                )
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        box.getChildren().addAll(label, spacer, valueLabel);

        return box;
    }

    public static HBox RajoutterCard(
            String title,
            IntegerProperty value,
            String buttonText,
            Runnable action,
            double width,
            double height
    ) {

        HBox card = new HBox();
        card.setSpacing(20);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("widget-card");

        card.setPrefSize(width, height);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("top-title");

        Label valueLabel = new Label();
        valueLabel.getStyleClass().add("top-date");

        //  IMPORTANT BIND LIVE
        valueLabel.textProperty().bind(value.asString());

        Button btn = createActionButton(buttonText, action);

        VBox box = new VBox(10, titleLabel, valueLabel, btn);
        box.setAlignment(Pos.CENTER);

        card.getChildren().add(box);

        return card;
    }

    public static HBox ThreeActionCard(
            String title,
            IntegerProperty value,

            String text1, Runnable action1,
            String text2, Runnable action2,
            String text3, Runnable action3,

            double width,
            double height
    ) {

        HBox card = new HBox();
        card.setSpacing(20);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("widget-card");

        card.setPrefSize(width, height);

        // =========================
        // TITLE
        // =========================
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("top-title");

        // =========================
        // VALUE (LIVE)
        // =========================
        Label valueLabel = new Label();
        valueLabel.getStyleClass().add("top-date");
        valueLabel.textProperty().bind(value.asString());

        // =========================
        // BUTTONS
        // =========================
        Button btn1 = createActionButton(text1, action1);
        Button btn2 = createActionButton(text2, action2);
        Button btn3 = createActionButton(text3, action3);

        HBox buttons = new HBox(10, btn1, btn2, btn3);
        buttons.setAlignment(Pos.CENTER);

        VBox box = new VBox(10, titleLabel, valueLabel, buttons);
        box.setAlignment(Pos.CENTER);

        card.getChildren().add(box);

        return card;
    }

    public static HBox createAnimatedTitle(String text) {

        Label title = new Label(text);
        title.getStyleClass().add("animated-title");

        // état initial (évite flicker)
        title.setOpacity(0);
        title.setTranslateY(-8);

        // animation propre (1 seule transition = plus fluide)
        javafx.animation.FadeTransition fade =
                new javafx.animation.FadeTransition(javafx.util.Duration.millis(350), title);

        fade.setFromValue(0);
        fade.setToValue(1);

        javafx.animation.TranslateTransition slide =
                new javafx.animation.TranslateTransition(javafx.util.Duration.millis(350), title);

        slide.setFromY(-8);
        slide.setToY(0);

        // lancer ensemble
        javafx.animation.ParallelTransition anim =
                new javafx.animation.ParallelTransition(fade, slide);

        anim.setOnFinished(e -> {
            title.setOpacity(1);
            title.setTranslateY(0);
        });

        anim.play();

        HBox container = new HBox(title);

        container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // ⚡ important performance
        container.setCache(true);
        container.setCacheHint(javafx.scene.CacheHint.SPEED);

        return container;
    }
}




class TableFactory {

    // =========================================
    // CONFIG TABLE
    // =========================================
    private static final int VISIBLE_ROWS = 6;
    private static final int ROW_HEIGHT = 35;

    // =========================================
    // CREATE TABLE (PURE - FIXED WIDTH + SCROLL LOGIC)
    // =========================================
    public static <T> ScrollPane createTable(
            List<String> headers,
            List<T> data,
            Function<T, List<String>> mapper,
            Consumer<T> onRowClick
    ) {

        VBox container = new VBox();
        container.setSpacing(0);
        container.setPadding(new javafx.geometry.Insets(10));

        GridPane table = new GridPane();
        table.setHgap(1);
        table.setVgap(1);
        table.getStyleClass().add("farm-table");

        container.getChildren().add(table);

        // =========================
        // FIX: equal column sizing
        // =========================
        for (int i = 0; i < headers.size(); i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.ALWAYS);
            cc.setPercentWidth(100.0 / headers.size());
            table.getColumnConstraints().add(cc);
        }

        // =========================
        // HEADERS
        // =========================
        for (int i = 0; i < headers.size(); i++) {

            Label header = new Label(headers.get(i));
            header.getStyleClass().add("table-header");

            header.setMaxWidth(Double.MAX_VALUE);
            header.setAlignment(Pos.CENTER);

            table.add(header, i, 0);
        }

        // =========================
        // ROWS (FIXED: NO HBOX → GRID ALIGNMENT FIX)
        // =========================
        int rowIndex = 1;

        for (T item : data) {

            List<String> values = mapper.apply(item);
            T captured = item;

            for (int col = 0; col < values.size(); col++) {

                Label cell = new Label(values.get(col));
                cell.getStyleClass().add("table-cell");

                cell.setMaxWidth(Double.MAX_VALUE);
                cell.setAlignment(Pos.CENTER);

                table.add(cell, col, rowIndex);

                // CLICK ROW (simple & stable)
                cell.setOnMouseClicked(e -> {
                    if (onRowClick != null) {
                        onRowClick.accept(captured);
                    }
                });
            }

            rowIndex++;
        }

        // =========================
        // SCROLL FIX
        // =========================
        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);


        if (data.size() > VISIBLE_ROWS) {
            scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        } else {
            scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        }

        return scroll;
    }

    // =========================================
    // ADD ROW (UNCHANGED LOGIC)
    // =========================================
    public static void addRow(VBox tableContainer, List<String> rowData) {

        GridPane table = (GridPane) tableContainer.getUserData();
        if (table == null) return;

        int rowIndex = getNextRowIndex(table);

        for (int i = 0; i < rowData.size(); i++) {

            Label cell = new Label(rowData.get(i));
            cell.getStyleClass().add("table-cell");

            cell.setMaxWidth(Double.MAX_VALUE);
            cell.setAlignment(Pos.CENTER_LEFT);

            table.add(cell, i, rowIndex);
        }
    }

    // =========================================
    // ADD MULTIPLE ROWS
    // =========================================
    public static void addRows(VBox tableContainer, List<List<String>> rows) {
        for (List<String> row : rows) {
            addRow(tableContainer, row);
        }
    }

    // =========================================
    // SEARCH TABLE CARD (FIXED ALIGNMENT SAME LOGIC)
    // =========================================
    public static <T> VBox createSearchTableCard(
            List<String> headers,
            Function<String, List<T>> searchFunction,
            Function<T, List<String>> mapper,
            Consumer<T> onRowClick,
            Consumer<Consumer<List<T>>> registerRefresher
    ) {

        // =========================
        // CARD (SIZE CONTROL HERE)
        // =========================
        VBox card = new VBox();
        card.setSpacing(12);
        card.setPadding(new Insets(15));

        // 👉 🔥 CHANGE TAILLE CARD ICI
        card.setPrefHeight(300);   // <-- hauteur totale de la card
        card.setMaxHeight(Double.MAX_VALUE);

        card.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 12;
            -fx-border-radius: 12;
            -fx-border-color: #e0e0e0;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0.2, 0, 2);
            """);

        // =========================
        // TABLE CONTAINER
        // =========================
        GridPane table = new GridPane();
        table.setHgap(1);
        table.setVgap(1);
        table.getStyleClass().add("farm-table");

        // FIX COL WIDTH
        for (int i = 0; i < headers.size(); i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.ALWAYS);
            cc.setPercentWidth(100.0 / headers.size());
            table.getColumnConstraints().add(cc);
        }

        // =========================
        // HEADERS
        // =========================
        for (int i = 0; i < headers.size(); i++) {
            Label header = new Label(headers.get(i));
            header.getStyleClass().add("table-header");

            header.setMaxWidth(Double.MAX_VALUE);
            header.setAlignment(Pos.CENTER);

            table.add(header, i, 0);
        }

        // =========================
        // SCROLL (IMPORTANT PART)
        // =========================
        ScrollPane scrollPane = new ScrollPane(table);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPannable(true);

        // 👉 IMPORTANT : le scroll doit EXPANDRE dans la card
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // =========================
        // STATE
        // =========================
        final List<T>[] currentData = new List[]{new ArrayList<>()};

        // =========================
        // REFRESH
        // =========================
        Consumer<List<T>> refresh = (list) -> {

            currentData[0] = list;

            // remove old rows
            table.getChildren().removeIf(n -> {
                Integer row = GridPane.getRowIndex(n);
                return row != null && row > 0;
            });

            int rowIndex = 1;

            for (T item : list) {

                List<String> values = mapper.apply(item);
                T captured = item;

                for (int col = 0; col < values.size(); col++) {

                    Label cell = new Label(values.get(col));
                    cell.getStyleClass().add("table-cell");

                    cell.setMaxWidth(Double.MAX_VALUE);
                    cell.setAlignment(Pos.CENTER);

                    table.add(cell, col, rowIndex);

                    cell.setOnMouseClicked(e -> {
                        if (onRowClick != null) {
                            onRowClick.accept(captured);
                        }
                    });
                }

                rowIndex++;
            }
        };

        // =========================
        // SEARCH BAR
        // =========================
        TextField search = UIFactory.createSearchBar(query -> {
            refresh.accept(searchFunction.apply(query));
        });

        HBox searchBox = new HBox(search);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        // INIT
        refresh.accept(searchFunction.apply(""));

        if (registerRefresher != null) {
            registerRefresher.accept(refresh);
        }

        // =========================
        // FINAL LAYOUT
        // =========================
        card.getChildren().addAll(searchBox, scrollPane);

        return card;
    }

    // =========================================
    // UTIL
    // =========================================
    private static int getNextRowIndex(GridPane table) {
        int max = 1;

        for (Node n : table.getChildren()) {
            Integer row = GridPane.getRowIndex(n);
            if (row != null && row >= max) {
                max = row + 1;
            }
        }
        return max;
    }
}


