package com.anatawa12.bccommand

import com.anatawa12.bccommand.BcCommandCore.Companion.MOD_ID
import net.minecraft.item.Item
import net.minecraft.util.ResourceLocation

object SelectionWand : Item() {
    const val ID = "selection_wand"

    init {
        registryName = ResourceLocation(MOD_ID, ID)
        translationKey = "$MOD_ID.$ID"
    }
}
