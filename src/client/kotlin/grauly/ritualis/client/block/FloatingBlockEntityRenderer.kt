package grauly.ritualis.client.block

import grauly.ritualis.block.FloatingBookBlockEntity
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.util.math.MatrixStack

class FloatingBlockEntityRenderer(val ctx: BlockEntityRendererFactory.Context): BlockEntityRenderer<FloatingBookBlockEntity> {
    override fun render(
        entity: FloatingBookBlockEntity?,
        tickDelta: Float,
        matrices: MatrixStack?,
        vertexConsumers: VertexConsumerProvider?,
        light: Int,
        overlay: Int
    ) {
        TODO("Not yet implemented")
    }
}