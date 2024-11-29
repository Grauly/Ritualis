package grauly.ritualis

import grauly.ritualis.particle.ExtinguishParticleEffect
import grauly.ritualis.particle.IgnitionParticleEffect
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes
import net.minecraft.particle.ParticleEffect
import net.minecraft.particle.ParticleType
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

object ModParticles {
    val IGNITION = register(FabricParticleTypes.complex(IgnitionParticleEffect.CODEC, IgnitionParticleEffect.PACKET_CODEC), "ignition")
    val EXTINGUISH = register(FabricParticleTypes.complex(ExtinguishParticleEffect.CODEC, ExtinguishParticleEffect.PACKET_CODEC), "extinguish")
    val RITUAL_SPARK = register(FabricParticleTypes.simple(), "ritual_spark")

    private fun <T: ParticleEffect>register(particleType: ParticleType<T>, id: String): ParticleType<T> =
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of(Ritualis.MODID, id), particleType)

    fun init() {}
}