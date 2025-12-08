module com.example.miniproyecto4 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;


    opens com.example.miniproyecto4 to javafx.fxml;
    exports com.example.miniproyecto4;
}