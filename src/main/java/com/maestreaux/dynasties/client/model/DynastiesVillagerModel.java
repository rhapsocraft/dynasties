package com.maestreaux.dynasties.client.model;// Made with Blockbench 4.11.2
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.maestreaux.dynasties.DynastiesMod;
import com.maestreaux.dynasties.client.animation.DynastiesVillagerAnimation;
import com.maestreaux.dynasties.client.renderer.entity.state.DynastiesVillagerRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.util.Mth;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class DynastiesVillagerModel extends EntityModel<DynastiesVillagerRenderState> implements HeadedModel {
    // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(DynastiesMod.MODID, "dynastiesvillager"), "main");
    private final ModelPart root;
    private final ModelPart villager;
    private final ModelPart body;
    private final ModelPart RightArm;
    private final ModelPart RightElbow;
    private final ModelPart LeftArm;
    private final ModelPart LeftElbow;
    private final ModelPart arms;
    private final ModelPart head;
    private final ModelPart RightEye;
    private final ModelPart RightEyelid;
    private final ModelPart LeftEye;
    private final ModelPart LeftEyelid;
    private final ModelPart LeftPupil;
    private final ModelPart RightPupil;
    private final ModelPart eyebrow;
    private final ModelPart RightEyebrow;
    private final ModelPart LeftEyebrow;
    private final ModelPart mouth;
    private final ModelPart nose;
    private final ModelPart RightLeg;
    private final ModelPart RightKnee;
    private final ModelPart LeftLeg;
    private final ModelPart LeftKnee;

    public DynastiesVillagerModel(ModelPart rootPart) {
        super(rootPart);
        this.root = rootPart.getChild("root");
        this.villager = this.root.getChild("villager");
        this.body = this.villager.getChild("body");
        this.RightArm = this.body.getChild("RightArm");
        this.RightElbow = this.RightArm.getChild("RightElbow");
        this.LeftArm = this.body.getChild("LeftArm");
        this.LeftElbow = this.LeftArm.getChild("LeftElbow");
        this.arms = this.body.getChild("arms");
        this.head = this.body.getChild("head");
        this.RightEye = this.head.getChild("RightEye");
        this.RightEyelid = this.RightEye.getChild("RightEyelid");
        this.LeftEye = this.head.getChild("LeftEye");
        this.LeftEyelid = this.LeftEye.getChild("LeftEyelid");
        this.LeftPupil = this.head.getChild("LeftPupil");
        this.RightPupil = this.head.getChild("RightPupil");
        this.eyebrow = this.head.getChild("eyebrow");
        this.RightEyebrow = this.eyebrow.getChild("RightEyebrow");
        this.LeftEyebrow = this.eyebrow.getChild("LeftEyebrow");
        this.mouth = this.head.getChild("mouth");
        this.nose = this.head.getChild("nose");
        this.RightLeg = this.villager.getChild("RightLeg");
        this.RightKnee = this.RightLeg.getChild("RightKnee");
        this.LeftLeg = this.villager.getChild("LeftLeg");
        this.LeftKnee = this.LeftLeg.getChild("LeftKnee");

        toggleArms(false);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition villager = root.addOrReplaceChild("villager", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition body = villager.addOrReplaceChild("body", CubeListBuilder.create().texOffs(29, 0).addBox(-4.0F, -11.0F, -3.0F, 8.0F, 12.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -14.0F, 0.0F));

        PartDefinition RightArm = body.addOrReplaceChild("RightArm", CubeListBuilder.create().texOffs(50, 40).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-6.0F, -9.0F, 0.0F));

        PartDefinition RightElbow = RightArm.addOrReplaceChild("RightElbow", CubeListBuilder.create().texOffs(50, 52).addBox(-2.0F, 0.5F, -3.5F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.5F, 1.5F));

        PartDefinition LeftArm = body.addOrReplaceChild("LeftArm", CubeListBuilder.create().texOffs(0, 56).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(6.0F, -9.0F, 0.0F));

        PartDefinition LeftElbow = LeftArm.addOrReplaceChild("LeftElbow", CubeListBuilder.create().texOffs(17, 54).addBox(-2.0F, 0.5F, -3.5F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.5F, 1.5F));

        PartDefinition arms = body.addOrReplaceChild("arms", CubeListBuilder.create().texOffs(33, 28).addBox(-8.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(33, 41).addBox(4.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(33, 19).addBox(-4.0F, 2.0F, -2.0F, 8.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -8.25F, 0.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 25).addBox(-4.0F, -11.0F, -4.0F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -10.0F, 0.0F));

        PartDefinition RightEye = head.addOrReplaceChild("RightEye", CubeListBuilder.create().texOffs(68, 26).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, -4.0F, -4.02F));

        PartDefinition RightEyelid = RightEye.addOrReplaceChild("RightEyelid", CubeListBuilder.create().texOffs(68, 34).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, -0.02F));

        PartDefinition LeftEye = head.addOrReplaceChild("LeftEye", CubeListBuilder.create().texOffs(68, 26).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, -4.0F, -4.02F));

        PartDefinition LeftEyelid = LeftEye.addOrReplaceChild("LeftEyelid", CubeListBuilder.create().texOffs(68, 34).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, -0.02F));

        PartDefinition LeftPupil = head.addOrReplaceChild("LeftPupil", CubeListBuilder.create().texOffs(68, 28).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(1.75F, -4.5F, -4.03F));

        PartDefinition RightPupil = head.addOrReplaceChild("RightPupil", CubeListBuilder.create().texOffs(70, 28).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.75F, -4.5F, -4.03F));

        PartDefinition eyebrow = head.addOrReplaceChild("eyebrow", CubeListBuilder.create(), PartPose.offset(0.0F, -5.5F, -4.08F));

        PartDefinition RightEyebrow = eyebrow.addOrReplaceChild("RightEyebrow", CubeListBuilder.create().texOffs(68, 30).addBox(-3.0F, -1.0F, 0.0F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.5F, 0.0F));

        PartDefinition LeftEyebrow = eyebrow.addOrReplaceChild("LeftEyebrow", CubeListBuilder.create().texOffs(68, 30).addBox(0.0F, -1.0F, 0.0F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.5F, 0.0F));

        PartDefinition mouth = head.addOrReplaceChild("mouth", CubeListBuilder.create().texOffs(68, 32).addBox(-2.0F, -0.5F, 0.0F, 4.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.5F, -4.05F));

        PartDefinition nose = head.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(17, 44).addBox(-1.0F, -0.5F, -2.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -3.5F, -4.0F));

        PartDefinition RightLeg = villager.addOrReplaceChild("RightLeg", CubeListBuilder.create().texOffs(0, 44).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, -13.0F, 0.0F));

        PartDefinition RightKnee = RightLeg.addOrReplaceChild("RightKnee", CubeListBuilder.create().texOffs(58, 0).addBox(-2.0F, 0.5F, -0.5F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 5.5F, -1.5F));

        PartDefinition LeftLeg = villager.addOrReplaceChild("LeftLeg", CubeListBuilder.create().texOffs(50, 28).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, -13.0F, 0.0F));

        PartDefinition LeftKnee = LeftLeg.addOrReplaceChild("LeftKnee", CubeListBuilder.create().texOffs(58, 12).addBox(-2.0F, 0.5F, -0.5F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 5.5F, -1.5F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    private void toggleArms(boolean visible) {
        this.arms.visible = visible;
        this.RightArm.visible = !visible;
        this.LeftArm.visible = !visible;
    }

    @Override
    public void setupAnim(DynastiesVillagerRenderState renderState) {
        super.setupAnim(renderState);
        // root.getAllParts().forEach(ModelPart::resetPose);

        applyHeadRotation(renderState.yRot, renderState.xRot);

        animate(renderState.idleFaceAnimationState, DynastiesVillagerAnimation.FACE_1, renderState.ageInTicks);
        animate(renderState.idleAnimationState, DynastiesVillagerAnimation.IDLE1, renderState.ageInTicks, 0.5F);

        if (renderState.isFleeing) {
            animateWalk(DynastiesVillagerAnimation.FLEE, renderState.walkAnimationPos, renderState.walkAnimationSpeed, 2.5F, 2.5F);
            animateWalk(DynastiesVillagerAnimation.FLEE_ARMS, renderState.walkAnimationPos, renderState.walkAnimationSpeed, 2.5F, 2.5F);
        } else {
            animateWalk(DynastiesVillagerAnimation.WALK, renderState.walkAnimationPos, renderState.walkAnimationSpeed, 1.5F, 2.5F);
            animateWalk(DynastiesVillagerAnimation.WALK_ARMS, renderState.walkAnimationPos, renderState.walkAnimationSpeed, 1.5F, 2.5F);
        }

        this.toggleArms(renderState.idleAnimationState.isStarted());

    }

    private void applyHeadRotation(float pNetHeadYaw, float pHeadPitch) {
        float sinYaw = Mth.sin(pNetHeadYaw * ((float)Math.PI / 180F));
        float cosPitch = Mth.cos(pHeadPitch * ((float)Math.PI / 180F)) - 1.0472F;

        pNetHeadYaw = Mth.clamp(pNetHeadYaw, -30.0F, 30.0F);
        pHeadPitch = Mth.clamp(pHeadPitch, -25.0F, 45.0F);

        float yawRadians = pNetHeadYaw * ((float)Math.PI / 180F);
        float pitchRadians = pHeadPitch * ((float)Math.PI / 180F);

        float rightNetPupilYaw = Mth.clamp(sinYaw - yawRadians, -0.25F, 0.75F);
        float leftNetPupilYaw = Mth.clamp(sinYaw - yawRadians, -0.75F, 0.25F);
        float pupilPitch = Mth.clamp(cosPitch - pitchRadians, 0F, 0.125F);

        this.head.yRot = pNetHeadYaw * ((float)Math.PI / 180F);
        this.head.xRot = pHeadPitch * ((float)Math.PI / 180F);

        this.RightPupil.x -= rightNetPupilYaw;
        this.LeftPupil.x -= leftNetPupilYaw;
        this.RightPupil.y -= pupilPitch;
        this.LeftPupil.y -= pupilPitch;
    }

    @Override
    public @NotNull ModelPart getHead() {
        return this.head;
    }
}