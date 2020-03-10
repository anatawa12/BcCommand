package com.anatawa12.bccommand

import com.anatawa12.bccommand.BcCommandCore.Companion.MOD_ID
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper
import net.minecraftforge.fml.relauncher.Side

object Network {
    private val channel = SimpleNetworkWrapper(MOD_ID)

    fun init() {
        channel.registerMessage(ShareYourRegion, ShareYourRegion::class.java, 0x00, Side.CLIENT)
    }

    fun sendToClient(playerMP: EntityPlayerMP, packet: IMessage) {
        channel.sendTo(packet, playerMP)
    }
}
