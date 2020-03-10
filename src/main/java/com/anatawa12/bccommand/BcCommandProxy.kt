package com.anatawa12.bccommand

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderGlobal
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.max
import kotlin.math.min

abstract class BcCommandProxy {
    abstract fun onShareYourRegion(worldId: Int?, first: BlockPos?, second: BlockPos?)
}

class ClientProxy : BcCommandProxy() {
    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent) {
        if (worldId != Minecraft.getMinecraft().world.provider.dimension)
            return
        val player = Minecraft.getMinecraft().player
        val offset = Vec3d(
                player.lastTickPosX + (player.posX - player.lastTickPosX) * event.partialTicks,
                player.lastTickPosY + (player.posY - player.lastTickPosY) * event.partialTicks,
                player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.partialTicks)
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO)
        GlStateManager.glLineWidth(2.0f)
        GlStateManager.disableTexture2D()
        GlStateManager.depthMask(false)
        blockRegionFirstBlock?.let { drawBlockBlock(it, it, offset, 0f, 1f, 0f) }
        blockRegionSecondBlock?.let { drawBlockBlock(it, it, offset, 0f, 0f, 1f) }
        blockRegionFirstBlock?.let { first ->
            blockRegionSecondBlock?.let { second ->
                val min = BlockPos(min(first.x, second.x), min(first.y, second.y), min(first.z, second.z))
                val max = BlockPos(max(first.x, second.x), max(first.y, second.y), max(first.z, second.z))
                drawBlockBlock(min, max, offset, 1f, 0f, 0f)
            }
        }
        GlStateManager.depthMask(true)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    private fun drawBlockBlock(minBlock: BlockPos, maxBlock: BlockPos, offset: Vec3d,
                               red: Float, green: Float, blue: Float) {
        val aabb = AxisAlignedBB(Vec3d(minBlock), Vec3d(maxBlock).add(1.0, 1.0, 1.0))
                .offset(-offset)
        RenderGlobal.drawSelectionBoundingBox(aabb.grow(0.0020000000949949026),
                red, green, blue, 1f)
        RenderGlobal.drawSelectionBoundingBox(aabb.grow(-0.0020000000949949026),
                red, green, blue, 1f)
    }

    override fun onShareYourRegion(worldId: Int?, first: BlockPos?, second: BlockPos?) {
        this.worldId = worldId
        this.blockRegionFirstBlock = first
        this.blockRegionSecondBlock = second
    }

    var blockRegionFirstBlock: BlockPos? = null
    var blockRegionSecondBlock: BlockPos? = null
    var worldId: Int? = null
}

class ServerProxy : BcCommandProxy() {
    override fun onShareYourRegion(worldId: Int?, first: BlockPos?, second: BlockPos?) = error("not impl on server")
}
