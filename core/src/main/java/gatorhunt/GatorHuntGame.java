package gatorhunt;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.Iterator;

/**
 * Shared entry point for both the desktop and Android launchers.
 *
 * Rendering uses LibGDX's default Y-up SpriteBatch (origin bottom-left);
 * every draw call converts from the top-left-origin, Y-down coordinates
 * the original AWT version used (see {@link Alligator#draw}). Input
 * (mouse/touch, unified by LibGDX) stays in Y-down screen coordinates
 * throughout, same as the original, so hit-testing math is untouched.
 */
public class GatorHuntGame extends ApplicationAdapter {

    private static final int MAX_ESCAPED_ALLIGATORS = 20;
    private static final float HUD_MARGIN = 24f;
    private static final float HUD_STAT_GAP = 48f;

    private enum State {
        PLAYING,
        GAME_OVER
    }

    private SpriteBatch batch;
    private BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();

    private Texture backgroundImage;
    private Texture grassImage;
    private Texture alligatorImage;

    private int screenWidth;
    private int screenHeight;

    private GameStats stats;
    private AlligatorSpawner spawner;
    private State state;

    @Override
    public void create() {
        batch = new SpriteBatch();

        backgroundImage = new Texture("sprites/background.jpg");
        grassImage = new Texture("sprites/grass.png");
        alligatorImage = new Texture("sprites/alligator.png");

        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();

        font = createHudFont();

        // Event-driven rather than polling Gdx.input.isTouched() once per frame:
        // polling only sees the touch state at the instant each frame happens to
        // check, so a quick tap can start and end between two polls and never
        // get seen. touchDown() is queued and guaranteed to fire for every touch,
        // regardless of frame timing.
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (state == State.PLAYING) {
                    fireIfOffCooldown(System.nanoTime(), screenX, screenY);
                } else if (state == State.GAME_OVER) {
                    startNewGame();
                }
                return true;
            }
        });

        startNewGame();
    }

    /**
     * A bold display font generated from a TTF at a size proportional to the
     * screen, with a dark outline so the HUD stays readable over the busy
     * swamp background. Replaces LibGDX's default bitmap font, which was
     * small and thin regardless of screen resolution.
     */
    private BitmapFont createHudFont() {
        int fontSize = Math.max(32, screenHeight / 18);

        FreeTypeFontGenerator generator =
                new FreeTypeFontGenerator(Gdx.files.internal("fonts/Bangers-Regular.ttf"));
        try {
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.size = fontSize;
            parameter.color = Color.WHITE;
            parameter.borderWidth = fontSize / 14f;
            parameter.borderColor = Color.BLACK;
            parameter.minFilter = Texture.TextureFilter.Linear;
            parameter.magFilter = Texture.TextureFilter.Linear;
            return generator.generateFont(parameter);
        } finally {
            generator.dispose();
        }
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
        } else if (state == State.GAME_OVER && isRestartKeyPressed()) {
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

    private void fireIfOffCooldown(long now, int touchX, int touchY) {
        if (!stats.canFireShot(now)) {
            return;
        }

        stats.incrementShotsFired();

        Iterator<Alligator> iterator = stats.getAlligators().iterator();
        while (iterator.hasNext()) {
            Alligator alligator = iterator.next();
            if (hits(alligator, touchX, touchY)) {
                stats.incrementKilledCount();
                stats.addScore(alligator.points);
                iterator.remove();
                break;
            }
        }

        stats.setLastShotTime(now);
    }

    /** The alligator's hitbox is its head and body, roughly — not the full sprite bounds. */
    private boolean hits(Alligator alligator, int touchX, int touchY) {
        boolean hitsHead = touchX >= alligator.x + 2 && touchX < alligator.x + 2 + 27
                && touchY >= alligator.y + 30 && touchY < alligator.y + 30 + 30;
        boolean hitsBody = touchX >= alligator.x + 30 && touchX < alligator.x + 30 + 88
                && touchY >= alligator.y + 30 && touchY < alligator.y + 30 + 25;
        return hitsHead || hitsBody;
    }

    private boolean isRestartKeyPressed() {
        return Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
                || Gdx.input.isKeyJustPressed(Input.Keys.ENTER);
    }

    private void draw() {
        batch.draw(backgroundImage, 0, 0, screenWidth, screenHeight);

        for (Alligator alligator : stats.getAlligators()) {
            alligator.draw(batch, screenHeight);
        }

        batch.draw(grassImage, 0, 0, screenWidth, grassImage.getHeight());

        drawHud();

        if (state == State.GAME_OVER) {
            drawCentered("GAME OVER", screenHeight * 0.25f);
            drawCentered("Tap, or press SPACE/ENTER, to try again.", screenHeight * 0.34f);
        }
    }

    private void drawHud() {
        float x = HUD_MARGIN;
        float y = textTopY(HUD_MARGIN);

        x = drawStat(x, y, "ESCAPED: " + stats.getEscapedCount());
        x = drawStat(x, y, "KILLED: " + stats.getKilledCount());
        x = drawStat(x, y, "SHOTS FIRED: " + stats.getShotsFired());
        drawStat(x, y, "SCORE: " + stats.getScore());
    }

    /** Draws one HUD stat at x and returns the x position the next one should start at. */
    private float drawStat(float x, float y, String text) {
        font.draw(batch, text, x, y);
        layout.setText(font, text);
        return x + layout.width + HUD_STAT_GAP;
    }

    private void drawCentered(String text, float distanceFromTop) {
        layout.setText(font, text);
        font.draw(batch, text, (screenWidth - layout.width) / 2f, textTopY(distanceFromTop));
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
    }
}
