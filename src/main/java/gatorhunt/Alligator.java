package gatorhunt;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * A single alligator crossing the screen from right to left.
 */
public class Alligator {

    /** Minimum time between two alligator spawns, so rows don't overlap. */
    public static final long SPAWN_INTERVAL_NS = Engine.NANOSECONDS_PER_SECOND / 2;

    /**
     * One row per entry: {startX, y, speed, points}. Speed is negative
     * because alligators move leftward. Computed lazily, the first time
     * this class is touched — by then Engine has already measured the
     * real fullscreen dimensions.
     */
    public static final int[][] SPAWN_ROWS = {
        { Engine.screenWidth, (int) (Engine.screenHeight * 0.60), -2, 20 },
        { Engine.screenWidth, (int) (Engine.screenHeight * 0.65), -3, 30 },
        { Engine.screenWidth, (int) (Engine.screenHeight * 0.70), -4, 40 },
        { Engine.screenWidth, (int) (Engine.screenHeight * 0.78), -5, 50 },
    };

    public static long lastSpawnTime = 0;
    public static int nextSpawnRow = 0;

    public int x;
    public int y;
    public final int points;
    private final int speed;
    private final BufferedImage image;

    public Alligator(int x, int y, int speed, int points, BufferedImage image) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.points = points;
        this.image = image;
    }

    public void updatePosition() {
        x += speed;
    }

    public void draw(Graphics2D g2d) {
        g2d.drawImage(image, x, y, null);
    }
}
