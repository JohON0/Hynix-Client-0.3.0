package io.hynix.utils.player;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.hynix.HynixMain;
import io.hynix.managers.friend.FriendManager;
import io.hynix.utils.client.IMinecraft;
import net.minecraft.util.math.BlockPos;

public class AttackUtils implements IMinecraft {
    public static boolean isPlayerFalling(boolean onlyCrit, boolean onlySpace, boolean sync, boolean strengthCheck) {

        boolean cancelReason = mc.player.areEyesInFluid(FluidTags.WATER) && mc.player.movementInput.jump
                || mc.player.areEyesInFluid(FluidTags.LAVA) && mc.player.movementInput.jump
                || mc.player.isOnLadder()
                || mc.world.getBlockState(new BlockPos(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ())).getBlock() == Blocks.COBWEB
                || mc.player.isPassenger() || mc.player.abilities.isFlying
                || mc.player.isPotionActive(Effects.LEVITATION) || mc.player.isPotionActive(Effects.BLINDNESS) || mc.player.isPotionActive(Effects.SLOW_FALLING);

        boolean onSpace = !mc.gameSettings.keyBindJump.isKeyDown() && mc.player.isOnGround() && onlySpace;

        float attackStrength = mc.player.getCooledAttackStrength(sync ? HynixMain.getInstance().getTpsCalc().getAdjustTicks() : 1.0f);

        if (attackStrength < 0.92 && strengthCheck) return false;

        if (!cancelReason && onlyCrit) {
            return onSpace || !mc.player.isOnGround() && mc.player.fallDistance > 0;
        }

        return true;
    }
    public static boolean isPlayerFalling(boolean onlyCrit, boolean onlySpace, boolean sync) {
        boolean cancelReason = AttackUtils.mc.player.areEyesInFluid(FluidTags.WATER) && AttackUtils.mc.player.movementInput.jump || AttackUtils.mc.player.areEyesInFluid(FluidTags.LAVA) && AttackUtils.mc.player.movementInput.jump || AttackUtils.mc.player.isOnLadder() || AttackUtils.mc.world.getBlockState(new BlockPos(AttackUtils.mc.player.getPosX(), AttackUtils.mc.player.getPosY(), AttackUtils.mc.player.getPosZ())).getBlock() == Blocks.COBWEB || AttackUtils.mc.player.isPassenger() || AttackUtils.mc.player.abilities.isFlying || AttackUtils.mc.player.isPotionActive(Effects.LEVITATION) || AttackUtils.mc.player.isPotionActive(Effects.BLINDNESS) || AttackUtils.mc.player.isPotionActive(Effects.SLOW_FALLING);
        boolean onSpace = !AttackUtils.mc.gameSettings.keyBindJump.isKeyDown() && AttackUtils.mc.player.isOnGround() && onlySpace;
        float attackStrength = AttackUtils.mc.player.getCooledAttackStrength(sync ? HynixMain.getInstance().getTpsCalc().getAdjustTicks() : 1.0f);
        if ((double)attackStrength < 0.92) {
            return false;
        }
        if (!cancelReason && onlyCrit) {
            return onSpace || !AttackUtils.mc.player.isOnGround() && AttackUtils.mc.player.fallDistance > 0.0f;
        }
        return true;
    }

    public final List<EntityType> entityTypes = new ArrayList<>();

    public EntityType ofType(Entity entity, EntityType... types) {
        List<EntityType> typeList = Arrays.asList(types);

        if (entity instanceof PlayerEntity) {
            if (entityIsMe(entity, typeList)) {
                return EntityType.SELF;
            } else if (entity != mc.player) {
                if (entityIsPlayer(entity, typeList)) {
                    return EntityType.PLAYERS;
                } else if (entityIsFriend(entity, typeList)) {
                    return EntityType.FRIENDS;
                }
                if (entityIsNakedPlayer(entity, typeList)) {
                    return EntityType.NAKED;
                }
            }
        } else if (entityIsMob(entity, typeList)) {
            return EntityType.MOBS;
        } else if (entityIsAnimal(entity, typeList)) {
            return EntityType.ANIMALS;
        }
        return null;
    }

    private static boolean entityIsMe(Entity entity, List<EntityType> typeList) {
        return entity == mc.player && typeList.contains(EntityType.SELF);
    }

    private static boolean entityIsPlayer(Entity entity, List<EntityType> typeList) {
        return typeList.contains(EntityType.PLAYERS) && !FriendManager.isFriend(entity.getName().getString());
    }

    private static boolean entityIsFriend(Entity entity, List<EntityType> typeList) {
        return typeList.contains(EntityType.FRIENDS) && FriendManager.isFriend(entity.getName().getString());
    }

    private static boolean entityIsMob(Entity entity, List<EntityType> typeList) {
        return entity instanceof MonsterEntity && typeList.contains(EntityType.MOBS);
    }

    private static boolean entityIsNakedPlayer(Entity entity, List<EntityType> typeList) {
        return entity instanceof PlayerEntity && ((LivingEntity) entity).getTotalArmorValue() == 0;
    }

    private boolean entityIsAnimal(Entity entity, List<EntityType> typeList) {
        return (entity instanceof AnimalEntity || entity instanceof GolemEntity || entity instanceof VillagerEntity)
                && typeList.contains(EntityType.ANIMALS);
    }

    public AttackUtils apply(EntityType type) {
        this.entityTypes.add(type);
        return this;
    }

    public EntityType[] build() {
        return this.entityTypes.toArray(new EntityType[0]);
    }

    public enum EntityType {
        PLAYERS, MOBS, ANIMALS, NAKED, FRIENDS, NPC, SELF
    }
}
