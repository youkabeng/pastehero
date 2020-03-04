package me.phph.app.pastehero

import javafx.scene.input.Clipboard

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

data class Entry(val type: EntryType, val value: String, val variants: MutableList<Entry>)

object Paster {

    private val entries = mutableListOf<Entry>()
    private val clipboard = Clipboard.getSystemClipboard()

    init {
        object : com.sun.glass.ui.ClipboardAssistance(com.sun.glass.ui.Clipboard.SYSTEM) {
            override fun contentChanged() {
                readClipboardEntry()
            }
        }
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
        return entries.toList()
    }

    fun readClipboardEntry() {
        if (clipboard.hasString()) {
            val entry = Entry(EntryType.STRING, clipboard.string, mutableListOf())
            if (clipboard.hasHtml()) {
                entry.variants + Entry(EntryType.HTML, clipboard.html, mutableListOf())
            }
            entries += entry
        }
    }

    fun setClipboardEntry() {

    }
}
