package sh.nes.combattle.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Duration

internal object DurationSerializer : KSerializer<Duration> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("sh.nes.wrightful.util.DurationSerializer", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Duration {
        val raw = decoder.decodeString()
        return Duration.parse(raw)
    }

    override fun serialize(encoder: Encoder, value: Duration) {
        encoder.encodeString(value.toString())
    }
}
