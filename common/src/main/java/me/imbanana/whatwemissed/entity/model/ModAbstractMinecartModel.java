package me.imbanana.whatwemissed.entity.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.state.MinecartRenderState;

public class ModAbstractMinecartModel extends EntityModel<MinecartRenderState> {
    private final ModelPart bone;

    public ModAbstractMinecartModel(ModelPart root) {
        super(root);

        this.bone = root.getChild("bone");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 33).addBox(-16.0F, -12.0F, -1.0F, 16.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 18).addBox(-18.0F, -10.0F, 0.0F, 20.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, 24.0F, -8.0F));

        PartDefinition cube_r1 = bone.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 18).addBox(-10.0F, -4.0F, -1.0F, 20.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.0F, -6.0F, 15.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition cube_r2 = bone.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(28, 38).addBox(-1.5F, -1.5F, -1.0F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(28, 38).addBox(-1.5F, -1.5F, -11.0F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-13.5F, 0.5F, 13.0F, 0.0F, 0.0F, 1.5708F));

        PartDefinition cube_r3 = bone.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(28, 38).addBox(-1.5F, -1.5F, -1.0F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(28, 38).addBox(-1.5F, -1.5F, -11.0F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5F, 0.5F, 13.0F, 0.0F, 0.0F, 1.5708F));

        PartDefinition cube_r4 = bone.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 38).addBox(-6.0F, -4.0F, -1.0F, 12.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -6.0F, 8.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition cube_r5 = bone.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(0, 38).addBox(-6.0F, -4.0F, -1.0F, 12.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-17.0F, -6.0F, 8.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition cube_r6 = bone.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(0, 28).addBox(-9.0F, -1.0F, -1.5F, 18.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, -11.0F, 8.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition cube_r7 = bone.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(0, 28).addBox(-9.0F, -1.0F, -1.5F, 18.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-17.5F, -11.0F, 8.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition cube_r8 = bone.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(0, 33).addBox(-8.0F, -1.0F, -1.5F, 16.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.0F, -11.0F, 15.5F, 0.0F, 3.1416F, 0.0F));

        PartDefinition cube_r9 = bone.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(0, 0).addBox(-10.0F, -8.0F, -1.0F, 20.0F, 16.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.0F, -1.0F, 8.0F, -1.5708F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(MinecartRenderState state) {
        super.setupAnim(state);
    }
}
