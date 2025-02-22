package grauly.ritualis.client

import grauly.ritualis.ModBlocks
import grauly.ritualis.block.RitualLine
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockRenderView
import java.awt.Color

object ModColorProviders {
    fun init() {
        ColorProviderRegistry.BLOCK.register(
            { state: BlockState,
              view: BlockRenderView?,
              pos: BlockPos?,
              tintIndex: Int ->
                state.get(RitualLine.POWER) * Color(0, 200, 255).rgb
            }, ModBlocks.RITUAL_LINE
        )
    }
}