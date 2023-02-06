package com.connorcode;

import paintingcanvas.Canvas;
import paintingcanvas.drawable.Square;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class IsoBlock {
    static final int PIXEL_SCALE = 8;
    static final ClassLoader loader = IsoBlock.class.getClassLoader();
    static final List<String> BLOCKS = new BufferedReader(new InputStreamReader(
            Objects.requireNonNull(loader.getResourceAsStream("resources/IsoBlock/textures.txt")))).lines()
            .filter(x -> !x.startsWith("#")).collect(Collectors.toUnmodifiableList());
    static Canvas canvas = new Canvas(700, 500, "IsoBlock - Connor Slade");

    public static void main(String[] args) {
        var textures = BLOCKS.stream()
                .map(i -> new Texture(loader.getResource("resources/IsoBlock/" + i)))
                .collect(Collectors.toList());

        for (int x = 0; x < 4; x++)
            for (int y = 0; y < 4; y++) {
                var index = x * 4 + y;
                if (index >= textures.size()) break;
                new Block(textures.get(index)).draw2D(x * PIXEL_SCALE * 16, y * PIXEL_SCALE * 16);
            }
    }

    static class Block {
        Square[] elements;
        Texture texture;

        Block(Texture texture) {
            this.texture = texture;
        }

        void draw2D(int x, int y) {
            elements = new Square[16 * 16];
            for (var xi = 0; xi < 16; xi++)
                for (var yi = 0; yi < 16; yi++)
                    elements[yi * 16 + xi] = new Square(x + xi * PIXEL_SCALE, y + yi * PIXEL_SCALE, PIXEL_SCALE).setColor(texture.get(xi, yi));
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
}
