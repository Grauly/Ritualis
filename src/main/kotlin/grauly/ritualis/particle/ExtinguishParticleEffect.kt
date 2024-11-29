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

class ExtinguishParticleEffect(val target: Vec3d, val arrivalInTicks: Int) : ParticleEffect {
    override fun getType(): ParticleType<*> = ModParticles.EXTINGUISH

    companion object {
        val CODEC: MapCodec<ExtinguishParticleEffect> =
            RecordCodecBuilder.mapCodec { instance: RecordCodecBuilder.Instance<ExtinguishParticleEffect> ->
                instance.group(
                    Vec3d.CODEC.fieldOf("target").forGetter(ExtinguishParticleEffect::target),
                    Codec.INT.fieldOf("arrival_in_ticks").forGetter(ExtinguishParticleEffect::arrivalInTicks)
                ).apply(instance, ::ExtinguishParticleEffect)
            }
        val PACKET_CODEC: PacketCodec<RegistryByteBuf, ExtinguishParticleEffect> = PacketCodec.tuple(
            Vec3d.PACKET_CODEC,
            ExtinguishParticleEffect::target,
            PacketCodecs.VAR_INT,
            ExtinguishParticleEffect::arrivalInTicks,
            ::ExtinguishParticleEffect
        )
    }
}