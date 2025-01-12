package grauly.ritualis.extensions

import net.minecraft.util.math.Vec3d
import kotlin.math.*

fun Vec3d.toSpherical(): Vec3d {
    val r = this.length()
    val theta = acos(y/r)
    val phi = sign(z) * acos(x /sqrt(z*z + x * x))
    return Vec3d(r, theta, phi)
}

fun Vec3d.fromSpherical(): Vec3d {
    val newX = x * sin(y) * cos(z)
    val newY = x * sin(y) * sin(z)
    val newZ = x * cos(y)
    return Vec3d(newX, newY, newZ)
}