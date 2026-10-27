import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Entry point for Expert Pancake.
 * A small, original Java prototype inspired by first-person arcade games.
 */
public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame window = new JFrame("Expert Pancake");
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setResizable(false);
            window.setContentPane(new Game());
            window.pack();
            window.setLocationRelativeTo(null);
            window.setVisible(true);
        });
    }
}
