package com.connorcode;

import javax.swing.*;
import java.awt.*;

public class DayCycle {
    final static Rgb[] grasses = new Rgb[]{
            new Rgb(65, 152, 10),
            new Rgb(19, 109, 21)
    };
    final static Rgb[] skys = new Rgb[]{
            new Rgb(248, 139, 24),
            new Rgb(235, 37, 34),
            new Rgb(39, 4, 47),
            new Rgb(39, 4, 47)
    };
    final static int width = 700;
    final static int height = 500;

    public static void main(String[] args) throws InterruptedException {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Day Cycle");
        frame.setSize(700, 500);
        frame.setVisible(true);
        frame.add(new Painter());

        while (true) {
            frame.repaint();
            Thread.sleep(25);
        }
    }

    static class Painter extends JComponent {
        static int frame = 0;

        public void paintComponent(Graphics g) {
            frame++;
            Graphics2D gc = (Graphics2D) g;
            gc.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            int width = getSize().width - 1;
            int height = getSize().height - 1;

            // Sky Changing
            int color = frame % 160 / 40;
            gc.setColor(skys[color].lerp(skys[(color + 1) % 4], frame % 40 / 40f)
                    .asColor());
            gc.fillRect(0, 0, width, height - 200);

            // Sun Moving
            double angle = Math.PI * ((frame + 5) % 320 / 80d + 1.5);
            double radius = Math.min(width, height) / 2d;
            double x = (width / 2d) + radius * Math.cos(angle);
            double y = height - 150 + radius * Math.sin(angle);

            gc.setColor(Color.white);
            gc.fillOval((int) x, (int) y, 50, 50);

            // Redraw Land
            boolean grass = frame % 160 < 80;
            gc.setColor(grasses[grass ? 0 : 1].lerp(grasses[grass ? 1 : 0], frame % 80 / 80f)
                    .asColor());
            gc.fillRect(0, height - 200, width, height);

            // Redraw House
            int centerX = width / 2;
            gc.setColor(Color.BLACK);
            gc.fillPolygon(new Polygon(new int[]{
                    centerX - 170 / 2,
                    centerX + 70 / 2,
                    centerX + 170 / 2
            }, new int[]{
                    200,
                    175,
                    200
            }, 3));
            gc.fillRect(centerX - 150 / 2, 200, 150, 100);
        }
    }

    static class Rgb {
        final int r;
        final int g;
        final int b;

        Rgb(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        String asHex() {
            return String.format("#%02X%02X%02X", r, g, b);
        }

        Color asColor() {
            return new Color(r, g, b);
        }

        Rgb diff(Rgb nc) {
            return new Rgb(nc.r - r, nc.g - g, nc.b - b);
        }

        Rgb lerp(Rgb nc, float per) {
            Rgb diff = this.diff(nc);
            float r = this.r + diff.r * per;
            float g = this.g + diff.g * per;
            float b = this.b + diff.b * per;

            return new Rgb((int) r, (int) g, (int) b);
        }
    }
}
