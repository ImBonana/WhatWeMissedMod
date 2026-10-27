package me.imbanana.whatwemissed.neoforge.services;

import me.imbanana.whatwemissed.neoforge.services.network.ClientNetworkServiceNeoForge;
import me.imbanana.whatwemissed.services.ClientPlatformBootstrap;
import me.imbanana.whatwemissed.services.network.ClientNetworkService;

public class ClientPlatformBootstrapNeoForge implements ClientPlatformBootstrap {
    @Override
    public ClientNetworkService network() {
        return new ClientNetworkServiceNeoForge();
    }
}
