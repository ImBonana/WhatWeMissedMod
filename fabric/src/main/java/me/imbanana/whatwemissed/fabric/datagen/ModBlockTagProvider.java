package me.imbanana.whatwemissed.fabric.datagen;

import me.imbanana.whatwemissed.block.ModBlockItemIds;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopperCollection;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends FabricTagsProvider.BlockTagsProvider {
    public ModBlockTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookupFuture) {
        super(output, registryLookupFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        this.builder(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlockItemIds.RAIL_JUNCTION)
                .add(ModBlockItemIds.DIRECTOR_RAIL)
                .add(ModBlockItemIds.CONVEYOR_RAIL)
                .addAll(toIds(ModBlockItemIds.COPPER_RAIL));

        this.builder(BlockTags.RAILS)
                .add(ModBlockItemIds.RAIL_JUNCTION)
                .add(ModBlockItemIds.DIRECTOR_RAIL)
                .add(ModBlockItemIds.CONVEYOR_RAIL)
                .addAll(toIds(ModBlockItemIds.COPPER_RAIL));
    }

    private static WeatheringCopperCollection<ResourceKey<Block>> toIds(final WeatheringCopperCollection<BlockItemId> ids) {
        return ids.map(BlockItemId::block);
    }
}
