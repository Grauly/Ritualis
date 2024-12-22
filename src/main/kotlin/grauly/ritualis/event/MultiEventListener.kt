package grauly.ritualis.event

import net.minecraft.entity.Entity
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.event.BlockPositionSource
import net.minecraft.world.event.EntityPositionSource
import net.minecraft.world.event.GameEvent
import net.minecraft.world.event.PositionSource
import net.minecraft.world.event.listener.GameEventListener

class MultiEventListener(private val positionSource: PositionSource) : GameEventListener {
    constructor(entity: Entity, feetPosOffset: Float = 0f) : this(EntityPositionSource(entity, feetPosOffset))
    constructor(blockPos: BlockPos) : this(BlockPositionSource(blockPos))

    private val subEventListeners: MutableList<TypedEventListener> = mutableListOf()

    override fun getPositionSource(): PositionSource = positionSource

    //max range of the ranges of the sub listeners
    override fun getRange(): Int = subEventListeners.maxOfOrNull { listener -> listener.getRange() } ?: 0

    override fun listen(
        world: ServerWorld,
        event: RegistryEntry<GameEvent>,
        emitter: GameEvent.Emitter,
        emitterPos: Vec3d
    ): Boolean =
        //basically, tried to trigger the events, and then ors all the return values to see if any sub event has accepted the event
        subEventListeners.map { listener ->
            if (listener.accepts(event)) listener.trigger(world, emitterPos, emitter) else false
        }.reduce { a, b -> a || b }

}