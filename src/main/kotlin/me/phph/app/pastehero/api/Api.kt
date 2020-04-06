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
                 var updateTs: Int = 0,
                 val variants: MutableList<Entry> = mutableListOf())

object PasteHero {

    private val clipboard = Clipboard.getSystemClipboard()

    val updated = SimpleIntegerProperty(0)

    // value to id map
    private var entryValueIdMap = mutableMapOf<String, Entry>()

    init {
        object : ClipboardAssistance(com.sun.glass.ui.Clipboard.SYSTEM) {
            override fun contentChanged() {
                readClipboard()
                updated.value += 1
            }
        }
        // cache entries when start
        val count = Storage.count()
        if (count > 0) {
            val entries = listEntries("")
            for (entry in entries) {
                if (entry.type == EntryType.STRING) {
                    val digest = md5(entry.value)
                    entryValueIdMap[digest] = entry
                } else if (entry.type == EntryType.IMAGE) {
                    val digest = md5(entry.image!!)
                    entryValueIdMap[digest] = entry
                }
            }
        }
    }

    fun count(): Int {
        return Cache.count()
    }

    fun listEntries(searchString: String): List<Entry> {
        val start = 0
        val end = Cache.count()
        val searchIgnoreCase = Configuration.getConfigurationBool(Configuration.CONF_SEARCH_IGNORECASE)
        return Cache.listEntries(start, end).filter { it.value.contains(searchString, searchIgnoreCase) }
    }

    fun readClipboard() {
        // todo
        // to support other format
        if (clipboard.hasString()) {
            val digest = md5(clipboard.string)
            val entry = entryValueIdMap[digest] ?: Entry(type = EntryType.STRING, value = clipboard.string)
            if (entry.id == -1) Storage.saveEntry(entry) else Storage.updateEntry(entry)
            entryValueIdMap[digest] = entry
        } else if (clipboard.hasImage()) {
            val bufferedImage = SwingFXUtils.fromFXImage(clipboard.image, null)
            val digest = md5(bufferedImage)
            val entry = entryValueIdMap[digest] ?: Entry(type = EntryType.IMAGE, image = bufferedImage)
            entryValueIdMap[digest] = entry
            if (entry.id == -1) Storage.saveEntry(entry) else Storage.updateEntry(entry)
        }
    }

    fun setClipboard(id: Int) {
        val entry = Storage.listEntryById(id)
        if (entry.type == EntryType.STRING) {
            clipboard.setContent(ClipboardContent().apply { putString(entry.value) })
        } else if (entry.type == EntryType.IMAGE) {
            clipboard.setContent(ClipboardContent().apply { putImage(SwingFXUtils.toFXImage(entry.image!!, null)) })
        }
    }
}
