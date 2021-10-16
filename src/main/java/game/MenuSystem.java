package game;

public class MenuSystem implements GameSystem {

    private final Game game;
    private Runnable finishFunc;

    public MenuSystem(Game game) {
        this.game = game;
    }

    @Override
    public void init() {
        finishFunc.run();
    }

    @Override
    public void setFinishedCallback(Runnable runnable) {
        this.finishFunc = runnable;
    }

    @Override
    public void update() {

    }

    @Override
    public void render() {
    }
}
