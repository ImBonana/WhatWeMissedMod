package me.imbanana.whatwemissed.fabric.services;

import me.imbanana.whatwemissed.fabric.services.network.NetworkServiceFabric;
import me.imbanana.whatwemissed.services.PlatformBootstrap;
import me.imbanana.whatwemissed.services.RegistryService;
import me.imbanana.whatwemissed.services.network.NetworkService;

public class PlatformBootstrapFabric implements PlatformBootstrap {
    @Override
    public NetworkService network() {
        return new NetworkServiceFabric();
    }

    @Override
    public RegistryService registry() {
        return new RegistryServiceFabric();
    }
}
