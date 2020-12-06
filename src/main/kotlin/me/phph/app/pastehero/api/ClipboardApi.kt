package me.phph.app.pastehero.api

import com.sun.glass.ui.ClipboardAssistance
import javafx.beans.property.SimpleIntegerProperty
import javafx.embed.swing.SwingFXUtils
import javafx.scene.image.Image
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import java.awt.image.BufferedImage
import java.io.File

/**
 * All the supported data types
 * HTML, XML, JSON, URL, RTF are all of type STRING with additional formats
 */
enum class EntryType {
    STRING,
    HTML,
    XML,
    JSON,
    URL,
    RTF,
    FILES,
    IMAGE
}

data class Entry(
    var id: Int = -1,
    var type: EntryType = EntryType.STRING,
    var value: String = "",
    var image: BufferedImage? = null,
    var md5Digest: String,
    var updateTs: Long = 0,
    val variants: MutableList<Entry> = mutableListOf()
)

object ClipboardApi {

    private val clipboard = Clipboard.getSystemClipboard()

    private val configuration = Configuration

    val updated = SimpleIntegerProperty(0)

    init {
        object : ClipboardAssistance(com.sun.glass.ui.Clipboard.SYSTEM) {
            override fun contentChanged() {
                readClipboard()
                updated.value += 1
            }
        }
    }

    fun listEntries(searchString: String): List<Entry> {
        val start = 0
        val end = Cache.count()
        val searchIgnoreCase = Configuration.getConfigurationBool(Configuration.CONF_SEARCH_IGNORECASE)
        return ((if (searchString.isEmpty()) arrayListOf() else Cache.defaultEntries.map { it.value }) + Cache.listEntries(
            start,
            end
        )).filter {
            if (searchString.isEmpty()) true else it.value.contains(searchString, searchIgnoreCase)
        }
    }

    private fun modData(data: String): String {
        var d = data
        if (configuration.getConfigurationBool(Configuration.CONF_AUTO_TRIM)) {
            d = d.trim()
        }
        return d
    }

    fun readClipboard() {
        var (data, type) = when {
            clipboard.hasString() -> {
                if (clipboard.hasFiles()) {
                    listOf(clipboard.string, EntryType.FILES)
                } else {
                    val str = clipboard.string
                    when {
                        isJson(str) -> listOf(str, EntryType.JSON)
                        isXml(str) -> listOf(str, EntryType.XML)
                        else -> listOf(str, EntryType.STRING)
                    }
                }
            }
            clipboard.hasImage() -> listOf(clipboard.image, EntryType.IMAGE)
            else -> return
        }

        if (type == EntryType.IMAGE) {
            val bufferedImage = SwingFXUtils.fromFXImage(data as Image, null)
            val md5Digest = md5(bufferedImage)
            if (!Cache.containsEntry(md5Digest)) {
                Cache.setEntry(Entry(type = EntryType.IMAGE, image = bufferedImage, md5Digest = md5Digest))
            }
        } else {
            data = modData(data as String)
            if (data.isEmpty()) {
                return
            }
            val md5Digest = md5(data)
            when {
                Cache.defaultEntries.containsKey(md5Digest) -> {
                    Cache.defaultEntries[md5Digest]?.let { e ->
                        if (type != e.type) {
                            e.type = type as EntryType
                        }
                        Cache.setEntry(e)
                    }
                }
                Cache.containsEntry(md5Digest) -> {
                    Cache.getEntry(md5Digest)?.let(Cache::setEntry)
                }
                else -> {
                    Cache.setEntry(Entry(type = type as EntryType, value = data, md5Digest = md5Digest))
                }
            }
        }
    }

    fun setClipboard(md5Digest: String) {
        Cache.getEntry(md5Digest)?.let { entry ->
            entry.updateTs = System.currentTimeMillis()
            when (entry.type) {
                EntryType.STRING,
                EntryType.URL,
                EntryType.JSON,
                EntryType.XML,
                EntryType.HTML,
                EntryType.RTF -> {
                    clipboard.setContent(ClipboardContent().apply { putString(entry.value) })
                }
                EntryType.IMAGE -> {
                    clipboard.setContent(ClipboardContent().apply {
                        putImage(
                            SwingFXUtils.toFXImage(
                                entry.image!!,
                                null
                            )
                        )
                    })
                }
                EntryType.FILES -> {
                    entry.value.split("\n").map { File(it.replace("file://", "")) }.let { fileList ->
                        clipboard.setContent(ClipboardContent().apply {
                            putFiles(fileList)
                        })
                    }
                }
            }
            Cache.setEntry(entry)
        }
    }

}
