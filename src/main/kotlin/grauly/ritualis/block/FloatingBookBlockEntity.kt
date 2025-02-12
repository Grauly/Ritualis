package grauly.ritualis.block

import grauly.ritualis.ModBlockEntities
import grauly.ritualis.easing.FloatingBookPositionEasing
import grauly.ritualis.util.ChangeVariance
import grauly.ritualis.util.PositionHandler
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
    var renderingContext: RenderingContext = RenderingContext(RANDOM.nextInt(1000))
    private val positionVariance: ChangeVariance<Vec3d> = ChangeVariance(
        20,
        0,
        0,
        0,
        RANDOM.nextInt(20),
        ticksUntilNextChange = 20,
        value = Vec3d(.5, .5, .5).add(.0, .12, .0),
        valueUpdate = { random: Random, previous: Vec3d ->
            previous.subtract(.5, .5, .5).multiply(-1.0).add(.5, .5, .5)
        },
        updateCallback = { newValue: Vec3d, oldValue: Vec3d, timeUntilChangeEnds: Int ->
            renderingContext.bookPositionHandler.moveTo(newValue)
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
            if (isWatchingPlayer) return@ChangeVariance
            renderingContext.bookRotationHandler.lookAt(newValue)
        }
    )

    private var lastPlayerLookAtTarget: Vec3d = Vec3d(1.0, .0, .0)
    private var isWatchingPlayer: Boolean = false


    fun tick(world: World, pos: BlockPos, state: BlockState) {
        if(!world.isClient()) return
        renderingContext.ticks++
        if(state.get(FloatingBook.ACTIVE)) {
            activeRenderingTick(world, pos, state)
        } else {
            passiveRenderingTick(world, pos, state)
        }
    }

    private fun passiveRenderingTick(world: World, pos: BlockPos, state: BlockState) {

    }

    private fun activeRenderingTick(world: World, pos: BlockPos, state: BlockState) {
        positionVariance.runUpdate(world.getRandom())
        lookTargetVariance.runUpdate(world.getRandom())

        val searchStartPos = pos.toCenterPos()
        val lookAtTarget = world.getClosestPlayer(searchStartPos.x, searchStartPos.y, searchStartPos.z, 5.0, false)
        if (lookAtTarget != null) {
            if (lookAtTarget.eyePos != lastPlayerLookAtTarget) {
                isWatchingPlayer = true
                val localPos = lookAtTarget.eyePos.subtract(pos.toCenterPos())
                lookTargetVariance.pushValue(localPos)
                lastPlayerLookAtTarget = lookAtTarget.eyePos
                renderingContext.bookRotationHandler.lookAt(localPos)
            }
        } else {
            isWatchingPlayer = false
        }

    }


    data class RenderingContext(
        var ticks: Int,
        var lastTime: Float = ticks.toFloat(),
        val bookRotationHandler: RotationHandler =
            RotationHandler(rotationOffset = Quaternionf().rotationY((PI / 2).toFloat())),
        val bookPositionHandler: PositionHandler =
            PositionHandler(easing = FloatingBookPositionEasing(), startPosition = Vec3d(.5, .62, .5),)
    )

    companion object {
        val RANDOM: kotlin.random.Random = kotlin.random.Random(0)
    }
}