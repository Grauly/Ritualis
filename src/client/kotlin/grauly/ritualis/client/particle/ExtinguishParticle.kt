package grauly.ritualis.client.particle

import grauly.ritualis.particle.ExtinguishParticleEffect
import net.minecraft.client.particle.Particle
import net.minecraft.client.particle.ParticleFactory
import net.minecraft.client.particle.SpriteProvider
import net.minecraft.client.particle.VibrationParticle
import net.minecraft.client.world.ClientWorld
import net.minecraft.world.event.PositionSource

class ExtinguishParticle(world: ClientWorld?, x: Double, y: Double, z: Double, vibration: PositionSource?, maxAge: Int) :
    VibrationParticle(world, x, y, z, vibration, maxAge) {

    class ExtinguishParticleFactory(private val spriteProvider: SpriteProvider) : ParticleFactory<ExtinguishParticleEffect> {
        override fun createParticle(
            parameters: ExtinguishParticleEffect,
            world: ClientWorld,
            x: Double,
            y: Double,
            z: Double,
            velocityX: Double,
            velocityY: Double,
            velocityZ: Double
        ): Particle {
            val extinguishParticle = ExtinguishParticle(world, x, y, z, parameters.vibration, parameters.arrivalInTicks)
            extinguishParticle.setSprite(spriteProvider)
            extinguishParticle.setAlpha(1f)
            return extinguishParticle
        }
    }
}