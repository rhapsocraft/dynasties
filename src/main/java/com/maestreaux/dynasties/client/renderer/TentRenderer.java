package com.maestreaux.dynasties.client.renderer;

import com.maestreaux.dynasties.DynastiesMod;
import com.maestreaux.dynasties.client.model.TentModel;
import com.maestreaux.dynasties.world.blocks.Tent;
import com.maestreaux.dynasties.world.entities.blockentity.TentBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TentRenderer<T extends TentBlockEntity> implements BlockEntityRenderer<TentBlockEntity> {
    private static final ResourceLocation TENT_TEXTURE = ResourceLocation.fromNamespaceAndPath(DynastiesMod.MODID, "textures/entity/tent.png");
    private TentModel model;

    public TentRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new TentModel(context.bakeLayer(TentModel.LAYER_LOCATION));
    }

    public ResourceLocation getTextureLocation(TentBlockEntity pEntity) {
        return TENT_TEXTURE;
    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    @Override
    public void render(TentBlockEntity tentBlockEntity, float v, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int i1) {
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(this.model.renderType(getTextureLocation(tentBlockEntity)));

        Direction direction = tentBlockEntity.getBlockState().getValue(Tent.FACING).getOpposite();

        poseStack.scale(-1, -1, 1);

        Vec3 vec = new Vec3(-0.5F, -1.5F, 0.5F);

        poseStack.translate(vec.x, vec.y, vec.z);
        poseStack.mulPose(Axis.YN.rotationDegrees(-direction.toYRot()));

        this.model.root().z = 5F;
        this.model.root().yRot = (float) Math.PI;
        this.model.renderToBuffer(poseStack, vertexConsumer, i, i1, -1);
    }
}
