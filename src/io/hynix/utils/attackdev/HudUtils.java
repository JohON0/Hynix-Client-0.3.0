package io.hynix.utils.attackdev;

import com.mojang.blaze3d.systems.RenderSystem;
import io.hynix.units.impl.combat.AttackAura;
import io.hynix.utils.client.IMinecraft;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import java.util.Collection;
import java.util.Iterator;

public class HudUtils implements IMinecraft {

    public static String calculateBPS() {
        double deltaX = mc.player.getPosX() - mc.player.prevPosX;
        double deltaZ = mc.player.getPosZ() - mc.player.prevPosZ;
        double deltaY = mc.player.getPosY() - mc.player.prevPosY;
        double distance = Math.hypot(deltaX, deltaZ);
        distance = Math.hypot(distance, deltaY);
        return String.format("%.2f", distance * (double)mc.timer.timerSpeed * 20.0);
    }

    public static String calculateBPSTargetStr() {
        if (AttackAura.getTarget() != null) {
            double deltaX = AttackAura.getTarget().getPosX() - AttackAura.getTarget().prevPosX;
            double deltaZ = AttackAura.getTarget().getPosZ() - AttackAura.getTarget().prevPosZ;
            double deltaY = AttackAura.getTarget().getPosY() - AttackAura.getTarget().prevPosY;
            double distance = Math.hypot(deltaX, deltaZ);
            distance = Math.hypot(distance, deltaY);
            return String.format("%.2f", distance * (double)mc.timer.timerSpeed * 20.0);
        } else {
            return "unknown";
        }
    }

    public static int calculateBPSTarget() {
        if (AttackAura.getTarget() != null) {
            double deltaX = AttackAura.getTarget().getPosX() - AttackAura.getTarget().prevPosX;
            double deltaZ = AttackAura.getTarget().getPosZ() - AttackAura.getTarget().prevPosZ;
            double deltaY = AttackAura.getTarget().getPosY() - AttackAura.getTarget().prevPosY;
            double distance = Math.hypot(deltaX, deltaZ);
            distance = Math.hypot(distance, deltaY);
            return (int)(distance * (double)mc.timer.timerSpeed * 20.0);
        } else {
            return 0;
        }
    }

    public static String calculatePingTarget() {
        if (AttackAura.getTarget() != null) {
            ClientPlayNetHandler connection = mc.player.connection;
            Collection<NetworkPlayerInfo> playerInfoList = connection.getPlayerInfoMap();
            Iterator var2 = playerInfoList.iterator();

            while(var2.hasNext()) {
                NetworkPlayerInfo playerInfo = (NetworkPlayerInfo)var2.next();
                String playerName = playerInfo.getGameProfile().getName();
                int ping = playerInfo.getResponseTime();
                if (playerName.equals(AttackAura.getTarget().getName().getString())) {
                    return "" + ping;
                }
            }
        }

        return "0";
    }

    public static String getTargetItem() {
        if (AttackAura.getTarget() != null) {
            LivingEntity var1 = AttackAura.getTarget();
            if (var1 instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity)var1;
                ItemStack stack = player.getHeldItemOffhand();
                String itemName = stack.getDisplayName().getString();
                return itemName;
            }
        }

        return "неизвестно";
    }

    public static int calculateWinrate() {
        if (AttackAura.getTarget() != null) {
            double playerHealth = (double)mc.player.getHealth();
            double targetHealth = (double) AttackAura.getTarget().getHealth();
            double playerMaxHealth = (double)mc.player.getMaxHealth();
            double targetMaxHealth = (double)AttackAura.getTarget().getMaxHealth();
            int playerHealthPercent = (int)(playerHealth / playerMaxHealth);
            int targetHealthPercent = (int)(targetHealth / targetMaxHealth);
            int victoryPercentage = playerHealthPercent / (playerHealthPercent + targetHealthPercent) * 100;
            return victoryPercentage;
        } else {
            return 100;
        }
    }

    public static void drawItemStack(ItemStack stack, float x, float y, boolean withoutOverlay, boolean scale, float scaleValue) {
        RenderSystem.pushMatrix();
        RenderSystem.translatef(x, y, 0.0F);
        if (scale) {
            GL11.glScaled((double)scaleValue, (double)scaleValue, (double)scaleValue);
        }

        mc.getItemRenderer().renderItemAndEffectIntoGUI(stack, 0, 0);
        if (withoutOverlay) {
            mc.getItemRenderer().renderItemOverlays(mc.fontRenderer, stack, 0, 0);
        }

        RenderSystem.popMatrix();
    }

    public static int calculate() {
        return mc.player.connection.getPlayerInfo(mc.player.getUniqueID()) != null ? mc.player.connection.getPlayerInfo(mc.player.getUniqueID()).getResponseTime() : 0;
    }

    public static String serverIP() {
        return mc.getCurrentServerData() != null && mc.getCurrentServerData().serverIP != null && !mc.isSingleplayer() ? mc.getCurrentServerData().serverIP : "";
    }

}
