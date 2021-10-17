package game;

public class ActiveNote {

    public Note note;
    public double playTime;
    public NoteStatus status;
    public double statusLastChanged;

    public ActiveNote(Note note, double startTime, double playTime) {
        this.note = note;
        this.playTime = playTime;
        this.status = NoteStatus.READY;
        this.statusLastChanged = startTime;
    }
}
