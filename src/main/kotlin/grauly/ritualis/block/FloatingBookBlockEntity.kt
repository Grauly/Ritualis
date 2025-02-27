package grauly.ritualis.block

import grauly.ritualis.ModBlockEntities
import grauly.ritualis.easing.FloatingBookPositionEasing
import grauly.ritualis.util.ChangeVariance
import grauly.ritualis.util.FloatHandler
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
    var renderingContext: RenderingContext = RenderingContext(RANDOM.nextInt(1000))

    //only needed client side
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
            if (!active) {
                return@ChangeVariance
            }
            renderingContext.bookPositionHandler.updateGoal(newValue)
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
            if (isWatchingPlayer || !active) {
                return@ChangeVariance
            }
            renderingContext.bookRotationHandler.updateGoal(newValue)
        }
    )
    private var lastPlayerLookAtTarget: Vec3d = Vec3d(1.0, .0, .0)
    private var isWatchingPlayer: Boolean = false
    var active: Boolean = false

    fun checkStateChange(world: World, pos: BlockPos, state: BlockState) {
        if (!world.isClient()) return
        val isNowActive = state.get(FloatingBook.ACTIVE)
        if (active == isNowActive) return
        active = isNowActive
        if (active) {
            onChangedToActive(world, pos, state)
        } else {
            onChangeToPassive(world, pos, state)
        }
    }

    private fun onChangedToActive(world: World, pos: BlockPos, state: BlockState) {
        renderingContext.bookRotationHandler.rotationOffset = ACTIVE_BOOK_ROTATION_OFFSET
        renderingContext.bookPositionHandler.updateGoal(Vec3d(.5, .5, .5))
        renderingContext.bookOpenStatusHandler.updateGoal(1f)
        renderingContext.referenceLocation = ACTIVE_MAIN_POSITION
    }

    private fun onChangeToPassive(world: World, pos: BlockPos, state: BlockState) {
        isWatchingPlayer = false
        renderingContext.bookPositionHandler.updateGoal(Vec3d(.5, .1, .5))
        val newLookVector = Vec3d(.0, .0, .0).addRandom(world.getRandom(), 1f).multiply(1.0, .0, 1.0)
        renderingContext.bookRotationHandler.rotationOffset = PASSIVE_BOOK_ROTATION_OFFSET
        renderingContext.bookRotationHandler.updateGoal(newLookVector)
        renderingContext.bookOpenStatusHandler.updateGoal(0f)
        renderingContext.referenceLocation = PASSIVE_MAIN_POSITION
    }

    fun tick(world: World, pos: BlockPos, state: BlockState) {
        if (!world.isClient()) return
        checkStateChange(world, pos, state)

        renderingContext.ticks++
        positionVariance.runUpdate(world.getRandom())
        lookTargetVariance.runUpdate(world.getRandom())

        if (state.get(FloatingBook.ACTIVE)) {
            activeRenderingTick(world, pos, state)
        } else {
            passiveRenderingTick(world, pos, state)
        }
    }

    private fun passiveRenderingTick(world: World, pos: BlockPos, state: BlockState) {

    }

    private fun activeRenderingTick(world: World, pos: BlockPos, state: BlockState) {
        val searchStartPos = pos.toCenterPos()
        val lookAtTarget = world.getClosestPlayer(searchStartPos.x, searchStartPos.y, searchStartPos.z, 5.0, false)
        if (lookAtTarget != null) {
            if (lookAtTarget.eyePos != lastPlayerLookAtTarget) {
                isWatchingPlayer = true
                val localPos = lookAtTarget.eyePos.subtract(pos.toCenterPos())
                lookTargetVariance.pushValue(localPos)
                lastPlayerLookAtTarget = lookAtTarget.eyePos
                renderingContext.bookRotationHandler.updateGoal(localPos)
            }
        } else {
            isWatchingPlayer = false
        }

    }


    data class RenderingContext(
        var ticks: Int,
        var lastTime: Float = ticks.toFloat(),
        var bookRotationHandler: RotationHandler =
            RotationHandler(
                startLookAt = Vec3d(.0, .0, .0)
                    .add(RANDOM.nextDouble(-1.0, 1.0), 0.0, RANDOM.nextDouble(-1.0, 1.0)),
                rotationOffset = PASSIVE_BOOK_ROTATION_OFFSET
            ),
        val bookPositionHandler: PositionHandler =
            PositionHandler(
                easing = FloatingBookPositionEasing(),
                startPosition = PASSIVE_MAIN_POSITION,
                maxMovementPerTick = 0.01
            ),
        val bookOpenStatusHandler: FloatHandler = FloatHandler(),
        var referenceLocation: Vec3d = PASSIVE_MAIN_POSITION
    )

    companion object {
        val RANDOM: kotlin.random.Random = kotlin.random.Random(0)
        val ACTIVE_BOOK_ROTATION_OFFSET: Quaternionf = Quaternionf().rotationY((PI / 2).toFloat())
        val PASSIVE_BOOK_ROTATION_OFFSET: Quaternionf = Quaternionf().rotationX((PI / 2).toFloat())
        val ACTIVE_MAIN_POSITION: Vec3d = Vec3d(.5, .5, .5)
        val PASSIVE_MAIN_POSITION: Vec3d = Vec3d(.5, .1, .5)
    }
}