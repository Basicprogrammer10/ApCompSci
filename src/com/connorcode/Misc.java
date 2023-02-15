package com.connorcode;

import paintingcanvas.App;
import paintingcanvas.Canvas;

import java.awt.*;
import java.util.List;
import java.util.Vector;

public class Misc {
    public static class Rgb {
        int r, g, b;

        Rgb(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        Color asColor() {
            return new Color(this.r, this.g, this.b);
        }

        Rgb diff(Rgb nc) {
            return new Rgb(nc.r - r, nc.g - g, nc.b - b);
        }

        Rgb lerp(Rgb other, float delta) {
            Rgb diff = this.diff(other);
            float r = this.r + diff.r * delta;
            float g = this.g + diff.g * delta;
            float b = this.b + diff.b * delta;

            return new Rgb((int) r, (int) g, (int) b);
        }
    }

    public static class Pair<T, K> {
        T left;
        K right;

        Pair(T left, K right) {
            this.left = left;
            this.right = right;
        }

        T left() {
            return this.left;
        }

        K right() {
            return this.right;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Pair) {
                Pair<?, ?> other = (Pair<?, ?>) obj;
                return this.left.equals(other.left) && this.right.equals(other.right);
            }
            return false;
        }
    }

    public static class FrameCounter implements Canvas.CanvasComponent.RenderLifecycle {
        public boolean enabled = true;
        long lastFrame = System.currentTimeMillis();
        List<Long> frameTimes = new Vector<>();

        @Override
        public void renderEnd(Graphics g) {
            // Update frame times
            var now = System.currentTimeMillis();
            frameTimes.add(now - lastFrame);
            while (frameTimes.size() > 10) frameTimes.remove(0);
            lastFrame = now;
            if (!enabled) return;

            // Get average times
            var sum = 0;
            for (var i : frameTimes) sum += i;
            var avg = (float) (sum) / frameTimes.size();

            // Draw UI
            var gc = (Graphics2D) g;
            var fh = gc.getFontMetrics().getHeight();
            gc.setColor(Color.WHITE);
            var i = 0;
            for (var e : new String[]{
                    String.format("FPS: %d", (int) (1000 / avg)),
                    String.format("FrameTime: %.1f", avg),
                    String.format("Elements: %d", App.canvas.canvas.elements.size()),
                    })
                gc.drawString(e, 10, 20 + (i++ * fh));
        }
    }
}
