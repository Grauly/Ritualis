package grauly.ritualis.block

import grauly.ritualis.ModBlockEntities
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
            renderingContext.previousLookTarget = oldValue
            renderingContext.lookTarget = newValue
            renderingContext.lookStartTimestamp = renderingContext.ticks
            renderingContext.lookEndTimestamp = renderingContext.ticks + timeUntilChangeEnds
        }
    )


    fun tick(world: World, pos: BlockPos, state: BlockState) {
        renderingTick(world, pos, state)
    }

    private fun renderingTick(world: World, pos: BlockPos, state: BlockState) {
        renderingContext.ticks++
        val searchStartPos = pos.toCenterPos()
        val lookAtTarget = world.getClosestPlayer(searchStartPos.x, searchStartPos.y, searchStartPos.z, 5.0, false)

        positionVariance.runUpdate(world.getRandom())
        lookTargetVariance.runUpdate(world.getRandom())

    }

    data class ChangeVariance<T>(
        val changeIntervalBase: Int,
        val changeIntervalVariance: Int,
        val idleIntervalBase: Int,
        val idleIntervalVariance: Int,

        var ticksPassed: Int,
        var value: T,

        var ticksUntilNextChange: Int = 0,
        var ticksUntilIdle: Int = 0,

        var previousValue: T = value,
        val valueUpdate: (Random, T) -> T,
        val updateCallback: (T, T, Int) -> Unit = { new: T, old: T, delta: Int -> }
    ) {
        fun runUpdate(random: Random) {
            ticksPassed++
            if (!(ticksUntilNextChange - ticksPassed <= 0)) return

            ticksPassed = 0
            ticksUntilIdle =
                changeIntervalBase + (callRandom(changeIntervalVariance, random) * 2 - changeIntervalVariance)
            ticksUntilNextChange =
                ticksUntilIdle + idleIntervalBase + (callRandom(idleIntervalVariance, random) * 2 - idleIntervalVariance)

            previousValue = value
            value = valueUpdate.invoke(random, value)
            updateCallback.invoke(value, previousValue, ticksUntilIdle)
        }

        private fun callRandom(value: Int, random: Random): Int {
            if (value <= 0) return 0
            return random.nextInt(value)
        }
    }

    data class RenderingContext(
        var ticks: Int,
        var targetPosition: Vec3d = Vec3d(1.0, .0, .0),
        var previousTargetPosition: Vec3d = Vec3d(1.0, .0, .0),
        var positionStartTimestamp: Int = ticks,
        var positionEndTimestamp: Int = ticks,
        var lookTarget: Vec3d = Vec3d(1.0, .0, .0),
        var previousLookTarget: Vec3d = Vec3d(1.0, .0, .0),
        var lookStartTimestamp: Int = ticks,
        var lookEndTimestamp: Int = ticks
    )
}