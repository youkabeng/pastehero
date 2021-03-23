package me.phph.app.pastehero.api

import com.sun.glass.ui.ClipboardAssistance
import javafx.beans.property.SimpleIntegerProperty
import javafx.embed.swing.SwingFXUtils
import javafx.scene.image.Image
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import java.awt.image.BufferedImage
import java.io.File

/**
 * All the supported data types
 * HTML, XML, JSON, URL, RTF are all of type STRING with additional formats
 */
enum class ItemType {
    STRING,
    HTML,
    XML,
    JSON,
    URL,
    RTF,
    FILES,
    IMAGE
}

data class Item(
    var id: Int = -1,
    var type: ItemType = ItemType.STRING,
    var value: String = "",
    var image: BufferedImage? = null,
    var md5Digest: String,
    var updateTs: Long = 0,
    val variants: MutableList<Item> = mutableListOf()
)

object ClipboardApi {

    private val clipboard = Clipboard.getSystemClipboard()

    private val configuration = Configuration

    val updated = SimpleIntegerProperty(0)

    init {
        object : ClipboardAssistance(com.sun.glass.ui.Clipboard.SYSTEM) {
            override fun contentChanged() {
                readClipboard()
                updated.value += 1
            }
        }
    }

    fun listItems(searchString: String): List<Item> {
        val start = 0
        val end = Cache.count()
        val searchIgnoreCase = Configuration.getConfigurationBool(Configuration.CONF_SEARCH_IGNORECASE)
        val items = Cache.listItems(start, end)
        return if (searchString.isEmpty()) items else items.filter {
            if (searchString.isEmpty()) true else it.value.contains(
                searchString,
                searchIgnoreCase
            )
        }
    }

    private fun modData(data: String): String {
        var d = data
        if (configuration.getConfigurationBool(Configuration.CONF_AUTO_TRIM)) {
            d = d.trim()
        }
        return d
    }

    fun readClipboard() {
        var (data, type) = when {
            clipboard.hasString() -> {
                if (clipboard.hasFiles()) {
                    listOf(clipboard.string, ItemType.FILES)
                } else {
                    val str = clipboard.string
                    when {
                        isJson(str) -> listOf(str, ItemType.JSON)
                        isXml(str) -> listOf(str, ItemType.XML)
                        else -> listOf(str, ItemType.STRING)
                    }
                }
            }
            clipboard.hasImage() -> {
                if (!configuration.getConfigurationBool(Configuration.CONF_IGNORE_IMAGE)) {
                    listOf(clipboard.image, ItemType.IMAGE)
                } else {
                    return
                }
            }
            else -> return
        }

        if (type == ItemType.IMAGE) {
            val bufferedImage = SwingFXUtils.fromFXImage(data as Image, null)
            val md5Digest = md5(bufferedImage)
            if (!Cache.contains(md5Digest)) {
                Cache.set(Item(type = ItemType.IMAGE, image = bufferedImage, md5Digest = md5Digest))
            }
        } else {
            data = modData(data as String)
            if (data.isEmpty()) {
                return
            }
            val md5Digest = md5(data)
            when {
                Cache.defaultItems.containsKey(md5Digest) -> {
                    Cache.defaultItems[md5Digest]?.let { e ->
                        if (type != e.type) {
                            e.type = type as ItemType
                        }
                        Cache.set(e)
                    }
                }
                Cache.contains(md5Digest) -> {
                    Cache.get(md5Digest)?.let(Cache::set)
                }
                else -> {
                    Cache.set(Item(type = type as ItemType, value = data, md5Digest = md5Digest))
                }
            }
        }
    }

    fun setClipboard(md5Digest: String) {
        Cache.get(md5Digest)?.let { item ->
            item.updateTs = System.currentTimeMillis()
            when (item.type) {
                ItemType.STRING,
                ItemType.URL,
                ItemType.JSON,
                ItemType.XML,
                ItemType.HTML,
                ItemType.RTF -> {
                    clipboard.setContent(ClipboardContent().apply { putString(item.value) })
                }
                ItemType.IMAGE -> {
                    if (!Configuration.getConfigurationBool(Configuration.CONF_IGNORE_IMAGE)) {
                        clipboard.setContent(ClipboardContent().apply {
                            putImage(
                                SwingFXUtils.toFXImage(
                                    item.image!!,
                                    null
                                )
                            )
                        })
                    }
                }
                ItemType.FILES -> {
                    item.value.split("\n").map { File(it.replace("file://", "")) }.let { fileList ->
                        clipboard.setContent(ClipboardContent().apply {
                            putFiles(fileList)
                        })
                    }
                }
            }
            Cache.set(item)
        }
    }

}
