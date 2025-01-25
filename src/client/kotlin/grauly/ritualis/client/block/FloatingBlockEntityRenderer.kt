package grauly.ritualis.client.block

import grauly.ritualis.Ritualis
import grauly.ritualis.block.FloatingBookBlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.VertexRendering
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.render.block.entity.EnchantingTableBlockEntityRenderer
import net.minecraft.client.render.entity.model.BookModel
import net.minecraft.client.render.entity.model.EntityModelLayers
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.*

class FloatingBlockEntityRenderer(ctx: BlockEntityRendererFactory.Context) :
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
        val context = entity.renderingContext
        val time = context.ticks + tickDelta
        val positionDelta: Float = Math.clamp(
            (time - context.positionStartTimestamp) / (context.positionEndTimestamp - context.positionStartTimestamp),
            0f,
            1f
        )
        val lookAtDelta: Float = Math.clamp(
            (time - context.lookStartTimestamp) / (context.lookEndTimestamp - context.lookStartTimestamp),
            0f,
            1f
        )

        val position = context.previousTargetPosition.lerp(context.targetPosition, easeInOutSine(positionDelta).toDouble())
        val offset = position.subtract(.5,.5,.5)

        entity.bookRotationHandler.handleOffset(offset)
        entity.bookRotationHandler.partialTick(tickDelta)

        matrices.push()
        matrices.translate(position)
        matrices.multiply(entity.bookRotationHandler.currentRotation)

        //pageTurnAmount: ???
        //leftFlipAmount: left page flip amount from 0 -> 1
        //rightFlipAmount: right page flip amount from 0 -> 1
        //pageTurnSpeed: open/close of book from 0 -> 1
        book.setPageAngles(0f, .5f, 0f, 1f)
        book.render(matrices, vertexConsumer, light, overlay)
        matrices.pop()
    }

    companion object {
        private val BOOK_ROTATION_OFFSET = Quaternionf().rotationY((PI / 2).toFloat())
    }

    private fun easeInOutCubic(x: Float): Float {
        return if(x < 0.5f) {4f * x * x * x} else {1 - (-2 * x + 2).pow(3) / 2f};
    }

    private fun easeInOutSine(x: Float): Float {
        return (-(cos(Math.PI * x) - 1) / 2).toFloat();
    }

    private fun lookDirectionToQuaternion(lookDirection: Vec3d): Quaternionf =
        Quaternionf().lookAlong(lookDirection.normalize().toVector3f(), Direction.UP.doubleVector.toVector3f())
            .invert()
            .mul(BOOK_ROTATION_OFFSET)


    private fun visualizeQuaternion(
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        quaternion: Quaternionf,
        color: Int = (0xFFFF0000.toInt())
    ) {
        val angle = acos(quaternion.w).toDouble()
        val quaternionAxis =
            Vec3d(quaternion.x.toDouble(), quaternion.y.toDouble(), quaternion.z.toDouble()).multiply(1 / sin(angle))
        VertexRendering.drawVector(
            matrices,
            vertexConsumers.getBuffer(RenderLayer.LINES),
            Vector3f(.5f, .5f, .5f),
            quaternionAxis,
            color
        )
        val textPoint = Vec3d(.5, .5, .5).add(quaternionAxis.multiply(1.2))

        putDebugText(
            "${Math.toDegrees(angle * 2)}",
            textPoint,
            matrices,
            vertexConsumers
        )
    }

    private fun putDebugText(
        text: String,
        offset: Vec3d,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        color: Int = 0xFFFFFFFF.toInt()
    ) {
        matrices.push()
        matrices.translate(offset)
        matrices.multiply(MinecraftClient.getInstance().gameRenderer.camera.rotation)
        matrices.scale(0.02f, -0.02f, 0.02f)
        MinecraftClient.getInstance().textRenderer.draw(
            text,
            .0f,
            .0f,
            color,
            false,
            matrices.peek().positionMatrix,
            vertexConsumers,
            TextRenderer.TextLayerType.SEE_THROUGH,
            0,
            15728880
        )
        matrices.pop()
    }

    private fun debugSpace(matrices: MatrixStack, vertexConsumers: VertexConsumerProvider) {
        VertexRendering.drawVector(
            matrices,
            vertexConsumers.getBuffer(RenderLayer.LINES),
            Vector3f(.0f, .0f, .0f),
            Vec3d(1.0, .0, .0),
            0xFFFF0000.toInt()
        )
        VertexRendering.drawVector(
            matrices,
            vertexConsumers.getBuffer(RenderLayer.LINES),
            Vector3f(.0f, .0f, .0f),
            Vec3d(.0, 1.0, .0),
            0xFF00FF00.toInt()
        )
        VertexRendering.drawVector(
            matrices,
            vertexConsumers.getBuffer(RenderLayer.LINES),
            Vector3f(.0f, .0f, .0f),
            Vec3d(.0, .0, 1.0),
            0xFF0000FF.toInt()
        )
    }
}