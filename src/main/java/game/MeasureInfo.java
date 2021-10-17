package game;

public interface MeasureInfo {

    void measureStart(Game game, double measureStart);
    void measureUpdate(Game game);
    void measurePreUpdate(Game game, double measureStart);
    void measureEnd(Game game);
    double getLength();
    double getPreUpdateRequirement();
}
