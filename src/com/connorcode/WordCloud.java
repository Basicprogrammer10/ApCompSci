package com.connorcode;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.locks.LockSupport;
import java.util.regex.Pattern;

public class WordCloud {
    static final Pattern WORD_REGEX = Pattern.compile("(\\w+)+");
    static final Misc.Rgb[] palette = new Misc.Rgb[] {

    };
    static JFrame frame = new JFrame();

    public static void main(String[] argv) throws IOException {
        var raw = new String(Objects.requireNonNull(WordCloud.class.getResourceAsStream("/resources/song.txt")).readAllBytes());
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

    static class Cloud extends JComponent {
        private final HashMap<String, String> meta;
        private final HashMap<String, Integer> words;

        Cloud(HashMap<String, String> meta, HashMap<String, Integer> words) {
            this.meta = meta;
            this.words = words;
        }

        public void paintComponent(Graphics g) {
            Graphics2D gc = (Graphics2D) g;
            gc.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            gc.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            gc.drawString("REDRAW", (int) (Math.random() * 600), (int) (Math.random() * 500));
            // todo: this
        }
    }
}
