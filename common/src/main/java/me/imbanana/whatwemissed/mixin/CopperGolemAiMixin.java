package me.imbanana.whatwemissed.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.imbanana.whatwemissed.entity.ai.TransportItemsWithMinecarts;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.TransportItemsBetweenContainers;
import net.minecraft.world.entity.animal.golem.CopperGolemAi;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Mixin(CopperGolemAi.class)
public abstract class CopperGolemAiMixin {
    @WrapOperation(
            method = "initIdleActivity",
            at = @At(
                    value = "NEW",
                    target = "(FLjava/util/function/Predicate;Ljava/util/function/Predicate;IILjava/util/Map;Ljava/util/function/Consumer;Ljava/util/function/Predicate;)Lnet/minecraft/world/entity/ai/behavior/TransportItemsBetweenContainers;"
            )
    )
    private static TransportItemsBetweenContainers useMinecartTransportBehavior(
            float speedModifier,
            Predicate<BlockState> sourceBlockType,
            Predicate<BlockState> destinationBlockType,
            int horizontalSearchDistance,
            int verticalSearchDistance,
            Map<TransportItemsBetweenContainers.ContainerInteractionState, TransportItemsBetweenContainers.OnTargetReachedInteraction> onTargetInteractionActions,
            Consumer<PathfinderMob> onStartTravelling,
            Predicate<TransportItemsBetweenContainers.TransportItemTarget> shouldQueueForTarget,
            Operation<TransportItemsBetweenContainers> original
    ) {
        return new TransportItemsWithMinecarts(
                speedModifier,
                sourceBlockType,
                destinationBlockType,
                horizontalSearchDistance,
                verticalSearchDistance,
                onTargetInteractionActions,
                onStartTravelling,
                shouldQueueForTarget
        );
    }
}
