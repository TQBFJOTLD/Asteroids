package Asteroids;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

public class AlienShip extends SpaceObject {
    public AlienShip(double x, double y) {
        super(x, y);
        generateShape();

        // Set the initial layout of the shape
        shape.setLayoutX(x);
        shape.setLayoutY(y);
    }

    public void generateShape() {
        Path path = new Path();
        path.getElements().addAll(
                new MoveTo(30, 10),
                new LineTo(40, 10),
                new LineTo(45, 20),
                new LineTo(65, 30),
                new LineTo(45, 40),
                new LineTo(25, 40),
                new LineTo(5, 30),
                new LineTo(25, 20),
                new LineTo(30, 10),
                new MoveTo(45, 20),
                new LineTo(25, 20),
                new MoveTo(65, 30),
                new LineTo(5, 30)
        );
        path.setFill(Color.BLACK);
        path.setStroke(Color.RED);
        path.setStrokeWidth(1);
        shape = path;
    }

    public Bullet fire(Pane pane, Player player) {
        double deltaX = player.getX() - this.getX();
        double deltaY = player.getY() - this.getY();
        double angle = Math.toDegrees(Math.atan2(deltaY, deltaX));

        Bullet bullet = new Bullet(x, y, 2, angle);
        bullet.setFill(Color.RED);
        bullet.addToPane(pane);
        bullet.setDx(bullet.getSpeed() * Math.cos(Math.toRadians(angle)));
        bullet.setDy(bullet.getSpeed() * Math.sin(Math.toRadians(angle)));
        return bullet;
    }
}




