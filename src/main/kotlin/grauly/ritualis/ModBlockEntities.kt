package grauly.ritualis

import grauly.ritualis.block.RitualCandleBlockEntity
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier


object ModBlockEntities {
    val RITUAL_CANDLE_ENTITY = register(::RitualCandleBlockEntity, "ritual_candle", *ModBlocks.candles.toTypedArray())

    private fun <T : BlockEntity> register(
        blockEntity: FabricBlockEntityTypeBuilder.Factory<T>,
        id: String,
        vararg blocks: Block
    ): BlockEntityType<T> {
        return Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            Identifier.of(Ritualis.MODID, id),
            FabricBlockEntityTypeBuilder.create(blockEntity).addBlocks(*blocks).build()
        )
    }

    fun init() { }

}