package grauly.ritualis.client

import net.fabricmc.api.ClientModInitializer

class RitualisClient : ClientModInitializer {

    override fun onInitializeClient() {
        ModClientParticles.init()
        ModBlockEntityRenderers.init()
        ModColorProviders.init()
    }
}
