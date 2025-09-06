package com.maestreaux.dynasties.world.blocks;

import com.maestreaux.dynasties.DynastiesMod;
import com.maestreaux.dynasties.world.entities.blockentity.TentBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Tent extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<Tent> CODEC = simpleCodec(Tent::new);

    protected static final VoxelShape COLLISSION_SHAPE = Shapes.or(Block.box(0.0F, 16.0F, 0.0F, 16.0F, 24.0F, 16.0F), Block.box(0.0F, 0.0F, 0.0F, 16.0F, 2.0F, 16.0F));
    protected static final VoxelShape SHAPE_VISUAL = Block.box(0.0F, 0.0F, 0.0F, 16.0F, 16.0F, 16.0F);

    public static final BooleanProperty OCCUPIED = BlockStateProperties.OCCUPIED;

    public Tent(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public Tent(String name) {
        this(
                BlockBehaviour.Properties.of()
                        .noOcclusion()
                        .isSuffocating((p1, p2, p3) -> false)
                        .setId(ResourceKey.create(ForgeRegistries.BLOCKS.getRegistryKey(), ResourceLocation.fromNamespaceAndPath(DynastiesMod.MODID, name)))
        );
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState p_49547_, BlockGetter p_49548_, BlockPos p_49549_, CollisionContext p_49550_) {
        return COLLISSION_SHAPE;
    }

    @Override
    protected VoxelShape getVisualShape(BlockState p_53311_, BlockGetter p_53312_, BlockPos p_53313_, CollisionContext p_53314_) {
        return SHAPE_VISUAL;
    }


    @Override
    protected @NotNull MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection()).setValue(OCCUPIED, false);
    }

    @Override
    public boolean isBed(BlockState state, BlockGetter level, BlockPos pos, @Nullable Entity player) {
        return true;
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OCCUPIED);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new TentBlockEntity(blockPos, blockState);
    }
}
