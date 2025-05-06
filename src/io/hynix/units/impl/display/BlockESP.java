package io.hynix.units.impl.display;

import com.google.common.eventbus.Subscribe;

import io.hynix.events.impl.EventRender3D;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.units.settings.impl.BooleanSetting;
import io.hynix.units.settings.impl.ColorSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.minecart.ChestMinecartEntity;
import net.minecraft.tileentity.*;
import net.minecraft.util.math.BlockPos;
import net.optifine.render.RenderUtils;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

@UnitRegister(name = "BlockESP", category = Category.Display, desc = "ESP на блоки")
public class BlockESP extends Unit {

    final BooleanSetting chest = new BooleanSetting("Chest", true);
    final ColorSetting chestColor = new ColorSetting("ChestColor", new Color(243, 172, 82).getRGB()).setVisible(() -> chest.getValue());

    final BooleanSetting trappedChest = new BooleanSetting("TrappedChest", true);
    final ColorSetting trappedChestColor = new ColorSetting("TrappedChestColor", new Color(143, 109, 62).getRGB()).setVisible(() -> trappedChest.getValue());

    final BooleanSetting barrel = new BooleanSetting("Barrel", true);
    final ColorSetting barrelColor = new ColorSetting("BarrelColor", new Color(250, 225, 62).getRGB()).setVisible(() -> barrel.getValue());

    final BooleanSetting hopper = new BooleanSetting("Hopper", true);
    final ColorSetting hopperColor = new ColorSetting("HopperColor", new Color(62, 137, 250).getRGB()).setVisible(() -> hopper.getValue());

    final BooleanSetting dispenser = new BooleanSetting("Dispenser", true);
    final ColorSetting dispenserColor = new ColorSetting("DispenserColor", new Color(27, 64, 250).getRGB()).setVisible(() -> dispenser.getValue());

    final BooleanSetting dropper = new BooleanSetting("Dropper", true);
    final ColorSetting dropperColor = new ColorSetting("DropperColor", new Color(0, 23, 255).getRGB()).setVisible(() -> dropper.getValue());

    final BooleanSetting furnace = new BooleanSetting("Furnace", true);
    final ColorSetting furnaceColor = new ColorSetting("FurnaceColor", new Color(115, 115, 115).getRGB()).setVisible(() -> furnace.getValue());

    final BooleanSetting enderChest = new BooleanSetting("EnderChest", true);
    final ColorSetting enderChestColor = new ColorSetting("EnderChestColor", new Color(82, 49, 238).getRGB()).setVisible(() -> enderChest.getValue());

    final BooleanSetting shulkerBox = new BooleanSetting("ShulkerBox", true);
    final ColorSetting shulkerBoxColor = new ColorSetting("ShulkerBoxColor", new Color(246, 123, 123).getRGB()).setVisible(() -> shulkerBox.getValue());

    final BooleanSetting mobSpawner = new BooleanSetting("MobSpawner", true);
    final ColorSetting mobSpawnerColor = new ColorSetting("MobSpawnerColor", new Color(112, 236, 166).getRGB()).setVisible(() -> mobSpawner.getValue());

    final BooleanSetting mineCart = new BooleanSetting("MineCart", true);
    final ColorSetting mineCartColor = new ColorSetting("MineCartColor", new Color(255, 255, 255).getRGB()).setVisible(() -> mineCart.getValue());


    public BlockESP() {
        addSettings(chest, chestColor,
                trappedChest, trappedChestColor,
                barrel, barrelColor,
                hopper, hopperColor,
                dispenser, dispenserColor,
                dropper, dropperColor,
                furnace, furnaceColor,
                enderChest, enderChestColor,
                shulkerBox, shulkerBoxColor,
                mobSpawner, mobSpawnerColor,
                mineCart, mineCartColor
        );
    }

    @Subscribe
    private void onRender(EventRender3D e) {
        Map<TileEntityType<?>, Integer> tiles = new HashMap<>(Map.of(
                new ChestTileEntity().getType(), chestColor.getValue(),
                new TrappedChestTileEntity().getType(), trappedChestColor.getValue(),
                new BarrelTileEntity().getType(), barrelColor.getValue(),
                new HopperTileEntity().getType(), hopperColor.getValue(),
                new DispenserTileEntity().getType(), dispenserColor.getValue(),
                new DropperTileEntity().getType(), dropperColor.getValue(),
                new FurnaceTileEntity().getType(), furnaceColor.getValue(),
                new EnderChestTileEntity().getType(), enderChestColor.getValue(),
                new ShulkerBoxTileEntity().getType(), shulkerBoxColor.getValue(),
                new MobSpawnerTileEntity().getType(), mobSpawnerColor.getValue()
        ));

        for (TileEntity tile : mc.world.loadedTileEntityList) {
            if (!tiles.containsKey(tile.getType())) continue;

            BlockPos pos = tile.getPos();
            int color = tiles.get(tile.getType());

            if (tile instanceof ChestTileEntity && chest.getValue()) {
                RenderUtils.drawBlockBox(pos, color);
            } else if (tile instanceof TrappedChestTileEntity && trappedChest.getValue()) {
                RenderUtils.drawBlockBox(pos, color);
            } else if (tile instanceof BarrelTileEntity && barrel.getValue()) {
                RenderUtils.drawBlockBox(pos, color);
            } else if (tile instanceof HopperTileEntity && hopper.getValue()) {
                RenderUtils.drawBlockBox(pos, color);
            } else if (tile instanceof DispenserTileEntity && dispenser.getValue()) {
                RenderUtils.drawBlockBox(pos, color);
            } else if (tile instanceof DropperTileEntity && dropper.getValue()) {
                RenderUtils.drawBlockBox(pos, color);
            } else if (tile instanceof FurnaceTileEntity && furnace.getValue()) {
                RenderUtils.drawBlockBox(pos, color);
            } else if (tile instanceof EnderChestTileEntity && enderChest.getValue()) {
                RenderUtils.drawBlockBox(pos, color);
            } else if (tile instanceof ShulkerBoxTileEntity && shulkerBox.getValue()) {
                RenderUtils.drawBlockBox(pos, color);
            } else if (tile instanceof MobSpawnerTileEntity && mobSpawner.getValue()) {
                RenderUtils.drawBlockBox(pos, color);
            }
        }

        if (mineCart.getValue()) {
            for (Entity entity : mc.world.getAllEntities()) {
                if (entity instanceof ChestMinecartEntity) {
                    RenderUtils.drawBlockBox(entity.getPosition(), mineCartColor.getValue());
                }
            }
        }
    }

}
