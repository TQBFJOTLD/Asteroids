package Asteroids;

import javafx.scene.layout.Pane;
import javafx.scene.shape.Shape;

public class SpaceObject {
    protected double x, y, dx, dy, size = 20, ratio, angle = 0, speed;
    protected Shape shape;

    public SpaceObject(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void addToPane(Pane pane) {
        pane.getChildren().add(shape);
    }

    public void removeFromPane(Pane pane) {
        pane.getChildren().remove(shape);
    }

    public Shape getShape() {
        return shape;
    }

    public double getSize() {
        return size;
    }
    public void setSize(double size)  {
        this.size = size;
    }
    public double getRatio() {
        return ratio;
    }
    public void setRatio(double ratio)  {
        this.ratio = ratio;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getDx() {
        return dx;
    }

    public double getDy() {
        return dy;
    }

    public void setDx(double dx) {
        this.dx = dx;
    }

    public void setDy(double dy) {
        this.dy = dy;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }
    public void rotateAngle() {
        shape.setRotate(angle);
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void update() {
        // Move the Object
        x += dx;
        y += dy;

        // Check if the object has flown outside the game pane
        if (x < -size * ratio) {
            x = Game.PANE_WIDTH + (-size * ratio);
        } else if (x > Game.PANE_WIDTH - (-size * ratio)) {
            x = -size * ratio;
        }
        if (y < -size * ratio) {
            y = Game.PANE_HEIGHT + (-size * ratio);
        } else if (y > Game.PANE_HEIGHT - (-size * ratio)) {
            y = -size * ratio;
        }

        // Update the position of the object
        shape.setLayoutX(x);
        shape.setLayoutY(y);
    }


    public Bullet fire(Pane pane) {
        angle = this.getAngle();
        Bullet bullet = new Bullet(x, y, 10, angle);
        bullet.addToPane(pane);
        bullet.update();
        return bullet;
    }
}
