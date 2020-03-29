package me.phph.app.pastehero.api

import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.Statement

object Storage {
    const val TABLE_ENTRIES = "entries"

    private val configuration = Configuration
    private var connection: Connection

    init {
        val homePath = configuration.getConfiguration(Configuration.CONF_APP_HOME)
        val databaseName = configuration.getConfiguration(Configuration.CONF_DB_NAME)
        Class.forName("org.sqlite.JDBC")
        connection = DriverManager.getConnection("jdbc:sqlite:${homePath + databaseName}")
        connection.autoCommit = false
        val statement = connection.createStatement()
        val sql = ("create table if not exists entries("
                + "id integer primary key autoincrement,"
                + "type integer,"
                + "data text,"
                + "update_ts integer)")

        statement.executeUpdate(sql)
    }

    fun cleanup() {
        connection.close()
    }

    fun count(): Int {
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery("select count(id) as c from entries")
        while (resultSet.next()) {
            return resultSet.getInt("c")
        }
        return 0;
    }

    fun saveEntry(entry: Entry) {
        saveEntries(listOf(entry))
    }

    fun saveEntries(entries: Collection<Entry>) {
        val statement: PreparedStatement?
        try {
            val sql = "insert into $TABLE_ENTRIES values(NULL,?,?,?)"
            statement = connection.prepareStatement(sql)
            for (entry in entries) {
                statement.setString(1, entry.type.toString())
                statement.setString(2, entry.value)
                statement.setInt(3, System.currentTimeMillis().toInt())
                statement.addBatch()
            }
            statement.executeBatch()
            connection.commit()
        } catch (e: Exception) {
            e.printStackTrace()
            connection.rollback()
        }
    }

    fun updateEntry(entry: Entry) {
        updateEntries(listOf(entry))
    }

    fun updateEntries(entries: Collection<Entry>) {
        val statement: PreparedStatement?
        try {
            val sql = "update $TABLE_ENTRIES set type=?, data=?, update_ts=? where id=?"
            statement = connection.prepareStatement(sql)
            for (entry in entries) {
                statement.setString(1, entry.type.toString())
                statement.setString(2, entry.value)
                statement.setInt(3, System.currentTimeMillis().toInt())
                statement.setInt(4, entry.id)
                statement.addBatch()
            }
            statement.executeBatch()
        } catch (e: Exception) {
            e.printStackTrace()
            connection.rollback()
        }
    }

    fun listEntryById(id: Int): Entry {
        val sql = "select * from $TABLE_ENTRIES where id=$id"
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery(sql)
        while (resultSet.next()) {
            return Entry(resultSet.getInt(1),
                    EntryType.valueOf(resultSet.getString(2)),
                    resultSet.getString(3),
                    resultSet.getInt(4))
        }
        return Entry(-1)
    }

    fun listEntries(countPerPage: Int, pageNumber: Int): List<Entry> {
        val start = countPerPage * (pageNumber - 1)
        val end = start + countPerPage

        val sql = ("select * from $TABLE_ENTRIES"
                + " order by update_ts desc limit $start,$end")

        val statement = connection.createStatement()
        val resultSet = statement.executeQuery(sql)
        val retList = mutableListOf<Entry>()
        while (resultSet.next()) {
            val entry = Entry(resultSet.getInt(1),
                    EntryType.valueOf(resultSet.getString(2)),
                    resultSet.getString(3),
                    resultSet.getInt(4))
            retList.add(entry)
        }
        return retList.toList()
    }

    fun deleteEntry(id: Int) {
        deleteEntries(listOf(id))
    }

    fun deleteEntries(ids: Collection<Int>) {
        val statement: Statement?
        try {
            statement = connection.createStatement()
            for (id in ids) {
                statement.addBatch("delete from $TABLE_ENTRIES where id=$id")
            }
            statement.executeBatch()
            connection.commit()
        } catch (e: Exception) {
            e.printStackTrace()
            connection.rollback()
        }
    }


}

