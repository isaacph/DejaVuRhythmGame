package game;

public class Note implements Comparable<Note> {

    // visual position on screen
    public final int position;

    // assume 4/4 measure, 1 value = one quarter note
    // so 0.0f is a note at the start
    // so 3.0f is the 4th note
    public final float timeInMeasure;

    public Note(float timeInMeasure, int position) {
        this.timeInMeasure = timeInMeasure;
        this.position = position;
    }

    @Override
    public int compareTo(Note o) {
        return Float.compare(timeInMeasure, o.timeInMeasure);
    }
}
