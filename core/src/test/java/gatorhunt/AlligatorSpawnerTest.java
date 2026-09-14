package gatorhunt;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlligatorSpawnerTest {

    @Test
    void spawnsNothingBeforeAnyRowsIntervalHasPassed() {
        AlligatorSpawner spawner = new AlligatorSpawner(1920, 1080, 0L);
        Random random = new Random(42);

        List<Alligator> spawned = spawner.spawnDue(1L, random, null, 140, 80);

        assertTrue(spawned.isEmpty());
    }

    @Test
    void everyRowHasSpawnedAtLeastOnceAfterTheSlowestRowsIntervalPasses() {
        AlligatorSpawner spawner = new AlligatorSpawner(1920, 1080, 0L);
        Random random = new Random(42);

        // the slowest row (speed -2) has the longest interval, 2.5x the
        // fastest row's -- comfortably past that guarantees all 4 are due
        long slowestRowInterval = GameStats.NANOSECONDS_PER_SECOND * 5 / 4;
        List<Alligator> spawned = spawner.spawnDue(slowestRowInterval, random, null, 140, 80);

        assertEquals(4, spawned.size());
    }

    @Test
    void fasterRowsSpawnMoreOftenThanSlowerOnes() {
        AlligatorSpawner spawner = new AlligatorSpawner(1920, 1080, 0L);
        Random random = new Random(42);

        // shortly after construction, only the fastest row (speed -5,
        // interval 0.5s) should be due -- the others need proportionally
        // longer
        long fastestRowInterval = GameStats.NANOSECONDS_PER_SECOND / 2;
        List<Alligator> spawned = spawner.spawnDue(fastestRowInterval, random, null, 140, 80);

        assertEquals(1, spawned.size());
        assertEquals((int) (1080 * 0.82), spawned.get(0).y); // the fastest row's y
    }

    @Test
    void spawnsAlligatorsStartingOffTheRightEdgeOfTheScreen() {
        AlligatorSpawner spawner = new AlligatorSpawner(1920, 1080, 0L);
        Random random = new Random(42);

        List<Alligator> spawned = spawner.spawnDue(GameStats.NANOSECONDS_PER_SECOND, random, null, 140, 80);

        assertTrue(spawned.stream().allMatch(a -> a.x >= 1920));
    }

    @Test
    void spawnedAlligatorHasTheGivenSize() {
        AlligatorSpawner spawner = new AlligatorSpawner(1920, 1080, 0L);
        Random random = new Random(42);

        List<Alligator> spawned = spawner.spawnDue(GameStats.NANOSECONDS_PER_SECOND, random, null, 224, 128);

        assertTrue(spawned.stream().allMatch(a -> a.width == 224 && a.height == 128));
    }
}
