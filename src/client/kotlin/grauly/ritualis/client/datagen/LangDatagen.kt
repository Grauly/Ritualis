package grauly.ritualis.client.datagen

import grauly.ritualis.ModBlocks
import grauly.ritualis.Ritualis
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider
import net.minecraft.block.Block
import net.minecraft.registry.RegistryWrapper.WrapperLookup
import java.util.*
import java.util.concurrent.CompletableFuture

class LangDatagen(output: FabricDataOutput, future: CompletableFuture<WrapperLookup>) :
    FabricLanguageProvider(output, future) {

    override fun generateTranslations(wrapper: WrapperLookup, builder: TranslationBuilder) {
        generateLangForCandle(ModBlocks.candles[0], builder)
        for (i: Int in 1..Ritualis.COLOR_ORDER.size) {
            val color = Ritualis.COLOR_ORDER[i - 1]
            val prefix = convertColorStringToDisplayString(color.getName())
            generateLangForCandle(ModBlocks.candles[i], builder, prefix)
        }
    }

    private fun convertColorStringToDisplayString(color: String): String =
        color.split("_")
            .map { s -> s.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } }
            .reduceRight { s, acc -> "$s $acc" }
            .plus(" ")

    private fun generateLangForCandle(candle: Block, builder: TranslationBuilder, colorPrefix: String = "") {
        val name = "${colorPrefix}Ritual Candle"
        builder.add(candle, name)
    }
}