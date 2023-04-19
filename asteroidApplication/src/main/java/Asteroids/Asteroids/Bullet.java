package Asteroids;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;

public class Bullet extends Asteroids.SpaceObject {
    private long creationTime;
    public Bullet(double x, double y, double speed, double angle) {
        super(x, y);
        this.angle = angle;
        this.speed = speed;
        this.dx = speed * Math.sin(Math.toRadians(angle));
        this.dy = -speed * Math.cos(Math.toRadians(angle));
        this.creationTime = System.currentTimeMillis();

        shape = new Circle(2, Color.WHITE);
        shape.setLayoutX(x);
        shape.setLayoutY(y);
    }

    public boolean intersects(Shape s) {
        return this.getShape().getBoundsInParent().intersects(s.getBoundsInParent());
    }

    public double getRadius() {
        return ((Circle) shape).getRadius();
    }
    public long getCreationTime() {
        return creationTime;
    }

    public void setFill(Color color) {
        ((Circle) shape).setFill(color);
    }

}
