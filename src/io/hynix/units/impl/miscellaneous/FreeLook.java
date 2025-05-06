package io.hynix.units.impl.miscellaneous;

import com.google.common.eventbus.Subscribe;
import io.hynix.events.impl.EventRender3D;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.units.settings.impl.BindSetting;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

@UnitRegister(
        name = "FreeLook",
        category = Category.Miscellaneous,
        desc = "Свободный обзор"
)
public class FreeLook extends Unit {
    public static BindSetting look = new BindSetting("Кнопка", -1);
    public static float prevX = 0.0F;
    public static float prevY = 0.0F;
    public static float x = 0.0F;
    public static float y = 0.0F;
    public static boolean isActive = false;
    public PointOfView pointOfView;

    public FreeLook() {
        this.addSettings(look);
    }

    @Subscribe
    public void onRender3D(EventRender3D e) {
        this.onRender(e);

    }

    public void onRender(EventRender3D e) {
        int id = (Integer)look.getValue();
        boolean flag = false;
        if (id < 0) {
            id = Math.abs(Math.abs(id) - 100);
            flag = GLFW.glfwGetMouseButton(sr.getHandle(), id) == 1;
        } else {
            flag = GLFW.glfwGetKey(sr.getHandle(), id) == 1;
        }

        if (flag && mc.currentScreen == null) {
            if (!isActive) {
                this.pointOfView = mc.gameSettings.getPointOfView();
                x = mc.player.rotationYaw;
                y = mc.player.rotationPitch;
                prevX = mc.player.prevRotationYaw;
                prevY = mc.player.prevRotationPitch;
            }

            mc.gameSettings.setPointOfView(PointOfView.THIRD_PERSON_BACK);
            isActive = true;
        } else if (isActive) {
            mc.gameSettings.setPointOfView(this.pointOfView);
            isActive = false;
        }

    }

    public static float getYaw(float partialTicks) {
        return partialTicks == 1.0F ? x : MathHelper.lerp(partialTicks, prevX, x);
    }

    public static float getPitch(float partialTicks) {
        return partialTicks == 1.0F ? y : MathHelper.lerp(partialTicks, prevY, y);
    }
}
