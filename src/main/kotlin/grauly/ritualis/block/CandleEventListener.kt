package grauly.ritualis.block

import grauly.ritualis.ModEvents
import grauly.ritualis.Ritualis
import net.minecraft.block.BlockState
import net.minecraft.block.CandleBlock
import net.minecraft.particle.DustParticleEffect
import net.minecraft.particle.ParticleEffect
import net.minecraft.particle.ParticleTypes
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.event.BlockPositionSource
import net.minecraft.world.event.GameEvent
import net.minecraft.world.event.PositionSource
import net.minecraft.world.event.listener.GameEventListener

class CandleEventListener(
    private val pos: BlockPos,
    private val candle: RitualCandleBlockEntity
) : GameEventListener {

    override fun getPositionSource(): PositionSource = BlockPositionSource(pos)

    override fun getRange(): Int {
        val state = getState() ?: return 0
        return state.get(CandleBlock.CANDLES).times(4)
    }

    override fun getTriggerOrder(): GameEventListener.TriggerOrder = GameEventListener.TriggerOrder.BY_DISTANCE

    private fun getState(): BlockState? = candle.getCurrentState()

    override fun listen(
        world: ServerWorld,
        event: RegistryEntry<GameEvent>,
        emitter: GameEvent.Emitter,
        emitterPos: Vec3d
    ): Boolean {
        if (emitterPos == pos.toCenterPos()) return false
        val state = getState() ?: return false
        val lit = state.get(CandleBlock.LIT)
        if (lit && event == ModEvents.CANDLE_IGNITE) return false
        if (!lit && event == ModEvents.CANDLE_EXTINGUISH) return false
        val eventData = CandleEventDataHandler.CandleEventData(event, emitterPos, pos.toCenterPos().subtract(emitterPos).length().toInt())
        candle.queueEvent(eventData)
        return false
    }
}