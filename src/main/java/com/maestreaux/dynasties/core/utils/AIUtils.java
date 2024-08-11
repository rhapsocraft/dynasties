package com.maestreaux.dynasties.core.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;


public class AIUtils {
    public static boolean isCloseEnoughToTarget(Entity entity, BlockPos target, float distance) {
        return entity.blockPosition().distSqr(target) <= (distance * distance);
    }

    public static boolean isCloseEnoughToTarget(Entity entity, BlockPos target) {
        return isCloseEnoughToTarget(entity, target, 2F);
    }
}
