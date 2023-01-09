package com.connorcode;

import java.awt.*;

public class Misc {
    record Pair<T, K> (T left, K right) {}

    record Rgb (int r, int g, int b) {
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
}
