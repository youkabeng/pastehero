package me.phph.app.pastehero.api

import java.io.File

object Configuration {
    private val configurations = mutableMapOf<String, String>()

    private const val CONF_VALUE_TRUE = "true"
    private const val CONF_VALUE_FALSE = "false"

    private const val CONF_CONFIG_NAME = "config_name"
    const val CONF_APP_HOME = "app_home"
    const val CONF_DB_NAME = "db_name"
    const val CONF_MAX_ITEM_COUNT = "max_item_count"
    const val CONF_COUNT_PER_PAGE = "count_per_page"
    const val CONF_AUTO_TRIM = "auto_trim"
    const val CONF_AUTO_STRIP = "auto_strip"
    const val CONF_SEARCH_IGNORECASE = "search_ignorecase"
    const val CONF_TRIGGER_SHORTCUT = "trigger_shortcut"

    private const val DEFAULT_ITEMS = "default_items"
    const val SPECIAL_COMMENT = "##########"

    init {
        // default configurations go here
        val sep = File.separatorChar
        configurations[CONF_APP_HOME] = System.getProperty("user.home") + sep + ".pastehero" + sep
        configurations[CONF_CONFIG_NAME] = "config"
        configurations[CONF_DB_NAME] = "data.db"
        configurations[CONF_MAX_ITEM_COUNT] = "100"
        configurations[CONF_COUNT_PER_PAGE] = "20"
        configurations[CONF_AUTO_TRIM] = CONF_VALUE_TRUE
        configurations[CONF_AUTO_STRIP] = CONF_VALUE_FALSE
        configurations[CONF_SEARCH_IGNORECASE] = CONF_VALUE_TRUE
        configurations[CONF_TRIGGER_SHORTCUT] = "control,shift,V"
        setup()
    }

    private fun setup() {
        val homePath = getHomePath()
        val homeDir = File(homePath)
        if (!homeDir.exists()) {
            if (!homeDir.mkdirs()) {
                throw RuntimeException("failed to create config directory")
            }
        }
        val rcFile = File(homePath + configurations[CONF_CONFIG_NAME])
        if (rcFile.exists()) {
            readConfiguration(rcFile)
        } else {
            rcFile.writer().use { writer ->
                val iterator = configurations.iterator()
                while (iterator.hasNext()) {
                    val kv = iterator.next()
                    writer.write("${kv.key} = ${kv.value}\n")
                }
            }
        }
        // default items
        val defaultEntriesFile = File(homePath + DEFAULT_ITEMS)
        if (!defaultEntriesFile.exists()) {
            defaultEntriesFile.writer().use { writer ->
                writer.write("$SPECIAL_COMMENT your default items go here")
            }
        }
    }

    private fun readConfiguration(f: File) {
        f.bufferedReader().useLines { lines ->
            lines.filter { line ->
                !line.trim().startsWith("#")
            }.forEach { line ->
                val kvs = line.split("=")
                putConfiguration(kvs[0].trim(), kvs[1].trim())
            }
        }
    }

    private fun getHomePath(): String {
        var homePath = configurations[CONF_APP_HOME]!!
        if (!homePath.endsWith(File.separatorChar)) {
            homePath += File.separatorChar
        }
        return homePath
    }

    fun getDefaultItemsFilePath(): String {
        val homePath = getHomePath()
        return homePath + DEFAULT_ITEMS
    }

    fun putConfiguration(key: String, value: String) {
        configurations[key] = value
    }

    fun getConfiguration(key: String): String {
        return configurations[key]!!
    }

    fun getConfigurationInt(key: String): Int {
        return configurations[key]!!.toInt()
    }

    fun getConfigurationBool(key: String): Boolean {
        return configurations[key]!! == CONF_VALUE_TRUE
    }

    private fun writeConfiguration() {

    }

}