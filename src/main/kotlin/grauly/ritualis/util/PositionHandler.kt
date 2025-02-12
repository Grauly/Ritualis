package grauly.ritualis.util

import grauly.ritualis.easing.EasingHandler
import net.minecraft.util.math.Vec3d
import kotlin.math.min
import kotlin.math.pow

class PositionHandler(
    private val maxMovementPerTick: Double = 0.1,
    private val epsilon: Double = 0.001,
    private val easing: EasingHandler = EasingHandler.IdentityEasingHandler(),
    private var startPosition: Vec3d = Vec3d(.0, .0, .0)
): ValueHandler<Vec3d> {
    private var currentPosition: Vec3d = startPosition
    private var targetPosition: Vec3d = startPosition
    private var delta: Double = 0.0

    override fun partialTick(timePassedTicks: Float) {
        if (currentPosition.squaredDistanceTo(targetPosition) <= epsilon.pow(2.0)) return
        val fullMovementVector = targetPosition.subtract(startPosition)
        val fullLength = fullMovementVector.length()
        if (fullLength <= epsilon) return
        val allowedMovement = min(fullLength, maxMovementPerTick * timePassedTicks)
        val deltaOffset = 1 / (fullLength / allowedMovement)
        delta = min(1.0, delta + deltaOffset)
        currentPosition = startPosition.lerp(targetPosition, delta)
    }

    override fun updateGoal(newGoal: Vec3d) {
        val newMovement = newGoal.subtract(this.getValue())
        val newDistance = newMovement.length()
        val oldDistanceTraveled = startPosition.lerp(targetPosition, delta).length()
        val newDelta = easing.recalculateDelta(delta, newDistance, oldDistanceTraveled)
        startPosition = newGoal.add(
            if (newDelta == 0.0)
                newMovement.multiply(-1.0)
            else
                newMovement.multiply(-1 / newDelta)
        )
        targetPosition = newGoal
        delta = newDelta
    }

    override fun setValue(newValue: Vec3d) {
        currentPosition = newValue
        targetPosition = newValue
        startPosition = newValue
        delta = 1.0
    }

    //yes, this does mean that it CAN move faster then maxMovementPerTick, but as long as it still arrives on time...
    override fun getValue(): Vec3d = startPosition.lerp(
        targetPosition,
        easing.retrieveOffset(delta, startPosition.lerp(targetPosition, delta).length())
    )
}