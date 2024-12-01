package grauly.ritualis.block

import grauly.ritualis.ModBlockEntities
import grauly.ritualis.ModEvents
import grauly.ritualis.particle.ExtinguishParticleEffect
import grauly.ritualis.particle.IgnitionParticleEffect
import net.minecraft.block.BlockState
import net.minecraft.block.CandleBlock
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtOps
import net.minecraft.particle.DustParticleEffect
import net.minecraft.particle.ParticleEffect
import net.minecraft.particle.ParticleTypes
import net.minecraft.particle.VibrationParticleEffect
import net.minecraft.registry.RegistryWrapper
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.event.BlockPositionSource
import net.minecraft.world.event.listener.GameEventListener

class RitualCandleBlockEntity(
    pos: BlockPos,
    state: BlockState
) : BlockEntity(ModBlockEntities.RITUAL_CANDLE_ENTITY, pos, state),
    GameEventListener.Holder<CandleEventListener> {
    private val listener = CandleEventListener(pos, this)
    private var dataHandler = CandleEventDataHandler()

    override fun getEventListener(): CandleEventListener = listener

    fun tick() {
        dataHandler.tick(::processEvent)
    }

    private fun processEvent(event: CandleEventDataHandler.CandleEventData) {
        if (world !is ServerWorld) return
        val serverWorld = world as ServerWorld
        val localState: BlockState = serverWorld.getBlockState(pos)!!
        val receivedEvent = event.event
        val lit = localState.get(CandleBlock.LIT)
        if (receivedEvent.matchesKey(ModEvents.CANDLE_IGNITE.registryKey()) && !lit) {
            doIgnite(localState, serverWorld)
            return
        }
        if (receivedEvent.matchesKey(ModEvents.CANDLE_EXTINGUISH.registryKey()) && lit) {
            doExtinguish(localState, serverWorld)
            return
        }
        serverWorld.markDirty(pos)
    }

    fun queueEvent(event: CandleEventDataHandler.CandleEventData) {
        if (world !is ServerWorld) return
        val serverWorld = world as ServerWorld
        val localState: BlockState = serverWorld.getBlockState(pos)!!
        if (dataHandler.isOnCooldown()) return
        if (dataHandler.isEventRedundant(event)) return
        if (event.event.matchesKey(ModEvents.CANDLE_IGNITE.registryKey()) && CandleBlock.isLitCandle(localState)) return
        if (event.event.matchesKey(ModEvents.CANDLE_EXTINGUISH.registryKey()) && !CandleBlock.isLitCandle(localState)) return
        dataHandler.queueEvent(event)
        serverWorld.markDirty(pos)
        spawnParticle(serverWorld, event)
    }

    private fun spawnParticle(serverWorld: ServerWorld, event: CandleEventDataHandler.CandleEventData) {
        val particleEffect = if (event.event.matchesKey(ModEvents.CANDLE_IGNITE.registryKey())) {
            IgnitionParticleEffect(pos.toCenterPos(), event.ticksTillArrival.toInt())
        } else {
            ExtinguishParticleEffect(pos.toCenterPos(), event.ticksTillArrival.toInt())
        }
        serverWorld.spawnParticles(
            particleEffect,
            event.source.x,
            event.source.y,
            event.source.z,
            1,
            0.0,
            0.0,
            0.0,
            0.1
        )
    }

    private fun doExtinguish(state: BlockState, serverWorld: ServerWorld) {
        CandleBlock.extinguish(null, state, serverWorld, pos)
        serverWorld.markDirty(pos)
    }

    private fun doIgnite(state: BlockState, serverWorld: ServerWorld) {
        serverWorld.setBlockState(pos, state.with(CandleBlock.LIT, true))
        serverWorld.markDirty(pos)
    }

    override fun readNbt(nbt: NbtCompound, registries: RegistryWrapper.WrapperLookup) {
        super.readNbt(nbt, registries)
        val regOps = registries.getOps(NbtOps.INSTANCE)
        if (nbt.contains(EVENT_QUEUE_KEY)) {
            CandleEventDataHandler.CODEC.parse(regOps, nbt).ifSuccess { handler -> dataHandler = handler }
        }
    }

    override fun writeNbt(nbt: NbtCompound, registries: RegistryWrapper.WrapperLookup) {
        super.writeNbt(nbt, registries)
        val regOps = registries.getOps(NbtOps.INSTANCE)
        CandleEventDataHandler.CODEC.encodeStart(regOps, dataHandler)
            .ifSuccess { encoded -> nbt.put(EVENT_QUEUE_KEY, encoded) }
    }

    companion object {
        private const val EVENT_QUEUE_KEY: String = "events"
    }
}