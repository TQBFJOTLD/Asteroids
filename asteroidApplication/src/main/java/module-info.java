module com.example.test {
    requires javafx.controls;
    requires javafx.fxml;


    opens Asteroids to javafx.fxml;
    exports Asteroids;
}