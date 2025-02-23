package grauly.ritualis.client

import grauly.ritualis.ModBlocks
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.minecraft.client.render.RenderLayer

object ModRenderLayerRegistering {
    fun init() {
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.RITUAL_LINE, RenderLayer.getCutout())
    }
}