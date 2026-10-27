package me.imbanana.whatwemissed.neoforge.services;

import me.imbanana.whatwemissed.neoforge.services.dataattchment.DataAttachmentServiceNeoForge;
import me.imbanana.whatwemissed.neoforge.services.network.NetworkServiceNeoForge;
import me.imbanana.whatwemissed.services.PlatformBootstrap;
import me.imbanana.whatwemissed.services.RegistryService;
import me.imbanana.whatwemissed.services.dataattachment.DataAttachmentService;
import me.imbanana.whatwemissed.services.network.NetworkService;

public class PlatformBootstrapNeoForge implements PlatformBootstrap {
    @Override
    public NetworkService network() {
        return new NetworkServiceNeoForge();
    }

    @Override
    public RegistryService registry() {
        return new RegistryServiceNeoForge();
    }

    @Override
    public DataAttachmentService dataAttachment() {
        return new DataAttachmentServiceNeoForge();
    }
}
