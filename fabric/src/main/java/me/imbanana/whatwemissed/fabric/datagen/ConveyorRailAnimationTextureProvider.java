package me.imbanana.whatwemissed.fabric.datagen;

import com.google.common.hash.HashCode;
import me.imbanana.whatwemissed.WhatWeMissed;
import me.imbanana.whatwemissed.block.custom.ConveyorRailBlock;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class ConveyorRailAnimationTextureProvider implements DataProvider {
    private final FabricPackOutput output;

    protected ConveyorRailAnimationTextureProvider(FabricPackOutput output) {
        this.output = output;

    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return CompletableFuture.runAsync(() -> {
            try {
                InputStream imageStream = ConveyorRailAnimationTextureProvider.class.getResourceAsStream("/assets/" + WhatWeMissed.MOD_ID + "/textures/block/conveyor_rail_1.png");
                if (imageStream == null) throw new RuntimeException("Base conveyor rail image not found!");

                BufferedImage tile = ImageIO.read(imageStream);
                Path dir = output.getOutputFolder().resolve("assets/" + WhatWeMissed.MOD_ID + "/textures/block");
                Files.createDirectories(dir);

                for (Integer speed : ConveyorRailBlock.SPEED.getPossibleValues()) {
                    if (speed.equals(0)) continue;

                    int frametime = 16 - speed;

                    if (!speed.equals(1)) {
                        Path texturePath = dir.resolve("conveyor_rail_" + speed + ".png");
                        ByteArrayOutputStream imageBytes = new ByteArrayOutputStream();
                        ImageIO.write(tile, "png", imageBytes);

                        cache.writeIfNeeded(
                                texturePath,
                                imageBytes.toByteArray(),
                                HashCode.fromBytes(imageBytes.toByteArray())
                        );
                    }

                    Path mcmetaPath = dir.resolve("conveyor_rail_" + speed + ".png.mcmeta");
                    String metadata = "{\"animation\":{\"frametime\":" + frametime + ",\"interpolate\":false}}";

                    cache.writeIfNeeded(
                            mcmetaPath,
                            metadata.getBytes(StandardCharsets.UTF_8),
                            HashCode.fromBytes(metadata.getBytes(StandardCharsets.UTF_8))
                    );
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    @Override
    public String getName() {
        return "Rail animation textures";
    }
}
