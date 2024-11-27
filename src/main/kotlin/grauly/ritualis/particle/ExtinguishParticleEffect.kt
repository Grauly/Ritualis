package grauly.ritualis.particle

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.particle.VibrationParticleEffect
import net.minecraft.world.event.EntityPositionSource
import net.minecraft.world.event.PositionSource

class ExtinguishParticleEffect(destination: PositionSource?, arrivalInTicks: Int) : VibrationParticleEffect(
    destination,
    arrivalInTicks
) {
    companion object {
        private val POSITION_SOURCE_CODEC: Codec<PositionSource> = PositionSource.CODEC
            .validate { positionSource: PositionSource ->
                if (positionSource is EntityPositionSource) DataResult.error { "Entity position sources are not allowed" }
                else DataResult.success(positionSource)
            }
        val CODEC: MapCodec<ExtinguishParticleEffect> =
            RecordCodecBuilder.mapCodec { instance: RecordCodecBuilder.Instance<ExtinguishParticleEffect> ->
                instance.group(
                    POSITION_SOURCE_CODEC.fieldOf("destination").forGetter(ExtinguishParticleEffect::getVibration),
                    Codec.INT.fieldOf("arrival_in_ticks").forGetter(ExtinguishParticleEffect::getArrivalInTicks)
                ).apply(instance, ::ExtinguishParticleEffect)
            }
        val PACKET_CODEC: PacketCodec<RegistryByteBuf, ExtinguishParticleEffect> = PacketCodec.tuple(
            PositionSource.PACKET_CODEC,
            ExtinguishParticleEffect::getVibration,
            PacketCodecs.VAR_INT,
            ExtinguishParticleEffect::getArrivalInTicks,
            ::ExtinguishParticleEffect
        )
    }
}