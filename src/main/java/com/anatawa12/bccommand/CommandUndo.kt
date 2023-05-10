package com.anatawa12.bccommand

import net.minecraft.command.CommandBase
import net.minecraft.command.CommandException
import net.minecraft.command.ICommandSender
import net.minecraft.server.MinecraftServer
import net.minecraft.util.text.TextComponentTranslation

object CommandUndo : CommandBase() {
    override fun getName(): String = BcCommandCore.commandName("undo")

    override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<String>) {
        val undo = PlayerDataStorage.undo(getCommandSenderAsPlayer(sender))
                ?: throw CommandException("you did not do something so you cannot undo")
        undo.asReversed().forEach {
            it.world.restoringBlockSnapshots = true
            it.restore(true)
            it.world.restoringBlockSnapshots = false
        }
        sender.sendMessage(TextComponentTranslation("undid"))
    }

    override fun getUsage(sender: ICommandSender): String = BcCommandCore.commandUsage("undo")
}
