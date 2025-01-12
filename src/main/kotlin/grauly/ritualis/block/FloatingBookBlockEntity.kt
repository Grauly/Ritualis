package grauly.ritualis.block

import grauly.ritualis.ModBlockEntities
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import org.joml.Quaternionf
import kotlin.math.*

class FloatingBookBlockEntity(
    pos: BlockPos,
    state: BlockState
) : BlockEntity(
    ModBlockEntities.FLOATING_BOOK_ENTITY,
    pos,
    state
) {
    //only needed client side
    var renderingContext: RenderingContext = RenderingContext(0, pos.toCenterPos(), pos.toCenterPos())

    fun tick(world: World, pos: BlockPos, state: BlockState) {
        renderingTick(world, pos, state)
    }

    fun renderingTick(world: World, pos: BlockPos, state: BlockState) {
        renderingContext.ticks++
        val searchBox = Box(pos).expand(5.0)
        val lookAtPlayerTarget: PlayerEntity? = world.getNonSpectatingEntities(PlayerEntity::class.java, searchBox).reduceOrNull { currentClosest: PlayerEntity, playerEntity: PlayerEntity ->
            val currentDistance = pos.toCenterPos().squaredDistanceTo(currentClosest.eyePos)
            val newDistance = pos.toCenterPos().squaredDistanceTo(playerEntity.eyePos)
            return@reduceOrNull if(currentDistance > newDistance) playerEntity else currentClosest
        }
        //update look at target
        if(lookAtPlayerTarget != null) {
            renderingContext.ticksSincePlayerLookAt = 0
            renderingContext.updateLookAtTarget(lookAtPlayerTarget.eyePos, pos.toCenterPos())
        } else {
            renderingContext.ticksSincePlayerLookAt++
        }
    }

    data class RenderingContext(
        var ticks: Int,
        var lastLookAtTarget: Vec3d,
        var lookAtTarget: Vec3d,
        var lookAtVector: Vec3d = Vec3d(.0,.0,.0),
        var lastRotation: Quaternionf = Quaternionf(),
        var currentRotation: Quaternionf = Quaternionf(),
        var ticksSincePlayerLookAt: Int = 0,
        var ticksUntilNewTarget: Int = 0,
        var bookOpenState: Float = 1f
    ) {
        fun updateLookAtTarget(newTarget: Vec3d, currentLocation: Vec3d) {
            if (newTarget == lookAtTarget) return
            lastLookAtTarget = lookAtTarget
            lookAtTarget = newTarget
            lookAtVector = lookAtTarget.subtract(currentLocation)
            val direction = lookAtVector.normalize()
            val origin = Vec3d(1.0,0.0,0.0)
            var localUp = origin.crossProduct(direction)
            //if(localUp.y < 0) localUp = localUp.multiply(-1.0)
            val angle = Math.toDegrees(origin.dotProduct(direction))
            currentRotation = quaternionAroundAxisAngle(localUp, abs(angle.toFloat()) + 90f)
        }

        fun quaternionAroundAxisAngle(axis: Vec3d, angle: Float): Quaternionf {
            var radiansAngle = Math.toRadians(angle.toDouble())/2
            while(radiansAngle > 2*PI) radiansAngle -= PI
            while(radiansAngle < 0) radiansAngle += PI
            val scaledAxis = axis.normalize().multiply(sin(radiansAngle)).toVector3f()
            return Quaternionf(scaledAxis.x, scaledAxis.y, scaledAxis.z, sin(radiansAngle + PI/2).toFloat())
        }

        fun extractAxisFromQuaternion(quaternionf: Quaternionf): Vec3d {
            val angle = cosh(quaternionf.w)
            return Vec3d(quaternionf.x.toDouble(), quaternionf.y.toDouble(), quaternionf.z.toDouble()).multiply(sin(angle).toDouble())
        }
    }
}