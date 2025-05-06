package io.hynix.ui.clickgui.snow;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.hynix.ui.clickgui.ClickGui;
import io.hynix.utils.johon0.render.color.ColorUtils;
import io.hynix.utils.johon0.render.render2d.RectUtils;
import io.hynix.utils.text.font.ClientFonts;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Snow {
    private int width, height;
    private List<Snowflake> snowflakes = new ArrayList<>();
    private Random random = new Random();

    public Snow(int width, int height) {
        this.width = width;
        this.height = height;

        // Инициализация снежинок
        int numberOfSnowflakes = 50; // Количество снежинок
        for (int i = 0; i < numberOfSnowflakes; i++) {
            snowflakes.add(new Snowflake(random.nextInt(width), random.nextInt(height)));
        }
    }

    public void render(MatrixStack matrixStack) {
        for (Snowflake snowflake : snowflakes) {
            snowflake.update();
            ClientFonts.dev[25].drawString(matrixStack, "G", snowflake.getX(), snowflake.getY(), ColorUtils.rgba(255,255,255, (int) (255*ClickGui.getGlobalAnim().getValue())));
        }
    }

    private class Snowflake {
        private float x, y;
        private float speed;

        public Snowflake(float x, float y) {
            this.x = x;
            this.y = y;
            this.speed = 1; // Случайная скорость (1-3)
        }

        public void update() {
            y += speed;
            if (y > height) { // Если снежинка вышла за пределы экрана
                y = 0; // Сбрасываем её на верх
                x = random.nextInt(width); // Случайно перемещаем по горизонтали
            }
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }
    }
}