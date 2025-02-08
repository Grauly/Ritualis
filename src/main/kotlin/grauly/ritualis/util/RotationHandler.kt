package grauly.ritualis.util

import grauly.ritualis.Ritualis
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

class RotationHandler(
    private val maxAngleSpeedPerTick: Float = 0.1f,
    private val epsilon: Float = 0.01f,
    private val rotationOffset: Quaternionf = Quaternionf()
) {
    private var currentRotation: Quaternionf = Quaternionf() //a
    private var targetRotation: Quaternionf = Quaternionf() //c
    private var currentLookAtTarget: Vec3d = Vec3d(.0, .0, .0)

    //TODO: fix broken rotation on target reach (FUCKING AGAIN)

    fun partialTick(deltaTime: Float) {
        //a^-1 * a * X = a^-1 * c
        val rotationQuaternion = Quaternionf(currentRotation).invert().mul(targetRotation)
        val fullMovementAngle = rotationQuaternion.angle()
        val axis =
            Vector3f(rotationQuaternion.x, rotationQuaternion.y, rotationQuaternion.z).div(sin(fullMovementAngle))
        val maxMovementAngle = maxAngleSpeedPerTick * deltaTime

        Ritualis.LOGGER.info("fullMovementAngle: {}, current: {} {}", fullMovementAngle, currentRotation, currentRotation.lengthSquared())
        if (fullMovementAngle < epsilon || fullMovementAngle > PI * 2 - epsilon || !fullMovementAngle.isFinite()) {
            Ritualis.LOGGER.info("returning")
            return
        }
        Ritualis.LOGGER.info("not returning")

        //giving up on smoothed movement for now
        val movementAngle: Float = if (fullMovementAngle > PI) {
            (2 * PI - min((2 * PI) - fullMovementAngle, maxMovementAngle.toDouble())).toFloat()
        } else {
            min(fullMovementAngle, maxMovementAngle)
        }

        val change = Quaternionf().rotationAxis(movementAngle, axis)
        Ritualis.LOGGER.info("change: {} {}", change, change.lengthSquared())
        currentRotation.mul(change)
    }

    fun getRotation(): Quaternionf = currentRotation

    fun lookAt(lookTarget: Vec3d) {
        currentLookAtTarget = lookTarget
        adjustRotationTo(lookTarget)
    }

    fun handleOffset(offset: Vec3d) {
        val newTarget = currentLookAtTarget.subtract(offset)
        adjustRotationTo(newTarget)
    }

    private fun adjustRotationTo(lookTarget: Vec3d) {
        targetRotation = Quaternionf()
            .lookAlong(lookTarget.normalize().toVector3f(), Direction.UP.doubleVector.toVector3f())
            .invert()
            .mul(rotationOffset)

    }
}