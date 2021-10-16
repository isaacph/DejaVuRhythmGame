package game;

import java.util.function.Consumer;

public interface GameSystem {

    void setFinishedCallback(Consumer<State> onFinish);
    void init();
    void update();
    void render();
}
