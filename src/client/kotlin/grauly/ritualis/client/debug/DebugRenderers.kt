package grauly.ritualis.client.debug

import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.VertexRendering
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Vec3d
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.acos
import kotlin.math.sin

object DebugRenderers {

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

    fun putDebugText(
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
}