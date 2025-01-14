package grauly.ritualis.client.block

import grauly.ritualis.block.FloatingBookBlockEntity
import grauly.ritualis.extensions.toSpherical
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
        val bookRotationOffset = quaternionAroundAxisAngle(Vec3d(.0,.0,1.0), 90f)
        matrices.push()

        matrices.translate(context.lastTargetPosition.lerp(context.targetPosition, tickDelta.toDouble()))
        matrices.multiply(context.lastTargetRotation.nlerp(context.targetRotation, tickDelta))

        //pageTurnAmount: ???
        //leftFlipAmount: left page flip amount from 0 -> 1
        //rightFlipAmount: right page flip amount from 0 -> 1
        //pageTurnSpeed: open/close of book from 0 -> 1
        book.setPageAngles(0f, .5f, 0f, 1f)
        book.render(matrices, vertexConsumer, light, overlay)
        matrices.pop()
    }

    fun visualizeQuaternion(
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

    fun putDebugText(text: String, offset: Vec3d, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, color: Int = 0xFFFFFFFF.toInt()) {
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

    fun debugSpace(matrices: MatrixStack, vertexConsumers: VertexConsumerProvider) {
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

    fun quaternionAroundAxisAngle(axis: Vec3d, angle: Float): Quaternionf {
        var radiansAngle = Math.toRadians(angle.toDouble()) / 2
        /*
                while(radiansAngle > 2* PI) radiansAngle -= PI
                while(radiansAngle < 0) radiansAngle += PI
        */
        val scaledAxis = axis.normalize().multiply(sin(radiansAngle)).toVector3f()
        return Quaternionf(scaledAxis.x, scaledAxis.y, scaledAxis.z, cos(radiansAngle).toFloat())
    }

    /*
        fun calculateRotationQuaternion(oldFacing: Vec3d, newFacing: Vec3d, tickDelta: Float): Quaternionf {
        }
    */
}