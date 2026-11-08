package me.imbanana.whatwemissed.managers.minecart;

import me.imbanana.whatwemissed.network.ModClientNetwork;
import me.imbanana.whatwemissed.network.payload.LinkMinecartPayloadC2S;
import me.imbanana.whatwemissed.network.payload.UnlinkMinecartPayloadC2S;
import me.imbanana.whatwemissed.util.ChainedCartLinkSide;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class MinecartLinkManagerClient {
    private static int selectedId = -1;
    private static ChainedCartLinkSide selectedSide = ChainedCartLinkSide.FRONT;

    public static InteractionResult onClientInteract(Player player, Level level, InteractionHand hand, Entity entity) {
        if (!player.isShiftKeyDown()) return InteractionResult.PASS;
        if (!(entity instanceof AbstractMinecart cart)) return InteractionResult.PASS;
        if (cart instanceof Container && !level.isClientSide()) return InteractionResult.SUCCESS;

        ItemStack stack = player.getItemInHand(hand);
        boolean isChain = MinecartLinkManager.chainTypeOf(stack) != null;
        boolean wantsUnlink = hand == InteractionHand.MAIN_HAND && stack.isEmpty() && cart.what_we_missed$hasLink();

        if (!isChain && !wantsUnlink) return InteractionResult.PASS;

        if (!level.isClientSide()) return InteractionResult.SUCCESS;

        if (wantsUnlink) {
            clearSelection();
            ModClientNetwork.sendToServer(new UnlinkMinecartPayloadC2S(cart.getId()));
            return InteractionResult.SUCCESS;
        }

        Entity first = selectedId == -1 ? null : level.getEntity(selectedId);
        if (!(first instanceof AbstractMinecart) || !first.isAlive()) {
            selectedId = cart.getId();
            selectedSide = MinecartLinkManager.clickedSide(player, cart);
            return InteractionResult.SUCCESS;
        }

        ChainedCartLinkSide secondSide = MinecartLinkManager.clickedSide(player, cart);
        ModClientNetwork.sendToServer(new LinkMinecartPayloadC2S(first.getId(), cart.getId(), hand, selectedSide, secondSide));
        clearSelection();

        return InteractionResult.SUCCESS;
    }

    public static void clearSelection() {
        selectedId = -1;
    }
}
