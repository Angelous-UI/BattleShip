module com.example.battleship {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    opens com.example.battleship to javafx.fxml;
    opens com.example.battleship.Controllers to javafx.fxml;

    exports com.example.battleship;
    exports com.example.battleship.Controllers;
}