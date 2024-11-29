package grauly.ritualis.client

import grauly.ritualis.ModParticles
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry
import grauly.ritualis.client.particle.IgnitionParticle.IgnitionParticleFactory
import grauly.ritualis.client.particle.ExtinguishParticle.ExtinguishParticleFactory
import net.minecraft.client.particle.EndRodParticle

object ModClientParticles {
    fun init() {
        ParticleFactoryRegistry.getInstance().register(ModParticles.IGNITION, ::IgnitionParticleFactory)
        ParticleFactoryRegistry.getInstance().register(ModParticles.EXTINGUISH, ::ExtinguishParticleFactory)
        ParticleFactoryRegistry.getInstance().register(ModParticles.RITUAL_SPARK, EndRodParticle::Factory)
    }
}