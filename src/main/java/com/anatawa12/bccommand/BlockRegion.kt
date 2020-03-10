package com.anatawa12.bccommand

import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class BlockPlateRegion private constructor(
        val width: Int,
        val height: Int,
        private val blocks: Array<Array<BlockData>>
) : Iterable<IBlockRegionElement> {
    init {
        require(width%2 == 1) { "width must be odd" }
        require(width == blocks.size) { "length of blocks must be width" }
        blocks.forEach {
            require(height == it.size) { "length of a line of blocks must be height" }
        }
    }

    override fun iterator(): Iterator<IBlockRegionElement> = iterator {
        repeat(width) {
            val x = it - width / 2;
            repeat(height) { y ->
                yield(BlockRegionElementImpl(x, y))
            }
        }
    }

    private inner class BlockRegionElementImpl(override val x: Int, override val y: Int) : IBlockRegionElement {
        override fun placeBlockTo(world: World, pos: BlockPos) {
            val realX = x + (width/2)
            val (state, tile) = blocks[realX][y]
            world.setBlockState(pos, state)
            if (tile != null) {
                tile.setInteger("x", pos.x)
                tile.setInteger("y", pos.y)
                tile.setInteger("z", pos.z)

                world.setTileEntity(pos, TileEntity.create(world, tile))
            }
        }
    }

    private data class BlockData(
            val blockState: IBlockState,
            val tileNBT: NBTTagCompound?
    ) {
        companion object {
            fun fromPos(world: World, pos: BlockPos): BlockData {
                val state = world.getBlockState(pos)
                val tile = world.getTileEntity(pos)
                val tileNBT = if (tile == null) null else {
                    val nbt = NBTTagCompound()
                    tile.readFromNBT(nbt)
                    nbt
                }
                return BlockData(state, tileNBT)
            }
        }
    }

    companion object {
        fun fromRegion(region: PlayerDataStorage.BlockRegion): BlockPlateRegionResult {
            when {
                region.min.x == region.max.x -> {
                    val width = region.max.z - region.min.z + 1
                    if (width % 2 != 1)
                        return BlockPlateRegionResult.error("width of the plate must be odd")
                    val height = region.max.y - region.min.y + 1
                    return BlockPlateRegionResult.success(BlockPlateRegion(
                            width, height,
                            Array(width) { z ->
                                Array(height) { y ->
                                    BlockData.fromPos(region.world, region.min.add(0, y, z))
                                }
                            }
                    ))
                }
                region.min.z == region.max.z -> {
                    val width = region.max.x - region.min.x + 1
                    if (width % 2 != 1)
                        return BlockPlateRegionResult.error("width of the plate must be odd")
                    val height = region.max.y - region.min.y + 1
                    return BlockPlateRegionResult.success(BlockPlateRegion(
                            width, height,
                            Array(width) { x ->
                                Array(height) { y ->
                                    BlockData.fromPos(region.world, region.min.add(x, y, 0))
                                }
                            }
                    ))
                }
                else -> return BlockPlateRegionResult.error("x or z size must be 1")
            }
        }
    }
}

@Suppress("NON_PUBLIC_PRIMARY_CONSTRUCTOR_OF_INLINE_CLASS")
inline class BlockPlateRegionResult private constructor(private val _value: Any) {
    val isSuccess get() = _value is BlockPlateRegion
    val value get() = _value as BlockPlateRegion
    val reason get() = _value as String
    companion object {
        fun success(region: BlockPlateRegion) = BlockPlateRegionResult(region)
        fun error(reason: String) = BlockPlateRegionResult(reason)
    }
}

interface IBlockRegionElement {
    val x: Int
    val y: Int
    fun placeBlockTo(world: World, pos: BlockPos)
}

operator fun IBlockRegionElement.component1() = x
operator fun IBlockRegionElement.component2() = y
