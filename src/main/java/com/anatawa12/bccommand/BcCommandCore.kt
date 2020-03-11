package com.anatawa12.bccommand

import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.item.Item
import net.minecraft.util.text.TextComponentTranslation
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.SidedProxy
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.event.FMLServerStartingEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.PlayerEvent
import net.minecraftforge.fml.relauncher.Side

@Mod(modid = BcCommandCore.MOD_ID)
class BcCommandCore {
    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        MinecraftForge.EVENT_BUS.register(this)
        MinecraftForge.EVENT_BUS.register(proxy)
        Network.init()
        if (event.side == Side.CLIENT) {
            ModelLoader.setCustomModelResourceLocation(SelectionWand, 0, ModelResourceLocation("minecraft:wooden_axe#inventory"))
        }
    }

    @Mod.EventHandler
    fun onServerStarting(event: FMLServerStartingEvent) {
        event.registerServerCommand(CommandBc)
        event.registerServerCommand(CommandRedo)
        event.registerServerCommand(CommandUndo)
        event.registerServerCommand(CommandClear)
    }

    @SubscribeEvent
    fun registerItems(event: RegistryEvent.Register<Item>) {
        event.registry.register(SelectionWand)
    }

    @SubscribeEvent
    fun onRightClickBlock(event: PlayerInteractEvent.RightClickBlock) {
        if (event.itemStack.item != SelectionWand) return
        event.isCanceled = true
        val playerMP = event.entityPlayer as? EntityPlayerMP
        if (playerMP != null) {
            PlayerDataStorage.setFirstPos(playerMP, event.world, event.pos)
            event.entityPlayer.sendMessage(TextComponentTranslation("first position is set to %s", "${event.pos}"))
        }
    }

    @SubscribeEvent
    fun onLeftClickBlock(event: PlayerInteractEvent.LeftClickBlock) {
        if (event.itemStack.item != SelectionWand) return
        event.isCanceled = true
        val playerMP = event.entityPlayer as? EntityPlayerMP
        if (playerMP != null) {
            PlayerDataStorage.setSecondPos(playerMP, event.world, event.pos)
            event.entityPlayer.sendMessage(TextComponentTranslation("second position is set to %s", "${event.pos}"))
        }
    }

    @SubscribeEvent
    fun onPlayerLoggedIn(event: PlayerEvent.PlayerLoggedInEvent) {
        PlayerDataStorage.shareRegionData(event.player as EntityPlayerMP)
    }

    companion object {
        @JvmStatic @SidedProxy(clientSide = "com.anatawa12.bccommand.ClientProxy", serverSide = "com.anatawa12.bccommand.ServerProxy")
        lateinit var proxy: BcCommandProxy

        const val MOD_ID = "bc-command"
    }
}
