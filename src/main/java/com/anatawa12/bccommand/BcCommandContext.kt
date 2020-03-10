package com.anatawa12.bccommand

import net.minecraft.util.math.Vec3d
import kotlin.math.atan2

abstract class BcCommandContext() {
    abstract val p0: Vec3d
    abstract val fineness: Int

    protected abstract fun getPosAt(t: Double): Vec3d
    protected abstract fun getAngleAt(t: Double): Vec2f

    fun getPositionsAndLength(): Pair<List<BcCommandContextPosition>, Double> {
        var prev = p0
        var length = 0.0
        val positions = ArrayList<BcCommandContextPosition>(fineness + 1)
        repeat(fineness + 1) {
            val t = it / fineness.toDouble()
            val centerPos = getPosAt(t)
            val (angleX, angleZ) = getAngleAt(t)
            val angle = atan2(angleZ, angleX).toDouble()

            positions.add(BcCommandContextPosition(centerPos, angle, length))

            length += prev.distanceTo(centerPos)
            prev = centerPos
        }
        return positions to length
    }
}
