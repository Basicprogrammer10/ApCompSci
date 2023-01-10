package com.connorcode;

import java.awt.*;

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
}
