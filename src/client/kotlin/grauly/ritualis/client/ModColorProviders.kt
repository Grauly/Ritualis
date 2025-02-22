package grauly.ritualis.client

import grauly.ritualis.ModBlocks
import grauly.ritualis.block.RitualLine
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockRenderView

object ModColorProviders {
    fun init() {
        ColorProviderRegistry.BLOCK.register(
            { state: BlockState,
              view: BlockRenderView?,
              pos: BlockPos?,
              tintIndex: Int ->
                RitualLine.COLORS[state.get(RitualLine.POWER)]
            }, ModBlocks.RITUAL_LINE
        )
    }
}