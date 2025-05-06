package io.hynix.units.impl.display;

import com.google.common.eventbus.Subscribe;

import io.hynix.managers.friend.FriendManager;
import io.hynix.events.impl.EventRender3D;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.units.impl.combat.BotRemover;
import io.hynix.units.settings.impl.BooleanSetting;
import io.hynix.units.settings.impl.ModeListSetting;
import io.hynix.utils.player.EntityUtils;
import io.hynix.utils.johon0.render.color.ColorUtils;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;

import static org.lwjgl.opengl.GL11.*;

@UnitRegister(name = "Tracers", category = Category.Display, desc = "Рисует линии до Entity")
public class Tracers extends Unit {

    public ModeListSetting targets = new ModeListSetting("Отображать",
            new BooleanSetting("Игроки", true),
            new BooleanSetting("Предметы", false),
            new BooleanSetting("Мобы", false)
    );

    public Tracers() {
        addSettings(targets);
    }

    @Subscribe
    public void onRender(EventRender3D e) {
        glPushMatrix();

        glDisable(GL_TEXTURE_2D);
        glDisable(GL_DEPTH_TEST);

        glEnable(GL_BLEND);
        glEnable(GL_LINE_SMOOTH);

        glLineWidth(1);

        Vector3d cam = new Vector3d(0, 0, 150)
                .rotatePitch((float) -(Math.toRadians(mc.getRenderManager().info.getPitch())))
                .rotateYaw((float) -Math.toRadians(mc.getRenderManager().info.getYaw()));

        for (Entity entity : mc.world.getAllEntities()) {
            if (!(entity instanceof PlayerEntity && entity != mc.player && targets.is("Игроки").getValue()
                    || entity instanceof ItemEntity && targets.is("Предметы").getValue()
                    || (entity instanceof AnimalEntity || entity instanceof MobEntity) && targets.is("Мобы").getValue()
            )) continue;
            if (BotRemover.isBot(entity) || !entity.isAlive()) continue;

            Vector3d pos = EntityUtils.getInterpolatedPositionVec(entity).subtract(mc.getRenderManager().info.getProjectedView());

            ColorUtils.setColor(FriendManager.isFriend(entity.getName().getString()) ? FriendManager.getColor() : -1);

            buffer.begin(1, DefaultVertexFormats.POSITION);

            buffer.pos(cam.x, cam.y, cam.z).endVertex();
            buffer.pos(pos.x, pos.y, pos.z).endVertex();


            tessellator.draw();
        }

        glDisable(GL_BLEND);
        glDisable(GL_LINE_SMOOTH);

        glEnable(GL_TEXTURE_2D);
        glEnable(GL_DEPTH_TEST);

        glPopMatrix();
    }
}
