package me.imbanana.whatwemissed.managers.minecart.train;

import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class TrainChain {
    private final List<AbstractMinecart> carts;

    private TrainChain(List<AbstractMinecart> carts) {
        this.carts = carts;
    }

    public static TrainChain of(AbstractMinecart start) {
        List<AbstractMinecart> carts = new ArrayList<>();
        carts.add(start);

        for (AbstractMinecart parent = start.what_we_missed$getParent(); parent != null; parent = parent.what_we_missed$getParent()) {
            if (parent == start) {
                Collections.reverse(carts);
                return new TrainChain(carts);
            }
            carts.add(parent);
        }
        Collections.reverse(carts);

        for (AbstractMinecart child = start.what_we_missed$getChild(); child != null && child != start; child = child.what_we_missed$getChild()) {
            carts.add(child);
        }

        return new TrainChain(carts);
    }

    public static AbstractMinecart headOf(AbstractMinecart cart) {
        AbstractMinecart head = cart;
        for (AbstractMinecart parent = cart.what_we_missed$getParent(); parent != null && parent != cart; parent = parent.what_we_missed$getParent()) {
            head = parent;
        }

        return head;
    }

    public int size() {
        return this.carts.size();
    }

    public AbstractMinecart cart(int index) {
        return this.carts.get(index);
    }

    public List<AbstractMinecart> carts() {
        return this.carts;
    }

    public List<UUID> memberIds() {
        List<UUID> ids = new ArrayList<>(this.carts.size());
        for (AbstractMinecart cart : this.carts) ids.add(cart.getUUID());
        return ids;
    }
}
