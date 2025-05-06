package io.hynix.command.feature;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.platform.GlStateManager;
import io.hynix.HynixMain;
import io.hynix.command.api.CommandException;
import io.hynix.command.interfaces.*;
import io.hynix.events.impl.EventRender2D;
import io.hynix.units.api.UnitManager;
import io.hynix.units.impl.miscellaneous.Unhook;
import io.hynix.units.impl.display.Crosshair;
import io.hynix.utils.johon0.animations.AnimationUtils;
import io.hynix.utils.johon0.animations.impl.DecelerateAnimation;
import io.hynix.utils.client.IMinecraft;

import io.hynix.utils.johon0.render.color.ColorUtils;
import io.hynix.utils.johon0.render.render2d.RenderUtils;
import io.hynix.utils.text.font.ClientFonts;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class GPSCommand implements Command, CommandWithAdvice, IMinecraft {
    final Prefix prefix;
    final Logger logger;
    Vector2f cordsMap = new Vector2f(0, 0);
    private final AnimationUtils alpha = new DecelerateAnimation(255, 255);
    public GPSCommand(Prefix prefix, Logger logger) {
        this.prefix = prefix;
        this.logger = logger;
        HynixMain.getInstance().getEventBus().register(this);
    }

    @Override
    public void execute(Parameters parameters) {
        String commandType = parameters.asString(0).orElse("");

        switch (commandType) {
            case "add" -> addGPS(parameters);
            case "off" -> removeGPS();
            default ->
                    throw new CommandException(TextFormatting.RED + "Укажите тип команды:" + TextFormatting.GRAY + " add, off");
        }
    }

    private void addGPS(Parameters param) {
        int x = param.asInt(1).orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите первую координату!"));
        int z = param.asInt(2).orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите вторую координату!"));

        if (x == 0 && z == 0) {
            logger.log("Координаты должны быть больше нуля.");
            return;
        }

        cordsMap = new Vector2f(x, z);

    }

    private void removeGPS() {
        cordsMap = new Vector2f(0, 0);
    }

    @Override
    public String name() {
        return "gps";
    }

    @Override
    public String description() {
        return "Показывает стрелочку которая ведёт к координатам";
    }

    @Override
    public List<String> adviceMessage() {
        String commandPrefix = prefix.get();
        return List.of(commandPrefix + "gps add <x, z> - Проложить путь",
                commandPrefix + "gps off - Удалить GPS",
                "Пример: " + TextFormatting.RED + commandPrefix + "gps add 500 152"
        );
    }

    @Subscribe
    private void onDisplay(EventRender2D e) {
        UnitManager moduleManager = HynixMain.getInstance().getModuleManager();
        Unhook selfDestruct = moduleManager.getSelfDestruct();

        if (selfDestruct.unhooked || cordsMap.x == 0 && cordsMap.y == 0) {
            return;
        }

        Vector3d vec3d = new Vector3d(cordsMap.x + 0.5, 100 + 0.5, cordsMap.y + 0.5);
        int dst = (int) Math.sqrt(Math.pow(vec3d.x - mc.player.getPosX(), 2) + Math.pow(vec3d.z - mc.player.getPosZ(), 2));

        String text = dst + "M";
        Vector3d localVec = vec3d.subtract(mc.getRenderManager().info.getProjectedView());

        double x = localVec.getX();
        double z = localVec.getZ();

        double cos = MathHelper.cos((float) (mc.getRenderManager().info.getYaw() * (Math.PI * 2 / 360)));
        double sin = MathHelper.sin((float) (mc.getRenderManager().info.getYaw() * (Math.PI * 2 / 360)));
        double rotY = -(z * cos - x * sin);
        double rotX = -(x * cos + z * sin);

        float angle = (float) (Math.atan2(rotY, rotX) * 180 / Math.PI);

        Crosshair crosshair = HynixMain.getInstance().getModuleManager().getCrosshair();

        double x2 = 10 * MathHelper.cos((float) Math.toRadians(angle)) + mc.getMainWindow().getScaledWidth() / 2f;
        double y2 = 10 * MathHelper.sin((float) Math.toRadians(angle)) + mc.getMainWindow().getScaledHeight() / 3.5f;

        GlStateManager.pushMatrix();
        GlStateManager.disableBlend();
        GlStateManager.translated(x2, y2, 0);
        ClientFonts.tenacity[14].drawCenteredString(e.getMatrixStack(), text, 0, -15, ColorUtils.setAlpha(-1, 255));
        GlStateManager.rotatef(angle, 0, 0, 1);
        RenderUtils.drawImage(new ResourceLocation("hynix/images/triangle.png"), -4.0F, -8.0F, 16.0F, 16.0F, ColorUtils.setAlpha(-1, 255));
        GlStateManager.enableBlend();
        GlStateManager.popMatrix();
    }
}
