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
                badge.setText(count > 99 ? "99+" : String.valueOf(count));
                badge.setVisible(count > 0);
            });
        });

        // Bell popup
        Popup popup = new Popup();
        popup.setAutoHide(true);

        // Track when the popup was last hidden so we can distinguish
        // "autoHide triggered by clicking the bell button" from a real outside click.
        final long[] lastHideMs = {0};
        popup.setOnHidden(e -> lastHideMs[0] = System.currentTimeMillis());

        bellBtn.setOnMouseClicked(e -> {
            // If the popup was just auto-hidden because this very click landed on the
            // bell button (autoHide fires before the button handler), don't reopen it.
            if (System.currentTimeMillis() - lastHideMs[0] < 200) return;

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

        VBox box = new VBox(0);
        box.setMinWidth(360);
        box.setMaxWidth(360);
        box.getStyleClass().add("bell-popup");

        // ── Header ──────────────────────────────────────────────
        List<Alerte> alertes = AlerteState.getAlertesActives();
        alertes.sort((a1, a2) -> a2.getDateCreation().compareTo(a1.getDateCreation()));

        int total = alertes.size();
        String countText = total == 0 ? "Aucune alerte active"
                : total == 1 ? "1 alerte active"
                : (total > 99 ? "99+" : total) + " alertes actives";

        HBox header = new HBox(8);
        header.setPadding(new Insets(14, 16, 12, 16));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #f8fdf8; -fx-background-radius: 14 14 0 0;");

        Label titleLbl = new Label("🔔 Alertes");
        titleLbl.getStyleClass().add("bell-popup-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label countLbl = new Label(countText);
        countLbl.setStyle(
                "-fx-font-size: 11px; -fx-font-weight: bold;"
                + "-fx-text-fill: white;"
                + "-fx-background-color: " + (total == 0 ? "#9e9e9e" : "#c62828") + ";"
                + "-fx-background-radius: 10;"
                + "-fx-padding: 2 8;"
        );

        header.getChildren().addAll(titleLbl, spacer, countLbl);
        box.getChildren().add(header);

        // ── Divider ─────────────────────────────────────────────
        javafx.scene.control.Separator sep = new javafx.scene.control.Separator();
        sep.setPadding(new Insets(0));
        box.getChildren().add(sep);

        if (alertes.isEmpty()) {
            HBox noneRow = new HBox();
            noneRow.setPadding(new Insets(16));
            noneRow.setAlignment(Pos.CENTER);
            Label none = new Label("✅ Aucune alerte active en ce moment");
            none.setStyle("-fx-text-fill: #4caf50; -fx-font-size: 13px;");
            noneRow.getChildren().add(none);
            box.getChildren().add(noneRow);
            return box;
        }

        // ── Alert rows ──────────────────────────────────────────
        VBox listBox = new VBox(6);
        listBox.setPadding(new Insets(10, 12, 10, 12));

        int shown = Math.min(alertes.size(), 8);
        for (int i = 0; i < shown; i++) {

            Alerte a = alertes.get(i);

            GestionnaireCapteursAlertes g = GestionnaireCapteursAlertes.getInstance();
            Capteur c = g.getCapteurById(a.getReleve().getIdCapteur());
            String zone    = (c != null) ? c.getZoneId()  : "?";
            String typeStr = (c != null) ? c.getTypeNom() : "?";

            String icon, color, bg, borderColor;
            switch (a.getNiveau()) {
                case critique      -> { icon = "🔴"; color = "#c62828"; bg = "#fff5f5"; borderColor = "#ffcdd2"; }
                case avertissement -> { icon = "🟡"; color = "#e65100"; bg = "#fffbf0"; borderColor = "#ffe0b2"; }
                default            -> { icon = "🟢"; color = "#2e7d32"; bg = "#f5fbf5"; borderColor = "#c8e6c9"; }
            }

            HBox row = new HBox(10);
            row.setPadding(new Insets(8, 12, 8, 12));
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle(
                    "-fx-background-color: " + bg + ";"
                    + "-fx-background-radius: 10;"
                    + "-fx-border-color: " + borderColor + ";"
                    + "-fx-border-radius: 10;"
                    + "-fx-border-width: 1;"
            );

            Label iconLbl = new Label(icon);
            iconLbl.setStyle("-fx-font-size: 15px;");

            VBox info = new VBox(2);
            HBox.setHgrow(info, Priority.ALWAYS);

            // Niveau + zone + valeur
            Label topLine = new Label(
                    a.getNiveau().name().toUpperCase()
                    + "  ·  Zone: " + zone
                    + "  ·  " + typeStr
                    + "  →  " + a.getReleve().getValeurAsString()
            );
            topLine.setStyle(
                    "-fx-font-weight: bold; -fx-font-size: 11.5px;"
                    + "-fx-text-fill: " + color + ";"
                    + "-fx-text-overrun: clip;"
            );
            topLine.setWrapText(false);

            // Date + capteur ID
            Label timeLine = new Label(
                    a.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm"))
                    + "   [" + a.getReleve().getIdCapteur() + "]"
            );
            timeLine.setStyle("-fx-font-size: 10.5px; -fx-text-fill: #888;");

            info.getChildren().addAll(topLine, timeLine);
            row.getChildren().addAll(iconLbl, info);
            listBox.getChildren().add(row);
        }

        box.getChildren().add(listBox);

        // ── Footer overflow ──────────────────────────────────────
        if (alertes.size() > 8) {
            javafx.scene.control.Separator sep2 = new javafx.scene.control.Separator();
            HBox footer = new HBox();
            footer.setPadding(new Insets(8, 16, 12, 16));
            footer.setAlignment(Pos.CENTER);
            int remaining = alertes.size() - 8;
            Label more = new Label("+ " + remaining + " autre" + (remaining > 1 ? "s" : "") + " alerte" + (remaining > 1 ? "s" : "") + " — voir la page Alertes");
            more.setStyle("-fx-text-fill: #757575; -fx-font-size: 11px; -fx-font-style: italic;");
            footer.getChildren().add(more);
            box.getChildren().addAll(sep2, footer);
        }

        return box;
    }

    public static void main(String[] args) {
        launch();
    }
}
