package io.hynix.utils.player;

import java.util.Objects;

import io.hynix.HynixMain;
import io.hynix.events.impl.EventInput;
import io.hynix.events.impl.EventMoving;
import io.hynix.units.impl.combat.AttackAura;
import io.hynix.utils.client.IMinecraft;
import io.hynix.utils.johon0.math.MathUtils;
import lombok.experimental.UtilityClass;
import net.minecraft.block.AirBlock;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

@UtilityClass
public class MoveUtils implements IMinecraft {
    public static double getSpeedByBPS(double bps) {
        return bps / 15.3571428571;
    }

    public boolean isMoving() {
        return mc.player.movementInput.moveForward != 0f || mc.player.movementInput.moveStrafe != 0f;
    }

    public static boolean reason(boolean water) {
        boolean critWater = water && mc.world.getBlockState(new BlockPos(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ())).getBlock()
                instanceof FlowingFluidBlock && mc.world.getBlockState(new BlockPos(mc.player.getPosX(), mc.player.getPosY() + 1,
                mc.player.getPosZ())).getBlock() instanceof AirBlock;
        return mc.player.isPotionActive(Effects.BLINDNESS) || mc.player.isOnLadder()
                || mc.player.isInWater() && !critWater || mc.player.abilities.isFlying;
    }

    public static double getMotionYaw() {
        double motionYaw = Math.toDegrees(Math.atan2(mc.player.motion.z, mc.player.motion.x) - 90.0);
        motionYaw = motionYaw < 0.0 ? motionYaw + 360.0 : motionYaw;
        return motionYaw;
    }

    public static void setMotionSpeed(boolean cutting, boolean onlyMove, double speed) {
        AttackAura hitAura = HynixMain.getInstance().getModuleManager().getAttackAura();
        boolean check = (hitAura.isEnabled() && hitAura.target != null && hitAura.options.is("Коррекция движения").getValue());
        float yawPre;
        if (!isMoving()) {
            speed = 0.0;
        }
        if (MathUtils.getDifferenceOf(yawPre = -(mc.player.lastReportedPreYaw - mc.player.rotationYaw) * 3.0f, 0.0f) > 30.0) {
            yawPre = yawPre > 0.0f ? 30.0f : -30.0f;
        }
        float yaw = mc.player.rotationYaw;
        if (check) {
            yaw = hitAura.rotateVector.x;
            yawPre = 0.0f;
        }
        float moveYaw = moveYaw(yaw + yawPre);
        double sin = -Math.sin(Math.toRadians(moveYaw)) * speed;
        double cos = Math.cos(Math.toRadians(moveYaw)) * speed;
        if (!onlyMove || isMoving()) {
            if (cutting) {
                mc.player.motion.x = sin / (double)1.06f;
            }
            mc.player.motion.x = sin;
            if (cutting) {
                mc.player.motion.z = cos / (double)1.06f;
            }
            mc.player.motion.z = cos;
        }
    }

    public static void negativeFixMovement(EventInput event, float yaw) {
        float forward = -event.getForward(); // Инвертируем движение вперед/назад
        float strafe = event.getStrafe();   // Инвертируем движение влево/вправо
        double angle = MathHelper.wrapDegrees(Math.toDegrees(direction(yaw, (double)forward, (double)strafe)));

        if (forward != 0.0F || strafe != 0.0F) {
            float closestForward = 0.0F;
            float closestStrafe = 0.0F;
            float closestDifference = Float.MAX_VALUE;

            for(float predictedForward = -1.0F; predictedForward <= 1.0F; ++predictedForward) {
                for(float predictedStrafe = -1.0F; predictedStrafe <= 1.0F; ++predictedStrafe) {
                    if (predictedStrafe != 0.0F || predictedForward != 0.0F) {
                        double predictedAngle = MathHelper.wrapDegrees(Math.toDegrees(direction(yaw, (double)predictedForward, (double)predictedStrafe)));
                        double difference = Math.abs(angle - predictedAngle);
                        if (difference < (double)closestDifference) {
                            closestDifference = (float)difference;
                            closestForward = predictedForward;
                            closestStrafe = predictedStrafe;
                        }
                    }
                }
            }

            event.setForward(closestForward); // Устанавливаем измененное значение
            event.setStrafe(closestStrafe);   // Устанавливаем измененное значение
        }
    }

    public static void fixMovement(EventInput event, float yaw) {
        float forward = event.getForward();
        float strafe = event.getStrafe();
        double angle = MathHelper.wrapDegrees(Math.toDegrees(direction(mc.player.isElytraFlying() ? yaw : mc.player.rotationYaw, (double)forward, (double)strafe)));
        if (forward != 0.0F || strafe != 0.0F) {
            float closestForward = 0.0F;
            float closestStrafe = 0.0F;
            float closestDifference = Float.MAX_VALUE;

            for(float predictedForward = -1.0F; predictedForward <= 1.0F; ++predictedForward) {
                for(float predictedStrafe = -1.0F; predictedStrafe <= 1.0F; ++predictedStrafe) {
                    if (predictedStrafe != 0.0F || predictedForward != 0.0F) {
                        double predictedAngle = MathHelper.wrapDegrees(Math.toDegrees(direction(yaw, (double)predictedForward, (double)predictedStrafe)));
                        double difference = Math.abs(angle - predictedAngle);
                        if (difference < (double)closestDifference) {
                            closestDifference = (float)difference;
                            closestForward = predictedForward;
                            closestStrafe = predictedStrafe;
                        }
                    }
                }
            }

            event.setForward(closestForward);
            event.setStrafe(closestStrafe);
        }
    }

	public static int getSpeedEffect() {
		if (mc.player.isPotionActive(Effects.SPEED)) {
			return Objects.requireNonNull(mc.player.getActivePotionEffect(Effects.SPEED)).getAmplifier() + 1;
		}
		return 0;
	}

	public static double getBaseSpeed() {
		double baseSpeed = 0.2873;
		if (mc.player.isPotionActive(Effects.SPEED)) {
			int amplifier = mc.player.getActivePotionEffect(Effects.SPEED).getAmplifier();
			baseSpeed *= 1.0 + 0.2 * (double) (amplifier + 1);
		}
		return baseSpeed;
	}

    public static double direction(float rotationYaw, final double moveForward, final double moveStrafing) {
        if (moveForward < 0F) rotationYaw += 180F;

        float forward = 1F;

        if (moveForward < 0F) forward = -0.5F;
        else if (moveForward > 0F) forward = 0.5F;

        if (moveStrafing > 0F) rotationYaw -= 90F * forward;
        if (moveStrafing < 0F) rotationYaw += 90F * forward;

        return Math.toRadians(rotationYaw);
    }

    public double getMotion() {
        return Math.hypot(mc.player.getMotion().x, mc.player.getMotion().z);
    }

    public static double getSpeed() {
        return Math.sqrt(mc.player.motion.x * mc.player.motion.x + mc.player.motion.z * mc.player.motion.z);
    }

	public static double[] getSpeed(double speed) {
        AttackAura hitAura = HynixMain.getInstance().getModuleManager().getAttackAura();
        boolean check = (hitAura.isEnabled() && hitAura.target != null && hitAura.options.is("Коррекция движения").getValue());
        float yaw = check ? hitAura.rotateVector.x : mc.player.rotationYaw;
		float forward = mc.player.movementInput.moveForward;
		float strafe = mc.player.movementInput.moveStrafe;
		if (forward != 0) {
			if (strafe > 0) {
				yaw += (forward > 0 ? -45 : 45);
			} else if (strafe < 0) {
				yaw += (forward > 0 ? 45 : -45);
			}
			strafe = 0;
			if (forward > 0) {
				forward = 1;
			} else if (forward < 0) {
				forward = -1;
			}
		}
		return new double[] {
				(forward * speed * Math.cos(Math.toRadians(yaw + 90))
						+ strafe * speed * Math.sin(Math.toRadians(yaw + 90))),
				(forward * speed * Math.sin(Math.toRadians(yaw + 90))
						- strafe * speed * Math.cos(Math.toRadians(yaw + 90))),
				yaw };
	}
    public static void setMotion(double speed, LivingEntity player) {
        if (!MoveUtils.isMoving()) {
            return;
        }
        double yaw = MoveUtils.getDirection(true);
        player.setMotion(-Math.sin(yaw) * speed, player.motion.y, Math.cos(yaw) * speed);
    }
    public static void setMotion(final double motion, FreeCameraUtils player) {
        double forward = player.movementInput.moveForward;
        double strafe = player.movementInput.moveStrafe;
        float yaw = player.rotationYaw;
        if (forward == 0 && strafe == 0) {
            player.motion.x = 0;
            player.motion.z = 0;
        } else {
            if (forward != 0) {
                if (strafe > 0) {
                    yaw += (float) (forward > 0 ? -45 : 45);
                } else if (strafe < 0) {
                    yaw += (float) (forward > 0 ? 45 : -45);
                }
                strafe = 0;
                if (forward > 0) {
                    forward = 1;
                } else if (forward < 0) {
                    forward = -1;
                }
            }
            player.motion.x = forward * motion * MathHelper.cos((float) Math.toRadians(yaw + 90.0f)) + strafe * motion * MathHelper.sin((float) Math.toRadians(yaw + 90.0f));
            player.motion.z = forward * motion * MathHelper.sin((float) Math.toRadians(yaw + 90.0f)) - strafe * motion * MathHelper.cos((float) Math.toRadians(yaw + 90.0f));
        }
    }

    public static void setMotion(final double motion) {
        if (HynixMain.getInstance().getModuleManager().getFreeCam().isEnabled()) return;

        double forward = mc.player.movementInput.moveForward;
        double strafe = mc.player.movementInput.moveStrafe;
        AttackAura hitAura = HynixMain.getInstance().getModuleManager().getAttackAura();
        boolean check = (hitAura.isEnabled() && hitAura.target != null && hitAura.options.is("Коррекция движения").getValue());
        float yaw = check ? hitAura.rotateVector.x : mc.player.rotationYaw;
        if (forward == 0 && strafe == 0) {
            mc.player.motion.x = 0;
            mc.player.motion.z = 0;
        } else {
            if (forward != 0) {
                if (strafe > 0) {
                    yaw += (float) (forward > 0 ? -45 : 45);
                } else if (strafe < 0) {
                    yaw += (float) (forward > 0 ? 45 : -45);
                }
                strafe = 0;
                if (forward > 0) {
                    forward = 1;
                } else if (forward < 0) {
                    forward = -1;
                }
            }
            mc.player.motion.x = forward * motion * MathHelper.cos(Math.toRadians(yaw + 90.0f))
                    + strafe * motion * MathHelper.sin(Math.toRadians(yaw + 90.0f));
            mc.player.motion.z = forward * motion * MathHelper.sin(Math.toRadians(yaw + 90.0f))
                    - strafe * motion * MathHelper.cos(Math.toRadians(yaw + 90.0f));
        }
    }

    public static void setCuttingSpeed(double speed) {
        if (HynixMain.getInstance().getModuleManager().getFreeCam().isEnabled()) return;

        boolean tickTime = mc.player.ticksExisted % 2 == 0;
        double forward = mc.player.movementInput.moveForward;
        double strafe = mc.player.movementInput.moveStrafe;
        float yaw = mc.player.rotationYaw - (mc.player.lastReportedYaw - mc.player.rotationYaw) * 2.0F;
        AttackAura hitAura = HynixMain.getInstance().getModuleManager().getAttackAura();
        boolean check = (hitAura.isEnabled() && hitAura.target != null && hitAura.options.is("Коррекция движения").getValue());
        if (check) {
            yaw = hitAura.rotateVector.x;
        }

        if (!mc.gameSettings.keyBindForward.isKeyDown() && !mc.gameSettings.keyBindBack.isKeyDown() && !mc.gameSettings.keyBindLeft.isKeyDown() && !mc.gameSettings.keyBindRight.isKeyDown()) {
            mc.player.motion.x = tickTime ? 1.0E-10 : -1.0E-10;
            mc.player.motion.z = tickTime ? 1.0E-10 : -1.0E-10;
        } else {
            if (forward != 0.0) {
                if (strafe > 0.0) {
                    yaw += (float)(forward > 0.0 ? -45 : 45);
                } else if (strafe < 0.0) {
                    yaw += (float)(forward > 0.0 ? 45 : -45);
                }

                strafe = 0.0;
                if (forward > 0.0) {
                    forward = 1.0;
                } else if (forward < 0.0) {
                    forward = -1.0;
                }
            }

            double cos = Math.cos(Math.toRadians(yaw + 89.5F));
            double sin = Math.sin(Math.toRadians(yaw + 89.5F));
            mc.player.motion.x = forward * speed * cos + strafe * speed * sin;
            mc.player.motion.z = forward * speed * sin - strafe * speed * cos;
        }

    }

	public static void setSpeed(float speed) {
        if (HynixMain.getInstance().getModuleManager().getFreeCam().isEnabled()) return;

        AttackAura hitAura = HynixMain.getInstance().getModuleManager().getAttackAura();
        boolean check = (hitAura.isEnabled() && hitAura.target != null && hitAura.options.is("Коррекция движения").getValue());
        float yaw = check ? hitAura.rotateVector.x : mc.player.rotationYaw;
		float forward = mc.player.movementInput.moveForward;
		float strafe = mc.player.movementInput.moveStrafe;
		if (forward != 0) {
			if (strafe > 0) {
				yaw += (forward > 0 ? -45 : 45);
			} else if (strafe < 0) {
				yaw += (forward > 0 ? 45 : -45);
			}
			strafe = 0;
			if (forward > 0) {
				forward = 1;
			} else if (forward < 0) {
				forward = -1;
			}
		}
		mc.player.motion.x = (forward * speed * Math.cos(Math.toRadians(yaw + 90)) + strafe * speed * Math.sin(Math.toRadians(yaw + 90)));
		mc.player.motion.z = (forward * speed * Math.sin(Math.toRadians(yaw + 90)) - strafe * speed * Math.cos(Math.toRadians(yaw + 90)));
	}

    public static void setSpeed(float speed, float noMoveSpeed) {
        if (HynixMain.getInstance().getModuleManager().getFreeCam().isEnabled()) return;

        AttackAura hitAura = HynixMain.getInstance().getModuleManager().getAttackAura();
        boolean check = (hitAura.isEnabled() && hitAura.target != null && hitAura.options.is("Коррекция движения").getValue());
        float yaw = check ? hitAura.rotateVector.x : mc.player.rotationYaw;
        float forward = mc.player.movementInput.moveForward;
        float strafe = mc.player.movementInput.moveStrafe;
        if (isMoving()) {
            if (forward != 0) {
                if (strafe > 0) {
                    yaw += (forward > 0 ? -45 : 45);
                } else if (strafe < 0) {
                    yaw += (forward > 0 ? 45 : -45);
                }
                strafe = 0;
                if (forward > 0) {
                    forward = 1;
                } else if (forward < 0) {
                    forward = -1;
                }
            }
            mc.player.motion.x = (forward * speed * Math.cos(Math.toRadians(yaw + 90)) + strafe * speed * Math.sin(Math.toRadians(yaw + 90)));
            mc.player.motion.z = (forward * speed * Math.sin(Math.toRadians(yaw + 90)) - strafe * speed * Math.cos(Math.toRadians(yaw + 90)));
        } else {
            mc.player.motion.x *= noMoveSpeed;
            mc.player.motion.z *= noMoveSpeed;
        }
    }

    public static boolean moveKeysPressed() {
        return mc.gameSettings.keyBindForward.isKeyDown() || mc.gameSettings.keyBindBack.isKeyDown() || mc.gameSettings.keyBindLeft.isKeyDown() || mc.gameSettings.keyBindRight.isKeyDown();
    }

    public static double getCuttingSpeed() {
        return Math.sqrt(mc.player.getMotion().x * mc.player.getMotion().x + mc.player.getMotion().z * mc.player.getMotion().z);
    }

    public static double[] forward(final double d) {
        float f = mc.player.movementInput.moveForward;
        float f2 = mc.player.movementInput.moveStrafe;
        AttackAura hitAura = HynixMain.getInstance().getModuleManager().getAttackAura();
        boolean check = (hitAura.isEnabled() && hitAura.target != null && hitAura.options.is("Коррекция движения").getValue());
        float f3 = check ? hitAura.rotateVector.x : mc.player.rotationYaw;
        if (f != 0.0f) {
            if (f2 > 0.0f) {
                f3 += ((f > 0.0f) ? -45 : 45);
            } else if (f2 < 0.0f) {
                f3 += ((f > 0.0f) ? 45 : -45);
            }
            f2 = 0.0f;
            if (f > 0.0f) {
                f = 1.0f;
            } else if (f < 0.0f) {
                f = -1.0f;
            }
        }
        final double d2 = Math.sin(Math.toRadians(f3 + 90.0f));
        final double d3 = Math.cos(Math.toRadians(f3 + 90.0f));
        final double d4 = f * d * d3 + f2 * d * d2;
        final double d5 = f * d * d2 - f2 * d * d3;
        return new double[]{d4, d5};
    }

    public static boolean isBlockUnder(float under) {
        if (mc.player.getPosY() < 0.0) {
            return false;
        } else {
            AxisAlignedBB aab = mc.player.getBoundingBox().offset(0.0, -under, 0.0);
            return mc.world.getCollisionShapes(mc.player, aab).toList().isEmpty();
        }
    }

    public double getDirection(final boolean toRadians) {
        AttackAura hitAura = HynixMain.getInstance().getModuleManager().getAttackAura();
        boolean check = (hitAura.isEnabled() && hitAura.target != null && hitAura.options.is("Коррекция движения").getValue());
        float rotationYaw = check ? hitAura.rotateVector.x : mc.player.rotationYaw;
        if (mc.player.moveForward < 0F)
            rotationYaw += 180F;
        float forward = 1F;
        if (mc.player.moveForward < 0F)
            forward = -0.5F;
        else if (mc.player.moveForward > 0F)
            forward = 0.5F;

        if (mc.player.moveStrafing > 0F)
            rotationYaw -= 90F * forward;
        if (mc.player.moveStrafing < 0F)
            rotationYaw += 90F * forward;

        return toRadians ? Math.toRadians(rotationYaw) : rotationYaw;
    }

    public static float getDirection() {
        AttackAura hitAura = HynixMain.getInstance().getModuleManager().getAttackAura();
        boolean check = (hitAura.isEnabled() && hitAura.target != null && hitAura.options.is("Коррекция движения").getValue());
        float rotationYaw = check ? hitAura.rotateVector.x : mc.player.rotationYaw;

        float strafeFactor = 0f;

        if (mc.player.movementInput.moveForward > 0)
            strafeFactor = 1;
        if (mc.player.movementInput.moveForward < 0)
            strafeFactor = -1;

        if (strafeFactor == 0) {
            if (mc.player.movementInput.moveStrafe > 0)
                rotationYaw -= 90;

            if (mc.player.movementInput.moveStrafe < 0)
                rotationYaw += 90;
        } else {
            if (mc.player.movementInput.moveStrafe > 0)
                rotationYaw -= 45 * strafeFactor;

            if (mc.player.movementInput.moveStrafe < 0)
                rotationYaw += 45 * strafeFactor;
        }

        if (strafeFactor < 0)
            rotationYaw -= 180;

        return (float) Math.toRadians(rotationYaw);
    }

    public static void setStrafe(double motion) {
        if (HynixMain.getInstance().getModuleManager().getFreeCam().isEnabled()) return;

        if (!isMoving()) return;
        double radians = getDirection();
        mc.player.motion.x = -Math.sin(radians) * motion;
        mc.player.motion.z = Math.cos(radians) * motion;
    }

    public static final boolean moveKeyPressed(int keyNumber) {
        boolean w = mc.gameSettings.keyBindForward.isKeyDown();
        boolean a = mc.gameSettings.keyBindLeft.isKeyDown();
        boolean s = mc.gameSettings.keyBindBack.isKeyDown();
        boolean d = mc.gameSettings.keyBindRight.isKeyDown();
        return keyNumber == 0 ? w : (keyNumber == 1 ? a : (keyNumber == 2 ? s : keyNumber == 3 && d));
    }

    public static final boolean w() {
        return moveKeyPressed(0);
    }

    public static final boolean a() {
        return moveKeyPressed(1);
    }

    public static final boolean s() {
        return moveKeyPressed(2);
    }

    public static final boolean d() {
        return moveKeyPressed(3);
    }

    public static final float moveYaw(float entityYaw) {
        return entityYaw + (float)(!a() || !d() || w() && s() || !w() && !s() ? (w() && s() && (!a() || !d()) && (a() || d()) ? (a() ? -90 : (d() ? 90 : 0)) : (a() && d() && (!w() || !s()) || w() && s() && (!a() || !d()) ? 0 : (!a() && !d() && !s() ? 0 : (w() && !s() ? 45 : (s() && !w() ? (!a() && !d() ? 180 : 135) : ((w() || s()) && (!w() || !s()) ? 0 : 90))) * (a() ? -1 : 1)))) : (w() ? 0 : (s() ? 180 : 0)));
    }

    public static final float negativeMoveYaw(float entityYaw) {
        return entityYaw + (float)(!a() || !d() || w() && s() || !w() && !s() ? (w() && s() && (!a() || !d()) && (a() || d()) ? (a() ? -90 : (d() ? 90 : 0)) : (a() && d() && (!w() || !s()) || w() && s() && (!a() || !d()) ? 0 : (!a() && !d() && !s() ? 0 : (w() && !s() ? 45 : (s() && !w() ? (!a() && !d() ? 180 : 135) : ((w() || s()) && (!w() || !s()) ? 0 : 90))) * (a() ? 1 : -1)))) : (w() ? 0 : (s() ? 180 : 0)));
    }

    public static class MoveEvent {
        public static void setMoveMotion(final EventMoving move, final double motion) {
            double forward = mc.player.movementInput.moveForward;
            double strafe = mc.player.movementInput.moveStrafe;
            AttackAura hitAura = HynixMain.getInstance().getModuleManager().getAttackAura();
            float yaw = mc.player.rotationYaw;
            if (forward == 0 && strafe == 0) {
                move.getMotion().x = 0;
                move.getMotion().z = 0;
            } else {
                if (forward != 0) {
                    if (strafe > 0) {
                        yaw += (float) (forward > 0 ? -45 : 45);
                    } else if (strafe < 0) {
                        yaw += (float) (forward > 0 ? 45 : -45);
                    }
                    strafe = 0;
                    if (forward > 0) {
                        forward = 1;
                    } else if (forward < 0) {
                        forward = -1;
                    }
                }
                move.getMotion().x = forward * motion * MathHelper.cos((float) Math.toRadians(yaw + 90.0f))
                        + strafe * motion * MathHelper.sin((float) Math.toRadians(yaw + 90.0f));
                move.getMotion().z = forward * motion * MathHelper.sin((float) Math.toRadians(yaw + 90.0f))
                        - strafe * motion * MathHelper.cos((float) Math.toRadians(yaw + 90.0f));
            }
        }
    }
}
