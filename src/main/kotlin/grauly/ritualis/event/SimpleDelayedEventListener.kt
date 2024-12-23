package grauly.ritualis.event

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import grauly.ritualis.Ritualis
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtOps
import net.minecraft.registry.RegistryWrapper
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.dynamic.Codecs
import net.minecraft.util.math.Vec3d
import net.minecraft.world.event.GameEvent.Emitter
import kotlin.math.max

abstract class SimpleDelayedEventListener(
    private val shouldTrackCooldown: Boolean = true
) : TypedEventListener {

    private val eventQueue: MutableList<PositionEventReference> = mutableListOf()
    private var cooldown: Int = 0

    protected abstract fun isEventListenable(world: ServerWorld, emitterPosition: Vec3d, emitter: Emitter): Boolean
    protected abstract fun isEventFromSelf(world: ServerWorld, emitterPosition: Vec3d, emitter: Emitter): Boolean
    protected open fun getPropagationTicksPerBlock(): Int = 1
    protected abstract fun calculatePropagationDelay(emitterPosition: Vec3d): Int

    override fun trigger(world: ServerWorld, emitterPosition: Vec3d, emitter: Emitter): Boolean {
        if (isEventFromSelf(world, emitterPosition, emitter)) return false
        if (!isEventListenable(world, emitterPosition, emitter)) return false
        if (isOnCooldown()) return false

        val delay = calculatePropagationDelay(emitterPosition)
        val isEventNew = eventQueue.none { e -> e.delay < delay }
        if (!isEventNew) return false

        cooldown += getCooldownTime()
        eventQueue.add(PositionEventReference(emitterPosition, delay))
        onEventQueued(world, emitterPosition, delay)
        return true
    }

    protected abstract fun onEventQueued(world: ServerWorld, emitterPosition: Vec3d, delay: Int)

    protected abstract fun onEventReceived(originalSource: Vec3d)

    override fun isOnCooldown(): Boolean = shouldTrackCooldown && cooldown != 0

    protected abstract fun getCooldownTime(): Int

    override fun tick() {
        if (shouldTrackCooldown) cooldown = max(0, cooldown - 1)
        eventQueue.forEach { event -> event.delay = max(0, event.delay - 1) }
        val events = eventQueue.filter { event -> event.delay == 0 }
        if (events.isEmpty()) return
        onEventReceived(events.last().triggerPosition)
    }

    override fun writeNbt(nbt: NbtCompound, registries: RegistryWrapper.WrapperLookup) {
        val regOps = registries.getOps(NbtOps.INSTANCE)
        PositionEventReference.LIST_CODEC.encodeStart(regOps, eventQueue)
            .resultOrPartial { error -> Ritualis.LOGGER.warn("Could not encode event data: {}", error) }
            .ifPresent { encoded -> nbt.put(EVENTS_KEY, encoded) }
        Codecs.NON_NEGATIVE_INT.encodeStart(regOps, cooldown)
            .resultOrPartial { error -> Ritualis.LOGGER.warn("Could not encode cooldown: {}", error) }
            .ifPresent { encoded -> nbt.put(COOLDOWN_KEY, encoded) }
    }

    override fun readNbt(nbt: NbtCompound, registries: RegistryWrapper.WrapperLookup) {
        val regOps = registries.getOps(NbtOps.INSTANCE)
        PositionEventReference.LIST_CODEC.parse(regOps, nbt.get(EVENTS_KEY))
            .resultOrPartial { error -> Ritualis.LOGGER.warn("Could not decode event data: {}", error) }
            .ifPresent { decoded -> eventQueue.clear(); eventQueue.addAll(decoded) }
        Codecs.NON_NEGATIVE_INT.parse(regOps, nbt.get(COOLDOWN_KEY))
            .resultOrPartial { error -> Ritualis.LOGGER.warn("Could not decode cooldown: {}", error) }
            .ifPresent { decoded -> cooldown = decoded }
    }

    companion object {
        private const val EVENTS_KEY: String = "events"
        private const val COOLDOWN_KEY: String = "cooldown"
    }

    private data class PositionEventReference(val triggerPosition: Vec3d, var delay: Int) {
        companion object {
            val CODEC: Codec<PositionEventReference> =
                RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<PositionEventReference> ->
                    instance.group(
                        Vec3d.CODEC.fieldOf("position").forGetter(PositionEventReference::triggerPosition),
                        Codecs.NON_NEGATIVE_INT.fieldOf("delay").forGetter(PositionEventReference::delay)
                    ).apply(instance, ::PositionEventReference)
                }

            val LIST_CODEC: Codec<MutableList<PositionEventReference>> = CODEC.listOf()
        }
    }
}