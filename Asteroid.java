package Asteroids;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import java.util.Random;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Asteroid {
    private double x, y, dx, dy, size;
    private Polygon shape;

    public Asteroid(double x, double y, double size) {
        this.x = x;
        this.y = y;
        this.size = size;

        Random rand = new Random();
        // Set the velocity based on the size
        if (size == 80) {           // Large Asteroids
            dx = rand.nextDouble() * 1 + 1;
            dy = rand.nextDouble() * 1 + 1;
        }
        else if (size == 40) {      // Medium Asteroids
            dx = rand.nextDouble() * 2 + 2;
            dy = rand.nextDouble() * 2 + 2;
        }
        else {                      // Small Asteroids
            dx = rand.nextDouble() * 3 + 3;
            dy = rand.nextDouble() * 3 + 3;
        }

        generateShape();

        // Set the initial layout of the shape
        shape.setLayoutX(x);
        shape.setLayoutY(y);
    }

    private void generateShape() {
        shape = new Polygon();
        Random rand = new Random();
        int numPoints = rand.nextInt(7) + 5; // random number of points between 5 and 12
        for (int i = 0; i < numPoints; i++) {
            double angle = 2 * Math.PI / numPoints * i;
            double radius = size * (rand.nextDouble() * 0.5 + 0.5); // random radius between 0.5 and 1.0 times size
            double x = radius * Math.cos(angle);
            double y = radius * Math.sin(angle);
            shape.getPoints().addAll(x, y);
        }
        shape.setFill(Color.WHITE);
        shape.setStroke(Color.GRAY);
        shape.setStrokeWidth(2);
    }

    public void update() {
        // Move the asteroid
        x += dx;
        y += dy;

        // Check if the asteroid has flown outside the game pane
        double paneWidth = shape.getScene().getWidth();
        double paneHeight = shape.getScene().getHeight();
        if (x < -size) {
            x = paneWidth + size;
        } else if (x > paneWidth + size) {
            x = -size;
        }
        if (y < -size) {
            y = paneHeight + size;
        } else if (y > paneHeight + size) {
            y = -size;
        }

        // Update the position of the asteroid's shape
        shape.setLayoutX(x);
        shape.setLayoutY(y);
    }


    public void addToPane(Pane pane) {
        pane.getChildren().add(shape);
    }

    public void removeFromPane(Pane pane) {
        pane.getChildren().remove(shape);
    }

    public Polygon getShape() {
        return shape;
    }

    public double getSize() {
        return size;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setDx(double dx) {
        this.dx = dx;
    }

    public void setDy(double dy) {
        this.dy = dy;
    }

    public List<Asteroid> destroyed(double size, Pane pane) {
        List<Asteroid> newAsteroids = new ArrayList<>();
        if (size == 80) {
            // Create two medium-sized asteroids
            for (int i = 0; i < 2; i++) {
                Asteroid asteroid = new Asteroid(this.x, this.y, 40);
                asteroid.addToPane(pane);
                newAsteroids.add(asteroid);
            }
        } else if (size == 40) {
            // Create two small-sized asteroids
            for (int i = 0; i < 2; i++) {
                Asteroid asteroid = new Asteroid(this.x, this.y, 20);
                asteroid.addToPane(pane);
                newAsteroids.add(asteroid);
            }
        }
        return newAsteroids;
    }


}
