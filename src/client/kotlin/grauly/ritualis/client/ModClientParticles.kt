package grauly.ritualis.client

import grauly.ritualis.ModParticles
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry
import grauly.ritualis.client.particle.IgnitionParticle.IgnitionParticleFactory
import grauly.ritualis.client.particle.ExtinguishParticle.ExtinguishParticleFactory

object ModClientParticles {
    fun init() {
        ParticleFactoryRegistry.getInstance().register(ModParticles.IGNITION, ::IgnitionParticleFactory)
        ParticleFactoryRegistry.getInstance().register(ModParticles.EXTINGUISH, ::ExtinguishParticleFactory)
    }
}