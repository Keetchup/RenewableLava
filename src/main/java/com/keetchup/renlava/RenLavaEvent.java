package com.keetchup.renlava;

import com.keetchup.renlava.common.block.CrucibleBlock;
import com.keetchup.renlava.common.block.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RenLavaEvent {

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        World world = event.getWorld();
        ItemStack itemStack = event.getItemStack();
        BlockPos blockPos = event.getPos();
        if (!world.isRemote) {
            if (world.getBlockState(blockPos).getBlock() == Blocks.CAULDRON) {
                if (itemStack.getItem() == Items.OBSIDIAN) {
                    world.setBlockState(blockPos, (BlockState) ModBlocks.CRUCIBLE_BLOCK.get().getDefaultState().with(CrucibleBlock.PRIMARY_RESOURCE, getInitialPrimaryValue()), 2);
                    if (!event.getPlayer().isCreative()) {
                        itemStack.shrink(1);
                    }
                    event.setCancellationResult(ActionResultType.CONSUME);
                    event.setCanceled(true);
                }
            }
        }
        event.setCancellationResult(ActionResultType.func_233537_a_(world.isRemote));
    }

    private int getInitialPrimaryValue() {
        int primaryResourceModifier = RenLavaConfig.RENLAVACONFIG.obsidianModifier.get();
        if (primaryResourceModifier > 4) {
            return 4;
        } else if (primaryResourceModifier < 1) {
            return 1;
        } else {
            return primaryResourceModifier;
        }
    }
}
