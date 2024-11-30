package grauly.ritualis.client.particle

import grauly.ritualis.particle.IgnitionParticleEffect
import net.minecraft.client.particle.*
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d

class IgnitionParticle(world: ClientWorld?, x: Double, y: Double, z: Double, private val target: Vec3d, maxAge: Int) :
    SpriteBillboardParticle(world, x, y, z, 0.0, 0.0, 0.0) {

    init {
        this.maxAge = maxAge
    }

    override fun tick() {
        prevPosX = x
        prevPosY = y
        prevPosZ = z
        if (age++ >= maxAge) {
            markDead()
            return
        }
        val remainingTime = maxAge - age
        val durationFraction: Double = 1.0.div(remainingTime)
        x = MathHelper.lerp(durationFraction, x, target.x)
        y = MathHelper.lerp(durationFraction, y, target.y)
        z = MathHelper.lerp(durationFraction, z, target.z)
    }

    override fun getType(): ParticleTextureSheet = ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT

    class IgnitionParticleFactory(private val spriteProvider: SpriteProvider) :
        ParticleFactory<IgnitionParticleEffect> {
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
            val ignitionParticle = IgnitionParticle(world, x, y, z, parameters.target, parameters.arrivalInTicks)
            ignitionParticle.setSprite(spriteProvider)
            ignitionParticle.setAlpha(1f)
            return ignitionParticle
        }
    }
}