package Asteroids;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.scene.text.*;
import javafx.scene.shape.Polygon;
import javafx.scene.paint.Color;
import java.util.Comparator;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;
import javafx.animation.PauseTransition;


public class Game extends Application {
    private Scene mainScene;
    private Scene gameScene;
    private Scene hallOfFameScene;
    private Scene helpScene;

    private int lives = 3;
    private int level = 1;
    private int score = 0;
    private Text scoreText = new Text();
    private List<Polygon> lifeIndicators = new ArrayList<>();
    private List<Integer> highScores = new ArrayList<>();
    Button playButton = new Button("Play");
    Button hallButton = new Button("Hall of Fame");
    Button helpButton = new Button("Instruction");

    private void createLifeIndicators(Pane gamePane) {
        for (int i = 0; i < lives; i++) {
            Polygon lifeIndicator = new Polygon(0, -10, 5, 10, -5, 10);
            lifeIndicator.setFill(Color.GREEN);
            lifeIndicator.setLayoutX(20 + i * 20);
            lifeIndicator.setLayoutY(20);
            lifeIndicators.add(lifeIndicator);
            gamePane.getChildren().add(lifeIndicator);
        }
    }

    private void updateLives(int lives) {
        this.lives = lives;
        for (int i = 0; i < lifeIndicators.size(); i++) {
            Polygon lifeIndicator = lifeIndicators.get(i);
            if (i < lives) {
                lifeIndicator.setVisible(true);
            } else {
                lifeIndicator.setVisible(false);
            }
        }
    }

    private void removeLife() {
        if (lives > 0) {
            lives--;
            updateLives(lives);
        }
    }

    private void updateScore(int amount) {
        score += amount;
        scoreText.setText("Score: " + score);
    }

    private void initGame(Stage stage, int level) {
        stage.setTitle("Level " + level);

        // Create the game scene with random asteroids
        Pane gamePane = new Pane();
        gamePane.setPrefSize(1000, 800);
        gameScene = new Scene(gamePane, 1000, 800);
        stage.setScene(gameScene);

        createLifeIndicators(gamePane);

        List<Asteroid> asteroids = new ArrayList<>();
        for (int i = 0; i < level; i++) {
            double x = Math.random() * gamePane.getPrefWidth();
            double y = Math.random() * gamePane.getPrefHeight();

            // Create 3 types of asteroids, 20 for small, 40 for medium and 80 for large.
            double[] sizes = {20, 40, 80};
            double size = sizes[(int) (Math.random() * sizes.length)];
            if (level == 1) size = 80;
            Asteroid asteroid = new Asteroid(x, y, size);
            asteroid.addToPane(gamePane);
            asteroids.add(asteroid);
        }

        List<Bullet> bullets = new ArrayList<>();

        // Create the player and add it to the game pane
        Player player = new Player(500.0, 500.0, 1000, 800, gamePane, gameScene);
        player.addToPane(gamePane);
        player.getShape().requestFocus();


        Text scoreText = new Text("Score: " + score);
        scoreText.setFont(new Font("Arial", 20));
        scoreText.setFill(Color.BLACK);
        scoreText.setLayoutX(850);
        scoreText.setLayoutY(30);
        gamePane.getChildren().add(scoreText);

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long l) {
                player.update();
                player.updateInvulnerability();

                // Call the fire method and add the returned bullet to the bullets list
                if (player.isFiring()) {
                    bullets.add(player.fire(gamePane));
                }

                for (Asteroid asteroid : asteroids) {
                    asteroid.update();
                    // check for collisions with player ship and bullets here
                    if (!player.isInvulnerable() && player.intersects(asteroid)) {
                        System.out.println("The lives is: " + lives);
                        player.destroyed(gamePane);
                        removeLife();
                        player.resetPosition();
                        if (lives > 0) {
                            player.getShape().setVisible(true);
                        }

                        if (lives == 0) {
                            // Add the score to the high scores list
                            highScores.add(score);

                            // Sort the high scores in descending order
                            highScores.sort(Comparator.reverseOrder());

                            // Reset the score to 0
                            score = 0;
                            Text gameOverText = new Text("Game Over");
                            gameOverText.setFont(new Font("Arial", 48));
                            gameOverText.setFill(Color.RED);
                            gameOverText.setLayoutX(350);
                            gameOverText.setLayoutY(400);
                            gamePane.getChildren().add(gameOverText);


                            // Add Restart button
                            Button restartButton = new Button("Restart");
                            restartButton.setLayoutX(420);
                            restartButton.setLayoutY(480);
                            restartButton.setOnAction(event -> restartGame(stage));
                            gamePane.getChildren().add(restartButton);

                            // Add Back to Menu button
                            Button backButton = new Button("Back to Menu");
                            backButton.setLayoutX(395);
                            backButton.setLayoutY(520);
                            backButton.setOnAction(event -> backToMenu(stage));
                            gamePane.getChildren().add(backButton);

                            lives=3;
                            this.stop();
                        }
                    }
                }

                for (Iterator<Bullet> bulletIterator = bullets.iterator(); bulletIterator.hasNext();) {
                    Bullet bullet = bulletIterator.next();
                    bullet.update();
                    // Check if the bullet is off-screen and remove it if necessary
                    if (bullet.getX() < 0 || bullet.getX() > gamePane.getPrefWidth() || bullet.getY() < 0 || bullet.getY() > gamePane.getPrefHeight()) {
                        bullet.removeFromPane(gamePane);
                        bulletIterator.remove();
                    }


                    // Check for collisions between bullets and asteroids
                    for (Iterator<Asteroid> asteroidIterator = asteroids.iterator(); asteroidIterator.hasNext();) {
                        Asteroid asteroid = asteroidIterator.next();
                        if (bullet.intersects(asteroid)) {
                            // Remove the bullet and the asteroid from their respective lists and the game pane
                            bullet.removeFromPane(gamePane);
                            bulletIterator.remove();

                            asteroid.removeFromPane(gamePane);
                            asteroidIterator.remove();

                            // Calculate and update the score
                            int asteroidScore = 0;
                            if (asteroid.getSize() == 80) {
                                asteroidScore = 900;
                            } else if (asteroid.getSize() == 40) {
                                asteroidScore = 600;
                            } else if (asteroid.getSize() == 20) {
                                asteroidScore = 300;
                            }
                            score += asteroidScore;
                            scoreText.setText("Score: " + score);

                            // Create new asteroids
                            List<Asteroid> newAsteroids = asteroid.destroyed(asteroid.getSize(), gamePane);
                            asteroids.addAll(newAsteroids);

                            // Create new asteroids if there are none left
                            if (asteroids.isEmpty()) {
                                Text successText = new Text("Success!");
                                successText.setFont(new Font("Arial", 48));
                                successText.setFill(Color.GREEN);
                                successText.setLayoutX(350);
                                successText.setLayoutY(400);
                                gamePane.getChildren().add(successText);

                                // Pause for 2 seconds before moving on to the next level
                                PauseTransition pause = new PauseTransition(Duration.seconds(2));
                                pause.setOnFinished(event -> initGame(stage, level + 1));
                                pause.play();
                                this.stop();
                            }
                            break;
                        }
                    }
                }
            }
        };
        timer.start();



        // Hide the buttons
        playButton.setVisible(false);
        hallButton.setVisible(false);
        helpButton.setVisible(false);

        // Request focus on the game pane to receive key inputs
        gamePane.requestFocus();

    }
    private void restartGame(Stage stage) {
        lives = 3;
        lifeIndicators.clear();
        initGame(stage, level);
    }


    private void backToMenu(Stage stage) {
        stage.setScene(mainScene);
        playButton.setVisible(true);
        hallButton.setVisible(true);
        helpButton.setVisible(true);
    }


    @Override
    public void start(Stage stage) throws IOException {
        Pane pane = new Pane();
        playButton.setLayoutX(400);
        playButton.setLayoutY(400);
        playButton.setFont(new Font("Arial", 20));
        pane.getChildren().add(playButton);

        hallButton.setLayoutX(400);
        hallButton.setLayoutY(450);
        hallButton.setFont(new Font("Arial", 20));
        pane.getChildren().add(hallButton);

        helpButton.setLayoutX(400);
        helpButton.setLayoutY(500);
        helpButton.setFont(new Font("Arial", 20));
        pane.getChildren().add(helpButton);

        hallButton.setOnAction(event -> {
            // Sort the high scores in descending order
            highScores.sort(Comparator.reverseOrder());

            // Create a VBox to hold the high scores
            VBox scoresBox = new VBox();
            scoresBox.setAlignment(Pos.CENTER);

            // Add each high score to the VBox
            for (int i = 0; i < highScores.size(); i++) {
                Text scoreText = new Text((i + 1) + ". " + highScores.get(i));
                scoreText.setFont(new Font("Arial", 20));
                scoreText.setFill(Color.BLACK);
                scoresBox.getChildren().add(scoreText);
            }

            // Create a back button to return to the main menu
            Button backButton = new Button("Back");
            backButton.setOnAction(event2 -> stage.setScene(mainScene));

            // Create a VBox to hold the high scores and the back button
            VBox hallBox = new VBox();
            hallBox.setAlignment(Pos.CENTER);
            hallBox.setSpacing(20);
            hallBox.getChildren().addAll(scoresBox, backButton);

            // Set the hallOfFameScene to display the high scores and the back button
            hallOfFameScene = new Scene(hallBox, 1000, 800);
            stage.setScene(hallOfFameScene);
        });


        mainScene = new Scene(pane, 1000, 800);

        playButton.setOnAction(event -> initGame(stage, level));

        stage.setTitle("Asteroids");
        stage.setScene(mainScene);
        stage.show();
    }

    public static void main(String[] args) {
        System.out.println("Game Start!");
        launch();
    }

}
