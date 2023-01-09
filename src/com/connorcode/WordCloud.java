package com.connorcode;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.LockSupport;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class WordCloud {
    static final Pattern WORD_REGEX = Pattern.compile("(\\w+)+");
    static final Misc.Rgb[] palette = new Misc.Rgb[]{
            new Misc.Rgb(255, 212, 178),
            new Misc.Rgb(255, 246, 189),
            new Misc.Rgb(206, 237, 199),
            new Misc.Rgb(134, 200, 188)
    };
    static final int orientations = 5;
    static final Misc.Pair<Integer, Integer> orentationRange = new Misc.Pair<>(-60, 60);
    static final Font font = new Font("Impact", Font.PLAIN, 100);
    static JFrame frame = new JFrame();

    public static void main(String[] argv) throws IOException, InterruptedException {
        var raw = new String(
                Objects.requireNonNull(WordCloud.class.getResourceAsStream("/resources/song.txt")).readAllBytes());
        var meta = trimMeta(raw);
        var occurrences = countWords(meta.left());

        if (!(meta.right().containsKey("title") && meta.right().containsKey("artist"))) {
            System.err.println("Song does not have a `title` and `artist`");
            return;
        }

        frame.addComponentListener(new CircleProject.ResizeListener());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Circle Project - Connor Slade");
        frame.setSize(700, 500);
        frame.setVisible(true);
        frame.add(new Cloud(meta.right(), occurrences));

        LockSupport.park();
    }

    static Misc.Pair<String, HashMap<String, String>> trimMeta(String raw) {
        var meta = new HashMap<String, String>();
        var parts = raw.split("---");

        parts[1].lines().filter(e -> !e.isBlank()).forEach(e -> {
            var entryParts = e.split(":", 2);
            meta.put(entryParts[0].trim(), entryParts[1].trim());
        });

        return new Misc.Pair<>(parts[2], meta);
    }

    static HashMap<String, Integer> countWords(String raw) {
        var out = new HashMap<String, Integer>();

        var matches = WORD_REGEX.matcher(raw);
        while (matches.find()) {
            var word = matches.group(1).toLowerCase(Locale.ROOT);
            var count = out.getOrDefault(word, 0);
            out.put(word, count + 1);
        }

        return out;
    }

    static Misc.Rgb getColor(float delta) {
        float colorSize = 1f / palette.length;
        int colorIndex = Math.min((int) (delta / colorSize), palette.length - 1);
        float colorDelta = (delta - colorIndex * colorSize) / colorSize;
        return palette[colorIndex].lerp(palette[(colorIndex + 1) % palette.length], colorDelta);
    }

    static int randomOrientation() {
        return (int) (orentationRange.left() + Math.random() * (orentationRange.right() - orentationRange.left()));
    }

    static int getFontSize(int count, int maxCount) {
//        return (int) (Math.log(count) / Math.log(maxCount) * 100);
        return (int) (Math.sqrt(count) / Math.sqrt(maxCount) * 100);
    }

    static class Cloud extends JComponent {
        private final HashMap<String, String> meta;
        private final HashMap<String, Integer> words;
        private final int maxCount;

        Cloud(HashMap<String, String> meta, HashMap<String, Integer> words) {
            this.meta = meta;
            this.words = words;
            this.maxCount = words.values().stream().max(Integer::compareTo).orElse(1);
        }

        public void paintComponent(Graphics g) {
            Graphics2D gc = (Graphics2D) g;
            AffineTransform transform = gc.getTransform();
            gc.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            gc.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            var width = getWidth();
            var height = getHeight();

            // Layout the words (the size is dependent on the number of occurrences as stated in the `words` hashmap
            {
                gc.setBackground(Color.GRAY);
                gc.clearRect(0, 0, width, height);

                for (Map.Entry<String, Integer> i : words.entrySet()
                        .stream()
                        .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                        .collect(Collectors.toList())) {
                    transform.setToRotation(Math.toRadians(randomOrientation()), width / 2f, height / 2f);
                    gc.setTransform(transform);

                    var textX = (int) (Math.random() * width);
                    var textY = (int) (Math.random() * height);

                    gc.setColor(getColor((float) i.getValue() / maxCount).asColor());
                    gc.setFont(font.deriveFont((float) getFontSize(i.getValue(), maxCount)));
                    gc.drawString(i.getKey(), textX, textY);
                }

                transform.setToRotation(0, width / 2f, height / 2f);
                gc.setTransform(transform);
            }

            // Draw song info
            {
                var title = meta.get("title");
                var artist = meta.get("artist");

                gc.setColor(Color.BLACK);
                gc.setFont(new Font("Arial", Font.PLAIN, 20));
                gc.drawString(artist, width - gc.getFontMetrics().stringWidth(artist) - 10, height - 10);
                var artistHeight = gc.getFontMetrics().getHeight();

                gc.setFont(new Font("Arial", Font.BOLD, 20));
                gc.drawString(title, width - gc.getFontMetrics().stringWidth(title) - 10, height - artistHeight - 20);
            }
        }
    }
}
