package gatorhunt;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

/**
 * Base panel: hides the cursor, tracks the left mouse button, and
 * forwards key/paint events to the subclass.
 */
@SuppressWarnings("serial") // never serialized
public abstract class GamePanel extends JPanel implements KeyListener, MouseListener {

    private static boolean leftMouseButtonDown;

    public GamePanel() {
        this.setFocusable(true);
        this.setBackground(Color.black);

        BufferedImage blankCursorImage = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Cursor blankCursor = Toolkit.getDefaultToolkit()
                .createCustomCursor(blankCursorImage, new Point(0, 0), "blank");
        this.setCursor(blankCursor);

        this.addKeyListener(this);
        this.addMouseListener(this);
    }

    public abstract void render(Graphics2D g2d);

    public abstract void onKeyReleased(KeyEvent e);

    public static boolean isLeftMouseButtonDown() {
        return leftMouseButtonDown;
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        super.paintComponent(g2d); // keeps the black background before the game starts
        render(g2d);
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        onKeyReleased(e);
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    private void setLeftMouseButtonDown(MouseEvent e, boolean down) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            leftMouseButtonDown = down;
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        setLeftMouseButtonDown(e, true);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        setLeftMouseButtonDown(e, false);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
