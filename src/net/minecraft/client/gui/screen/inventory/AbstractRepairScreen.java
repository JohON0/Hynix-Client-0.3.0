package net.minecraft.client.gui.screen.inventory;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import io.hynix.units.impl.display.Animations;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.AbstractRepairContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import io.hynix.HynixMain;
import io.hynix.units.api.UnitManager;
import io.hynix.units.impl.miscellaneous.BetterMinecraft;
import io.hynix.utils.johon0.animations.AnimationUtils;
import io.hynix.utils.johon0.animations.Direction;
import io.hynix.utils.johon0.animations.impl.EaseBackIn;

public class AbstractRepairScreen<T extends AbstractRepairContainer> extends ContainerScreen<T> implements IContainerListener {
    private ResourceLocation guiTexture;
    public static final AnimationUtils openAnimation = new EaseBackIn(400, 1, 1);
    private boolean isClosing;

    public AbstractRepairScreen(T container, PlayerInventory playerInventory, ITextComponent title, ResourceLocation guiTexture) {
        super(container, playerInventory, title);
        this.guiTexture = guiTexture;
        openAnimation.setDirection(Direction.FORWARDS);
    }

    protected void initFields() {
    }

    protected void init() {
        super.init();
        this.initFields();
        this.container.addListener(this);
    }

    public void sizeAnimation(double width, double height, double scale) {
        RenderSystem.translated(width, height, 0);
        RenderSystem.scaled(scale, scale, scale);
        RenderSystem.translated(-width, -height, 0);
    }

    public void onClose() {
        openAnimation.setDirection(Direction.BACKWARDS);
        isClosing = true; // ������������� ��������� ��������
        super.onClose();
        this.container.removeListener(this);
    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        UnitManager moduleManager = HynixMain.getInstance().getModuleManager();
        Animations betterMinecraft = moduleManager.getAnimations();
        if (openAnimation.isDone() && isClosing) {
                this.minecraft.displayGuiScreen(new InventoryScreen(this.minecraft.player));
            }
        if (betterMinecraft.animationcontainer.getValue() && betterMinecraft.isEnabled()) {
        sizeAnimation(this.width / 2, this.height / 2, openAnimation.getOutput());

    }
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        RenderSystem.disableBlend();
        this.renderNameField(matrixStack, mouseX, mouseY, partialTicks);
        this.renderHoveredTooltip(matrixStack, mouseX, mouseY);
    }

    protected void renderNameField(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    }

    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(this.guiTexture);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.blit(matrixStack, i, j, 0, 0, this.xSize, this.ySize);
        this.blit(matrixStack, i + 59, j + 20, 0, this.ySize + (this.container.getSlot(0).getHasStack() ? 0 : 16), 110, 16);

        if ((this.container.getSlot(0).getHasStack() || this.container.getSlot(1).getHasStack()) && !this.container.getSlot(2).getHasStack()) {
            this.blit(matrixStack, i + 99, j + 45, this.xSize, 0, 28, 21);
        }
    }

    public void sendAllContents(Container containerToSend, NonNullList<ItemStack> itemsList) {
        this.sendSlotContents(containerToSend, 0, containerToSend.getSlot(0).getStack());
    }

    public void sendWindowProperty(Container containerIn, int varToUpdate, int newValue) {
    }

    public void sendSlotContents(Container containerToSend, int slotInd, ItemStack stack) {
    }
}
