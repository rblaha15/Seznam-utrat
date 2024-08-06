package cz.rblaha15.seznamUtrat.ui.savers

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import java.time.LocalDate

object StateOfLocalDateSaver : Saver<MutableState<LocalDate>, Long> {
    override fun restore(value: Long): MutableState<LocalDate> = mutableStateOf(LocalDate.ofEpochDay(value))
    override fun SaverScope.save(value: MutableState<LocalDate>) = value.value.toEpochDay()
}