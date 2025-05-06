package io.hynix.ui.hud.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import io.hynix.HynixMain;
import io.hynix.events.impl.EventRender2D;
import io.hynix.events.impl.EventUpdate;
import io.hynix.ui.clickgui.ClickGui;
import io.hynix.units.impl.traversal.Timer;
import io.hynix.ui.hud.updater.ElementRenderer;
import io.hynix.ui.hud.updater.ElementUpdater;
import io.hynix.managers.theme.Theme;
import io.hynix.managers.drag.Dragging;
import io.hynix.utils.johon0.animations.AnimationUtils;
import io.hynix.utils.johon0.animations.Direction;
import io.hynix.utils.johon0.animations.impl.EaseBackIn;
import io.hynix.utils.johon0.math.MathUtils;
import io.hynix.utils.player.MoveUtils;
import io.hynix.utils.johon0.render.render2d.RenderUtils;
import io.hynix.utils.text.font.ClientFonts;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector4f;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class TimerHud implements ElementRenderer, ElementUpdater {
    final AnimationUtils animation = new EaseBackIn(400, 1, 1);
    final Dragging dragging;
    float perc;

    @Override
    public void render(EventRender2D eventRender2D) {
        MatrixStack ms = eventRender2D.getMatrixStack();
        Timer timer = HynixMain.getInstance().getModuleManager().getTimer();
        String text = "Timer";

        boolean isTimerCharged = false;
        float posX = dragging.getX();
        float posY = dragging.getY();
        float quotient = timer.maxViolation / timer.speed.getValue();
        float minimumValue = Math.min(timer.violation, quotient);
        float textWidth = ClientFonts.msMedium[16].getWidth(text);
        float width = 40 + textWidth + 3;
        float height = 15;
        float timerWidth = ((width - textWidth - 9) * perc);
        perc = MathUtils.lerp(perc, ((quotient - minimumValue) / quotient), 10);
        dragging.setWidth(width);
        dragging.setHeight(height);

        if (perc < 0.96 || mc.currentScreen instanceof ChatScreen) {
            isTimerCharged = true;
        }

        animation.setDirection(isTimerCharged ? Direction.FORWARDS : Direction.BACKWARDS);
        animation.setDuration(isTimerCharged ? 300 : 200);

        GlStateManager.pushMatrix();
        RenderUtils.sizeAnimation(posX + (width / 2), (posY + height / 2), animation.getOutput());

        //RenderUtility.drawStyledShadowRectWithChange(ms, posX, posY, width, height);
        RenderUtils.drawShadow(posX,posY, (float) width, height, 5, ClickGui.backgroundColor);
        RenderUtils.drawRoundedRect(posX,posY, (float) width, height, 4, ClickGui.backgroundColor);
        RenderUtils.drawRoundedRect(posX + 3, posY + 3, timerWidth + 10, height - (3 * 2), new Vector4f(3, 3, 3, 3), ClickGui.lightcolor);
     //   RenderUtility.drawShadow(posX + 3, posY + 3, timerWidth + 10, height - (3 * 2), 8, Theme.mainRectColor);

        ClientFonts.timer[30].drawString(ms, "A", Math.max(posX + 7, posX + timerWidth + 17), posY - 2f + 5.5f, Theme.rectColor);

        GlStateManager.popMatrix();
    }

    @Override
    public void update(EventUpdate e) {
        Timer timer = HynixMain.getInstance().getModuleManager().getTimer();

        if (!MoveUtils.isMoving()) {
            timer.violation = (float) ((double) timer.violation - ((double) timer.ticks.getValue() + 0.4));
        } else if (timer.moveUp.getValue()) {
            timer.violation -= (timer.moveUpValue.getValue());
        }

        timer.violation = (float) MathHelper.clamp(timer.violation, 0.0, Math.floor(timer.maxViolation));
    }
}
