package me.phph.app.pastehero.api

import com.sun.glass.ui.ClipboardAssistance
import javafx.beans.property.SimpleIntegerProperty
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent

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

data class Entry(val id: Int = 0,
                 val type: EntryType = EntryType.STRING,
                 val value: String = "",
                 val updateTs: Int = 0,
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
            val entries = listEntries(count, 1, "")
            for (entry in entries) {
                val digest = md5(entry.value)
                entryValueIdMap[digest] = entry
            }
        }
    }

    fun count(): Int {
        return Storage.count()
    }

    fun listEntries(countPerPage: Int, pageNumber: Int, searchString: String): List<Entry> {
        val searchIgnorecase = Configuration.getConfigurationBool(Configuration.CONF_SEARCH_IGNORECASE)
        return Storage.listEntries(countPerPage, pageNumber).filter { it.value.contains(searchString, searchIgnorecase) }
    }

    fun readClipboard() {
        // todo
        // to support other format
        if (clipboard.hasString()) {
            val digest = md5(clipboard.string)
            val entry = entryValueIdMap[digest] ?: Entry(0, EntryType.STRING, clipboard.string, 0, mutableListOf())
            if (entry.id == 0) Storage.saveEntry(entry) else Storage.updateEntry(entry)
            entryValueIdMap[digest] = entry
        }
    }

    fun setClipboard(id: Int) {
        val entry = Storage.listEntryById(id)
        clipboard.setContent(ClipboardContent().apply { putString(entry.value) })
    }
}
