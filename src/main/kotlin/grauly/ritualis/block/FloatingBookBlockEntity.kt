package grauly.ritualis.block

import grauly.ritualis.ModBlockEntities
import grauly.ritualis.util.ChangeVariance
import grauly.ritualis.util.RotationHandler
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.random.Random
import net.minecraft.world.World
import org.joml.Quaternionf
import kotlin.math.PI

class FloatingBookBlockEntity(
    pos: BlockPos,
    state: BlockState
) : BlockEntity(
    ModBlockEntities.FLOATING_BOOK_ENTITY,
    pos,
    state
) {
    //only needed client side
    var renderingContext: RenderingContext = RenderingContext(0)
    val bookRotationHandler: RotationHandler =
        RotationHandler(rotationOffset = Quaternionf().rotationY((PI / 2).toFloat()))
    private val positionVariance: ChangeVariance<Vec3d> = ChangeVariance(
        20,
        0,
        0,
        0,
        getWorld()?.getRandom()?.nextInt(20) ?: (20),
        Vec3d(.5, .5, .5).add(.0, .12, .0),
        valueUpdate = { random: Random, previous: Vec3d ->
            previous.subtract(.5, .5, .5).multiply(-1.0).add(.5, .5, .5)
        },
        updateCallback = { newValue: Vec3d, oldValue: Vec3d, timeUntilChangeEnds: Int ->
            renderingContext.previousTargetPosition = oldValue
            renderingContext.targetPosition = newValue
            renderingContext.positionStartTimestamp = renderingContext.ticks
            renderingContext.positionEndTimestamp = renderingContext.ticks + timeUntilChangeEnds
        }
    )

    private val lookTargetVariance: ChangeVariance<Vec3d> = ChangeVariance(
        2 * 20,
        5,
        6 * 20,
        2 * 20,
        20 * 20,
        Vec3d(1.0, .0, .0),
        valueUpdate = { random: Random, previous: Vec3d ->
            Vec3d(.0, .0, .0).addRandom(random, 10f)
        },
        updateCallback = { newValue: Vec3d, oldValue: Vec3d, timeUntilChangeEnds: Int ->

        }
    )

    private var lastPlayerLookAtTarget: Vec3d = Vec3d(1.0, .0, .0)


    fun tick(world: World, pos: BlockPos, state: BlockState) {
        renderingTick(world, pos, state)
    }

    private fun renderingTick(world: World, pos: BlockPos, state: BlockState) {
        renderingContext.ticks++
        positionVariance.runUpdate(world.getRandom())
        lookTargetVariance.runUpdate(world.getRandom())

        val searchStartPos = pos.toCenterPos()
        val lookAtTarget = world.getClosestPlayer(searchStartPos.x, searchStartPos.y, searchStartPos.z, 5.0, false)
        if (lookAtTarget != null && lookAtTarget.eyePos != lastPlayerLookAtTarget) {
            val localPos = lookAtTarget.eyePos.subtract(pos.toCenterPos())
            lookTargetVariance.pushValue(localPos)
            lastPlayerLookAtTarget = lookAtTarget.eyePos
            bookRotationHandler.lookAt(localPos)
        }

    }


    data class RenderingContext(
        var ticks: Int,
        var targetPosition: Vec3d = Vec3d(1.0, .0, .0),
        var previousTargetPosition: Vec3d = Vec3d(1.0, .0, .0),
        var positionStartTimestamp: Int = ticks,
        var positionEndTimestamp: Int = ticks,
    )
}