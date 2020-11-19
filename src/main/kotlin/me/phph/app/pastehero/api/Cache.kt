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
    private val cache = LRUCache<String, Entry>(maxEntryCount)

    init {
        loadData()
    }

    private fun loadData() {
        val entryList = Storage.listRecentEntries(maxEntryCount).reversed()
        for (entry in entryList) {
            cache.set(entry.md5Digest, entry)
        }
    }

    fun listEntries(start: Int, end: Int): List<Entry> {
        val retList = mutableListOf<Entry>()
        var i = 0
        val listIterator = ArrayList<Entry>(cache.values).listIterator(count())
        while (listIterator.hasPrevious()) {
            val entry = listIterator.previous()
            if (i in start until end) {
                retList.add(entry)
            }
            i++
        }
        return retList
    }

    fun containsEntry(md5Digest: String): Boolean {
        return cache.containsKey(md5Digest)
    }

    fun setEntry(entry: Entry) {
        cache.set(entry.md5Digest, entry)
        if(entry.id == -1) {
            Storage.saveEntry(entry)
        } else {
            Storage.updateEntry(entry)
        }
    }

    fun getEntry(md5Digest: String): Entry? {
        return cache[md5Digest]
    }

    fun count(): Int {
        return cache.size
    }

//    fun saveEntries() {
//        val iterator = cache.iterator()
//        while (iterator.hasNext()) {
//            val entry = iterator.next().value
//            if (entry.id == -1) {
//                Storage.saveEntry(entry)
//            } else {
//                Storage.updateEntry(entry)
//            }
//        }
//    }

}
