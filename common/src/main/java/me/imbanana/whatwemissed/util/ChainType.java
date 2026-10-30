package me.imbanana.whatwemissed.util;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import me.imbanana.whatwemissed.managers.minecart.MinecartLinkManager;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public enum ChainType implements StringRepresentable {
    IRON("iron", 0, Items.IRON_CHAIN),
    COPPER("copper", 1, Items.COPPER_CHAIN.weathering().unaffected()),
    COPPER_EXPOSED("exposed_copper", 2, Items.COPPER_CHAIN.weathering().exposed()),
    COPPER_WEATHERED("weathered_copper", 3, Items.COPPER_CHAIN.weathering().weathered()),
    COPPER_OXIDIZED("oxidized_copper", 4, Items.COPPER_CHAIN.weathering().oxidized());

    public static final Codec<ChainType> CODEC = StringRepresentable.fromEnum(ChainType::values);
    public static final StreamCodec<ByteBuf, ChainType> STREAM_CODEC = ByteBufCodecs.idMapper(
            ByIdMap.continuous(
                    ChainType::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO
            ),
            ChainType::getId
    );

    private final String name;
    private final int id;
    private final Item dropItem;

    ChainType(String name, int id, Item dropItem) {
        this.name = name;
        this.id = id;
        this.dropItem = dropItem;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public Item getDropItem() {
        return dropItem;
    }

    public int getId() {
        return id;
    }
}
