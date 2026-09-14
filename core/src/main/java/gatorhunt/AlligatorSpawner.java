package gatorhunt;

import com.badlogic.gdx.graphics.Texture;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Spawns alligators into four rows that move at different speeds.
 *
 * Each row gets its own spawn interval, inversely proportional to its
 * speed, so every row ends up with roughly the same number of
 * alligators visible at once. A single shared interval (as the original
 * AWT version used) makes the slow rows pile up: a fast row clears
 * itself almost as quickly as new alligators arrive, but a slow one
 * doesn't, so it keeps accumulating.
 *
 * Kept as per-session instance state (rather than the static globals
 * the original AWT version used) so a restarted game starts clean.
 */
public class AlligatorSpawner {

    // The original AWT version cycled through all 4 rows on one shared
    // 0.5s timer, so any GIVEN row actually only got a new alligator every
    // 4 * 0.5s = 2s -- not every 0.5s, which was a mistake the first time
    // this was tuned (it used the 0.5s round-robin *check* interval as if
    // it were a single row's own interval, making every row spawn 4x more
    // often than before instead of just rebalancing the slow one).
    // The fastest row keeps that real, original 2s pacing; every other
    // row's interval is scaled up by how much slower it is than the
    // fastest one, so it accumulates alligators at the same rate.
    private static final long FASTEST_ROW_INTERVAL_NS = GameStats.NANOSECONDS_PER_SECOND * 2;
    private static final int FASTEST_ROW_SPEED = 5;

    /** One row per entry: {startX, y, speed, points}. Speed is negative — alligators move leftward. */
    private final int[][] spawnRows;
    private final long[] spawnIntervalNs;
    private final long[] lastSpawnTime;

    public AlligatorSpawner(int screenWidth, int screenHeight, long now) {
        // spaced further apart than a plain 1/N split of the screen, since
        // drawn alligators are scaled up noticeably from their sprite's
        // native size (see GatorHuntGame.ALLIGATOR_SCALE)
        spawnRows = new int[][] {
            { screenWidth, (int) (screenHeight * 0.55), -2, 20 },
            { screenWidth, (int) (screenHeight * 0.64), -3, 30 },
            { screenWidth, (int) (screenHeight * 0.73), -4, 40 },
            { screenWidth, (int) (screenHeight * 0.82), -5, 50 },
        };

        spawnIntervalNs = new long[spawnRows.length];
        lastSpawnTime = new long[spawnRows.length];
        for (int i = 0; i < spawnRows.length; i++) {
            int speed = Math.abs(spawnRows[i][2]);
            spawnIntervalNs[i] = FASTEST_ROW_INTERVAL_NS * FASTEST_ROW_SPEED / speed;
            // starts each row's clock at construction time, rather than
            // System.nanoTime()'s arbitrary large epoch, so rows don't all
            // spawn simultaneously on the very first check
            lastSpawnTime[i] = now;
        }
    }

    /** Spawns into every row whose own interval has elapsed since its last spawn — usually none, sometimes one. */
    public List<Alligator> spawnDue(long now, Random random, Texture image, int width, int height) {
        List<Alligator> spawned = new ArrayList<>(1);

        for (int i = 0; i < spawnRows.length; i++) {
            if (now - lastSpawnTime[i] >= spawnIntervalNs[i]) {
                int[] row = spawnRows[i];
                int startX = row[0] + random.nextInt(200);
                spawned.add(new Alligator(startX, row[1], row[2], row[3], width, height, image));
                lastSpawnTime[i] = now;
            }
        }

        return spawned;
    }
}
