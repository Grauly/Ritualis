package grauly.ritualis.client.block

import grauly.ritualis.block.FloatingBookBlockEntity
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.render.block.entity.EnchantingTableBlockEntityRenderer
import net.minecraft.client.render.entity.model.BookModel
import net.minecraft.client.render.entity.model.EntityModelLayers
import net.minecraft.client.util.math.MatrixStack

class FloatingBookBlockEntityRenderer(ctx: BlockEntityRendererFactory.Context) :
    BlockEntityRenderer<FloatingBookBlockEntity> {

    private val book = BookModel(ctx.getLayerModelPart(EntityModelLayers.BOOK))

    //NOTE: There is only ONE instance of this thing, shared across all instances of the BE

    override fun render(
        entity: FloatingBookBlockEntity,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        overlay: Int
    ) {
        val vertexConsumer = EnchantingTableBlockEntityRenderer.BOOK_TEXTURE.getVertexConsumer(
            vertexConsumers,
            RenderLayer::getEntitySolid
        )

        val context = entity.renderingContext;

        val time = context.ticks + tickDelta
        val actualDeltaTime = time - context.lastTime
        context.lastTime = time

        context.bookPositionHandler.partialTick(actualDeltaTime.toDouble())
        val position = context.bookPositionHandler.getCurrentPosition()
        val offset = position.subtract(.5, .5, .5)

        context.bookRotationHandler.handleOffset(offset)
        context.bookRotationHandler.partialTick(actualDeltaTime)

        matrices.push()
        matrices.translate(position)
        matrices.multiply(context.bookRotationHandler.getRotation())

        //pageTurnAmount: ???
        //leftFlipAmount: left page flip amount from 0 -> 1
        //rightFlipAmount: right page flip amount from 0 -> 1
        //pageTurnSpeed: open/close of book from 0 -> 1
        book.setPageAngles(0f, .5f, 0f, 1f)
        book.render(matrices, vertexConsumer, light, overlay)
        matrices.pop()
    }
}