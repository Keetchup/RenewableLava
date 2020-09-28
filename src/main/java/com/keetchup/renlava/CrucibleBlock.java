package com.keetchup.renlava;

import com.keetchup.renlava.config.RenLavaConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityContext;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BooleanBiFunction;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.Random;

public class CrucibleBlock extends Block {

    public static final IntProperty PRIMARY_RESOURCE = IntProperty.of("primary_resource", 0, 4);
    public static final IntProperty SECONDARY_RESOURCE = IntProperty.of("secondary_resource", 0, 16);
    public static final IntProperty LAVA_LEVEL = IntProperty.of("lava_level", 0, 4);

    private static final VoxelShape[] LAVA_TO_SHAPE = new VoxelShape[]{
            Block.createCuboidShape(2.0D, 4.0D, 2.0D, 14.0D, 16.0D, 14.0D),     //empty
            Block.createCuboidShape(2.0D, 6.0D, 2.0D, 14.0D, 16.0D, 14.0D),     //one quarter
            Block.createCuboidShape(2.0D, 8.0D, 2.0D, 14.0D, 16.0D, 14.0D),     //one half
            Block.createCuboidShape(2.0D, 12.0D, 2.0D, 14.0D, 16.0D, 14.0D),    //three quarters
            Block.createCuboidShape(2.0D, 15.0D, 2.0D, 14.0D, 16.0D, 14.0D)     //full
    };

    public CrucibleBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(PRIMARY_RESOURCE, 0).with(SECONDARY_RESOURCE, 0).with(LAVA_LEVEL, 0));
    }

    public ActionResult onUse(BlockState blockState, World world, BlockPos blockPos, PlayerEntity playerEntity, Hand hand, BlockHitResult blockHitResult) {
        ItemStack itemStack = playerEntity.getStackInHand(hand);
        if (!world.isClient()) {
            if (itemStack.isEmpty()) {
                return ActionResult.PASS;
            } else if (itemStack.getItem() == RenLavaConfig.getPrimaryResource()) {
                if (this.getPrimaryResource(blockState) < 4) {
                    addPrimaryResource(world, blockPos);
                    if (!playerEntity.abilities.creativeMode) {
                        itemStack.decrement(1);
                    }
                    return ActionResult.CONSUME;
                }
            } else if (itemStack.getItem() == RenLavaConfig.getSecondaryResource()) {
                if (this.getSecondaryResource(blockState) < 16) {
                    addSecondaryResource(world, blockPos);
                    if (!playerEntity.abilities.creativeMode) {
                        itemStack.decrement(1);
                    }
                    return ActionResult.CONSUME;
                }
            } else if (itemStack.getItem() == Items.BUCKET) {
                if (this.getLavaLevel(blockState) == 4) {
                    world.setBlockState(blockPos, (BlockState) Blocks.CAULDRON.getDefaultState(), 2);
                    if (!playerEntity.abilities.creativeMode) {
                        itemStack.decrement(1);
                        if (itemStack.isEmpty()) {
                            playerEntity.setStackInHand(hand, new ItemStack(Items.LAVA_BUCKET));
                        } else if (!playerEntity.inventory.insertStack(new ItemStack(Items.LAVA_BUCKET))) {
                            playerEntity.dropItem(new ItemStack(Items.LAVA_BUCKET), false);
                        }
                    }
                    world.playSound((PlayerEntity) null, blockPos, SoundEvents.ITEM_BUCKET_FILL_LAVA, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    return ActionResult.SUCCESS;
                }
            } else {
                return ActionResult.PASS;
            }
        }
        return ActionResult.SUCCESS;
    }

    public void onEntityCollision(BlockState blockState, World world, BlockPos blockPos, Entity entity) {
        if (!world.isClient()) {
            if (this.getLavaLevel(blockState) > 0) {
                entity.setOnFireFor(15);
                entity.damage(DamageSource.LAVA, 4.0F);
            }
        }
    }

    public boolean hasRandomTicks(BlockState blockState) {
        if (this.getLavaLevel(blockState) != 4 && this.getPrimaryResource(blockState) == 4 && this.getSecondaryResource(blockState) == 16) {
            return true;
        } else {
            return false;
        }
    }

    public void randomTick(BlockState blockState, ServerWorld world, BlockPos blockPos, Random random) {
        if (this.getLavaLevel(blockState) < 4) {
            if (random.nextInt((int) (100.0F / (getAdjacentHeat(blockPos, world) * getLavaChanceModifier())) + 1) == 0) {
                world.setBlockState(blockPos, (BlockState) blockState.with(LAVA_LEVEL, (Integer) blockState.get(LAVA_LEVEL) + 1), 2);
            }
        }
    }

    private float getAdjacentHeat(BlockPos blockPos, BlockView world) {
        float adjVariable = 0.1F;
        for (int x = -1; x <= 1; x++) {
            BlockState blockStateX = world.getBlockState(blockPos.add(x, 0, 0));
            adjVariable += checkSurroundingBlock(blockStateX);
        }
        for (int z = -1; z <= 1; z++) {
            BlockState blockStateZ = world.getBlockState(blockPos.add(0, 0, z));
            adjVariable += checkSurroundingBlock(blockStateZ);
        }
        BlockState blockStateY = world.getBlockState(blockPos.add(0, -1, 0));
        adjVariable += checkSurroundingBlock(blockStateY);
        return adjVariable;
    }

    private float checkSurroundingBlock(BlockState blockState) {
        float surroundingVariable = 0F;
        Block block = blockState.getBlock();
        if (block == Blocks.LAVA && blockState.getFluidState().isStill()) {
            surroundingVariable += 1F;
        } else if (block == Blocks.LAVA && !(blockState.getFluidState().isStill())) {
            surroundingVariable += 0.8F;
        } else if (block == Blocks.CAMPFIRE || block == Blocks.FIRE) {
            surroundingVariable += 0.5F;
        } else if (block == Blocks.TORCH || block == Blocks.WALL_TORCH) {
            surroundingVariable += 0.2F;
        }
        return surroundingVariable;
    }

    private void addPrimaryResource(World world, BlockPos blockPos) {
        int addedPrimaryResourceStack = 0;
        while (this.getPrimaryResource(world.getBlockState(blockPos)) < 4 && addedPrimaryResourceStack < getPrimaryModifier()) {
            BlockState currentBlockState = world.getBlockState(blockPos);
            world.setBlockState(blockPos, currentBlockState.with(PRIMARY_RESOURCE, (Integer) currentBlockState.get(PRIMARY_RESOURCE) + 1), 4);
            addedPrimaryResourceStack++;
        }
    }

    private void addSecondaryResource(World world, BlockPos blockPos) {
        int addedSecondaryResourceStack = 0;
        while (this.getSecondaryResource(world.getBlockState(blockPos)) < 16 && addedSecondaryResourceStack < getSecondaryModifier()) {
            BlockState currentBlockState = world.getBlockState(blockPos);
            world.setBlockState(blockPos, currentBlockState.with(SECONDARY_RESOURCE, (Integer) currentBlockState.get(SECONDARY_RESOURCE) + 1), 4);
            addedSecondaryResourceStack++;
        }
    }

    private int getPrimaryModifier() {
        return RenLavaConfig.getPrimaryConfigModifier();
    }

    private int getSecondaryModifier() {
        return RenLavaConfig.getSecondaryConfigModifier();
    }

    private float getLavaChanceModifier() {
        return RenLavaConfig.getLavaConvertChanceModifier();
    }

    private int getPrimaryResource(BlockState blockState) {
        return blockState.get(PRIMARY_RESOURCE);
    }

    private int getSecondaryResource(BlockState blockState) {
        return blockState.get(SECONDARY_RESOURCE);
    }

    private int getLavaLevel(BlockState blockState) {
        return blockState.get(LAVA_LEVEL);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(PRIMARY_RESOURCE).add(SECONDARY_RESOURCE).add(LAVA_LEVEL);
    }

    public VoxelShape getOutlineShape(BlockState blockState, BlockView world, BlockPos blockPos, EntityContext entityContext) {
        int currentLevel = getInsideLevel(blockState);
        return VoxelShapes.combineAndSimplify(VoxelShapes.fullCube(), VoxelShapes.union(
                createCuboidShape(0.0D, 0.0D, 4.0D, 16.0D, 3.0D, 12.0D),
                createCuboidShape(4.0D, 0.0D, 0.0D, 12.0D, 3.0D, 16.0D),
                createCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 3.0D, 14.0D),
                LAVA_TO_SHAPE[getInsideLevel(blockState)]), BooleanBiFunction.ONLY_FIRST);
    }

    private int getInsideLevel(BlockState blockState) {
        int currentPrimary = this.getPrimaryResource(blockState);
        if (currentPrimary == 4 && this.getLavaLevel(blockState) == 4) {
            return 0;
        } else {
            return currentPrimary;
        }
    }

}
