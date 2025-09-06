package com.maestreaux.dynasties.core.production.asset;

import com.maestreaux.dynasties.core.production.Production;
import com.maestreaux.dynasties.world.entities.blockentity.WorkstationBlockEntity;

public class BlockEntityAsset<B extends WorkstationBlockEntity> implements Production.IAsset<B> {
    private final B blockEntity;

    public BlockEntityAsset(B blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public B get() {
        return this.blockEntity;
    }
}