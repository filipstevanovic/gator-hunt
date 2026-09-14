package gatorhunt;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.Iterator;

/**
 * Shared entry point for both the desktop and Android launchers.
 *
 * Rendering uses LibGDX's default Y-up SpriteBatch (origin bottom-left);
 * every draw call converts from the top-left-origin, Y-down coordinates
 * the original AWT version used, via {@link #flipY}. Input (mouse/touch,
 * unified by LibGDX) stays in Y-down screen coordinates throughout, same
 * as the original, so hit-testing math is untouched.
 */
public class GatorHuntGame extends ApplicationAdapter {

    private static final int MAX_ESCAPED_ALLIGATORS = 20;

    private enum State {
        PLAYING,
        GAME_OVER
    }

    private SpriteBatch batch;
    private BitmapFont font;

    private Texture backgroundImage;
    private Texture grassImage;
    private Texture alligatorImage;
    private Texture crosshairImage;
    private int crosshairHalfWidth;
    private int crosshairHalfHeight;

    private int screenWidth;
    private int screenHeight;

    private GameStats stats;
    private AlligatorSpawner spawner;
    private State state;

    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.WHITE);

        backgroundImage = new Texture("sprites/background.jpg");
        grassImage = new Texture("sprites/grass.png");
        alligatorImage = new Texture("sprites/alligator.png");
        crosshairImage = new Texture("sprites/crosshair.png");

        // halved for more precise aiming around the crosshair's center
        crosshairHalfWidth = crosshairImage.getWidth() / 2;
        crosshairHalfHeight = crosshairImage.getHeight() / 2;

        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();

        startNewGame();
    }

    private void startNewGame() {
        stats = new GameStats();
        spawner = new AlligatorSpawner(screenWidth, screenHeight);
        state = State.PLAYING;
    }

    @Override
    public void render() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
            return;
        }

        if (state == State.PLAYING) {
            update();
        } else if (state == State.GAME_OVER && shouldRestart()) {
            startNewGame();
        }

        ScreenUtils.clear(0f, 0f, 0f, 1f);
        batch.begin();
        draw();
        batch.end();
    }

    private void update() {
        long now = System.nanoTime();

        if (spawner.isDue(now)) {
            stats.getAlligators().add(spawner.spawn(now, stats.getRandom(), alligatorImage));
        }

        moveAlligatorsAndRemoveEscaped();

        if (Gdx.input.isTouched()) {
            fireIfOffCooldown(now);
        }

        if (stats.getEscapedCount() >= MAX_ESCAPED_ALLIGATORS) {
            state = State.GAME_OVER;
        }
    }

    private void moveAlligatorsAndRemoveEscaped() {
        Iterator<Alligator> iterator = stats.getAlligators().iterator();
        while (iterator.hasNext()) {
            Alligator alligator = iterator.next();
            alligator.updatePosition();

            if (alligator.hasEscapedOffLeftEdge()) {
                iterator.remove();
                stats.incrementEscapedCount();
            }
        }
    }

    private void fireIfOffCooldown(long now) {
        if (!stats.canFireShot(now)) {
            return;
        }

        stats.incrementShotsFired();

        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.input.getY();

        Iterator<Alligator> iterator = stats.getAlligators().iterator();
        while (iterator.hasNext()) {
            Alligator alligator = iterator.next();
            if (hits(alligator, mouseX, mouseY)) {
                stats.incrementKilledCount();
                stats.addScore(alligator.points);
                iterator.remove();
                break;
            }
        }

        stats.setLastShotTime(now);
    }

    /** The alligator's hitbox is its head and body, roughly — not the full sprite bounds. */
    private boolean hits(Alligator alligator, int mouseX, int mouseY) {
        boolean hitsHead = mouseX >= alligator.x + 2 && mouseX < alligator.x + 2 + 27
                && mouseY >= alligator.y + 30 && mouseY < alligator.y + 30 + 30;
        boolean hitsBody = mouseX >= alligator.x + 30 && mouseX < alligator.x + 30 + 88
                && mouseY >= alligator.y + 30 && mouseY < alligator.y + 30 + 25;
        return hitsHead || hitsBody;
    }

    private boolean shouldRestart() {
        return Gdx.input.justTouched()
                || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
                || Gdx.input.isKeyJustPressed(Input.Keys.ENTER);
    }

    private void draw() {
        batch.draw(backgroundImage, 0, 0, screenWidth, screenHeight);

        for (Alligator alligator : stats.getAlligators()) {
            alligator.draw(batch, screenHeight);
        }

        batch.draw(grassImage, 0, 0, screenWidth, grassImage.getHeight());

        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.input.getY();
        batch.draw(crosshairImage,
                mouseX - crosshairHalfWidth,
                flipY(mouseY - crosshairHalfHeight, crosshairImage.getHeight()));

        font.draw(batch, "ESCAPED: " + stats.getEscapedCount(), 10, textTopY(21));
        font.draw(batch, "KILLED: " + stats.getKilledCount(), 160, textTopY(21));
        font.draw(batch, "SHOTS FIRED: " + stats.getShotsFired(), 300, textTopY(21));
        font.draw(batch, "SCORE: " + stats.getScore(), 530, textTopY(21));

        if (state == State.GAME_OVER) {
            font.draw(batch, "GAME OVER", screenWidth / 2f - 50, textTopY(screenHeight * 0.25f));
            font.draw(batch, "Tap, or press SPACE/ENTER, to try again.",
                    screenWidth / 2f - 250, textTopY(screenHeight * 0.30f));
        }
    }

    /** Converts a top-left-origin, Y-down draw position into LibGDX's bottom-left-origin, Y-up space. */
    private float flipY(float topDownY, float height) {
        return screenHeight - topDownY - height;
    }

    private float textTopY(float distanceFromTop) {
        return screenHeight - distanceFromTop;
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        backgroundImage.dispose();
        grassImage.dispose();
        alligatorImage.dispose();
        crosshairImage.dispose();
    }
}
