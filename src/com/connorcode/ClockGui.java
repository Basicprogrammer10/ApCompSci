package com.connorcode;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

class ClockGui {
    // == Config ==
    static final boolean TITLE_TIME = true;
    static final boolean DIGITAL_TIME = true;
    static final boolean SECOND_COLOR = true;
    static final boolean RING_NUMBERS = true;
    static final boolean SMOOTH_HANDS = true;

    static JFrame frame = new JFrame();

    public static void main(String[] args) {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("clock");
        frame.setSize(300, 300);
        frame.setVisible(true);
        frame.setAlwaysOnTop(true);
        frame.add(new Painter());

        while (true) frame.repaint();
    }

    static class Painter extends JComponent {
        static String tmpForVideo = "";

        public void paintComponent(Graphics g) {
            Graphics2D gc = (Graphics2D) g;
            gc.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            gc.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            AffineTransform transform = gc.getTransform();
            int width = getSize().width - 1;
            int height = getSize().height - 1;
            int minSize = Math.min(width, height);

            int wc = width / 2;
            int hc = height / 2;
            int radius = (int) (minSize * 0.80);
            int radiusBorder = (int) (minSize * 0.85);

            // Draw Clock Border
            gc.setColor(Color.BLACK);
            gc.fillOval(wc - radiusBorder / 2, hc - radiusBorder / 2, radiusBorder, radiusBorder);
            gc.setColor(Color.WHITE);
            gc.fillOval(wc - radius / 2, hc - radius / 2, radius, radius);

            // Draw line things
            gc.setColor(Color.BLACK);
            for (int i = 0; i < 60; i++) {
                double angle = Math.PI * 2f * (i / 60f);
                transform.setToRotation(angle, wc, hc);
                gc.setTransform(transform);

                int thick = (int) (minSize * 0.006f * (i % 5 == 0 ? 2f : 1f));
                int centerWidth = (int) ((width - thick) / 2 + minSize * 0.35);
                int centerHeight = (height - thick) / 2;
                gc.fillRect(centerWidth, centerHeight, (int) (minSize * 0.04), thick);

                gc.setFont(new Font("Arial", Font.PLAIN, (int) (minSize * 0.04)));
                transform.setToRotation(0);
                gc.setTransform(transform);
                if (i % 5 == 0 && RING_NUMBERS) {
                    String text = String.valueOf((i / 5 + 2) % 12 + 1);
                    FontMetrics fm = gc.getFontMetrics();
                    int stringHeight = fm.getAscent();
                    int stringWidth = fm.stringWidth(text);
                    int distance = (int) (minSize * 0.31);

                    double y = hc + distance * Math.sin(angle);
                    double x = wc + distance * Math.cos(angle);
                    gc.drawString(text, (int) x - stringWidth / 2f, (int) y + stringHeight / 2f);
                }
            }

            // Get Time
            long rawTime = System.currentTimeMillis();
            double epoch = rawTime / 1000d;
            double ms = rawTime % 1000 / 1000d;
            if (!SMOOTH_HANDS) epoch = Math.floor(epoch);
            double secs = epoch % 60d / 60d;
            double mins = epoch / 60d % 60d / 60d;
            double hors = (epoch / 60d / 60d - 4d) % 12d / 12d;

            // Hand constants
            double cor = Math.PI / 2d;
            int handThick = (int) (minSize * 0.008);
            int handW = (width - handThick) / 2;
            int handH = (height - handThick) / 2;

            // Draw Minute Hand
            gc.setColor(Color.BLACK);
            transform.setToRotation(Math.PI * 2 * mins - cor, wc, hc);
            gc.setTransform(transform);
            gc.fillRect(handW, handH, (int) (minSize * 0.30), handThick);

            // Draw Hour Hand
            transform.setToRotation(Math.PI * 2 * hors - cor, wc, hc);
            gc.setTransform(transform);
            gc.fillRect(handW, handH, (int) (minSize * 0.20), handThick);

            // Draw Second Hand
            gc.setColor(Color.RED);
            transform.setToRotation(Math.PI * 2 * secs - cor, wc, hc);
            gc.setTransform(transform);
            gc.fillRect(handW, handH, (int) (minSize * 0.30), handThick);

            // Center dot thing
            int dotSize = (int) (minSize * 0.035);
            gc.setColor(Color.BLACK);
            gc.fillOval(wc - dotSize / 2, hc - dotSize / 2, dotSize, dotSize);

            // Draw Digital Time
            if (!DIGITAL_TIME) return;
            transform.setToRotation(0);
            gc.setTransform(transform);
            gc.setFont(new Font("Arial", Font.PLAIN, (int) (minSize * 0.08)));
            String text = String.format("%02d:%02d:%02d", (int) (hors * 12d), (int) (mins * 60d), (int) (secs * 60d));
            if (TITLE_TIME && !tmpForVideo.equals(text)) {
                frame.setTitle(String.format("clock | %s", text));
                tmpForVideo = text;
            }
            int stringWidth = gc.getFontMetrics()
                    .stringWidth(text);
            gc.setColor(new Color(0, 0, 0, (int) (77 * (SECOND_COLOR ? Math.max(0.5, 1 - ms) : 1))));
            gc.drawString(text, wc - stringWidth / 2 + (int) (minSize * 0.0045), (int) (hc * 0.75 + minSize * 0.0045));
            gc.setColor(Color.BLACK);
            gc.drawString(text, wc - stringWidth / 2, (int) (hc * 0.75));


            // egg off
            gc.setColor(new Color(0, 0, 0, 1));
            if (rawTime % 1000 < 100) gc.fillRect(0, 0, width, height);
        }
    }
}