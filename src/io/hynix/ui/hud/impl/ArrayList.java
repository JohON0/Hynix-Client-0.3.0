package io.hynix.ui.hud.impl;

import com.mojang.blaze3d.matrix.MatrixStack;

import io.hynix.HynixMain;
import io.hynix.events.impl.EventRender2D;
import io.hynix.events.impl.EventUpdate;
import io.hynix.ui.clickgui.ClickGui;
import io.hynix.ui.hud.updater.ElementRenderer;
import io.hynix.ui.hud.updater.ElementUpdater;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitManager;
import io.hynix.utils.johon0.math.TimerUtils;
import io.hynix.utils.johon0.render.color.ColorUtils;
import io.hynix.utils.johon0.render.font.Fonts;
import io.hynix.utils.johon0.render.render2d.RenderUtils;
import io.hynix.utils.text.font.ClientFonts;
import net.minecraft.util.math.vector.Vector4f;
import ru.hogoshi.Animation;

import java.util.List;

public class ArrayList implements ElementRenderer, ElementUpdater {

    private int lastIndex;

    List<Unit> list;


    TimerUtils stopWatch = new TimerUtils();

    @Override
    public void update(EventUpdate e) {
        if (stopWatch.isReached(1000)) {
            list = HynixMain.getInstance().getModuleManager().getSorted(Fonts.sfui, 9 - 1.5f)
                    .stream()
                    .filter(m -> m.getCategory() != Category.Display)
                    .filter(m -> m.getCategory() != Category.Miscellaneous)
                    .toList();
            stopWatch.reset();
        }
    }

    @Override
    public void render(EventRender2D eventDisplay) {
        MatrixStack ms = eventDisplay.getMatrixStack();
        float rounding = 4;
        float padding = 3.5f;


        float posX = 0;
        float posY = 44;
        int index = 0;

        if (list == null) return;

        for (Unit f : list) {
            float fontSize = 6.5f;
            Animation anim = f.getAnimation();
            float value = (float) anim.getValue();
            String text = f.getName();
            float textWidth = ClientFonts.tenacityBold[12].getWidth(text);

            if (value != 0) {
                float localFontSize = fontSize * value;
                float localTextWidth = textWidth * value;

//                RenderUtils.drawShadow(posX, posY, localTextWidth + padding * 2, localFontSize + padding * 2, 14, ColorUtils.setAlpha(ClickGui.backgroundColor, (int) (255 * value)));
                posY += (fontSize + padding * 2) * value;
                index++;
            }
        }
        index = 0;
        posY = 5;
        for (Unit f : list) {
            float width = ClientFonts.tenacityBold[12].getWidth(f.getName()) + (padding * 2);
            posX = window.scaledWidth() - 4 - width;
            float fontSize = 4;
            ru.hogoshi.Animation anim = f.getAnimation();
            anim.update();
            int in = 0;
            float value = (float) anim.getValue();

            String text = f.getName();
            float textWidth = ClientFonts.tenacityBold[12].getWidth(text);

            if (value != 0) {
                float localFontSize = fontSize * value;
                float localTextWidth = textWidth * value;

                boolean isFirst = index == 0;
                boolean isLast = index == lastIndex;

                float localRounding = rounding;

                for (Unit f2 : list.subList(list.indexOf(f) + 1, list.size())) {
                    if (f2.getAnimation().getValue() != 0) {
                        localRounding = isLast ? rounding : Math.min(textWidth - ClientFonts.tenacityBold[12].getWidth(f2.getName()), rounding);
                        break;
                    }
                }


                Vector4f right_vec = new Vector4f(isFirst ? rounding : 0, localRounding, isFirst ? rounding : 0, isLast ? rounding : 0);
                Vector4f rectVec = new Vector4f(isFirst ? rounding : 0, isLast ? 0 : rounding, isFirst ? rounding : 0, isFirst ? 0 : localRounding);


                RenderUtils.drawShadow(posX, posY, localTextWidth + padding * 2, localFontSize + padding * 2, 4, ClickGui.backgroundColor);

                RenderUtils.drawRoundedRect(posX, posY, localTextWidth + padding * 2, localFontSize + padding * 2, right_vec, ClickGui.backgroundColor);

                ClientFonts.tenacityBold[12].drawString(ms, f.getName(), posX + padding, posY + padding + 1, ColorUtils.setAlpha(ClickGui.textcolor, (int) (255 * value)));

                posY += (fontSize + padding * 2) * value;
                index++;
            }
        }

        lastIndex = index - 1;
    }
}
