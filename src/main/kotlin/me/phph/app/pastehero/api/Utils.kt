package me.phph.app.pastehero.api

import java.security.MessageDigest


fun md5(input: String): String {
    val digest = MessageDigest.getInstance("MD5")
    val result = digest.digest(input.toByteArray())
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