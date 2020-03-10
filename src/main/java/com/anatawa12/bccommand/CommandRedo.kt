package com.anatawa12.bccommand

import net.minecraft.command.CommandBase
import net.minecraft.command.CommandException
import net.minecraft.command.ICommandSender
import net.minecraft.server.MinecraftServer
import net.minecraft.util.text.TextComponentTranslation

object CommandRedo : CommandBase() {
    override fun getName(): String = "//redo"

    override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<String>) {
        val redo = PlayerDataStorage.redo(getCommandSenderAsPlayer(sender))
                ?: throw CommandException("you did not undo(with ///undo) so you cannot redo")
        redo()
        sender.sendMessage(TextComponentTranslation("redid"))
    }

    override fun getUsage(sender: ICommandSender): String = "///undo"
}
