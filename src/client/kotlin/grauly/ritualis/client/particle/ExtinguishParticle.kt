package grauly.ritualis.client.particle

import grauly.ritualis.particle.ExtinguishParticleEffect
import net.minecraft.client.particle.*
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d

class ExtinguishParticle(
    world: ClientWorld?,
    x: Double,
    y: Double,
    z: Double,
    private val target: Vec3d,
    maxAge: Int
) :
    SpriteBillboardParticle(world, x, y, z, 0.0, 0.0, 0.0) {

    init {
        scale(1.5f)
        this.maxAge = maxAge
    }

    override fun getType(): ParticleTextureSheet = ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT

    override fun tick() {
        prevPosX = x
        prevPosY = y
        prevPosZ = z
        if (age++ >= maxAge) {
            markDead()
            return
        }
        val remainingTime = maxAge - age
        val durationFraction: Double = 1.0 / remainingTime
        x = MathHelper.lerp(durationFraction, x, target.x)
        y = MathHelper.lerp(durationFraction, y, target.y)
        z = MathHelper.lerp(durationFraction, z, target.z)
    }

    class ExtinguishParticleFactory(private val spriteProvider: SpriteProvider) :
        ParticleFactory<ExtinguishParticleEffect> {
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
            val extinguishParticle = ExtinguishParticle(world, x, y, z, parameters.target, parameters.arrivalInTicks)
            extinguishParticle.setSprite(spriteProvider)
            extinguishParticle.setAlpha(1f)
            return extinguishParticle
        }
    }
}