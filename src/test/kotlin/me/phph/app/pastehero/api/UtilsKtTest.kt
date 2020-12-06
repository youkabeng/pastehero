package me.phph.app.pastehero.api

import org.junit.Assert.assertEquals
import org.junit.Test

class UtilsKtTest {
    @Test
    fun testIsJson() {
        assertEquals(true, isJson("{\"a\":1,\"b\":\"v\"}"))
        assertEquals(false, isJson("{\"a\":1,\"b\":\"v\""))
        assertEquals(false, isJson("this is a string"))
    }

    @Test
    fun testIsXml() {
        assertEquals(true, isXml("<tag>this is a string in tag</tag>"))
        assertEquals(false, isXml("<tag>this is a string in tag<tag>"))
        assertEquals(false, isXml("<tag>this is a string in tag</tag"))
        assertEquals(false, isXml("<tag>this is a string in tag"))
        assertEquals(false, isXml("this is a string"))
    }

    @Test
    fun testMd5() {
        assertEquals("41fb5b5ae4d57c5ee528adb00e5e8e74", md5("This is a string"))
        assertEquals("4046773c0a08ed3b46a185dcf04ffde8", md5("<tag>this is a string in tag</tag>"))
    }

}