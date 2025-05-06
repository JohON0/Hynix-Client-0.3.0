package io.hynix.units.impl.display;

import com.google.common.eventbus.Subscribe;
import io.hynix.events.impl.EventRender2D;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.utils.johon0.animations.easing.CompactAnimation;
import io.hynix.utils.johon0.animations.easing.Easing;
import io.hynix.utils.johon0.math.MathUtils;

import io.hynix.utils.johon0.render.color.ColorUtils;
import io.hynix.utils.johon0.render.render2d.ProjectionUtils;
import io.hynix.utils.johon0.render.render2d.RenderUtils;
import io.hynix.utils.text.font.ClientFonts;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

import java.util.HashMap;
import java.util.Map;

@UnitRegister(name = "TNTTimer", category = Category.Display, desc = "Показывает когда взорвется Динамит")
public class TntTimer extends Unit {

    private Map<String, CompactAnimation> animations = new HashMap<>();

    @Subscribe
    public void onDisplay(EventRender2D e) {
        for (Entity entity : mc.world.getAllEntities()) {
            if (entity instanceof TNTEntity tnt) {
                final String name = MathUtils.round(tnt.getFuse() / 20.0F, 1) + " сек.";
                Vector3d pos = ProjectionUtils.interpolate(tnt, e.getPartialTicks());
                Vector2f vec = ProjectionUtils.project(pos.x, pos.y + tnt.getHeight() + 0.5, pos.z);
                if (vec == null) return;

                float iconSize = 10;
                float width = ClientFonts.comfortaa[14].getWidth(name) + 4 + iconSize;
                float height = ClientFonts.comfortaa[14].getFontHeight();

                CompactAnimation easing = animations.getOrDefault(tnt.getDisplayName().getString(), null);
                if (easing == null) {
                    easing = new CompactAnimation(Easing.EASE_IN_OUT_CUBIC, 250);
                    animations.put(tnt.getDisplayName().getString(), easing);
                }

                boolean tntActive = tnt.getFuse() > 10;
                easing.run(tntActive ? 1 : 0);

                float x = (float) vec.x;
                float y = (float) vec.y;

                int black = ColorUtils.setAlpha(ColorUtils.rgb(10, 10, 10), (int) (140 * easing.getValue()));
                RenderUtils.drawImage(new ResourceLocation("hynix/images/modules/timers/tnt.png"), (x - width / 2), y, iconSize , iconSize, ColorUtils.setAlpha(-1, (int) (255 * easing.getValue())));
                RenderUtils.drawRoundedRect((x - width / 2 - 2), (float) y - 2, (float) (width) + 4, (float) (height) + 4, 2, black);
                ClientFonts.comfortaa[14].drawCenteredString(e.getMatrixStack(), name, (x - width / 2 + iconSize * 2 + 8), y + 2.5f, ColorUtils.setAlpha(-1, (int) (255 * easing.getValue())));
            }
        }
    }

}
