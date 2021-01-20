package me.phph.app.pastehero.api

import com.alibaba.fastjson.JSONObject
import org.xml.sax.InputSource
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.StringReader
import java.security.MessageDigest
import javax.imageio.ImageIO
import javax.xml.parsers.DocumentBuilderFactory

private val md5Digest = MessageDigest.getInstance("MD5")

fun md5(input: String): String {
    val result = md5Digest.digest(input.toByteArray())
    return toHex(result)
}

fun md5(image: BufferedImage): String {
    val os = ByteArrayOutputStream()
    ImageIO.write(image, "png", os)
    val result = md5Digest.digest(os.toByteArray())
    return toHex(result)
}

private fun toHex(byteArray: ByteArray): String {
    return with(StringBuilder()) {
        byteArray.forEach {
            val hex = it.toInt() and (0xFF)
            val hexStr = Integer.toHexString(hex)
            if (hexStr.length == 1) {
                append("0").append(hexStr)
            } else {
                append(hexStr)
            }
        }
        toString()
    }
}

fun readImage(image: BufferedImage): ByteArray {
    val os = ByteArrayOutputStream()
    ImageIO.write(image, "png", os)
    return os.toByteArray()
}

fun isJson(data: String): Boolean {
    try {
        JSONObject.parseObject(data)
        return true
    } catch (e: Exception) {
    }
    return false
}

fun isXml(data: String): Boolean {
    try {
        val f = DocumentBuilderFactory.newInstance()
        val b = f.newDocumentBuilder()
        b.parse(InputSource(StringReader(data)))
        return true
    } catch (e: Exception) {
    }
    return false
}

fun loadStylesheet(path: String): String? {
    return Thread.currentThread().contextClassLoader.getResource(path)?.toExternalForm()
}


fun isWindows(): Boolean {
    return System.getProperty("os.name").toLowerCase().contains("windows")
}

fun isLinux(): Boolean {
    return System.getProperty("os.name").toLowerCase().contains("linux")
}
