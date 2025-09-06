package com.maestreaux.dynasties.init;

import com.maestreaux.dynasties.core.production.Production;
import com.maestreaux.dynasties.core.production.asset.BlockEntityWorkstationAssetAccessor;
import com.maestreaux.dynasties.core.production.product.ItemProduct;
import com.maestreaux.dynasties.core.production.requirement.ProcurementsRequirement;
import com.maestreaux.dynasties.world.entities.blockentity.SpinningWheelBlockEntity;
import net.minecraft.world.item.Items;

import java.util.Map;

public class ModProductions {
    public static Production<?, ?, ?> WOOL_YARN = new Production<>(new ItemProduct(ModItems.WOOL_YARN.get(), 1), new BlockEntityWorkstationAssetAccessor<>(SpinningWheelBlockEntity.class), new ProcurementsRequirement(Map.of(Items.WHITE_WOOL, 4)));
}
