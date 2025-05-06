package io.hynix.ui.mainmenu.changelog;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.hynix.utils.johon0.render.render2d.RenderUtils;
import io.hynix.utils.text.font.ClientFonts;

@SuppressWarnings("all")
public class ChangelogRender {
    private Changelog changelog;

    public ChangelogRender(Changelog changelog) {
        this.changelog = changelog;
    }

    public void renderChangelog(MatrixStack matrixStack, float x, float y) {
        // Начальное смещение по Y для строк
        int offset = 20;

        // Рендерим каждую строку изменений
        for (Changelog.Change change : changelog.getChanges()) {
            // Рендерим цветной круг рядом с текстом
            RenderUtils.drawShadowCircle(x + 8, y + offset + 2, 6, change.getColor());
            RenderUtils.drawCircle(x + 8, y + offset + 2, 6, change.getColor());

            // Рендерим текст изменения с учетом смещения для круга
            ClientFonts.tenacityBold[14].drawString(matrixStack, change.getMessage(), x + 13, y + offset, -1);
            offset += 10; // Увеличиваем смещение для следующей строки
        }
    }
}