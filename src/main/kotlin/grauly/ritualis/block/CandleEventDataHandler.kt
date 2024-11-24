package grauly.ritualis.block

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.util.math.Vec3d
import net.minecraft.world.event.GameEvent

class CandleEventDataHandler(
    private val events: MutableList<CandleEventData> = mutableListOf(),
    private var cooldown: Long = 0L
) {

    fun queueEvent(eventData: CandleEventData) {
        events.add(eventData)
        events.sortWith(compareBy { e: CandleEventData -> e.ticksTillArrival })
    }

    private fun actEvents(eventHandler: (CandleEventData) -> Unit) {
        val pops = events.filter { e -> e.ticksTillArrival <= 0 }
        if (pops.isEmpty()) return
        if (!isOnCooldown()) {
            pops.first().apply(eventHandler)
            cooldown = COOLDOWN_TIME_TICKS
        }
        events.removeAll(pops)
    }

    fun tick(eventHandler: (CandleEventData) -> Unit) {
        events.forEach { e -> e.ticksTillArrival -= 1 }
        cooldown -= 1
        actEvents(eventHandler)
    }

    fun isOnCooldown(): Boolean = cooldown > 0

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
