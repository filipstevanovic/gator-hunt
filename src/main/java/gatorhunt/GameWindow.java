package gatorhunt;

import java.awt.Frame;

import javax.swing.JFrame;

/**
 * The undecorated, fullscreen window that hosts the game.
 */
@SuppressWarnings("serial") // never serialized
public class GameWindow extends JFrame {

    public GameWindow() {
        this.setUndecorated(true);
        this.setExtendedState(Frame.MAXIMIZED_BOTH);
        this.setContentPane(new Engine());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }
}
