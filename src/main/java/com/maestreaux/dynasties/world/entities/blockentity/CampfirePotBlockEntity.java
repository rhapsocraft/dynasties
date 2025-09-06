package com.maestreaux.dynasties.world.entities.blockentity;

import com.maestreaux.dynasties.core.MealType;
import com.maestreaux.dynasties.init.ModBlockEntityTypes;
import com.maestreaux.dynasties.init.ModMealTypes;
import com.maestreaux.dynasties.world.blocks.CampfirePot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CampfirePotBlockEntity extends BlockEntity {
    private MealType meal;
    private int servings;

    public CampfirePotBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public CampfirePotBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntityTypes.CAMPFIRE_POT_BE.get(), pPos, pBlockState);
    }

    public boolean hasContents() {
        return this.getMeal() != null && this.servings > 0;
    }

    public void setMeal(MealType mealType, int servings) {
        this.meal = mealType;
        this.servings = servings;

        this.updateCookState();
    }

    public MealType getMeal() {
        return this.meal;
    }

    public int getServingsLevel() {
        return Math.round(((float) this.servings / this.meal.getServings()) * 5F);
    }

    public void updateCookState() {
        if (this.level != null && this.meal != null) {
            this.level.setBlockAndUpdate(this.getBlockPos(),
                    this.getBlockState()
                            .setValue(CampfirePot.COOKED, this.servings > 0)
                            .setValue(CampfirePot.SERVINGS, this.getServingsLevel())
                            .setValue(CampfirePot.MEAL_TYPE, ModMealTypes.getMealTypeResourceName(this.meal))
            );
        }

        this.setChanged();

    }

    public ItemStack getServing() {
        if (this.servings > 0) {
            this.servings -= 1;

            this.updateCookState();

            return new ItemStack(this.meal.getItem());
        } else {
            this.updateCookState();

            return ItemStack.EMPTY;
        }
    }

    @Override
    protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider lookup) {
        super.loadAdditional(compoundTag, lookup);
        this.servings = compoundTag.getInt("villagerdynasties:campfire_pot_servings");
        var mealKey = ResourceLocation.parse(compoundTag.getString("villagerdynasties:campfire_pot_meal"));
        this.meal = ModMealTypes.MEAL_TYPE_REGISTRY.get().getValue(mealKey);

        if (level != null) {
            this.updateCookState();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider lookup) {
        super.saveAdditional(compoundTag, lookup);
        compoundTag.putInt("villagerdynasties:campfire_pot_servings", this.servings);
        var mealKey = ModMealTypes.MEAL_TYPE_REGISTRY.get().getKey(this.meal);

        if (mealKey != null) {
            compoundTag.putString("villagerdynasties:campfire_pot_meal", mealKey.toString());
        }
    }
}
