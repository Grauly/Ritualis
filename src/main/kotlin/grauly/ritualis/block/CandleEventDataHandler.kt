package grauly.ritualis.block

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import grauly.ritualis.Ritualis
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d
import net.minecraft.world.event.GameEvent

class CandleEventDataHandler(
    private val events: MutableList<CandleEventData> = mutableListOf(),
    private var cooldown: Long = 0L
) {
    private var lastAccessedTimestamp = -1L

    fun queueEvent(eventData: CandleEventData, serverWorld: ServerWorld) {
        handleTimeStamp(serverWorld)
        events.add(eventData)
        events.sortWith(compareBy { e: CandleEventData -> e.ticksTillArrival })
        Ritualis.LOGGER.info("queued $eventData, cooldown at: $cooldown now have \n$events")
        //TODO spawn particle and such
    }

    fun actEvents(eventHandler: (CandleEventData) -> Unit, serverWorld: ServerWorld) {
        handleTimeStamp(serverWorld)
        val pops = events.filter { e -> e.ticksTillArrival <= 0 }
        Ritualis.LOGGER.info("found ${pops.size} events to process")
        if (cooldown <= 0) {
            Ritualis.LOGGER.info("acting on first: ${pops.first()}")
            pops.first().apply(eventHandler)
            cooldown = COOLDOWN_TIME_TICKS
        } else {
            Ritualis.LOGGER.info("disregarding, due to cooldown: $cooldown")
        }
        events.removeAll(pops)
    }

    private fun handleTimeStamp(serverWorld: ServerWorld) {
        if (lastAccessedTimestamp == -1L) lastAccessedTimestamp = serverWorld.time
        val deltaTime = serverWorld.time - lastAccessedTimestamp
        events.forEach { e -> e.ticksTillArrival -= deltaTime }
        lastAccessedTimestamp = serverWorld.time
        cooldown -= deltaTime
    }

    companion object {
        val CODEC: Codec<CandleEventDataHandler> =
            RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<CandleEventDataHandler> ->
                instance.group(
                    CandleEventData.CODEC.listOf().fieldOf("events").forGetter(CandleEventDataHandler::events),
                    Codec.LONG.fieldOf("cooldown").forGetter(CandleEventDataHandler::cooldown)
                ).apply(instance, ::CandleEventDataHandler)
            }

        const val COOLDOWN_TIME_TICKS = 20L
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
