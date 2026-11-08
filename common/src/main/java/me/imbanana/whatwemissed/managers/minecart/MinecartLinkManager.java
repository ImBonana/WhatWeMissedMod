package me.imbanana.whatwemissed.managers.minecart;

import me.imbanana.whatwemissed.network.payload.LinkMinecartPayloadC2S;
import me.imbanana.whatwemissed.network.payload.UnlinkMinecartPayloadC2S;
import me.imbanana.whatwemissed.util.ChainType;
import me.imbanana.whatwemissed.util.ChainedCartLinkSide;
import me.imbanana.whatwemissed.util.MinecartMath;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class MinecartLinkManager {
    public static void handleLink(LinkMinecartPayloadC2S payload, ServerPlayer player) {
        ServerLevel level = player.level();
        ItemStack stack = player.getItemInHand(payload.hand());
        ChainType chainType = chainTypeOf(stack);
        if (chainType == null) return;

        if (!(level.getEntity(payload.firstCartId()) instanceof AbstractMinecart first) || !first.isAlive()) return;
        if (!(level.getEntity(payload.secondCartId()) instanceof AbstractMinecart second) || !second.isAlive()) return;
        if (first == second) return;

        double playerReach = player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE);
        if (player.distanceTo(first) > playerReach || player.distanceTo(second) > playerReach) return;

        if (first.distanceTo(second) > 3) {
            return;
        }

        AbstractMinecart owner = first.what_we_missed$isLinkedTo(second) ? first
                : second.what_we_missed$isLinkedTo(first) ? second
                : null;

        ChainedCartLinkSide firstSide = payload.firstSide();
        ChainedCartLinkSide secondSide = payload.secondSide();

        if (owner != null) {
            owner.what_we_missed$unlink();
            if (owner == first) {
                first.what_we_missed$link(second, chainType, firstSide, secondSide);
            } else {
                second.what_we_missed$link(first, chainType, secondSide, firstSide);
            }
        } else if (!first.what_we_missed$hasLink()) {
            first.what_we_missed$link(second, chainType, firstSide, secondSide);
        } else {
            second.what_we_missed$link(first, chainType, secondSide, firstSide);
        }

        if (!player.hasInfiniteMaterials()) stack.shrink(1);
        level.playSound(null, second.blockPosition(), SoundEvents.CHAIN_PLACE, SoundSource.PLAYERS, 1f, 1f);
    }

    public static void handleUnlink(UnlinkMinecartPayloadC2S payload, ServerPlayer player) {
        ServerLevel level = player.level();
        if (!(level.getEntity(payload.cartId()) instanceof AbstractMinecart cart)) return;

        double playerReach = player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE);
        if (player.distanceTo(cart) > playerReach) return;

        if (!cart.what_we_missed$hasLink()) return;

        cart.what_we_missed$unlink();
        level.playSound(null, cart.blockPosition(), SoundEvents.CHAIN_BREAK, SoundSource.PLAYERS, 1f, 1f);
    }

    public static ChainedCartLinkSide clickedSide(Player player, AbstractMinecart cart) {
        Vec3 eye = player.getEyePosition();
        double reach = player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE);
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(reach));
        Vec3 hit = cart.getBoundingBox().clip(eye, end).orElse(cart.position());

        Vec3 forward = MinecartMath.forward(cart);
        Vec3 offset = hit.subtract(cart.position());
        return offset.x * forward.x + offset.z * forward.z >= 0.0 ? ChainedCartLinkSide.FRONT : ChainedCartLinkSide.BACK;
    }

    public static ChainType chainTypeOf(ItemStack s) {
        if (s.is(Items.IRON_CHAIN)) return ChainType.IRON;
        if (s.is(Items.COPPER_CHAIN.weathering().unaffected()) || s.is(Items.COPPER_CHAIN.waxed().unaffected()))
            return ChainType.COPPER;
        if (s.is(Items.COPPER_CHAIN.weathering().exposed()) || s.is(Items.COPPER_CHAIN.waxed().exposed()))
            return ChainType.COPPER_EXPOSED;
        if (s.is(Items.COPPER_CHAIN.weathering().weathered()) || s.is(Items.COPPER_CHAIN.waxed().weathered()))
            return ChainType.COPPER_WEATHERED;
        if (s.is(Items.COPPER_CHAIN.weathering().oxidized()) || s.is(Items.COPPER_CHAIN.waxed().oxidized()))
            return ChainType.COPPER_OXIDIZED;
        return null;
    }

}
