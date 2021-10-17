package game;

import static org.lwjgl.glfw.GLFW.*;

public class WaitMeasure implements MeasureInfo {

    public String text;

    public WaitMeasure(Game game, String text) {
        this.text = game.smallFont.cutOffStringBasedOnSize(text, 112 / 320.0f * game.gameScreenSize.x);
    }

    @Override
    public void measureStart(Game game, double measureStart) {
        game.musicPlayer.topText = this.text;
        game.musicPlayer.waiting = true;
        game.musicPlayer.currentMeasure = measureStart;
    }

    @Override
    public void measureUpdate(Game game) {
        if(glfwGetKey(game.window, GLFW_KEY_SPACE) == GLFW_PRESS) {
            game.musicPlayer.waiting = false;
        }
    }

    @Override
    public void measurePreUpdate(Game game, double measureStart) {

    }

    @Override
    public void measureEnd(Game game) {
        game.musicPlayer.topText = "";
    }

    @Override
    public double getLength() {
        return 0.0f;
    }

    @Override
    public double getPreUpdateRequirement() {
        return 0;
    }
}
