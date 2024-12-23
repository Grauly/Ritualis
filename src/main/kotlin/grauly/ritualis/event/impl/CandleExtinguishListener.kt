package grauly.ritualis.event.impl

import grauly.ritualis.ModEvents
import grauly.ritualis.block.RitualCandleBlockEntity
import grauly.ritualis.extensions.spawnDirectionalParticle
import grauly.ritualis.particle.ExtinguishParticleEffect
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.event.GameEvent

class CandleExtinguishListener(
    pos: BlockPos,
    candleBlockEntity: RitualCandleBlockEntity
) : CandleEventListener(pos, candleBlockEntity) {

    override fun onEventQueued(world: ServerWorld, emitterPosition: Vec3d, delay: Int) {
        world.spawnDirectionalParticle(
            ExtinguishParticleEffect(pos.toCenterPos(), delay),
            emitterPosition
        )
    }

    override fun onEventReceived(originalSource: Vec3d) {
        candleBlockEntity.doExtinguish(candleBlockEntity.cachedState, candleBlockEntity.world as ServerWorld)
    }

    override fun accepts(incomingEvent: RegistryEntry<GameEvent>): Boolean =
        incomingEvent.matchesKey(ModEvents.CANDLE_EXTINGUISH.registryKey())
}