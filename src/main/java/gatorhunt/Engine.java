package gatorhunt;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;

/**
 * Drives the game loop: measures the real screen size once the panel
 * is on screen, then runs an update/render cycle at a fixed rate.
 */
@SuppressWarnings("serial") // never serialized
public class Engine extends GamePanel {

    public static final long NANOSECONDS_PER_SECOND = 1_000_000_000L;
    public static final long MICROSECONDS_PER_SECOND = 1_000_000L;

    private static final int TARGET_FPS = 70;
    private static final long UPDATE_INTERVAL_NS = NANOSECONDS_PER_SECOND / TARGET_FPS;
    private static final long MIN_SLEEP_MS = 10;

    public static int screenWidth;
    public static int screenHeight;

    public enum GameState {
        INITIALIZING,
        STARTING,
        PLAYING,
        GAME_OVER
    }

    public static GameState state;

    private Gameplay gameplay;

    public Engine() {
        super();
        state = GameState.INITIALIZING;

        Thread gameThread = new Thread(this::gameLoop, "gatorhunt-game-loop");
        gameThread.start();
    }

    /** Updates game logic and repaints at UPDATE_INTERVAL_NS, roughly TARGET_FPS times a second. */
    private void gameLoop() {
        // used during INITIALIZING to wait until the panel has reported a stable size
        long initializingElapsedNs = 0;
        long lastInitializingCheck = System.nanoTime();

        while (true) {
            long loopStart = System.nanoTime();

            switch (state) {
                case PLAYING -> gameplay.update(mousePosition());
                case STARTING -> startNewGame();
                case INITIALIZING -> {
                    // getWidth() can report 0 before the panel is actually laid out,
                    // so also wait at least a second before trusting the measurement
                    if (this.getWidth() > 1 && initializingElapsedNs > NANOSECONDS_PER_SECOND) {
                        screenWidth = this.getWidth();
                        screenHeight = this.getHeight();
                        state = GameState.STARTING;
                    } else {
                        initializingElapsedNs += System.nanoTime() - lastInitializingCheck;
                        lastInitializingCheck = System.nanoTime();
                    }
                }
                default -> {
                }
            }

            repaint();

            long loopDurationNs = System.nanoTime() - loopStart;
            long sleepMs = Math.max(MIN_SLEEP_MS,
                    (UPDATE_INTERVAL_NS - loopDurationNs) / MICROSECONDS_PER_SECOND);

            try {
                Thread.sleep(sleepMs);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    @Override
    public void render(Graphics2D g2d) {
        switch (state) {
            case PLAYING -> gameplay.draw(g2d, mousePosition());
            case GAME_OVER -> gameplay.drawGameOver(g2d, mousePosition());
            default -> {
            }
        }
    }

    private void startNewGame() {
        gameplay = new Gameplay();
    }

    private void restartGame() {
        gameplay = new Gameplay();
        state = GameState.PLAYING;
    }

    private Point mousePosition() {
        Point mp = this.getMousePosition();
        return mp != null ? mp : new Point(0, 0);
    }

    @Override
    public void onKeyReleased(KeyEvent e) {
        switch (state) {
            case GAME_OVER -> {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    System.exit(0);
                } else if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER) {
                    restartGame();
                }
            }
            case PLAYING -> {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    System.exit(0);
                }
            }
            default -> {
            }
        }
    }
}
