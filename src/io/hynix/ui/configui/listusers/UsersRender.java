package io.hynix.ui.configui.listusers;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.hynix.ui.clickgui.ClickGui;
import io.hynix.utils.client.IMinecraft;
import io.hynix.utils.johon0.math.MathUtils;
import io.hynix.utils.johon0.math.Vector4i;
import io.hynix.utils.johon0.render.color.ColorUtils;
import io.hynix.utils.johon0.render.other.Scissor;
import io.hynix.utils.johon0.render.render2d.RenderUtils;
import io.hynix.utils.text.font.ClientFonts;
import lombok.Getter;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

public class UsersRender implements IMinecraft {
    private UserConfigList userConfigList;
//    @Getter
//    LivingEntity entity = mc.player;
    public UsersRender(UserConfigList userConfigList) {
        this.userConfigList = userConfigList;
    }

    public void renderUserConfigs(MatrixStack matrixStack, float x, float y, float mouseX, float mouseY) {
        // Начальное смещение по Y для строк
        int offset = 20;

        // Рендерим каждую строку конфигурации
        for (Users config : userConfigList.getConfigs()) {
            // Рендерим цветной круг рядом с текстом
            RenderUtils.drawRoundedRect(x, y + offset + 5, 290, 40, 2, ClickGui.backgroundColor);
            ClientFonts.tenacityBold[20].drawString(matrixStack, config.getMessage(), x + 40, y + offset + 10, ClickGui.textcolor);

            ClientFonts.tenacity[12].drawString(matrixStack, "Содержит конфиги "
                            + TextFormatting.RED + "F" + (ClickGui.themelightofdark ? TextFormatting.BLACK : TextFormatting.WHITE) + "T"
                            + (ClickGui.themelightofdark ? TextFormatting.BLACK : TextFormatting.WHITE) + ", "
                            + TextFormatting.GOLD + "RW"
                            + (ClickGui.themelightofdark ? TextFormatting.BLACK : TextFormatting.WHITE) + ", "
                            + TextFormatting.BLUE + "HW"
                            + (ClickGui.themelightofdark ? TextFormatting.BLACK : TextFormatting.WHITE) + ", "
                            + TextFormatting.RED + "S" + (ClickGui.themelightofdark ? TextFormatting.BLACK : TextFormatting.WHITE) + "H"
                    , x + 40, y + offset + 23, ClickGui.textcolor);

//            //skin
//            String rs = EntityType.getKey(((Entity)entity).getType()).getPath();
//            ResourceLocation skin = entity instanceof AbstractClientPlayerEntity e ? (e).getLocationSkin() : new ResourceLocation("textures/entity/"+rs+".png");
//            try {
//                mc.getResourceManager().getResource(skin);
//            } catch(Exception e){
//                if(! (entity instanceof AbstractClientPlayerEntity))
//                {
//                    skin = new ResourceLocation("textures/entity/"+rs+"/"+rs+".png");
//                }
//
//            }
            RenderUtils.drawImage(new ResourceLocation("hynix/images/avatar/johon0.png"),x, y + offset + 5,35,35,-1);

            RenderUtils.drawRoundedRect(x + 235, y + offset + 15, 50, 20, 2, ClickGui.modescolor);
            int colortext;
            if (MathUtils.isHovered(mouseX,mouseY, x + 235, y + offset + 15, 50, 20)) {
                colortext = ClickGui.textcolor;
            } else {
                colortext = ClickGui.lighttextcolor;
            }
            ClientFonts.tenacityBold[16].drawString(matrixStack,"Скачать", x + 244, y + offset + 22, colortext);


            offset += 42; // Увеличиваем смещение для следующей строки
        }
    }

}
