package Smart_Farm;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

class FarmBarGraph {

    // =========================================================
    // MAIN GRAPH CREATION
    // =========================================================

    public static VBox createBarGraphCard(
            String title,
            List<Integer> values,
            double width,
            double height
    ) {

        VBox card = new VBox(15);

        card.setPadding(new Insets(20));
        card.setAlignment(Pos.TOP_CENTER);

        card.setPrefSize(width, height);

        card.getStyleClass().add("farm-graph-card");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("farm-graph-title");

        BarChart<String, Number> chart = createChart(width - 40, height - 80);

        updateChart(chart, values);

        card.getChildren().addAll(titleLabel, chart);

        return card;
    }

    // =========================================================
    // CHART FACTORY
    // =========================================================

    private static BarChart<String, Number> createChart(
            double width,
            double height
    ) {

        CategoryAxis xAxis = createXAxis();
        NumberAxis yAxis = createYAxis();

        BarChart<String, Number> chart =
                new BarChart<>(xAxis, yAxis);

        chart.setLegendVisible(false);

        chart.setAnimated(false);

        chart.setCategoryGap(18);
        chart.setBarGap(6);

        chart.setPrefSize(width, height);

        chart.getStyleClass().add("farm-bar-chart");

        return chart;
    }

    // =========================================================
    // AXIS
    // =========================================================

    private static CategoryAxis createXAxis() {

        CategoryAxis axis = new CategoryAxis();

        axis.setTickLabelGap(10);

        return axis;
    }

    private static NumberAxis createYAxis() {

        NumberAxis axis = new NumberAxis();

        axis.setAutoRanging(true);

        axis.setMinorTickVisible(false);

        axis.setTickLabelGap(10);

        return axis;
    }

    // =========================================================
    // UPDATE CHART
    // =========================================================

    public static void updateChart(
            BarChart<String, Number> chart,
            List<Integer> values
    ) {

        chart.getData().clear();

        XYChart.Series<String, Number> series =
                new XYChart.Series<>();

        for (int i = 0; i < values.size(); i++) {

            int value = values.get(i);

            XYChart.Data<String, Number> data =
                    new XYChart.Data<>(
                            "Z" + (i + 1),
                            value
                    );

            series.getData().add(data);
        }

        chart.getData().add(series);

        Platform.runLater(() -> {

            for (XYChart.Data<String, Number> data : series.getData()) {

                Node node = data.getNode();

                if (node != null) {

                    node.getStyleClass().add("farm-bar");

                    animateBar(node);

                    addValueLabel(data);
                }
            }
        });
    }

    // =========================================================
    // BAR ANIMATION
    // =========================================================

    private static void animateBar(Node node) {

        node.setScaleY(0);

        ScaleTransition scale =
                new ScaleTransition(Duration.millis(700), node);

        scale.setToY(1);

        scale.setInterpolator(Interpolator.EASE_OUT);

        scale.play();
    }

    // =========================================================
    // VALUE LABEL
    // =========================================================

    private static void addValueLabel(
            XYChart.Data<String, Number> data
    ) {

        Node node = data.getNode();

        if (node == null) return;

        Label valueLabel =
                new Label(String.valueOf(data.getYValue()));

        valueLabel.getStyleClass().add("farm-bar-value");

        StackPane parent = (StackPane) node;

        parent.getChildren().add(valueLabel);

        StackPane.setAlignment(
                valueLabel,
                Pos.TOP_CENTER
        );

        valueLabel.translateYProperty().bind(
                parent.heightProperty().multiply(-0.5)
        );
    }

    // =========================================================
    // LIVE AUTO REFRESH
    // =========================================================

    public static void startLiveUpdate(
            BarChart<String, Number> chart,
            Supplier<List<Integer>> dataSupplier
    ) {

        Timeline timeline = new Timeline(

                new KeyFrame(
                        Duration.seconds(10),
                        e -> {

                            List<Integer> newValues =
                                    dataSupplier.get();

                            updateChart(chart, newValues);
                        }
                )
        );

        timeline.setCycleCount(Animation.INDEFINITE);

        timeline.play();
    }

    // =========================================================
    // TEST DATA
    // =========================================================

    public static List<Integer> generateRandomFarmData() {

        Random random = new Random();

        List<Integer> values = new ArrayList<>();

        for (int i = 0; i < 7; i++) {

            values.add(
                    random.nextInt(90) + 10
            );
        }

        return values;
    }

}