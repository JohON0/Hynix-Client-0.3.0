package net.minecraft.client.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import java.util.Random;

import io.hynix.HynixMain;
import io.hynix.units.impl.miscellaneous.BetterMinecraft;
import net.minecraft.block.*;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourceManagerReloadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.VanillaResourceType;
import net.optifine.reflect.Reflector;

public class BlockRendererDispatcher implements IResourceManagerReloadListener
{
    private final BlockModelShapes blockModelShapes;
    private final BlockModelRenderer blockModelRenderer;
    private final FluidBlockRenderer fluidRenderer;
    private final Random random = new Random();
    private final BlockColors blockColors;

    public BlockRendererDispatcher(BlockModelShapes shapes, BlockColors colors)
    {
        this.blockModelShapes = shapes;
        this.blockColors = colors;

        if (Reflector.ForgeBlockModelRenderer_Constructor.exists())
        {
            this.blockModelRenderer = (BlockModelRenderer)Reflector.newInstance(Reflector.ForgeBlockModelRenderer_Constructor, this.blockColors);
        }
        else
        {
            this.blockModelRenderer = new BlockModelRenderer(this.blockColors);
        }

        this.fluidRenderer = new FluidBlockRenderer();
    }

    public BlockModelShapes getBlockModelShapes()
    {
        return this.blockModelShapes;
    }

    public void renderBlockDamage(BlockState blockStateIn, BlockPos posIn, IBlockDisplayReader lightReaderIn, MatrixStack matrixStackIn, IVertexBuilder vertexBuilderIn)
    {
        this.renderBlockDamage(blockStateIn, posIn, lightReaderIn, matrixStackIn, vertexBuilderIn, EmptyModelData.INSTANCE);
    }

    public void renderBlockDamage(BlockState p_renderBlockDamage_1_, BlockPos p_renderBlockDamage_2_, IBlockDisplayReader p_renderBlockDamage_3_, MatrixStack p_renderBlockDamage_4_, IVertexBuilder p_renderBlockDamage_5_, IModelData p_renderBlockDamage_6_)
    {
        if (p_renderBlockDamage_1_.getRenderType() == BlockRenderType.MODEL)
        {
            IBakedModel ibakedmodel = this.blockModelShapes.getModel(p_renderBlockDamage_1_);
            long i = p_renderBlockDamage_1_.getPositionRandom(p_renderBlockDamage_2_);
            this.blockModelRenderer.renderModel(p_renderBlockDamage_3_, ibakedmodel, p_renderBlockDamage_1_, p_renderBlockDamage_2_, p_renderBlockDamage_4_, p_renderBlockDamage_5_, true, this.random, i, OverlayTexture.NO_OVERLAY, p_renderBlockDamage_6_);
        }
    }

    public boolean renderModel(BlockState blockStateIn, BlockPos posIn, IBlockDisplayReader lightReaderIn, MatrixStack matrixStackIn, IVertexBuilder vertexBuilderIn, boolean checkSides, Random rand)
    {
        return this.renderModel(blockStateIn, posIn, lightReaderIn, matrixStackIn, vertexBuilderIn, checkSides, rand, EmptyModelData.INSTANCE);
    }

    public boolean renderModel(BlockState p_renderModel_1_, BlockPos p_renderModel_2_, IBlockDisplayReader p_renderModel_3_, MatrixStack p_renderModel_4_, IVertexBuilder p_renderModel_5_, boolean p_renderModel_6_, Random p_renderModel_7_, IModelData p_renderModel_8_)
    {
        if (BetterMinecraft.isFpsMode() || HynixMain.getInstance().getModuleManager().getNoRender().isEnabled() && HynixMain.getInstance().getModuleManager().getNoRender().element.is("Трава").getValue()) {
            if (p_renderModel_1_.getBlock() instanceof TallGrassBlock ||
                    p_renderModel_1_.getBlock() instanceof FlowerBlock ||
                    p_renderModel_1_.getBlock() instanceof TallFlowerBlock ||
                    p_renderModel_1_.getBlock() instanceof SeaGrassBlock ||
                    p_renderModel_1_.getBlock() == Blocks.TALL_GRASS ||
                    p_renderModel_1_.getBlock() instanceof TallSeaGrassBlock) {
                return false;
            }
        }

        try
        {
            BlockRenderType blockrendertype = p_renderModel_1_.getRenderType();
            return blockrendertype != BlockRenderType.MODEL ? false : this.blockModelRenderer.renderModel(p_renderModel_3_, this.getModelForState(p_renderModel_1_), p_renderModel_1_, p_renderModel_2_, p_renderModel_4_, p_renderModel_5_, p_renderModel_6_, p_renderModel_7_, p_renderModel_1_.getPositionRandom(p_renderModel_2_), OverlayTexture.NO_OVERLAY, p_renderModel_8_);
        }
        catch (Throwable throwable1)
        {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable1, "Tesselating block in world");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being tesselated");
            CrashReportCategory.addBlockInfo(crashreportcategory, p_renderModel_2_, p_renderModel_1_);
            throw new ReportedException(crashreport);
        }
    }

    public boolean renderFluid(BlockPos posIn, IBlockDisplayReader lightReaderIn, IVertexBuilder vertexBuilderIn, FluidState fluidStateIn)
    {
        try
        {
            return this.fluidRenderer.render(lightReaderIn, posIn, vertexBuilderIn, fluidStateIn);
        }
        catch (Throwable throwable)
        {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Tesselating liquid in world");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being tesselated");
            CrashReportCategory.addBlockInfo(crashreportcategory, posIn, (BlockState)null);
            throw new ReportedException(crashreport);
        }
    }

    public BlockModelRenderer getBlockModelRenderer()
    {
        return this.blockModelRenderer;
    }

    public IBakedModel getModelForState(BlockState state)
    {
        return this.blockModelShapes.getModel(state);
    }

    public void renderBlock(BlockState blockStateIn, MatrixStack matrixStackIn, IRenderTypeBuffer bufferTypeIn, int combinedLightIn, int combinedOverlayIn)
    {
        this.renderBlock(blockStateIn, matrixStackIn, bufferTypeIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
    }

    public void renderBlock(BlockState p_renderBlock_1_, MatrixStack p_renderBlock_2_, IRenderTypeBuffer p_renderBlock_3_, int p_renderBlock_4_, int p_renderBlock_5_, IModelData p_renderBlock_6_) {
        BlockRenderType blockRenderType = p_renderBlock_1_.getRenderType();

        if (blockRenderType != BlockRenderType.INVISIBLE) {
            switch (blockRenderType) {
                case MODEL:
                    IBakedModel bakedModel = this.getModelForState(p_renderBlock_1_);
                    int color = this.blockColors.getColor(p_renderBlock_1_, (IBlockDisplayReader) null, (BlockPos) null, 0);
                    float red = (float)(color >> 16 & 255) / 255.0F;
                    float green = (float)(color >> 8 & 255) / 255.0F;
                    float blue = (float)(color & 255) / 255.0F;
                    this.blockModelRenderer.renderModel(p_renderBlock_2_.getLast(), p_renderBlock_3_.getBuffer(RenderTypeLookup.func_239220_a_(p_renderBlock_1_, false)), p_renderBlock_1_, bakedModel, red, green, blue, p_renderBlock_4_, p_renderBlock_5_, p_renderBlock_6_);
                    break;

                case ENTITYBLOCK_ANIMATED:
                    ItemStack itemStack = new ItemStack(p_renderBlock_1_.getBlock());
                    ItemStackTileEntityRenderer.instance.func_239207_a_(itemStack, ItemCameraTransforms.TransformType.NONE, p_renderBlock_2_, p_renderBlock_3_, p_renderBlock_4_, p_renderBlock_5_);
                    break;
            }
        }
    }

    public void onResourceManagerReload(IResourceManager resourceManager)
    {
        this.fluidRenderer.initAtlasSprites();
    }

    public IResourceType getResourceType()
    {
        return VanillaResourceType.MODELS;
    }
}
