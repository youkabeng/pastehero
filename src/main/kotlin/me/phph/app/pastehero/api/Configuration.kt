package me.phph.app.pastehero.api

import java.io.File

object Configuration {
    private val configurations = mutableMapOf<String, String>()

    const val CONF_VALUE_TRUE = "true"
    const val CONF_VALUE_FALSE = "false"

    const val CONF_APP_HOME = "app_home"
    const val CONF_DB_NAME = "db_name"
    const val CONF_CONFIG_NAME = "config_name"
    const val CONF_MAX_ENTRIES = "max_entries"
    const val CONF_COUNT_PER_PAGE = "count_per_page"
    const val CONF_AUTO_TRIM = "auto_trim"
    const val CONF_AUTO_STRIP = "auto_strip"
    const val CONF_SEARCH_IGNORECASE = "search_ignorecase"
    const val CONF_TRIGGER_SHORTCUT = "trigger_shortcut"


    init {
        // default configurations go here
        configurations[CONF_APP_HOME] = System.getProperty("user.home") + "/" + ".pastehero/"
        configurations[CONF_CONFIG_NAME] = "config"
        configurations[CONF_DB_NAME] = "data.db"
        configurations[CONF_MAX_ENTRIES] = "100"
        configurations[CONF_COUNT_PER_PAGE] = "20"
        configurations[CONF_AUTO_TRIM] = CONF_VALUE_TRUE
        configurations[CONF_AUTO_STRIP] = CONF_VALUE_FALSE
        configurations[CONF_SEARCH_IGNORECASE] = CONF_VALUE_TRUE
        configurations[CONF_TRIGGER_SHORTCUT] = "ctrl,alt,v"
        setup()
    }

    private fun setup() {
        val homePath = configurations[CONF_APP_HOME]!!
        val homeDir = File(homePath)
        if (!homeDir.exists()) {
            if (!homeDir.mkdirs()) {
                throw RuntimeException("failed to create config directory")
            }
        }
        val rcFile = File(homePath + configurations[CONF_CONFIG_NAME])
        if (rcFile.exists()) {
            readConfiguration(rcFile);
        } else {
            rcFile.bufferedWriter().use { it.write("# all custom settings goes here\n") }
        }
    }

    private fun readConfiguration(f: File) {
        f.bufferedReader().useLines {
            it.filter { !it.startsWith("#") }.forEach {
                val kvs = it.split("=")
                putConfiguration(kvs[0], kvs[1])
            }
        }
    }

    fun putConfiguration(key: String, value: String) {
        configurations[key] = value;
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