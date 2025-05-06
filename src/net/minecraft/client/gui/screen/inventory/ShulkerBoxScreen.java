package net.minecraft.client.gui.screen.inventory;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import io.hynix.units.impl.display.Animations;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ShulkerBoxContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import io.hynix.HynixMain;
import io.hynix.units.api.UnitManager;
import io.hynix.units.impl.miscellaneous.BetterMinecraft;
import io.hynix.utils.johon0.animations.AnimationUtils;
import io.hynix.utils.johon0.animations.Direction;
import io.hynix.utils.johon0.animations.impl.EaseBackIn;

public class ShulkerBoxScreen extends ContainerScreen<ShulkerBoxContainer> {
    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation("textures/gui/container/shulker_box.png");

    public static final AnimationUtils openAnimation = new EaseBackIn(400, 1, 1);
    private boolean isClosing;

    public ShulkerBoxScreen(ShulkerBoxContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
        ++this.ySize;
        isClosing = false;
        openAnimation.setDirection(Direction.FORWARDS);
    }

    @Override
    public void tick() {
        if (isClosing) {
            if (openAnimation.isDone()) {
                this.minecraft.displayGuiScreen(null);
            }
        }
    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        UnitManager moduleManager = HynixMain.getInstance().getModuleManager();
        Animations betterMinecraft = moduleManager.getAnimations();
        if (betterMinecraft.animationcontainer.getValue() && betterMinecraft.isEnabled()) {

            if (!openAnimation.isDone()) {
                sizeAnimation(this.width / 2, this.height / 2, openAnimation.getOutput());
            }
        }


        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderHoveredTooltip(matrixStack, mouseX, mouseY);
    }

    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(GUI_TEXTURE);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.blit(matrixStack, i, j, 0, 0, this.xSize, this.ySize);
    }

    @Override
    public void onClose() {
        openAnimation.setDirection(Direction.BACKWARDS);
        isClosing = true;
    }

    public static void sizeAnimation(double width, double height, double scale) {
        RenderSystem.translated(width, height, 0);
        RenderSystem.scaled(scale, scale, scale);
        RenderSystem.translated(-width, -height, 0);
    }
}
