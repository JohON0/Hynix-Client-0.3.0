package io.hynix.utils.johon0.math;

import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

import static java.lang.Math.abs;
import static java.lang.Math.signum;
import static net.minecraft.util.math.MathHelper.clamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

import io.hynix.utils.client.IMinecraft;
import org.joml.Vector2d;

@UtilityClass
public class MathUtils implements IMinecraft {
    private static final Random random = new Random();

    public static <T extends Number> T lerp(T input, T target, double step) {
        double start = input.doubleValue();
        double end = target.doubleValue();
        double result = start + step * (end - start);


        if (input instanceof Integer) {
            return (T) Integer.valueOf((int) Math.round(result));
        } else if (input instanceof Double) {
            return (T) Double.valueOf(result);
        } else if (input instanceof Float) {
            return (T) Float.valueOf((float) result);
        } else if (input instanceof Long) {
            return (T) Long.valueOf(Math.round(result));
        } else if (input instanceof Short) {
            return (T) Short.valueOf((short) Math.round(result));
        } else if (input instanceof Byte) {
            return (T) Byte.valueOf((byte) Math.round(result));
        } else {
            throw new IllegalArgumentException("Unsupported type: " + input.getClass().getSimpleName());
        }
    }

    public static double getDifferenceOf(float num1, float num2) {
        return Math.abs(num2 - num1) > Math.abs(num1 - num2) ? (double)Math.abs(num1 - num2) : (double)Math.abs(num2 - num1);
    }
    public static double getRandom(double min, double max) {
        return min + (max - min) * random.nextDouble();
    }
    public static double getDifferenceOf(double num1, double num2) {
        return Math.abs(num2 - num1) > Math.abs(num1 - num2) ? Math.abs(num1 - num2) : Math.abs(num2 - num1);
    }

    public static double getDifferenceOf(int num1, int num2) {
        return Math.abs(num2 - num1) > Math.abs(num1 - num2) ? (double)Math.abs(num1 - num2) : (double)Math.abs(num2 - num1);
    }

    public float normalize(float value, float min, float max) {
        return (value - min) / (max - min);
    }

    public float clamp01(float x) {
        return (float) clamp(0, 1, x);
    }

    public double interporate(double p_219803_0_, double p_219803_2_, double p_219803_4_) {
        return p_219803_2_ + p_219803_0_ * (p_219803_4_ - p_219803_2_);
    }

    private double armor(ItemStack stack) {
        if (!stack.isEnchanted()) return 0.0;
        if (!(stack.getItem() instanceof ArmorItem armor)) return 0.0;

        return armor.getDamageReduceAmount() + ((double) EnchantmentHelper.getEnchantmentLevel(Enchantments.PROTECTION, stack) * 0.25);
    }

    public double armor(LivingEntity entity) {
        double armor = entity.getTotalArmorValue();

        for (ItemStack item : entity.getArmorInventoryList()) {
            armor += armor(item);
        }

        return armor;
    }

    public double health(LivingEntity entity) {
        return entity.getHealth() + entity.getAbsorptionAmount();
    }

    public double buffs(LivingEntity entity) {
        double buffs = 0.0;

        for (EffectInstance effect : entity.getActivePotionEffects()) {
            if (effect.getPotion() == Effects.ABSORPTION) {
                buffs += 1.2 * (effect.getAmplifier() + 1);
            } else if (effect.getPotion() == Effects.RESISTANCE) {
                buffs += 1.0 * (effect.getAmplifier() + 1);
            } else if (effect.getPotion() == Effects.REGENERATION) {
                buffs += 1.1 * (effect.getAmplifier() + 1);
            }
        }

        return buffs;
    }

    public double entity(LivingEntity entity, boolean health, boolean armor, boolean distance, double maxDistance, boolean buffs) {
        double a = 1.0, b = 1.0, c = 1.0, d = 1.0;

        if (health) a += health(entity);
        if (armor) b += armor(entity);
        if (distance) c += entity.getDistanceSq(Minecraft.getInstance().player) / maxDistance;
        if (buffs) d += buffs(entity);

        return (a * b * d) * c;
    }

    public static Vector3d interpolatePos(float oldx, float oldy, float oldz, float x, float y, float z) {
        double getx = (double) (oldx + (x - oldx) * mc.getRenderPartialTicks()) - mc.getRenderManager().info.getProjectedView().getX();
        double gety = (double) (oldy + (y - oldy) * mc.getRenderPartialTicks()) - mc.getRenderManager().info.getProjectedView().getY();
        double getz = (double) (oldz + (z - oldz) * mc.getRenderPartialTicks()) - mc.getRenderManager().info.getProjectedView().getZ();
        return new Vector3d(getx, gety, getz);
    }

    private final int SCALE = 2;

    public Vector2d getMouse(double mouseX, double mouseY) {
        return new Vector2d(mouseX * mc.getMainWindow().getScaleFactor() / SCALE, mouseY * mc.getMainWindow().getScaleFactor() / SCALE);
    }

    public static Vector2i getMouse2i(int mouseX, int mouseY) {
        return new Vector2i((int)((double)mouseX * Minecraft.getInstance().getMainWindow().getGuiScaleFactor() / 2.0), (int)((double)mouseY * Minecraft.getInstance().getMainWindow().getGuiScaleFactor() / 2.0));
    }

    public static Vector3d getVector(LivingEntity target) {

        double wHalf = target.getWidth() / 2;

        double yExpand = clamp(target.getPosYEye() - target.getPosY(), 0, target.getHeight());

        double xExpand = clamp(mc.player.getPosX() - target.getPosX(), -wHalf, wHalf);
        double zExpand = clamp(mc.player.getPosZ() - target.getPosZ(), -wHalf, wHalf);

        return new Vector3d(
                target.getPosX() - mc.player.getPosX() + xExpand,
                target.getPosY() - mc.player.getPosYEye() + yExpand,
                target.getPosZ() - mc.player.getPosZ() + zExpand
        );
    }

    public static int randomInt(int min, int max) {
        if (min >= max) {
            System.out.println("Ну ты и дцп...");
            return -1;
        }

        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }

    public double interpolate(double current, double old, double scale) {
        return old + (current - old) * scale;
    }

    // is hovered
    public boolean isHovered(float mouseX, float mouseY, float x,float y,float width,float height) {
        
        return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
    }
    
    public static int calculatePing() {
        return mc.player.connection.getPlayerInfo(mc.player.getUniqueID()) != null ?
                mc.player.connection.getPlayerInfo(mc.player.getUniqueID()).getResponseTime() : 0;
    }

    public static float random(float min, float max) {
        return (float) (Math.random() * (max - min) + min);
    }

    public static double randomWithUpdate(double min, double max, long ms, TimerUtils timerUtility) {
        double randomValue = 0;

        if (timerUtility.isReached(ms)) {
            randomValue = random((float) min, (float) max);
            timerUtility.reset();
        }

        return randomValue;
    }

    public Vector2f rotationToVec(Vector3d vec) {
        Vector3d eyesPos = mc.player.getEyePosition(1.0f);
        double diffX = vec != null ? vec.x - eyesPos.x : 0;
        double diffY = vec != null ? vec.y - (mc.player.getPosY() + (double) mc.player.getEyeHeight() + 0.5) : 0;
        double diffZ = vec != null ? vec.z - eyesPos.z : 0;

        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        float yaw = (float) (Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0);
        float pitch = (float) (-Math.toDegrees(Math.atan2(diffY, diffXZ)));
        yaw = mc.player.rotationYaw + MathHelper.wrapDegrees(yaw - mc.player.rotationYaw);
        pitch = mc.player.rotationPitch + MathHelper.wrapDegrees(pitch - mc.player.rotationPitch);
        pitch = MathHelper.clamp(pitch, -90.0f, 90.0f);

        return new Vector2f(yaw, pitch);
    }

    public static Vector2f rotationToEntity(Entity target) {
    	if (target == null) return null;
        Vector3d vector3d = target.getPositionVec().subtract(Minecraft.getInstance().player.getPositionVec());
        double magnitude = Math.hypot(vector3d.x, vector3d.z);
        return new Vector2f(
                (float) Math.toDegrees(Math.atan2(vector3d.z, vector3d.x)) - 90.0F,
                (float) (-Math.toDegrees(Math.atan2(vector3d.y, magnitude))));
    }

    public Vector2f rotationToVec(Vector2f rotationVector, Vector3d target) {
        double x = target.x - mc.player.getPosX();
        double y = target.y - mc.player.getEyePosition(1).y;
        double z = target.z - mc.player.getPosZ();
        double dst = Math.sqrt(Math.pow(x, 2) + Math.pow(z, 2));
        float yaw = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(z, x)) - 90);
        float pitch = (float) (-Math.toDegrees(Math.atan2(y, dst)));
        float yawDelta = MathHelper.wrapDegrees(yaw - rotationVector.x);
        float pitchDelta = (pitch - rotationVector.y);

        if (abs(yawDelta) > 180)
            yawDelta -= signum(yawDelta) * 360;

        return new Vector2f(yawDelta, pitchDelta);
    }

    // round
    public double round(double num, double increment) {
        double v = (double) Math.round(num / increment) * increment;
        BigDecimal bd = new BigDecimal(v);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    // distance
    public double distance(double x1, double y1, double z1, double x2, double y2, double z2) {
        double d0 = x1 - x2;
        double d1 = y1 - y2;
        double d2 = z1 - z2;
        return Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
    }

    public double distance(double x1, double y1, double x2, double y2) {
        double x = x1 - x2;
        double y = y1 - y2;
        return Math.sqrt(x * x + y * y);
    }

    public double deltaTime() {
        return mc.debugFPS > 0 ? (1.0000 / mc.debugFPS) : 1;
    }

    public float fast(float end, float start, float multiple) {
        return (1 - MathHelper.clamp((float) (deltaTime() * multiple), 0, 1)) * end
                + MathHelper.clamp((float) (deltaTime() * multiple), 0, 1) * start;
    }

    public Vector3d interpolate(Vector3d end, Vector3d start, float multiple) {
        return new Vector3d(
                interpolate(end.getX(), start.getX(), multiple),
                interpolate(end.getY(), start.getY(), multiple),
                interpolate(end.getZ(), start.getZ(), multiple));
    }

    public org.joml.Vector2f interpolate(org.joml.Vector2f end, org.joml.Vector2f start, float multiple) {
        return new org.joml.Vector2f(
                (float) interpolate(end.x, start.x, multiple),
                (float) interpolate(end.y, start.y, multiple));
    }

    public Vector3d fast(Vector3d end, Vector3d start, float multiple) {
        return new Vector3d(
                fast((float) end.getX(), (float) start.getX(), multiple),
                fast((float) end.getY(), (float) start.getY(), multiple),
                fast((float) end.getZ(), (float) start.getZ(), multiple));
    }

    public static org.joml.Vector2f fast(org.joml.Vector2f end, org.joml.Vector2f start, float multiple) {
        return new org.joml.Vector2f(
                fast((float) end.x, (float) start.x, multiple),
                fast((float) end.y, (float) start.y, multiple));
    }

    public float lerp(float end, float start, float multiple) {
        return (float) (end + (start - end) * MathHelper.clamp(deltaTime() * multiple, 0, 1));
    }

    public double lerp(double end, double start, double multiple) {
        return (end + (start - end) * MathHelper.clamp(deltaTime() * multiple, 0, 1));
    }

    public static float calculateDelta(float a, float b) {
        return a - b;
    }
}
