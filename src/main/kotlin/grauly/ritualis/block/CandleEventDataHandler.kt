package grauly.ritualis.block

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.util.math.Vec3d
import net.minecraft.world.event.GameEvent

class CandleEventDataHandler(
    private val events: MutableList<CandleEventData> = mutableListOf()
) {
    fun queueEvent(eventData: CandleEventData) {
        events.add(eventData)
        events.sortWith(compareBy { e: CandleEventData -> e.ticksTillArrival })
        //TODO spawn particle and such
    }

    fun popEvent(): CandleEventData {
        val first = events.first()
        events.removeFirst()
        return first
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
        val ticksTillArrival: Int
    ) {
        companion object {
            val CODEC: Codec<CandleEventData> =
                RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<CandleEventData> ->
                    instance.group(
                        GameEvent.CODEC.fieldOf("event").forGetter(CandleEventData::event),
                        Vec3d.CODEC.fieldOf("source").forGetter(CandleEventData::source),
                        Codec.intRange(0, Int.MAX_VALUE).fieldOf("ticksTillArrival")
                            .forGetter(CandleEventData::ticksTillArrival)
                    ).apply(instance, ::CandleEventData)
                }
        }
    }
}
