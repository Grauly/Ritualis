package grauly.ritualis.event

import com.mojang.serialization.codecs.RecordCodecBuilder
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

/**
 * Listens and reacts to multiple given sub listeners.
 *
 * The sub listeners must always be given in the same order, otherwise cooldown persistence is not guaranteed
 */
class QueuedMultiEventListener(
    private val positionSource: PositionSource,
    private val subEventListeners: MutableList<QueuedEventListener>
) : GameEventListener {
    constructor(entity: Entity, subEventListeners: MutableList<QueuedEventListener>, feetPosOffset: Float = 0f) : this(EntityPositionSource(entity, feetPosOffset), subEventListeners)
    constructor(blockPos: BlockPos, subEventListeners: MutableList<QueuedEventListener>) : this(BlockPositionSource(blockPos), subEventListeners)

    private val queuedEvents: MutableList<QueuedEvent> = mutableListOf()

    override fun getPositionSource(): PositionSource = positionSource

    //max range of the ranges of the sub listeners
    override fun getRange(): Int = subEventListeners.maxOfOrNull { listener -> listener.getRange() } ?: 0

    //TODO: top level cooldown handling

    override fun listen(
        world: ServerWorld,
        event: RegistryEntry<GameEvent>,
        emitter: GameEvent.Emitter,
        emitterPos: Vec3d
    ): Boolean = subEventListeners
        .asSequence()
        .filter { subListener -> subListener.accepts(event) }
        .filter { subListener -> !subListener.isOnCooldown() }
        .filter { subListener -> subListener.shouldQueue(world, emitterPos, emitter) }
        .mapIndexed { index, queueReadyListener ->
            val delay = queueReadyListener.calculateDelay(world, emitterPos, emitter)
            val queueReadyEvent = QueuedEvent(delay, event, emitterPos, index)
            val queued = queueEvent(queueReadyEvent)
            if (queued) queueReadyListener.reportQueued(world, emitterPos, emitter)
            return@mapIndexed queued
        }
        .filter { it }
        .toList()
        .isNotEmpty()

    private fun queueEvent(event: QueuedEvent): Boolean {
        val precedingEvents = queuedEvents
            .filter { queuedEvent -> queuedEvent.listenerIndex == event.listenerIndex }
            .filter { queuedEvent -> queuedEvent.delay < event.delay }
        if (precedingEvents.isEmpty()) {
            //no previous events, this event will make a difference
            queuedEvents.add(event)
            return true
        }
        val eventRedundant = precedingEvents
            .map { precedingEvent -> precedingEvent.event }
            .all { precedingEvent -> precedingEvent == event.event }
        if (eventRedundant) return false
        queuedEvents.add(event)
        return true
    }

    fun readNbt(
        nbt: NbtCompound,
        registries: RegistryWrapper.WrapperLookup,
        eventsNbtKey: String = "multiListenerEvents",
        subListenerDataKey: String = "listenerData"
    ) {
        val regOps = registries.getOps(NbtOps.INSTANCE)
        if (nbt.contains(eventsNbtKey)) {
            CODEC.parse(regOps, nbt.get(eventsNbtKey))
                .resultOrPartial { error -> Ritualis.LOGGER.warn("Could not parse event queue for multi event listener: $error") }
                .ifPresent { parsedEvents -> queuedEvents.addAll(parsedEvents) }
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
        eventsNbtKey: String = "multiListenerEvents",
        subListenerDataKey: String = "listenerData"
    ) {
        val regOps = registries.getOps(NbtOps.INSTANCE)
        CODEC.encodeStart(regOps, queuedEvents)
            .resultOrPartial {error -> Ritualis.LOGGER.warn("Could not encode event queue for multi event listener: $error")}
            .ifPresent { encoded -> nbt.put(eventsNbtKey, encoded) }
        val subListenerData = nbt.getCompound(subListenerDataKey)
        subEventListeners.forEachIndexed { index, queuedEventListener ->
            val specificSubListenerData = subListenerData.getCompound("$index")
            queuedEventListener.writeNbt(specificSubListenerData, registries)
        }
    }

    companion object {
        private val CODEC = QueuedEvent.CODEC.codec().listOf()
    }

    private data class QueuedEvent(
        val delay: Int,
        val event: RegistryEntry<GameEvent>,
        val emitterPos: Vec3d,
        val listenerIndex: Int
    ) {
        companion object {
            val CODEC = RecordCodecBuilder.mapCodec { instance: RecordCodecBuilder.Instance<QueuedEvent> ->
                instance.group(
                    Codecs.NON_NEGATIVE_INT.fieldOf("ticks_till_arrival").forGetter(QueuedEvent::delay),
                    GameEvent.CODEC.fieldOf("event").forGetter(QueuedEvent::event),
                    Vec3d.CODEC.fieldOf("emitter_pos").forGetter(QueuedEvent::emitterPos),
                    Codecs.NON_NEGATIVE_INT.fieldOf("listener_index").forGetter(QueuedEvent::listenerIndex)
                ).apply(instance, ::QueuedEvent)
            }
        }
    }
}