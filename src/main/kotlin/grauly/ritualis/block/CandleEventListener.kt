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
import kotlin.math.roundToInt
import kotlin.math.roundToLong

class CandleEventListener(
    private val pos: BlockPos,
    private val candle: RitualCandleBlockEntity
) : GameEventListener {

    override fun getPositionSource(): PositionSource = BlockPositionSource(pos)

    override fun getRange(): Int = 16

    override fun getTriggerOrder(): GameEventListener.TriggerOrder = GameEventListener.TriggerOrder.BY_DISTANCE

    override fun listen(
        world: ServerWorld,
        event: RegistryEntry<GameEvent>,
        emitter: GameEvent.Emitter,
        emitterPos: Vec3d
    ): Boolean {
        if (!(event.matchesKey(ModEvents.CANDLE_IGNITE.registryKey()) || event.matchesKey(ModEvents.CANDLE_EXTINGUISH.registryKey()))) return false
        if (emitterPos == pos.toCenterPos()) return false
        val dist = pos.toCenterPos().subtract(emitterPos).length()
        val delay = dist.roundToLong()
        val eventData = CandleEventDataHandler.CandleEventData(event, emitterPos, delay)
        candle.queueEvent(eventData)
        return true
    }
}