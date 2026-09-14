package gatorhunt;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Mutable state for one game session: the alligators currently on
 * screen, the score, and shot timing.
 */
public class GameStats {

    private final List<Alligator> alligators = new ArrayList<>();
    private final Random random = new Random();

    private int escapedCount;
    private int killedCount;
    private int score;
    private int shotsFired;
    private long lastShotTime;

    /** A shot can only be fired once this much time has passed since the last one. */
    private final long shotCooldownNs = Engine.NANOSECONDS_PER_SECOND / 3;

    public Random getRandom() {
        return random;
    }

    public List<Alligator> getAlligators() {
        return alligators;
    }

    public int getEscapedCount() {
        return escapedCount;
    }

    public void incrementEscapedCount() {
        escapedCount++;
    }

    public int getKilledCount() {
        return killedCount;
    }

    public void incrementKilledCount() {
        killedCount++;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int points) {
        score += points;
    }

    public int getShotsFired() {
        return shotsFired;
    }

    public void incrementShotsFired() {
        shotsFired++;
    }

    public long getLastShotTime() {
        return lastShotTime;
    }

    public void setLastShotTime(long lastShotTime) {
        this.lastShotTime = lastShotTime;
    }

    public long getShotCooldownNs() {
        return shotCooldownNs;
    }
}
