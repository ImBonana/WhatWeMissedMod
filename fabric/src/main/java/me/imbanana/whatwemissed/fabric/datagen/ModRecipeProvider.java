package me.imbanana.whatwemissed.fabric.datagen;

import me.imbanana.whatwemissed.block.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Blocks;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends FabricRecipeProvider {
    public ModRecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, BootstrapContext<Recipe<?>> recipes, BootstrapContext<Advancement> advancements) {
        return new RecipeProvider(recipes, advancements) {
            @Override
            public void buildRecipes() {
                shaped(RecipeCategory.TRANSPORTATION, ModBlocks.COPPER_RAIL.weathering().unaffected(), 8)
                        .pattern("c c")
                        .pattern("csc")
                        .pattern("c c")
                        .define('c', Items.COPPER_INGOT)
                        .define('s', Items.STICK)
                        .unlockedBy("has_rail", this.has(Blocks.RAIL))
                        .save(output);

                shaped(RecipeCategory.TRANSPORTATION, ModBlocks.DIRECTOR_RAIL, 8)
                        .pattern("iri")
                        .pattern("isi")
                        .pattern("iri")
                        .define('i', Items.IRON_INGOT)
                        .define('r', Items.REDSTONE)
                        .define('s', Items.STICK)
                        .unlockedBy("has_rail", this.has(Blocks.RAIL))
                        .save(output);

                shaped(RecipeCategory.TRANSPORTATION, ModBlocks.RAIL_JUNCTION, 8)
                        .pattern(" i ")
                        .pattern("isi")
                        .pattern(" i ")
                        .define('i', Items.IRON_INGOT)
                        .define('s', Items.STICK)
                        .unlockedBy("has_rail", this.has(Blocks.RAIL))
                        .save(output);

                shaped(RecipeCategory.TRANSPORTATION, ModBlocks.CONVEYOR_RAIL, 8)
                        .pattern("nrn")
                        .pattern("isi")
                        .pattern("nrn")
                        .define('i', Items.IRON_INGOT)
                        .define('n', Items.IRON_NUGGET)
                        .define('r', Items.REDSTONE)
                        .define('s', Items.STICK)
                        .unlockedBy("has_rail", this.has(Blocks.RAIL))
                        .save(output);
            }
        };
    }

    @Override
    public String getName() {
        return "What We Missed Recipe provider";
    }
}
