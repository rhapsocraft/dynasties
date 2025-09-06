package com.maestreaux.dynasties.client.renderer.entity;

import com.maestreaux.dynasties.DynastiesMod;
import com.maestreaux.dynasties.client.model.DynastiesVillagerModel;
import com.maestreaux.dynasties.client.renderer.entity.state.DynastiesVillagerRenderState;
import com.maestreaux.dynasties.client.renderer.layer.VillagerClothesLayer;
import com.maestreaux.dynasties.client.renderer.layer.VillagerItemInHandLayer;
import com.maestreaux.dynasties.world.entities.DynastiesVillager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class DynastiesVillagerRenderer extends MobRenderer<DynastiesVillager, DynastiesVillagerRenderState, DynastiesVillagerModel> {
    private static final ResourceLocation VILLAGER_BASE_SKIN = ResourceLocation.fromNamespaceAndPath(DynastiesMod.MODID, "textures/entity/villager.png");

    public DynastiesVillagerRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new DynastiesVillagerModel(ctx.bakeLayer(DynastiesVillagerModel.LAYER_LOCATION)), 0.5F);
        this.addLayer(new VillagerItemInHandLayer<>(this));
        this.addLayer(new VillagerClothesLayer<>(this));

        // this.addLayer(new CustomHeadLayer<>(this, p_174437_.getModelSet(), p_174437_.getItemInHandRenderer()));
        // this.addLayer(new CrossedArmsItemLayer(this, p_174437_.getItemInHandRenderer()));
    }

    public void extractRenderState(DynastiesVillager villager, DynastiesVillagerRenderState renderState, float p_368483_) {
        super.extractRenderState(villager, renderState, p_368483_);
        ArmedEntityRenderState.extractArmedEntityRenderState(villager, renderState, this.itemModelResolver);
        renderState.fleeAnimationState.copyFrom(villager.fleeAnimationState);
        renderState.idleAnimationState.copyFrom(villager.idleAnimationState);
        renderState.idleFaceAnimationState.copyFrom(villager.idleFaceAnimationState);
        renderState.eatAnimationState.copyFrom(villager.eatAnimationState);
        renderState.fallAnimationState.copyFrom(villager.fallAnimationState);
        renderState.bounceAnimationState.copyFrom(villager.bounceAnimationState);
        renderState.turnRightAnimationState.copyFrom(villager.turnRightAnimationState);
        renderState.turnLeftAnimationState.copyFrom(villager.turnLeftAnimationState);
        renderState.swingAnimationState.copyFrom(villager.swingAnimationState);
        renderState.mainArm = villager.getMainArm();


        // TODO: TEMPORARY NOBILITY FLAG
        renderState.isNobility = villager.isNobility();
        renderState.hasItem = !villager.getItemInHand(InteractionHand.MAIN_HAND).isEmpty() || !villager.getItemInHand(InteractionHand.OFF_HAND).isEmpty();

        if (villager.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof BlockItem blockItem) {
            var blockId = BuiltInRegistries.BLOCK.getKey(blockItem.getBlock());
            var itemId = BuiltInRegistries.ITEM.getKey(blockItem);

            renderState.isHoldingBlock = blockId == itemId;
        }

        renderState.isFleeing = villager.isFleeing();
        renderState.isEating = villager.isEating();
        renderState.isFalling = villager.isFalling();
    }

    @Override
    public @NotNull DynastiesVillagerRenderState createRenderState() {
        return new DynastiesVillagerRenderState();
    }

    @Override
    protected void scale(DynastiesVillagerRenderState renderState, PoseStack pPoseStack) {
        float $$3 = 0.9375F;

//        if (entity.isBaby()) {
//            $$3 *= 0.5F;
//            this.shadowRadius = 0.25F;
//        } else {
//            this.shadowRadius = 0.5F;
//        }

        pPoseStack.scale($$3, $$3, $$3);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull DynastiesVillagerRenderState dynastiesVillagerRenderState) {
        return VILLAGER_BASE_SKIN;
    }
}
