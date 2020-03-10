package com.anatawa12.bccommand

import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraftforge.common.util.BlockSnapshot

val searchSize = 200

operator fun BlockPos.component1() = x
operator fun BlockPos.component2() = y
operator fun BlockPos.component3() = z

operator fun Vec2f.component1() = x
operator fun Vec2f.component2() = y


operator fun Vec3d.minus(other: Vec3d) = this.subtract(other)
operator fun Vec3d.plus(other: Vec3d) = this.add(other)
operator fun Vec3d.times(other: Double) = this.scale(other)
operator fun Double.times(other: Vec3d) = other.scale(this)
operator fun Vec3d.times(other: Int) = this.scale(other.toDouble())
operator fun Int.times(other: Vec3d) = other.scale(this.toDouble())
operator fun Vec3d.unaryPlus() = this
operator fun Vec3d.unaryMinus() = Vec3d.ZERO - this

fun List<BlockSnapshot>.notifyAll() {
    for (snap in this) {
        val world = snap.world
        world.markAndNotifyBlock(snap.pos, world.getChunk(snap.pos), snap.replacedBlock, world.getBlockState(snap.pos), snap.flag)
    }
}

