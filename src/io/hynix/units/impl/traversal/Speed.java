package io.hynix.units.impl.traversal;

import com.google.common.eventbus.Subscribe;

import io.hynix.HynixMain;
import io.hynix.events.impl.EventMotion;
import io.hynix.events.impl.EventMoving;
import io.hynix.events.impl.EventPacket;
import io.hynix.events.impl.EventUpdate;
import io.hynix.managers.premium.PremiumChecker;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.units.settings.impl.BooleanSetting;
import io.hynix.units.settings.impl.ModeSetting;
import io.hynix.units.settings.impl.SliderSetting;
import io.hynix.utils.johon0.math.TimerUtils;
import io.hynix.utils.player.InventoryUtils;
import io.hynix.utils.player.MoveUtils;
import io.hynix.utils.player.StrafeMovement;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.potion.Effects;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

@UnitRegister(name = "Speed", category = Category.Traversal, desc = "Ускорение игрока")
public class Speed extends Unit {

	private final ModeSetting mode = new ModeSetting("Мод", "Matrix", "Matrix", "Grim", "Timer", "Funtime", "FunTime 2");
	private final BooleanSetting autoJump = new BooleanSetting("Auto Jump", false).setVisible(() -> mode.is("Matrix") || mode.is("NCP") || mode.is("Vanilla"));
	// aac
	private final BooleanSetting longjump_aac = new BooleanSetting("LongJump", false).setVisible(() -> mode.is("AAC"));
	private final BooleanSetting onground_aac = new BooleanSetting("OnGround", false).setVisible(() -> mode.is("AAC"));

	// matrix
	private final BooleanSetting timerboost_matrix = new BooleanSetting("Timer", false).setVisible(() -> mode.is("Matrix"));
	private final BooleanSetting motionboost_matrix = new BooleanSetting("Motion", true).setVisible(() -> mode.is("Matrix"));
	private final BooleanSetting strafemotion_matrix = new BooleanSetting("Strafe", false).setVisible(() -> mode.is("Matrix") && motionboost_matrix.getValue());
	private final BooleanSetting damageboost_matrix = new BooleanSetting("DamageBoost", true).setVisible(() -> mode.is("Matrix"));
	private final BooleanSetting airboost_matrix = new BooleanSetting("AirBoost", false).setVisible(() -> mode.is("Matrix"));

	// grim
	private final BooleanSetting blockboost_grim = new BooleanSetting("BlockBoost", true).setVisible(() -> mode.is("Grim"));
	private final BooleanSetting entityboost_grim = new BooleanSetting("EntityBoost", true).setVisible(() -> mode.is("Grim"));
	private final BooleanSetting timerboost_grim = new BooleanSetting("Timer", false).setVisible(() -> mode.is("Grim"));

	// ncp
	private final BooleanSetting timerboost_ncp = new BooleanSetting("Timer", true).setVisible(() -> mode.is("NCP"));
	private final BooleanSetting yport_ncp = new BooleanSetting("YPort", true).setVisible(() -> mode.is("NCP"));
	private final BooleanSetting bhop_ncp = new BooleanSetting("BHop", false).setVisible(() -> mode.is("NCP"));
	private final BooleanSetting spoofJump = new BooleanSetting("Spoof", false).setVisible(() -> mode.is("NCP") && autoJump.getValue() && bhop_ncp.getValue());


	// vanilla
	private final SliderSetting speed = new SliderSetting("Скорость", 1, 0.1f, 5, 0.1f).setVisible(() -> mode.is("Vanilla"));

	private final BooleanSetting antiFlag = new BooleanSetting("AntiFlag", true);

	private final StrafeMovement strafeMovement = new StrafeMovement();
	private boolean enabled = false;
	public static int stage;
	public double less, stair, moveSpeed;
	public boolean slowDownHop, wasJumping, boosting, restart;
	private int prevSlot = -1;
	boolean isVelocity, damage, velocity;
	int ticks;
	double motion;
	public TimerUtils timerUtils = new TimerUtils();
	public TimerUtils racTimer = new TimerUtils();
	
	public Speed() {
		addSettings(mode, speed,
				autoJump,
				blockboost_grim, entityboost_grim, timerboost_grim,
				damageboost_matrix, motionboost_matrix, strafemotion_matrix, airboost_matrix, timerboost_matrix,
				timerboost_ncp, yport_ncp, bhop_ncp, spoofJump,
				longjump_aac, onground_aac, antiFlag);
	}

	@Override
	public void onDisable() {

		mc.timer.timerSpeed = 1;
		super.onDisable();
	}

	@Override
	public void onEnable() {
		super.onEnable();
	}

	@Subscribe
	public void onPacket(EventPacket e) {
		if (HynixMain.getInstance().getModuleManager().getFreeCam().isEnabled()) return;

		if (antiFlag.getValue()) {
			if (e.getPacket() instanceof SPlayerPositionLookPacket p) {
				mc.player.setPacketCoordinates(p.getX(), p.getY(), p.getZ());
				mc.player.setRawPosition(p.getX(), p.getY(), p.getZ());
				toggle();
			}
		}

		if (mode.is("Grim") && timerboost_grim.getValue()) {
			if (e.getPacket() instanceof CConfirmTransactionPacket p) {
				e.cancel();
			}
		}

		if (mode.is("Matrix") && damageboost_matrix.getValue() && e.isReceive()) {
			if (e.getPacket() instanceof SEntityVelocityPacket) {
				if (((SEntityVelocityPacket) e.getPacket()).getMotionY() > 0) {
					isVelocity = true;
				}
				if ((((SEntityVelocityPacket) e.getPacket()).getMotionY() / 8000.0D) > 0.2) {
					motion = (((SEntityVelocityPacket) e.getPacket()).getMotionY() / 8000.0D);
					velocity = true;
				}
			}
		}
	}

	@Subscribe
	public void onUpdate(EventUpdate e) {
		if (HynixMain.getInstance().getModuleManager().getFreeCam().isEnabled()) return;

		switch (mode.getValue()) {
			case "Matrix" -> {
				if (mc.player.isOnGround() && autoJump.getValue() && !mc.player.isInLava() && !mc.player.isInWater() && !airboost_matrix.getValue()) {
					mc.player.jump();
				}
			}

			case "NCP" -> {
				NCPSpeed(timerboost_ncp.getValue(), yport_ncp.getValue(), bhop_ncp.getValue());
			}

			case "Vanilla" -> {
				MoveUtils.setMotion(speed.getValue());

				if (autoJump.getValue() && mc.player.isOnGround() && !mc.player.isInWater() && !mc.player.isInLava())
					mc.player.jump();
			}

			case "RAC" -> {
				if (racTimer.isReached(10)) {
					if (mc.player.onGround && !mc.player.isJumping) {
						MoveUtils.setSpeed((float) MathHelper.clamp(MoveUtils.getSpeed() * (mc.player.rayGround ? 1.8 : 0.8), 0.2, MoveUtils.w() && mc.player.isSprinting() ? 1.715499997138977 : 1.7450000047683716));
						mc.player.rayGround = mc.player.onGround;
					} else {
						mc.player.serverSprintState = true;
						MoveUtils.setSpeed((float) MathHelper.clamp(MoveUtils.getSpeed() * (!mc.player.onGround && !mc.player.rayGround ? 1.2 : 1.0), 0.195, 1.823585033416748), 0.12F);
						mc.player.rayGround = mc.player.onGround;
					}

					racTimer.reset();
				}
			}

			case "Funtime" -> {
				if (!PremiumChecker.isPremium) {
				toggle();
				print("Предупреждение: Мод " + mode.getValue() + " работает только для премиум пользователей! Если хочешь подержать проект, то премиум-подписку можно преобрести на сайте https://hynix.fun/");
				}

				if (PremiumChecker.isPremium) {
					if (!mc.player.isElytraFlying() && mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST).getItem() == Items.ELYTRA && MoveUtils.isMoving()) {
						mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
						mc.player.startFallFlying();
					}

					if (mc.player.isOnGround() && mc.player.isElytraFlying()) {
						if (!mc.gameSettings.keyBindJump.isKeyDown()) {
							mc.player.jump();
							mc.player.motion.y = 0.085;
						}
					}
				}
			}

			case "FunTime 2" -> {
				AxisAlignedBB aabb = mc.player.getBoundingBox().grow(0.1);
				int armorstans = mc.world.getEntitiesWithinAABB(ArmorStandEntity.class, aabb).size();
				boolean canBoost = armorstans > 1 || mc.world.getEntitiesWithinAABB(LivingEntity.class, aabb).size() > 1;
				if (canBoost && !mc.player.isOnGround()) {
					mc.player.jumpMovementFactor = armorstans > 1 ? 1.0f / (float) armorstans : 0.16f;
				}
			}

			case "Grim" -> {
				if (timerboost_grim.getValue()) {
					if (timerUtils.isReached(1150)) {
						boosting = true;
					}
					if (timerUtils.isReached(7000)) {
						boosting = false;
						timerUtils.reset();
					}
					if (boosting) {
						if (mc.player.isOnGround() && !mc.gameSettings.keyBindJump.pressed) {
							mc.player.jump();
						}
						mc.timer.timerSpeed = (mc.player.ticksExisted % 2 == 0 ? 1.5f : 1.2f);
					} else {
						mc.timer.timerSpeed = (0.05f);
					}
				}

				if (blockboost_grim.getValue()) {
					int block = InventoryUtils.getInstance().getSlotInInventoryOrHotbar(Items.ICE, true);
					if (block == -1 || mc.player.isInWater()) {
						return;
					}
					if (mc.player.isOnGround()) {
						if (!wasJumping) {
							wasJumping = true;
							placeBlock();
							mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ(), mc.player.rotationYaw, 90.0f, mc.player.isOnGround()));
						}
					} else {
						wasJumping = false;
						mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ(), mc.player.rotationYaw, 90.0f, mc.player.isOnGround()));
					}
					if (mc.player.isOnGround()) {
						mc.player.jump();
					}
				}
			}

			case "Timer" -> {
				float timerValue = mc.player.fallDistance <= 0.25f ? 2.2f : (float) (mc.player.fallDistance != Math.ceil(mc.player.fallDistance) ? 0.4f : 1f);
				if (MoveUtils.isMoving()) {
					mc.timer.timerSpeed = timerValue;
					if (mc.player.onGround) {
						mc.player.jump();
					}
				} else {
					mc.timer.timerSpeed = 1.0f;
				}
			}


			case "Vulcan" -> {
				mc.player.jumpMovementFactor = 0.025f;
				if (mc.player.isOnGround() && MoveUtils.isMoving()) {
					if (mc.player.collidedHorizontally && mc.gameSettings.keyBindJump.pressed) {
						if (!mc.gameSettings.keyBindJump.pressed) {
							mc.player.jump();
						}
						return;
					}
					mc.player.jump();
					mc.player.motion.y = 0.1f;
				}
			}

			case "AAC" -> {
				boolean longHop = longjump_aac.getValue() && (mc.player.isJumping || mc.player.fallDistance != 0.0F);
				boolean onGround = onground_aac.getValue() && !mc.player.isJumping && mc.player.onGround && mc.player.collidedVertically && MoveUtils.getSpeed() < 0.9;
				mc.timer.timerSpeed = 1.2f;
				if (longHop) {
					mc.player.jumpMovementFactor = 0.17F;
					mc.player.multiplyMotionXZ(1.005F);
				}

				if (onGround) {
					mc.player.multiplyMotionXZ(1.212F);
				}
			}
		}
	}
	
	@Subscribe
	public void onMotion(EventMotion move) {
		if (HynixMain.getInstance().getModuleManager().getFreeCam().isEnabled()) return;

		if (mode.is("Matrix") && timerboost_matrix.getValue()) {
			if (mc.player.isOnGround()) {
				mc.timer.timerSpeed = (1.1f);
			}
			if (mc.player.fallDistance > 0.1 && mc.player.fallDistance < 1) {
				mc.timer.timerSpeed = (1 + (1F - Math.floorMod((long) 2.520, (long) 2.600)));
			}
			if (mc.player.fallDistance >= 1) {
				mc.timer.timerSpeed = (0.978F);
			}
		}

		if (mode.is("Matrix") && damageboost_matrix.getValue()) {
			double radians = MoveUtils.getDirection();
			if (mc.player.hurtTime == 9) {
				damage = true;
			}
			if (damage && isVelocity) {
				if (velocity) {
					if (mc.player.onGround && MoveUtils.isMoving()) {
						mc.player.addVelocity(-Math.sin(radians) * 8 / 24.5, 0, Math.cos(radians) * 8 / 24.5);
						MoveUtils.setStrafe(MoveUtils.getSpeed());
					}
					ticks++;
				}
				if (ticks >= Math.max(24, 30)) {
					isVelocity = false;
					velocity = false;
					damage = false;
					toggle();
					ticks = 0;
				}
			}
		}

		if (mode.is("Matrix") && airboost_matrix.getValue()) {
			if (mc.player.isOnGround()) {
				enabled = true;
			} else if (mc.player.fallDistance > 0f) {
				enabled = false;
			}

			if (!mc.world.getCollisionShapes(mc.player, mc.player.getBoundingBox().expand(0.5, 0, 0.5).offset(0, -1, 0)).toList().isEmpty() && mc.player.ticksExisted % 2 == 0) {
				if (!motionboost_matrix.getValue() && !autoJump.getValue()) {
					mc.player.fallDistance = 0;
					move.setOnGround(true);
					mc.player.onGround = true;
				}
				if (enabled && !mc.player.movementInput.jump && autoJump.getValue()) mc.player.jump();
				mc.player.jumpMovementFactor = 0.026523f;
			}
		}
	}
	
	@Subscribe
	public void onMove(EventMoving e) {
		if (HynixMain.getInstance().getModuleManager().getFreeCam().isEnabled()) return;

        if (mode.is("Matrix") && motionboost_matrix.getValue()) {
            if (!mc.player.isOnGround() && mc.player.fallDistance >= 0.5f && e.toGround) {
            	double speed = 2;
				if (strafemotion_matrix.getValue()) {
					double[] newSpeed = MoveUtils.getSpeed((Math.hypot(mc.player.motion.x, mc.player.motion.z) - 0.0001) * speed);
					e.motion.x = newSpeed[0];
					e.motion.z = newSpeed[1];
					mc.player.motion.x = e.motion.x;
					mc.player.motion.z = e.motion.z;
					return;
				}
                mc.player.motion.x *= speed;
                mc.player.motion.z *= speed;
                strafeMovement.setOldSpeed(speed);
            }
        }
        
        if (mode.is("Grim") && entityboost_grim.getValue()) {
			for (Entity ent : mc.world.getAllEntities()) {
				int collisions = 0;
				if (ent != mc.player && (ent instanceof LivingEntity || ent instanceof BoatEntity) && mc.player.getBoundingBox().expand(0, 1.0, 0).intersects(ent.getBoundingBox())) collisions++;
				double[] motion = MoveUtils.forward(0.08 * collisions);
				mc.player.addVelocity(motion[0], 0.0, motion[1]);
			}
		}
	}
	
	public void NCPSpeed(boolean timer, boolean yPort, boolean bhop) {
		if (timer) {
			mc.timer.timerSpeed = 1.075f;
		}

		double speed = 0.0;

		if (yPort) {
			speed = MoveUtils.getSpeed();
			mc.player.speedInAir = mc.player.isPotionActive(Effects.SPEED) ? 0.06f : 0.05f;
			if (mc.player.onGround) {
				mc.player.jump();
				if (mc.player.isPotionActive(Effects.SPEED)) {
					mc.player.jump();
				}
				mc.player.motion.y /= 1.05;
			} else {
				if (!mc.player.collidedHorizontally) {
					mc.player.motion.y -= 1.0;
				}
				speed = mc.player.isPotionActive(Effects.SPEED) ? 0.45 : 0.32;
			}
			MoveUtils.setSpeed((float) speed);
		} else if (mc.player.speedInAir == 0.06f || mc.player.speedInAir == 0.05f) {
			mc.player.speedInAir = 0.02f;
		}

		if (bhop) {
			if (!autoJump.getValue() && !mc.gameSettings.keyBindJump.isKeyDown() && !yPort) {
				return;
			}

			mc.player.jumpMovementFactor = (float) ((double) mc.player.jumpMovementFactor * 1.04);
			boolean collided = mc.player.collidedHorizontally;

			if (collided) {
				stage = -1;
			}
			if (this.stair > 0.0) {
				this.stair -= 0.3;
			}
			this.less -= this.less > 1.0 ? 0.24 : 0.17;
			if (this.less < 0.0) {
				this.less = 0.0;
			}

			if (!mc.player.isInWater() && mc.player.onGround) {
				collided = mc.player.collidedHorizontally;
				if (stage >= 0 || collided) {
					stage = 0;
					float motY = 0.42f;
					if (spoofJump.getValue())
						mc.player.motion.y = motY;
					else
						mc.player.jump();
					this.less += 1.0;
					this.slowDownHop = this.less > 1.0 && !this.slowDownHop;
					if (this.less > 1.15) {
						this.less = 1.15;
					}
				}
			}
			this.moveSpeed = this.getCurrentSpeed(stage) + 0.0335;
			this.moveSpeed *= 0.85;
			if (this.stair > 0.0) {
				this.moveSpeed *= 1.0;
			}
			if (this.slowDownHop) {
				this.moveSpeed *= 0.8575;
			}
			if (mc.player.isInWater()) {
				this.moveSpeed = 0.351;
			}
			if (MoveUtils.isMoving()) {
				MoveUtils.setSpeed((float)moveSpeed);
			}
			++stage;
		}
	}

	public void placeBlock() {
		if (HynixMain.getInstance().getModuleManager().getAttackAura().isEnabled() && HynixMain.getInstance().getModuleManager().getAttackAura().target != null) {
			return;
		}
		BlockPos blockPos = new BlockPos(mc.player.getPosX(), mc.player.getPosY() - 0.6, mc.player.getPosZ());
		if (mc.world.getBlockState(blockPos).isAir()) {
			return;
		}
		int block = InventoryUtils.findBlockInHotbar();
		if (block == -1) {
			return;
		}
		mc.player.connection.sendPacket(new CHeldItemChangePacket(block));
		mc.player.connection.sendPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.STOP_DESTROY_BLOCK, blockPos, Direction.UP));
		Vector3d blockCenter = new Vector3d((double)blockPos.getX() + 0.5, blockPos.getY(), (double)blockPos.getZ() + 0.5);
		mc.player.connection.sendPacket(new CPlayerTryUseItemOnBlockPacket(Hand.MAIN_HAND, new BlockRayTraceResult(blockCenter, Direction.UP, blockPos, false)));
		mc.player.connection.sendPacket(new CHeldItemChangePacket(prevSlot));
		mc.world.setBlockState(blockPos, Blocks.ICE.getDefaultState());
		prevSlot = mc.player.inventory.currentItem;
	}
	
	public double getCurrentSpeed(int stage) {
		double speed = MoveUtils.getBaseSpeed() + 0.028 * (double) MoveUtils.getSpeedEffect() + (double) MoveUtils.getSpeedEffect() / 15.0;
		double initSpeed = 0.4145 + (double) MoveUtils.getSpeedEffect() / 12.5;
		double decrease = (double) stage / 500.0 * 1.87;
		if (stage == 0) {
			speed = 0.64 + ((double) MoveUtils.getSpeedEffect() + 0.028 * (double) MoveUtils.getSpeedEffect()) * 0.134;
		} else if (stage == 1) {
			speed = initSpeed;
		} else if (stage >= 2) {
			speed = initSpeed - decrease;
		}
		return Math.max(speed, this.slowDownHop ? speed : MoveUtils.getBaseSpeed() + 0.028 * (double) MoveUtils.getSpeedEffect());
	}
}
