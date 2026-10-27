package me.imbanana.whatwemissed.services;

import me.imbanana.whatwemissed.services.dataattachment.DataAttachmentService;
import me.imbanana.whatwemissed.services.network.NetworkService;

public interface PlatformBootstrap {
    NetworkService network();
    RegistryService registry();
    DataAttachmentService dataAttachment();
}
