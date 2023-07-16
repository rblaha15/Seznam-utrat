package cz.rblaha15.seznamUtrat

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate
import java.time.LocalTime

class LocalTimeSeralizer : KSerializer<LocalTime> {
    override fun deserialize(decoder: Decoder): LocalTime {
        val str = decoder.decodeString()
        val h = str.split(":")[0].replace(" ", "").toInt()
        val m = str.split(":")[1].replace(" ", "").toInt()
        val s = str.split(":")[2].replace(" ", "").toInt()
        return LocalTime.of(h, m, s)
    }

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalTime) {
        encoder.encodeString(value.asString())
    }
}

fun LocalTime.asString() = buildString {
    if ("$hour".length == 1) append(0)
    append(hour)

    append(":")
    if ("$minute".length == 1) append(0)
    append(minute)

    if (second != 0) {
        append(":")
        if ("$second".length == 1) append(0)
        append(second)
    }
}
