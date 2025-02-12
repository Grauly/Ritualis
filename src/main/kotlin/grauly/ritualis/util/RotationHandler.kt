package grauly.ritualis.util

import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.min
import kotlin.math.sin

class RotationHandler(
    private val maxAngleSpeedPerTick: Float = 0.1f,
    private val epsilon: Float = 0.01f,
    var rotationOffset: Quaternionf = Quaternionf()
): ValueHandler<Quaternionf> {
    private var currentRotation: Quaternionf = Quaternionf() //a
    private var targetRotation: Quaternionf = Quaternionf() //c
    private var currentLookAtTarget: Vec3d = Vec3d(.0, .0, .0)

    override fun partialTick(deltaTime: Float) {
        //a^-1 * a * X = a^-1 * c
        val rotationQuaternion = Quaternionf(currentRotation).invert().mul(targetRotation)
        val fullMovementAngle = rotationQuaternion.angle()
        val axis =
            Vector3f(rotationQuaternion.x, rotationQuaternion.y, rotationQuaternion.z).div(sin(fullMovementAngle))
        val maxMovementAngle = maxAngleSpeedPerTick * deltaTime

        if (fullMovementAngle < epsilon || fullMovementAngle > PI * 2 - epsilon || !fullMovementAngle.isFinite()) {
            return
        }

        //giving up on smoothed movement for now
        val movementAngle: Float = if (fullMovementAngle > PI) {
            (2 * PI - min((2 * PI) - fullMovementAngle, maxMovementAngle.toDouble())).toFloat()
        } else {
            min(fullMovementAngle, maxMovementAngle)
        }

        val change = Quaternionf().rotationAxis(movementAngle, axis)
        currentRotation.mul(change)
    }

    override fun getValue(): Quaternionf = currentRotation

    override fun setValue(newValue: Quaternionf) {
        currentLookAtTarget = lookAtFromQuaternion(newValue)
        targetRotation = newValue
        currentRotation = newValue
    }

    override fun updateGoal(newGoal: Quaternionf) {
        currentLookAtTarget = lookAtFromQuaternion(newGoal)
        targetRotation = newGoal
    }

    fun updateGoal(lookTarget: Vec3d) {
        currentLookAtTarget = lookTarget
        adjustRotationTo(lookTarget)
    }

    fun handleOffset(offset: Vec3d) {
        val newTarget = currentLookAtTarget.subtract(offset)
        adjustRotationTo(newTarget)
    }

    private fun lookAtFromQuaternion(quaternion: Quaternionf): Vec3d {
        val angle = acos(quaternion.w).toDouble()
        val quaternionAxis =
            Vec3d(quaternion.x.toDouble(), quaternion.y.toDouble(), quaternion.z.toDouble()).multiply(1 / sin(angle))
        return quaternionAxis
    }

    private fun adjustRotationTo(lookTarget: Vec3d) {
        targetRotation = Quaternionf()
            .lookAlong(lookTarget.normalize().toVector3f(), Direction.UP.doubleVector.toVector3f())
            .invert()
            .mul(rotationOffset)
    }
}