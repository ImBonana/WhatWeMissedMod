package me.imbanana.whatwemissed.item.custom;

import me.imbanana.whatwemissed.item.ModDataComponents;
import me.imbanana.whatwemissed.item.components.CopperStateDataComponent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MinecartItem;

import java.util.Objects;

public class MinecartCopperChestItem extends MinecartItem {
    public MinecartCopperChestItem(EntityType<? extends AbstractMinecart> type, Properties properties) {
        super(type, properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        Component name = super.getName(stack);
        Component defaultName = this.components().get(DataComponents.ITEM_NAME);
        if (!Objects.equals(name, defaultName)) {
            return name;
        }
        CopperStateDataComponent state = stack.getOrDefault(ModDataComponents.COPPER_STATE.get(), CopperStateDataComponent.DEFAULT);
        return Component.translatable(state.getTranslationKey(this.getDescriptionId()));
    }
}
