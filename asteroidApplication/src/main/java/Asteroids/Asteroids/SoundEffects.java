package Asteroids;

import javafx.scene.media.AudioClip;

public class SoundEffects {
    private static final AudioClip FIRE_SOUND = new AudioClip(SoundEffects.class.getResource("/fire.wav").toExternalForm());
    private static final AudioClip BANG_SOUND = new AudioClip(SoundEffects.class.getResource("/bang.wav").toExternalForm());
    private static final AudioClip BONUS_SOUND = new AudioClip(SoundEffects.class.getResource("/bonus.mp3").toExternalForm());
    private static final AudioClip JUMP_SOUND = new AudioClip(SoundEffects.class.getResource("/jump.mp3").toExternalForm());
    private static final AudioClip LEVEL_SOUND = new AudioClip(SoundEffects.class.getResource("/level.wav").toExternalForm());
    private static final AudioClip KEY_SOUND = new AudioClip(SoundEffects.class.getResource("/key.wav").toExternalForm());
    private static final AudioClip GAMEOVER_SOUND = new AudioClip(SoundEffects.class.getResource("/gameover.mp3").toExternalForm());

    public void playFireSound() {
        FIRE_SOUND.play();
    }
    public void playBangSound() {
        BANG_SOUND.play();
    }
    public void playBonusSound() {
        BONUS_SOUND.play();
    }
    public void playLevelSound() {
        LEVEL_SOUND.play();
    }
    public void playJumpSound() {
        JUMP_SOUND.play();
    }
    public void playKeySound() {
        KEY_SOUND.play();
    }
    public void playGameOverSound() {
        GAMEOVER_SOUND.play();
    }
}