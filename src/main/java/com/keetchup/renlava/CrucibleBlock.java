package com.keetchup.renlava;

import com.keetchup.renlava.config.RenLavaConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
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
import net.minecraft.util.Hand;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.Random;

public class CrucibleBlock extends Block {

    public static final IntProperty OBSIDIAN = IntProperty.of("obsidian", 0, 4);
    public static final IntProperty BLAZE_POWDER = IntProperty.of("blaze_powder", 0, 16);
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
        setDefaultState(getStateManager().getDefaultState().with(OBSIDIAN, 0));
        setDefaultState(getStateManager().getDefaultState().with(BLAZE_POWDER, 0));
        setDefaultState(getStateManager().getDefaultState().with(LAVA_LEVEL, 0));
    }

    public ActionResult onUse(BlockState blockState, World world, BlockPos blockPos, PlayerEntity playerEntity, Hand hand, BlockHitResult blockHitResult) {
        ItemStack itemStack = playerEntity.getStackInHand(hand);
        if (!world.isClient()) {
            if (itemStack.isEmpty()) {
                return ActionResult.PASS;
            } else if (itemStack.getItem() == Items.OBSIDIAN) {
                if (this.getObsidian(blockState) < 4) {
                    addObsidian(world, blockPos);
                    if (!playerEntity.abilities.creativeMode) {
                        itemStack.decrement(1);
                    }
                    return ActionResult.CONSUME;
                }
            } else if (itemStack.getItem() == Items.BLAZE_POWDER) {
                if (this.getBlazePowder(blockState) < 16) {
                    addBlazePowder(world, blockPos);
                    if (!playerEntity.abilities.creativeMode) {
                        itemStack.decrement(1);
                    }
                    return ActionResult.CONSUME;
                }
            } else if (itemStack.getItem() == Items.BUCKET) {
                if (this.getLavaLevel(blockState) == 4) {
                    world.setBlockState(blockPos, (BlockState) Blocks.CAULDRON.getDefaultState(), 2);
                    if (!playerEntity.abilities.creativeMode) {
                        playerEntity.setStackInHand(hand, new ItemStack(Items.LAVA_BUCKET));
                    }
                    world.playSound((PlayerEntity) null, blockPos, SoundEvents.ITEM_BUCKET_FILL_LAVA, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    return ActionResult.SUCCESS;
                }
            } else {
                return ActionResult.PASS;
            }
        }
        return ActionResult.success(world.isClient);
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
        if (this.getLavaLevel(blockState) != 4 && this.getObsidian(blockState) == 4 && this.getBlazePowder(blockState) == 16) {
            return true;
        } else {
            return false;
        }
    }

    public void randomTick(BlockState blockState, ServerWorld world, BlockPos blockPos, Random random) {
        int lavaLevel = this.getLavaLevel(blockState);
        if (lavaLevel < 4) {
            float adjHeat = getAdjacentHeat(blockPos, world);
            if (random.nextInt((int) (100.0F / (adjHeat * getLavaChanceModifier())) + 1) == 0) {
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
        System.out.println(adjVariable);
        return adjVariable;
    }

    private float checkSurroundingBlock(BlockState blockState) {
        float surroundingVariable = 0F;
        Block block = blockState.getBlock();
        if (block == Blocks.LAVA && blockState.getFluidState().isStill()) {
            surroundingVariable += 1F;
        } else if (block == Blocks.LAVA && !(blockState.getFluidState().isStill())) {
            surroundingVariable += 0.8F;
        } else if (block == Blocks.SOUL_CAMPFIRE || block == Blocks.SOUL_FIRE) {
            surroundingVariable += 0.65F;
        } else if (block == Blocks.CAMPFIRE || block == Blocks.FIRE) {
            surroundingVariable += 0.5F;
        } else if (block == Blocks.TORCH || block == Blocks.WALL_TORCH || block == Blocks.SOUL_TORCH || block == Blocks.SOUL_WALL_TORCH) {
            surroundingVariable += 0.2F;
        }
        return surroundingVariable;
    }

    private void addObsidian(World world, BlockPos blockPos) {
        int addedObsidianStack = 0;
        while (this.getObsidian(world.getBlockState(blockPos)) < 4 && addedObsidianStack < getStacksPerObsidian()) {
            BlockState currentBlockState = world.getBlockState(blockPos);
            world.setBlockState(blockPos, currentBlockState.with(OBSIDIAN, (Integer) currentBlockState.get(OBSIDIAN) + 1), 4);
            addedObsidianStack++;
        }
    }

    private void addBlazePowder(World world, BlockPos blockPos) {
        int addedBlazePowderStack = 0;
        while (this.getBlazePowder(world.getBlockState(blockPos)) < 16 && addedBlazePowderStack < getStacksPerBlazePowder()) {
            BlockState currentBlockState = world.getBlockState(blockPos);
            world.setBlockState(blockPos, currentBlockState.with(BLAZE_POWDER, (Integer) currentBlockState.get(BLAZE_POWDER) + 1), 4);
            addedBlazePowderStack++;
        }
    }

    private int getStacksPerObsidian() {
        return RenLavaConfig.getObsidianModifier();
    }

    private int getStacksPerBlazePowder() {
        return RenLavaConfig.getBlazePowderModifier();
    }

    private float getLavaChanceModifier() {
        return RenLavaConfig.getLavaConvertChanceModifier();
    }

    private int getObsidian(BlockState blockState) {
        return blockState.get(OBSIDIAN);
    }

    private int getBlazePowder(BlockState blockState) {
        return blockState.get(BLAZE_POWDER);
    }

    private int getLavaLevel(BlockState blockState) {
        return blockState.get(LAVA_LEVEL);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(OBSIDIAN);
        stateManager.add(BLAZE_POWDER);
        stateManager.add(LAVA_LEVEL);
    }

    public VoxelShape getOutlineShape(BlockState blockState, BlockView world, BlockPos blockPos, ShapeContext shapeContext) {
        int currentLevel = getInsideLevel(blockState);
        return VoxelShapes.combineAndSimplify(VoxelShapes.fullCube(), VoxelShapes.union(
                createCuboidShape(0.0D, 0.0D, 4.0D, 16.0D, 3.0D, 12.0D),
                createCuboidShape(4.0D, 0.0D, 0.0D, 12.0D, 3.0D, 16.0D),
                createCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 3.0D, 14.0D),
                LAVA_TO_SHAPE[currentLevel]), BooleanBiFunction.ONLY_FIRST);
    }

    private int getInsideLevel(BlockState blockState) {
        int currentObsidian = this.getObsidian(blockState);
        int lavaToShape = 0;
        if (currentObsidian == 4) {
            int currentLavaLevel = this.getLavaLevel(blockState);
            if (currentLavaLevel == 0) {
                lavaToShape = 4;
            } else if (currentLavaLevel == 1) {
                lavaToShape = 3;
            } else if (currentLavaLevel == 2) {
                lavaToShape = 2;
            } else if (currentLavaLevel == 3) {
                lavaToShape = 1;
            } else {
                lavaToShape = 0;
            }
        } else {
            switch (currentObsidian) {
                case 0:
                    lavaToShape = 0;
                    break;
                case 1:
                    lavaToShape = 1;
                    break;
                case 2:
                    lavaToShape = 2;
                    break;
                case 3:
                    lavaToShape = 3;
                    break;
            }
        }
        return lavaToShape;
    }

}
