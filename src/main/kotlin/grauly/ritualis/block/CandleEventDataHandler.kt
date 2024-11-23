package grauly.ritualis.block

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import grauly.ritualis.Ritualis
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.util.math.Vec3d
import net.minecraft.world.event.GameEvent

class CandleEventDataHandler(
    private val events: MutableList<CandleEventData> = mutableListOf()
) {
    fun queueEvent(eventData: CandleEventData) {
        events.add(eventData)
        events.sortWith(compareBy { e: CandleEventData -> e.ticksTillArrival })
        Ritualis.LOGGER.info("queued events: $events")
        //TODO spawn particle and such
    }

    fun canPopEvent(): Boolean = events.isNotEmpty()

    fun popEvent(): CandleEventData {
        val first = events.first()
        events.removeFirst()
        return first
    }

    fun actEvents(eventHandler: (CandleEventData) -> Unit) {
        val pops = events.filter { e -> e.ticksTillArrival <= 0 }
        pops.forEach(eventHandler)
        events.removeAll(pops)
    }

    fun applyDelta(deltaTime: Long) {
        events.forEach { e -> e.ticksTillArrival -= deltaTime}
    }

    companion object {
        val CODEC: Codec<CandleEventDataHandler> =
            RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<CandleEventDataHandler> ->
                instance.group(
                    CandleEventData.CODEC.listOf().fieldOf("events").forGetter(CandleEventDataHandler::events)
                ).apply(instance, ::CandleEventDataHandler)
            }
    }


    data class CandleEventData(
        val event: RegistryEntry<GameEvent>,
        val source: Vec3d,
        var ticksTillArrival: Long
    ) {
        override fun toString(): String = "{${event.idAsString} from $source, arrives in: $ticksTillArrival}"

        companion object {
            val CODEC: Codec<CandleEventData> =
                RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<CandleEventData> ->
                    instance.group(
                        GameEvent.CODEC.fieldOf("event").forGetter(CandleEventData::event),
                        Vec3d.CODEC.fieldOf("source").forGetter(CandleEventData::source),
                        Codec.LONG.fieldOf("ticksTillArrival").forGetter(CandleEventData::ticksTillArrival)
                    ).apply(instance, ::CandleEventData)
                }
        }
    }
}
