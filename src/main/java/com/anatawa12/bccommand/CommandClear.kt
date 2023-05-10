package com.anatawa12.bccommand

import net.minecraft.command.CommandBase
import net.minecraft.command.CommandException
import net.minecraft.command.ICommandSender
import net.minecraft.server.MinecraftServer
import net.minecraft.util.text.TextComponentTranslation

object CommandClear : CommandBase() {
    override fun getName(): String = BcCommandCore.commandName("clear")

    override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<String>) {
        PlayerDataStorage.clearSelection(getCommandSenderAsPlayer(sender))
        sender.sendMessage(TextComponentTranslation("cleared"))
    }

    override fun getUsage(sender: ICommandSender): String = BcCommandCore.commandUsage("clear")
}
