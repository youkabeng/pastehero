package me.phph.app.pastehero

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

data class Entry(val id: Int, val type: EntryType, val value: String, val variants: MutableList<Entry>)

object Paster {

    private var initialId = 0
    private val entryList = mutableListOf<Entry>()
    private val clipboard = Clipboard.getSystemClipboard()

    val updated = SimpleIntegerProperty(0)

    private var entryMap = mutableMapOf<Int, Entry>()

    init {
        object : com.sun.glass.ui.ClipboardAssistance(com.sun.glass.ui.Clipboard.SYSTEM) {
            override fun contentChanged() {
                readClipboardEntry()
                updated.value += 1
            }
        }
    }

    fun generateId(): Int {
        return initialId++
    }

    fun loadEntries() {

    }

    fun saveEntries() {

    }

    fun updateEntry() {

    }

    fun deleteEntry() {

    }

    fun searchEntry() {

    }

    fun listEntries(): List<Entry> {
        return entryList.toList()
    }

    fun readClipboardEntry() {
        if (clipboard.hasString()) {
            val entry = Entry(generateId(), EntryType.STRING, clipboard.string, mutableListOf())
            if (clipboard.hasHtml()) {
                entry.variants.add(Entry(generateId(), EntryType.HTML, clipboard.html, mutableListOf()))
            }
            entryList += entry
            entryMap[entry.id] = entry
        }
    }

    fun setClipboardEntry(id: Int) {
        val entry = entryMap[id]!!
        clipboard.setContent(ClipboardContent().apply { putString(entry.value) })
    }
}
