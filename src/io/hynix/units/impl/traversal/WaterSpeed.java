package io.hynix.units.impl.traversal;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.common.eventbus.Subscribe;

import io.hynix.events.impl.EventMoving;
import io.hynix.events.impl.EventUpdate;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.units.settings.impl.BooleanSetting;
import io.hynix.units.settings.impl.ModeSetting;
import io.hynix.units.settings.impl.SliderSetting;
import io.hynix.utils.player.MoveUtils;
import io.hynix.utils.player.StrafeMovement;
import net.minecraft.block.AirBlock;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.AirItem;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effects;

@UnitRegister(name = "WaterSpeed", category = Category.Traversal, desc = "Ускорение, когда плывешь в воде")
public class WaterSpeed extends Unit {

    private ModeSetting mode = new ModeSetting("Обход", "Matrix", "Matrix", "Grim", "Funtime");
    private SliderSetting speed = new SliderSetting("Скорость", 4f, 1f, 10f, 0.1f).setVisible(() -> mode.is("Grim"));
    private StrafeMovement strafeMovement = new StrafeMovement();
    private BooleanSetting smartWork = new BooleanSetting("Умный", true).setVisible(() -> mode.is("Matrix"));

    int tick;

    public WaterSpeed() {
        addSettings(mode, speed, smartWork);
    }

    @Subscribe
    public void onPlayer(EventMoving e) {
        if (mode.is("Grim")) {
            if (mc.player.isInWater() && MoveUtils.isMoving()) {
                if (mc.gameSettings.keyBindJump.isKeyDown() && mc.gameSettings.keyBindSneak.isKeyDown()) {
                    mc.player.motion.y = 0;
                }

                mc.player.setSwimming(true);

                float moveSpeed = speed.getValue() + new Random().nextFloat() * 1.1f;
                moveSpeed /= 100.0f;

                double moveX = mc.player.getForward().x * moveSpeed;
                double moveZ = mc.player.getForward().z * moveSpeed;

                mc.player.addVelocity(moveX, 0, moveZ);
            }
        }
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mode.is("Funtime")) {
            if (mc.player != null && mc.player.isAlive()) {
                if (mc.player.isInWater()) {
                    mc.player.setMotion(mc.player.getMotion().x * (double) 1.0505, mc.player.getMotion().y, mc.player.getMotion().z * (double) 1.0505);
                }
            }
        }

        if (mode.is("Matrix")) {
            List<ItemStack> stacks = new ArrayList<>();
            mc.player.getArmorInventoryList().forEach(stacks::add);
            stacks.removeIf(w -> w.getItem() instanceof AirItem);
            float motion = (float) MoveUtils.getMotion();
            boolean hasEnchantments = false;
            for (ItemStack stack : stacks) {

                int enchantmentLevel = 0;

                if (buildEnchantments(stack, 1)) {
                    enchantmentLevel = 1;
                }

                if (enchantmentLevel > 0) {
                    motion = 0.5f;
                    hasEnchantments = true;
                }
            }

            if (mc.player.collidedHorizontally) {
                tick = 0;
                return;
            }
            if (!mc.player.isInWater()) return;
            if (mc.gameSettings.keyBindJump.isKeyDown() && !mc.player.isSneaking() && !(mc.world.getBlockState(mc.player.getPosition().add(0, 1, 0)).getBlock() instanceof AirBlock)) {
                mc.player.motion.y = 0.12f;
            }
            if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                mc.player.motion.y = -0.35f;
            }

            if (smartWork.getValue()) {
                if (!mc.player.isPotionActive(Effects.SPEED)) {
                    tick = 0;
                    return;
                }

                if (!hasEnchantments) {
                    return;
                }
            }

            if (mc.world.getBlockState(mc.player.getPosition().add(0, 1, 0)).getBlock() instanceof AirBlock && mc.gameSettings.keyBindJump.isKeyDown()) {
                tick++;
                mc.player.motion.y = 0.12f;
            }
            tick++;
            MoveUtils.setMotion(0.4f);
            strafeMovement.setOldSpeed(0.4);
        }
    }

    public boolean buildEnchantments(ItemStack stack, float strenght) {
        if (stack != null) {
            if (stack.getItem() instanceof ArmorItem) {
                return EnchantmentHelper.getEnchantmentLevel(Enchantments.DEPTH_STRIDER, stack) > 0;
            }
        } else {
            return false;
        }

        return false;
    }
}
