module com.example.test {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;


    opens Asteroids to javafx.fxml;
    exports Asteroids;
}