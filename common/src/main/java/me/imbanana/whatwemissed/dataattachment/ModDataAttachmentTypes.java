package me.imbanana.whatwemissed.dataattachment;

import me.imbanana.whatwemissed.WhatWeMissed;
import me.imbanana.whatwemissed.dataattachment.attachment.MinecartLinkData;
import me.imbanana.whatwemissed.services.dataattachment.EntityAttachment;

public class ModDataAttachmentTypes {

    public static final EntityAttachment<MinecartLinkData> MINECART_LINK = ModAttachments.syncedEntity(
            WhatWeMissed.ifOd("minecart_link"),
            MinecartLinkData.CODEC,
            MinecartLinkData.STREAM_CODEC,
            () -> MinecartLinkData.EMPTY
    );

    public static void init() { }
}
