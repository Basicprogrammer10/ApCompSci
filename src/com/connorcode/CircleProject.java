package com.connorcode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;

class CircleProject {
    // == Config ==
    static final int sceneWait = 2000;
    static final int balls = 20;
    static final int ballSize = 50;
    static final int width = 700;
    static final int height = 500;
    static JFrame frame = new JFrame();

    public static void main(String[] args) {
        frame.addComponentListener(new ResizeListener());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Circle Project - Connor Slade");
        frame.setSize(width, height);
        frame.setVisible(true);
        frame.add(new Painter(balls));

        while (true) frame.repaint();
    }

    enum Activity {
        PlaceRandomly, // BASE
        RandomColor, // 2
        RedGradientTime, // 2
        RedGradient, // 2
        SpaceRandomly, // 4
        FallDown; // 2

        boolean process(Painter self, Graphics2D gc) {
            Rectangle bounds = self.getBounds();
            long now = System.currentTimeMillis();

            switch (this) {
                case PlaceRandomly -> {
                    if (now - self.lastFrame >= sceneWait && self.frameCount != 0) return true;
                    if (self.frameCount > 0) return false;
                    for (Circle circle : self.circles) {
                        circle.color = Color.BLACK;
                        circle.hidden = false;
                        circle.x = (int) (Math.random() * bounds.width);
                        circle.y = (int) (Math.random() * bounds.height);
                    }
                }
                case RandomColor -> {
                    if (now - self.lastFrame >= sceneWait && self.frameCount != 0) return true;
                    if (self.frameCount > 0) return false;
                    for (Circle circle : self.circles)
                        circle.color = new Color((int) (Math.random() * 255), (int) (Math.random() * 255),
                                (int) (Math.random() * 255));
                }
                case RedGradientTime -> {
                    if (now - self.lastFrame >= sceneWait && self.frameCount != 0) return true;
                    for (Circle circle : self.circles)
                        circle.color = new Color((int) (255 * (now - self.lastFrame) / sceneWait), 0, 0);
                }
                case RedGradient -> {
                    if (now - self.lastFrame >= sceneWait && self.frameCount != 0) return true;
                    if (self.frameCount > 0) return false;
                    for (Circle circle : self.circles)
                        circle.color = new Color((int) (Math.random() * 255), 0, 0);
                }
                case SpaceRandomly -> {
                    boolean done = self.activityData != null && (Boolean) self.activityData;
                    if (now - self.lastFrame >= sceneWait && self.frameCount != 0 && done) return true;
                    if (done) return false;

                    for (Circle circle : self.circles) {
                        for (Circle other : self.circles) {
                            if (circle == other) continue;
                            while (circle.distance(other) < ballSize) {
                                circle.x = (int) (Math.random() * bounds.width);
                                circle.y = (int) (Math.random() * bounds.height);
                            }
                        }
                    }

                    boolean out = true;
                    for (Circle circle : self.circles) {
                        for (Circle other : self.circles) {
                            if (circle == other) continue;
                            if (circle.distance(other) < ballSize) out = false;
                        }
                    }

                    self.activityData = out;
                }
                case FallDown -> {
                    boolean done = self.circles.stream()
                            .allMatch(circle -> circle.y >= bounds.height + circle.radius);
                    if (now - self.lastFrame >= sceneWait && self.frameCount != 0 && done) return true;
                    if (done) return false;
                    for (Circle circle : self.circles)
                        circle.y += 1;
                }
            }
            return false;
        }

        Activity next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }

    static class Painter extends JComponent {
        List<Circle> circles = new ArrayList<>();
        Object activityData;
        Activity activity = Activity.PlaceRandomly;
        int frameCount = 0;
        long lastFrame = System.currentTimeMillis();

        Painter(int circleCount) {
            for (int i = 0; i < circleCount; i++)
                circles.add(new Circle(50, 50, ballSize, new Color(0, 0, 0), true));
        }

        public void paintComponent(Graphics g) {
            Graphics2D gc = (Graphics2D) g;
            gc.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            gc.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            String activityName = activity.name();

            boolean next = activity.process(this, gc);
            for (Circle circle : circles) circle.draw(gc);
            frameCount++;

            Rectangle bounds = this.getBounds();
            FontMetrics metrics = gc.getFontMetrics();
            gc.setColor(Color.BLACK);
            gc.drawString(activityName, bounds.width - metrics.stringWidth(activityName) - metrics.getHeight(),
                    bounds.height - metrics.getHeight());

            if (!next) return;
            frameCount = 0;
            lastFrame = System.currentTimeMillis();
            activity = activity.next();
            activityData = null;
        }
    }

    static class ResizeListener extends ComponentAdapter {
        Painter painter;

        ResizeListener() {
            painter = new Painter(balls);
        }

        public void componentResized(ComponentEvent e) {
            painter.activity = Activity.PlaceRandomly;
            painter.lastFrame = System.currentTimeMillis();
            painter.frameCount = 0;
            painter.repaint();
        }
    }

    static class Circle {
        int x;
        int y;
        int radius;
        boolean hidden;
        Color color;

        public Circle(int x, int y, int radius, Color color, boolean hidden) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.color = color;
            this.hidden = hidden;
        }

        float distance(Circle other) {
            return (float) Math.sqrt(Math.pow(x - other.x, 2) + Math.pow(y - other.y, 2));
        }

        public void draw(Graphics2D gc) {
            if (this.hidden) return;
            gc.setColor(color);
            gc.fillOval(x - radius / 2, y - radius / 2, radius, radius);
        }
    }
}