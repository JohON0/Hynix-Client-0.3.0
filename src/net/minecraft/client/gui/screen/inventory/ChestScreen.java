package net.minecraft.client.gui.screen.inventory;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import io.hynix.units.impl.display.Animations;
import io.hynix.utils.client.IMinecraft;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import io.hynix.HynixMain;
import io.hynix.units.api.UnitManager;
import io.hynix.units.impl.miscellaneous.BetterMinecraft;
import io.hynix.utils.johon0.animations.AnimationUtils;
import io.hynix.utils.johon0.animations.Direction;
import io.hynix.utils.johon0.animations.impl.EaseBackIn;

public class ChestScreen extends ContainerScreen<ChestContainer> implements IHasContainer<ChestContainer>, IMinecraft {
    public static final AnimationUtils openAnimation = new EaseBackIn(400, 1, 1);
    public boolean isClosing;
    /**
     * The ResourceLocation containing the chest GUI texture.
     */
    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");

    /**
     * Window height is calculated with these values; the more rows, the higher
     */
    private final int inventoryRows;
    private final ITextComponent title;

    public ChestScreen(ChestContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        this.title = title;
        this.passEvents = false;
        int i = 222;
        int j = 114;
        this.inventoryRows = container.getNumRows();
        this.ySize = 114 + this.inventoryRows * 18;
        this.playerInventoryTitleY = this.ySize - 94;
        isClosing = false;
    }

    Button button;

    @Override
    protected void init() {
        openAnimation.setDirection(Direction.FORWARDS);
        super.init();
    }

    @Override
    public void closeScreen() {
        super.closeScreen();

        openAnimation.setDirection(Direction.BACKWARDS);
        isClosing = true;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        UnitManager moduleManager = HynixMain.getInstance().getModuleManager();

        Animations betterMinecraft = moduleManager.getAnimations();

        this.renderBackground(matrixStack);

        if (betterMinecraft.animationcontainer.getValue() && betterMinecraft.isEnabled()) {
            if (!openAnimation.isDone()) {
                AnimationUtils.sizeAnimation(this.width / 2, this.height / 2, openAnimation.getOutput());
            }
        }
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderHoveredTooltip(matrixStack, mouseX, mouseY);

    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.blit(matrixStack, i, j, 0, 0, this.xSize, this.inventoryRows * 18 + 17);
        this.blit(matrixStack, i, j + this.inventoryRows * 18 + 17, 0, 126, this.xSize, 96);
    }
}
