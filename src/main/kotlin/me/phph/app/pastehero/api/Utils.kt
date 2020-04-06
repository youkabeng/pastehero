package me.phph.app.pastehero.api

import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import javax.imageio.ImageIO

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
    ImageIO.write(image, "png", os);
    return os.toByteArray()
}
