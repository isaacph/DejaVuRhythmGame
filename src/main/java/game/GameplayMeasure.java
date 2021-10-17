package game;

import java.util.ArrayList;

public class GameplayMeasure implements MeasureInfo {
    public ArrayList<Note> noteInfo = new ArrayList<>();
    public ArrayList<String> startingSounds = new ArrayList<>();
    public int noteInfoPos = 0;
    public double measureStartTime = 0;
    public double preUpdateRequirement = 0;

    public GameplayMeasure() {}

    public GameplayMeasure(double preUpdateRequirement) {
        this.preUpdateRequirement = preUpdateRequirement;
    }

    @Override
    public void measureStart(Game game, double measureStart) {
        System.out.println("Start measure " + measureStart);
        this.measureStartTime = measureStart;
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
            if(game.musicPlayer.currentMeasure >= measureStartTime + note.timeInMeasure / 4.0f) {
                game.musicPlayer.activeNotes.add(
                    new ActiveNote(
                        note,
                        game.musicPlayer.currentMeasure,
                        measureStartTime + note.timeInMeasure / 4.0 + note.hitTime));
                System.out.println((float) measureStartTime + note.timeInMeasure / 4.0f + note.hitTime);
                noteInfoPos++;
                added = true;
            }
        }
    }

    @Override
    public void measurePreUpdate(Game game, double measureStart) {
        this.measureStartTime = measureStart;
        this.measureUpdate(game);
    }

    @Override
    public void measureEnd(Game game) {

    }

    @Override
    public double getLength() {
        return 1;
    }

    @Override
    public double getPreUpdateRequirement() {
        return preUpdateRequirement;
    }
}
