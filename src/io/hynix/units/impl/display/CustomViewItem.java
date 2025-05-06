package io.hynix.units.impl.display;

import com.mojang.blaze3d.matrix.MatrixStack;

import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.units.impl.combat.AttackAura;
import io.hynix.units.settings.impl.BooleanSetting;
import io.hynix.units.settings.impl.ModeSetting;
import io.hynix.units.settings.impl.SliderSetting;
import net.minecraft.util.math.vector.Vector3f;


@UnitRegister(name = "CustomViewItem", category = Category.Display,desc = "Изменение положение предмета (анимаций)")
public class CustomViewItem extends Unit {

    public BooleanSetting swingAnim = new BooleanSetting("SwingAnim", true);
    public ModeSetting animationMode = new ModeSetting("Мод", "Swing Up", "Spin", "Swing Up", "Charge", "Flip", "Throw", "Twirl", "Diagonal").setVisible(() -> swingAnim.getValue());
    public SliderSetting swingPower = new SliderSetting("Сила", 5, 1.0f, 10.0f, 0.05f).setVisible(() -> swingAnim.getValue());
    public SliderSetting swingSpeed = new SliderSetting("Скорость", 8, 3.0f, 10.0f, 1.0f).setVisible(() -> swingAnim.getValue());
    public SliderSetting scale = new SliderSetting("Размер", 1, 0.5f, 1.5f, 0.05f).setVisible(() -> swingAnim.getValue());
    public final BooleanSetting onlyAura = new BooleanSetting("Только с AttackAura", false).setVisible(() -> swingAnim.getValue());

    public final SliderSetting x = new SliderSetting("Позиция X", 0.0F, -2.0f, 2.0f, 0.1F);
    public final SliderSetting y = new SliderSetting("Позиция Y", 0.0F, -2.0f, 2.0f, 0.1F);
    public final SliderSetting z = new SliderSetting("Позиция Z", 0.0F, -2.0f, 2.0f, 0.1F);

    public AttackAura hitAura;

    public CustomViewItem(AttackAura hitAura) {
        this.hitAura = hitAura;
        addSettings(swingAnim, animationMode, swingPower, swingSpeed, scale, onlyAura, x, y, z);
    }

    public void animationProcess(MatrixStack stack, float swingProgress, Runnable runnable) {
        float anim = (float) Math.sin(swingProgress * (Math.PI / 2) * 2);

        if (onlyAura.getValue() && (hitAura.target == null)) {
            return;
        }

        switch (animationMode.getValue()) {
            case "Swing Up" -> {
                stack.scale(scale.getValue(), scale.getValue(), scale.getValue());
                stack.translate(0.0f, 0.2f, -0.5f);
                stack.rotate(Vector3f.XP.rotationDegrees(-45));
                stack.rotate(Vector3f.YP.rotationDegrees(30));
                stack.rotate(Vector3f.ZP.rotationDegrees((swingPower.getValue() * 15) * anim));
            }

            case "Charge" -> {
                stack.scale(scale.getValue(), scale.getValue(), scale.getValue());
                stack.translate(0.0f, 0.f, -0.3f);
                stack.rotate(Vector3f.YP.rotationDegrees(-90));
                stack.rotate(Vector3f.XP.rotationDegrees(-20));
                stack.rotate(Vector3f.ZP.rotationDegrees((swingPower.getValue() * 15) * anim));
                stack.translate(0.0f, 0.1f, 0.0f);
            }

            case "Flip" -> {
                stack.scale(scale.getValue(), scale.getValue(), scale.getValue());
                stack.translate(0.0f, 0.1f, -0.5f);
                stack.rotate(Vector3f.YP.rotationDegrees(180));
                stack.rotate(Vector3f.XP.rotationDegrees((swingPower.getValue() * 90)* anim));
                stack.rotate(Vector3f.ZP.rotationDegrees(15));
                stack.translate(0.0f, -0.1f, 0.0f);
            }

            case "Spin" -> {
                stack.scale(scale.getValue(), scale.getValue(), scale.getValue());
                stack.translate(0.0f, 0.0f, -0.5f); // Позиция перед вращением
                stack.rotate(Vector3f.XP.rotationDegrees(360 * anim)); // 360 градусов вращения вокруг оси Y
            }

            case "Throw" -> {
                stack.scale(scale.getValue(), scale.getValue(), scale.getValue());
                stack.translate(0f, 0.2f, -0.5f);
                stack.rotate(Vector3f.YP.rotationDegrees(45));
                stack.rotate(Vector3f.XP.rotationDegrees(-30));
                stack.rotate(Vector3f.ZP.rotationDegrees((swingPower.getValue() * 15) * anim));
            }

            case "Twirl" -> {
                stack.scale(scale.getValue(), scale.getValue(), scale.getValue());
                stack.translate(0f, 0.1f, -0.3);
                stack.rotate(Vector3f.YP.rotationDegrees(360 * anim)); // Вращение вокруг оси Y
                stack.rotate(Vector3f.XP.rotationDegrees((swingPower.getValue() * 5) * anim)); // Наклон по Y
            }

            case "Diagonal" -> {
                stack.scale(scale.getValue(), scale.getValue(), scale.getValue());
                stack.translate(0.3f, 0.0f, -0.5f);
                stack.rotate(Vector3f.YP.rotationDegrees(-30));
                stack.rotate(Vector3f.XP.rotationDegrees(-45));
                stack.rotate(Vector3f.ZP.rotationDegrees((swingPower.getValue() * 10) * anim)); // Вращение по оси Z
            }
            default -> {
                stack.scale(scale.getValue(), scale.getValue(), scale.getValue());
                runnable.run();
            }
        }

    }

}
