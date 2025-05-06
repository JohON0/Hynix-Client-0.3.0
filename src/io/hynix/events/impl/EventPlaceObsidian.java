package io.hynix.events.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

@Data
@AllArgsConstructor
public class EventPlaceObsidian {
    private Block block;
    private BlockPos pos;
}
