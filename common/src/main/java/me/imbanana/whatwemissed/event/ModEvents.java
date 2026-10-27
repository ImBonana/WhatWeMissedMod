package me.imbanana.whatwemissed.event;

import me.imbanana.whatwemissed.dataattachment.attachment.MinecartLinkData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class ModEvents {
    private static AbstractMinecart firstCart;

    public static InteractionResult onPlayerInteractionWithEntity(Player player, Level level, InteractionHand hand, Entity entity) {
        if (player.level().isClientSide()) return InteractionResult.PASS;
        if (!player.isShiftKeyDown()) return InteractionResult.PASS;
        if (!(entity instanceof AbstractMinecart minecart)) return InteractionResult.PASS;

        ItemStack itemInHand = player.getItemInHand(hand);

        if (itemInHand.is(Items.IRON_CHAIN) || Items.COPPER_CHAIN.asList().stream().anyMatch(itemInHand::is)) {
            if (firstCart != null && firstCart.isAlive()) {
                if (!minecart.equals(firstCart)) {
                    if (minecart.distanceTo(firstCart) < 3) {
                        boolean hasLinkFirst = firstCart.what_we_missed$hasLink();
                        boolean hasLinkMinecart = minecart.what_we_missed$hasLink();

                        MinecartLinkData.ChainType chainType = MinecartLinkData.ChainType.IRON;

                        if (itemInHand.is(Items.COPPER_CHAIN.weathering().unaffected()) || itemInHand.is(Items.COPPER_CHAIN.waxed().unaffected())) {
                            chainType = MinecartLinkData.ChainType.COPPER;
                        } else if (itemInHand.is(Items.COPPER_CHAIN.weathering().exposed()) || itemInHand.is(Items.COPPER_CHAIN.waxed().exposed())) {
                            chainType = MinecartLinkData.ChainType.COPPER_EXPOSED;
                        } else if (itemInHand.is(Items.COPPER_CHAIN.weathering().weathered()) || itemInHand.is(Items.COPPER_CHAIN.waxed().weathered())) {
                            chainType = MinecartLinkData.ChainType.COPPER_WEATHERED;
                        } else if (itemInHand.is(Items.COPPER_CHAIN.weathering().oxidized()) || itemInHand.is(Items.COPPER_CHAIN.waxed().oxidized())) {
                            chainType = MinecartLinkData.ChainType.COPPER_OXIDIZED;
                        }

                        if (firstCart.what_we_missed$isLinkedTo(minecart)) {
                            firstCart.what_we_missed$unlink();
                            player.sendOverlayMessage(Component.translatable("interaction.what_we_missed.minecart_unlink").withColor(TextColor.RED));
                        } else if (minecart.what_we_missed$isLinkedTo(firstCart)) {
                            minecart.what_we_missed$unlink();
                            player.sendOverlayMessage(Component.translatable("interaction.what_we_missed.minecart_unlink").withColor(TextColor.RED));
                        } else if (hasLinkFirst && !hasLinkMinecart) {
                            minecart.what_we_missed$link(firstCart, chainType);
                            player.sendOverlayMessage(Component.translatable("interaction.what_we_missed.minecart_link").withColor(TextColor.GREEN));
                        } else {
                            firstCart.what_we_missed$link(minecart, chainType);
                            player.sendOverlayMessage(Component.translatable("interaction.what_we_missed.minecart_link").withColor(TextColor.GREEN));
                        }
                    } else {
                        player.sendOverlayMessage(Component.translatable("interaction.what_we_missed.minecart_distance").withColor(TextColor.RED));
                    }
                } else {
                    player.sendOverlayMessage(Component.translatable("interaction.what_we_missed.minecart_deselect").withColor(TextColor.RED));
                }

                firstCart = null;
            } else {
                firstCart = minecart;
                player.sendOverlayMessage(Component.translatable("interaction.what_we_missed.minecart_select"));
            }

            player.playSound(SoundEvents.CHAIN_PLACE);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
