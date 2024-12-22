package grauly.ritualis

import net.minecraft.block.Block
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Identifier

object ModBlockTags {
    val RITUAL_CANDLES: TagKey<Block> = createTag("ritual_candles")

    private fun createTag(path: String): TagKey<Block> =
        TagKey.of(RegistryKeys.BLOCK, Identifier.of(Ritualis.MODID, path))
}