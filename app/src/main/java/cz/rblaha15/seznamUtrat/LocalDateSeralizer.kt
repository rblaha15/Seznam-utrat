package cz.rblaha15.seznamUtrat

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate

class LocalDateSeralizer : KSerializer<LocalDate> {
    override fun deserialize(decoder: Decoder): LocalDate {
        val str = decoder.decodeString()
        val mesic = str.split(".")[1].replace(" ", "").toInt()
        val den = str.split(".")[0].replace(" ", "").toInt()
        return LocalDate.of(LocalDate.now().year, mesic, den)
    }

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDate) {
        encoder.encodeString(value.asString())
    }
}

fun LocalDate.asString() = "$dayOfMonth. $monthValue."