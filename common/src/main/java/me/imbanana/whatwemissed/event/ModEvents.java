package me.imbanana.whatwemissed.event;

import me.imbanana.whatwemissed.item.ModDataComponents;
import me.imbanana.whatwemissed.item.components.MinecartFurnaceFuelDataComponent;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ModEvents {
    public static void modifyDefaultItemComponents(BiConsumer<Predicate<Item>, ModifyConsumer> modifier) {
        modifier.accept(item ->
                Items.WOOL.asList().stream().anyMatch(testItem -> testItem == item)
                || Items.WOOL_SLAB.asList().stream().anyMatch(testItem -> testItem == item)
                || Items.WOOL_STAIRS.asList().stream().anyMatch(testItem -> testItem == item),
                (builder, lookupProvider, item) -> {
            builder.set(ModDataComponents.MINECART_FURNACE_FUEL.get(), new MinecartFurnaceFuelDataComponent(100, 1));
        });

        modifier.accept(item -> item == Items.BAMBOO, (builder, lookupProvider, item) -> {
            builder.set(ModDataComponents.MINECART_FURNACE_FUEL.get(), new MinecartFurnaceFuelDataComponent(50, 2));
        });

        modifier.accept(item -> item == Items.PAPER, (builder, lookupProvider, item) -> {
            builder.set(ModDataComponents.MINECART_FURNACE_FUEL.get(), new MinecartFurnaceFuelDataComponent(20, 4));
        });

        modifier.accept(item -> item == Items.DRIED_KELP_BLOCK, (builder, lookupProvider, item) -> {
            builder.set(ModDataComponents.MINECART_FURNACE_FUEL.get(), new MinecartFurnaceFuelDataComponent(4000, 6));
        });

        modifier.accept(ModEvents::isBurnableWood, (builder, lookupProvider, item) -> {
            builder.set(ModDataComponents.MINECART_FURNACE_FUEL.get(), new MinecartFurnaceFuelDataComponent(300, 6));
        });

        modifier.accept(item -> item == Items.COAL, (builder, lookupProvider, item) -> {
            builder.set(ModDataComponents.MINECART_FURNACE_FUEL.get(), new MinecartFurnaceFuelDataComponent(1600, 8));
        });

        modifier.accept(item -> item == Items.COAL_BLOCK, (builder, lookupProvider, item) -> {
            builder.set(ModDataComponents.MINECART_FURNACE_FUEL.get(), new MinecartFurnaceFuelDataComponent(4000, 12));
        });

        modifier.accept(item -> item == Items.LAVA_BUCKET, (builder, lookupProvider, item) -> {
            builder.set(ModDataComponents.MINECART_FURNACE_FUEL.get(), new MinecartFurnaceFuelDataComponent(20_000, 16));
        });

        modifier.accept(item -> item == Items.BLAZE_ROD, (builder, lookupProvider, item) -> {
            builder.set(ModDataComponents.MINECART_FURNACE_FUEL.get(), new MinecartFurnaceFuelDataComponent(2400, 20));
        });
    }

    // TODO: Please change this shit :D
    private static boolean isBurnableWood(Item item) {
        return Stream.of(
                // Logs
                Items.DARK_OAK_LOG, Items.STRIPPED_DARK_OAK_LOG, Items.DARK_OAK_WOOD, Items.STRIPPED_DARK_OAK_WOOD,
                Items.PALE_OAK_LOG, Items.STRIPPED_PALE_OAK_LOG, Items.PALE_OAK_WOOD, Items.STRIPPED_PALE_OAK_WOOD,
                Items.OAK_LOG, Items.STRIPPED_OAK_LOG, Items.OAK_WOOD, Items.STRIPPED_OAK_WOOD,
                Items.ACACIA_LOG, Items.STRIPPED_ACACIA_LOG, Items.ACACIA_WOOD, Items.STRIPPED_ACACIA_WOOD,
                Items.BIRCH_LOG, Items.STRIPPED_BIRCH_LOG, Items.BIRCH_WOOD, Items.STRIPPED_BIRCH_WOOD,
                Items.JUNGLE_LOG, Items.STRIPPED_JUNGLE_LOG, Items.JUNGLE_WOOD, Items.STRIPPED_JUNGLE_WOOD,
                Items.SPRUCE_LOG, Items.STRIPPED_SPRUCE_LOG, Items.SPRUCE_WOOD, Items.STRIPPED_SPRUCE_WOOD,
                Items.MANGROVE_LOG, Items.STRIPPED_MANGROVE_LOG, Items.MANGROVE_WOOD, Items.STRIPPED_MANGROVE_WOOD,
                Items.CHERRY_LOG, Items.STRIPPED_CHERRY_LOG, Items.CHERRY_WOOD, Items.STRIPPED_CHERRY_WOOD,
                Items.POPLAR_LOG, Items.STRIPPED_POPLAR_LOG, Items.POPLAR_WOOD, Items.STRIPPED_POPLAR_WOOD,
                // Planks
                Items.DARK_OAK_SLAB, Items.PALE_OAK_SLAB, Items.OAK_SLAB, Items.ACACIA_SLAB, Items.BIRCH_SLAB, Items.JUNGLE_SLAB, Items.SPRUCE_SLAB, Items.MANGROVE_SLAB, Items.CHERRY_SLAB, Items.POPLAR_SLAB,
                // STAIRS
                Items.DARK_OAK_STAIRS, Items.PALE_OAK_STAIRS, Items.OAK_STAIRS, Items.ACACIA_STAIRS, Items.BIRCH_STAIRS, Items.JUNGLE_STAIRS, Items.SPRUCE_STAIRS, Items.MANGROVE_STAIRS, Items.CHERRY_STAIRS, Items.POPLAR_STAIRS,
                // PRESSURE_PLATES
                Items.DARK_OAK_PRESSURE_PLATE, Items.PALE_OAK_PRESSURE_PLATE, Items.OAK_PRESSURE_PLATE, Items.ACACIA_PRESSURE_PLATE, Items.BIRCH_PRESSURE_PLATE, Items.JUNGLE_PRESSURE_PLATE, Items.SPRUCE_PRESSURE_PLATE, Items.MANGROVE_PRESSURE_PLATE, Items.CHERRY_PRESSURE_PLATE, Items.POPLAR_PRESSURE_PLATE,
                // Buttons
                Items.DARK_OAK_BUTTON, Items.PALE_OAK_BUTTON, Items.OAK_BUTTON, Items.ACACIA_BUTTON, Items.BIRCH_BUTTON, Items.JUNGLE_BUTTON, Items.SPRUCE_BUTTON, Items.MANGROVE_BUTTON, Items.CHERRY_BUTTON, Items.POPLAR_BUTTON,
                // Trapdoors
                Items.DARK_OAK_TRAPDOOR, Items.PALE_OAK_TRAPDOOR, Items.OAK_TRAPDOOR, Items.ACACIA_TRAPDOOR, Items.BIRCH_TRAPDOOR, Items.JUNGLE_TRAPDOOR, Items.SPRUCE_TRAPDOOR, Items.MANGROVE_TRAPDOOR, Items.CHERRY_TRAPDOOR, Items.POPLAR_TRAPDOOR,
                // Doors
                Items.DARK_OAK_DOOR, Items.PALE_OAK_DOOR, Items.OAK_DOOR, Items.ACACIA_DOOR, Items.BIRCH_DOOR, Items.JUNGLE_DOOR, Items.SPRUCE_DOOR, Items.MANGROVE_DOOR, Items.CHERRY_DOOR, Items.POPLAR_DOOR,
                // Fences
                Items.DARK_OAK_FENCE, Items.PALE_OAK_FENCE, Items.OAK_FENCE, Items.ACACIA_FENCE, Items.BIRCH_FENCE, Items.JUNGLE_FENCE, Items.SPRUCE_FENCE, Items.MANGROVE_FENCE, Items.CHERRY_FENCE, Items.POPLAR_FENCE,
                // Fence gates
                Items.DARK_OAK_FENCE_GATE, Items.PALE_OAK_FENCE_GATE, Items.OAK_FENCE_GATE, Items.ACACIA_FENCE_GATE, Items.BIRCH_FENCE_GATE, Items.JUNGLE_FENCE_GATE, Items.SPRUCE_FENCE_GATE, Items.MANGROVE_FENCE_GATE, Items.CHERRY_FENCE_GATE, Items.POPLAR_FENCE_GATE,
                // Shelves
                Items.DARK_OAK_SHELF, Items.PALE_OAK_SHELF, Items.OAK_SHELF, Items.ACACIA_SHELF, Items.BIRCH_SHELF, Items.JUNGLE_SHELF, Items.SPRUCE_SHELF, Items.MANGROVE_SHELF, Items.CHERRY_SHELF, Items.POPLAR_SHELF,
                // Sign
                Items.DARK_OAK_SIGN, Items.PALE_OAK_SIGN, Items.OAK_SIGN, Items.ACACIA_SIGN, Items.BIRCH_SIGN, Items.JUNGLE_SIGN, Items.SPRUCE_SIGN, Items.MANGROVE_SIGN, Items.CHERRY_SIGN, Items.POPLAR_SIGN,
                // Hanging Sign
                Items.DARK_OAK_HANGING_SIGN, Items.PALE_OAK_HANGING_SIGN, Items.OAK_HANGING_SIGN, Items.ACACIA_HANGING_SIGN, Items.BIRCH_HANGING_SIGN, Items.JUNGLE_HANGING_SIGN, Items.SPRUCE_HANGING_SIGN, Items.MANGROVE_HANGING_SIGN, Items.CHERRY_HANGING_SIGN, Items.POPLAR_HANGING_SIGN,
                // Boats
                Items.DARK_OAK_BOAT, Items.PALE_OAK_BOAT, Items.OAK_BOAT, Items.ACACIA_BOAT, Items.BIRCH_BOAT, Items.JUNGLE_BOAT, Items.SPRUCE_BOAT, Items.MANGROVE_BOAT, Items.CHERRY_BOAT, Items.POPLAR_BOAT,
                // Chest boats
                Items.DARK_OAK_CHEST_BOAT, Items.PALE_OAK_CHEST_BOAT, Items.OAK_CHEST_BOAT, Items.ACACIA_CHEST_BOAT, Items.BIRCH_CHEST_BOAT, Items.JUNGLE_CHEST_BOAT, Items.SPRUCE_CHEST_BOAT, Items.MANGROVE_CHEST_BOAT, Items.CHERRY_CHEST_BOAT, Items.POPLAR_CHEST_BOAT,
                // bamboo
                Items.BAMBOO, Items.BAMBOO_BLOCK, Items.STRIPPED_BAMBOO_BLOCK,
                Items.BAMBOO_MOSAIC, Items.BAMBOO_MOSAIC_SLAB, Items.BAMBOO_MOSAIC_STAIRS,
                Items.SCAFFOLDING,
                // misc
                Items.MANGROVE_ROOTS, Items.STICK, Items.BOWL, Items.LADDER,
                // workstations
                Items.CRAFTING_TABLE, Items.CARTOGRAPHY_TABLE, Items.FLETCHING_TABLE,
                Items.SMITHING_TABLE, Items.LOOM, Items.BOOKSHELF, Items.CHISELED_BOOKSHELF,
                Items.LECTERN, Items.COMPOSTER, Items.CHEST, Items.TRAPPED_CHEST, Items.BARREL,
                Items.DAYLIGHT_DETECTOR, Items.JUKEBOX, Items.NOTE_BLOCK,
                // tools
                Items.BOW, Items.CROSSBOW, Items.FISHING_ROD,
                Items.WOODEN_PICKAXE, Items.WOODEN_SHOVEL, Items.WOODEN_HOE,
                Items.WOODEN_AXE, Items.WOODEN_SWORD, Items.WOODEN_SPEAR
        ).anyMatch(itemTest -> itemTest == item);
    }

    @FunctionalInterface
    public interface ModifyConsumer {
        void modify(DataComponentMap.Builder builder, HolderLookup.Provider lookupProvider, Item item);
    }
}
