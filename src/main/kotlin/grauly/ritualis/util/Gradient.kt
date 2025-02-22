package grauly.ritualis.util

class Gradient<T>(
    start: T,
    end: T,
    val interpolator: (T, T, Double) -> T,
    additionalPoints: MutableList<GradientPoint<T>> = mutableListOf(),
) {
    private val gradient: MutableList<GradientPoint<T>> = mutableListOf(
        GradientPoint(0.0, start),
        GradientPoint(1.0, end)
    )

    init {
        gradient.addAll(additionalPoints)
        gradient.sort()
    }

    fun retrieve(delta: Double): T {
        if (gradient.size == 2) return interpolator.invoke(gradient[0].value, gradient[1].value, delta)
        var smallerPoint = gradient.first()
        var largerPoint = gradient.first()
        for (gradientPoint in gradient) {
            if (gradientPoint.point <= delta) {
                smallerPoint = gradientPoint
                continue
            }
            largerPoint = gradientPoint
            break
        }
        val deltaSpan = largerPoint.point - smallerPoint.point
        val relativeDelta = (delta - deltaSpan) * 1 / deltaSpan
        return interpolator.invoke(smallerPoint.value, largerPoint.value, relativeDelta)
    }

    data class GradientPoint<T>(
        val point: Double,
        val value: T
    ) : Comparable<GradientPoint<T>> {
        override fun compareTo(other: GradientPoint<T>): Int {
            if (point == other.point) return 0
            return if (point < other.point) -1 else 1
        }
    }
}