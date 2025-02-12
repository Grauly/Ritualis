package grauly.ritualis.client

import grauly.ritualis.ModBlockEntities
import grauly.ritualis.client.block.FloatingBookEntityRenderer
import grauly.ritualis.client.block.RitualCandleBlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories

object ModBlockEntityRenderers {
    fun init() {
        BlockEntityRendererFactories.register(ModBlockEntities.RITUAL_CANDLE_ENTITY, ::RitualCandleBlockEntityRenderer)
        BlockEntityRendererFactories.register(ModBlockEntities.FLOATING_BOOK_ENTITY, ::FloatingBookEntityRenderer)
    }
}