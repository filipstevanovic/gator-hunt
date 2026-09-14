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

    /**
     * Alligators are drawn (and hit-tested) at this multiple of the sprite's
     * native size. The sprite is small relative to a modern phone's
     * resolution, and a touch needs a bigger, easier target than a mouse
     * cursor did.
     */
    private static final float ALLIGATOR_SCALE = 1.6f;

    private enum State {
        START,
        PLAYING,
        GAME_OVER
    }

    private SpriteBatch batch;
    private BitmapFont hudFont;
    private BitmapFont titleFont;
    private final GlyphLayout layout = new GlyphLayout();

    private Texture backgroundImage;
    private Texture grassImage;
    private Texture alligatorImage;
    private int alligatorWidth;
    private int alligatorHeight;

    private int screenWidth;
    private int screenHeight;

    private GameStats stats;
    private AlligatorSpawner spawner;
    private State state;
    private DifficultyRamp difficultyRamp;
    private long gameStartTime;
    private long elapsedNs;

    @Override
    public void create() {
        batch = new SpriteBatch();

        backgroundImage = new Texture("sprites/background.jpg");
        grassImage = new Texture("sprites/grass.png");
        alligatorImage = new Texture("sprites/alligator.png");
        alligatorWidth = Math.round(alligatorImage.getWidth() * ALLIGATOR_SCALE);
        alligatorHeight = Math.round(alligatorImage.getHeight() * ALLIGATOR_SCALE);

        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();

        createFonts();

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
                } else {
                    startNewGame();
                }
                return true;
            }
        });

        state = State.START;
    }

    /**
     * Two sizes of a bold display font generated from a TTF, with a dark
     * outline so text stays readable over the busy swamp background.
     * Replaces LibGDX's default bitmap font, which was small and thin
     * regardless of screen resolution.
     */
    private void createFonts() {
        FreeTypeFontGenerator generator =
                new FreeTypeFontGenerator(Gdx.files.internal("fonts/Bangers-Regular.ttf"));
        try {
            hudFont = generateFont(generator, Math.max(32, screenHeight / 18));
            titleFont = generateFont(generator, Math.max(64, screenHeight / 7));
        } finally {
            generator.dispose();
        }
    }

    private BitmapFont generateFont(FreeTypeFontGenerator generator, int size) {
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = size;
        parameter.color = Color.WHITE;
        parameter.borderWidth = size / 14f;
        parameter.borderColor = Color.BLACK;
        parameter.minFilter = Texture.TextureFilter.Linear;
        parameter.magFilter = Texture.TextureFilter.Linear;
        return generator.generateFont(parameter);
    }

    private void startNewGame() {
        stats = new GameStats();
        long now = System.nanoTime();
        spawner = new AlligatorSpawner(screenWidth, screenHeight, now, stats.getRandom());
        difficultyRamp = new DifficultyRamp(now);
        gameStartTime = now;
        elapsedNs = 0;
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
        } else if (isRestartKeyPressed()) {
            startNewGame();
        }

        ScreenUtils.clear(0f, 0f, 0f, 1f);
        batch.begin();
        draw();
        batch.end();
    }

    private void update() {
        long now = System.nanoTime();
        elapsedNs = now - gameStartTime;

        stats.getAlligators().addAll(
                spawner.spawnDue(now, stats.getRandom(), alligatorImage, alligatorWidth, alligatorHeight));

        moveAlligatorsAndRemoveEscaped(difficultyRamp.speedMultiplier(now));

        if (stats.getEscapedCount() >= MAX_ESCAPED_ALLIGATORS) {
            state = State.GAME_OVER;
        }
    }

    private void moveAlligatorsAndRemoveEscaped(float speedMultiplier) {
        Iterator<Alligator> iterator = stats.getAlligators().iterator();
        while (iterator.hasNext()) {
            Alligator alligator = iterator.next();
            alligator.updatePosition(speedMultiplier);

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
            if (alligator.containsPoint(touchX, touchY)) {
                stats.incrementKilledCount();
                stats.addScore(alligator.points);
                iterator.remove();
                break;
            }
        }

        stats.setLastShotTime(now);
    }

    private boolean isRestartKeyPressed() {
        return Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
                || Gdx.input.isKeyJustPressed(Input.Keys.ENTER);
    }

    private void draw() {
        batch.draw(backgroundImage, 0, 0, screenWidth, screenHeight);

        switch (state) {
            case START -> drawStartScreen();
            case PLAYING, GAME_OVER -> drawGameplay();
        }
    }

    private void drawStartScreen() {
        // a few still alligators, just for atmosphere
        drawDecorativeAlligator(0.08f, 0.62f);
        drawDecorativeAlligator(0.74f, 0.70f);
        drawDecorativeAlligator(0.40f, 0.80f);

        drawCentered(titleFont, "GATOR HUNT", screenHeight * 0.30f);
        drawCentered(hudFont, "Tap to start", screenHeight * 0.48f);
    }

    private void drawDecorativeAlligator(float xFraction, float yFraction) {
        float x = screenWidth * xFraction;
        float topY = screenHeight * yFraction;
        batch.draw(alligatorImage, x, screenHeight - topY - alligatorHeight, alligatorWidth, alligatorHeight);
    }

    private void drawGameplay() {
        for (Alligator alligator : stats.getAlligators()) {
            alligator.draw(batch, screenHeight);
        }

        batch.draw(grassImage, 0, 0, screenWidth, grassImage.getHeight());

        drawHud();
        drawElapsedTime();

        if (state == State.GAME_OVER) {
            drawCentered(titleFont, "GAME OVER", screenHeight * 0.25f);
            drawCentered(hudFont, "Tap to try again.", screenHeight * 0.44f);
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
        hudFont.draw(batch, text, x, y);
        layout.setText(hudFont, text);
        return x + layout.width + HUD_STAT_GAP;
    }

    /** Freezes at the final time once the game ends, since update() (which advances elapsedNs) stops running. */
    private void drawElapsedTime() {
        String text = formatElapsedTime(elapsedNs);
        layout.setText(hudFont, text);
        hudFont.draw(batch, text, screenWidth - HUD_MARGIN - layout.width, textTopY(HUD_MARGIN));
    }

    static String formatElapsedTime(long elapsedNs) {
        long totalSeconds = elapsedNs / GameStats.NANOSECONDS_PER_SECOND;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void drawCentered(BitmapFont font, String text, float distanceFromTop) {
        layout.setText(font, text);
        font.draw(batch, text, (screenWidth - layout.width) / 2f, textTopY(distanceFromTop));
    }

    private float textTopY(float distanceFromTop) {
        return screenHeight - distanceFromTop;
    }

    @Override
    public void dispose() {
        batch.dispose();
        hudFont.dispose();
        titleFont.dispose();
        backgroundImage.dispose();
        grassImage.dispose();
        alligatorImage.dispose();
    }
}
