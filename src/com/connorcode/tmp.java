package com.connorcode;

import paintingcanvas.drawable.Polygon;

import java.awt.*;

public class tmp {
    /*
screen.x = (map.x - map.y) * TILE_WIDTH_HALF;
screen.y = (map.x + map.y) * TILE_HEIGHT_HALF;

new Circle(0, (10 + 10) * 4, 5);
new Circle((20 - 10) * 8, (20 + 10) * 4, 5);
new Circle((10 - 20) * 8, (10 + 20) * 4, 5);
new Circle(0, (20 + 20) * 4, 5);
 */

    public static void main(String[] args) {
        // Top
        new Polygon(new int[][]{
                {
                        (10 - 10) * 8,
                        (10 + 10) * 4
                },
                {
                        (20 - 10) * 8,
                        (20 + 10) * 4
                },
                {
                        (20 - 20) * 8,
                        (20 + 20) * 4
                },
                {
                        (10 - 20) * 8,
                        (10 + 20) * 4
                }
        }).setColor(0xFFFFFF).setOutline(Color.RED, 1);

        // Left
        new Polygon(new int[][]{
                {
                        (20 - 20) * 8,
                        (20 + 20) * 4
                },
                {
                        (20 - 20) * 8,
                        (20 + 20 + 25) * 4
                },
                {
                        (10 - 20) * 8,
                        (10 + 20 + 25) * 4
                },
                {
                        (10 - 20) * 8,
                        (10 + 20) * 4
                },
                }).setColor(0xFFFFFF).setOutline(Color.BLUE, 1);

        // RIght
        new Polygon(new int[][]{
                {
                        (20 - 10) * 8,
                        (20 + 10) * 4
                },
                {
                        (20 - 20) * 8,
                        (20 + 20) * 4
                },
                {
                        (20 - 20) * 8,
                        (20 + 20 + 25) * 4
                },
                {
                        (20 - 10) * 8,
                        (20 + 10 + 25) * 4
                }
        }).setColor(0xFFFFFF).setOutline(Color.MAGENTA, 1);
    }
}
