package Smart_Farm;
// ===============================
// Main.java
// ===============================

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class SmartFarmApp extends Application {

    @Override
    public void start(Stage stage) {

        BorderPane root = new BorderPane();

        // =========================
        // BELL BUTTON (in top bar)
        // =========================
        Label bellBtn = new Label("🔔");
        bellBtn.getStyleClass().add("bell-button");
        bellBtn.setAlignment(Pos.CENTER);

        Label badge = new Label("0");
        badge.getStyleClass().add("bell-badge");
        badge.setVisible(false);

        StackPane bellContainer = new StackPane(bellBtn, badge);
        StackPane.setAlignment(badge, Pos.TOP_RIGHT);
        bellContainer.setPickOnBounds(false);

        // Live badge update
        AlerteState.nbrAlertesProperty().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> {
                int count = newVal.intValue();
                badge.setText(String.valueOf(count));
                badge.setVisible(count > 0);
            });
        });

        // Bell popup
        Popup popup = new Popup();
        popup.setAutoHide(true);

        bellBtn.setOnMouseClicked(e -> {
            if (popup.isShowing()) {
                popup.hide();
                return;
            }

            VBox popupBox = buildBellPopup();
            popup.getContent().setAll(popupBox);

            double screenX = e.getScreenX() - 280;
            double screenY = e.getScreenY() + 10;
            popup.show(stage, screenX, screenY);
        });

        // =========================
        // TOP BAR
        // =========================
        root.setTop(UIFactory.createTopBar(
                "Tableau de Bord",
                "Smart Farm",
                bellContainer
        ));

        // =========================
        // PAGE PAR DÉFAUT
        // =========================
        root.setCenter(PageDashboard.dashboardPage());

        // =========================
        // NAV BAR
        // =========================
        root.setLeft(UIFactory.createNavBar(root, bellContainer));

        // =========================
        // SAMPLE DATA (demo)
        // set SampleData.ENABLED = false to use only real data
        // =========================
        SampleData.load();

        // =========================
        // SCENE
        // =========================
        Scene scene = new Scene(root);

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
        scene.setOnMousePressed(ev -> root.requestFocus());

        stage.setTitle("Smart Farm Management");
        stage.setScene(scene);
        stage.show();
    }

    // =========================================
    // BELL POPUP CONTENT
    // =========================================
    private VBox buildBellPopup() {

        VBox box = new VBox(8);
        box.setPadding(new Insets(15));
        box.setMinWidth(340);
        box.setMaxWidth(340);
        box.getStyleClass().add("bell-popup");

        Label title = new Label("🔔 Dernières Alertes");
        title.getStyleClass().add("bell-popup-title");
        box.getChildren().add(title);

        List<Alerte> alertes = AlerteState.getAlertesActives();

        if (alertes.isEmpty()) {
            Label none = new Label("✅ Aucune alerte active");
            none.setStyle("-fx-text-fill: #4caf50; -fx-font-size: 13px;");
            box.getChildren().add(none);
            return box;
        }

        // Show up to 8 most recent active alerts (already sorted critical first)
        int shown = Math.min(alertes.size(), 8);
        for (int i = 0; i < shown; i++) {

            Alerte a = alertes.get(i);

            GestionnaireCapteursAlertes g = GestionnaireCapteursAlertes.getInstance();
            Capteur c = g.getCapteurById(a.getReleve().getIdCapteur());
            String zone = (c != null) ? c.getZoneId() : "?";

            String icon, color, bg;
            switch (a.getNiveau()) {
                case critique      -> { icon = "🔴"; color = "#c62828"; bg = "#ffebee"; }
                case avertissement -> { icon = "🟡"; color = "#e65100"; bg = "#fff3e0"; }
                default            -> { icon = "🟢"; color = "#2e7d32"; bg = "#e8f5e9"; }
            }

            HBox row = new HBox(8);
            row.setPadding(new Insets(6, 10, 6, 10));
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle(
                    "-fx-background-color: " + bg + ";"
                    + "-fx-background-radius: 8;"
            );

            Label iconLbl = new Label(icon);
            iconLbl.setStyle("-fx-font-size: 14px;");

            VBox info = new VBox(1);
            Label topLine = new Label(a.getNiveau().name().toUpperCase() + "  —  Zone: " + zone
                    + "  |  " + a.getReleve().getValeurAsString());
            topLine.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: " + color + ";");

            Label timeLine = new Label(
                    a.getDateCreation().format(DateTimeFormatter.ofPattern("yyyy-MM-dd  HH:mm"))
                    + "   [" + a.getReleve().getIdCapteur() + "]"
            );
            timeLine.setStyle("-fx-font-size: 11px; -fx-text-fill: #757575;");

            info.getChildren().addAll(topLine, timeLine);
            row.getChildren().addAll(iconLbl, info);
            box.getChildren().add(row);
        }

        if (alertes.size() > 8) {
            Label more = new Label("... et " + (alertes.size() - 8) + " autres alertes");
            more.setStyle("-fx-text-fill: #9e9e9e; -fx-font-size: 11px;");
            box.getChildren().add(more);
        }

        return box;
    }

    public static void main(String[] args) {
        launch();
    }
}
