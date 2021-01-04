package me.phph.app.pastehero.api

import java.io.File

/**
 * LRU cache that holds [capacity] entries in max
 * If the cache is full, the earliest entry will be deleted
 */
class LRUCache<K, V>(private val capacity: Int) : LinkedHashMap<K, V>(capacity + 1, 1.0f, true) {

    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean {
        return this.size > capacity
    }

    fun set(key: K, value: V): V? {
        return super.put(key, value)
    }

}

/**
 * A wrapper class for LRCCache
 */
object Cache {
    private val maxItemsCount: Int = Configuration.getConfigurationInt(Configuration.CONF_MAX_ENTRY_COUNT)
    private val defaultItemsFilePath = Configuration.getDefaultEntryFilePath()
    private val cache = LRUCache<String, Item>(maxItemsCount)
    val defaultItems = mutableMapOf<String, Item>()

    init {
        loadData()
    }

    /**
     * Load default entries first and then the regular entries
     * The id of a default entry is hard coded to -2 for now
     */
    private fun loadData() {
        val items = Storage.listRecentEntries(maxItemsCount).reversed()
        File(defaultItemsFilePath).useLines { lines ->
            lines.map(String::trim).forEach { line ->
                if (!line.startsWith(Configuration.SPECIAL_COMMENT) && line.isNotEmpty()) {
                    val md5 = md5(line.trim())
                    val entry = Item(-2, ItemType.STRING, line.trim(), null, md5)
                    defaultItems[md5] = entry
                }
            }
        }
        for (item in items) {
            if (!defaultItems.containsKey(item.md5Digest)) {
                cache.set(item.md5Digest, item)
            }
        }
    }

    fun listItems(start: Int, end: Int): List<Item> {
        val retList = mutableListOf<Item>()
        var i = 0
        val listIterator = ArrayList<Item>(cache.values).listIterator(count())
        while (listIterator.hasPrevious()) {
            val item = listIterator.previous()
            if (item.type == ItemType.STRING) {
                if (item.value.isEmpty()) {
                    continue
                }
                if (i in start until end) {
                    defaultItems[item.md5Digest]?.also { retList.add(it) } ?: run { retList.add(item) }
                }
            } else {
                retList.add(item)
            }
            i++
        }
        return retList
    }

    fun contains(md5Digest: String): Boolean {
        return cache.containsKey(md5Digest) || defaultItems.containsKey(md5Digest)
    }

    fun set(item: Item) {
        cache.set(item.md5Digest, item)
        when (item.id) {
            -1 -> Storage.saveEntry(item)
            -2 -> Unit
            else -> Storage.updateEntry(item)
        }
    }

    fun get(md5Digest: String): Item? {
        return cache[md5Digest] ?: defaultItems[md5Digest]
    }

    fun delete(md5Digest: String) {
        cache[md5Digest]?.let { item ->
            cache.remove(md5Digest)
            Storage.deleteEntry(item.id)
        }
    }

    fun count(): Int {
        return cache.size
    }

}
