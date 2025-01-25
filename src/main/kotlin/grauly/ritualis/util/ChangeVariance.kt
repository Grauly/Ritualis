package grauly.ritualis.util

import net.minecraft.util.math.random.Random
import kotlin.math.max

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
        if (ticksUntilNextChange - ticksPassed > 0) return

        updateValue(valueUpdate.invoke(random, value), random)
    }

    private fun updateValue(newValue: T, random: Random) {
        ticksPassed = 0
        ticksUntilIdle =
            changeIntervalBase + (callRandom(changeIntervalVariance, random) * 2 - changeIntervalVariance)
        ticksUntilNextChange =
            ticksUntilIdle + idleIntervalBase + (callRandom(
                idleIntervalVariance,
                random
            ) * 2 - idleIntervalVariance)

        previousValue = value
        value = newValue
        updateCallback.invoke(value, previousValue, ticksUntilIdle)
    }

    fun pushValue(newValue: T) {
        value = newValue
        updateCallback.invoke(value, previousValue, max(ticksUntilIdle, 1))
    }

    private fun callRandom(value: Int, random: Random): Int {
        if (value <= 0) return 0
        return random.nextInt(value)
    }
}
