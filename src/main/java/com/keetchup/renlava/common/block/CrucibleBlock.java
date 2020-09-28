package com.keetchup.renlava.common.block;

import com.keetchup.renlava.RenLavaConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.Random;

public class CrucibleBlock extends Block {

    public static final IntegerProperty PRIMARY_RESOURCE = IntegerProperty.create("primary_resource", 0, 4);
    public static final IntegerProperty SECONDARY_RESOURCE = IntegerProperty.create("secondary_resource", 0, 16);
    public static final IntegerProperty LAVA_LEVEL = IntegerProperty.create("lava_level", 0, 4);

    private static final VoxelShape[] LAVA_TO_SHAPE = new VoxelShape[]{
            Block.makeCuboidShape(2.0D, 4.0D, 2.0D, 14.0D, 16.0D, 14.0D),
            Block.makeCuboidShape(2.0D, 6.0D, 2.0D, 14.0D, 16.0D, 14.0D),
            Block.makeCuboidShape(2.0D, 8.0D, 2.0D, 14.0D, 16.0D, 14.0D),
            Block.makeCuboidShape(2.0D, 12.0D, 2.0D, 14.0D, 16.0D, 14.0D),
            Block.makeCuboidShape(2.0D, 15.0D, 2.0D, 14.0D, 16.0D, 14.0D)};

    public CrucibleBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(PRIMARY_RESOURCE, 0).with(SECONDARY_RESOURCE, 0).with(LAVA_LEVEL, 0));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(PRIMARY_RESOURCE, SECONDARY_RESOURCE, LAVA_LEVEL);
    }

    @Override
    public ActionResultType onBlockActivated(BlockState blockState, World worldIn, BlockPos blockPos, PlayerEntity playerEntity, Hand handIn, BlockRayTraceResult blockRayTraceResult) {
        ItemStack itemStack = playerEntity.getHeldItem(handIn);
        if (!worldIn.isRemote()) {
            if (itemStack.isEmpty()) {
                return ActionResultType.PASS;
            } else if (itemStack.getItem() == Items.OBSIDIAN) {
                if (this.getPrimaryResource(blockState) < 4) {
                    addPrimaryResource(worldIn, blockPos);
                    if (!playerEntity.isCreative()) {
                        itemStack.shrink(1);
                    }
                    return ActionResultType.CONSUME;
                }
            } else if (itemStack.getItem() == Items.BLAZE_POWDER) {
                if (this.getSecondaryResource(blockState) < 16) {
                    addSecondaryResource(worldIn, blockPos);
                    if (!playerEntity.isCreative()) {
                        itemStack.shrink(1);
                    }
                    return ActionResultType.CONSUME;
                }
            } else if (itemStack.getItem() == Items.BUCKET) {
                if (this.getLavaLevel(blockState) == 4) {
                    worldIn.setBlockState(blockPos, (BlockState) Blocks.CAULDRON.getDefaultState(), 2);
                    if (!playerEntity.isCreative()) {
                        itemStack.shrink(1);
                        if (itemStack.isEmpty()) {
                            playerEntity.setHeldItem(handIn, new ItemStack(Items.LAVA_BUCKET));
                        } else if (!playerEntity.inventory.addItemStackToInventory(new ItemStack(Items.LAVA_BUCKET))) {
                            playerEntity.dropItem(new ItemStack(Items.LAVA_BUCKET), false);
                        }
                    }
                    return ActionResultType.SUCCESS;
                }
            }
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public boolean ticksRandomly(BlockState blockState) {
        if (this.getPrimaryResource(blockState) == 4 && this.getSecondaryResource(blockState) == 16 && this.getLavaLevel(blockState) != 4) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void randomTick(BlockState blockState, ServerWorld worldIn, BlockPos blockPos, Random random) {
        if (this.getLavaLevel(blockState) < 4) {
            if (random.nextInt((int) (100.0F / (getAdjacentHeat(blockPos, worldIn) * getLavaModifier())) + 1) == 0) {
                worldIn.setBlockState(blockPos, (BlockState) blockState.with(LAVA_LEVEL, (Integer) blockState.get(LAVA_LEVEL) + 1), 2);
            }
        }
    }

    private float getAdjacentHeat(BlockPos blockPos, IWorldReader worldIn) {
        float adjVariable = 0.1F;
        for (int x = -1; x <= 1; x++) {
            BlockState blockStateX = worldIn.getBlockState(blockPos.add(x, 0, 0));
            adjVariable += checkSurroundingBlock(blockStateX);
        }
        for (int z = -1; z <= 1; z++) {
            BlockState blockStateZ = worldIn.getBlockState(blockPos.add(0, 0, z));
            adjVariable += checkSurroundingBlock(blockStateZ);
        }
        BlockState blockStateY = worldIn.getBlockState(blockPos.add(0, -1, 0));
        adjVariable += checkSurroundingBlock(blockStateY);
        return adjVariable;
    }

    private float checkSurroundingBlock(BlockState blockState) {
        float surroundingVariable = 0F;
        Block block = blockState.getBlock();
        if (block == Blocks.LAVA && blockState.getFluidState().isSource()) {
            surroundingVariable += 1F;
        } else if (block == Blocks.LAVA && !(blockState.getFluidState().isSource())) {
            surroundingVariable += 0.8F;
        } else if (block == Blocks.CAMPFIRE || block == Blocks.FIRE) {
            surroundingVariable += 0.5F;
        } else if (block == Blocks.TORCH || block == Blocks.WALL_TORCH) {
            surroundingVariable += 0.2F;
        }
        return surroundingVariable;
    }

    @Override
    public void onEntityCollision(BlockState blockState, World worldIn, BlockPos blockPos, Entity entityIn) {
        if (!worldIn.isRemote) {
            if (this.getLavaLevel(blockState) > 0) {
                entityIn.setFire(15);
                entityIn.attackEntityFrom(DamageSource.LAVA, 4.0F);
            }
        }
    }

    private void addPrimaryResource(World worldIn, BlockPos blockPos) {
        int addedPrimaryResourceStack = 0;
        while (this.getPrimaryResource(worldIn.getBlockState(blockPos)) < 4 && addedPrimaryResourceStack < getPrimaryModifier()) {
            BlockState currentBlockState = worldIn.getBlockState(blockPos);
            worldIn.setBlockState(blockPos, (BlockState) currentBlockState.with(PRIMARY_RESOURCE, (Integer) currentBlockState.get(PRIMARY_RESOURCE) + 1), 4);
            addedPrimaryResourceStack++;
        }
    }

    private void addSecondaryResource(World worldIn, BlockPos blockPos) {
        int addedSecondaryResourceStack = 0;
        while (this.getSecondaryResource(worldIn.getBlockState(blockPos)) < 16 && addedSecondaryResourceStack < getSecondaryModifier()) {
            BlockState currentBlockState = worldIn.getBlockState(blockPos);
            worldIn.setBlockState(blockPos, (BlockState) currentBlockState.with(SECONDARY_RESOURCE, (Integer) currentBlockState.get(SECONDARY_RESOURCE) + 1), 4);
            addedSecondaryResourceStack++;
        }
    }

    private int getPrimaryModifier(){
        return RenLavaConfig.RENLAVACONFIG.obsidianModifier.get();
    }

    private int getSecondaryModifier(){
        return RenLavaConfig.RENLAVACONFIG.blazePowderModifier.get();
    }

    private double getLavaModifier(){
        return RenLavaConfig.RENLAVACONFIG.lavaModifier.get();
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

    private int getInsideLevel(BlockState blockState) {
        int currentPrimary = this.getPrimaryResource(blockState);
        if (currentPrimary == 4 && this.getLavaLevel(blockState) == 4) {
            return 0;
        } else {
            return currentPrimary;
        }
    }

    @Override
    public VoxelShape getShape(BlockState blockState, IBlockReader worldIn, BlockPos blockPos, ISelectionContext context) {
        return VoxelShapes.combineAndSimplify(VoxelShapes.fullCube(), VoxelShapes.or(
                Block.makeCuboidShape(0.0D, 0.0D, 4.0D, 16.0D, 3.0D, 12.0D),
                Block.makeCuboidShape(4.0D, 0.0D, 0.0D, 12.0D, 3.0D, 16.0D),
                Block.makeCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 3.0D, 14.0D),
                LAVA_TO_SHAPE[getInsideLevel(blockState)]), IBooleanFunction.ONLY_FIRST);
    }

    @Override
    public VoxelShape getRaytraceShape(BlockState blockState, IBlockReader worldIn, BlockPos blockPos) {
        return LAVA_TO_SHAPE[getInsideLevel(blockState)];
    }
}
