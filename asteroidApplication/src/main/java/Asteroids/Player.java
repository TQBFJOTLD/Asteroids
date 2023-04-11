package Asteroids;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.util.Pair;

import java.util.*;

public class Player extends SpaceObject {
    private Pane pane;
    private boolean firing;
    public long lastFiringTime = 0;
    private Map<KeyCode, Boolean> keyStateMap = new HashMap<>();
    private boolean invulnerable = false;
    private long invulnerableStartTime;
    private List<Asteroid> asteroids = new ArrayList<>();
    private AlienShip alienShip;
    private List<Bullet> alienShipBullets;
    private static final int bulletLimit = 50; // Maximum number of bullets on the screen at the same time
    private static final double playerMaxSpeed = 35; // Maximum speed for the player
    public static final long playerFiringInterval = 300; // Delay between shots in milliseconds

    public Player(double x, double y, Pane pane, Scene scene) {
        super(x, y);
        this.pane = pane;
        this.speed = 2;
        this.angle = 0;

        for (KeyCode key : Arrays.asList(KeyCode.LEFT, KeyCode.A, KeyCode.RIGHT, KeyCode.D, KeyCode.UP, KeyCode.W, KeyCode.DOWN, KeyCode.S, KeyCode.SPACE, KeyCode.H)) {
            keyStateMap.put(key, false);
        }

        // Add a listener to handle key presses
        scene.setOnKeyPressed(event -> {
//            System.out.println("Key pressed: " + event.getCode());
            switch (event.getCode()) {
                case LEFT:
                case A:
                    this.rotateLeft();
                    break;
                case RIGHT:
                case D:
                    this.rotateRight();
                    break;
                case UP:
                case W:
                    this.applyThrust();
                    break;
                case DOWN:
                case S:
                    this.removeThrust();
                    break;
                case SPACE:
                    setFiring(true);
                    break;
                case H:
                    hyperspaceJump(asteroids, alienShip, alienShipBullets);
                    break;
            }
        });

        scene.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.SPACE) {
                setFiring(false);
            }
        });

        // Add the player's shape to the pane
        shape = generateShape(1.0);
        shape.setLayoutX(x);
        shape.setLayoutY(y);
        shape.setStroke(Color.WHITE);
    }


    public Polygon generateShape(double ratio) {
        double[] points = {
                ratio * 0, ratio * -20,
                ratio * 12, ratio * 20,
                7 * ratio, 17 * ratio,
                -7 * ratio, 17 * ratio,
                -12 * ratio, 20 * ratio
        };
        return new Polygon(points);
    }

    private void rotateRight() {
        angle += 15;
        shape.setRotate(angle);
    }

    private void rotateLeft() {
        angle -= 15;
        shape.setRotate(angle);
    }

    private void applyThrust() {
        dx += speed * Math.sin(Math.toRadians(angle)) * 0.5;
        dy -= speed * Math.cos(Math.toRadians(angle)) * 0.5;

        // Limit the player's speed
        double currentSpeed = Math.sqrt(dx * dx + dy * dy);
        if (currentSpeed > playerMaxSpeed) {
            dx = (dx / currentSpeed) * playerMaxSpeed;
            dy = (dy / currentSpeed) * playerMaxSpeed;
        }
    }

    private void removeThrust() {
        dx -= speed * Math.sin(Math.toRadians(angle)) * 0.1;
        dy += speed * Math.cos(Math.toRadians(angle)) * 0.1;

        // Limit the player's speed
        double currentSpeed = Math.sqrt(dx * dx + dy * dy);
        if (currentSpeed > playerMaxSpeed) {
            dx = (dx / currentSpeed) * playerMaxSpeed;
            dy = (dy / currentSpeed) * playerMaxSpeed;
        }
    }

    public boolean isFiring() {
        return firing;
    }

    public void setFiring(boolean firing) {
        this.firing = firing;
    }

    public boolean canFire(int bulletCount) {
        return bulletCount < bulletLimit;
    }


    public boolean intersects(Asteroid asteroid) {
        return this.getShape().getBoundsInParent().intersects(asteroid.getShape().getBoundsInParent());
    }

    public void destroyed() {
        invulnerable = true;
        invulnerableStartTime = System.currentTimeMillis();
        shape.setVisible(false);
    }

    public void resetPosition() {
        x = Game.pWidth / 2;
        y = Game.pHeight / 2;
        dx = 0;
        dy = 0;
        shape.setLayoutX(x);
        shape.setLayoutY(y);
        shape.setVisible(true);
    }

    public void updateInvulnerability() {
        if (invulnerable) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - invulnerableStartTime > 3000) { // 3000 milliseconds = 3 seconds
                invulnerable = false;
                shape.setVisible(true);
            } else {
                // Flicker the player's shape
                shape.setVisible(currentTime % 200 > 100);
            }
        }
    }

    public boolean isInvulnerable() {
        return invulnerable;
    }

    private boolean safeAsteroid(double x, double y, List<Asteroid> asteroids, double safeDistance) {
        for (Asteroid asteroid : asteroids) {
            double distance = Math.sqrt(Math.pow(x - asteroid.getShape().getLayoutX(), 2) + Math.pow(y - asteroid.getShape().getLayoutY(), 2));
            if (distance <= safeDistance) {
                return false;
            }
        }
        return true;
    }

    public Pair<Double, Double> getSafeCoordinates(double safeDistance) {
        double x, y;
        do {
            x = Math.random() * Game.pWidth;
            y = Math.random() * Game.pHeight;
        } while (!isSafe(x, y, this.getShape().getLayoutX(), this.getShape().getLayoutY(), safeDistance));
        return new Pair<>(x, y);
    }

    private boolean isSafe(double x, double y, double playerX, double playerY, double safeDistance) {
        return Math.sqrt(Math.pow(x - playerX, 2) + Math.pow(y - playerY, 2)) > safeDistance;
    }

    public void hyperspaceJump(List<Asteroid> asteroids, AlienShip alienShip, List<Bullet> alienShipBullets) {
        // Generate a random position for the player
        double a, b;
        boolean safeToJump;
        do {
            a = Math.random() * Game.pWidth;
            b = Math.random() * Game.pHeight;
            safeToJump = safeAsteroid(a, b, asteroids, 200);

            if (alienShip != null) {
                double alienDistance = Math.sqrt(Math.pow(a - alienShip.getX(), 2) + Math.pow(b - alienShip.getY(), 2));
                if (alienDistance < 200) {
                    safeToJump = false;
                }
            }

            if (alienShipBullets != null) {
                for (Bullet bullet : alienShipBullets) {
                    double bulletDistance = Math.sqrt(Math.pow(a - bullet.getX(), 2) + Math.pow(b - bullet.getY(), 2));
                    if (bulletDistance < 100) {
                        safeToJump = false;
                        break;
                    }
                }
            }
        } while (!safeToJump);

        // Set the position of the player to the new location
        x = a;
        y = b;
        shape.setLayoutX(a);
        shape.setLayoutY(b);
    }

    public void setEnemies(List<Asteroid> asteroids, AlienShip alienShip, List<Bullet> alienShipBullets) {
        this.asteroids = asteroids;
        this.alienShip = alienShip;
        this.alienShipBullets = alienShipBullets;
    }

}

