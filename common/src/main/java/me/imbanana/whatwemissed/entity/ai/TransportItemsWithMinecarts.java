package me.imbanana.whatwemissed.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.behavior.TransportItemsBetweenContainers;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecartContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class TransportItemsWithMinecarts extends TransportItemsBetweenContainers {
    private static final double MAX_PARKED_MINECART_SPEED_SQR = 1.0E-4;

    private final float speedModifier;
    private final int horizontalSearchDistance;
    private final int verticalSearchDistance;
    private final Predicate<BlockState> sourceBlockType;
    private final Predicate<BlockState> destinationBlockType;
    private final Predicate<TransportItemTarget> shouldQueueForTarget;
    private final Consumer<PathfinderMob> onStartTravelling;
    private final Map<ContainerInteractionState, OnTargetReachedInteraction> onTargetInteractionActions;
    private @Nullable Target target = null;
    private TransportItemState state;
    private @Nullable ContainerInteractionState interactionState;
    private int ticksSinceReachingTarget;

    public TransportItemsWithMinecarts(
            final float speedModifier,
            final Predicate<BlockState> sourceBlockType,
            final Predicate<BlockState> destinationBlockType,
            final int horizontalSearchDistance,
            final int verticalSearchDistance,
            final Map<ContainerInteractionState, OnTargetReachedInteraction> onTargetInteractionActions,
            final Consumer<PathfinderMob> onStartTravelling,
            final Predicate<TransportItemTarget> shouldQueueForTarget
    ) {
        super(speedModifier, sourceBlockType, destinationBlockType, horizontalSearchDistance, verticalSearchDistance, onTargetInteractionActions, onStartTravelling, shouldQueueForTarget);
        this.speedModifier = speedModifier;
        this.sourceBlockType = sourceBlockType;
        this.destinationBlockType = destinationBlockType;
        this.horizontalSearchDistance = horizontalSearchDistance;
        this.verticalSearchDistance = verticalSearchDistance;
        this.onStartTravelling = onStartTravelling;
        this.shouldQueueForTarget = shouldQueueForTarget;
        this.onTargetInteractionActions = onTargetInteractionActions;
        this.state = TransportItemState.TRAVELLING;
    }

    @Override
    protected void tick(final ServerLevel level, final PathfinderMob body, final long timestamp) {
        boolean updatedInvalidTarget = this.updateInvalidTarget(level, body);
        if (this.target == null) {
            this.stop(level, body, timestamp);
        } else if (!updatedInvalidTarget) {
            if (this.state.equals(TransportItemState.QUEUING)) {
                this.onQueuingForTarget(this.target, level, body);
            }

            if (this.state.equals(TransportItemState.TRAVELLING)) {
                this.onTravelToTarget(this.target, level, body);
            }

            if (this.state.equals(TransportItemState.INTERACTING)) {
                this.onReachedTarget(this.target, level, body);
            }
        }
    }

    private boolean updateInvalidTarget(final ServerLevel level, final PathfinderMob body) {
        if (!this.hasValidTarget(level, body)) {
            this.stopTargetingCurrentTarget(body);
            Optional<Target> targetBlockPosition = this.getTransportTarget(level, body);
            if (targetBlockPosition.isPresent()) {
                this.target = targetBlockPosition.get();
                this.onStartTravelling(body);
                this.setVisitedBlockPos(body, level, this.target.pos());
            } else {
                this.enterCooldownAfterNoMatchingTargetFound(body);
            }

            return true;
        }

        return false;
    }

    private void onQueuingForTarget(final Target target, final Level level, final PathfinderMob body) {
        if (!this.isAnotherMobInteractingWithTarget(target, level)) {
            this.resumeTravelling(body);
        }
    }

    private void onTravelToTarget(final Target target, final Level level, final PathfinderMob body) {
        if (this.isWithinTargetDistance(3.0, target, level, body, this.getCenterPos(body)) && this.isAnotherMobInteractingWithTarget(target, level)) {
            this.startQueuing(body);
        } else if (this.isWithinTargetDistance(getInteractionRange(body), target, level, body, this.getCenterPos(body))) {
            this.startOnReachedTargetInteraction(target, body);
        } else {
            this.walkTowardsTarget(body);
        }
    }

    private Vec3 getCenterPos(final PathfinderMob body) {
        return this.setMiddleYPosition(body, body.position());
    }

    private void onReachedTarget(final Target target, final Level level, final PathfinderMob body) {
        if (!this.isWithinTargetDistance(2.0, target, level, body, this.getCenterPos(body))) {
            this.onStartTravelling(body);
        } else {
            this.ticksSinceReachingTarget++;
            this.onTargetInteraction(target, body);
            if (this.ticksSinceReachingTarget >= 60) {
                this.doReachedTargetInteraction(
                        body,
                        target.container(),
                        this::pickUpItems,
                        (mob, container) -> this.stopTargetingCurrentTarget(body),
                        this::putDownItem,
                        (mob, container) -> this.stopTargetingCurrentTarget(body)
                );
                this.onStartTravelling(body);
            }
        }
    }

    private void startQueuing(final PathfinderMob body) {
        this.stopInPlace(body);
        this.setTransportingState(TransportItemState.QUEUING);
    }

    private void resumeTravelling(final PathfinderMob body) {
        this.setTransportingState(TransportItemState.TRAVELLING);
        this.walkTowardsTarget(body);
    }

    private void walkTowardsTarget(final PathfinderMob body) {
        if (this.target != null) {
            if (this.target.minecart() != null) {
                BehaviorUtils.setWalkAndLookTargetMemories(body, this.target.minecart(), this.speedModifier, 0);
            } else {
                BehaviorUtils.setWalkAndLookTargetMemories(body, this.target.pos(), this.speedModifier, 0);
            }
        }
    }

    private void startOnReachedTargetInteraction(final Target target, final PathfinderMob body) {
        this.doReachedTargetInteraction(
                body,
                target.container(),
                this.onReachedInteraction(ContainerInteractionState.PICKUP_ITEM),
                this.onReachedInteraction(ContainerInteractionState.PICKUP_NO_ITEM),
                this.onReachedInteraction(ContainerInteractionState.PLACE_ITEM),
                this.onReachedInteraction(ContainerInteractionState.PLACE_NO_ITEM)
        );
        this.setTransportingState(TransportItemState.INTERACTING);
    }

    private void onStartTravelling(final PathfinderMob body) {
        this.closeMinecartIfOpen(body);
        this.onStartTravelling.accept(body);
        this.setTransportingState(TransportItemState.TRAVELLING);
        this.interactionState = null;
        this.ticksSinceReachingTarget = 0;
    }

    private BiConsumer<PathfinderMob, Container> onReachedInteraction(final ContainerInteractionState state) {
        return (mob, container) -> this.setInteractionState(state);
    }

    private void setTransportingState(final TransportItemState state) {
        this.state = state;
    }

    private void setInteractionState(final ContainerInteractionState state) {
        this.interactionState = state;
    }

    private void onTargetInteraction(final Target target, final PathfinderMob body) {
        PositionTracker lookTarget = target.minecart() != null ? new EntityTracker(target.minecart(), false) : new BlockPosTracker(target.pos());
        body.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, lookTarget);
        this.stopInPlace(body);
        if (this.interactionState != null) {
            Optional.ofNullable(this.onTargetInteractionActions.get(this.interactionState))
                    .ifPresent(action -> action.accept(body, target.vanilla(), this.ticksSinceReachingTarget));
        }
    }

    private void doReachedTargetInteraction(
            final PathfinderMob body,
            final Container container,
            final BiConsumer<PathfinderMob, Container> onPickupSuccess,
            final BiConsumer<PathfinderMob, Container> onPickupFailure,
            final BiConsumer<PathfinderMob, Container> onPlaceSuccess,
            final BiConsumer<PathfinderMob, Container> onPlaceFailure
    ) {
        if (isPickingUpItems(body)) {
            if (matchesGettingItemsRequirement(container)) {
                onPickupSuccess.accept(body, container);
            } else {
                onPickupFailure.accept(body, container);
            }
        } else if (matchesLeavingItemsRequirement(body, container)) {
            onPlaceSuccess.accept(body, container);
        } else {
            onPlaceFailure.accept(body, container);
        }
    }

    private Optional<Target> getTransportTarget(final ServerLevel level, final PathfinderMob body) {
        AABB targetBlockSearchArea = this.getTargetSearchArea(body);
        Set<GlobalPos> visitedPositions = getVisitedPositions(body);
        Set<GlobalPos> unreachablePositions = getUnreachablePositions(body);
        List<ChunkPos> list = ChunkPos.rangeClosed(ChunkPos.containing(body.blockPosition()), Math.floorDiv(this.getHorizontalSearchDistance(body), 16) + 1).toList();
        Target target = null;
        double closestDistance = Float.MAX_VALUE;

        for (ChunkPos chunkPos : list) {
            LevelChunk levelChunk = level.getChunkSource().getChunkNow(chunkPos.x(), chunkPos.z());
            if (levelChunk != null) {
                for (BlockEntity potentialTarget : levelChunk.getBlockEntities().values()) {
                    if (potentialTarget instanceof ChestBlockEntity chestBlockEntity) {
                        double distance = chestBlockEntity.getBlockPos().distToCenterSqr(body.position());
                        if (distance < closestDistance) {
                            Target targetValidToPick = this.isTargetValidToPick(
                                    body, level, chestBlockEntity, visitedPositions, unreachablePositions, targetBlockSearchArea
                            );
                            if (targetValidToPick != null) {
                                target = targetValidToPick;
                                closestDistance = distance;
                            }
                        }
                    }
                }
            }
        }

        for (AbstractMinecartContainer minecart : level.getEntitiesOfClass(AbstractMinecartContainer.class, targetBlockSearchArea, Entity::isAlive)) {
            double distance = minecart.position().distanceToSqr(body.position());
            if (distance < closestDistance) {
                Target targetValidToPick = this.isMinecartValidToPick(
                        body, level, minecart, visitedPositions, unreachablePositions, targetBlockSearchArea
                );
                if (targetValidToPick != null) {
                    target = targetValidToPick;
                    closestDistance = distance;
                }
            }
        }

        return target == null ? Optional.empty() : Optional.of(target);
    }

    private @Nullable Target isTargetValidToPick(
            final PathfinderMob body,
            final Level level,
            final BlockEntity blockEntity,
            final Set<GlobalPos> visitedPositions,
            final Set<GlobalPos> unreachablePositions,
            final AABB targetBlockSearchArea
    ) {
        BlockPos blockPos = blockEntity.getBlockPos();
        boolean isWithinSearchArea = targetBlockSearchArea.contains(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        if (!isWithinSearchArea) {
            return null;
        }

        TransportItemTarget transportItemTarget = TransportItemTarget.tryCreatePossibleTarget(blockEntity, level);
        if (transportItemTarget == null) {
            return null;
        }

        Target target = Target.of(transportItemTarget);
        boolean isValidTarget = this.isWantedBlock(body, target.state())
                && !this.isPositionAlreadyVisited(visitedPositions, unreachablePositions, target, level)
                && !this.isContainerLocked(target);
        return isValidTarget ? target : null;
    }

    private @Nullable Target isMinecartValidToPick(
            final PathfinderMob body,
            final Level level,
            final AbstractMinecartContainer minecart,
            final Set<GlobalPos> visitedPositions,
            final Set<GlobalPos> unreachablePositions,
            final AABB targetBlockSearchArea
    ) {
        if (!targetBlockSearchArea.contains(minecart.position()) || !isParked(minecart)) {
            return null;
        }

        Target target = Target.of(minecart);
        boolean isValidTarget = this.isWantedBlock(body, target.state())
                && !this.isPositionAlreadyVisited(visitedPositions, unreachablePositions, target, level);
        return isValidTarget ? target : null;
    }

    private boolean isContainerLocked(final Target target) {
        return target.blockEntity() instanceof BaseContainerBlockEntity blockEntity && blockEntity.isLocked();
    }

    private boolean hasValidTarget(final Level level, final PathfinderMob body) {
        boolean targetIsOfValidType = this.target != null && this.isWantedBlock(body, this.target.state()) && this.targetHasNotChanged(level, this.target);
        if (targetIsOfValidType && !this.isTargetBlocked(level, this.target)) {
            if (!this.state.equals(TransportItemState.TRAVELLING)) {
                return true;
            }

            if (!isParked(this.target)) {
                return false;
            }

            if (this.hasValidTravellingPath(level, this.target, body)) {
                return true;
            }

            this.markVisitedBlockPosAsUnreachable(body, level, this.target.pos());
        }

        return false;
    }

    private boolean hasValidTravellingPath(final Level level, final Target target, final PathfinderMob body) {
        Path path = body.getNavigation().getPath() == null ? this.createPath(target, body) : body.getNavigation().getPath();
        Vec3 posFromWhichToReachTarget = this.getPositionToReachTargetFrom(path, body);
        boolean canReachTarget = this.isWithinTargetDistance(getInteractionRange(body), target, level, body, posFromWhichToReachTarget);
        boolean hasNotYetCreatedPathToTarget = path == null && !canReachTarget;
        return hasNotYetCreatedPathToTarget || this.targetIsReachableFromPosition(level, canReachTarget, posFromWhichToReachTarget, target, body);
    }

    private @Nullable Path createPath(final Target target, final PathfinderMob body) {
        return target.minecart() != null ? body.getNavigation().createPath(target.minecart(), 0) : body.getNavigation().createPath(target.pos(), 0);
    }

    private Vec3 getPositionToReachTargetFrom(final @Nullable Path path, final PathfinderMob body) {
        boolean haveNoValidPath = path == null || path.getEndNode() == null;
        Vec3 bottomCenter = haveNoValidPath ? body.position() : Vec3.atBottomCenterOf(path.getEndNode().asBlockPos());
        return this.setMiddleYPosition(body, bottomCenter);
    }

    private Vec3 setMiddleYPosition(final PathfinderMob body, final Vec3 pos) {
        return pos.add(0.0, body.getBoundingBox().getYsize() / 2.0, 0.0);
    }

    private boolean isTargetBlocked(final Level level, final Target target) {
        return target.minecart() == null && ChestBlock.isChestBlockedAt(level, target.pos());
    }

    private boolean targetHasNotChanged(final Level level, final Target target) {
        if (target.minecart() != null) {
            return target.minecart().isAlive() && target.minecart().level() == level;
        }

        return target.blockEntity().equals(level.getBlockEntity(target.pos()));
    }

    private Stream<Target> getConnectedTargets(final Target target, final Level level) {
        if (target.minecart() == null && target.state().getValueOrElse(ChestBlock.TYPE, ChestType.SINGLE) != ChestType.SINGLE) {
            TransportItemTarget connectedTarget = TransportItemTarget.tryCreatePossibleTarget(
                    ChestBlock.getConnectedBlockPos(target.pos(), target.state()), level
            );
            return connectedTarget != null ? Stream.of(target, Target.of(connectedTarget)) : Stream.of(target);
        } else {
            return Stream.of(target);
        }
    }

    private AABB getTargetSearchArea(final PathfinderMob mob) {
        int horizontalSearchDistance = this.getHorizontalSearchDistance(mob);
        return new AABB(mob.blockPosition()).inflate(horizontalSearchDistance, this.getVerticalSearchDistance(mob), horizontalSearchDistance);
    }

    private int getHorizontalSearchDistance(final PathfinderMob mob) {
        return mob.isPassenger() ? 1 : this.horizontalSearchDistance;
    }

    private int getVerticalSearchDistance(final PathfinderMob mob) {
        return mob.isPassenger() ? 1 : this.verticalSearchDistance;
    }

    private static Set<GlobalPos> getVisitedPositions(final PathfinderMob mob) {
        return mob.getBrain().getMemory(MemoryModuleType.VISITED_BLOCK_POSITIONS).orElse(Set.of());
    }

    private static Set<GlobalPos> getUnreachablePositions(final PathfinderMob mob) {
        return mob.getBrain().getMemory(MemoryModuleType.UNREACHABLE_TRANSPORT_BLOCK_POSITIONS).orElse(Set.of());
    }

    private boolean isPositionAlreadyVisited(final Set<GlobalPos> visitedPositions, final Set<GlobalPos> unreachablePositions, final Target target, final Level level) {
        return this.getConnectedTargets(target, level)
                .map(transportItemTarget -> new GlobalPos(level.dimension(), transportItemTarget.pos()))
                .anyMatch(pos -> visitedPositions.contains(pos) || unreachablePositions.contains(pos));
    }

    private static boolean hasFinishedPath(final PathfinderMob body) {
        return body.getNavigation().getPath() != null && body.getNavigation().getPath().isDone();
    }

    private boolean isWantedBlock(final PathfinderMob mob, final BlockState block) {
        return isPickingUpItems(mob) ? this.sourceBlockType.test(block) : this.destinationBlockType.test(block);
    }

    private static double getInteractionRange(final PathfinderMob body) {
        return hasFinishedPath(body) ? 1.0 : 0.5;
    }

    private boolean isWithinTargetDistance(final double distance, final Target target, final Level level, final PathfinderMob body, final Vec3 fromPos) {
        AABB boundingBox = body.getBoundingBox();
        AABB movedBoundBox = AABB.ofSize(fromPos, boundingBox.getXsize(), boundingBox.getYsize(), boundingBox.getZsize());
        if (target.minecart() != null) {
            return target.minecart().getBoundingBox().inflate(distance, 0.5, distance).intersects(movedBoundBox);
        }

        return target.state().getCollisionShape(level, target.pos()).bounds().inflate(distance, 0.5, distance).move(target.pos()).intersects(movedBoundBox);
    }

    private boolean targetIsReachableFromPosition(final Level level, final boolean canReachTarget, final Vec3 pos, final Target target, final PathfinderMob body) {
        return canReachTarget && this.canSeeAnyTargetSide(target, level, body, pos);
    }

    private boolean canSeeAnyTargetSide(final Target target, final Level level, final PathfinderMob body, final Vec3 eyePosition) {
        if (target.minecart() != null) {
            AABB boundingBox = target.minecart().getBoundingBox();
            Vec3 center = boundingBox.getCenter();
            return Stream.of(center, new Vec3(center.x, boundingBox.maxY, center.z))
                    .map(hitTarget -> level.clip(new ClipContext(eyePosition, hitTarget, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, body)))
                    .anyMatch(hitResult -> hitResult.getType() == HitResult.Type.MISS);
        }

        Vec3 center = Vec3.atCenterOf(target.pos());
        return Direction.stream()
                .map(direction -> center.add(0.5 * direction.getStepX(), 0.5 * direction.getStepY(), 0.5 * direction.getStepZ()))
                .map(hitTarget -> level.clip(new ClipContext(eyePosition, hitTarget, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, body)))
                .anyMatch(hitResult -> hitResult.getType() == HitResult.Type.BLOCK && hitResult.getBlockPos().equals(target.pos()));
    }

    private boolean isAnotherMobInteractingWithTarget(final Target target, final Level level) {
        if (target.minecart() != null) {
            return !target.minecart().getEntitiesWithContainerOpen().isEmpty();
        }

        return this.getConnectedTargets(target, level).map(Target::vanilla).anyMatch(this.shouldQueueForTarget);
    }

    private static boolean isPickingUpItems(final PathfinderMob body) {
        return body.getMainHandItem().isEmpty();
    }

    private static boolean matchesGettingItemsRequirement(final Container container) {
        return !container.isEmpty();
    }

    private static boolean matchesLeavingItemsRequirement(final PathfinderMob body, final Container container) {
        return container.isEmpty() || hasItemMatchingHandItem(body, container);
    }

    private static boolean hasItemMatchingHandItem(final PathfinderMob body, final Container container) {
        ItemStack mainHandItem = body.getMainHandItem();

        for (ItemStack itemStack : container) {
            if (ItemStack.isSameItem(itemStack, mainHandItem)) {
                return true;
            }
        }

        return false;
    }

    private void pickUpItems(final PathfinderMob body, final Container container) {
        body.setItemSlot(EquipmentSlot.MAINHAND, pickupItemFromContainer(container));
        body.setGuaranteedDrop(EquipmentSlot.MAINHAND);
        container.setChanged();
        this.clearMemoriesAfterMatchingTargetFound(body);
    }

    private void putDownItem(final PathfinderMob body, final Container container) {
        ItemStack itemsLeftAfterVisitingChest = addItemsToContainer(body, container);
        container.setChanged();
        body.setItemSlot(EquipmentSlot.MAINHAND, itemsLeftAfterVisitingChest);
        if (itemsLeftAfterVisitingChest.isEmpty()) {
            this.clearMemoriesAfterMatchingTargetFound(body);
        } else {
            this.stopTargetingCurrentTarget(body);
        }
    }

    private static ItemStack pickupItemFromContainer(final Container container) {
        int slot = 0;

        for (ItemStack itemStack : container) {
            if (!itemStack.isEmpty()) {
                int itemCount = Math.min(itemStack.getCount(), 16);
                return container.removeItem(slot, itemCount);
            }

            slot++;
        }

        return ItemStack.EMPTY;
    }

    private static ItemStack addItemsToContainer(final PathfinderMob body, final Container container) {
        int slot = 0;
        ItemStack itemStack = body.getMainHandItem();

        for (ItemStack containerItemStack : container) {
            if (containerItemStack.isEmpty()) {
                container.setItem(slot, itemStack);
                return ItemStack.EMPTY;
            }

            if (ItemStack.isSameItemSameComponents(containerItemStack, itemStack) && containerItemStack.getCount() < containerItemStack.getMaxStackSize()) {
                int countThatCanBeAdded = containerItemStack.getMaxStackSize() - containerItemStack.getCount();
                int countToAdd = Math.min(countThatCanBeAdded, itemStack.getCount());
                containerItemStack.setCount(containerItemStack.getCount() + countToAdd);
                itemStack.setCount(itemStack.getCount() - countThatCanBeAdded);
                container.setItem(slot, containerItemStack);
                if (itemStack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }

            slot++;
        }

        return itemStack;
    }

    @Override
    protected void stopTargetingCurrentTarget(final PathfinderMob body) {
        this.closeMinecartIfOpen(body);
        this.ticksSinceReachingTarget = 0;
        this.target = null;
        super.stopTargetingCurrentTarget(body);
    }

    private void enterCooldownAfterNoMatchingTargetFound(final PathfinderMob body) {
        this.stopTargetingCurrentTarget(body);
        body.getBrain().setMemory(MemoryModuleType.TRANSPORT_ITEMS_COOLDOWN_TICKS, 140);
        body.getBrain().eraseMemory(MemoryModuleType.VISITED_BLOCK_POSITIONS);
        body.getBrain().eraseMemory(MemoryModuleType.UNREACHABLE_TRANSPORT_BLOCK_POSITIONS);
    }

    @Override
    protected void stop(final ServerLevel level, final PathfinderMob body, final long timestamp) {
        this.onStartTravelling(body);
        if (body.getNavigation() instanceof GroundPathNavigation pathNavigation) {
            pathNavigation.setCanPathToTargetsBelowSurface(false);
        }
    }

    private void stopInPlace(final PathfinderMob mob) {
        mob.getNavigation().stop();
        mob.setXxa(0.0F);
        mob.setYya(0.0F);
        mob.setSpeed(0.0F);
        mob.setDeltaMovement(0.0, mob.getDeltaMovement().y, 0.0);
    }

    private void closeMinecartIfOpen(final PathfinderMob body) {
        if (this.target == null || this.target.minecart() == null || !(body instanceof ContainerUser containerUser)) {
            return;
        }

        AbstractMinecartContainer minecart = this.target.minecart();
        if (minecart.getEntitiesWithContainerOpen().contains(containerUser)) {
            minecart.stopOpen(containerUser);
        }
    }

    private static boolean isParked(final Target target) {
        return target.minecart() == null || isParked(target.minecart());
    }

    private static boolean isParked(final AbstractMinecartContainer minecart) {
        return minecart.getDeltaMovement().horizontalDistanceSqr() < MAX_PARKED_MINECART_SPEED_SQR;
    }

    private record Target(TransportItemTarget vanilla, @Nullable AbstractMinecartContainer minecart) {
        private static Target of(final TransportItemTarget target) {
            return new Target(target, null);
        }

        private static Target of(final AbstractMinecartContainer minecart) {
            return new Target(new TransportItemTarget(minecart.blockPosition(), minecart, null, minecart.getDefaultDisplayBlockState()), minecart);
        }

        private BlockPos pos() {
            return this.vanilla.pos();
        }

        private Container container() {
            return this.vanilla.container();
        }

        private BlockEntity blockEntity() {
            return this.vanilla.blockEntity();
        }

        private BlockState state() {
            return this.minecart != null ? this.minecart.getDefaultDisplayBlockState() : this.vanilla.state();
        }
    }
}
