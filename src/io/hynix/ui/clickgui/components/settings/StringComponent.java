package io.hynix.ui.clickgui.components.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.hynix.ui.clickgui.ClickGui;
import io.hynix.units.settings.impl.StringSetting;
import io.hynix.utils.johon0.math.MathUtils;
import io.hynix.utils.johon0.render.Cursors;
import io.hynix.utils.johon0.render.color.ColorUtils;
import io.hynix.utils.johon0.render.render2d.RenderUtils;
import io.hynix.utils.text.font.ClientFonts;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import org.apache.commons.lang3.math.NumberUtils;
import org.lwjgl.glfw.GLFW;
import io.hynix.ui.clickgui.components.builder.Component;

import java.util.ArrayList;
import java.util.List;


@FieldDefaults(level = AccessLevel.PRIVATE)
public class StringComponent extends Component {

    final StringSetting setting;
    boolean typing;
    String text = "";

    private static final int X_OFFSET = 5;
    private static final int Y_OFFSET = 10;
    private static final int WIDTH_OFFSET = -9;
    private static final int TEXT_Y_OFFSET = -7;

    public StringComponent(StringSetting setting) {
        this.setting = setting;
        this.setHeight(24);
    }

    boolean hovered = false;

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        super.render(stack, mouseX, mouseY);
        text = setting.getValue();
        if (setting.isOnlyNumber() && !NumberUtils.isNumber(text)) {
            text = text.replaceAll("[a-zA-Z]", "");
        }
        float x = calculateX();
        float y = calculateY();
        float width = calculateWidth();
        String settingName = setting.getName();
        String settingDesc = setting.getDescription();
        String textToDraw = setting.getValue();

        if (!typing && setting.getValue().isEmpty()) {
            textToDraw = settingDesc;
        }
        if (setting.isOnlyNumber() && !NumberUtils.isNumber(textToDraw)) {
            textToDraw = textToDraw.replaceAll("[a-zA-Z]", "");
        }

        float height = calculateHeight(textToDraw, width - 1);
        drawSettingName(stack, settingName, x, y);
        drawBackground(x, y, width, height);
        drawTextWithLineBreaks(stack, textToDraw + (typing && text.length() < 59 && System.currentTimeMillis() % 1000 > 500 ? "_" : ""), x + 1, y + ClientFonts.tenacityBold[14].getFontHeight() / 2, width - 1);

        if (isHovered(mouseX, mouseY)) {
            if (MathUtils.isHovered(mouseX, mouseY, x, y, width, height)) {
                if (!hovered) {
                    GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.IBEAM);
                    hovered = true;
                }
            } else {
                if (hovered) {
                    GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.ARROW);
                    hovered = false;
                }
            }
        }
        setHeight(height + 12);
    }

    private void drawTextWithLineBreaks(MatrixStack stack, String text, float x, float y, float maxWidth) {

        String[] lines = text.split("\n");
        float currentY = y;

        for (String line : lines) {
            List<String> wrappedLines = wrapText(line, 6, maxWidth);
            for (String wrappedLine : wrappedLines) {
                ClientFonts.tenacityBold[14].drawString(stack, wrappedLine, x, currentY, ColorUtils.setAlpha(-1, (int) (255 * io.hynix.ui.clickgui.ClickGui.getGlobalAnim().getValue())));
                currentY += ClientFonts.tenacityBold[14].getFontHeight();
            }
        }
    }

    private List<String> wrapText(String text, float size, float maxWidth) {

        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if (ClientFonts.tenacityBold[14].getWidth(word) <= maxWidth) {
                if (ClientFonts.tenacityBold[14].getWidth(currentLine.toString() + word) <= maxWidth) {
                    currentLine.append(word).append(" ");
                } else {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word).append(" ");
                }
            } else {
                if (!currentLine.toString().isEmpty()) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder();
                }
                currentLine = breakAndAddWord(word, currentLine, size, maxWidth, lines);
            }
        }

        if (!currentLine.toString().isEmpty()) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    private StringBuilder breakAndAddWord(String word, StringBuilder currentLine, float size, float maxWidth, List<String> lines) {
        int wordLength = word.length();
        for (int i = 0; i < wordLength; i++) {
            char c = word.charAt(i);
            String nextPart = currentLine.toString() + c;
            if (ClientFonts.tenacityBold[14].getWidth(nextPart) <= maxWidth) {
                currentLine.append(c);
            } else {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(String.valueOf(c));
            }
        }
        return currentLine;
    }


    private float calculateX() {
        return getX() + X_OFFSET;
    }

    private float calculateY() {
        return getY() + Y_OFFSET;
    }

    private float calculateWidth() {
        return getWidth() + WIDTH_OFFSET;
    }

    private float calculateHeight(String text, float maxWidth) {
        List<String> wrappedLines = wrapText(text, 6, maxWidth);
        int numberOfLines = wrappedLines.size();
        float lineHeight = ClientFonts.tenacityBold[14].getFontHeight();
        float spacingBetweenLines = 1.5f;
        float initialHeight = 5;

        return initialHeight + (numberOfLines * lineHeight) + ((numberOfLines - 1));
    }


    private void drawSettingName(MatrixStack stack, String settingName, float x, float y) {
        ClientFonts.tenacityBold[14].drawString(stack, settingName, x, y + TEXT_Y_OFFSET, ColorUtils.setAlpha(ClickGui.textcolor, (int) (255 * io.hynix.ui.clickgui.ClickGui.getGlobalAnim().getValue())));
    }

    private void drawBackground(float x, float y, float width, float height) {
        RenderUtils.drawShadow(x, y, width, height, 10, ColorUtils.rgba(35, 35, 35, (int) (45 * io.hynix.ui.clickgui.ClickGui.getGlobalAnim().getValue())));
        RenderUtils.drawRoundedRect(x, y, width, height, 2, ColorUtils.rgba(35, 35, 35, (int) (45 * io.hynix.ui.clickgui.ClickGui.getGlobalAnim().getValue())));
    }


    @Override
    public boolean mouseClick(float mouseX, float mouseY, int mouse) {
        if (isHovered(mouseX, mouseY)) {
            typing = !typing; // Переключить состояние
        } else {
            typing = false; // Закрыть ввод, если не наведено
        }
        super.mouseClick(mouseX, mouseY, mouse);
        return false;
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {
        // Если настройка принимает только числа, игнорируем остальные символы
        if (setting.isOnlyNumber() && !NumberUtils.isNumber(String.valueOf(codePoint))) {
            return;
        }

        // Проверяем, что мы находимся в режиме ввода и длина текста меньше 60
        if (typing && text.length() < 60) {
            text += codePoint; // Добавляем введенный символ в текст
            setting.setValue(text); // Обновляем значение настройки
        }
        super.charTyped(codePoint, modifiers);
    }


    @Override
    public void keyPressed(int key, int scanCode, int modifiers) {
        if (typing) {
            if (Screen.isPaste(key)) {
                pasteFromClipboard(); // Вставка из буфера
            } else if (key == GLFW.GLFW_KEY_BACKSPACE) {
                deleteLastCharacter(); // Удаление последнего символа
            } else if (key == GLFW.GLFW_KEY_ENTER) {
                typing = false; // Завершение текстового ввода
            } else {
                // Здесь мы можем вызвать charTyped для обработки символов, если они не специальное
                char typedChar = (char) key; // Преобразуем ключ в символ
                charTyped(typedChar, modifiers); // Вызываем метод для обработки
            }
        }
        super.keyPressed(key, scanCode, modifiers);
    }


    private boolean isControlDown() {
        return GLFW.glfwGetKey(Minecraft.getInstance().getMainWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS || GLFW.glfwGetKey(Minecraft.getInstance().getMainWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
    }

    private void pasteFromClipboard() {
        try {
            text += GLFW.glfwGetClipboardString(Minecraft.getInstance().getMainWindow().getHandle());
            setting.setValue(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteLastCharacter() {
        if (!text.isEmpty()) {
            text = text.substring(0, text.length() - 1);
            setting.setValue(text);
        }
    }

    @Override
    public boolean isVisible() {
        return setting.visible.get();
    }

}
