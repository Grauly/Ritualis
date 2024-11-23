package grauly.ritualis.block

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d
import net.minecraft.world.event.GameEvent

class CandleEventDataHandler(
    private val events: MutableList<CandleEventData> = mutableListOf()
) {
    private var lastAccessedTimestamp = -1L

    fun queueEvent(eventData: CandleEventData, serverWorld: ServerWorld) {
        handleTimeStamp(serverWorld)
        events.add(eventData)
        events.sortWith(compareBy { e: CandleEventData -> e.ticksTillArrival })
        //TODO spawn particle and such
    }

    fun actEvents(eventHandler: (CandleEventData) -> Unit, serverWorld: ServerWorld) {
        handleTimeStamp(serverWorld)
        val pops = events.filter { e -> e.ticksTillArrival <= 0 }
        pops.forEach(eventHandler)
        events.removeAll(pops)
    }

    private fun handleTimeStamp(serverWorld: ServerWorld) {
        if (lastAccessedTimestamp == -1L) lastAccessedTimestamp = serverWorld.time
        val deltaTime = serverWorld.time - lastAccessedTimestamp
        events.forEach { e -> e.ticksTillArrival -= deltaTime }
        lastAccessedTimestamp = serverWorld.time
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
