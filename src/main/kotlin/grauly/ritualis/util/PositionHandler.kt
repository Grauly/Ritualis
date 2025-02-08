package grauly.ritualis.util

import grauly.ritualis.easing.EasingHandler
import net.minecraft.util.math.Vec3d
import kotlin.math.pow

class PositionHandler(
    private val maxMovementPerTick: Double = 0.1,
    private val epsilon: Double = 0.001,
    private val easing: EasingHandler = EasingHandler.IdentityEasingHandler(),
    private var startPosition: Vec3d = Vec3d(.0, .0, .0)
) {
    private var currentPosition: Vec3d = Vec3d(.0, .0, .0)
    private var targetPosition: Vec3d = Vec3d(.0, .0, .0)
    private var delta: Double = 0.0

    fun partialTick(deltaTime: Double) {
        if (currentPosition.squaredDistanceTo(targetPosition) <= epsilon.pow(2.0)) return
        val fullMovementVector = targetPosition.subtract(startPosition)
        val fullLength = fullMovementVector.length()
        val deltaOffset = 1 / (fullLength / maxMovementPerTick * deltaTime)
        delta += deltaOffset
        currentPosition = startPosition.lerp(targetPosition, delta)
    }

    fun moveTo(newPosition: Vec3d) {
        val newMovement = newPosition.subtract(currentPosition)
        val newDistance = newMovement.length()
        val oldDistanceTraveled = startPosition.lerp(targetPosition, delta).length()
        val newDelta = easing.recalculateDelta(delta, newDistance, oldDistanceTraveled)
        startPosition = newMovement.multiply(-1/newDelta)
        targetPosition = newPosition
        delta = newDelta
    }

    fun teleport(newPosition: Vec3d) {
        currentPosition = newPosition
        targetPosition = newPosition
        startPosition = newPosition
        delta = 1.0
    }

    //yes, this does mean that it CAN move faster then maxMovementPerTick, but as long as it still arrives on time...
    fun getCurrentPosition(): Vec3d = startPosition.lerp(targetPosition, easing.retrieveOffset(delta, startPosition.lerp(targetPosition, delta).length()))
}