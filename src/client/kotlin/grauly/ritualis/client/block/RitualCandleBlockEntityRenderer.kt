package grauly.ritualis.client.block

import grauly.ritualis.Ritualis
import grauly.ritualis.block.RitualCandleBlockEntity
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.util.math.MatrixStack

class RitualCandleBlockEntityRenderer(val ctx: BlockEntityRendererFactory.Context): BlockEntityRenderer<RitualCandleBlockEntity> {
    override fun render(
        entity: RitualCandleBlockEntity,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        overlay: Int
    ) {
        matrices.push()
        matrices.pop()
    }
}