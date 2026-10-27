import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Expert Pancake's first 3D-world prototype.
 *
 * This uses real 3D world coordinates and perspective projection, rendered in
 * software with Java2D so it has no external dependencies. Projectile motion
 * is simulated in 3D with velocity, gravity, and target collisions.
 */
public final class Game extends JPanel implements ActionListener, MouseListener,
        MouseMotionListener, KeyListener {
    private static final int WIDTH = 960;
    private static final int HEIGHT = 640;
    private static final double FOCAL_LENGTH = 520.0;
    private static final double NEAR_CLIP = 0.35;
    private static final double GRAVITY = 9.8;
    private static final int STARTING_BALLS = 35;

    private final ArrayList<Glass> glasses = new ArrayList<Glass>();
    private final ArrayList<Pyramid> pyramids = new ArrayList<Pyramid>();
    private final ArrayList<Ball> ballsInFlight = new ArrayList<Ball>();
    private final ArrayList<Particle> particles = new ArrayList<Particle>();
    private final Random random = new Random();
    private final Timer timer = new Timer(16, this);

    private long previousNanos = System.nanoTime();
    private double worldSpeed = 2.6;
    private int balls = STARTING_BALLS;
    private int score = 0;
    private int broken = 0;
    private int spawnCounter = 0;
    private String message = "CLICK ANYWHERE: THROW  |  R: RESTART";
    private boolean gameOver = false;
    private int mouseX = WIDTH / 2;
    private int mouseY = HEIGHT / 2;

    public Game() {
        setPreferredSize(new java.awt.Dimension(WIDTH, HEIGHT));
        setBackground(new Color(4, 8, 18));
        setFocusable(true);
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
        spawnGlass(15.0);
        spawnPyramid(11.0);
        spawnPyramid(21.0);
        spawnPyramid(30.0);
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        long now = System.nanoTime();
        double dt = Math.min(0.04, (now - previousNanos) / 1_000_000_000.0);
        previousNanos = now;

        if (!gameOver) {
            updateWorld(dt);
        }
        repaint();
    }

    private void updateWorld(double dt) {
        spawnCounter++;
        if (spawnCounter >= 115) {
            spawnCounter = 0;
            if (random.nextDouble() < 0.72) {
                spawnPyramid(27.0 + random.nextDouble() * 8.0);
            } else {
                spawnGlass(27.0 + random.nextDouble() * 8.0);
            }
        }

        worldSpeed = Math.min(5.0, 2.6 + broken * 0.025);

        Iterator<Pyramid> pyramidIterator = pyramids.iterator();
        while (pyramidIterator.hasNext()) {
            Pyramid pyramid = pyramidIterator.next();
            pyramid.z -= worldSpeed * dt;
            if (pyramid.z < 0.8) {
                pyramidIterator.remove();
                message = "PYRAMID PASSED!";
                score += 2;
            }
        }

        Iterator<Glass> glassIterator = glasses.iterator();
        while (glassIterator.hasNext()) {
            Glass glass = glassIterator.next();
            glass.z -= worldSpeed * dt;
            if (glass.z < 0.65) {
                glassIterator.remove();
                message = "MISSED GLASS!";
                score = Math.max(0, score - 2);
            }
        }

        Iterator<Ball> ballIterator = ballsInFlight.iterator();
        while (ballIterator.hasNext()) {
            Ball ball = ballIterator.next();
            ball.x += ball.vx * dt;
            ball.y += ball.vy * dt;
            ball.z += ball.vz * dt;
            ball.vy -= GRAVITY * dt;
            ball.life -= dt;

            boolean hit = false;
            for (int i = pyramids.size() - 1; i >= 0; i--) {
                Pyramid pyramid = pyramids.get(i);
                if (Math.abs(ball.x - pyramid.x) < pyramid.width * 0.5 + ball.radius
                        && ball.y >= pyramid.baseY - ball.radius
                        && ball.y <= pyramid.baseY + pyramid.height + ball.radius
                        && Math.abs(ball.z - pyramid.z) < pyramid.depth * 0.5 + ball.radius) {
                    hit = true;
                    message = "PYRAMID HIT!";
                    break;
                }
            }
            for (int i = glasses.size() - 1; i >= 0 && !hit; i--) {
                Glass glass = glasses.get(i);
                if (Math.abs(ball.x - glass.x) < glass.size * 0.5 + ball.radius
                        && Math.abs(ball.y - glass.y) < glass.size * 0.5 + ball.radius
                        && Math.abs(ball.z - glass.z) < glass.size * 0.5 + ball.radius) {
                    burst(glass.x, glass.y, glass.z);
                    glasses.remove(i);
                    score += 10;
                    broken++;
                    message = "GLASS SMASHED  +10";
                    hit = true;
                    break;
                }
            }

            if (hit || ball.life <= 0 || ball.z > 42 || Math.abs(ball.x) > 18
                    || ball.y < -12 || ball.y > 15) {
                ballIterator.remove();
            }
        }

        Iterator<Particle> particleIterator = particles.iterator();
        while (particleIterator.hasNext()) {
            Particle p = particleIterator.next();
            p.x += p.vx * dt;
            p.y += p.vy * dt;
            p.z += p.vz * dt;
            p.vy -= 5.5 * dt;
            p.life -= dt;
            if (p.life <= 0 || p.z <= NEAR_CLIP) {
                particleIterator.remove();
            }
        }

        if (balls <= 0 && ballsInFlight.isEmpty()) {
            gameOver = true;
            message = "RUN FINISHED  -  PRESS R";
        }
    }

    private void spawnGlass(double z) {
        double x = -3.2 + random.nextDouble() * 6.4;
        double y = -1.8 + random.nextDouble() * 3.6;
        double size = 0.75 + random.nextDouble() * 0.45;
        glasses.add(new Glass(x, y, z, size));
    }

    private void spawnPyramid(double z) {
        double x = -3.4 + random.nextDouble() * 6.8;
        double width = 1.5 + random.nextDouble() * 1.1;
        double height = 2.3 + random.nextDouble() * 1.3;
        double depth = 1.1 + random.nextDouble() * 0.5;
        pyramids.add(new Pyramid(x, -2.4, z, width, height, depth));
    }

    private void burst(double x, double y, double z) {
        // Original simple glass-like particles, each with its own 3D velocity.
        for (int i = 0; i < 24; i++) {
            double vx = (random.nextDouble() - 0.5) * 9.0;
            double vy = (random.nextDouble() - 0.15) * 9.0;
            double vz = (random.nextDouble() - 0.5) * 7.0;
            particles.add(new Particle(x, y, z, vx, vy, vz, 0.45 + random.nextDouble() * 0.65));
        }
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        drawSky(g);
        drawCorridor(g);

        // Draw distant objects first so near objects naturally overlay them.
        ArrayList<Pyramid> sortedPyramids = new ArrayList<Pyramid>(pyramids);
        sortedPyramids.sort((a, b) -> Double.compare(b.z, a.z));
        for (Pyramid pyramid : sortedPyramids) drawPyramid(g, pyramid);

        ArrayList<Glass> sortedGlass = new ArrayList<Glass>(glasses);
        sortedGlass.sort((a, b) -> Double.compare(b.z, a.z));
        for (Glass glass : sortedGlass) drawGlass(g, glass);

        for (Particle p : particles) {
            drawParticle(g, p);
        }
        for (Ball ball : ballsInFlight) {
            drawBall(g, ball);
        }

        drawCrosshair(g);
        drawHud(g);
        g.dispose();
    }

    private void drawSky(Graphics2D g) {
        g.setPaint(new java.awt.GradientPaint(0, 0, new Color(11, 24, 48),
                0, HEIGHT, new Color(1, 3, 10)));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setColor(new Color(36, 143, 255, 22));
        g.fillOval(WIDTH / 2 - 190, HEIGHT / 2 - 210, 380, 380);
    }

    private void drawCorridor(Graphics2D g) {
        double length = 34.0;
        double halfWidth = 5.5;
        double floorY = -3.2;
        double ceilY = 3.2;

        g.setStroke(new BasicStroke(1.5f));
        g.setColor(new Color(40, 130, 210, 95));

        // Long edges of the corridor are 3D lines projected into the camera.
        for (double x : new double[] {-halfWidth, halfWidth}) {
            drawLine3D(g, x, floorY, 0.8, x, floorY, length);
            drawLine3D(g, x, ceilY, 0.8, x, ceilY, length);
        }
        for (double y : new double[] {floorY, ceilY}) {
            drawLine3D(g, -halfWidth, y, 0.8, -halfWidth, y, length);
            drawLine3D(g, halfWidth, y, 0.8, halfWidth, y, length);
        }
        drawLine3D(g, -halfWidth, floorY, 0.8, halfWidth, floorY, 0.8);
        drawLine3D(g, -halfWidth, ceilY, 0.8, halfWidth, ceilY, 0.8);

        // Receding floor/ceiling ribs give a stronger sense of speed and depth.
        for (double z = 2.0; z < length; z += 2.4) {
            drawLine3D(g, -halfWidth, floorY, z, halfWidth, floorY, z);
            drawLine3D(g, -halfWidth, ceilY, z, halfWidth, ceilY, z);
        }
    }

    private void drawGlass(Graphics2D g, Glass glass) {
        double s = glass.size / 2.0;
        double x = glass.x, y = glass.y, z = glass.z;
        double[][] v = {
            {x-s,y-s,z-s}, {x+s,y-s,z-s}, {x+s,y+s,z-s}, {x-s,y+s,z-s},
            {x-s,y-s,z+s}, {x+s,y-s,z+s}, {x+s,y+s,z+s}, {x-s,y+s,z+s}
        };
        int[][] faces = {{0,1,2,3},{4,5,6,7},{0,1,5,4},{3,2,6,7},{0,3,7,4},{1,2,6,5}};
        Color[] faceColors = {
            new Color(28, 115, 160, 45), new Color(80, 220, 255, 38),
            new Color(40, 160, 210, 45), new Color(60, 190, 235, 35),
            new Color(30, 140, 195, 35), new Color(80, 205, 240, 48)
        };

        for (int i = 0; i < faces.length; i++) {
            Polygon poly = projectFace(v, faces[i]);
            if (poly != null) {
                g.setColor(faceColors[i]);
                g.fillPolygon(poly);
                g.setColor(new Color(160, 240, 255, 190));
                g.drawPolygon(poly);
            }
        }
        // Bright glint on the front face.
        drawLine3D(g, x - s * 0.55, y + s * 0.55, z - s,
                x + s * 0.2, y + s * 0.55, z - s);
    }

    private void drawPyramid(Graphics2D g, Pyramid p) {
        double left = p.x - p.width / 2.0;
        double right = p.x + p.width / 2.0;
        double front = p.z - p.depth / 2.0;
        double back = p.z + p.depth / 2.0;
        double bottom = p.baseY;
        double top = p.baseY + p.height;
        double[][] v = {
            {left,bottom,front}, {right,bottom,front},
            {right,bottom,back}, {left,bottom,back},
            {p.x,top,p.z}
        };
        int[][] faces = {{0,1,4},{1,2,4},{2,3,4},{3,0,4}};
        Color[] colors = {
            new Color(255, 100, 45, 210), new Color(190, 48, 35, 220),
            new Color(125, 30, 55, 220), new Color(255, 155, 65, 190)
        };
        for (int i = 0; i < faces.length; i++) {
            Polygon face = projectFace(v, faces[i]);
            if (face != null) {
                g.setColor(colors[i]);
                g.fillPolygon(face);
                g.setColor(new Color(255, 205, 150, 230));
                g.setStroke(new BasicStroke(1.5f));
                g.drawPolygon(face);
            }
        }
        // Highlight the tall /\ silhouette and the apex.
        drawLine3D(g, p.x, top, p.z, left, bottom, front);
        drawLine3D(g, p.x, top, p.z, right, bottom, front);
    }

    private Polygon projectFace(double[][] vertices, int[] indices) {
        Polygon polygon = new Polygon();
        for (int index : indices) {
            Point2 p = project(vertices[index][0], vertices[index][1], vertices[index][2]);
            if (p == null) return null;
            polygon.addPoint(p.x, p.y);
        }
        return polygon;
    }

    private void drawBall(Graphics2D g, Ball ball) {
        Point2 p = project(ball.x, ball.y, ball.z);
        if (p == null) return;
        int radius = Math.max(2, (int) (FOCAL_LENGTH * ball.radius / ball.z));
        g.setColor(new Color(70, 180, 255, 70));
        g.fillOval(p.x - radius * 2, p.y - radius * 2, radius * 4, radius * 4);
        g.setColor(new Color(215, 245, 255));
        g.fillOval(p.x - radius, p.y - radius, radius * 2, radius * 2);
        g.setColor(Color.WHITE);
        g.drawOval(p.x - radius, p.y - radius, radius * 2, radius * 2);
    }

    private void drawParticle(Graphics2D g, Particle particle) {
        Point2 p = project(particle.x, particle.y, particle.z);
        if (p == null) return;
        int size = Math.max(1, (int) (FOCAL_LENGTH * 0.035 / particle.z));
        int alpha = (int) (Math.min(1.0, particle.life) * 220);
        g.setColor(new Color(120, 225, 255, Math.max(0, Math.min(255, alpha))));
        g.fillRect(p.x, p.y, size, size);
    }

    private void drawLine3D(Graphics2D g, double x1, double y1, double z1,
            double x2, double y2, double z2) {
        Point2 a = project(x1, y1, z1);
        Point2 b = project(x2, y2, z2);
        if (a != null && b != null) g.drawLine(a.x, a.y, b.x, b.y);
    }

    private Point2 project(double x, double y, double z) {
        if (z < NEAR_CLIP) return null;
        int sx = (int) (getWidth() / 2.0 + x * FOCAL_LENGTH / z);
        int sy = (int) (getHeight() / 2.0 - y * FOCAL_LENGTH / z);
        return new Point2(sx, sy);
    }

    private void drawCrosshair(Graphics2D g) {
        int cx = getWidth() / 2;
        int cy = getHeight() / 2;
        g.setColor(new Color(215, 250, 255, 210));
        g.setStroke(new BasicStroke(1.5f));
        g.drawLine(cx - 9, cy, cx - 3, cy);
        g.drawLine(cx + 3, cy, cx + 9, cy);
        g.drawLine(cx, cy - 9, cx, cy - 3);
        g.drawLine(cx, cy + 3, cx, cy + 9);
        g.setColor(new Color(90, 220, 255, 100));
        g.drawOval(cx - 13, cy - 13, 26, 26);
    }

    private void drawHud(Graphics2D g) {
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 17));
        g.setColor(new Color(230, 248, 255));
        g.drawString("EXPERT PANCAKE  /  3D PHYSICS PROTOTYPE", 20, 30);
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
        g.drawString("SCORE " + score, 22, 57);
        g.drawString("BALLS " + balls, 22, 80);
        g.drawString("GLASS " + broken, 22, 103);

        String hudMessage = message;
        int textWidth = g.getFontMetrics().stringWidth(hudMessage);
        g.setColor(new Color(175, 230, 255));
        g.drawString(hudMessage, (getWidth() - textWidth) / 2, getHeight() - 25);

        if (gameOver) {
            g.setColor(new Color(0, 0, 8, 190));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 32));
            g.setColor(Color.WHITE);
            String title = "RUN FINISHED";
            g.drawString(title, (getWidth() - g.getFontMetrics().stringWidth(title)) / 2,
                    getHeight() / 2 - 18);
            g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 18));
            String subtitle = "SCORE " + score + "  -  PRESS R TO RESTART";
            g.drawString(subtitle, (getWidth() - g.getFontMetrics().stringWidth(subtitle)) / 2,
                    getHeight() / 2 + 20);
        }
    }

    private void throwBall(int targetX, int targetY) {
        if (gameOver || balls <= 0) return;

        balls--;
        double nx = (targetX - getWidth() / 2.0) / FOCAL_LENGTH;
        double ny = (getHeight() / 2.0 - targetY) / FOCAL_LENGTH;
        double speed = 23.0;
        double length = Math.sqrt(nx * nx + ny * ny + 1.0);
        Ball ball = new Ball(0, 0, 0.8,
                nx / length * speed, ny / length * speed, 1.0 / length * speed);
        ballsInFlight.add(ball);
        message = "THROW!";
    }

    private void restart() {
        balls = STARTING_BALLS;
        score = 0;
        broken = 0;
        spawnCounter = 0;
        worldSpeed = 2.6;
        gameOver = false;
        glasses.clear();
        pyramids.clear();
        ballsInFlight.clear();
        particles.clear();
        spawnGlass(15.0);
        spawnPyramid(11.0);
        spawnPyramid(21.0);
        spawnPyramid(30.0);
        message = "CLICK ANYWHERE: THROW  |  R: RESTART";
        previousNanos = System.nanoTime();
        requestFocusInWindow();
    }

    @Override public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) throwBall(e.getX(), e.getY());
    }
    @Override public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) throwBall(e.getX(), e.getY());
    }
    @Override public void mouseMoved(MouseEvent e) { mouseX = e.getX(); mouseY = e.getY(); }
    @Override public void mouseDragged(MouseEvent e) { mouseMoved(e); }
    @Override public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_R) restart();
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) System.exit(0);
    }
    @Override public void mouseReleased(MouseEvent e) { }
    @Override public void mouseEntered(MouseEvent e) { }
    @Override public void mouseExited(MouseEvent e) { }
    @Override public void keyTyped(KeyEvent e) { }
    @Override public void keyReleased(KeyEvent e) { }

    private static final class Point2 {
        final int x, y;
        Point2(int x, int y) { this.x = x; this.y = y; }
    }

    private static final class Glass {
        final double x, y, size;
        double z;
        Glass(double x, double y, double z, double size) {
            this.x = x; this.y = y; this.z = z; this.size = size;
        }
    }

    private static final class Pyramid {
        final double x, baseY, width, height, depth;
        double z;
        Pyramid(double x, double baseY, double z, double width, double height, double depth) {
            this.x = x; this.baseY = baseY; this.z = z;
            this.width = width; this.height = height; this.depth = depth;
        }
    }

    private static final class Ball {
        double x, y, z, vx, vy, vz, life = 2.5;
        final double radius = 0.12;
        Ball(double x, double y, double z, double vx, double vy, double vz) {
            this.x = x; this.y = y; this.z = z;
            this.vx = vx; this.vy = vy; this.vz = vz;
        }
    }

    private static final class Particle {
        double x, y, z, vx, vy, vz, life;
        Particle(double x, double y, double z, double vx, double vy, double vz, double life) {
            this.x = x; this.y = y; this.z = z;
            this.vx = vx; this.vy = vy; this.vz = vz; this.life = life;
        }
    }
}
