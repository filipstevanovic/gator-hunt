package gatorhunt;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AlligatorTest {

    @Test
    void movesLeftByItsSpeedEachUpdate() {
        Alligator alligator = new Alligator(100, 50, -5, 30, null);

        alligator.updatePosition();

        assertEquals(95, alligator.x);
        assertEquals(50, alligator.y);
    }

    @Test
    void keepsThePointsItWasCreatedWith() {
        Alligator alligator = new Alligator(0, 0, -2, 40, null);

        assertEquals(40, alligator.points);
    }
}
