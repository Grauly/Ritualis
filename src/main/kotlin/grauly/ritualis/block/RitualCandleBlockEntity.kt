package grauly.ritualis.block

import grauly.ritualis.ModBlockEntities
import grauly.ritualis.ModEvents
import grauly.ritualis.Ritualis
import net.minecraft.block.BlockState
import net.minecraft.block.CandleBlock
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtOps
import net.minecraft.particle.DustParticleEffect
import net.minecraft.particle.ParticleEffect
import net.minecraft.particle.ParticleTypes
import net.minecraft.registry.RegistryWrapper
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.event.listener.GameEventListener

class RitualCandleBlockEntity(
    private val pos: BlockPos,
    private val state: BlockState
) : BlockEntity(ModBlockEntities.RITUAL_CANDLE_ENTITY, pos, state),
    GameEventListener.Holder<CandleEventListener> {
    private val listener = CandleEventListener(pos, this)
    private var dataHandler = CandleEventDataHandler()

    override fun getEventListener(): CandleEventListener = listener

    fun getCurrentState(): BlockState? = world?.getBlockState(pos)

    fun processEvent(event: CandleEventDataHandler.CandleEventData) {
        if (world !is ServerWorld) return
        val receivedEvent = event.event
        if (receivedEvent == ModEvents.CANDLE_IGNITE && !state.get(CandleBlock.LIT)) {
            doIgnite(event.source, state)
            return
        }
        if (receivedEvent == ModEvents.CANDLE_EXTINGUISH && state.get(CandleBlock.LIT)) {
            doExtinguish(event.source, state)
            return
        }
    }

    fun queueEvent(event: CandleEventDataHandler.CandleEventData) {
        dataHandler.queueEvent(event)
        if(world !is ServerWorld) return
        val serverWorld = world as ServerWorld
        serverWorld.scheduleBlockTick(pos, state.block, event.ticksTillArrival)
        Ritualis.LOGGER.info("enqueued ${event.event} from ${event.source} for ${event.ticksTillArrival} ticks later")
    }

    fun scheduledTick() {
        val event = dataHandler.popEvent()
        Ritualis.LOGGER.info("popping $event")
        processEvent(event)
    }

    private fun doExtinguish(emitterPos: Vec3d, state: BlockState) {
        CandleBlock.extinguish(null, state, world, pos)
        world?.markDirty(pos)
        particleLine(emitterPos, pos.toCenterPos(), DustParticleEffect(0, .5f), 10)
    }

    private fun doIgnite(emitterPos: Vec3d, state: BlockState) {
        world?.setBlockState(pos, state.with(CandleBlock.LIT, true))
        world?.markDirty(pos)
        particleLine(emitterPos, pos.toCenterPos(), ParticleTypes.FLAME)
    }

    private fun particleLine(from: Vec3d, to: Vec3d, particle: ParticleEffect, resolution: Int = 2) {
        if (world !is ServerWorld) return
        val serverWorld = (world as ServerWorld)
        val deltaVector = to.subtract(from)
        val length = deltaVector.length()
        val pointNum = (length * resolution).toInt()
        for (i in 0..pointNum) {
            val delta = i.div(pointNum.toDouble())
            val point = from.lerp(to, delta)
            serverWorld.spawnParticles(particle, point.x, point.y, point.z, 0, 0.0, 0.0, 0.0, 0.0)
        }
    }

    override fun readNbt(nbt: NbtCompound, registries: RegistryWrapper.WrapperLookup) {
        super.readNbt(nbt, registries)
        val regOps = registries.getOps(NbtOps.INSTANCE)
        if (nbt.contains(EVENT_QUEUE_KEY)) {
            CandleEventDataHandler.CODEC.parse(regOps, nbt).ifSuccess{ handler -> dataHandler = handler}
        }
    }

    override fun writeNbt(nbt: NbtCompound, registries: RegistryWrapper.WrapperLookup) {
        super.writeNbt(nbt, registries)
        val regOps = registries.getOps(NbtOps.INSTANCE)
        CandleEventDataHandler.CODEC.encodeStart(regOps, dataHandler).ifSuccess { encoded -> nbt.put(EVENT_QUEUE_KEY, encoded)}
    }

    companion object {
        private const val EVENT_QUEUE_KEY: String = "events"
    }
}