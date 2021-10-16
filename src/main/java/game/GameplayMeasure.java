package game;

import java.util.ArrayList;

public class GameplayMeasure implements MeasureInfo {
    public ArrayList<Note> noteInfo = new ArrayList<>();
    public ArrayList<String> startingSounds = new ArrayList<>();
    public int noteInfoPos = 0;
    public int measureIndex = 0;

    public GameplayMeasure() {}

    @Override
    public void measureStart(Game game, int measureIndex) {
        this.measureIndex = measureIndex;
        this.noteInfoPos = 0;
        for(String name : startingSounds) {
            game.soundPlayer.play(game.musicPlayer.sounds.get(name), (float) (4.0 / game.musicPlayer.bpm * 60));
        }
    }

    @Override
    public void measureUpdate(Game game) {
        boolean added = true;
        while(added && noteInfoPos < noteInfo.size()) {
            added = false;
            Note note = noteInfo.get(noteInfoPos);
            if(game.musicPlayer.currentMeasure >= measureIndex + note.timeInMeasure / 4.0f) {
                game.musicPlayer.activeNotes.add(note);
                game.musicPlayer.playMusicTime.put(note, measureIndex + note.timeInMeasure / 4.0f + 1);
                System.out.println(measureIndex + note.timeInMeasure / 4.0f + 1);
                noteInfoPos++;
                added = true;
            }
        }
    }

    @Override
    public void measureEnd(Game game) {

    }
}
