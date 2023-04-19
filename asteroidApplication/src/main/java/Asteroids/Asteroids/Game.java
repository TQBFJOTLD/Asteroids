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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
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
    private SoundEffects soundEffects = new SoundEffects();
    public static final double PANE_WIDTH = 1200, PANE_HEIGHT = 800;
    public static final int INITIAL_LIVES = 10;
    public static final double ALIEN_SHIP_FIRING_INTERVAL = 2, ALIEN_SHIP_INTERVAL = 20000; // Frequency of alienShip firing and generation
    private static final int LARGE_ASTEROID_SCORE = 900, MEDIUM_ASTEROID_SCORE = 600, SMALL_ASTEROID_SCORE = 300, ALIEN_SHIP_SCORE = 1000, BONUS_LIFE_SCORE = 10000;
    private double centerX = PANE_WIDTH / 2, centerY = PANE_HEIGHT / 2;
    private static long BULLET_LIFE_SPAN = 2000;
    private int lives = INITIAL_LIVES, extraLives = 0;
    private int level = 1;
    private int score = 0;
    private double lastAlienShipTime = 0.0;
    private int letterArrayPos = 0;
    private AlienShip alienShip;
    private Text scoreText = null;
    private Text levelText = null;
    private List<Polygon> lifeIndicators = new ArrayList<>();
    private List<ScoreEntry> highScores = new ArrayList<>();
    private List<Bullet> playerBullets;
    private List<Bullet> alienShipBullets;
    private Timeline alienShipFireTimeline;
    private AnimationTimer timer;
    private volatile boolean running;
    public static boolean isPaused = false;
    private String userName = "";
    private int tmpScore = 0, currentMainOption = 0, currentPauseOption, currentEndOption;
    public static boolean gameOver = false;
    Text titleText = new Text();
    Text playText = new Text();
    Text hallText = new Text();
    Text helpText = new Text();
    Text quitText = new Text();
    Font Atarifont = Font.loadFont("file:SF Atarian System.ttf", 200);
    Font mariofont = Font.loadFont("file:Super Mario Maker Font.ttf", 40);

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

    private String addSpacesBetweenDigits(int number) {
        String numberStr = Integer.toString(number);
        StringBuilder spacedNumber = new StringBuilder();

        for (int i = 0; i < numberStr.length(); i++) {
            spacedNumber.append(numberStr.charAt(i));
            if (i < numberStr.length() - 1) {
                spacedNumber.append(' ');
            }
        }

        return spacedNumber.toString();
    }


    private void setScoreText(Pane pane) {
        if (scoreText == null) {
            scoreText = createText("S c o r e : " + addSpacesBetweenDigits(score), PANE_WIDTH - 150, 30, Font.loadFont("file:Super Mario Maker Font.ttf", 20), null, false);
        } else {
            scoreText.setText("S c o r e : " + addSpacesBetweenDigits(score));
        }
        if (!pane.getChildren().contains(scoreText)) {
            pane.getChildren().add(scoreText);
        }
    }

    private void setLevelText(Pane pane) {
        if (levelText == null) {
            levelText = createText("L E V E L : " + addSpacesBetweenDigits(level), centerX, 30, Font.loadFont("file:Super Mario Maker Font.ttf", 20), null, false);
        } else {
            levelText.setText("L E V E L : " + addSpacesBetweenDigits(level));
        }
        if (!pane.getChildren().contains(levelText)) {
            pane.getChildren().add(levelText);
        }
    }

    private void playerDead(Player player, Scene scene, Pane pane, Stage stage) {
        System.out.println("Player destroyed! lives = " + lives);
        player.destroyed();
        removeLife(pane);
        player.resetPosition();
        if (lives == 0) {
            soundEffects.playGameOverSound();
            System.out.println("GAME OVER!");
            gameOver = true;
            timer.stop();

            // Set temporary score
            tmpScore = score + extraLives * BONUS_LIFE_SCORE;
            Text endText = createText("G A M E  O V E R", centerX, 250, Font.loadFont("file:Super Mario Maker Font.ttf", 100), null, false);
            pane.getChildren().add(endText);

            Platform.runLater(() -> {
            // Input name and score
            if (true) {
                Text entryLabelText = createText("E N T E R   Y O U R   N A M E", centerX, 350, mariofont, null, false);
                pane.getChildren().add(entryLabelText);

                //Reset UserName
                userName = "";

                //Array to toggle value
                String[] letters = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "_"};

                //Array to hold 3 numbers that = Alphabet array index to simplify toggle between letters userName
                ArrayList<Integer> entryletters = new ArrayList<Integer>();
                entryletters.add(0);entryletters.add(0);entryletters.add(0);

                // 0 =  "A" to start
                letterArrayPos = 0;
                ArrayList<Text> entryLetterTexts = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    Text letterText = createText(letters[entryletters.get(i)], centerX + (i - 1) * 35, 550, mariofont, null, false);
                    entryLetterTexts.add(letterText);
                    pane.getChildren().add(letterText);
                }
                entryLetterTexts.get(letterArrayPos).setFill(Color.BLUE);

                userName = letters[entryletters.get(0)] + letters[entryletters.get(1)] + letters[entryletters.get(2)];

                //"OK" Button  to save name & score
                Text okBtn = createText("O K", centerX, 650, mariofont, event -> addHighScores(letters, entryletters, stage), true);
                pane.getChildren().add(okBtn);

                scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    stage.setScene(scene);
                    stage.show();

                    // Listen for keys
                    if (event.getCode() == KeyCode.LEFT ){
                        soundEffects.playKeySound();
                        System.out.println("Left pressed!");
                        if (letterArrayPos > 0) {
                            entryLetterTexts.get(letterArrayPos).setFill(Color.WHITE);
                            letterArrayPos--;
                            entryLetterTexts.get(letterArrayPos).setFill(Color.BLUE);
                        }
                    } else if(event.getCode() == KeyCode.RIGHT ){
                        soundEffects.playKeySound();
                        System.out.println("Right pressed!");
                        if (letterArrayPos < 2) {
                            entryLetterTexts.get(letterArrayPos).setFill(Color.WHITE);
                            letterArrayPos++;
                            entryLetterTexts.get(letterArrayPos).setFill(Color.BLUE);
                        }
                    } else if(event.getCode() == KeyCode.UP ){
                        soundEffects.playKeySound();
                        System.out.println("UP pressed!");
                        int pos = entryletters.get(letterArrayPos);
                        if (pos == 0) {
                            entryletters.set(letterArrayPos, 26);
                            pos = 26;
                        }else {
                            entryletters.set(letterArrayPos, -- pos);
                        }
                        entryLetterTexts.get(letterArrayPos).setText(letters[entryletters.get(letterArrayPos)]);
                        if (entryLetterTexts.get(letterArrayPos).getParent() != pane) {
                            pane.getChildren().add(entryLetterTexts.get(letterArrayPos));
                        }

                    } else if(event.getCode() == KeyCode.DOWN ){
                        soundEffects.playKeySound();
                        System.out.println("DOWN pressed!");
                        int pos = entryletters.get(letterArrayPos);
                        System.out.println("POS: " + pos);
                        if (pos == 26) {
                            entryletters.set(letterArrayPos, 0);
                            pos = 0;
                        }else {
                            entryletters.set(letterArrayPos, ++ pos);
                        }
                        entryLetterTexts.get(letterArrayPos).setText(letters[entryletters.get(letterArrayPos)]);
                        if (entryLetterTexts.get(letterArrayPos).getParent() != pane) {
                            pane.getChildren().add(entryLetterTexts.get(letterArrayPos));
                        }
                    } else if(event.getCode() == KeyCode.ENTER ) {
                        soundEffects.playKeySound();
                        System.out.println("ENTER pressed!");
                        MouseEvent mouseEvent = new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY, 1, true, true, true, true, true, true, true, true, true, true, null);
                        okBtn.fireEvent(mouseEvent);
                    }
                });

            }
            });
            //Reset score
            score = 0;
            extraLives = 0;
        }
    }

    private void addHighScores(String[] letters, ArrayList<Integer> entryletters, Stage stage) {
        // Add the score to the high scores list
        userName = letters[entryletters.get(0)] + letters[entryletters.get(1)] + letters[entryletters.get(2)];
        highScores.add(new ScoreEntry(userName, tmpScore));
        highScores.sort((entry1, entry2) -> Integer.compare(entry2.getScore(), entry1.getScore()));
        saveScores();
//        backToMenu(stage);
        showHallOfFame(stage);
    }


    private void createLifeIndicators(Pane gamePane) {
        lifeIndicators.clear();
        for (int i = 0; i < lives; i++) {
            Polygon lifeIndicator = Player.generateShape(0.6);
            lifeIndicator.setStroke(Color.WHITE);
            lifeIndicator.setLayoutX(20 + i * 20);
            lifeIndicator.setLayoutY(20);
            lifeIndicators.add(lifeIndicator);
            gamePane.getChildren().add(lifeIndicator);
        }
        System.out.println("number of indicators: " + lifeIndicators.size());
    }

    private void updateLives(Pane gamePane, Player player) {
        if (score >= BONUS_LIFE_SCORE) {
            lives++;
            extraLives++;
            score -= BONUS_LIFE_SCORE; // Deduct the points used for the extra life
            setScoreText(gamePane);
            soundEffects.playBonusSound();

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
            double x = random.nextDouble() * PANE_WIDTH;
            double y = random.nextDouble() * PANE_HEIGHT;
            double ratio = Asteroid.SMALL_RATIO + random.nextDouble() * (Asteroid.LARGE_RATIO - Asteroid.SMALL_RATIO);

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
            double[] ratios = {Asteroid.SMALL_RATIO, Asteroid.MEDIUM_RATIO, Asteroid.LARGE_RATIO};
            double ratio = ratios[(int) (Math.random() * ratios.length)];
            if (level == 1) ratio = Asteroid.LARGE_RATIO;
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
        Scene hallOfFameScene = new Scene(hallOfFamePane, PANE_WIDTH, PANE_HEIGHT);

        // Add title
        Text hallOfFameTitle = createText("H I G H   S C O R E S", centerX, 100, Font.loadFont("file:Super Mario Maker Font.ttf", 80), null, false);
        hallOfFamePane.getChildren().add(hallOfFameTitle);

        // Add the scores
        for (int i = 0; i < Math.min(highScores.size(), 10); i++) {
            ScoreEntry entry = highScores.get(i);
            String playerName = entry.getName();
            int playerScore = entry.getScore();
            Text scoreText = createText((i + 1) + ". " + playerName + " :  " + addSpacesBetweenDigits(playerScore), centerX, 200 + i * 40, Font.loadFont("file:Super Mario Maker Font.ttf", 30), null, false);
            hallOfFamePane.getChildren().add(scoreText);
        }

        // Add a back button
        Text backButton = createText("B A C K", centerX, 700, Font.loadFont("file:Super Mario Maker Font.ttf", 40), event -> stage.setScene(mainScene), true);
        backButton.setFill(Color.BLUE);
        hallOfFamePane.getChildren().add(backButton);
        hallOfFameScene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                soundEffects.playKeySound();
                MouseEvent mouseEvent = new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY, 1, true, true, true, true, true, true, true, true, true, true, null);
                backButton.fireEvent(mouseEvent);
            }
        });

        stage.setScene(hallOfFameScene);
    }

    private void showControls(Stage stage) {
        Pane controlsPane = new Pane();
        controlsPane.setStyle("-fx-background-color: black;");
        controlsPane.setPrefSize(PANE_WIDTH, PANE_HEIGHT);
        Scene controlsScene = new Scene(controlsPane, PANE_WIDTH, PANE_HEIGHT);

        Text controlsText1 = createText("K E Y   B I N D I N G S", centerX, 100, Font.loadFont("file:Super Mario Maker Font.ttf", 80), null, false);
        controlsPane.getChildren().add(controlsText1);

        GridPane controlsGrid = new GridPane();
        controlsGrid.setHgap(20); // Set horizontal gap between columns
        controlsGrid.setVgap(10); // Set vertical gap between rows
        controlsGrid.setLayoutX(centerX - 250); // Adjust the layout position
        controlsGrid.setLayoutY(200);

        // Adding text to the GridPane
        controlsGrid.add(createText("U P / W", 0, 0, Font.loadFont("file:Super Mario Maker Font.ttf", 30), null, false), 0, 0);
        controlsGrid.add(createText("A P P L Y  T H R U S T", 0, 0, Font.loadFont("file:Super Mario Maker Font.ttf", 30), null, false), 2, 0);

        controlsGrid.add(createText("D O W N / S", 0, 0, Font.loadFont("file:Super Mario Maker Font.ttf", 30), null, false), 0, 1);
        controlsGrid.add(createText("R E M O V E  T H R U S T", 0, 0, Font.loadFont("file:Super Mario Maker Font.ttf", 30), null, false), 2, 1);

        controlsGrid.add(createText("L E F T / A", 0, 0, Font.loadFont("file:Super Mario Maker Font.ttf", 30), null, false), 0, 2);
        controlsGrid.add(createText("R O T A T E  L E F T", 0, 0, Font.loadFont("file:Super Mario Maker Font.ttf", 30), null, false), 2, 2);

        controlsGrid.add(createText("R I G H T / D", 0, 0, Font.loadFont("file:Super Mario Maker Font.ttf", 30), null, false), 0, 3);
        controlsGrid.add(createText("R O T A T E  R I G H T", 0, 0, Font.loadFont("file:Super Mario Maker Font.ttf", 30), null, false), 2, 3);

        controlsGrid.add(createText("H", 0, 0, Font.loadFont("file:Super Mario Maker Font.ttf", 30), null, false), 0, 4);
        controlsGrid.add(createText("H Y P E R S P A C E  J U M P", 0, 0, Font.loadFont("file:Super Mario Maker Font.ttf", 30), null, false), 2, 4);

        controlsGrid.add(createText("S P A C E", 0, 0, Font.loadFont("file:Super Mario Maker Font.ttf", 30), null, false), 0, 5);
        controlsGrid.add(createText("F I R E", 0, 0, Font.loadFont("file:Super Mario Maker Font.ttf", 30), null, false), 2, 5);

        controlsGrid.add(createText("E S C", 0, 0, Font.loadFont("file:Super Mario Maker Font.ttf", 30), null, false), 0, 6);
        controlsGrid.add(createText("P A U S E", 0, 0, Font.loadFont("file:Super Mario Maker Font.ttf", 30), null, false), 2, 6);

        // Add the GridPane to the parent pane
        controlsPane.getChildren().add(controlsGrid);


        // Add a back button
        Text backButton = createText("B A C K", centerX, 700, Font.loadFont("file:Super Mario Maker Font.ttf", 40), event -> stage.setScene(mainScene), true);
        controlsPane.getChildren().add(backButton);
        backButton.setFill(Color.BLUE);
        controlsScene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                soundEffects.playKeySound();
                MouseEvent mouseEvent = new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY, 1, true, true, true, true, true, true, true, true, true, true, null);
                backButton.fireEvent(mouseEvent);
            }
        });

        stage.setScene(controlsScene);

    }

    private void removeLife(Pane pane) {
        if (lives > 0) {
            lives--;
            for (int i = 0; i < lifeIndicators.size(); i ++) {
                Polygon lifeIndicator = lifeIndicators.get(i);
                pane.getChildren().remove(lifeIndicator);
            }
            createLifeIndicators(pane);
        }
    }

    private void restartGame(Stage stage) {
        // Reset lives and level
        gameOver = false;
        resetVal();
        lifeIndicators.clear();
        lastAlienShipTime = System.currentTimeMillis();
        initGame(stage);
    }

    private void backToMenu(Stage stage) {
        gameOver = false;
        // Reset lives and level
        resetVal();
        lifeIndicators.clear();
        stage.setScene(mainScene);
        if (timer != null) {
            timer.stop();
        }

        playText.setFill(Color.BLUE);
        stage.setTitle("Asteroids");
    }


    public void resetVal() {
        lives = INITIAL_LIVES;
        lifeIndicators.clear();
        extraLives = 0;
        score = 0;
        level = 1;
    }

    private void spawnAlienShip(Player player, Pane pane) {
        // Ensure the alien ship is generated at a safe distance from the player
        Pair<Double, Double> safeCoordinates = player.getSafeCoordinates(Player.SAFE_DISTANCE);
        double sx = safeCoordinates.getKey();
        double sy = safeCoordinates.getValue();

        // Generate alienShip every (ALIEN_SHIP_INTERVAL + Math.random() * 1000) milliseconds
        if (alienShip == null && System.currentTimeMillis() - lastAlienShipTime >= ALIEN_SHIP_INTERVAL + Math.random() * 1000) {
            lastAlienShipTime = System.currentTimeMillis();
            alienShip = new AlienShip(sx, sy);
            alienShip.addToPane(pane);
            alienShip.setSpeed(5);
            alienShip.setAngle(Math.random() * 180);
            alienShip.setDx(alienShip.getSpeed() * Math.sin(Math.toRadians(alienShip.getAngle())));
            alienShip.setDy(-alienShip.getSpeed() * Math.cos(Math.toRadians(alienShip.getAngle())));

            rotateAlienShips(alienShip);

            // Start alienShipFiring
            initializeAlienShipFiring(player, pane);
        }
    }

    private void rotateAlienShips(AlienShip alienShip) {
        Timeline alienShipTimeline = new Timeline(new KeyFrame(Duration.seconds(3), event -> {
            alienShip.setAngle(Math.random() * 180);
            alienShip.setDx(alienShip.getSpeed() * Math.sin(Math.toRadians(alienShip.getAngle())));
            alienShip.setDy(-alienShip.getSpeed() * Math.cos(Math.toRadians(alienShip.getAngle())));
        }
        ));
        alienShipTimeline.setCycleCount(Timeline.INDEFINITE);
        alienShipTimeline.play();
    }

    private void initializeAlienShipFiring(Player player, Pane pane) {
        if (alienShipFireTimeline != null) {
            alienShipFireTimeline.stop();
        }

        // Fire towards the player every ALIEN_SHIP_FIRING_INTERVAL seconds
        if (alienShip != null) {
            alienShipFireTimeline = new Timeline(new KeyFrame(Duration.seconds(ALIEN_SHIP_FIRING_INTERVAL), event -> {
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
            Text successText = createText("S U C C E S S", centerX, 150, mariofont, null, false);
            successText.setLayoutX(centerX - successText.getBoundsInLocal().getWidth() / 2);
            pane.getChildren().add(successText);
            soundEffects.playLevelSound();

            // Pause for 2 seconds before moving on to the next level
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            level++;
            pause.setOnFinished(event -> initGame(stage));
            pause.play();
            timer.stop();
        }
    }


    private void initGame(Stage stage) {
        gameOver = false;
        isPaused = false;
        stage.setTitle("Level " + level);
        alienShip = null;

        // Create the game scene
        Pane gamePane = new Pane();
        gamePane.setPrefSize(PANE_WIDTH, PANE_HEIGHT);
        gameScene = new Scene(gamePane, PANE_WIDTH, PANE_HEIGHT);
        stage.setScene(gameScene);
        gamePane.setStyle("-fx-background-color: black;");

        // Create the player and add it to the game pane
        Player player = new Player(PANE_WIDTH / 2, PANE_HEIGHT / 2, gamePane, gameScene);
        player.addToPane(gamePane);
        player.getShape().requestFocus();

        createLifeIndicators(gamePane);

        List<Asteroid> asteroids = new ArrayList<>();
        playerBullets = new ArrayList<>();
        alienShipBullets = new ArrayList<>();

        // Generate asteroids based on the level
        generateLevelAsteroids(level, asteroids, player, gamePane);

        // Set level and score texts
        setLevelText(gamePane);
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
                    if (now - player.lastFiringTime > player.PLAYER_FIRING_INTERVAL) {
                        playerBullets.add(player.fire(gamePane));
                        player.lastFiringTime = now;
                    }
                }

                for (Asteroid asteroid : asteroids) {
                    asteroid.update();
                    // check for collisions with player ship and asteroid
                    if (!player.isInvulnerable() && player.intersects(asteroid)) {
                        soundEffects.playBangSound();
                        playerDead(player, gameScene, gamePane, stage);
                        if (lives == 0) this.stop();
                    }
                }

                player.updateInvulnerability();
                player.updateJumpStatus();

                for (Iterator<Bullet> bulletIterator = playerBullets.iterator(); bulletIterator.hasNext(); ) {
                    Bullet bullet = bulletIterator.next();
                    bullet.update();
                    boolean bulletRemoved = false;

                    // Check for collisions between playerBullets and the AlienShip
                    if (alienShip != null && bullet.intersects(alienShip.getShape())) {
                        soundEffects.playBangSound();
                        // Remove the bullet from the playerBullets list and the game pane
                        bullet.removeFromPane(gamePane);
                        bulletIterator.remove();
                        bulletRemoved = true;

                        // Remove the AlienShip from the game pane and set it to null
                        alienShip.removeFromPane(gamePane);
                        alienShip = null;

                        // Stop the alienShipFireTimeline when the alienShip is destroyed
                        if (alienShipFireTimeline != null) {
                            alienShipFireTimeline.stop();
                        }

                        // Update the score
                        score += ALIEN_SHIP_SCORE;
                        setScoreText(gamePane);

                        // Update lives
                        updateLives(gamePane, player);

                        // Check if successful
                        checkForSuccess(asteroids, gamePane, stage);
                    }

                    // Check for collisions between playerBullets and asteroids
                    for (Iterator<Asteroid> asteroidIterator = asteroids.iterator(); asteroidIterator.hasNext(); ) {
                        Asteroid asteroid = asteroidIterator.next();

                        if (!bulletRemoved && System.currentTimeMillis() - bullet.getCreationTime() > BULLET_LIFE_SPAN) {
                            bullet.removeFromPane(gamePane);
                            bulletIterator.remove();
                            bulletRemoved = true;
                        } else {
                            if (bullet.intersects(asteroid.getShape())) {
                                soundEffects.playBangSound();
                                // Remove the bullet and the asteroid from their respective lists and the game pane
                                if (!bulletRemoved) {
                                    bullet.removeFromPane(gamePane);
                                    bulletIterator.remove();
                                }

                                double destroyedAsteroidRatio = asteroid.getRatio();
                                double destroyedSpeed = asteroid.getSpeed();

                                asteroid.removeFromPane(gamePane);
                                asteroidIterator.remove();

                                // Calculate and update the score
                                int asteroidScore = 0;
                                if (asteroid.getRatio() == Asteroid.LARGE_RATIO) {
                                    asteroidScore = LARGE_ASTEROID_SCORE;
                                } else if (asteroid.getRatio() == Asteroid.MEDIUM_RATIO) {
                                    asteroidScore = MEDIUM_ASTEROID_SCORE;
                                } else if (asteroid.getRatio() == Asteroid.SMALL_RATIO) {
                                    asteroidScore = SMALL_ASTEROID_SCORE;
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
                        }};

                    }
                }

                // Check collisions between alienship and player
                if (alienShip != null && !player.isInvulnerable() && alienShip.intersects(player)){
                    soundEffects.playBangSound();
                    playerDead(player, gameScene, gamePane, stage);
                }

                for (Iterator<Bullet> bulletIterator = alienShipBullets.iterator(); bulletIterator.hasNext(); ) {
                    Bullet bullet = bulletIterator.next();
                    bullet.update();
                    boolean bulletRemoved = false;
                    player.setEnemies(asteroids, alienShip, alienShipBullets);

                    // Check if the bullet has been alive for more than three seconds
                    if (System.currentTimeMillis() - bullet.getCreationTime() > BULLET_LIFE_SPAN) {
                        bullet.removeFromPane(gamePane);
                        if (!bulletRemoved) {
                            bulletIterator.remove();
                            bulletRemoved = true;
                        }
                    } else {
                        if (!player.isInvulnerable() && !bulletRemoved && bullet.intersects(player.getShape())) {
                            soundEffects.playBangSound();
                            // Remove the bullet from the gamePane
                            bullet.removeFromPane(gamePane);
                            if (!bulletRemoved) {
                                bulletIterator.remove();
                            }
                            playerDead(player, gameScene, gamePane, stage);
                            if (lives == 0) this.stop();
                        }
                    };
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

        Text resumeText = createText("R E S U M E", centerX, 400, mariofont, event -> {
            timer.start();
            if (alienShip != null && alienShipFireTimeline != null) {
                alienShipFireTimeline.play();
            }
            pausePane.setVisible(false);
        }, true);

        pausePane.getChildren().add(resumeText);

        Text BackText = createText("B A C K   T O   M E N U", centerX, 500, mariofont, event -> backToMenu(stage), true);
        pausePane.getChildren().add(BackText);

        Text quitText = createText("Q U I T  G A M E", centerX, 600, mariofont, event -> {
            if (timer != null) timer.stop();
            stage.close();
        }, true);
        pausePane.getChildren().add(quitText);
        gamePane.getChildren().add(pausePane);


        // Highlight the resumeText option by default
        highlightOption(resumeText, Arrays.asList(BackText, quitText));
        currentPauseOption = 0;
        List<Text> pauseMenuOptions = Arrays.asList(resumeText, BackText, quitText);

        // Listen for the ESC key press to pause the game
        gameScene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE && !gameOver) {
                isPaused = !isPaused;
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
            } else if (event.getCode() == KeyCode.DOWN) {
                if (isPaused) soundEffects.playKeySound();
                currentPauseOption = (currentPauseOption + 1) % pauseMenuOptions.size();
                List<Text> otherTexts = new ArrayList<>();
                for (Text text : pauseMenuOptions) {
                    if (text != pauseMenuOptions.get(currentPauseOption)) {
                        otherTexts.add(text);
                    }
                }
                highlightOption(pauseMenuOptions.get(currentPauseOption), otherTexts);
            } else if (event.getCode() == KeyCode.UP) {
                if (isPaused) soundEffects.playKeySound();
                currentPauseOption = (currentPauseOption - 1 + pauseMenuOptions.size()) % pauseMenuOptions.size();
                List<Text> otherTexts = new ArrayList<>();
                for (Text text : pauseMenuOptions) {
                    if (text != pauseMenuOptions.get(currentPauseOption)) {
                        otherTexts.add(text);
                    }
                }
                highlightOption(pauseMenuOptions.get(currentPauseOption), otherTexts);
            } else if (event.getCode() == KeyCode.ENTER) {
                if (currentPauseOption == 0) isPaused = false;
                if (isPaused) soundEffects.playKeySound();
                MouseEvent mouseEvent = new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY, 1, true, true, true, true, true, true, true, true, true, true, null);
                pauseMenuOptions.get(currentPauseOption).fireEvent(mouseEvent);
            }
        });

        // Request focus on the game pane to receive key inputs
        gamePane.requestFocus();
    }

    private void highlightOption(Text selected, List<Text> otherTexts) {
        // Set the selected text color to blue
        selected.setFill(Color.BLUE);

        // Set the other text colors to white
        for (Text other : otherTexts) {
            other.setFill(Color.WHITE);
        }
    }

    @Override
    public void start(Stage stage) throws IOException {
        Pane mainPane = new Pane();
        mainScene = new Scene(mainPane, PANE_WIDTH, PANE_HEIGHT);
        mainPane.setStyle("-fx-background-color: black;");

        // Add background asteroids
        generateBackgroundAsteroids(mainPane);

        loadScores();
        titleText = createText("ASTEROID", centerX, 250, Font.loadFont("file:vediogame2.ttf", 100), null, false);
        playText = createText("P L A Y", centerX, 450, Font.loadFont("file:Super Mario Maker Font.ttf", 50), event -> initGame(stage), true);
        hallText = createText("H A L L  O F  F A M E", centerX, 550, Font.loadFont("file:Super Mario Maker Font.ttf", 50), event -> showHallOfFame(stage), true);
        helpText = createText("C O N T R O L S", centerX, 650, Font.loadFont("file:Super Mario Maker Font.ttf", 50), event -> showControls(stage), true);
        quitText = createText("Q U I T  G A M E", centerX, 750, Font.loadFont("file:Super Mario Maker Font.ttf", 50), event -> {
            if (timer != null) timer.stop();
            stage.close();
        }, true);
        playText.setFill(Color.BLUE);

        mainPane.getChildren().addAll(titleText, playText, hallText, helpText, quitText);

        stage.setTitle("Asteroids");
        stage.setScene(mainScene);
        List<Text> menuOptions = Arrays.asList(playText, hallText, helpText, quitText);
        highlightOption(playText, Arrays.asList(hallText, helpText, quitText));
        currentMainOption = 0;

        mainScene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.DOWN) {
                soundEffects.playKeySound();
                currentMainOption = (currentMainOption + 1) % menuOptions.size();
                List<Text> otherTexts = new ArrayList<>();
                for (Text text : menuOptions) {
                    if (text != menuOptions.get(currentMainOption)) {
                        otherTexts.add(text);
                    }
                }
                highlightOption(menuOptions.get(currentMainOption), otherTexts);
            } else if (event.getCode() == KeyCode.UP) {
                soundEffects.playKeySound();
                currentMainOption = (currentMainOption - 1 + menuOptions.size()) % menuOptions.size();
                List<Text> otherTexts = new ArrayList<>();
                for (Text text : menuOptions) {
                    if (text != menuOptions.get(currentMainOption)) {
                        otherTexts.add(text);
                    }
                }
                highlightOption(menuOptions.get(currentMainOption), otherTexts);
            } else if (event.getCode() == KeyCode.ENTER) {
                soundEffects.playKeySound();
                MouseEvent mouseEvent = new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY, 1, true, true, true, true, true, true, true, true, true, true, null);
                menuOptions.get(currentMainOption).fireEvent(mouseEvent);
            }
        });

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
