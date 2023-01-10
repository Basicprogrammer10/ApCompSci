package com.connorcode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class WordCloud {
    // == Config ==
    public static final Misc.Rgb[] palette = new Misc.Rgb[]{
            new Misc.Rgb(255, 212, 178),
            new Misc.Rgb(255, 246, 189),
            new Misc.Rgb(206, 237, 199),
            new Misc.Rgb(134, 200, 188)
    };
    public static final ScaleType scaleType = ScaleType.Sqrt;
    public static final int orientations = 5;
    public static final Misc.Pair<Integer, Integer> orientationRange = new Misc.Pair<>(-60, 60);
    public static final Font font = new Font("Impact", Font.PLAIN, 100);
    public static final int showTop = 50;
    public static final float layoutAngleModifier = (float) Math.toRadians(6);
    public static final int layoutRadiusModifier = 50;

    // == Other ==
    static final Pattern WORD_REGEX = Pattern.compile("([A-z']+)+");
    private static final FontRenderContext FRC = new FontRenderContext(null, true, true);
    static JFrame frame = new JFrame();

    public static void main(String[] argv) throws IOException {
        var raw = new String(
                Objects.requireNonNull(WordCloud.class.getResourceAsStream("/resources/song.txt")).readAllBytes());
        var meta = trimMeta(raw);
        var occurrences = countWords(meta.left());
        stripStopWords(occurrences);

        if (!(meta.right().containsKey("title") && meta.right().containsKey("artist"))) {
            System.err.println("Song does not have a `title` and `artist`");
            return;
        }

        var cloud = new Cloud(meta.right(), occurrences);
        frame.addComponentListener(new ResizeListener(cloud));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Word Cloud Project - Connor Slade");
        frame.setSize(700, 500);
        frame.setVisible(true);
        frame.add(cloud);
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

    static void stripStopWords(HashMap<String, Integer> words) throws IOException {
        var stopWords = new String(
                Objects.requireNonNull(WordCloud.class.getResourceAsStream("/resources/stopWords.txt"))
                        .readAllBytes()).lines().collect(Collectors.toUnmodifiableList());

        stopWords.forEach(words::remove);
    }

    static Misc.Rgb getColor(float delta) {
        float colorSize = 1f / palette.length;
        int colorIndex = Math.min((int) (delta / colorSize), palette.length - 1);
        float colorDelta = (delta - colorIndex * colorSize) / colorSize;
        return palette[colorIndex].lerp(palette[(colorIndex + 1) % palette.length], colorDelta);
    }

    static int randomOrientation() {
        var quantized = ((int) (Math.random() * orientations)) / (float) orientations;
        return (int) (orientationRange.left() + quantized * (orientationRange.right() - orientationRange.left()));
    }

    static int getFontSize(int count, int maxCount) {
        return switch (scaleType) {
            case Linear -> (int) ((float) count / (float) maxCount * 100f);
            case Log -> (int) (Math.log(count) / Math.log(maxCount) * 100);
            case Sqrt -> (int) (Math.sqrt(count) / Math.sqrt(maxCount) * 100);
        };
    }

    public static Shape genTextPath(double size, String text, double rotation) {
        final Font sizedFont = font.deriveFont((float) size);
        final GlyphVector gv =
                sizedFont.layoutGlyphVector(FRC, text.toCharArray(), 0, text.length(), Font.LAYOUT_LEFT_TO_RIGHT);
        return AffineTransform.getRotateInstance(rotation).createTransformedShape(gv.getOutline());
    }

    enum ScaleType {
        Log,
        Sqrt,
        Linear
    }

    static class Cloud extends JComponent {
        private final HashMap<String, String> meta;
        private final HashMap<String, Integer> words;
        private final int maxCount;
        private final ArrayList<Misc.Pair<Area, Color>> shapes = new ArrayList<>();
        private boolean isLoading = false;

        Cloud(HashMap<String, String> meta, HashMap<String, Integer> words) {
            this.meta = meta;
            this.words = words;
            this.maxCount = words.values().stream().max(Integer::compareTo).orElse(1);
        }

        public void paintComponent(Graphics g) {
            Graphics2D gc = (Graphics2D) g;
            gc.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            gc.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            var width = getWidth();
            var height = getHeight();

            // Layout the words (the size is dependent on the number of occurrences as stated in the `words` hashmap
            {
                gc.setBackground(Color.GRAY);
                gc.clearRect(0, 0, width, height);

                if (shapes.isEmpty()) {
                    if (!isLoading) {
                        isLoading = true;
                        var str = "Generating...";
                        gc.drawString(str, width / 2 - gc.getFontMetrics().stringWidth(str) / 2,
                                height / 2 - gc.getFontMetrics().getHeight() / 2);
                        frame.repaint();
                        return;
                    }

                    AtomicInteger count = new AtomicInteger();
                    for (Map.Entry<String, Integer> i : words.entrySet()
                            .stream()
                            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                            .takeWhile(e -> count.getAndIncrement() < showTop)
                            .collect(Collectors.toList())) {

                        var rotation = Math.toRadians(randomOrientation());
                        var angle = 0f;
                        var radius = 10f;

                        while (true) {
                            var text = genTextPath(getFontSize(i.getValue(), maxCount), i.getKey(), rotation);
                            var area = new Area(text);

                            area.transform(AffineTransform.getTranslateInstance(
                                    width / 2f + radius * Math.cos(Math.PI * angle),
                                    height / 2f + radius * Math.sin(Math.PI * angle)));

                            angle += layoutAngleModifier;
                            if (angle > 2) {
                                angle = 0;
                                radius += layoutRadiusModifier;
                            }

                            if (shapes.stream().anyMatch(e -> {
                                if (!e.left().intersects(area.getBounds2D())) return false;
                                var intersect = (Area) e.left().clone();
                                intersect.intersect(area);
                                return !intersect.isEmpty();
                            })) continue;

                            var wordCount = Math.min(words.size(), showTop);
                            System.out.printf("\r%d/%d", wordCount - count.decrementAndGet(), wordCount);
                            shapes.add(new Misc.Pair<>(area, getColor((float) Math.random()).asColor()));
                            break;
                        }
                    }
                }

                shapes.forEach(e -> {
                    gc.setColor(e.right());
                    gc.fill(e.left());
                });
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

    static class ResizeListener extends ComponentAdapter {
        Cloud cloud;
        private Misc.Pair<Integer, Integer> lastSize = new Misc.Pair<>(0, 0);

        ResizeListener(Cloud cloud) {
            this.cloud = cloud;
        }

        public void componentResized(ComponentEvent e) {
            var newWidth = e.getComponent().getWidth();
            var newHeight = e.getComponent().getHeight();
            var newSize = new Misc.Pair<>(newWidth, newHeight);
            if (lastSize.equals(newSize)) return;

            var oldWidth = lastSize.left();
            var oldHeight = lastSize.right();
            lastSize = newSize;

            var widthDiff = newWidth / 2f - oldWidth / 2f;
            var heightDiff = newHeight / 2f - oldHeight / 2f;
            var transform = AffineTransform.getTranslateInstance(widthDiff, heightDiff);

            cloud.shapes.forEach(s -> s.left().transform(transform));
            cloud.repaint();
        }
    }
}
