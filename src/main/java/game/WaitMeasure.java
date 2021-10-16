package game;

public class WaitMeasure implements MeasureInfo {
    private int measureIndex;

    @Override
    public void measureStart(Game game, int measureIndex) {
        this.measureIndex = measureIndex;
    }

    @Override
    public void measureUpdate(Game game) {

    }

    @Override
    public void measureEnd(Game game) {

    }
}
