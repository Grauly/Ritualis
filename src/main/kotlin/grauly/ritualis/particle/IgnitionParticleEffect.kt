package grauly.ritualis.particle

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import grauly.ritualis.ModParticles
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.particle.ParticleEffect
import net.minecraft.particle.ParticleType
import net.minecraft.util.math.Vec3d

class IgnitionParticleEffect(val target: Vec3d, val arrivalInTicks: Int) : ParticleEffect
{
    override fun getType(): ParticleType<IgnitionParticleEffect> = ModParticles.IGNITION

    companion object {
        val CODEC: MapCodec<IgnitionParticleEffect> =
            RecordCodecBuilder.mapCodec { instance: RecordCodecBuilder.Instance<IgnitionParticleEffect> ->
                instance.group(
                    Vec3d.CODEC.fieldOf("target").forGetter(IgnitionParticleEffect::target),
                    Codec.INT.fieldOf("arrival_in_ticks").forGetter(IgnitionParticleEffect::arrivalInTicks)
                ).apply(instance, ::IgnitionParticleEffect)
            }
        val PACKET_CODEC: PacketCodec<RegistryByteBuf, IgnitionParticleEffect> = PacketCodec.tuple(
            Vec3d.PACKET_CODEC,
            IgnitionParticleEffect::target,
            PacketCodecs.VAR_INT,
            IgnitionParticleEffect::arrivalInTicks,
            ::IgnitionParticleEffect
        )
    }
}