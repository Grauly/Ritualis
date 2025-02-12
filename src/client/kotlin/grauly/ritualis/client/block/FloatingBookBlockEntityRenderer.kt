package grauly.ritualis.client.block

import grauly.ritualis.block.FloatingBookBlockEntity
import grauly.ritualis.client.debug.DebugRenderers
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.render.block.entity.EnchantingTableBlockEntityRenderer
import net.minecraft.client.render.entity.model.BookModel
import net.minecraft.client.render.entity.model.EntityModelLayers
import net.minecraft.client.util.math.MatrixStack
import org.joml.Quaternionf
import kotlin.math.PI

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
        if(!entity.active) {
            matrices.multiply(Quaternionf().rotationX((PI/2).toFloat()))
        }

        //pageTurnAmount: ???
        //leftFlipAmount: left page flip amount from 0 -> 1
        //rightFlipAmount: right page flip amount from 0 -> 1
        //pageTurnSpeed: open/close of book from 0 -> 1
        val pageTurnSpeed = if(entity.active) 1f else 0f
        book.setPageAngles(0f, .5f, 0f, pageTurnSpeed)
        DebugRenderers.debugSpace(matrices, vertexConsumers)
        book.render(matrices, vertexConsumer, light, overlay)
        matrices.pop()
    }
}