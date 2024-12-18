package grauly.ritualis.block

import grauly.ritualis.ModBlockEntities
import grauly.ritualis.ModEvents
import grauly.ritualis.Ritualis
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
        spawnParticle(serverWorld, particleEffect, event.source, 1, speed = 0.1)
    }

    private fun playSound(serverWorld: ServerWorld, event: SoundEvent, volume: Float = 1f, pitch: Float = 1f) {
        val worldPos = pos.toCenterPos()
        serverWorld.playSound(null, worldPos.x, worldPos.y, worldPos.z, event, SoundCategory.BLOCKS, volume, pitch, serverWorld.random.nextLong())
    }

    private fun doExtinguish(state: BlockState, serverWorld: ServerWorld) {
        CandleBlock.extinguish(null, state, serverWorld, pos)
        spawnParticle(serverWorld, ParticleTypes.DUST_PLUME, pos.toCenterPos(), amount = 3)
        playSound(serverWorld, SoundEvents.BLOCK_CANDLE_EXTINGUISH, volume = 2f)
        serverWorld.markDirty(pos)
    }

    private fun doIgnite(state: BlockState, serverWorld: ServerWorld) {
        serverWorld.setBlockState(pos, state.with(CandleBlock.LIT, true))
        playSound(serverWorld, SoundEvents.ENTITY_BLAZE_SHOOT, .3f, 1.7f)
        for (i in 0..5) {
            spawnParticle(
                serverWorld,
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
        val regOps = registries.getOps(NbtOps.INSTANCE)
        if (nbt.contains(EVENT_QUEUE_KEY)) {
            CandleEventDataHandler.CODEC.parse(regOps, nbt.get(EVENT_QUEUE_KEY))
                .resultOrPartial { error -> Ritualis.LOGGER.info("Failed to deserialize event data at $pos, with error: $error") }
                .ifPresent { handler -> dataHandler = handler }
        }
    }

    override fun writeNbt(nbt: NbtCompound, registries: RegistryWrapper.WrapperLookup) {
        super.writeNbt(nbt, registries)
        val regOps = registries.getOps(NbtOps.INSTANCE)
        CandleEventDataHandler.CODEC.encodeStart(regOps, dataHandler)
            .resultOrPartial { error -> Ritualis.LOGGER.info("Failed to encode event data at $pos, with error: $error") }
            .ifPresent { encoded -> nbt.put(EVENT_QUEUE_KEY, encoded) }
    }

    private fun spawnParticle(
        serverWorld: ServerWorld,
        particle: ParticleEffect,
        location: Vec3d,
        amount: Int = 1,
        direction: Vec3d = Vec3d(0.0, 0.0, 0.0),
        speed: Double = 1.0
    ) {
        for (i in 1..amount) {
            serverWorld.spawnParticles(
                particle,
                location.x,
                location.y,
                location.z,
                0,
                direction.x,
                direction.y,
                direction.z,
                speed
            )
        }
    }

    companion object {
        private const val EVENT_QUEUE_KEY: String = "events"
    }
}