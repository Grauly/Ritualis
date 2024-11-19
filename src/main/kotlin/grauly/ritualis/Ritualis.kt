package grauly.ritualis

import net.fabricmc.api.ModInitializer
import net.minecraft.util.DyeColor
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Ritualis : ModInitializer {

    override fun onInitialize() {
        ModEvents.init()
        ModBlocks.init()
        ModItems.init()
        ModBlockEntities.init()
    }

    companion object {
        const val MODID: String = "ritualis"
        val LOGGER: Logger = LoggerFactory.getLogger(MODID)
        val COLOR_ORDER = listOf(
            DyeColor.WHITE,
            DyeColor.LIGHT_GRAY,
            DyeColor.GRAY,
            DyeColor.BLACK,
            DyeColor.BROWN,
            DyeColor.RED,
            DyeColor.ORANGE,
            DyeColor.YELLOW,
            DyeColor.LIME,
            DyeColor.GREEN,
            DyeColor.CYAN,
            DyeColor.LIGHT_BLUE,
            DyeColor.PURPLE,
            DyeColor.MAGENTA,
            DyeColor.PINK
        )
    }
}
