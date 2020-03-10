package com.anatawa12.bccommand

import net.minecraft.util.math.Vec3d
import kotlin.math.pow

data class BcCommandNonLineContext(
        override val p0: Vec3d,
        val p1: Vec3d,
        val p2: Vec3d,
        val p3: Vec3d,
        val rtmMode: Boolean,
        // options
        override val fineness: Int): BcCommandContext() {
    private val p1FromP0 = p1 - p0
    private val p2FromP1 = p2 - p1
    private val p3FromP2 = p3 - p2
    private val p3FromP0 = p3 - p0

    override fun getPosAt(t: Double): Vec3d {
        require(t in 0.0 .. 1.0) { "t must be in 0.0 to 1.0" }
        val p4 = p1FromP0 * t + p0
        val p5 = p2FromP1 * t + p1
        val p6 = p3FromP2 * t + p2

        val p7 = (p5 - p4) * t + p4
        val p8 = (p6 - p5) * t + p5

        val p9 = (p8 - p7) * t + p7

        if (!rtmMode) return p9
        // liner for y axis
        val y = p3FromP0.y * t + p0.y
        return Vec3d(p9.x, y, p9.z)
    }

    /*
       calculate by wolfram engine
       -3*(p0*(-1 + t)^2 + t*(-2*p2 + 3*p2*t - p3*t) + p1*(-1 + 4*t - 3*t^2))
     */
    override fun getAngleAt(t: Double): Vec2f {
        val d = -3*(p0*(-1 + t).pow(2) + t*(-2*p2 + 3*p2*t - p3*t) + p1*(-1 + 4*t - 3*t.pow(2)))
        return Vec2f(d.x.toFloat(), d.z.toFloat())
    }
}
