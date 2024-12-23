package grauly.ritualis.block

import grauly.ritualis.ModBlockEntities
import grauly.ritualis.ModEvents
import grauly.ritualis.Ritualis
import grauly.ritualis.event.MultiEventListener
import grauly.ritualis.event.impl.CandleExtinguishListener
import grauly.ritualis.event.impl.CandleIgniteListener
import grauly.ritualis.extensions.spawnDirectionalParticle
import grauly.ritualis.particle.ExtinguishParticleEffect
import grauly.ritualis.particle.IgnitionParticleEffect
import net.minecraft.block.BlockState
import net.minecraft.block.CandleBlock
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtOps
import net.minecraft.particle.ParticleEffect
import net.minecraft.particle.ParticleTypes
import net.minecraft.registry.RegistryWrapper
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.event.listener.GameEventListener

class RitualCandleBlockEntity(
    pos: BlockPos,
    state: BlockState
) : BlockEntity(ModBlockEntities.RITUAL_CANDLE_ENTITY, pos, state),
    GameEventListener.Holder<MultiEventListener> {

    private val listener = MultiEventListener(pos, shouldTrackCooldown = true, subEventListeners = listOf(
        CandleExtinguishListener(pos, this),
        CandleIgniteListener(pos, this)
    ))

    override fun getEventListener(): MultiEventListener = listener

    fun tick() {
        listener.tick()
    }

    private fun playSound(serverWorld: ServerWorld, event: SoundEvent, volume: Float = 1f, pitch: Float = 1f) {
        val worldPos = pos.toCenterPos()
        serverWorld.playSound(null, worldPos.x, worldPos.y, worldPos.z, event, SoundCategory.BLOCKS, volume, pitch, serverWorld.random.nextLong())
    }

    fun doExtinguish(state: BlockState, serverWorld: ServerWorld) {
        CandleBlock.extinguish(null, state, serverWorld, pos)
        for (i in 0..3) serverWorld.spawnDirectionalParticle(ParticleTypes.DUST_PLUME, pos.toCenterPos())
        playSound(serverWorld, SoundEvents.BLOCK_CANDLE_EXTINGUISH, volume = 2f)
        serverWorld.markDirty(pos)
    }

    fun doIgnite(state: BlockState, serverWorld: ServerWorld) {
        serverWorld.setBlockState(pos, state.with(CandleBlock.LIT, true))
        playSound(serverWorld, SoundEvents.ENTITY_BLAZE_SHOOT, .3f, 1.7f)
        for (i in 0..5) {
            serverWorld.spawnDirectionalParticle(
                ParticleTypes.FLAME,
                pos.toCenterPos(),
                direction = Vec3d(0.0, serverWorld.random.nextDouble() * 0.3, 0.0),
                speed = 0.5
            )
        }
        serverWorld.markDirty(pos)
    }

    override fun readNbt(nbt: NbtCompound, registries: RegistryWrapper.WrapperLookup) {
        super.readNbt(nbt, registries)
        listener.readNbt(nbt, registries)
    }

    override fun writeNbt(nbt: NbtCompound, registries: RegistryWrapper.WrapperLookup) {
        super.writeNbt(nbt, registries)
        listener.writeNbt(nbt, registries)
    }
}