package grauly.ritualis.extensions

import net.minecraft.particle.ParticleEffect
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d

fun ServerWorld.spawnDirectionalParticle(
    particle: ParticleEffect,
    position: Vec3d,
    direction: Vec3d = Vec3d(0.0, 0.0, 0.0),
    speed: Double = 1.0,
    force: Boolean = false,
    important: Boolean = false
) {
    spawnParticles(
        particle,
        force,
        important,
        position.x,
        position.y,
        position.z,
        0,
        direction.x,
        direction.y,
        direction.z,
        speed
    )
}

fun ServerWorld.spawnAreaParticle(
    particle: ParticleEffect,
    position: Vec3d,
    deltaBox: Vec3d,
    amount: Int = 1,
    speed: Double = 1.0,
    force: Boolean = false,
    important: Boolean = false
) {
    spawnParticles(
        particle,
        force,
        important,
        position.x,
        position.y,
        position.z,
        amount,
        deltaBox.x,
        deltaBox.y,
        deltaBox.z,
        speed
    )
}