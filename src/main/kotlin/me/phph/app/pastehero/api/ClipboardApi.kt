package me.phph.app.pastehero.api

import com.sun.glass.ui.ClipboardAssistance
import javafx.beans.property.SimpleIntegerProperty
import javafx.embed.swing.SwingFXUtils
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import java.awt.image.BufferedImage

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

data class Entry(var id: Int = -1,
                 val type: EntryType = EntryType.STRING,
                 var value: String = "",
                 var image: BufferedImage? = null,
                 var md5Digest: String,
                 var updateTs: Long = 0,
                 val variants: MutableList<Entry> = mutableListOf())

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
        return ((if (searchString.isEmpty()) arrayListOf() else Cache.defaultEntries.map { it.value }) + Cache.listEntries(start, end)).filter {
            if (searchString.isEmpty()) true else it.value.contains(searchString, searchIgnoreCase)
        }
    }

    fun readClipboard() {
        val md5Digest: String?
        when {
            clipboard.hasString() -> {
                var clipboardString = clipboard.string
                if (configuration.getConfigurationBool(Configuration.CONF_AUTO_TRIM)) {
                    clipboardString = clipboardString.trim()
                }
                md5Digest = md5(clipboardString)
                if (!Cache.containsEntry(md5Digest)) {
                    Cache.setEntry(Entry(type = EntryType.STRING, value = clipboard.string, md5Digest = md5Digest))
                } else if (Cache.defaultEntries.containsKey(md5Digest)) {
                    Cache.defaultEntries[md5Digest]?.let { Cache.setEntry(it) }
                }
            }
            clipboard.hasImage() -> {
                val bufferedImage = SwingFXUtils.fromFXImage(clipboard.image, null)
                md5Digest = md5(bufferedImage)
                if (!Cache.containsEntry(md5Digest)) {
                    Cache.setEntry(Entry(type = EntryType.IMAGE, image = bufferedImage, md5Digest = md5Digest))
                }
            }
            else -> {
                return
            }
        }
    }

    fun setClipboard(md5Digest: String) {
        val entry = Cache.getEntry(md5Digest)
        entry?.let {
            it.updateTs = System.currentTimeMillis()
            when (entry.type) {
                EntryType.STRING -> {
                    clipboard.setContent(ClipboardContent().apply { putString(entry.value) })
                }
                EntryType.IMAGE -> {
                    clipboard.setContent(ClipboardContent().apply { putImage(SwingFXUtils.toFXImage(entry.image!!, null)) })
                }
                else -> {
                    return
                }
            }
        }
    }

}