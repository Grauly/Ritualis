package grauly.ritualis

import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier
import net.minecraft.world.event.GameEvent

object ModEvents {

    val CANDLE_IGNITE: GameEvent = register("candle_ignite", 16)
    val CANDLE_EXTINGUISH: GameEvent = register("candle_extinguish", 16)

    fun init() {

    }

    private fun register(id: String, range: Int): GameEvent = Registry.register(Registries.GAME_EVENT, Identifier.of(Ritualis.MODID, id), GameEvent(range))
}