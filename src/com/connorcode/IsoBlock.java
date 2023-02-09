package com.connorcode;

import paintingcanvas.Canvas;
import paintingcanvas.drawable.Polygon;
import paintingcanvas.drawable.Rectangle;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class IsoBlock {
    static final int PIXEL_SIZE = 16;
    static final int PIXEL_WIDTH = 16;
    static final int PIXEL_HEIGHT = 8;
    static final ClassLoader loader = IsoBlock.class.getClassLoader();
    static final List<String> BLOCKS = new BufferedReader(new InputStreamReader(
            Objects.requireNonNull(loader.getResourceAsStream("resources/IsoBlock/textures.txt")))).lines()
            .filter(x -> !x.startsWith("#")).collect(Collectors.toUnmodifiableList());
    static Canvas canvas = new Canvas(700, 500, "IsoBlock - Connor Slade");

    public static void main(String[] args) {
        // enable dark mode
        new Rectangle(0, 0, 10000, 10000, Color.BLACK);

        var textures = BLOCKS.stream()
                .map(i -> new Texture(loader.getResource("resources/IsoBlock/" + i)))
                .collect(Collectors.toList());

        for (int x = 0; x < 4; x++)
            for (int y = 0; y < 4; y++) {
                var index = x * 4 + y;
                if (index >= textures.size()) break;
                new Block(textures.get(index)).draw(x + PIXEL_WIDTH, y + PIXEL_HEIGHT);
            }
    }

    static Color darken(Color color, float light) {
        return new Color((int) (color.getRed() * light), (int) (color.getGreen() * light),
                (int) (color.getBlue() * light));
    }

    static class Block {
        Polygon[] elements;
        Texture texture;

        Block(Texture texture) {
            this.texture = texture;
        }

        void draw(int x, int y) {
            elements = new Polygon[16 * 16];
            System.out.println(Arrays.deepToString(top(x, y)));
            elements[0] = new Polygon(top(x, y)).setColor(Color.RED);
            elements[1] = new Polygon(left(x, y)).setColor(Color.BLUE);
            elements[2] = new Polygon(right(x, y)).setColor(Color.MAGENTA);
//            _drawSide(x, y, PIXEL_SCALE / 2, 1);
//            _drawSide(x + PIXEL_WIDTH * 16, y + (PIXEL_SCALE / 2 * 16), PIXEL_SCALE / -2, .7f);
//            _drawSide(x, y, PIXEL_SCALE / 2, 90, .85f);
        }

        void _drawSide(int x, int y, int sheer, float light) {
            for (var xi = 0; xi < 16; xi++) {
                for (var yi = 0; yi < 16; yi++) {
                    var color = darken(new Color(texture.get(xi, yi)), light);
                    var idk = 0;
                    var gon = new Gon(sheer)
                            .translate(x + xi * PIXEL_WIDTH, y + yi * idk + xi * (sheer))
                            .build();
                    elements[yi * 16 + xi] = new Polygon(gon).setColor(color).setOutline(color, 1);
                }
            }
        }
    }

    static class Texture {
        int[] texture;

        // Load texture from file
        Texture(URL file) {
            BufferedImage image;
            try {
                image = ImageIO.read(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.printf("Loaded %s - (%d x %d)\n", file, image.getWidth(), image.getHeight());
            assert image.getWidth() == 16 && image.getHeight() == 16;
            this.texture = image.getRGB(0, 0, 16, 16, null, 0, 16);
        }

        int get(int x, int y) {
            return texture[y * 16 + x];
        }
    }

    static class Gon {
        int[][] gon;

        Gon(int sheer) {
            gon = new int[][]{
                    {
                            0,
                            0
                    },
                    {
                            PIXEL_WIDTH,
                            sheer
                    },
                    {
                            PIXEL_WIDTH,
                            PIXEL_SIZE + sheer
                    },
                    {
                            0,
                            PIXEL_SIZE
                    }
            };
        }

        Gon translate(int x, int y) {
            for (var yi = 0; yi < gon.length; yi++) {
                var old = gon[yi];
                gon[yi] = new int[]{
                        x + old[0],
                        y + old[1]
                };
            }
            return this;
        }

        int[][] build() {
            return this.gon;
        }
    }

    static int[][] top(int x, int y) {
        var wh = PIXEL_WIDTH / 2;
        var hh = PIXEL_HEIGHT / 2;
        var size = PIXEL_SIZE;
        return new int[][]{
                {
                        (x - y) * wh ,
                        (x + y) * hh
                },
                {
                        (size + x - y) * wh,
                        (size + x + y) * hh
                },
                {
                        (x - y) * wh,
                        (2 * size + x + y) * hh
                },
                {
                        (x - y - size) * wh,
                        (x + y + size) * hh
                }
        };
    }

    static int[][] left(int x, int y) {
        var wh = PIXEL_WIDTH / 2;
        var hh = PIXEL_HEIGHT / 2;
        var size = PIXEL_SIZE;
        var height = (int)(Math.sqrt(2) * PIXEL_SIZE);
        return new int[][]{
                {
                        (x - y) * wh ,
                        (2 * size + x + y) * hh
                },
                {
                        (x - y) * wh ,
                        (3 * size + x + y + height) * hh
                },
                {
                        (x - y - size) * wh,
                        (2 * size + x + y + height) * hh
                },
                {
                        (x - y - size) * wh,
                        (x + y + size) * hh
                },
        };
    }

    static int[][] right(int x, int y) {
        var wh = PIXEL_WIDTH / 2;
        var hh = PIXEL_HEIGHT / 2;
        var size = PIXEL_SIZE;
        var height = (int)(Math.sqrt(2) * PIXEL_SIZE);
        return new int[][]{
                {
                        (size + x - y) * wh ,
                        (size + x + y) * hh
                },
                {
                        (x - y) * wh ,
                        (2 * size + x + y) * hh
                },
                {
                        (x - y) * wh,
                        (3 * size + x + y + height) * hh
                },
                {
                        (size + x - y) * wh,
                        (2 * size + x + y + height) * hh
                },
                };
    }
}
