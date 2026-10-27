import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Playable first prototype: a pseudo-3D corridor, glass targets, and limited
 * throws. The renderer intentionally uses basic Java2D so it has no libraries
 * to install and can later be replaced by a renderer suitable for J2ME.
 */
public final class Game extends JPanel implements ActionListener, MouseListener, KeyListener {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int STARTING_BALLS = 25;

    private final ArrayList<Obstacle> obstacles = new ArrayList<Obstacle>();
    private final Random random = new Random();
    private final Timer timer = new Timer(16, this);

    private int balls = STARTING_BALLS;
    private int score = 0;
    private int ticks = 0;
    private int spawnDelay = 65;
    private boolean gameOver = false;
    private String message = "CLICK GLASS TO THROW";
    private Rectangle2D.Double lastTarget = null;

    public Game() {
        setPreferredSize(new java.awt.Dimension(WIDTH, HEIGHT));
        setBackground(new Color(7, 12, 25));
        setFocusable(true);
        addMouseListener(this);
        addKeyListener(this);
        spawnObstacle();
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (!gameOver) {
            ticks++;

            if (ticks % spawnDelay == 0) {
                spawnObstacle();
            }

            Iterator<Obstacle> iterator = obstacles.iterator();
            while (iterator.hasNext()) {
                Obstacle obstacle = iterator.next();
                obstacle.z -= 0.0045;
                if (obstacle.z <= 0.07) {
                    iterator.remove();
                    score = Math.max(0, score - 1);
                    message = "MISSED! WATCH THE GLASS";
                }
            }

            if (balls <= 0 && obstacles.isEmpty()) {
                gameOver = true;
                message = "OUT OF BALLS - PRESS R TO RESTART";
            }
        }
        repaint();
    }

    private void spawnObstacle() {
        double x = -0.58 + random.nextDouble() * 1.16;
        double y = -0.36 + random.nextDouble() * 0.72;
        obstacles.add(new Obstacle(x, y, 0.98));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawBackground(g);
        drawCorridor(g);

        for (Obstacle obstacle : obstacles) {
            drawObstacle(g, obstacle);
        }

        drawHud(g);
        g.dispose();
    }

    private void drawBackground(Graphics2D g) {
        g.setPaint(new java.awt.GradientPaint(0, 0, new Color(16, 29, 55),
                0, HEIGHT, new Color(2, 4, 12)));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // Soft light at the distant end of the corridor.
        g.setColor(new Color(74, 170, 255, 28));
        g.fillOval(WIDTH / 2 - 115, HEIGHT / 2 - 135, 230, 230);
    }

    private void drawCorridor(Graphics2D g) {
        int cx = WIDTH / 2;
        int horizon = HEIGHT / 2 - 15;
        int farW = 64;
        int farH = 45;
        int nearW = WIDTH - 70;
        int nearH = HEIGHT - 105;

        g.setStroke(new BasicStroke(2f));
        g.setColor(new Color(63, 145, 205, 110));
        g.drawRect(cx - farW / 2, horizon - farH / 2, farW, farH);

        int[][] farCorners = {
            {cx - farW / 2, horizon - farH / 2},
            {cx + farW / 2, horizon - farH / 2},
            {cx + farW / 2, horizon + farH / 2},
            {cx - farW / 2, horizon + farH / 2}
        };
        int[][] nearCorners = {
            {cx - nearW / 2, horizon - nearH / 2},
            {cx + nearW / 2, horizon - nearH / 2},
            {cx + nearW / 2, horizon + nearH / 2},
            {cx - nearW / 2, horizon + nearH / 2}
        };

        for (int i = 0; i < 4; i++) {
            g.drawLine(farCorners[i][0], farCorners[i][1],
                    nearCorners[i][0], nearCorners[i][1]);
        }

        // Perspective floor and ceiling ribs.
        for (int i = 1; i <= 7; i++) {
            double t = i / 8.0;
            int x1 = (int) (cx - farW / 2.0 - (nearW - farW) * t / 2.0);
            int x2 = (int) (cx + farW / 2.0 + (nearW - farW) * t / 2.0);
            int y = (int) (horizon + (nearH / 2.0 - farH / 2.0) * t);
            g.setColor(new Color(45, 100, 150, 45));
            g.drawLine(x1, y, x2, y);
            g.drawLine(x1, 2 * horizon - y, x2, 2 * horizon - y);
        }
    }

    private void drawObstacle(Graphics2D g, Obstacle obstacle) {
        double scale = 1.12 - obstacle.z;
        int cx = WIDTH / 2 + (int) (obstacle.x * WIDTH * 0.46 * scale);
        int cy = HEIGHT / 2 + (int) (obstacle.y * HEIGHT * 0.42 * scale);
        int size = Math.max(7, (int) (112 * scale));
        int x = cx - size / 2;
        int y = cy - size / 2;

        g.setColor(new Color(40, 220, 255, 28));
        g.fillRect(x - 5, y - 5, size + 10, size + 10);
        g.setColor(new Color(88, 220, 255, 65));
        g.fillRect(x, y, size, size);
        g.setColor(new Color(185, 248, 255, 210));
        g.setStroke(new BasicStroke(Math.max(1f, (float) (2.5 * scale))));
        g.drawRect(x, y, size, size);
        g.setColor(new Color(255, 255, 255, 100));
        g.drawLine(x + size / 5, y + size / 5, x + size * 4 / 5, y + size / 5);
        g.drawLine(x + size / 5, y + size / 5, x + size / 5, y + size * 4 / 5);

        obstacle.screenBounds = new Rectangle2D.Double(x - 5, y - 5, size + 10, size + 10);
    }

    private void drawHud(Graphics2D g) {
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 18));
        g.setColor(Color.WHITE);
        g.drawString("EXPERT PANCAKE  //  PROTOTYPE", 22, 34);
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        g.drawString("SCORE: " + score, 22, 62);
        g.drawString("BALLS: " + balls, 22, 86);

        g.setColor(new Color(120, 210, 255));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(message, (WIDTH - fm.stringWidth(message)) / 2, HEIGHT - 28);

        g.setColor(new Color(255, 255, 255, 75));
        g.drawLine(0, HEIGHT - 50, WIDTH, HEIGHT - 50);

        if (gameOver) {
            g.setColor(new Color(0, 0, 0, 175));
            g.fillRect(0, 0, WIDTH, HEIGHT);
            g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 30));
            g.setColor(Color.WHITE);
            String title = "RUN FINISHED";
            fm = g.getFontMetrics();
            g.drawString(title, (WIDTH - fm.stringWidth(title)) / 2, HEIGHT / 2 - 20);
            g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 17));
            String subtitle = "FINAL SCORE: " + score + "  -  PRESS R";
            fm = g.getFontMetrics();
            g.drawString(subtitle, (WIDTH - fm.stringWidth(subtitle)) / 2, HEIGHT / 2 + 20);
        }
    }

    private void throwBall(int mouseX, int mouseY) {
        if (gameOver || balls <= 0) {
            return;
        }

        balls--;
        for (int i = obstacles.size() - 1; i >= 0; i--) {
            Obstacle obstacle = obstacles.get(i);
            if (obstacle.screenBounds != null && obstacle.screenBounds.contains(mouseX, mouseY)) {
                obstacles.remove(i);
                score += 10;
                message = "GLASS SMASHED! +10";
                return;
            }
        }
        message = "MISSED THROW";
    }

    private void restart() {
        balls = STARTING_BALLS;
        score = 0;
        ticks = 0;
        gameOver = false;
        message = "CLICK GLASS TO THROW";
        obstacles.clear();
        spawnObstacle();
        requestFocusInWindow();
    }

    @Override
    public void mouseClicked(MouseEvent event) {
        if (event.getButton() == MouseEvent.BUTTON1) {
            throwBall(event.getX(), event.getY());
        }
    }

    @Override
    public void keyPressed(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_R) {
            restart();
        } else if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
            System.exit(0);
        }
    }

    @Override public void mousePressed(MouseEvent event) { }
    @Override public void mouseReleased(MouseEvent event) { }
    @Override public void mouseEntered(MouseEvent event) { }
    @Override public void mouseExited(MouseEvent event) { }
    @Override public void keyTyped(KeyEvent event) { }
    @Override public void keyReleased(KeyEvent event) { }

    private static final class Obstacle {
        final double x;
        final double y;
        double z;
        Rectangle2D.Double screenBounds;

        Obstacle(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}
