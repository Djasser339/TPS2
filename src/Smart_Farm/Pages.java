package Smart_Farm;


import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.*;

public class Pages {

    public static ScrollPane dashboardPage() {
        return PageDashboard.dashboardPage();
    }

    public static ScrollPane zonePage() {

       return PageZone.zonePage();
    }

    public static ScrollPane culturePage() {

       return PageCulture.culturePage();
    }

    public static ScrollPane animalPage() {

        return PageAnimaux.animauxPage();

    }

    public static ScrollPane capteurPage() {

        return PageCapteurs.capteurPage();
    }

    public static ScrollPane alertPage() {

        return PageAlertes.alertePage();
    }

    public static ScrollPane commercialPage() {
        return PageCommercial.commercialPage();
    }
}
