# Gator Hunt

A small fullscreen arcade game written in Java Swing/AWT: alligators crawl in from the right edge of the screen in four lanes, and you shoot them with the mouse before too many escape off the left edge.

This started as a student project years ago and was later modernized: migrated from a raw Eclipse project to Maven, translated from Serbian to English, refactored for clarity, and given a couple of unit tests around the non-UI game logic.

## How to play

- Move the mouse to aim the crosshair.
- Left-click to shoot. There's a short cooldown between shots.
- Score points for every alligator you hit before 20 of them escape off-screen.
- Press `Space` or `Enter` on the game-over screen to play again, or `Esc` at any time to quit.

## Running it

Requires Java 17+ and Maven.

```bash
mvn package
java -jar target/gatorhunt.jar
```

The game launches fullscreen and undecorated — `Esc` is the way out.

## Project structure

- `Main` — entry point.
- `GameWindow` — the fullscreen `JFrame` that hosts everything.
- `GamePanel` — abstract base panel: hides the cursor, tracks mouse/keyboard input.
- `Engine` — the game loop and state machine (initializing → starting → playing → game over), running at a fixed ~70 updates/second.
- `Gameplay` — one game session: spawning, movement, collision detection, scoring, and drawing.
- `Alligator` — a single alligator's position, speed, points, and sprite.
- `GameStats` — score and counters for the current session.

## Tests

```bash
mvn test
```

Covers the parts that don't need a display: alligator movement and the score/counter bookkeeping in `GameStats`.
