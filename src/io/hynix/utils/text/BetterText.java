package io.hynix.utils.text;

import lombok.Data;

import java.util.List;

@Data
public class BetterText {

    private List<String> texts;
    public final StringBuilder output = new StringBuilder();
    private int delay;
    private int textIndex = 0;
    private int charIndex = 0;
    private boolean forward = true;
    private long lastUpdateTime = System.currentTimeMillis();

    public BetterText(List<String> texts, int delay) {
        this.texts = texts;
        this.delay = delay;
    }

    public void update() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime >= 100) {
            lastUpdateTime = currentTime;
            if (forward) {
                if (charIndex < texts.get(textIndex).length()) {
                    output.append(texts.get(textIndex).charAt(charIndex));
                    charIndex++;
                } else {
                    forward = false;
                    lastUpdateTime = currentTime + delay;
                }
            } else {
                if (charIndex > 0) {
                    output.deleteCharAt(charIndex - 1);
                    charIndex--;
                } else {
                    forward = true;
                    textIndex = (textIndex + 1) % texts.size();
                }
            }
        }
    }

    public static String replaceSymbols(String string) {
        return string
                .replaceAll("⚡", "")
                .replaceAll("ᴀ", "a")
                .replaceAll("ʙ", "b")
                .replaceAll("ᴄ", "c")
                .replaceAll("ᴅ", "d")
                .replaceAll("ᴇ", "e")
                .replaceAll("ғ", "f")
                .replaceAll("ɢ", "g")
                .replaceAll("ʜ", "h")
                .replaceAll("ɪ", "i")
                .replaceAll("ᴊ", "j")
                .replaceAll("ᴋ", "k")
                .replaceAll("ʟ", "l")
                .replaceAll("ᴍ", "m")
                .replaceAll("ɴ", "n")
                .replaceAll("ᴏ", "o")
                .replaceAll("ᴘ", "p")
                .replaceAll("ǫ", "q")
                .replaceAll("ʀ", "r")
                .replaceAll("s", "s")
                .replaceAll("ᴛ", "t")
                .replaceAll("ᴜ", "u")
                .replaceAll("ᴠ", "v")
                .replaceAll("ᴡ", "w")
                .replaceAll("x", "x")
                .replaceAll("ʏ", "y")
                .replaceAll("ᴢ", "z");
    }
}
