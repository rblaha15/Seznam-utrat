package cz.rblaha15.seznamUtrat.ui.seznam

import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import cz.rblaha15.seznamUtrat.GenericActivityResultLauncher
import cz.rblaha15.seznamUtrat.data.Razeni
import cz.rblaha15.seznamUtrat.data.Ucastnik
import cz.rblaha15.seznamUtrat.data.Utrata
import cz.rblaha15.seznamUtrat.data.cloveka
import cz.rblaha15.seznamUtrat.data.serializers.asString
import cz.rblaha15.seznamUtrat.toString
import java.io.OutputStream
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

fun vygenerovatPDF(
    seznamUcastniku: List<Ucastnik>,
    seznamUtrat: List<Utrata>,
    mena: String,
    nazevAkce: String,
    launcher: GenericActivityResultLauncher<String, Uri?>,
    useOutputStream: Uri.((OutputStream) -> Unit) -> Unit,
) {
    launcher.launch(nazevAkce) { uri ->
        if (uri == null) return@launch
        val text = """
    |${nazevAkce}
    |
    |Celkem utraceno: ${seznamUtrat.sumOf { it.cena.toDouble() }.toString(2)} $mena
    |
    |Útraty za jednotlivé účastníky:
    ${
            seznamUcastniku
                .sortedByDescending {
                    seznamUtrat.cloveka(it.id).sumOf { utrata -> utrata.cena.toDouble() }.toString(2)
                }
                .joinToString("\n") { ucastnik ->
                    "|    ${ucastnik.jmeno} – ${
                        seznamUtrat.cloveka(ucastnik.id).sumOf { (it.cena.toDouble() / it.ucastnici.size) }.toString(2)
                    } $mena"
                }
        }
    |
    |Seznam útrat:
    ${
            seznamUtrat.sortedWith(Razeni.Datum2)
                .joinToString("\n") { utrata ->
                    val ucastnici =
                        if (utrata.ucastnici != seznamUcastniku.map { it.id })
                            " – pouze ${
                                seznamUcastniku.filter { it.id in utrata.ucastnici }.joinToString { it.jmeno }
                            }"
                        else ""

                    with(utrata) {
                        "|    ${datum.asString()} – ${
                            cena.toDouble().toString(2)
                        } $mena – $nazev$ucastnici"
                    }
                }
        }
    |
    |""".trimMargin(marginPrefix = "|")

        println(uri)
        print(text)

        PdfDocument().use { doc ->
            text
                .lines()
                .flatMapIndexed { index: Int, line: String ->
                    val paint = Paint().apply { if (index == 0 || index == 2) textSize = 20F }

                    line.splitToLines(paint, PRINTABLE_WIDTH.toFloat()).map { paint to it }
                }
                .chunkedBySum(
                    maxWindowSize = PRINTABLE_HEIGHT.toFloat(),
                    initialWindowSize = VERTICAL_PADDING.toFloat(),
                    getElementSize = { (paint, _) ->
                        paint.descent() - paint.ascent()
                    },
                )
                .forEachIndexed { pageIndex, linesOnPage ->

                    val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex + 1).create()

                    doc.startAndUsePage(pageInfo) { page ->
                        linesOnPage.fold(VERTICAL_PADDING.toFloat()) { y, (paint, line) ->

                            val baselineY = y - paint.ascent()
                            page.canvas.drawText(line, HORIZONTAL_PADDING.toFloat(), baselineY, paint)

                            baselineY + paint.descent()
                        }
                    }
                }

            uri.useOutputStream {
                doc.writeTo(it)
            }
        }
    }
}

inline fun <T, R : Comparable<R>> List<T>.chunkedByFold(
    maxWindowSize: R,
    initialWindowSize: R,
    updateWindowSize: (currentWindowSize: R, T) -> R,
): List<List<T>> {
    val result = mutableListOf<List<T>>()
    var currentWindowSize = initialWindowSize
    var startIndex = 0

    for ((index, element) in this.withIndex()) {
        currentWindowSize = updateWindowSize(currentWindowSize, element)
        if (currentWindowSize > maxWindowSize) {
            result.add(subList(startIndex, index))
            startIndex = index
            currentWindowSize = updateWindowSize(initialWindowSize, element)
        }
    }

    if (startIndex < this.size) {
        result.add(subList(startIndex, this.size))
    }

    return result
}

inline fun <T> List<T>.chunkedBySum(
    maxWindowSize: Float,
    initialWindowSize: Float,
    getElementSize: (T) -> Float,
) = chunkedByFold(
    maxWindowSize = maxWindowSize,
    initialWindowSize = initialWindowSize,
    updateWindowSize = { currentWindowSize, element ->
        currentWindowSize + getElementSize(element)
    },
)

fun String.splitToLines(paint: Paint, lineWidth: Float) = splitToLengths { zbyva ->
    val vejdeSe = paint.breakText(zbyva, true, lineWidth, null)

    if (vejdeSe < zbyva.length) {
        val pozicePosledniMezery = zbyva.take(vejdeSe).indexOfLast { "\\s".toRegex().matches(it.toString()) }

        if (pozicePosledniMezery != -1) return@splitToLengths pozicePosledniMezery
    }

    vejdeSe
}

fun String.splitToLengths(getNextLength: (remaining: String) -> Int): List<String> {
    if (isEmpty()) return listOf("")
    var remaining = this
    val result = mutableListOf<String>()
    do {
        val nextLength = getNextLength(remaining)
        result.add(remaining.take(nextLength))
        remaining = remaining.drop(nextLength)
    } while (remaining.isNotEmpty())
    return result
}

@OptIn(ExperimentalContracts::class)
inline fun <T> PdfDocument.use(block: (PdfDocument) -> T): T {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    try {
        return block(this)
    } catch (e: Throwable) {
        throw e
    } finally {
        close()
    }
}

@OptIn(ExperimentalContracts::class)
inline fun <T> PdfDocument.usePage(page: PdfDocument.Page, block: (PdfDocument.Page) -> T): T {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    try {
        return block(page)
    } catch (e: Throwable) {
        throw e
    } finally {
        finishPage(page)
    }
}

@OptIn(ExperimentalContracts::class)
inline fun <T> PdfDocument.startAndUsePage(pageInfo: PdfDocument.PageInfo, block: (PdfDocument.Page) -> T): T {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    val page = startPage(pageInfo)
    return usePage(page, block)
}