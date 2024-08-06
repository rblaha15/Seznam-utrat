package cz.rblaha15.seznamUtrat.ui.savers

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import java.time.LocalTime

object StateOfLocalTimeSaver : Saver<MutableState<LocalTime>, Long> {
    override fun restore(value: Long): MutableState<LocalTime> = mutableStateOf(LocalTime.ofSecondOfDay(value))
    override fun SaverScope.save(value: MutableState<LocalTime>) = value.value.toSecondOfDay().toLong()
}