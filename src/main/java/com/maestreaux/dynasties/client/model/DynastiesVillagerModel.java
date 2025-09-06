package com.maestreaux.dynasties.client.model;// Made with Blockbench 4.11.2
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports

import com.google.common.collect.Maps;
import com.maestreaux.dynasties.DynastiesMod;
import com.maestreaux.dynasties.client.animation.DynastiesVillagerAnimation;
import com.maestreaux.dynasties.client.renderer.entity.state.DynastiesVillagerRenderState;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.util.Mth;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AnimationState;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

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

    private Map<ModelPart, PartPose> poseMap = Maps.newHashMap();

    private final static AnimationDefinition STATIC_ANIM = DynastiesVillagerAnimation.BASE_POSE;
    private static final AnimationDefinition WALK_ANIM = offsetAnimation(DynastiesVillagerAnimation.WALK);
    private static final AnimationDefinition WALK_ARMS_ANIM = offsetAnimation(DynastiesVillagerAnimation.WALK_ARMS);
    private static final AnimationDefinition IDLE1_ANIM = offsetAnimation(DynastiesVillagerAnimation.IDLE1);

    public DynastiesVillagerModel(ModelPart rootPart) {
        super(rootPart);
        this.root = getChildWithInitialPose(rootPart, "root");
        this.villager = getChildWithInitialPose(this.root, "villager");
        this.body = getChildWithInitialPose(this.villager, "body");
        this.RightArm = getChildWithInitialPose(this.body, "RightArm");
        this.RightElbow = getChildWithInitialPose(this.RightArm, "RightElbow");
        this.LeftArm = getChildWithInitialPose(this.body, "LeftArm");
        this.LeftElbow = getChildWithInitialPose(this.LeftArm, "LeftElbow");
        this.arms = getChildWithInitialPose(this.body, "arms");
        this.head = getChildWithInitialPose(this.body, "head");
        this.RightEye = getChildWithInitialPose(this.head, "RightEye");
        this.RightEyelid = getChildWithInitialPose(this.RightEye, "RightEyelid");
        this.LeftEye = getChildWithInitialPose(this.head, "LeftEye");
        this.LeftEyelid = getChildWithInitialPose(this.LeftEye, "LeftEyelid");
        this.LeftPupil = getChildWithInitialPose(this.head, "LeftPupil");
        this.RightPupil = getChildWithInitialPose(this.head, "RightPupil");
        this.eyebrow = getChildWithInitialPose(this.head, "eyebrow");
        this.RightEyebrow = getChildWithInitialPose(this.eyebrow, "RightEyebrow");
        this.LeftEyebrow = getChildWithInitialPose(this.eyebrow, "LeftEyebrow");
        this.mouth = getChildWithInitialPose(this.head, "mouth");
        this.nose = getChildWithInitialPose(this.head, "nose");
        this.RightLeg = getChildWithInitialPose(this.villager, "RightLeg");
        this.RightKnee = getChildWithInitialPose(this.RightLeg, "RightKnee");
        this.LeftLeg = getChildWithInitialPose(this.villager, "LeftLeg");
        this.LeftKnee = getChildWithInitialPose(this.LeftLeg, "LeftKnee");

        toggleArms(true);
    }

    private ModelPart getChildWithInitialPose(ModelPart part, String childName) {
        var child = part.getChild(childName);
        this.poseMap.put(child, part.storePose());

        var animChannels = STATIC_ANIM.boneAnimations().get(childName);

        if (animChannels != null) {
            var pos = new Vector3f(child.x, child.y, child.z);
            var rot = new Vector3f();
            var scale = new Vector3f(child.xScale, child.yScale, child.zScale);

            animChannels.forEach(channel -> {
                var firstKeyFrame = channel.keyframes()[0];

                if (channel.target() == AnimationChannel.Targets.POSITION) {
                    pos.add(firstKeyFrame.target());
                } else if (channel.target() == AnimationChannel.Targets.ROTATION) {
                    rot.add(firstKeyFrame.target());
                } else if (channel.target() == AnimationChannel.Targets.SCALE) {
                    scale.add(firstKeyFrame.target());
                }
            });

            child.setInitialPose(new PartPose(pos.x, pos.y, pos.z, rot.x, rot.y, rot.z, scale.x, scale.y, scale.z));
        }

        return child;
    }

    private static AnimationDefinition offsetAnimation(AnimationDefinition animationToOffset) {
        var builder = AnimationDefinition.Builder.withLength(animationToOffset.lengthInSeconds());

        if (animationToOffset.looping()) {
            builder.looping();
        }

        var animations = animationToOffset.boneAnimations();

        animations.forEach((key, animation) -> {
            var staticAnimChannels = STATIC_ANIM.boneAnimations().get(key);
            Map<AnimationChannel.Target, AnimationChannel> staticAnimChannelsMap;

            if (staticAnimChannels != null) {
                staticAnimChannelsMap = staticAnimChannels.stream().collect(Collectors.toMap(AnimationChannel::target, channel -> channel));
            } else {
                staticAnimChannelsMap = Map.of();
            }

            animation.forEach(channel -> {
                    var offsetKeyframes = Arrays.stream(channel.keyframes()).map(keyframe -> {
                        var staticAnimChannel = staticAnimChannelsMap.get(channel.target());

                        if (staticAnimChannel != null) {
                            var keyFrameToSub = staticAnimChannel.keyframes()[0];

                            var newKeyframeTarget = keyframe.target().sub(keyFrameToSub.target());

                            return new Keyframe(keyframe.timestamp(), newKeyframeTarget, keyframe.interpolation());
                        }

                        return keyframe;
                    }).toList().toArray(new Keyframe[0]);


                    builder.addAnimation(key, new AnimationChannel(channel.target(), offsetKeyframes));
                });
        });

        return builder.build();
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

        PartDefinition eyebrow = head.addOrReplaceChild("eyebrow", CubeListBuilder.create().texOffs(48, 29).addBox(-1.0F, -0.5F, 0.75F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -5.5F, -4.08F));

        PartDefinition RightEyebrow = eyebrow.addOrReplaceChild("RightEyebrow", CubeListBuilder.create().texOffs(68, 30).addBox(-3.0F, -1.0F, 0.0F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.5F, 0.0F));

        PartDefinition LeftEyebrow = eyebrow.addOrReplaceChild("LeftEyebrow", CubeListBuilder.create().texOffs(68, 30).addBox(0.0F, -1.0F, 0.0F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.5F, 0.0F));

        PartDefinition RightEye = head.addOrReplaceChild("RightEye", CubeListBuilder.create().texOffs(68, 26).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, -4.0F, -4.02F));

        PartDefinition RightEyelid = RightEye.addOrReplaceChild("RightEyelid", CubeListBuilder.create().texOffs(68, 34).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, -0.02F));

        PartDefinition LeftEye = head.addOrReplaceChild("LeftEye", CubeListBuilder.create().texOffs(68, 26).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, -4.0F, -4.02F));

        PartDefinition LeftEyelid = LeftEye.addOrReplaceChild("LeftEyelid", CubeListBuilder.create().texOffs(68, 34).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, -0.02F));

        PartDefinition LeftPupil = head.addOrReplaceChild("LeftPupil", CubeListBuilder.create().texOffs(68, 28).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(1.75F, -4.5F, -4.03F));

        PartDefinition RightPupil = head.addOrReplaceChild("RightPupil", CubeListBuilder.create().texOffs(70, 28).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.75F, -4.5F, -4.03F));

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
    protected void animate(AnimationState p_368871_, AnimationDefinition p_365491_, float p_363110_, float p_368202_) {
        super.animate(p_368871_, p_365491_, p_363110_, p_368202_);
    }

    @Override
    protected void animateWalk(AnimationDefinition p_363127_, float p_364817_, float p_364163_, float p_365350_, float p_365167_) {
        super.animateWalk(p_363127_, p_364817_, p_364163_, p_365350_, p_365167_);
    }

    @Override
    public void setupAnim(DynastiesVillagerRenderState renderState) {
        super.setupAnim(renderState);

        applyHeadRotation(renderState.yRot, renderState.xRot);

        if (renderState.isFleeing) {
            animateWalk(DynastiesVillagerAnimation.FLEE, renderState.walkAnimationPos, renderState.walkAnimationSpeed, 2.5F, 2.5F);
            animateWalk(DynastiesVillagerAnimation.FLEE_ARMS, renderState.walkAnimationPos, renderState.walkAnimationSpeed, 2.5F, 2.5F);
        } else {
            animateWalk(WALK_ANIM, renderState.walkAnimationPos, renderState.walkAnimationSpeed, 2F, 2.5F);
            animateWalk(WALK_ARMS_ANIM, renderState.walkAnimationPos, renderState.walkAnimationSpeed, 2F, 2.5F);
        }

        animate(renderState.idleFaceAnimationState, DynastiesVillagerAnimation.FACE_1, renderState.ageInTicks);
        animate(renderState.idleAnimationState, IDLE1_ANIM, renderState.ageInTicks, 0.5F);

        //this.toggleArms(renderState.idleAnimationState.isStarted());
    }

    private void applyHeadRotation(float pNetHeadYaw, float pHeadPitch) {
        float sinYaw = Mth.sin(pNetHeadYaw * ((float) Math.PI / 180F));
        float cosPitch = Mth.cos(pHeadPitch * ((float) Math.PI / 180F)) - 1.0472F;

        pNetHeadYaw = Mth.clamp(pNetHeadYaw, -30.0F, 30.0F);
        pHeadPitch = Mth.clamp(pHeadPitch, -25.0F, 45.0F);

        float yawRadians = pNetHeadYaw * ((float) Math.PI / 180F);
        float pitchRadians = pHeadPitch * ((float) Math.PI / 180F);

        float rightNetPupilYaw = Mth.clamp(sinYaw - yawRadians, -0.25F, 0.75F);
        float leftNetPupilYaw = Mth.clamp(sinYaw - yawRadians, -0.75F, 0.25F);
        float pupilPitch = Mth.clamp(cosPitch - pitchRadians, 0F, 0.125F);

        this.head.yRot = pNetHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = pHeadPitch * ((float) Math.PI / 180F);

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