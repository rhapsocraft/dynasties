package com.maestreaux.dynasties.world.blocks;

import com.maestreaux.dynasties.DynastiesMod;
import com.maestreaux.dynasties.init.ModMealTypes;
import com.maestreaux.dynasties.world.entities.blockentity.CampfirePotBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;


public class CampfirePot extends BaseEntityBlock implements EntityBlock {
    public static final MapCodec<CampfirePot> CODEC = simpleCodec(CampfirePot::new);
    public static final BooleanProperty COOKED;
    public static final Property<String> MEAL_TYPE;
    public static final IntegerProperty SERVINGS;

    public CampfirePot(BlockBehaviour.Properties properties) {
        super(properties);

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(COOKED, false)
                .setValue(MEAL_TYPE, ModMealTypes.getMealTypeResourceName(ModMealTypes.BEEF_CARROT_STEW))
                .setValue(SERVINGS, 0));
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public CampfirePot(String name) {
        this(
                BlockBehaviour.Properties.of()
                        .noOcclusion()
                        .lightLevel((state) -> 15)
                        .isSuffocating((p1, p2, p3) -> false)
                        .setId(ResourceKey.create(ForgeRegistries.BLOCKS.getRegistryKey(), ResourceLocation.fromNamespaceAndPath(DynastiesMod.MODID, name)))
        );
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new CampfirePotBlockEntity(blockPos, blockState);
    }

    protected @NotNull VoxelShape getVisualShape(BlockState p_312193_, BlockGetter p_310654_, BlockPos p_310658_, CollisionContext p_311129_) {
        return Shapes.empty();
    }

    protected float getShadeBrightness(BlockState p_312407_, BlockGetter p_310193_, BlockPos p_311965_) {
        return 1.0F;
    }

    protected boolean propagatesSkylightDown(BlockState p_312717_) {
        return true;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState()
                .setValue(COOKED, false)
                .setValue(MEAL_TYPE, ModMealTypes.getMealTypeResourceName(ModMealTypes.BEEF_CARROT_STEW))
                .setValue(SERVINGS, 0);
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(COOKED).add(MEAL_TYPE).add(SERVINGS);
    }

    static {
        SERVINGS = IntegerProperty.create("servings", 0, 5);
        COOKED = BooleanProperty.create("cooked");
        MEAL_TYPE = new Property<>("meal_type", String.class) {
            private static final List<String> mealValues = ModMealTypes.MEAL_TYPES.getEntries()
                    .stream().map(registryObject -> {
                        assert registryObject.getKey() != null;
                        // First part of property name should be the mod id
                        return String.valueOf(registryObject.getKey().location()).replace(":", "_");
                    })
                    .toList();

            @Override
            public List<String> getPossibleValues() {
                return mealValues;
            }

            @Override
            public String getName(String s) {
                return s;
            }

            @Override
            public Optional<String> getValue(String s) {
                return s.replace(":", "_").describeConstable();
            }

            @Override
            public int getInternalIndex(String s) {
                return mealValues.indexOf(s);
            }
        };
    }
}
