package me.imbanana.whatwemissed.services;

import me.imbanana.whatwemissed.services.network.ClientNetworkService;

public interface ClientPlatformBootstrap {
    ClientNetworkService network();
}
