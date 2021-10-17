package game;

import static org.lwjgl.glfw.GLFW.*;

public class CountingMeasure implements MeasureInfo {

    public String text;
    public double length;

    public CountingMeasure(Game game, String text, double length) {
        this.text = game.smallFont.cutOffStringBasedOnSize(text, 112 / 320.0f * game.screenSize.x);
        this.length = length;
    }

    @Override
    public void measureStart(Game game, double measureStart) {
        game.musicPlayer.topText = this.text;
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
