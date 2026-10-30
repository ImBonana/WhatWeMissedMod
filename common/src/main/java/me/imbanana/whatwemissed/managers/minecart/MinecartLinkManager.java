package me.imbanana.whatwemissed.managers.minecart;

import me.imbanana.whatwemissed.network.payload.LinkMinecartPayloadC2S;
import me.imbanana.whatwemissed.network.payload.UnlinkMinecartPayloadC2S;
import me.imbanana.whatwemissed.util.ChainType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

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
            sendStatus(player, "minecart_distance", TextColor.RED);
            return;
        }

        AbstractMinecart owner = first.what_we_missed$isLinkedTo(second) ? first
                : second.what_we_missed$isLinkedTo(first) ? second
                : null;

        if (owner != null) {
            owner.what_we_missed$unlink();
            owner.what_we_missed$link(owner == first ? second : first, chainType);
        } else if (!first.what_we_missed$hasLink()) {
            first.what_we_missed$link(second, chainType);
        } else {
            second.what_we_missed$link(first, chainType);
        }

        if (!player.hasInfiniteMaterials()) stack.shrink(1);
        sendStatus(player, "minecart_link", TextColor.GREEN);
        level.playSound(null, second.blockPosition(), SoundEvents.CHAIN_PLACE, SoundSource.PLAYERS, 1f, 1f);
    }

    public static void handleUnlink(UnlinkMinecartPayloadC2S payload, ServerPlayer player) {
        ServerLevel level = player.level();
        if (!(level.getEntity(payload.cartId()) instanceof AbstractMinecart cart)) return;

        double playerReach = player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE);
        if (player.distanceTo(cart) > playerReach) return;

        if (!cart.what_we_missed$hasLink()) return;

        cart.what_we_missed$unlink();
        sendStatus(player, "minecart_unlink", TextColor.RED);
        level.playSound(null, cart.blockPosition(), SoundEvents.CHAIN_BREAK, SoundSource.PLAYERS, 1f, 1f);
    }

    private static void sendStatus(ServerPlayer player, String key, TextColor color) {
        player.sendOverlayMessage(Component.translatable("interaction.what_we_missed." + key).withColor(color));
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
