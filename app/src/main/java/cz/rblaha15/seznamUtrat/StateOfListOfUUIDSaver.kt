package cz.rblaha15.seznamUtrat

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import kotlinx.serialization.Serializable
import java.util.UUID

object StateOfListOfUUIDSaver :
    Saver<MutableState<List<@Serializable(with = UUIDSerializer::class) UUID>>, String> {

    override fun restore(value: String) =
        mutableStateOf(value.fromJson<List<@Serializable(with = UUIDSerializer::class) UUID>>())

    override fun SaverScope.save(value: MutableState<List<@Serializable(with = UUIDSerializer::class) UUID>>) =
        value.value.toJson()
}
