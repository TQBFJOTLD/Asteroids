package Asteroids;

import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.animation.AnimationTimer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;


public class Player {
    private double x, y, dx=0, dy=0, angle, radius, acceleration, speed, bulletSpeed;
    private Pane pane;
    private Polygon shape;
    private boolean accelerating;
    private int screenWidth, screenHeight;

    private boolean firing;

    private boolean invulnerable = false;
    private long invulnerableStartTime;


    public Player(double x, double y, double screenWidth, double screenHeight, Pane pane, Scene scene) {
        this.x = x;
        this.y = y;
        this.screenWidth = (int) screenWidth;
        this.screenHeight = (int) screenHeight;
        this.pane = pane;
        this.speed = 5;
        this.angle = 0;
//        this.acceleration = 0.2;
        this.radius = 20;
        this.bulletSpeed = 10;


        // Add a listener to handle key presses
        scene.setOnKeyPressed(event -> {
            System.out.println("Key pressed: " + event.getCode());
            switch (event.getCode()) {
                case LEFT:
                    this.rotateLeft();
                    break;
                case RIGHT:
                    this.rotateRight();
                    break;
                case UP:
                    dx += speed * Math.sin(Math.toRadians(angle));
                    dy -= speed * Math.cos(Math.toRadians(angle));
                    break;
                case DOWN:
                    dx = 0;
                    dy = 0;
                    break;
                case SPACE:
                    setFiring(true);
                    break;
            }
        });

        scene.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.SPACE) {
                setFiring(false);
            }
        });

        // Add the player's shape to the pane
        shape = new Polygon(0, -20, 10, 20, -10, 20);
        shape.setLayoutX(x);
        shape.setLayoutY(y);
        shape.setFill(Color.DARKGRAY);
        shape.setStroke(Color.WHITE);
    }

    public void rotateRight() {
        angle += 15;
        shape.setRotate(angle);
    }

    public void rotateLeft() {
        angle -= 15;
        shape.setRotate(angle);
    }

    public void update() {
        System.out.println("Updating player position");
        x += dx;
        y += dy;
        // Wrap around the screen
        if (x < -radius) {
            x = screenWidth + radius;
        } else if (x > screenWidth + radius) {
            x = -radius;
        }
        if (y < -radius) {
            y = screenHeight + radius;
        } else if (y > screenHeight + radius) {
            y = -radius;
        }
        shape.setLayoutX(x);
        shape.setLayoutY(y);
    }

    public Bullet fire(Pane pane) {
        double tipX = x - 20 * Math.sin(Math.toRadians(angle));
        double tipY = y + 20 * Math.cos(Math.toRadians(angle));
        Bullet bullet = new Bullet(tipX, tipY, bulletSpeed, angle);
        bullet.addToPane(pane);
        bullet.update();
        return bullet;
    }


    public boolean isFiring() {
        return firing;
    }

    public void setFiring(boolean firing) {
        this.firing = firing;
    }


    public Polygon getShape() {
        return shape;
    }

    public void addToPane(Pane pane) {
        pane.getChildren().add(shape);
    }

    public void setDx(double dx) {
        this.dx = dx;
    }

    public void setDy(double dy) {
        this.dy = dy;
    }
    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public double getDx() {
        return dx;
    }

    public double getDy() {
        return dy;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
        shape.setRotate(angle);
    }

    public void hyperspace() {
        x = Math.random() * screenWidth;
        y = Math.random() * screenHeight;
        shape.setLayoutX(x);
        shape.setLayoutY(y);
    }

    public boolean intersects(Asteroid asteroid) {
        return this.getShape().getBoundsInParent().intersects(asteroid.getShape().getBoundsInParent());
    }

    public void destroyed(Pane pane) {
        pane.getChildren().remove(shape);
    }

    public void resetPosition() {
        x = screenWidth / 2;
        y = screenHeight / 2;
        invulnerable = true;
        invulnerableStartTime = System.currentTimeMillis();
        shape.setLayoutX(x);
        shape.setLayoutY(y);
        pane.getChildren().add(shape);
    }

    public void updateInvulnerability() {
        if (invulnerable) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - invulnerableStartTime > 3000) { // 3000 milliseconds = 3 seconds
                invulnerable = false;
            } else {
                // Flicker the player's shape
                shape.setVisible(currentTime % 200 > 100);
            }
        }
    }

    public boolean isInvulnerable() {
        return invulnerable;
    }


}

