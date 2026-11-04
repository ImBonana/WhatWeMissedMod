package me.imbanana.whatwemissed.fabric.datagen;

import me.imbanana.whatwemissed.block.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

public class ModBlockLootTableProvider extends FabricBlockLootSubProvider {
    protected ModBlockLootTableProvider(FabricPackOutput packOutput, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(packOutput, registriesFuture);
    }

    @Override
    public void generate() {
        ModBlocks.COPPER_RAIL.forEach(this::dropSelf);
        this.dropSelf(ModBlocks.RAIL_JUNCTION);
        this.dropSelf(ModBlocks.DIRECTOR_RAIL);
    }
}
