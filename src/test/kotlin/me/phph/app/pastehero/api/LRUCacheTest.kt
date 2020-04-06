package me.phph.app.pastehero.api

import org.junit.Assert
import org.junit.Test

class LRUCacheTest {

    @Test
    fun testLRUCache() {
        val cache = LRUCache<Int, Int>(2)
        cache.set(2, 1)
        cache.set(3, 2)
        cache.set(4, 3)
        Assert.assertEquals(null, cache[2])
        Assert.assertEquals(2, cache[3])
        Assert.assertEquals(3, cache[4])
    }
}