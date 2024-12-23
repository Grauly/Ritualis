package grauly.ritualis.event.impl

import grauly.ritualis.ModEvents
import grauly.ritualis.block.RitualCandleBlockEntity
import grauly.ritualis.extensions.spawnDirectionalParticle
import grauly.ritualis.particle.IgnitionParticleEffect
import net.minecraft.block.CandleBlock
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.event.GameEvent

class CandleIgniteListener(
    pos: BlockPos,
    candleBlockEntity: RitualCandleBlockEntity
) : CandleEventListener(pos, candleBlockEntity) {

    override fun onEventQueued(world: ServerWorld, emitterPosition: Vec3d, delay: Int) {
        world.spawnDirectionalParticle(
            IgnitionParticleEffect(pos.toCenterPos(), delay),
            emitterPosition
        )
    }

    override fun isEventListenable(world: ServerWorld, emitterPosition: Vec3d, emitter: GameEvent.Emitter): Boolean {
        if(candleBlockEntity.cachedState.get(CandleBlock.LIT)) return false
        return super.isEventListenable(world, emitterPosition, emitter)
    }

    override fun onEventReceived(originalSource: Vec3d) {
        candleBlockEntity.doIgnite(candleBlockEntity.cachedState, candleBlockEntity.world as ServerWorld)
    }

    override fun accepts(incomingEvent: RegistryEntry<GameEvent>): Boolean =
        incomingEvent.matchesKey(ModEvents.CANDLE_IGNITE.registryKey())
}