package game;

import static org.lwjgl.glfw.GLFW.*;

public class CountingMeasure implements MeasureInfo {

    public String text;
    public double length;
    public SoundHandle sound;

    public CountingMeasure(Game game, String text, double length, SoundHandle sound) {
        this.text = game.smallFont.cutOffStringBasedOnSize(text, 112 / 320.0f * game.screenSize.x);
        this.length = length;
        this.sound = sound;
    }

    public CountingMeasure(Game game, String text, double length) {
        this(game, text, length, null);
    }

    @Override
    public void measureStart(Game game, double measureStart) {
        game.musicPlayer.topText = this.text;
        if(sound != null) {
            game.soundPlayer.play(sound, 1);
        }
    }

    @Override
    public void measureUpdate(Game game) {
    }

    @Override
    public void measureEnd(Game game) {
        game.musicPlayer.topText = "";
    }

    @Override
    public double getLength() {
        return length;
    }
}
