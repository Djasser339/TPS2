package Smart_Farm;
// ===============================
// Main.java
// ===============================

import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class SmartFarmApp extends Application {

    @Override
    public void start(Stage stage) {

        BorderPane root = new BorderPane();

        // =========================
        // TOP BAR
        // =========================
        root.setTop(UIFactory.createTopBar(
                "Gestion des Zones",
                "Smart Farm"
        ));

        // =========================
        // PAGE PAR DÉFAUT
        // =========================
        root.setCenter(PageZone.zonePage());

        // =========================
        // NAV BAR
        // =========================
        root.setLeft(UIFactory.createNavBar(root));

        // =========================
        // SCENE
        // =========================
        Scene scene = new Scene(root);

        // CSS
        scene.getStylesheets().add(
                getClass().getResource("style.css").toExternalForm()
        );

        // =========================
        // ADAPTATION ÉCRAN
        // =========================
        Rectangle2D screen = Screen.getPrimary().getVisualBounds();

        stage.setX(screen.getMinX());
        stage.setY(screen.getMinY());

        stage.setWidth(screen.getWidth() * 0.9);
        stage.setHeight(screen.getHeight() * 0.9);

        stage.setMinWidth(900);
        stage.setMinHeight(600);

        // =========================
        // FINAL
        // =========================
        scene.setOnMousePressed(e -> {
            root.requestFocus(); // enlève le focus du TextField
        });

        stage.setTitle("Smart Farm Management");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}