package com.keetchup.renlava.mixin;

import com.keetchup.renlava.RenLava;
import com.keetchup.renlava.config.RenLavaConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.CauldronBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import static com.keetchup.renlava.CrucibleBlock.PRIMARY_RESOURCE;

@Mixin(CauldronBlock.class)
public class CauldronBlockMixin {

    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    private void primaryResourceUse(BlockState blockState, World world, BlockPos blockPos, PlayerEntity playerEntity, Hand hand, BlockHitResult blockHitResult, CallbackInfoReturnable<ActionResult> callbackInfo) {
        ItemStack itemStack = playerEntity.getStackInHand(hand);
        if (!world.isClient()) {
            if (itemStack.getItem() == RenLavaConfig.getPrimaryResource()) {
                world.setBlockState(blockPos, (BlockState) RenLava.CRUCIBLE.getDefaultState().with(PRIMARY_RESOURCE, getInitialPrimaryValue()), 2);
                if (!playerEntity.abilities.creativeMode) {
                    itemStack.decrement(1);
                }
                callbackInfo.setReturnValue(ActionResult.CONSUME);
                callbackInfo.cancel();
            }
        }
        callbackInfo.setReturnValue(ActionResult.SUCCESS);
    }

    private int getInitialPrimaryValue() {
        int primaryResourceModifier = RenLavaConfig.getPrimaryConfigModifier();
        if (primaryResourceModifier > 4) {
            return 4;
        } else if (primaryResourceModifier < 1) {
            return 1;
        } else {
            return primaryResourceModifier;
        }
    }

}
