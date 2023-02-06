package com.connorcode;

import paintingcanvas.Canvas;
import paintingcanvas.drawable.Rectangle;
import paintingcanvas.drawable.Square;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.stream.Collectors;

public class IsoBlock {
    static final int PIXEL_SCALE = 8;
    static final String[] BLOCKS = {
            "acacia_log.png",
            "birch_log.png",
            "dark_oak_log.png",
            "jungle_log.png",
            "mangrove_log.png",
            "oak_log.png",
            "spruce_log.png",
            "stripped_acacia_log.png",
            "stripped_birch_log.png",
            "stripped_dark_oak_log.png",
            "stripped_jungle_log.png",
            "stripped_mangrove_log.png",
            "stripped_oak_log.png",
            "stripped_spruce_log.png",
            };
    static Canvas canvas = new Canvas(700, 500, "IsoBlock - Connor Slade");

    public static void main(String[] args) {
        var classLoader = IsoBlock.class.getClassLoader();
        var textures = Arrays.stream(BLOCKS)
                .map(i -> new Texture(classLoader.getResource("resources/IsoBlock/" + i)))
                .collect(Collectors.toList());

        for (int x = 0; x < 4; x++)
            for (int y = 0; y < 4; y++) {
                var index = x * 4 + y;
                if (index >= textures.size()) break;
                new Block(textures.get(index)).draw2D(x * PIXEL_SCALE * 16, y * PIXEL_SCALE * 16);
            }
    }

    static class Block {
        Rectangle[] elements;
        Texture texture;

        Block(Texture texture) {
            this.texture = texture;
        }

        void draw2D(int x, int y) {
            for (var xi = 0; xi < 16; xi++)
                for (var yi = 0; yi < 16; yi++)
                    new Square(x + xi * PIXEL_SCALE, y + yi * PIXEL_SCALE, PIXEL_SCALE).setColor(texture.get(xi, yi));
        }
    }

    static class Texture {
        int[] texture;

        // Load texture from file
        Texture(URL file) {
            BufferedImage image = null;
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
}
