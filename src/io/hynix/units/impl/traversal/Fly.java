package io.hynix.units.impl.traversal;

import com.google.common.eventbus.Subscribe;

import io.hynix.HynixMain;
import io.hynix.events.impl.EventMotion;
import io.hynix.events.impl.EventPacket;
import io.hynix.events.impl.EventUpdate;
import io.hynix.events.impl.EventMoving;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.units.settings.impl.BooleanSetting;
import io.hynix.units.settings.impl.ModeListSetting;
import io.hynix.units.settings.impl.ModeSetting;
import io.hynix.units.settings.impl.SliderSetting;
import io.hynix.utils.johon0.math.TimerUtils;
import io.hynix.utils.player.InventoryUtils;
import io.hynix.utils.player.MoveUtils;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;

@UnitRegister(name = "Fly", category = Category.Traversal, desc = "Полет")
public class Fly extends Unit {

    private final ModeSetting mode = new ModeSetting("Мод", "Vanilla", "Vanilla", "MatrixJump", "MatrixGlide",
            "MatrixChunk", "MatrixUp", "AAC", "NCP", "Grim", "BlockPlace");

    // speed
    private final SliderSetting horizontal = new SliderSetting("По горизонтали", 1f, 0f, 5f, 0.1f).setVisible(() -> !mode.is("BlockPlace") && !mode.is("FunTime") && !mode.is("AAC") && !mode.is("Grim") && !mode.is("NCP"));
    private final SliderSetting vertical = new SliderSetting("По вертикали", 0.5f, 0f, 5f, 0.1f).setVisible(() -> !mode.is("BlockPlace") && !mode.is("FunTime") && !mode.is("AAC") && !mode.is("Grim") && !mode.is("MatrixChunk") && !mode.is("NCP"));

    // vanilla
    private BooleanSetting superbow = new BooleanSetting("Дергать Y", false).setVisible(() -> mode.is("Vanilla"));

    // block place
    final ModeListSetting options = new ModeListSetting("Опции", new BooleanSetting("Авто прыжок", false), new BooleanSetting("Ротация", true)).setVisible(() -> mode.is("BlockPlace"));
    private final ModeSetting speedMode = new ModeSetting("Тип скорости", "Vanilla", "Vanilla", "Motion").setVisible(() -> this.mode.is("BlockPlace"));
    public SliderSetting vanillaSpeed = new SliderSetting("Скорость Vanilla", 1f, 1.f, 2f, 0.01f).setVisible(() -> this.mode.is("BlockPlace") && this.speedMode.is("Vanilla"));
    public SliderSetting motionSpeed = new SliderSetting("Скорость Motion", 0.3f, 0.1f, 1.0f, 0.01f).setVisible(() -> this.mode.is("BlockPlace") && this.speedMode.is("Motion"));

    // matrix chunk
    public BooleanSetting upWard = new BooleanSetting("Вверх", true).setVisible(() -> mode.is("MatrixChunk"));
    public BooleanSetting downWard = new BooleanSetting("Вниз", true).setVisible(() -> mode.is("MatrixChunk"));
    public BooleanSetting autoUp = new BooleanSetting("Поднимать", true).setVisible(() -> mode.is("MatrixChunk"));
    public BooleanSetting smoothSpeed = new BooleanSetting("Плавная скорость", true).setVisible(() -> mode.is("MatrixChunk"));
    public BooleanSetting noVanillaKick = new BooleanSetting("Vanilla Disabler", true).setVisible(() -> mode.is("MatrixChunk"));

    public Fly() {
        addSettings(mode, horizontal, vertical, superbow, options, speedMode, vanillaSpeed, motionSpeed, upWard, downWard, autoUp, smoothSpeed, noVanillaKick);
    }

    public Entity vehicle;
    int tickerFinalling;
    boolean enableGround;
    TimerUtils timeFlying = new TimerUtils();

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mc.player == null || mc.world == null || HynixMain.getInstance().getModuleManager().getFreeCam().isEnabled())
            return;

        if (options.is("Авто прыжок").getValue() && mc.player.isOnGround() && mode.is("BlockPlace") && !MoveUtils.isMoving()) {
            mc.player.jump();
        }

        switch (mode.getValue()) {
            case "Vanilla" -> {
                updatePlayerMotion();
            }

            case "MatrixUp" -> {
                if (mc.player != null && mc.player.isAlive()) {
                    mc.player.setMotion(mc.player.getMotion().x, mc.player.getMotion().y + 0.05499999761581421D, mc.player.getMotion().z);
                }
            }

            case "NCP" -> {
                boolean up = mc.player.movementInput.jump;
                boolean down = mc.player.isSneaking();
                boolean move = MoveUtils.moveKeysPressed();
                double speed = up || down ? 0.0 : (move ? MoveUtils.getSpeedByBPS(4.010000000000001) : 0.0);
                float moveYaw = (float)Math.toRadians(MoveUtils.moveYaw(mc.player.rotationYaw));
                double sinSpeed = -Math.sin(moveYaw) * speed;
                double cosSpeed = Math.cos(moveYaw) * speed;
                double ySpeed = up ? 0.062 : (down ? -0.102 : (move ? 0.0 : (mc.player.ticksExisted % 2 == 0 && !mc.player.onGround ? 0.01 : -0.01)));
                double pX = mc.player.getPosX() + sinSpeed;
                double pY = mc.player.getPosY() + ySpeed;
                double pZ = mc.player.getPosZ() + cosSpeed;
                MoveUtils.setMotionSpeed(true, false, 0.0);
                MoveUtils.setSpeed((float) (-speed / 5.0));
                mc.player.motion.y = 1.0E-45;
                mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(pX, pY, pZ, false));
                mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(0.0, pY + ySpeed, 0.0, true));
            }

            case "MatrixChunk" -> {
                MoveUtils.setSpeed((float) (smoothSpeed.getValue() ? MoveUtils.getSpeed() / 5.0 : MoveUtils.getSpeed() / 30.0), smoothSpeed.getValue() ? 0.3F : 0.0F);
            }

            case "MatrixJump" -> { // Matrix Jump
                if (mc.player.isOnGround())
                    mc.player.jump();
                else {
                    MoveUtils.setMotion(Math.min(horizontal.getValue(), 1.97f));
                    mc.player.motion.y = vertical.getValue();
                }
            }

            case "MatrixGlide" -> { // Matrix Glide
                MoveUtils.setMotion(horizontal.getValue());
                mc.player.motion.y = -0.003;
            }

            case "BlockPlace" -> {
                if (speedMode.is("Vanilla") && mc.player.isOnGround()) {
                    mc.player.motion.x *= vanillaSpeed.getValue();
                    mc.player.motion.z *= vanillaSpeed.getValue();
                }
                if (speedMode.is("Motion") && mc.player.isOnGround()) {
                    MoveUtils.setMotion(motionSpeed.getValue());
                }
                BlockPos posBelow;
                if (!mc.world.getBlockState(posBelow = mc.player.getPosition().down()).getMaterial().isReplaceable()) break;

                placeBlockUnderPlayer(posBelow, (MoveUtils.isMoving() ? 0.2f : 0.1f));
            }

            case "Grim" -> {
                int crate;
                mc.player.multiplyMotionXZ(0.0F);
                crate = 10;
                float ySpeed = 0.02F * (float)crate;
                mc.player.motion.y = mc.player.isJumping ? (double)ySpeed : (mc.player.isSneaking() ? (double)(-ySpeed) : 0.0);
                float speed = 0.05F * (float)crate;
                MoveUtils.setSpeed(speed);

                for(int i = 0; i < crate; ++i) {
                    mc.player.connection.sendPacket(new CPlayerPacket(false));
                }
            }

            case "AAC" -> {
                if (!mc.player.isJumping && !posBlock(mc.player.getPosX(), mc.player.getPosY() - 0.044, mc.player.getPosZ())) {
                    this.enableGround = false;
                }

                if (mc.player.onGround && mc.player.collidedVertically) {
                    this.enableGround = true;
                }

                double setedMotY = mc.player.isJumping ? (enableGround ? (double)mc.player.getJumpUpwardsMotion() : mc.player.motion.y) : mc.player.motion.y;
                mc.player.motion.y = setedMotY;
                if (MoveUtils.getSpeed() > 0.19) {
                    mc.player.jumpMovementFactor = 0.17F;
                }

                mc.player.multiplyMotionXZ(1.005F);
            }
        }
    }

    @Subscribe
    public void onMove(EventMoving e) {
        if (mc.player == null || mc.world == null || HynixMain.getInstance().getModuleManager().getFreeCam().isEnabled())
            return;

        double motion;
        float curMotion;
        if (mode.is("MatrixChunk") && mc.player.fallDistance != 0.0F) {
            curMotion = horizontal.getValue() / 10.0F;
            motion = smoothSpeed.getValue() ? MoveUtils.getCuttingSpeed() / 1.3 : 0.0;
            if (mc.player.fallDistance != 0.0F || !autoUp.getValue()) {
                motion = (mc.player.isJumping && upWard.getValue() ? 8.2675F : 9.4675F) * curMotion;
            }

            if (!MoveUtils.moveKeysPressed() && smoothSpeed.getValue()) {
                MoveUtils.setSpeed((float) motion, 0.6F);
            } else {
                MoveUtils.setCuttingSpeed(motion / 1.06);
                MoveUtils.setSpeed((float) motion);
            }
        }
    }

    @Subscribe
    public void onMotion(EventMotion e) {
        if (mc.player == null || mc.world == null || HynixMain.getInstance().getModuleManager().getFreeCam().isEnabled())
            return;

        if (options.is("Ротация").getValue() && mode.is("BlockPlace")) {
            if (HynixMain.getInstance().getModuleManager().getAttackAura().isEnabled() && HynixMain.getInstance().getModuleManager().getAttackAura().target != null) {
                return;
            }
            mc.player.rotationPitchHead = 90.0f;
            e.setPitch(90.0f);
        }

        if (mode.is("MatrixChunk")) {
            if (mc.player.onGround && mc.player.fallDistance == 0.0F && autoUp.getValue()) {
                MoveUtils.setSpeed(0);
                mc.player.jumpMovementFactor = 0.0F;
                if (!mc.player.isJumping) {
                    mc.player.motion.y = mc.player.getJumpUpwardsMotion();
                }

                return;
            }

            if (mc.player.fallDistance != 0.0F) {
                ++this.tickerFinalling;
                if (mc.player.isJumping && this.upWard.getValue() && MoveUtils.moveKeysPressed() && MoveUtils.getCuttingSpeed() > 0.1) {
                    mc.player.motion.y = mc.player.fallDistance != 0.0F ? 0.42 : (noVanillaKick.getValue() ? -0.02 : 0.0);
                    if (tickerFinalling % 2 == 0) {
                        mc.player.motion.y = noVanillaKick.getValue() ? -0.05 : -1.0E-6;
                    }
                } else if (!mc.player.isSneaking() || !this.downWard.getValue()) {
                    if (noVanillaKick.getValue() && timeFlying.isReached(50)) {
                        mc.player.motion.y = (0.035 * (float)(this.tickerFinalling % 2 != 1 ? -1 : 1));
                    } else {
                        mc.player.motion.y = 0.0;
                        mc.player.motion.y = -1.0E-6;
                    }
                }
            } else {
                this.tickerFinalling = 0;
                this.timeFlying.reset();
            }
        }
    }

    public void onToggle(boolean actived) {
        if (actived) {
            this.timeFlying.reset();
            if (mode.is("MatrixChunk")) {
                this.tickerFinalling = 0;
            }
        }
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (mc.player == null || mc.world == null || HynixMain.getInstance().getModuleManager().getFreeCam().isEnabled())
            return;

        switch (mode.getValue()) {
            case "MatrixJump" -> { // Matrix Jump
                if (e.getPacket() instanceof SPlayerPositionLookPacket p) {
                    if (mc.player == null) toggle();
                    mc.player.setPosition(p.getX(), p.getY(), p.getZ());
                    mc.player.connection.sendPacket(new CConfirmTeleportPacket(p.getTeleportId()));
                    e.cancel();
                    toggle();
                }
            }
        }
    }

    public static boolean posBlock(double x, double y, double z) {
        return mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.AIR && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.WATER && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.LAVA && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.CAKE && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.TALL_GRASS && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.STONE_BUTTON && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.FLOWER_POT && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.CHORUS_FLOWER && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.FLOWER_POT && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.SUNFLOWER && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.VINE && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.ACACIA_FENCE && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.ACACIA_FENCE_GATE && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.BIRCH_FENCE && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.BIRCH_FENCE_GATE && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.DARK_OAK_FENCE && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.DARK_OAK_FENCE_GATE && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.JUNGLE_FENCE && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.JUNGLE_FENCE_GATE && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.NETHER_BRICK_FENCE && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.OAK_FENCE && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.OAK_FENCE_GATE && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.SPRUCE_FENCE && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.SPRUCE_FENCE_GATE && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.ENCHANTING_TABLE && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.END_PORTAL_FRAME && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.PLAYER_HEAD && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.DAYLIGHT_DETECTOR && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.DAYLIGHT_DETECTOR && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.PURPUR_SLAB && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.STONE_SLAB && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.DEAD_BUSH && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.REDSTONE_WIRE && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.REDSTONE_TORCH && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.TORCH && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.REDSTONE_WIRE && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.WATER && mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() != Blocks.SNOW;
    }

    public static void placeBlockUnderPlayer(BlockPos pos, float fallDistance) {
        int blockSlot = InventoryUtils.findBlockInHotbar();
        if (blockSlot == -1) {
            return;
        }
        if (mc.player.fallDistance > fallDistance) {
            int lastSlot = mc.player.inventory.currentItem;
            mc.player.inventory.currentItem = blockSlot;
            Vector3d vector3d = new Vector3d(pos.getX(), pos.getY(), pos.getZ());
            mc.playerController.processRightClickBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockRayTraceResult(vector3d, Direction.UP, pos,false));
            mc.player.connection.sendPacket(new CAnimateHandPacket(Hand.MAIN_HAND));
            mc.player.inventory.currentItem = lastSlot;
        }
    }

    private void updatePlayerMotion() {
        double motionY = getMotionY();

        MoveUtils.setMotion(horizontal.getValue());
        mc.player.motion.y = motionY;

        if (superbow.getValue()) {
            float i = 0;
            if (mc.player.ticksExisted % 2 == 0)
                i = 0.1f;
            else
                i = -0.1f;
            if (!(mc.gameSettings.keyBindSneak.pressed || mc.gameSettings.keyBindJump.pressed))
                mc.player.motion.y += i;
        }
    }

    private double getMotionY() {
        return mc.gameSettings.keyBindSneak.pressed ? -vertical.getValue()
                : mc.gameSettings.keyBindJump.pressed ? vertical.getValue() : 0;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        onToggle(true);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        onToggle(false);
        mc.player.abilities.isFlying = false;
    }
}
