package com.anatawa12.bccommand

import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.common.DimensionManager
import net.minecraftforge.common.util.BlockSnapshot
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.max
import kotlin.math.min

object PlayerDataStorage {
    private val players: MutableMap<UUID, PlayerData> = mutableMapOf()

    private class PlayerData() {
        val arrayList = ArrayList<UndoRedoPair>()
        var redoIndex = 0
        fun did(undo: List<BlockSnapshot>, redo: UndoReplacer.() -> Unit) {
            while (redoIndex < arrayList.size)
                arrayList.removeAt(arrayList.lastIndex)
            arrayList.add(UndoRedoPair(undo, redo))
            redoIndex++
        }

        fun redo(): (() -> Unit)? {
            if (redoIndex == arrayList.size)
                return null
            val pair = arrayList[redoIndex++]
            val redo = pair.redo
            return { pair.redo() }
        }

        fun undo(): List<BlockSnapshot>? {
            if (redoIndex == 0)
                return null
            return arrayList[--redoIndex].undo
        }

        var worldId: Int? = null
        var first: BlockPos? = null
        var second: BlockPos? = null

        fun setFirstPos(player: EntityPlayerMP, world: World, position: BlockPos) {
            if (world.provider.dimension != worldId)
                second = null
            first = position
            worldId = world.provider.dimension
            shareRegionData(player)
        }

        fun setSecondPos(player: EntityPlayerMP, world: World, position: BlockPos) {
            if (world.provider.dimension != worldId)
                first = null
            second = position
            worldId = world.provider.dimension
            shareRegionData(player)
        }

        fun shareRegionData(player: EntityPlayerMP) {
            Network.sendToClient(player, ShareYourRegion(worldId, first, second))
        }

        fun clearSelection(player: EntityPlayerMP) {
            first = null
            second = null
            worldId = null
            shareRegionData(player)
        }

        private class UndoRedoPair(
                var undo: List<BlockSnapshot>,
                val redo: UndoReplacer.() -> Unit
        ) : UndoReplacer{
            override fun resetUndo(undo: List<BlockSnapshot>) {
                this.undo = undo
            }
        }
    }

    fun did(player: EntityPlayerMP, undo: List<BlockSnapshot>, redo: UndoReplacer.() -> Unit) {
        players.getOrPut(player.persistentID, ::PlayerData).did(undo, redo)
    }
    fun redo(player: EntityPlayerMP): (() -> Unit)? = players[player.persistentID]?.redo()
    fun undo(player: EntityPlayerMP): List<BlockSnapshot>? = players[player.persistentID]?.undo()

    fun setFirstPos(player: EntityPlayerMP, world: World, position: BlockPos) {
        players.getOrPut(player.persistentID, ::PlayerData).setFirstPos(player, world, position)
    }

    fun setSecondPos(player: EntityPlayerMP, world: World, position: BlockPos) {
        players.getOrPut(player.persistentID, ::PlayerData).setSecondPos(player, world, position)
    }

    fun getBlockRegion(player: EntityPlayerMP): BlockRegion? {
        val data = players[player.persistentID] ?: return null
        val worldId = data.worldId ?: return null
        val first = data.first ?: return null
        val second = data.second ?: return null

        return BlockRegion(
                DimensionManager.getWorld(worldId)!!,
                BlockPos(min(first.x, second.x), min(first.y, second.y), min(first.z, second.z)),
                BlockPos(max(first.x, second.x), max(first.y, second.y), max(first.z, second.z))
        )
    }

    fun shareRegionData(player: EntityPlayerMP) {
        val data = players[player.persistentID]
        if (data == null)
            Network.sendToClient(player, ShareYourRegion(null, null, null))
        else
            data.shareRegionData(player)
    }

    fun clearSelection(player: EntityPlayerMP) = players[player.persistentID]?.clearSelection(player)

    class BlockRegion(
            val world: World,
            val min: BlockPos,
            val max: BlockPos
    )

    interface UndoReplacer {
        fun resetUndo(undo: List<BlockSnapshot>)
    }
}
