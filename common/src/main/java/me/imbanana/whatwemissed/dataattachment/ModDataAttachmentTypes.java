package me.imbanana.whatwemissed.dataattachment;

import com.mojang.serialization.Codec;
import me.imbanana.whatwemissed.WhatWeMissed;
import me.imbanana.whatwemissed.dataattachment.attachment.MinecartLinkData;
import me.imbanana.whatwemissed.services.dataattachment.EntityAttachment;
import net.minecraft.network.codec.ByteBufCodecs;

public class ModDataAttachmentTypes {

    public static final EntityAttachment<MinecartLinkData> MINECART_LINK = ModAttachments.syncedEntity(
            WhatWeMissed.idOf("minecart_link"),
            MinecartLinkData.CODEC,
            MinecartLinkData.STREAM_CODEC,
            () -> MinecartLinkData.EMPTY
    );

    public static final EntityAttachment<Boolean> MINECART_CHEST_LID_OPEN = ModAttachments.syncedEntity(
            WhatWeMissed.idOf("minecart_chest_lid_open"),
            Codec.BOOL,
            ByteBufCodecs.BOOL,
            () -> false
    );

    public static void init() { }
}
