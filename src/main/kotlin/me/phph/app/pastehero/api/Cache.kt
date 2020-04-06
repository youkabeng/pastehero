package me.phph.app.pastehero.api


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
    private val data = LRUCache<Int, Entry>(maxEntryCount)

    init {
        loadData()
    }

    private fun loadData() {
        val entryList = Storage.listRecentEntries(maxEntryCount).reversed()
        for (entry in entryList) {
            data.set(entry.id, entry)
        }
    }

    fun listEntries(start: Int, end: Int): List<Entry> {
        val retList = mutableListOf<Entry>()
        var i = 0
        val iterator = data.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (i in start until end) {
                retList.add(entry.value)
            }
            i++
        }
        return retList
    }

    fun setEntry(entry: Entry) {
        data.set(entry.id, entry)
    }

    fun getEntry(id: Int): Entry? {
        return data[id]
    }

    fun count(): Int {
        return data.size
    }

}
