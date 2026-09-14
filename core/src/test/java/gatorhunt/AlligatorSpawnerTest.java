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

        spawner.spawn(1_000_000_000L, random, null, 140, 80);

        assertFalse(spawner.isDue(1_000_000_000L + 1));
        assertTrue(spawner.isDue(1_000_000_000L + GameStats.NANOSECONDS_PER_SECOND / 2));
    }

    @Test
    void spawnsAlligatorsStartingOffTheRightEdgeOfTheScreen() {
        AlligatorSpawner spawner = new AlligatorSpawner(1920, 1080);
        Random random = new Random(42);

        Alligator alligator = spawner.spawn(0L, random, null, 140, 80);

        assertTrue(alligator.x >= 1920);
    }

    @Test
    void cyclesThroughAllFourRowsBeforeRepeating() {
        AlligatorSpawner spawner = new AlligatorSpawner(1920, 1080);
        Random random = new Random(42);

        int firstRowY = spawner.spawn(0L, random, null, 140, 80).y;
        spawner.spawn(1L, random, null, 140, 80);
        spawner.spawn(2L, random, null, 140, 80);
        spawner.spawn(3L, random, null, 140, 80);
        int fifthRowY = spawner.spawn(4L, random, null, 140, 80).y;

        assertEquals(firstRowY, fifthRowY);
    }

    @Test
    void spawnedAlligatorHasTheGivenSize() {
        AlligatorSpawner spawner = new AlligatorSpawner(1920, 1080);
        Random random = new Random(42);

        Alligator alligator = spawner.spawn(0L, random, null, 224, 128);

        assertEquals(224, alligator.width);
        assertEquals(128, alligator.height);
    }
}
