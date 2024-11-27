package grauly.ritualis.client.particle

import grauly.ritualis.particle.IgnitionParticleEffect
import net.minecraft.client.particle.Particle
import net.minecraft.client.particle.ParticleFactory
import net.minecraft.client.particle.SpriteProvider
import net.minecraft.client.particle.VibrationParticle
import net.minecraft.client.world.ClientWorld
import net.minecraft.world.event.PositionSource

class IgnitionParticle(world: ClientWorld?, x: Double, y: Double, z: Double, vibration: PositionSource?, maxAge: Int) :
    VibrationParticle(world, x, y, z, vibration, maxAge) {

    class IgnitionParticleFactory(private val spriteProvider: SpriteProvider) : ParticleFactory<IgnitionParticleEffect> {
        override fun createParticle(
            parameters: IgnitionParticleEffect,
            world: ClientWorld,
            x: Double,
            y: Double,
            z: Double,
            velocityX: Double,
            velocityY: Double,
            velocityZ: Double
        ): Particle {
            val ignitionParticle = IgnitionParticle(world, x, y, z, parameters.vibration, parameters.arrivalInTicks)
            ignitionParticle.setSprite(spriteProvider)
            ignitionParticle.setAlpha(1f)
            return ignitionParticle
        }
    }
}