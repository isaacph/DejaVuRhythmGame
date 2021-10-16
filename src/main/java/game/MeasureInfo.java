package game;

public interface MeasureInfo {

    void measureStart(Game game, int measureIndex);
    void measureUpdate(Game game);
    void measureEnd(Game game);
}
