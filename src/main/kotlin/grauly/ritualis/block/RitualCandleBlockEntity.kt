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

    fun processEvent(event: CandleEventDataHandler.CandleEventData) {
        if (world !is ServerWorld) return
        val serverWorld = world as ServerWorld
        val localState: BlockState = serverWorld.getBlockState(pos)!!
        val receivedEvent = event.event
        val lit = localState.get(CandleBlock.LIT)
        if (receivedEvent.matchesKey(ModEvents.CANDLE_IGNITE.registryKey()) && !lit) {
            doIgnite(event.source, localState, serverWorld)
            return
        }
        if (receivedEvent.matchesKey(ModEvents.CANDLE_EXTINGUISH.registryKey()) && lit) {
            doExtinguish(event.source, localState, serverWorld)
            return
        }
        serverWorld.markDirty(pos)
    }

    fun queueEvent(event: CandleEventDataHandler.CandleEventData) {
        if (world !is ServerWorld) return
        val serverWorld = world as ServerWorld
        dataHandler.queueEvent(event)
        serverWorld.markDirty(pos)
    }

    private fun doExtinguish(emitterPos: Vec3d, state: BlockState, serverWorld: ServerWorld) {
        CandleBlock.extinguish(null, state, serverWorld, pos)
        serverWorld.markDirty(pos)
        particleLine(emitterPos, pos.toCenterPos(), DustParticleEffect(0, .5f), serverWorld, 10)
    }

    private fun doIgnite(emitterPos: Vec3d, state: BlockState, serverWorld: ServerWorld) {
        serverWorld.setBlockState(pos, state.with(CandleBlock.LIT, true))
        serverWorld.markDirty(pos)
        particleLine(emitterPos, pos.toCenterPos(), ParticleTypes.FLAME, serverWorld)
    }

    private fun particleLine(
        from: Vec3d,
        to: Vec3d,
        particle: ParticleEffect,
        serverWorld: ServerWorld,
        resolution: Int = 2
    ) {
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