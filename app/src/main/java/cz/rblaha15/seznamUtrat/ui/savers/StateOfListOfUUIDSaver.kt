package cz.rblaha15.seznamUtrat.ui.savers

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import java.util.UUID

object StateOfListOfUUIDSaver :
    Saver<MutableState<List<UUID>>, List<String>> {

    override fun restore(value: List<String>) =
        mutableStateOf(value.map { UUID.fromString(it) })

    override fun SaverScope.save(value: MutableState<List<UUID>>) =
        value.value.map { it.toString() }
}
