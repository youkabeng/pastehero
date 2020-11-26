package me.phph.app.pastehero.api

import java.io.File


class LRUCache<K, V>(private val capacity: Int) : LinkedHashMap<K, V>(capacity + 1, 1.0f, true) {

    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean {
        return this.size > capacity
    }

    fun set(key: K, value: V): V? {
        return super.put(key, value)
    }

}

object Cache {
    private val maxEntryCount: Int = Configuration.getConfigurationInt(Configuration.CONF_MAX_ENTRY_COUNT)
    private val defaultEntryFilePath = Configuration.getDefaultEntryFilePath()
    private val cache = LRUCache<String, Entry>(maxEntryCount)
    val defaultEntries = mutableMapOf<String, Entry>()

    init {
        loadData()
    }

    private fun loadData() {
        val entryList = Storage.listRecentEntries(maxEntryCount).reversed()
        File(defaultEntryFilePath).useLines { lines ->
            lines.map(String::trim).forEach { line ->
                if (!line.startsWith(Configuration.SPECIAL_COMMENT) && line.isNotEmpty()) {
                    val md5 = md5(line.trim())
                    val entry = Entry(-2, EntryType.STRING, line.trim(), null, md5)
                    defaultEntries[md5] = entry
                }
            }
        }
        for (entry in entryList) {
            if (!defaultEntries.containsKey(entry.md5Digest)) {
                cache.set(entry.md5Digest, entry)
            }
        }
    }

    fun listEntries(start: Int, end: Int): List<Entry> {
        val retList = mutableListOf<Entry>()
        var i = 0
        val listIterator = ArrayList<Entry>(cache.values).listIterator(count())
        while (listIterator.hasPrevious()) {
            val entry = listIterator.previous()
            if (i in start until end) {
                defaultEntries[entry.md5Digest]?.also { retList.add(it) } ?: run { retList.add(entry) }
            }
            i++
        }
        return retList
    }

    fun containsEntry(md5Digest: String): Boolean {
        return cache.containsKey(md5Digest) || defaultEntries.containsKey(md5Digest)
    }

    fun setEntry(entry: Entry) {
        cache.set(entry.md5Digest, entry)
        when (entry.id) {
            -1 -> Storage.saveEntry(entry)
            -2 -> Unit
            else -> Storage.updateEntry(entry)
        }
    }

    fun getEntry(md5Digest: String): Entry? {
        return cache[md5Digest] ?: defaultEntries[md5Digest]
    }

    fun count(): Int {
        return cache.size
    }

}
