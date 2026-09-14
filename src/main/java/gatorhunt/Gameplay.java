package gatorhunt;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Iterator;

import javax.imageio.ImageIO;

/**
 * One game session: spawns alligators, moves them, handles shooting,
 * tracks the escape count, and draws every frame.
 */
public class Gameplay {

    private static final int MAX_ESCAPED_ALLIGATORS = 20;

    private final Font font = new Font("monospaced", Font.BOLD, 18);
    private final GameStats stats = new GameStats();

    private BufferedImage backgroundImage;
    private BufferedImage grassImage;
    private BufferedImage alligatorImage;
    private BufferedImage crosshairImage;
    private int crosshairHalfWidth;
    private int crosshairHalfHeight;

    public Gameplay() {
        loadAssets();
        Engine.state = Engine.GameState.PLAYING;
    }

    private void loadAssets() {
        try {
            backgroundImage = readSprite("/sprites/background.jpg");
            grassImage = readSprite("/sprites/grass.png");
            alligatorImage = readSprite("/sprites/alligator.png");
            crosshairImage = readSprite("/sprites/crosshair.png");

            // halved for more precise aiming around the crosshair's center
            crosshairHalfWidth = crosshairImage.getWidth() / 2;
            crosshairHalfHeight = crosshairImage.getHeight() / 2;
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to load game sprites", ex);
        }
    }

    private BufferedImage readSprite(String resourcePath) throws IOException {
        URL url = this.getClass().getResource(resourcePath);
        if (url == null) {
            throw new IOException("Missing sprite resource: " + resourcePath);
        }
        return ImageIO.read(url);
    }

    /** Called every frame from Engine's game loop while the game is in progress. */
    public void update(Point mousePosition) {
        spawnAlligatorIfDue();
        moveAlligatorsAndRemoveEscaped();

        if (GamePanel.isLeftMouseButtonDown()) {
            fireIfOffCooldown(mousePosition);
        }

        if (stats.getEscapedCount() >= MAX_ESCAPED_ALLIGATORS) {
            Engine.state = Engine.GameState.GAME_OVER;
        }
    }

    private void spawnAlligatorIfDue() {
        // avoids overlapping alligators in the same row
        if (System.nanoTime() - Alligator.lastSpawnTime < Alligator.SPAWN_INTERVAL_NS) {
            return;
        }

        int[] row = Alligator.SPAWN_ROWS[Alligator.nextSpawnRow];
        int startX = row[0] + stats.getRandom().nextInt(200);
        stats.getAlligators().add(new Alligator(startX, row[1], row[2], row[3], alligatorImage));

        Alligator.nextSpawnRow = (Alligator.nextSpawnRow + 1) % Alligator.SPAWN_ROWS.length;
        Alligator.lastSpawnTime = System.nanoTime();
    }

    private void moveAlligatorsAndRemoveEscaped() {
        Iterator<Alligator> iterator = stats.getAlligators().iterator();
        while (iterator.hasNext()) {
            Alligator alligator = iterator.next();
            alligator.updatePosition();

            if (alligator.x < -alligatorImage.getWidth()) {
                iterator.remove();
                stats.incrementEscapedCount();
            }
        }
    }

    private void fireIfOffCooldown(Point mousePosition) {
        if (System.nanoTime() - stats.getLastShotTime() < stats.getShotCooldownNs()) {
            return;
        }

        stats.incrementShotsFired();

        Iterator<Alligator> iterator = stats.getAlligators().iterator();
        while (iterator.hasNext()) {
            Alligator alligator = iterator.next();
            if (hits(alligator, mousePosition)) {
                stats.incrementKilledCount();
                stats.addScore(alligator.points);
                iterator.remove();
                break;
            }
        }

        stats.setLastShotTime(System.nanoTime());
    }

    /** The alligator's hitbox is its head and body, roughly — not the full sprite bounds. */
    private boolean hits(Alligator alligator, Point mousePosition) {
        Rectangle head = new Rectangle(alligator.x + 2, alligator.y + 30, 27, 30);
        Rectangle body = new Rectangle(alligator.x + 30, alligator.y + 30, 88, 25);
        return head.contains(mousePosition) || body.contains(mousePosition);
    }

    public void draw(Graphics2D g2d, Point mousePosition) {
        g2d.drawImage(backgroundImage, 0, 0, Engine.screenWidth, Engine.screenHeight, null);

        for (Alligator alligator : stats.getAlligators()) {
            alligator.draw(g2d);
        }

        g2d.drawImage(grassImage, 0, Engine.screenHeight - grassImage.getHeight(),
                Engine.screenWidth, grassImage.getHeight(), null);

        g2d.drawImage(crosshairImage, mousePosition.x - crosshairHalfWidth,
                mousePosition.y - crosshairHalfHeight, null);

        g2d.setFont(font);
        g2d.setColor(Color.white);
        g2d.drawString("ESCAPED: " + stats.getEscapedCount(), 10, 21);
        g2d.drawString("KILLED: " + stats.getKilledCount(), 160, 21);
        g2d.drawString("SHOTS FIRED: " + stats.getShotsFired(), 300, 21);
        g2d.drawString("SCORE: " + stats.getScore(), 530, 21);
    }

    public void drawGameOver(Graphics2D g2d, Point mousePosition) {
        draw(g2d, mousePosition); // keeps the last frame of play visible underneath

        g2d.drawString("GAME OVER", Engine.screenWidth / 2 - 50,
                (int) (Engine.screenHeight * 0.25) + 1);
        g2d.drawString("Press SPACE or ENTER to try again.",
                Engine.screenWidth / 2 - 250,
                (int) (Engine.screenHeight * 0.30) + 1);
    }
}
