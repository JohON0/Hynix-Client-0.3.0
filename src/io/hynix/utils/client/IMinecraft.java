package io.hynix.utils.client;

import io.hynix.utils.johon0.math.MathUtils;
import io.hynix.utils.text.GradientUtil;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.List;

public interface IMinecraft {
    Minecraft mc = Minecraft.getInstance();
    MainWindow sr = mc.getMainWindow();
    MainWindow window = mc.getMainWindow();
    BufferBuilder buffer = Tessellator.getInstance().getBuffer();
    Tessellator tessellator = Tessellator.getInstance();
    List<ITextComponent> clientMessages = new ArrayList<>();
    default void print(String input) {
        if (mc.player == null) return;
        ITextComponent text = GradientUtil.gradient("Hynix").append(new StringTextComponent(TextFormatting.DARK_GRAY + ": " + TextFormatting.RESET + input));
        clientMessages.add(text);
        mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(text, 0);
    }

    default Vector2d scaled() {
        return MathUtils.getMouse(mc.getMainWindow().getScaledWidth(), mc.getMainWindow().getScaledHeight());
    }
}
