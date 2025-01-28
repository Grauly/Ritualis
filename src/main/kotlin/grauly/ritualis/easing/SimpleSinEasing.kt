package grauly.ritualis.easing

import kotlin.math.cos

class SimpleSinEasing: EasingHandler {
    override fun retrieveOffset(delta: Double, realDelta: Double): Double = easeInOutSine(delta)

    override fun recalculateDelta(oldDelta: Double, immediateRealDelta: Double, oldRealDelta: Double): Double = oldDelta

    private fun easeInOutSine(x: Double): Double{
        return (-(cos(Math.PI * x) - 1) / 2);
    }
}