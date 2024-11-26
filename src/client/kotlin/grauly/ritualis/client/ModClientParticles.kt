package grauly.ritualis.client

import grauly.ritualis.ModParticles
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry
import net.minecraft.client.particle.VibrationParticle.Factory

object ModClientParticles {
    fun init() {
        ParticleFactoryRegistry.getInstance().register(ModParticles.IGNITION, ::Factory)
        ParticleFactoryRegistry.getInstance().register(ModParticles.EXTINGUISH, ::Factory)
    }
}