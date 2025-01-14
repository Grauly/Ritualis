package grauly.ritualis.block

import grauly.ritualis.ModBlockEntities
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.random.Random
import net.minecraft.world.World
import org.joml.Quaternionf

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
        5,
        0,
        0,
        getWorld()?.getRandom()?.nextInt(20) ?: (20),
        Vec3d(.5, .5, .5)
    )
    { random: Random, previous: Vec3d -> Vec3d(.5, .5, .5).addRandom(random, .2f) }

    private val lookAtVariance: ChangeVariance<Vec3d> = ChangeVariance(
        6 * 20,
        2 * 20,
        3 * 20,
        20,
        getWorld()?.getRandom()?.nextInt(6 * 20) ?: (20),
        Vec3d(1.0, .0, .0)
    )
    { random: Random, previous: Vec3d -> Vec3d(.0, .0, .0).addRandom(random, 1f) }


    fun tick(world: World, pos: BlockPos, state: BlockState) {
        renderingTick(world, pos, state)
    }

    private fun renderingTick(world: World, pos: BlockPos, state: BlockState) {
        renderingContext.ticks++
        val searchStartPos = pos.toCenterPos()
        val lookAtTarget = world.getClosestPlayer(searchStartPos.x, searchStartPos.y, searchStartPos.z, 5.0, false)

        positionVariance.runUpdate(world.getRandom())
        if (lookAtTarget == null) lookAtVariance.runUpdate(world.getRandom())

        renderingContext.update(
            positionVariance.previousValue.lerp(positionVariance.value, positionVariance.nextChangeDelta().toDouble()),
            lookAtVariance.previousValue.lerp(lookAtVariance.value, lookAtVariance.nextChangeDelta().toDouble())
        )
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
        val valueUpdate: (Random, T) -> T
    ) {
        fun runUpdate(random: Random) {
            ticksPassed++
            if (ticksUntilNextChange - ticksPassed > 0) return

            ticksPassed = 0
            ticksUntilIdle = changeIntervalBase + (callRandom(changeIntervalVariance, random) * 2 - changeIntervalVariance)
            ticksUntilNextChange =
                ticksUntilIdle + idleIntervalBase + (callRandom(idleIntervalVariance, random) * 2 - idleIntervalVariance)

            previousValue = value
            value = valueUpdate.invoke(random, value)
        }

        private fun callRandom(value: Int, random: Random): Int {
            if(value <= 0) return 0
            return random.nextInt(value)
        }

        fun changeDelta(): Float = ticksPassed.toFloat().div(ticksUntilIdle.toFloat())
        fun nextChangeDelta(): Float = (ticksPassed + 1).toFloat().div(ticksUntilIdle.toFloat())
    }

    data class RenderingContext(
        var ticks: Int,
        var targetRotation: Quaternionf = Quaternionf(),
        var lastTargetRotation: Quaternionf = Quaternionf(),
        var targetPosition: Vec3d = Vec3d(1.0, .0, .0),
        var lastTargetPosition: Vec3d = Vec3d(1.0, .0, .0)
    ) {
        fun update(newTargetPos: Vec3d, newTargetRotation: Vec3d) {
            lastTargetPosition = targetPosition
            lastTargetRotation = targetRotation
            targetPosition = newTargetPos
            targetRotation = Quaternionf().lookAlong(newTargetRotation.toVector3f(), Direction.UP.unitVector).invert()
        }
    }
}