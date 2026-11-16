package me.imbanana.whatwemissed.item;

import me.imbanana.whatwemissed.entity.ModEntityTypes;
import me.imbanana.whatwemissed.item.components.CopperStateDataComponent;
import me.imbanana.whatwemissed.item.custom.MinecartCopperChestItem;
import me.imbanana.whatwemissed.registry.ModRegistry;
import net.minecraft.core.dispenser.MinecartDispenseItemBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.DispenserBlock;

import java.util.function.Function;

public class ModItems {
    public static final Item COPPER_CHEST_MINECART = register(
            ModItemIds.COPPER_CHEST_MINECART,
            properties -> new MinecartCopperChestItem(ModEntityTypes.COPPER_CHEST_MINECART, properties),
            new Item.Properties()
                    .stacksTo(1)
                    .component(ModDataComponents.COPPER_STATE, CopperStateDataComponent.DEFAULT)
    );

    public static Item register(ResourceKey<Item> itemKey, Function<Item.Properties, Item> itemFactory, Item.Properties settings) {
        Item item = itemFactory.apply(settings.setId(itemKey));
        ModRegistry.register(BuiltInRegistries.ITEM, itemKey, () -> item);
        return item;
    }

    public static void init() {
        DispenserBlock.registerBehavior(COPPER_CHEST_MINECART, new MinecartDispenseItemBehavior(ModEntityTypes.COPPER_CHEST_MINECART));
    }
}
