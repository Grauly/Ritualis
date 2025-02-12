package grauly.ritualis.util

import grauly.ritualis.easing.EasingHandler
import org.joml.Math.lerp
import kotlin.math.abs
import kotlin.math.min

class FloatHandler(
    private val maxChangePerTick: Float = 0.1f,
    private val epsilon: Float = 0.001f,
    private val easing: EasingHandler = EasingHandler.IdentityEasingHandler(),
    private var startValue: Float = 0.0f
) : ValueHandler<Float> {
    private var currentValue: Float = startValue
    private var targetValue: Float = startValue
    private var delta: Float = 0.0f

    override fun partialTick(timePassedTicks: Float) {
        if (abs(targetValue - currentValue) <= epsilon) return
        val fullChange = abs(targetValue - currentValue)
        if (fullChange <= epsilon) return
        val allowedChange = min(maxChangePerTick * timePassedTicks, fullChange)
        val deltaOffset = 1 / (fullChange / allowedChange)
        delta = min(1.0f, delta + deltaOffset)
        currentValue = lerp(startValue, targetValue, delta)
    }

    override fun updateGoal(newGoal: Float) {
        val newChange = (newGoal - currentValue)
        val previousChangeCompleted = abs(startValue - currentValue)
        val newDelta =
            easing.recalculateDelta(delta.toDouble(), abs(newChange).toDouble(), previousChangeCompleted.toDouble())
        startValue = newGoal + (if (newDelta == 0.0) -newChange else ((-1 / newDelta) * newChange).toFloat())
        targetValue = newGoal
        delta = newDelta.toFloat()
    }

    override fun getValue(): Float =
        lerp(
            startValue,
            targetValue,
            easing.retrieveOffset(delta.toDouble(), lerp(startValue, targetValue, delta).toDouble()).toFloat()
        )

    override fun setValue(newValue: Float) {
        startValue = newValue
        targetValue = newValue
        currentValue = newValue
        delta = 0f
    }
}