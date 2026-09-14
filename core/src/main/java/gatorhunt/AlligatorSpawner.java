package gatorhunt;

import com.badlogic.gdx.graphics.Texture;

import java.util.Random;

/**
 * Spawns alligators into one of four rows, spaced out in time so rows
 * don't overlap. Kept as per-session instance state (rather than the
 * static globals the original AWT version used) so a restarted game
 * starts clean.
 */
public class AlligatorSpawner {

    private static final long SPAWN_INTERVAL_NS = GameStats.NANOSECONDS_PER_SECOND / 2;

    /** One row per entry: {startX, y, speed, points}. Speed is negative — alligators move leftward. */
    private final int[][] spawnRows;

    private long lastSpawnTime = 0;
    private int nextRow = 0;

    public AlligatorSpawner(int screenWidth, int screenHeight) {
        // spaced further apart than a plain 1/N split of the screen, since
        // drawn alligators are scaled up noticeably from their sprite's
        // native size (see GatorHuntGame.ALLIGATOR_SCALE)
        spawnRows = new int[][] {
            { screenWidth, (int) (screenHeight * 0.55), -2, 20 },
            { screenWidth, (int) (screenHeight * 0.64), -3, 30 },
            { screenWidth, (int) (screenHeight * 0.73), -4, 40 },
            { screenWidth, (int) (screenHeight * 0.82), -5, 50 },
        };
    }

    public boolean isDue(long now) {
        return now - lastSpawnTime >= SPAWN_INTERVAL_NS;
    }

    public Alligator spawn(long now, Random random, Texture image, int width, int height) {
        int[] row = spawnRows[nextRow];
        int startX = row[0] + random.nextInt(200);

        Alligator alligator = new Alligator(startX, row[1], row[2], row[3], width, height, image);

        nextRow = (nextRow + 1) % spawnRows.length;
        lastSpawnTime = now;
        return alligator;
    }
}
