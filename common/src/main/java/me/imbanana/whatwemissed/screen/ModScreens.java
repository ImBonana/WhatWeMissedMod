package me.imbanana.whatwemissed.screen;

import me.imbanana.whatwemissed.screen.minecartfurnace.MinecartFurnaceScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public class ModScreens {
    public static void registerAll(Registrar registrar) {
        registrar.register(ModMenuTypes.MINECART_FURNACE.get(), MinecartFurnaceScreen::new);
    }

    @FunctionalInterface
    public interface Registrar {
        <M extends AbstractContainerMenu, S extends Screen & MenuAccess<M>> void register(MenuType<? extends M> type, MenuScreens.ScreenConstructor<M, S> constructor);
    }
}
