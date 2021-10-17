package game;

public interface MeasureInfo {

    void measureStart(Game game, double measureStart);
    void measureUpdate(Game game);
    void measureEnd(Game game);
    double getLength();
}
