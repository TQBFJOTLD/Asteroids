package Asteroids;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;

import java.io.*;
import java.util.*;


public class Game extends Application {
    private Scene mainScene;
    private Scene gameScene;
    public static final double pWidth = 1200, pHeight = 800;
    public static final int initialLives = 3;
    public static final double alienShipFiringInterval = 2, alienShipInterval = 20000; // Frequency of alienShip firing and generation
    private static final int largeAsteroidScore = 900, mediumAsteroidScore = 600, smallAsteroidScore = 300, alienShipScore = 1000, bonusLifeScore = 25000;
    private double centerX = pWidth / 2, centerY = pHeight / 2;
    private int lives = initialLives, extraLives = 0;
    private int level = 1;
    private int score = 0;
    private double lastAlienShipTime = 0.0;
    private AlienShip alienShip;
    private Text scoreText = null;
    private List<Polygon> lifeIndicators = new ArrayList<>();
    private List<ScoreEntry> highScores = new ArrayList<>();
    private List<Bullet> playerBullets;
    private List<Bullet> alienShipBullets;
    private Timeline alienShipFireTimeline;
    private AnimationTimer timer;
    private volatile boolean running;
    Text titleText = new Text("Asteroid");
    Text playText = new Text("New Game");
    Text hallText = new Text("Hall of Fame");
    Text helpText = new Text("Instruction");

    private Text createText(String text, double x, double y, Font font, EventHandler<MouseEvent> mouseClickedEvent, boolean needClickEvent) {
        Text newText = new Text(text);
        newText.setFont(font);
        newText.setFill(Color.WHITE);
        newText.setLayoutX(x - newText.getBoundsInLocal().getWidth() / 2);
        newText.setLayoutY(y);

        if (needClickEvent) {
            newText.setOnMouseEntered(event -> {
                newText.setFill(Color.BLUE);
                mainScene.setCursor(Cursor.HAND);
            });
            newText.setOnMouseExited(event -> {
                newText.setFill(Color.WHITE);
                mainScene.setCursor(Cursor.DEFAULT);
            });
            newText.setOnMouseClicked(mouseClickedEvent);
        }

        return newText;
    }

    private void setScoreText(Pane pane) {
        if (scoreText == null) {
            scoreText = createText("Score: " + score, pWidth - 150, 30, new Font("Courier New", 20), null, false);
        } else {
            scoreText.setText("Score: " + score);
        }
        if (!pane.getChildren().contains(scoreText)) {
            pane.getChildren().add(scoreText);
        }
    }

    private void playerDead(Player player, Scene scene, Pane pane, Stage stage) {
        player.destroyed();
        removeLife();
        player.resetPosition();
        if (lives == 0) {
            // Add Text
            Text endText = createText("Game Over", centerX, 250, new Font("Impact", 100), null, false);
            pane.getChildren().add(endText);

            Text RestartText = createText("Restart", centerX, 450, new Font("Courier New", 40), event -> restartGame(stage), true);
            pane.getChildren().add(RestartText);

            Text BackText = createText("Back to Menu", centerX, 550, new Font("Courier New", 40), event -> backToMenu(stage), true);
            pane.getChildren().add(BackText);

            // Stop the game loop
            timer.stop();
            int tmpScore = score + extraLives * bonusLifeScore;
            // Show input dialog for the player to enter their name
            Platform.runLater(() -> {
                System.out.println("Score = " + score);

                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Enter Your Name");
                dialog.setHeaderText(null);
                dialog.setContentText("Name:");


                Optional<String> result = dialog.showAndWait();
                result.ifPresent(name -> {

                    // Add the score to the high scores list
                    highScores.add(new ScoreEntry(name, tmpScore));
                    highScores.sort((entry1, entry2) -> Integer.compare(entry2.getScore(), entry1.getScore()));
                    saveScores();
                });
            });
            //Reset score
            score = 0;
            extraLives = 0;
        }
    }

    private void createLifeIndicators(Pane gamePane, Player player) {
        lifeIndicators.clear();
        for (int i = 0; i < lives; i++) {
            Polygon lifeIndicator = player.generateShape(0.6);
            lifeIndicator.setStroke(Color.WHITE);
            lifeIndicator.setLayoutX(20 + i * 20);
            lifeIndicator.setLayoutY(20);
            lifeIndicators.add(lifeIndicator);
            gamePane.getChildren().add(lifeIndicator);
        }
    }

    private void updateLives(Pane gamePane, Player player) {
        if (score >= bonusLifeScore) {
            lives++;
            extraLives++;
            score -= bonusLifeScore; // Deduct the points used for the extra life
            setScoreText(gamePane);

            // Create a new life indicator and add it to the game pane
            Polygon lifeIndicator = player.generateShape(0.6);
            lifeIndicator.setStroke(Color.WHITE);
            lifeIndicator.setLayoutX(20 + (lives - 1) * 20);
            lifeIndicator.setLayoutY(20);
            lifeIndicators.add(lifeIndicator);
            gamePane.getChildren().add(lifeIndicator);
        }
    }

    private void generateBackgroundAsteroids(Pane pane) {
        Random random = new Random();

        for (int i = 0; i < 10; i++) {
            double x = random.nextDouble() * pWidth;
            double y = random.nextDouble() * pHeight;
            double ratio = Asteroid.smallRatio + random.nextDouble() * (Asteroid.largeRatio - Asteroid.smallRatio);

            Asteroid asteroid = new Asteroid(x, y, ratio);
            asteroid.addToPane(pane);

            // Set a random direction and speed for the asteroid
            asteroid.setAngle(random.nextDouble() * 360);
            double speed = random.nextDouble();
            asteroid.setSpeed(speed);

            // Create a Timeline to update the asteroid's position
            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1.0 / 60.0), event -> {
                asteroid.update();
            }));

            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();
        }
    }

    private void generateLevelAsteroids(int level, List<Asteroid> asteroids, Player player, Pane pane) {
        int asteroidNumber = Math.min(level, 7) + (int) Math.log(level);
        // Generate asteroids for the level
        for (int i = 0; i < asteroidNumber; i++) {
            // Ensure the asteroid is generated at a safe distance from the player
            Pair<Double, Double> safeCoordinates = player.getSafeCoordinates(200);
            double x = safeCoordinates.getKey();
            double y = safeCoordinates.getValue();

            // Create 3 types of asteroids;
            double[] ratios = {Asteroid.smallRatio, Asteroid.mediumRatio, Asteroid.largeRatio};
            double ratio = ratios[(int) (Math.random() * ratios.length)];
            if (level == 1) ratio = Asteroid.largeRatio;
            Asteroid asteroid = new Asteroid(x, y, ratio);

            // Set Asteroid speed according to the level
            asteroid.setSpeed(asteroid.getSpeed() + (int) (level / 10));

            asteroid.addToPane(pane);
            asteroids.add(asteroid);
        }
    }

    private void saveScores() {
        try {
            File file = new File("highScores.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(file);

            for (ScoreEntry entry : highScores) {
                fileWriter.write(entry.getName() + "," + entry.getScore() + "\n");
            }
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadScores() {
        highScores = new ArrayList<>();
        try {
            File scoreFile = new File("highScores.txt");
            System.out.println("High scores file location: " + scoreFile.getAbsolutePath());
            if (scoreFile.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(scoreFile));
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] records = line.split(",");
                    highScores.add(new ScoreEntry(records[0], Integer.parseInt(records[1])));
                }
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void showHallOfFame(Stage stage) {
        Pane hallOfFamePane = new Pane();
        hallOfFamePane.setStyle("-fx-background-color: black;");
        Scene hallOfFameScene = new Scene(hallOfFamePane, pWidth, pHeight);

        // Add title
        Text hallOfFameTitle = createText("High Scores", centerX, 100, new Font("Impact", 50), null, false);
        hallOfFamePane.getChildren().add(hallOfFameTitle);

        // Add the scores
        for (int i = 0; i < Math.min(highScores.size(), 10); i++) {
            ScoreEntry entry = highScores.get(i);
            String playerName = entry.getName();
            int playerScore = entry.getScore();
            Text scoreText = createText((i + 1) + ". " + playerName + ": " + playerScore, centerX, 200 + i * 40, new Font("Courier New", 30), null, false);
            hallOfFamePane.getChildren().add(scoreText);
        }

        // Add a back button
        Text backButton = createText("Back", centerX, 700, new Font("Courier New", 40), event -> stage.setScene(mainScene), true);
        hallOfFamePane.getChildren().add(backButton);

        stage.setScene(hallOfFameScene);
    }

    private void showControls(Stage stage) {
        Pane controlsPane = new Pane();
        controlsPane.setStyle("-fx-background-color: black;");
        controlsPane.setPrefSize(pWidth, pHeight);
        Scene controlsScene = new Scene(controlsPane, pWidth, pHeight);

        Text controlsText1 = createText("Key Bindings", centerX, 100, new Font("Impact", 50), null, false);
        Text controlsText2 = createText("UP / W      Apply Thrust   ", centerX, 200, new Font("Courier New", 30), null, false);
        Text controlsText3 = createText("DOWN / S    Remove Thrust  ", centerX, 250, new Font("Courier New", 30), null, false);
        Text controlsText4 = createText("Left / A    Rotate Left    ", centerX, 300, new Font("Courier New", 30), null, false);
        Text controlsText5 = createText("Right / D   Rotate Right   ", centerX, 350, new Font("Courier New", 30), null, false);
        Text controlsText6 = createText("H           Hyperspace Jump", centerX, 400, new Font("Courier New", 30), null, false);
        Text controlsText7 = createText("Space       Fire           ", centerX, 450, new Font("Courier New", 30), null, false);
        Text controlsText8 = createText("ESC         Pause          ", centerX, 500, new Font("Courier New", 30), null, false);
        controlsPane.getChildren().addAll(controlsText1, controlsText2, controlsText3, controlsText4, controlsText5, controlsText6, controlsText7, controlsText8);

        // Add a back button
        Text backButton = createText("Back", centerX, 700, new Font("Courier New", 40), event -> stage.setScene(mainScene), true);
        controlsPane.getChildren().add(backButton);

        stage.setScene(controlsScene);

    }


    private void removeLife() {
        if (lives > 0) {
            lives--;
            lifeIndicators.get(lives).setVisible(false);
        }
    }

    private void restartGame(Stage stage) {
        // Reset lives and level
        resetVal();
        lifeIndicators.clear();
        lastAlienShipTime = System.currentTimeMillis();
        initGame(stage);
    }

    private void backToMenu(Stage stage) {
        // Reset lives and level
        resetVal();
        stage.setScene(mainScene);
        stage.setTitle("Asteroids");
    }

    public void resetVal() {
        lives = initialLives;
        extraLives = 0;
        score = 0;
        level = 1;
    }

    private void spawnAlienShip(Player player, Pane pane) {
        // Ensure the alien ship is generated at a safe distance from the player
        Pair<Double, Double> safeCoordinates = player.getSafeCoordinates(200);
        double sx = safeCoordinates.getKey();
        double sy = safeCoordinates.getValue();

        // Generate alienShip every (alienShipInterval + Math.random() * 1000) milliseconds
        if (alienShip == null && System.currentTimeMillis() - lastAlienShipTime >= alienShipInterval + Math.random() * 1000) {
            lastAlienShipTime = System.currentTimeMillis();
            alienShip = new AlienShip(sx, sy);
            alienShip.addToPane(pane);
            alienShip.setSpeed(1);
            alienShip.setAngle(Math.random() * 180);
            alienShip.setDx(alienShip.getSpeed() * Math.sin(Math.toRadians(alienShip.getAngle())));
            alienShip.setDy(-alienShip.getSpeed() * Math.cos(Math.toRadians(alienShip.getAngle())));

            // Start alienShipFiring
            initializeAlienShipFiring(player, pane);
        }
    }

    private void initializeAlienShipFiring(Player player, Pane pane) {
        if (alienShipFireTimeline != null) {
            alienShipFireTimeline.stop();
        }

        // Fire towards the player every alienShipFiringInterval seconds
        if (alienShip != null) {
            alienShipFireTimeline = new Timeline(new KeyFrame(Duration.seconds(alienShipFiringInterval), event -> {
                if (alienShip != null) {
                    Bullet bullet = alienShip.fire(pane, player);
                    alienShipBullets.add(bullet);
                }
            }));

            alienShipFireTimeline.setCycleCount(Timeline.INDEFINITE);
            alienShipFireTimeline.play();
        }
    }


    private void checkForSuccess(List<Asteroid> asteroids, Pane pane, Stage stage) {
        if (asteroids.isEmpty() && alienShip == null) {
            // Set Success Text
            Text successText = createText("SUCCESS", centerX, 150, new Font("Courier New", 45), null, false);
            successText.setLayoutX(centerX - successText.getBoundsInLocal().getWidth() / 2);
            pane.getChildren().add(successText);

            // Pause for 2 seconds before moving on to the next level
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            level++;
            pause.setOnFinished(event -> initGame(stage));
            pause.play();
            timer.stop();
        }
    }


    private void initGame(Stage stage) {
        stage.setTitle("Level " + level);
        alienShip = null;

        // Create the game scene
        Pane gamePane = new Pane();
        gamePane.setPrefSize(pWidth, pHeight);
        gameScene = new Scene(gamePane, pWidth, pHeight);
        stage.setScene(gameScene);
        gamePane.setStyle("-fx-background-color: black;");


        // Create the player and add it to the game pane
        Player player = new Player(pWidth / 2, pHeight / 2, gamePane, gameScene);
        player.addToPane(gamePane);
        player.getShape().requestFocus();

        createLifeIndicators(gamePane, player);

        List<Asteroid> asteroids = new ArrayList<>();
        playerBullets = new ArrayList<>();
        alienShipBullets = new ArrayList<>();

        // Generate asteroids based on the level
        generateLevelAsteroids(level, asteroids, player, gamePane);

        setScoreText(gamePane);

        timer = new AnimationTimer() {
            @Override
            public void handle(long l) {
                player.update();
                spawnAlienShip(player, gamePane);
                if (alienShip != null) {
                    alienShip.update();
                }

                // Call the fire method and add the returned bullet to the playerBullets list
                if (player.isFiring() && player.canFire(playerBullets.size())) {
                    long now = System.currentTimeMillis();
                    if (now - player.lastFiringTime > player.playerFiringInterval) {
                        playerBullets.add(player.fire(gamePane));
                        player.lastFiringTime = now;
                    }
                }

                for (Asteroid asteroid : asteroids) {
                    asteroid.update();
                    // check for collisions with player ship and playerBullets
                    if (!player.isInvulnerable() && player.intersects(asteroid)) {
                        playerDead(player, gameScene, gamePane, stage);
                        if (lives == 0) this.stop();
                    }
                }

                player.updateInvulnerability();

                for (Iterator<Bullet> bulletIterator = playerBullets.iterator(); bulletIterator.hasNext(); ) {
                    Bullet bullet = bulletIterator.next();
                    bullet.update();

                    // Check for collisions between playerBullets and the AlienShip
                    if (alienShip != null && bullet.intersects(alienShip.getShape())) {
                        // Remove the bullet from the playerBullets list and the game pane
                        bullet.removeFromPane(gamePane);
                        bulletIterator.remove();

                        // Remove the AlienShip from the game pane and set it to null
                        alienShip.removeFromPane(gamePane);
                        alienShip = null;

                        // Stop the alienShipFireTimeline when the alienShip is destroyed
                        if (alienShipFireTimeline != null) {
                            alienShipFireTimeline.stop();
                        }

                        // Update the score
                        score += alienShipScore;
                        setScoreText(gamePane);

                        // Check if successful
                        checkForSuccess(asteroids, gamePane, stage);
                    }

                    // Check for collisions between playerBullets and asteroids
                    for (Iterator<Asteroid> asteroidIterator = asteroids.iterator(); asteroidIterator.hasNext(); ) {
                        Asteroid asteroid = asteroidIterator.next();
                        if (bullet.intersects(asteroid.getShape())) {
                            // Remove the bullet and the asteroid from their respective lists and the game pane
                            bullet.removeFromPane(gamePane);
                            bulletIterator.remove();

                            double destroyedAsteroidRatio = asteroid.getRatio();
                            double destroyedSpeed = asteroid.getSpeed();

                            asteroid.removeFromPane(gamePane);
                            asteroidIterator.remove();

                            // Calculate and update the score
                            int asteroidScore = 0;
                            if (asteroid.getRatio() == Asteroid.largeRatio) {
                                asteroidScore = largeAsteroidScore;
                            } else if (asteroid.getRatio() == Asteroid.mediumRatio) {
                                asteroidScore = mediumAsteroidScore;
                            } else if (asteroid.getRatio() == Asteroid.smallRatio) {
                                asteroidScore = smallAsteroidScore;
                            }
                            score += asteroidScore;
                            setScoreText(gamePane);

                            // Update lives
                            updateLives(gamePane, player);

                            // Create new asteroids
                            List<Asteroid> newAsteroids = asteroid.destroyed(destroyedAsteroidRatio, gamePane, destroyedSpeed);
                            asteroids.addAll(newAsteroids);

                            // Check if successful
                            checkForSuccess(asteroids, gamePane, stage);
                            break;
                        }
                    }
                }

                for (Iterator<Bullet> bulletIterator = alienShipBullets.iterator(); bulletIterator.hasNext(); ) {
                    Bullet bullet = bulletIterator.next();
                    bullet.update();
                    boolean bulletRemoved = false;

                    player.setEnemies(asteroids, alienShip, alienShipBullets);
                    if (!player.isInvulnerable() && !bulletRemoved && bullet.intersects(player.getShape())) {
                        // Remove the bullet from the gamePane
                        bullet.removeFromPane(gamePane);
                        if (!bulletRemoved) {
                            bulletIterator.remove();
                        }
                        playerDead(player, gameScene, gamePane, stage);
                        if (lives == 0) this.stop();
                    }
                }
            }

            @Override
            public void start() {
                super.start();
                running = true;
            }

            @Override
            public void stop() {
                super.stop();
                running = false;
            }

        };
        timer.start();

        // Pause menu
        Pane pausePane = new Pane();
        pausePane.setVisible(false);

        // Create a transparent background
        Rectangle background = new Rectangle(0, 0, gameScene.getWidth(), gameScene.getHeight());
        background.setFill(Color.rgb(0, 0, 0, 0.5));
        pausePane.getChildren().add(background);

        Text resumeText = createText("Resume", centerX, centerY - 50, new Font("Courier New", 50), event -> {
            timer.start();
            if (alienShip != null && alienShipFireTimeline != null) {
                alienShipFireTimeline.play();
            }
            pausePane.setVisible(false);
        }, true);

        pausePane.getChildren().add(resumeText);

        Text backToMenuText = createText("Back to Menu", centerX, centerY + 50, new Font("Courier New", 50), event -> backToMenu(stage), true);
        pausePane.getChildren().add(backToMenuText);
        gamePane.getChildren().add(pausePane);


        // Listen for the ESC key press to pause the game
        gameScene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                if (running) {
                    timer.stop();
                    if (alienShip != null && alienShipFireTimeline != null) {
                        alienShipFireTimeline.pause();
                    }
                    pausePane.setVisible(true);
                } else {
                    timer.start();
                    if (alienShip != null && alienShipFireTimeline != null) {
                        alienShipFireTimeline.play();
                    }
                    pausePane.setVisible(false);
                }
            }
        });

        // Request focus on the game pane to receive key inputs
        gamePane.requestFocus();
    }

    @Override
    public void start(Stage stage) throws IOException {
        Pane mainPane = new Pane();
        mainScene = new Scene(mainPane, pWidth, pHeight);
        mainPane.setStyle("-fx-background-color: black;");

        // Add background asteroids
        generateBackgroundAsteroids(mainPane);

        loadScores();
        titleText = createText("Asteroid", centerX, 250, new Font("Impact", 100), null, false);
        playText = createText("Play", centerX, 450, new Font("Courier New", 40), event -> initGame(stage), true);
        hallText = createText("Hall of Fame", centerX, 550, new Font("Courier New", 40), event -> showHallOfFame(stage), true);
        helpText = createText("Help", centerX, 650, new Font("Courier New", 40), event -> showControls(stage), true);

        mainPane.getChildren().addAll(titleText, playText, hallText, helpText);

        stage.setTitle("Asteroids");
        stage.setScene(mainScene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
