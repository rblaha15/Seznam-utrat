package cz.rblaha15.seznamUtrat

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.Composable

fun interface GenericActivityResultLauncher<I, O> {
    fun launch(input: I, callback: ActivityResultCallback<O>)
}

@Composable
@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE", "ObjectLiteralToLambda")
fun <I, O> rememberResultLauncher(contract: ActivityResultContract<I, O>): GenericActivityResultLauncher<I, O> {
    val launcher = rememberLauncherForActivityResult(contract) {
        val cb = callbacks[i] as ActivityResultCallback<O>
        cb.onActivityResult(it)
    }

    return object : GenericActivityResultLauncher<I, O> {
        override inline fun launch(input: I, callback: ActivityResultCallback<O>) {
            i = callbacks.size
            callbacks.add(callback as ActivityResultCallback<*>)
            launcher.launch(input)
        }
    }
}

var i = 0
val callbacks = mutableListOf<ActivityResultCallback<*>>()