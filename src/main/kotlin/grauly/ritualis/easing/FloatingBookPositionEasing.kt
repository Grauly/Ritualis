package grauly.ritualis.easing

class FloatingBookPositionEasing: EasingHandler {
    override fun retrieveOffset(delta: Double, realDelta: Double): Double = Easings.easeInOutCubic(delta)

    override fun recalculateDelta(oldDelta: Double, immediateRealDelta: Double, oldRealDelta: Double): Double = 0.0
}