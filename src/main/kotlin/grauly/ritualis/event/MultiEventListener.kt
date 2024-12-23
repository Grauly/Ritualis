package grauly.ritualis.event

import grauly.ritualis.Ritualis
import net.minecraft.entity.Entity
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtOps
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.dynamic.Codecs
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.event.BlockPositionSource
import net.minecraft.world.event.EntityPositionSource
import net.minecraft.world.event.GameEvent
import net.minecraft.world.event.PositionSource
import net.minecraft.world.event.listener.GameEventListener

class MultiEventListener(
    private val positionSource: PositionSource,
    private val shouldTrackCooldown: Boolean = false
) : GameEventListener {
    constructor(entity: Entity, feetPosOffset: Float = 0f, shouldTrackCooldown: Boolean = false) : this(
        EntityPositionSource(entity, feetPosOffset),
        shouldTrackCooldown
    )

    constructor(blockPos: BlockPos, shouldTrackCooldown: Boolean = false) : this(
        BlockPositionSource(blockPos),
        shouldTrackCooldown
    )

    private var cooldown: Int = 0

    private val subEventListeners: MutableList<TypedEventListener> = mutableListOf()

    override fun getPositionSource(): PositionSource = positionSource

    //max range of the ranges of the sub listeners
    override fun getRange(): Int = subEventListeners.maxOfOrNull { listener -> listener.getRange() } ?: 0

    fun tick() {
        subEventListeners.forEach { listener ->
            listener.tick()
        }
    }

    fun readNbt(
        nbt: NbtCompound,
        registries: RegistryWrapper.WrapperLookup,
        subListenerDataKey: String = "subListenerData",
        cooldownDataKey: String = "cooldown"
    ) {
        val regOps = registries.getOps(NbtOps.INSTANCE)
        if (nbt.contains(cooldownDataKey)) {
            val cooldownData = nbt.getCompound(cooldownDataKey)
            Codecs.NON_NEGATIVE_INT.parse(regOps, cooldownData)
                .resultOrPartial { error -> Ritualis.LOGGER.warn("Could not parse cooldown for multi event listener: $error") }
                .ifPresent { parsedCooldown -> cooldown = parsedCooldown }
        }
        if (nbt.contains(subListenerDataKey)) {
            val subListenerData = nbt.getCompound(subListenerDataKey)
            subEventListeners.forEachIndexed { index, listener ->
                val specificSubListenerData = subListenerData.getCompound("$index")
                listener.readNbt(specificSubListenerData, registries)
            }
        }
    }

    fun writeNbt(
        nbt: NbtCompound,
        registries: RegistryWrapper.WrapperLookup,
        subListenerDataKey: String = "subListenerData",
        cooldownDataKey: String = "cooldown"
    ) {
        val regOps = registries.getOps(NbtOps.INSTANCE)
        Codecs.NON_NEGATIVE_INT.encodeStart(regOps, cooldown)
            .resultOrPartial { error -> Ritualis.LOGGER.warn("Could not encode cooldown for multi event listener: $error") }
            .ifPresent { encodedCooldown -> nbt.put(cooldownDataKey, encodedCooldown)}
        val subListenerData = nbt.getCompound(subListenerDataKey)
        subEventListeners.forEachIndexed { index, queuedEventListener ->
            val specificSubListenerData = subListenerData.getCompound("$index")
            queuedEventListener.writeNbt(specificSubListenerData, registries)
        }
    }

    override fun listen(
        world: ServerWorld,
        event: RegistryEntry<GameEvent>,
        emitter: GameEvent.Emitter,
        emitterPos: Vec3d
    ): Boolean =
        //basically, tries to trigger the events, and then ors all the return values to see if any sub event has accepted the event
        subEventListeners.map { listener ->
            if (listener.accepts(event)) listener.trigger(world, emitterPos, emitter) else false
        }.reduce { a, b -> a || b }

}