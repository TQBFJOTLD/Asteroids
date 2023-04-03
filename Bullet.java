package Asteroids;

import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Bullet {
    private Circle bullet;
    private double speed=10;
    private double x, dx;
    private double y, dy;
    private double angle;

    public Bullet(double x, double y, double speed, double angle) {
        bullet = new Circle(x, y, 2, Color.BLACK);
        this.speed = speed;
        this.angle = angle;
    }

    public void addToPane(Pane pane) {
        pane.getChildren().add(bullet);
    }

    public void update() {
        dx = speed * Math.sin(Math.toRadians(angle));
        dy = -speed * Math.cos(Math.toRadians(angle));
        x += dx;
        y += dy;
        bullet.setLayoutX(x);
        bullet.setLayoutY(y);
    }

    public boolean intersects(Asteroid asteroid) {
        return this.getShape().getBoundsInParent().intersects(asteroid.getShape().getBoundsInParent());
    }


    public void removeFromPane(Pane pane) {
        pane.getChildren().remove(bullet);
    }

    public double getX() {
        return bullet.getCenterX();
    }

    public double getY() {
        return bullet.getCenterY();
    }

    public double getRadius() {
        return bullet.getRadius();
    }

    public Circle getShape() {
        return bullet;
    }

}
