package com.keetchup.renlava.mixin;

import com.keetchup.renlava.RenLava;
import com.keetchup.renlava.config.RenLavaConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.CauldronBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CancellationException;

import static com.keetchup.renlava.CrucibleBlock.BLAZE_POWDER;
import static com.keetchup.renlava.CrucibleBlock.OBSIDIAN;

@Mixin(CauldronBlock.class)
public class CauldronBlockMixin {

    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    private void obsidianUse(BlockState blockState, World world, BlockPos blockPos, PlayerEntity playerEntity, Hand hand, BlockHitResult blockHitResult, CallbackInfoReturnable<ActionResult> callbackInfo) {
        ItemStack itemStack = playerEntity.getStackInHand(hand);
        if (!world.isClient()) {
            if (itemStack.getItem() == Items.OBSIDIAN) {
                world.setBlockState(blockPos, (BlockState) RenLava.CRUCIBLE.getDefaultState().with(OBSIDIAN, getInitialObsidianValue()), 2);
                if (!playerEntity.abilities.creativeMode) {
                    itemStack.decrement(1);
                }
                callbackInfo.setReturnValue(ActionResult.CONSUME);
                callbackInfo.cancel();
            }
        }
        callbackInfo.setReturnValue(ActionResult.SUCCESS);
    }

    private int getInitialObsidianValue() {
        int obsidianModifier = RenLavaConfig.getObsidianModifier();
        if (obsidianModifier > 4) {
            return 4;
        } else if (obsidianModifier < 1) {
            return 1;
        } else {
            return obsidianModifier;
        }
    }

}
