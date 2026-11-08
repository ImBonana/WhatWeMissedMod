package me.imbanana.whatwemissed.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.imbanana.whatwemissed.WhatWeMissed;
import me.imbanana.whatwemissed.entity.ModEntityModelLayers;
import me.imbanana.whatwemissed.entity.model.ModAbstractMinecartModel;
import me.imbanana.whatwemissed.util.ChainedCartLinkSide;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.AbstractMinecartRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.MinecartRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractMinecartRenderer.class)
public abstract class AbstractMinecartRendererMixin<T extends AbstractMinecart, S extends MinecartRenderState> extends EntityRenderer<T, S> {
    @Unique
    private static final Identifier TEXTURE = WhatWeMissed.idOf("textures/entity/minecart/minecart.png");

    @Unique
    private static final double CHAIN_LENGTH = 2.05f * 0.55f;
    @Unique
    private static final double MAX_SAG = 1.5;
    @Unique
    private static final int PIECES_PER_TILE = 8;
    @Unique
    private static final double CART_END_OFFSET = 0.55;
    @Unique
    private static final double HITCH_HEIGHT = 0.45;
    @Unique
    private static final float HALF_WIDTH = 1.5f / 16f;

    @Unique
    private ModAbstractMinecartModel newModel;

    protected AbstractMinecartRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void createModel(EntityRendererProvider.Context context, ModelLayerLocation model, CallbackInfo ci) {
        this.newModel = new ModAbstractMinecartModel(context.bakeLayer(ModEntityModelLayers.MINECART));
    }

    @WrapOperation(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/MinecartRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/resources/Identifier;III)V"
            )
    )
    private void changeMinecartModel(SubmitNodeCollector instance, Model model, Object o, PoseStack poseStack, Identifier identifier, int i, int j, int k, Operation<Void> original, @Local(argsOnly = true) S state) {
        poseStack.translate(0, -1.211, 0);
        instance.submitModel(this.newModel, state, poseStack, TEXTURE, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
    }

    @Inject(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/MinecartRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At("TAIL")
    )
    private void renderLinkConnection(S state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        Vec3 anchorStart = state.what_we_missed$getLinkAnchorStart();
        Vec3 anchorEnd = state.what_we_missed$getLinkAnchorEnd();
        if (anchorEnd == null || anchorStart == null) return;

        Vec3 delta = anchorEnd.subtract(anchorStart);
        double span = delta.length();
        if (span <= 0.05) return;

        double slack = CHAIN_LENGTH - span;
        double sag = slack <= 0 ? 0 : Math.min(MAX_SAG, Math.sqrt(3f * Math.max(span, 0.3f) * slack / 8f));

        final int dense = 32;
        Vec3[] curve = new Vec3[dense + 1];
        double[] arcLengths = new double[dense + 1];
        for (int i = 0; i <= dense; i++) {
            double t = (double) i / dense;
            curve[i] = anchorStart.add(delta.scale(t)).add(0, -sag * 4f * t * (1f - t), 0);
            arcLengths[i] = i == 0 ? 0f : arcLengths[i - 1] + curve[i].distanceTo(curve[i - 1]);
        }

        double totalLength = arcLengths[dense];

        int tiles = Math.max(1, (int) Math.round(totalLength * 2.0));
        int count = tiles * PIECES_PER_TILE;
        double pieceLength = totalLength / count;

        Vec3[] points = new Vec3[count + 1];
        for (int i = 0; i <= count; i++) {
            points[i] = pointAt(curve, arcLengths, i * pieceLength);
        }

        Vec3[] sides1 = new Vec3[count + 1];
        Vec3[] sides2 = new Vec3[count + 1];
        Vec3[] dirs = new Vec3[count + 1];
        double r = Math.sqrt(0.5);
        for (int k = 0; k <= count; k++) {
            Vec3 prev = points[Math.max(k - 1, 0)];
            Vec3 next = points[Math.min(k + 1, count)];
            Vec3 tangent = next.subtract(prev);
            if (tangent.lengthSqr() < 1.0E-12) tangent = delta;
            Vec3 dir = tangent.normalize();

            Vec3 ref = Math.abs(dir.y) > 0.99 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
            Vec3 base1 = dir.cross(ref).normalize();
            Vec3 base2 = dir.cross(base1).normalize();

            dirs[k] = dir;
            sides1[k] = base1.add(base2).scale(r * HALF_WIDTH);
            sides2[k] = base2.subtract(base1).scale(r * HALF_WIDTH);
        }


        int light = state.lightCoords;
        Identifier texture = Identifier.withDefaultNamespace("textures/block/%s_chain.png".formatted(state.what_we_missed$getChainType().getSerializedName()));

        collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(texture), (pose, buffer) -> {
            for (int i = 0; i < count; i++) {
                float tilePos = (float) (i % PIECES_PER_TILE) / PIECES_PER_TILE;
                float vA = 1f - tilePos;
                float vB = vA - 1f / PIECES_PER_TILE;

                quad(pose, buffer, points[i], points[i + 1], sides1[i], sides1[i + 1], dirs[i], 0f, 3f / 16f, vA, vB, light);
                quad(pose, buffer, points[i], points[i + 1], sides2[i], sides2[i + 1], dirs[i], 3f / 16f, 6f / 16f, vA, vB, light);
            }
        });
    }

    @Unique
    private static Vec3 pointAt(Vec3[] curve, double[] archLengths, double distance) {
        for (int i = 1; i < archLengths.length; i++) {
            if (distance <= archLengths[i]) {
                double segmentLength = archLengths[i] - archLengths[i - 1];
                double fraction = segmentLength < 1.0E-9 ? 0.0 : (distance - archLengths[i - 1]) / segmentLength;
                return curve[i - 1].lerp(curve[i], fraction);
            }
        }
        return curve[curve.length - 1];
    }

    @Unique
    private static void quad(PoseStack.Pose pose, VertexConsumer buffer, Vec3 a, Vec3 b, Vec3 sideA, Vec3 sideB, Vec3 dir, float u0, float u1, float vA, float vB, int light) {
        Vec3 push = dir.cross(sideA).normalize().scale(0.0001);

        Vec3 a1 = a.add(push), b1 = b.add(push);
        vertex(pose, buffer, a1.subtract(sideA), u0, vA, light);
        vertex(pose, buffer, a1.add(sideA),      u1, vA, light);
        vertex(pose, buffer, b1.add(sideB),      u1, vB, light);
        vertex(pose, buffer, b1.subtract(sideB), u0, vB, light);

        Vec3 a2 = a.subtract(push), b2 = b.subtract(push);
        vertex(pose, buffer, b2.subtract(sideB), u0, vB, light);
        vertex(pose, buffer, b2.add(sideB),      u1, vB, light);
        vertex(pose, buffer, a2.add(sideA),      u1, vA, light);
        vertex(pose, buffer, a2.subtract(sideA), u0, vA, light);
    }

    @Unique
    private static void vertex(PoseStack.Pose pose, VertexConsumer buffer, Vec3 p, float u, float v, int light) {
        buffer.addVertex(pose, (float) p.x, (float) p.y, (float) p.z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0f, -1f, 0f);
    }

    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/vehicle/minecart/AbstractMinecart;Lnet/minecraft/client/renderer/entity/state/MinecartRenderState;F)V",
            at = @At("TAIL")
    )
    private void extractLinkRenderState(T entity, S state, float partialTicks, CallbackInfo ci) {
        if (entity.what_we_missed$hasLink()) {
            AbstractMinecart other = entity.what_we_missed$getLinkedEntity();

            Vec3 cartAnchor = calculateAnchor(entity, entity.what_we_missed$getThisLinkSide(), partialTicks, CART_END_OFFSET);
            Vec3 otherAnchor = calculateAnchor(other, entity.what_we_missed$getOtherLinkSide(), partialTicks, CART_END_OFFSET);

            Vec3 cartPos = entity.getPosition(partialTicks);

            state.what_we_missed$setLinkAnchorStart(cartAnchor.subtract(cartPos));
            state.what_we_missed$setLinkAnchorEnd(otherAnchor.subtract(cartPos));
        } else {
            state.what_we_missed$setLinkAnchorStart(null);
            state.what_we_missed$setLinkAnchorEnd(null);
        }

        state.what_we_missed$setChainType(entity.what_we_missed$getChainType());
    }

    @Unique
    private Vec3 calculateAnchor(AbstractMinecart minecart, ChainedCartLinkSide side, float partialTick, double endOffset) {
        float yRot = minecart.getYRot(partialTick);
        double rads = Math.toRadians(yRot);
        Vec3 forward = new Vec3(-Math.cos(rads), 0f, Math.sin(rads));

        double sign = side == ChainedCartLinkSide.FRONT ? 1 : -1;

        return minecart.getPosition(partialTick)
                .add(forward.scale(sign * endOffset))
                .add(0, HITCH_HEIGHT, 0);
    }
}
