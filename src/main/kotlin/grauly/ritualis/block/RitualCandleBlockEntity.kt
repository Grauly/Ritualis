package grauly.ritualis.block

import grauly.ritualis.ModBlockEntities
import grauly.ritualis.ModEvents
import grauly.ritualis.Ritualis
import net.minecraft.block.BlockState
import net.minecraft.block.CandleBlock
import net.minecraft.block.entity.BlockEntity
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

class RitualCandleBlockEntity(
    private val pos: BlockPos,
    private val state: BlockState
) : BlockEntity(ModBlockEntities.RITUAL_CANDLE_ENTITY, pos, state),
    GameEventListener.Holder<RitualCandleBlockEntity.CandleEventListener> {


    override fun getEventListener(): CandleEventListener = CandleEventListener(pos, state)


    class CandleEventListener(
        private val pos: BlockPos,
        private val state: BlockState
    ) : GameEventListener {

        override fun getPositionSource(): PositionSource = BlockPositionSource(pos)

        override fun getRange(): Int = state.get(CandleBlock.CANDLES) * 4

        override fun listen(
            world: ServerWorld,
            event: RegistryEntry<GameEvent>,
            emitter: GameEvent.Emitter,
            emitterPos: Vec3d
        ): Boolean {
            if (event == ModEvents.CANDLE_IGNITE && !state.get(CandleBlock.LIT)) {
                doIgnite(emitterPos, world)
                return true
            }
            if (event == ModEvents.CANDLE_EXTINGUISH && state.get(CandleBlock.LIT)) {
                doExtinguish(emitterPos, world)
                return true
            }
            return false
        }

        private fun doExtinguish(emitterPos: Vec3d, world: ServerWorld) {
            CandleBlock.extinguish(null, state, world, pos)
            world.markDirty(pos)
            particleLine(world, emitterPos, pos.toCenterPos(), DustParticleEffect(0, .1f))
        }

        private fun doIgnite(emitterPos: Vec3d, world: ServerWorld) {
            world.setBlockState(pos, state.with(CandleBlock.LIT, true))
            world.markDirty(pos)
            particleLine(world, emitterPos, pos.toCenterPos(), ParticleTypes.FLAME)
        }

        private fun particleLine(world: ServerWorld, from: Vec3d, to: Vec3d, particle: ParticleEffect) {
            val resolution = 10
            val deltaVector = from.subtract(to)
            val length = deltaVector.length()
            val step = deltaVector.multiply(resolution / length)
            for (i in 0..(length * resolution).toInt()) {
                val point = from.add(step.multiply(i.toDouble()))
                world.addParticle(particle, point.x, point.y, point.z, 0.0, 0.0, 0.0)
            }
        }
    }
}