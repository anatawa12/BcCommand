package com.anatawa12.bccommand

import io.netty.buffer.ByteBuf
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

class ShareYourRegion : IMessage {
    @Deprecated("", level = DeprecationLevel.HIDDEN)
    constructor()

    private var worldId: Int? = null
    private var first: BlockPos? = null
    private var second: BlockPos? = null

    constructor(worldId: Int?, first: BlockPos?, second: BlockPos?) {
        this.worldId = worldId
        this.first = first
        this.second = second
    }

    override fun toBytes(buf: ByteBuf) {
        val worldId = worldId
        val first = first
        val second = second
        var firstByte = 0
        if (worldId == null)
            firstByte += WORLD_NULL
        if (first == null)
            firstByte += FIRST_NULL
        if (second == null)
            firstByte += SECOND_NULL
        buf.writeByte(firstByte)

        if (worldId != null) {
            buf.writeInt(worldId)
        }
        if (first != null) {
            buf.writeInt(first.x)
            buf.writeByte(first.y)
            buf.writeInt(first.z)
        }
        if (second != null) {
            buf.writeInt(second.x)
            buf.writeByte(second.y)
            buf.writeInt(second.z)
        }
    }

    override fun fromBytes(buf: ByteBuf) {
        val firstByte = buf.readByte().toInt()
        if (firstByte and WORLD_NULL == 0) {
            worldId = buf.readInt()
        }
        if (firstByte and FIRST_NULL == 0) {
            first = BlockPos(buf.readInt(), buf.readByte().toInt(), buf.readInt())
        }
        if (firstByte and SECOND_NULL == 0) {
            second = BlockPos(buf.readInt(), buf.readByte().toInt(), buf.readInt())
        }
    }

    companion object : IMessageHandler<ShareYourRegion, Nothing?> {
        const val WORLD_NULL = 0x01
        const val FIRST_NULL = 0x02
        const val SECOND_NULL = 0x04
        override fun onMessage(message: ShareYourRegion, ctx: MessageContext): Nothing? {
            BcCommandCore.proxy.onShareYourRegion(message.worldId, message.first, message.second)
            return null
        }
    }
}
