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

class IgnitionParticleEffect(destination: PositionSource?, arrivalInTicks: Int) : VibrationParticleEffect(
    destination,
    arrivalInTicks
) {
    companion object {
        private val POSITION_SOURCE_CODEC: Codec<PositionSource> = PositionSource.CODEC
            .validate { positionSource: PositionSource ->
                if (positionSource is EntityPositionSource) DataResult.error { "Entity position sources are not allowed" }
                else DataResult.success(positionSource)
            }
        val CODEC: MapCodec<IgnitionParticleEffect> =
            RecordCodecBuilder.mapCodec { instance: RecordCodecBuilder.Instance<IgnitionParticleEffect> ->
                instance.group(
                    POSITION_SOURCE_CODEC.fieldOf("destination").forGetter(IgnitionParticleEffect::getVibration),
                    Codec.INT.fieldOf("arrival_in_ticks").forGetter(IgnitionParticleEffect::getArrivalInTicks)
                ).apply(instance, ::IgnitionParticleEffect)
            }
        val PACKET_CODEC: PacketCodec<RegistryByteBuf, IgnitionParticleEffect> = PacketCodec.tuple(
            PositionSource.PACKET_CODEC,
            IgnitionParticleEffect::getVibration,
            PacketCodecs.VAR_INT,
            IgnitionParticleEffect::getArrivalInTicks,
            ::IgnitionParticleEffect
        )
    }
}