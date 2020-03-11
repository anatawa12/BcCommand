package com.anatawa12.bccommand

import com.anatawa12.bccommand.BcCommandCore.Companion.MOD_ID
import com.anatawa12.bccommand.BlockPlateRegion.Companion.fromRegion
import net.minecraft.command.*
import net.minecraft.init.Blocks
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.text.Style
import net.minecraft.util.text.TextComponentTranslation
import net.minecraft.util.text.TextFormatting
import net.minecraft.world.World
import net.minecraftforge.common.util.BlockSnapshot
import kotlin.math.cos
import kotlin.math.sin

object CommandBc : CommandBase() {
    override fun getName(): String = "//bc"

    override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<String>) {
        val player = getCommandSenderAsPlayer(sender)
        val world = sender.entityWorld
        try {
            world.captureBlockSnapshots = true
            world.capturedBlockSnapshots.clear()
            val parameters = parseParameters(args)

            val region = PlayerDataStorage.getBlockRegion(player)?.let(BlockPlateRegion.Companion::fromRegion)
                    ?: throw CommandException("please select with $MOD_ID:${SelectionWand.ID}. you can get it with /give command")
            if (!region.isSuccess)
                throw CommandException(region.reason)

            val (p0, p1, p2, p3) = getPoints(sender, parameters.isLine)

            val context = if (parameters.isLine) {
                BcCommandLineContext(
                        p0, p3,
                        fineness = 1000
                )
            } else {
                BcCommandNonLineContext(
                        p0, p1, p2, p3,
                        rtmMode = false,
                        fineness = 1000
                )
            }

            val (positions, length) = context.getPositionsAndLength()

            doMain(parameters, positions, region.value, world, p0, p1, p2, p3)

            sender.sendMessage(TextComponentTranslation("length: %s", "$length"))

            @Suppress("UNCHECKED_CAST")
            PlayerDataStorage.did(player, world.capturedBlockSnapshots.clone() as List<BlockSnapshot>) {
                try {
                    world.captureBlockSnapshots = true
                    world.capturedBlockSnapshots.clear()
                    doMain(parameters, positions, region.value, world, p0, p1, p2, p3)
                    resetUndo(world.capturedBlockSnapshots.clone() as List<BlockSnapshot>)
                } finally {
                    (world.capturedBlockSnapshots.clone() as List<BlockSnapshot>).notifyAll()
                    world.captureBlockSnapshots = false
                    world.capturedBlockSnapshots.clear()
                }
            }

        } finally {
            (world.capturedBlockSnapshots.clone() as List<BlockSnapshot>).notifyAll()
            world.captureBlockSnapshots = false
            world.capturedBlockSnapshots.clear()
        }
    }

    fun doMain(parameters: Parameters, positions: List<BcCommandContextPosition>, region: BlockPlateRegion, world: World,
               p0: Vec3d, p1: Vec3d, p2: Vec3d, p3: Vec3d) {
        var placeNextLength = parameters.offset

        positions.forEach { (centerPos, angle, length) ->
            if (length <= placeNextLength)
                return@forEach
            placeNextLength += parameters.perBlocks

            val diffX = sin(-angle)
            val diffZ = cos(-angle)

            region.forEach { element ->
                val (x, y) = element
                val blockPos = BlockPos(centerPos.add(diffX * x, y.toDouble(), diffZ * x))

                if (parameters.isForce || world.getBlockState(blockPos).block == Blocks.AIR)
                    element.placeBlockTo(world, blockPos)
            }
        }

        if (parameters.keep) {
            world.setBlockState(BlockPos(p0), Blocks.SPONGE.defaultState)
            world.setBlockState(BlockPos(p1), Blocks.DIAMOND_BLOCK.defaultState)
            world.setBlockState(BlockPos(p2), Blocks.EMERALD_BLOCK.defaultState)
            world.setBlockState(BlockPos(p3), Blocks.END_STONE.defaultState)
        } else {
            world.setBlockState(BlockPos(p0), Blocks.GLASS.defaultState)
            world.setBlockState(BlockPos(p1), Blocks.GLASS.defaultState)
            world.setBlockState(BlockPos(p2), Blocks.GLASS.defaultState)
            world.setBlockState(BlockPos(p3), Blocks.GLASS.defaultState)
        }
    }

    override fun getUsage(sender: ICommandSender): String = "///bc <per blocks> <offset> [keep] [force] [line] [rtm]"

    override fun getRequiredPermissionLevel(): Int = 0

    override fun getTabCompletions(server: MinecraftServer, sender: ICommandSender, args: Array<String>, targetPos: BlockPos?)
            = getListOfStringsMatchingLastWord(args, "keep", "force", "line", "rtm")

    private fun parseParameters(args: Array<String>): Parameters {
        var perBlocks = 0.0
        var offset = 0.0
        var keep = false
        var isForce = false
        var isLine = false
        var isRTM = false

        var index = 0

        args.getOrNull(index)?.toDoubleOrNull()?.let {
            index++
            if (it < 0) throw NumberInvalidException("commands.generic.num.tooSmall", it, 0);
            perBlocks = it
        }

        args.getOrNull(index)?.toDoubleOrNull()?.let {
            index++
            if (it < 0) throw NumberInvalidException("commands.generic.num.tooSmall", it, 0);
            offset = it
        }

        while (index in args.indices) {
            when (val arg = args[index]) {
                "keep" -> keep = true
                "force" -> isForce = true
                "line" -> isLine = true
                "rtm" -> isRTM = true
                else -> throw SyntaxErrorException("unknown argument: %s", arg)
            }
            index++
        }

        return Parameters(
                perBlocks = perBlocks,
                keep = keep,
                offset = offset,
                isForce = isForce,
                isLine = isLine,
                isRTM = isRTM
        )
    }

    private fun checkArg(sender: ICommandSender, list: List<*>, name: String) {
        if (list.isEmpty()) {
            sender.sendMessage(TextComponentTranslation("Error: cannot find %s", name).setStyle(Style().setColor(TextFormatting.RED)))
        } else if (list.size != 1) {
            sender.sendMessage(TextComponentTranslation("Error: found two or more %s", name).setStyle(Style().setColor(TextFormatting.RED)))
        }
    }

    private fun getPoints(sender: ICommandSender, isLine: Boolean): Array<Vec3d> {
        val (cx, _, cz) = sender.position

        val p0s = mutableListOf<BlockPos>()
        val p1s = mutableListOf<BlockPos>()
        val p2s = mutableListOf<BlockPos>()
        val p3s = mutableListOf<BlockPos>()

        for (x in cx - searchSize .. cx + searchSize) {
            for (z in cz - searchSize..cz + searchSize) {
                if (!sender.entityWorld.isBlockLoaded(BlockPos(x, 0, z))) continue
                for (y in 0..sender.entityWorld.height) {
                    val pos = BlockPos(x, y, z)
                    val block = sender.entityWorld.getBlockState(pos).block
                    when (block) {
                        Blocks.SPONGE -> p0s += pos
                        Blocks.DIAMOND_BLOCK -> p1s += pos
                        Blocks.EMERALD_BLOCK -> p2s += pos
                        Blocks.END_STONE -> p3s += pos
                    }
                }
            }
        }
        checkArg(sender, p0s, "p0")
        checkArg(sender, p3s, "p3")
        if (!isLine) {
            checkArg(sender, p1s, "p1")
            checkArg(sender, p2s, "p2")
        }
        if (p0s.size != 1 || p3s.size != 1) throw CommandException("");
        if (!isLine)
            if (p1s.size != 1 || p2s.size != 1) throw CommandException("");

        sender.sendMessage(TextComponentTranslation("found p0 at %s", "${p0s.single()}"))
        if (!isLine) {
            sender.sendMessage(TextComponentTranslation("found p1 at %s", "${p1s.single()}"))
            sender.sendMessage(TextComponentTranslation("found p2 at %s", "${p2s.single()}"))
        }
        sender.sendMessage(TextComponentTranslation("found p3 at %s", "${p3s.single()}"))

        return arrayOf(
                Vec3d(p0s.single()).add(.5, .5, .5),
                if (!isLine) Vec3d(p1s.single()).add(.5, .5, .5) else Vec3d.ZERO,
                if (!isLine) Vec3d(p2s.single()).add(.5, .5, .5) else Vec3d.ZERO,
                Vec3d(p3s.single()).add(.5, .5, .5)
        )
    }

    class Parameters(
            val perBlocks: Double,
            val offset: Double,
            val keep: Boolean,
            val isForce: Boolean,
            val isLine: Boolean,
            val isRTM: Boolean
    )
}
