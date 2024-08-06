package cz.rblaha15.seznamUtrat.ui.savers

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import cz.rblaha15.seznamUtrat.data.Utrata
import cz.rblaha15.seznamUtrat.data.fromJson
import cz.rblaha15.seznamUtrat.data.toJson

object StateOfNulableUtrataSaver : Saver<MutableState<Utrata?>, String> {
    override fun restore(value: String) = mutableStateOf(value.fromJson<Utrata?>())
    override fun SaverScope.save(value: MutableState<Utrata?>) = value.value.toJson()
}
