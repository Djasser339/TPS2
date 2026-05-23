package Smart_Farm;


import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.*;

public class Pages {

    public static ScrollPane zonePage() {

       return PageZone.zonePage();
    }

    public static ScrollPane culturePage() {

       return PageCulture.culturePage();
    }

    public static ScrollPane animalPage() {

        return PageAnimaux.animauxPage();

    }

    public static StackPane capteurPage() {

        StackPane pane = new StackPane();

        Label label = new Label("PAGE CAPTEURS");

        pane.getChildren().add(label);
        pane.setAlignment(Pos.CENTER);

        return pane;
    }

    public static StackPane alertPage() {

        StackPane pane = new StackPane();

        Label label = new Label("PAGE ALERTES");

        pane.getChildren().add(label);
        pane.setAlignment(Pos.CENTER);

        return pane;
    }
}
