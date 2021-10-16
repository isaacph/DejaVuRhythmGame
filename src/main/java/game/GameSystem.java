package game;

public interface GameSystem {

    void setFinishedCallback(Runnable runnable);
    void init();
    void update();
    void render();
}
