package grauly.ritualis.event

import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d
import net.minecraft.world.event.GameEvent
import net.minecraft.world.event.GameEvent.Emitter

interface TypedEventListener {
    fun accepts(incomingEvent: RegistryEntry<GameEvent>): Boolean
    fun getRange(): Int
    fun trigger(world: ServerWorld, emitterPosition: Vec3d, emitter: Emitter): Boolean
    fun isOnCooldown(): Boolean
    fun tick()
    fun writeNbt(nbt: NbtCompound, registries: RegistryWrapper.WrapperLookup)
    fun readNbt(nbt: NbtCompound, registries: RegistryWrapper.WrapperLookup)
}