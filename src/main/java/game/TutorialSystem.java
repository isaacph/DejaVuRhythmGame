package game;

public class TutorialSystem implements GameSystem {

    private final Game game;
    private Runnable finishFunc;
    private MusicPlayer musicPlayer;

    public TutorialSystem(Game game) {
        this.game = game;
        this.musicPlayer = new MusicPlayer(game);
    }

    @Override
    public void init() {
    }

    @Override
    public void setFinishedCallback(Runnable runnable) {
        this.finishFunc = runnable;
    }

    @Override
    public void update() {
        musicPlayer.update();
    }

    @Override
    public void render() {
        musicPlayer.render();
    }
}
