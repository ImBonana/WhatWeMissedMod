package me.imbanana.whatwemissed.fabric.services;

import me.imbanana.whatwemissed.fabric.services.network.ClientNetworkServiceFabric;
import me.imbanana.whatwemissed.services.ClientPlatformBootstrap;
import me.imbanana.whatwemissed.services.ClientRegistryService;
import me.imbanana.whatwemissed.services.network.ClientNetworkService;

public class ClientPlatformBootstrapFabric implements ClientPlatformBootstrap {
    @Override
    public ClientNetworkService network() {
        return new ClientNetworkServiceFabric();
    }

    @Override
    public ClientRegistryService registry() {
        return new ClientRegistryServiceFabric();
    }
}
