package grauly.ritualis

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes
import net.minecraft.particle.ParticleEffect
import net.minecraft.particle.ParticleType
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

object ModParticles {
    val IGNITION = register(FabricParticleTypes.simple(), "ignition")
    val EXTINGUISH = register(FabricParticleTypes.simple(), "extinguish")

    private fun <T: ParticleEffect>register(particleType: ParticleType<T>, id: String): ParticleType<T> =
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of(Ritualis.MODID, id), particleType)

    fun init() {}
}