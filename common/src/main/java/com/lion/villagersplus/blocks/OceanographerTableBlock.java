package com.lion.villagersplus.blocks;

import com.lion.villagersplus.blockentities.OceanographerTableBlockEntity;
import com.lion.villagersplus.init.VPParticles;
import com.lion.villagersplus.init.VPTags;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class OceanographerTableBlock extends WorkstationBlock {
    public static final IntProperty CORALS;
    public static final IntProperty FISH;
    public static final DirectionProperty FACING;
    public static final BooleanProperty IS_FILLED;

    public OceanographerTableBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(CORALS, 0).with(FISH, 0).with(IS_FILLED, false).with(FACING, Direction.NORTH));
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new OceanographerTableBlockEntity(pos, state);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (random.nextBoolean()) {
            double x = pos.getX() + 0.1D + (pos.getX() + 0.9D - (pos.getX() + 0.1D)) * random.nextDouble();
            double y = pos.getY() + 0.1D + (pos.getY() + 0.4D - (pos.getY() + 0.1D)) * random.nextDouble();
            double z = pos.getZ() + 0.1D + (pos.getZ() + 0.9D - (pos.getZ() + 0.1D)) * random.nextDouble();

            world.addParticle(VPParticles.BUBBLE_PARTICLE, x, y, z, 0.0D, 0.000001D, 0.0D);
        }

    }

    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack itemStack = player.getStackInHand(hand);

        if (world.getBlockEntity(pos) instanceof OceanographerTableBlockEntity blockEntity) {
                if (itemStack.isIn(VPTags.AQUARIUM_PLANTABLE_ITEMS) && state.get(CORALS) < 4) {
                    blockEntity.insertCoral(itemStack, state.get(CORALS));

                    if (!player.isCreative()) {
                        itemStack.decrement(1);
                    }

                    if (!world.isClient()) {
                        world.setBlockState(pos, state.with(CORALS, state.get(CORALS) + 1), 3);
                        world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                    }

                    if (world.isClient) {
                        world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_CORAL_BLOCK_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
                    }

                    return ActionResult.success(world.isClient);
                } else if (itemStack.getItem() instanceof EntityBucketItem bucketItem && state.get(FISH) < 1) {
                    blockEntity.insertCoral(itemStack, 4);

                    if (!player.isCreative()) {
                        itemStack.decrement(1);
                    }

                    if (!world.isClient()) {
                        world.setBlockState(pos, state.with(FISH, state.get(FISH) + 1), 3);
                        world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                    }

                    if (world.isClient) player.playSound(SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);

                    return ActionResult.success(world.isClient);
                } /* else if (itemStack.isOf(Items.WATER_BUCKET) && state.get(FISH) >= 1) {
                    ItemStack fish = blockEntity.getStack(4);
                    if (fish != null) {
                        ItemUsage.exchangeStack(itemStack, player, fish);
                        blockEntity.removeStack(4);

                        if (world.isClient) {
                            player.playSound(SoundEvents.ITEM_BUCKET_FILL_FISH, 1.0F, 1.0F);
                        }

                        if (!world.isClient()) {
                            world.setBlockState(pos, state.with(FISH, 0), 3);
                            world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                        }

                        return ActionResult.success(world.isClient);
                    }
                }
                     */
        }
        return ActionResult.PASS;
    }

    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof OceanographerTableBlockEntity) {
                ItemScatterer.spawn(world, pos, (OceanographerTableBlockEntity)blockEntity);
            }

            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(CORALS, FISH, IS_FILLED, FACING);
    }

    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return (BlockState)state.with(FACING, rotation.rotate((Direction)state.get(FACING)));
    }

    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation((Direction)state.get(FACING)));
    }

    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return (Integer)state.get(FISH) + state.get(CORALS);
    }

    static {
        IS_FILLED = BooleanProperty.of("is_filled");
        FISH = IntProperty.of("fish", 0, 1);
        CORALS = IntProperty.of("corals", 0, 4);
        FACING = HorizontalFacingBlock.FACING;
    }
}
