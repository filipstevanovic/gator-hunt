package gatorhunt;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * A single alligator crossing the screen from right to left.
 *
 * Positions (x, y) are in top-left-origin, Y-down screen space, matching
 * touch/mouse input coordinates and the original AWT version. LibGDX's
 * SpriteBatch is Y-up from the bottom-left by default, so draw() flips
 * Y itself rather than relying on a Y-down camera — BitmapFont's own
 * layout math assumes Y-up, so a Y-down camera would render text
 * upside down while leaving sprites looking correct, which is worse to
 * get subtly wrong than converting coordinates explicitly here.
 */
public class Alligator {

    public int x;
    public int y;
    public final int points;
    private final int speed;
    private final Texture image;

    public Alligator(int x, int y, int speed, int points, Texture image) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.points = points;
        this.image = image;
    }

    public void updatePosition() {
        x += speed;
    }

    public boolean hasEscapedOffLeftEdge() {
        return x < -image.getWidth();
    }

    public void draw(SpriteBatch batch, int screenHeight) {
        batch.draw(image, x, screenHeight - y - image.getHeight());
    }
}
