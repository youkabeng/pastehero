package me.phph.app.pastehero.api

import java.io.ByteArrayInputStream
import java.sql.*
import javax.imageio.ImageIO

object Storage {
    private const val TABLE_ITEMS = "items"

    private var connection: Connection? = null

    init {
        initTable()
    }

    private fun getConnection(): Connection {
        if (connection == null) {
            val homePath = Configuration.getConfiguration(Configuration.CONF_APP_HOME)
            val databaseName = Configuration.getConfiguration(Configuration.CONF_DB_NAME)
            Class.forName("org.sqlite.JDBC")
            connection = DriverManager.getConnection("jdbc:sqlite:${homePath + databaseName}")
            connection?.autoCommit = false
        }
        return connection!!
    }

    private fun initTable() {
        val sql = ("create table if not exists $TABLE_ITEMS("
                + "id integer primary key autoincrement,"
                + "type integer,"
                + "data text,"
                + "binary blob,"
                + "md5_digest text,"
                + "update_ts integer"
                + ")")
        var statement: Statement? = null
        try {
            statement = getConnection().createStatement()
            statement.executeUpdate(sql)
            getConnection().commit()
        } catch (e: Exception) {
            getConnection().rollback()
            e.printStackTrace()
            throw e
        } finally {
            statement?.close()
        }
    }

    fun cleanup() {
        getConnection().close()
    }

    fun count(): Int {
        var statement: Statement? = null
        var resultSet: ResultSet? = null
        try {
            statement = getConnection().createStatement()
            resultSet = statement.executeQuery("select count(id) as c from $TABLE_ITEMS")
            while (resultSet.next()) {
                return resultSet.getInt("c")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            statement?.close()
            resultSet?.close()
        }
        return 0
    }

    fun saveItem(item: Item) {
        var statement1: PreparedStatement? = null
        var statement2: Statement? = null
        var resultSet: ResultSet? = null
        try {
            val sql = "insert into $TABLE_ITEMS values(NULL,?,?,?,?,?)"
            val updateTs = System.currentTimeMillis()
            statement1 = getConnection().prepareStatement(sql).apply {
                setString(1, item.type.toString())
                setString(2, if (item.value.isNotEmpty()) item.value else null)
                setBytes(3, item.image?.let(::readImage))
                setString(4, item.md5Digest)
                setLong(5, updateTs)
            }
            statement1.executeUpdate()
            getConnection().commit()

            val queryIdSql = "select seq from sqlite_sequence where name='$TABLE_ITEMS'"
            statement2 = getConnection().createStatement()
            resultSet = statement2.executeQuery(queryIdSql)
            while (resultSet.next()) {
                val id = resultSet.getInt(1)
                item.id = id
                break
            }
            item.updateTs = updateTs
        } catch (e: Exception) {
            e.printStackTrace()
            getConnection().rollback()
        } finally {
            statement1?.close()
            statement2?.close()
            resultSet?.close()
        }
    }

    fun updateItem(item: Item) {
        var statement: PreparedStatement? = null
        try {
            val sql = "update $TABLE_ITEMS set type=?, data=?, binary=?, md5_digest=?, update_ts=? where id=?"
            statement = getConnection().prepareStatement(sql)
            statement.setString(1, item.type.toString())
            if (item.type == ItemType.STRING) {
                statement.setString(2, item.value)
                statement.setString(3, null)
            } else if (item.type == ItemType.IMAGE) {
                statement.setString(2, null)
                statement.setBytes(3, readImage(item.image!!))
            }
            val updateTs = System.currentTimeMillis()
            statement.setString(4, item.md5Digest)
            statement.setLong(5, updateTs)
            statement.setInt(6, item.id)
            statement.executeUpdate()
            getConnection().commit()
            item.updateTs = updateTs
        } catch (e: Exception) {
            e.printStackTrace()
            getConnection().rollback()
        } finally {
            statement?.close()
        }
    }

    fun listItemById(id: Int): Item {
        val sql = "select * from $TABLE_ITEMS where id=$id"
        var statement: Statement? = null
        var resultSet: ResultSet? = null
        try {
            statement = getConnection().createStatement()
            resultSet = statement.executeQuery(sql)
            while (resultSet.next()) {
                return Item(
                    resultSet.getInt(1),
                    ItemType.valueOf(resultSet.getString(2)),
                    resultSet.getString(3) ?: "",
                    resultSet.getBytes(4)?.let { ImageIO.read(ByteArrayInputStream(it)) },
                    resultSet.getString(5),
                    resultSet.getLong(6)
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            statement?.close()
            resultSet?.close()
        }
        return Item(-1, md5Digest = "")
    }

    fun listRecentItems(count: Int): List<Item> {
        val sql = ("select id,type,data,binary,md5_digest,update_ts from $TABLE_ITEMS"
                + if (Configuration.getConfigurationBool(Configuration.CONF_IGNORE_IMAGE)) " where type!='${ItemType.IMAGE.name}'" else ""
                + " order by update_ts desc limit $count")
        val retList = mutableListOf<Item>()
        var statement: Statement? = null
        var resultSet: ResultSet? = null
        try {
            statement = getConnection().createStatement()
            resultSet = statement.executeQuery(sql)
            while (resultSet.next()) {
                retList.add(
                    Item(
                        id = resultSet.getInt(1),
                        type = ItemType.valueOf(resultSet.getString(2)),
                        value = resultSet.getString(3) ?: "",
                        image = resultSet.getBytes(4)?.let { ImageIO.read(ByteArrayInputStream(it)) },
                        md5Digest = resultSet.getString(5),
                        updateTs = resultSet.getLong(6)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            statement?.close()
            resultSet?.close()
        }
        return retList.toList()
    }

    fun listItems(countPerPage: Int, pageNumber: Int): List<Item> {
        val start = countPerPage * (pageNumber - 1)
        val end = start + countPerPage

        val sql = ("select * from $TABLE_ITEMS"
                + " order by update_ts desc limit $start,$end")
        val retList = mutableListOf<Item>()
        var statement: Statement? = null
        var resultSet: ResultSet? = null
        try {
            statement = getConnection().createStatement()
            resultSet = statement.executeQuery(sql)
            while (resultSet.next()) {
                retList.add(
                    Item(
                        resultSet.getInt(1),
                        ItemType.valueOf(resultSet.getString(2)),
                        resultSet.getString(3) ?: "",
                        resultSet.getBytes(4)?.let { ImageIO.read(ByteArrayInputStream(it)) },
                        resultSet.getString(5),
                        resultSet.getLong(6)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            statement?.close()
            resultSet?.close()
        }
        return retList.toList()
    }

    fun deleteItem(id: Int) {
        var statement: Statement? = null
        try {
            statement = getConnection().createStatement()
            statement.executeUpdate("delete from $TABLE_ITEMS where id=$id")
            getConnection().commit()
        } catch (e: Exception) {
            e.printStackTrace()
            getConnection().rollback()
        } finally {
            statement?.close()
        }
    }

}

