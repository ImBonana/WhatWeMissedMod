package me.imbanana.whatwemissed.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
    private static final double CART_HALF_LENGTH = 0.55;
    @Unique
    private static final double HITCH_HEIGHT = 0.45;
    @Unique
    private static final float HALF_WIDTH = 1.5f / 16f;

    protected AbstractMinecartRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Inject(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/MinecartRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At("TAIL")
    )
    private void renderLinkConnection(S state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        Vec3 offset = state.what_we_missed$getLinkPositionOffset();
        if (offset == null) return;

        double dist = offset.length();
        double span = dist - CART_HALF_LENGTH * 2;
        if (span <= 0.05) return;

        Vec3 dir = offset.scale(1.0 / dist);
        Vec3 start = dir.scale(CART_HALF_LENGTH).add(0, HITCH_HEIGHT, 0);

        Vec3 ref = Math.abs(dir.y) > 0.99 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 side1 = dir.cross(ref).normalize().scale(HALF_WIDTH);
        Vec3 side2 = dir.cross(side1).normalize().scale(HALF_WIDTH);

        int count = Math.max(1, (int) Math.ceil(span));
        double pieceLength = span / count;
        int light = state.lightCoords;

        Identifier texture = Identifier.withDefaultNamespace("textures/block/%s_chain.png".formatted(state.what_we_missed$getChainType().getSerializedName()));

        collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(texture), (pose, buffer) -> {
            for (int i = 0; i < count; i++) {
                Vec3 a = start.add(dir.scale(pieceLength * i));
                Vec3 b = start.add(dir.scale(pieceLength * (i + 1)));

                quad(pose, buffer, a, b, side1, dir, 0f, 3f / 16f, light);
                quad(pose, buffer, a, b, side2, dir, 0f, 3f / 16f, light);
            }
        });
    }

    @Unique
    private static void quad(PoseStack.Pose pose, VertexConsumer buffer, Vec3 a, Vec3 b, Vec3 side, Vec3 dir, float u0, float u1, int light) {
        Vec3 push = dir.cross(side).normalize().scale(0.00001);

        Vec3 a1 = a.add(push), b1 = b.add(push);
        vertex(pose, buffer, a1.subtract(side), u0, 1f, light);
        vertex(pose, buffer, a1.add(side),      u1, 1f, light);
        vertex(pose, buffer, b1.add(side),      u1, 0f, light);
        vertex(pose, buffer, b1.subtract(side), u0, 0f, light);

        Vec3 a2 = a.subtract(push), b2 = b.subtract(push);
        vertex(pose, buffer, b2.subtract(side), u0, 0f, light);
        vertex(pose, buffer, b2.add(side),      u1, 0f, light);
        vertex(pose, buffer, a2.add(side),      u1, 1f, light);
        vertex(pose, buffer, a2.subtract(side), u0, 1f, light);
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
        Vec3 otherPos = entity.what_we_missed$getLinkPosition(partialTicks);

        state.what_we_missed$setLinkPositionOffset(otherPos != null ? otherPos.subtract(entity.getPosition(partialTicks)) : null);
        state.what_we_missed$setChainType(entity.what_we_missed$getChainType());
    }
}
