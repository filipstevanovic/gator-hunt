package gatorhunt;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlligatorSpawnerTest {

    @Test
    void isNotDueBeforeSpawnIntervalHasPassed() {
        AlligatorSpawner spawner = new AlligatorSpawner(1920, 1080);
        Random random = new Random(42);

        spawner.spawn(1_000_000_000L, random, null);

        assertFalse(spawner.isDue(1_000_000_000L + 1));
        assertTrue(spawner.isDue(1_000_000_000L + GameStats.NANOSECONDS_PER_SECOND / 2));
    }

    @Test
    void spawnsAlligatorsStartingOffTheRightEdgeOfTheScreen() {
        AlligatorSpawner spawner = new AlligatorSpawner(1920, 1080);
        Random random = new Random(42);

        Alligator alligator = spawner.spawn(0L, random, null);

        assertTrue(alligator.x >= 1920);
    }

    @Test
    void cyclesThroughAllFourRowsBeforeRepeating() {
        AlligatorSpawner spawner = new AlligatorSpawner(1920, 1080);
        Random random = new Random(42);

        int firstRowY = spawner.spawn(0L, random, null).y;
        spawner.spawn(1L, random, null);
        spawner.spawn(2L, random, null);
        spawner.spawn(3L, random, null);
        int fifthRowY = spawner.spawn(4L, random, null).y;

        assertEquals(firstRowY, fifthRowY);
    }
}
