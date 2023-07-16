package cz.rblaha15.seznamUtrat

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope

object StateOfNulableUtrataSaver : Saver<MutableState<Utrata?>, String> {
    override fun restore(value: String) = mutableStateOf(value.fromJson<Utrata?>())
    override fun SaverScope.save(value: MutableState<Utrata?>) = value.value.toJson()
}
