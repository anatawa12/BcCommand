package com.anatawa12.bccommand

import net.minecraft.util.math.Vec3d

data class BcCommandLineContext(
        override val p0: Vec3d,
        val p3: Vec3d,
        // options
        override val fineness: Int): BcCommandContext() {
    private val p3FromP0 = p3 - p0
    private val angle = Vec2f(p3FromP0.x.toFloat(), p3FromP0.z.toFloat())

    override fun getPosAt(t: Double): Vec3d {
        require(t in 0.0 .. 1.0) { "t must be in 0.0 to 1.0" }
        return p3FromP0 * t + p0
    }

    override fun getAngleAt(t: Double): Vec2f = angle
}
