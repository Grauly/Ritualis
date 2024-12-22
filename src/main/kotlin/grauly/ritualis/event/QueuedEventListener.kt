package grauly.ritualis.event

import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d
import net.minecraft.world.event.GameEvent.Emitter

interface QueuedEventListener : TypedEventListener {
    fun shouldQueue(serverWorld: ServerWorld, emitterPos: Vec3d, emitter: Emitter): Boolean
    fun calculateDelay(serverWorld: ServerWorld, emitterPos: Vec3d, emitter: Emitter): Int
    fun reportQueued(serverWorld: ServerWorld, emitterPos: Vec3d, emitter: Emitter)
}