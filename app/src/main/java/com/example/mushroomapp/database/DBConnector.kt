package com.example.mushroomapp.database

import android.content.ContentValues
import android.graphics.Bitmap
import android.util.Log
import com.example.mushroomapp.modal.HistoryEntry
import com.example.mushroomapp.modal.InnerMushroom
import com.example.mushroomapp.modal.User
import java.io.ByteArrayOutputStream
import java.sql.Connection
import java.sql.Date
import java.sql.DriverManager
import java.sql.SQLException
import java.util.concurrent.LinkedBlockingQueue

class DBConnector {
    private val URL = "database"
    private val USER = "username"
    private val PASSWORD = "password"

    companion object {

        fun checkIfUserExists(dbConnector: DBConnector, username: String) : Int {
            val queue = LinkedBlockingQueue<Int>()
            Thread {
                try {
                    val connection = dbConnector.connect()
                    val query = "SELECT 1 FROM users WHERE login = ?"
                    val stat = connection.prepareStatement(query)
                    stat.setString(1, username)
                    val resultSet = stat.executeQuery()
                    if (resultSet.next()) {
                        queue.add(1)
                    } else {
                        queue.add(0)
                    }
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }.start()
            return queue.take()
        }

        fun checkUserPassword(dbConnector: DBConnector, username: String, password: String) : Int {
            val queue = LinkedBlockingQueue<Int>()
            Thread {
                try {
                    val connection = dbConnector.connect()
                    val query = "SELECT user_id FROM users WHERE login = ? and password = ?"
                    val stat = connection.prepareStatement(query)
                    stat.setString(1, username)
                    stat.setString(2, password)
                    val resultSet = stat.executeQuery()
                    if (resultSet.next()) {
                        queue.add(resultSet.getInt("user_id"))
                    } else {
                        queue.add(-1)
                    }
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }.start()
            return queue.take()
        }

        fun addUser(dbConnector: DBConnector, user: User): Int {
            val queue = LinkedBlockingQueue<Int>()
            Thread {
                try {
                    val connection = dbConnector.connect()
                    val query = "INSERT INTO users (login, email, password) VALUES (?, ?, ?)"
                    val stat = connection.prepareStatement(query)
                    stat.setString(1, user.login)
                    stat.setString(2, user.email)
                    stat.setString(3, user.password)
                    val affectedRows = stat.executeUpdate()
                    if (affectedRows > 0) {
                        queue.add(1)
                    } else {
                        queue.add(0)
                    }
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }.start()
            return queue.take()
        }

        fun getAllMushrooms(dbConnector: DBConnector): List<InnerMushroom> {
            val queue = LinkedBlockingQueue<List<InnerMushroom>>()
            Thread {
                try {
                    val connection = dbConnector.connect()
                    val query = "SELECT * FROM mushrooms ORDER BY species_pl"
                    val stat = connection.prepareStatement(query)
                    val resultSet = stat.executeQuery()
                    val mushroomList = ArrayList<InnerMushroom>()
                    while (resultSet.next()) {
                        mushroomList.add(
                            InnerMushroom(
                                resultSet.getInt("mushroom_id"),
                                resultSet.getString("species_pl"),
                                resultSet.getBlob("image"),
                                resultSet.getString("edible"),
                                resultSet.getString("species_la"),
                            )
                        )
                    }
                    queue.add(mushroomList)
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }.start()
            return queue.take()
        }

        fun addUserHistoryEntry(dbConnector: DBConnector, user_id: Int, mushroom_id: Int, image: ByteArray, date: Date): Int {
            val queue = LinkedBlockingQueue<Int>()
            Thread {
                try {
                    val connection = dbConnector.connect()
                    val query = "INSERT INTO history (user_id, mushroom_id, image, date) VALUES (?, ?, ?, ?)"
                    val stat = connection.prepareStatement(query)
                    stat.setInt(1, user_id)
                    stat.setInt(2, mushroom_id)
                    val blob = connection.createBlob()
                    blob.setBytes(1, image)
                    stat.setBlob(3, blob)
                    stat.setDate(4, date)
                    val affectedRows = stat.executeUpdate()
                    if (affectedRows > 0) {
                        queue.add(1)
                    } else {
                        queue.add(0)
                    }
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }.start()
            return queue.take()
        }

        fun addUserHistoryEntry(dbConnector: DBConnector, entry: HistoryEntry): Int {
            val queue = LinkedBlockingQueue<Int>()
            Thread {
                try {
                    val connection = dbConnector.connect()
                    val query = "INSERT INTO history (user_id, mushroom_id, image, date) VALUES (?, ?, ?, ?)"
                    val stat = connection.prepareStatement(query)
                    stat.setInt(1, entry.user_id!!)
                    stat.setInt(2, entry.mushroom_id!!)
                    val blob = connection.createBlob()
                    val stream = ByteArrayOutputStream()
                    entry.image!!.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    blob.setBytes(1, stream.toByteArray())
                    stat.setBlob(3, blob)
                    stat.setDate(4, entry.date!!)
                    val affectedRows = stat.executeUpdate()
                    if (affectedRows > 0) {
                        queue.add(1)
                    } else {
                        queue.add(0)
                    }
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }.start()
            return queue.take()
        }

        fun getUserHistory(dbConnector: DBConnector, user: Int): List<HistoryEntry> {
            val queue = LinkedBlockingQueue<List<HistoryEntry>>()
            Thread {
                try {
                    val connection = dbConnector.connect()
                    val query = "SELECT m.species_pl as species, h.image as image, h.mushroom_id as mushroom_id " +
                            "FROM mushrooms m JOIN history h USING(mushroom_id) " +
                            "WHERE user_id = ? " +
                            "GROUP BY species " +
                            "ORDER BY species, mushroom_id DESC"
                    val stat = connection.prepareStatement(query)
                    stat.setInt(1, user)
                    val resultSet = stat.executeQuery()
                    val historyList = ArrayList<HistoryEntry>()
                    while (resultSet.next()) {
                        historyList.add(HistoryEntry(
                            resultSet.getInt("mushroom_id"),
                            resultSet.getBlob("image"),
                            null,
                            resultSet.getString("species")
                        ))
                    }
                    queue.add(historyList)
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }.start()
            return queue.take()
        }

        fun getEntryImages(dbConnector: DBConnector, user: Int, mushroom: Int): List<HistoryEntry> {
            val queue = LinkedBlockingQueue<List<HistoryEntry>>()
            Thread {
                try {
                    val connection = dbConnector.connect()
                    val query = "SELECT m.species_pl as species, h.image as image, h.date as date, h.mushroom_id as mushroom_id " +
                            "FROM mushrooms m JOIN history h USING(mushroom_id) " +
                            "WHERE h.user_id = ? AND h.mushroom_id = ? " +
                            "ORDER BY species, date DESC"
                    val stat = connection.prepareStatement(query)
                    stat.setInt(1, user)
                    stat.setInt(2, mushroom)
                    val resultSet = stat.executeQuery()
                    val historyList = ArrayList<HistoryEntry>()
                    while (resultSet.next()) {
                        historyList.add(HistoryEntry(
                            resultSet.getInt("mushroom_id"),
                            resultSet.getBlob("image"),
                            resultSet.getDate("date"),
                            resultSet.getString("species")
                        ))
                    }
                    queue.add(historyList)
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }.start()
            return queue.take()
        }

        fun getClassifiedMushrooms(dbConnector: DBConnector, mushrooms: String): List<InnerMushroom> {
            val queue = LinkedBlockingQueue<List<InnerMushroom>>()
            Thread {
                try {
                    val connection = dbConnector.connect()
                    val query = "SELECT * FROM mushrooms WHERE FIND_IN_SET( mushroom_id, ? ) > 0"
                    val stat = connection.prepareStatement(query)
                    stat.setString(1, mushrooms)
                    val resultSet = stat.executeQuery()
                    val mushroomList = ArrayList<InnerMushroom>()
                    while (resultSet.next()) {
                        mushroomList.add(
                            InnerMushroom(
                                resultSet.getInt("mushroom_id"),
                                resultSet.getString("species_pl"),
                                resultSet.getBlob("image"),
                                resultSet.getString("edible"),
                                resultSet.getString("species_la"),
                                )
                        )
                    }
                    queue.add(mushroomList)
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }.start()
            return queue.take()
        }

        fun getUserStats(dbConnector: DBConnector, userId: Int): Pair<HistoryEntry?, Int> {
            val queue = LinkedBlockingQueue<Pair<HistoryEntry?, Int>>()
            Thread {
                try {
                    val connection = dbConnector.connect()
                    val query = "SELECT m.species_pl AS species, m.image AS image, h.mushroom_id AS id, " +
                            "(SELECT COUNT(DISTINCT mushroom_id) FROM history WHERE user_id = ?) AS img_count " +
                            "FROM history h JOIN mushrooms m USING(mushroom_id) " +
                            "WHERE h.user_id = ? " +
                            "ORDER BY h.date DESC " +
                            "LIMIT 1"
                    val stat = connection.prepareStatement(query)
                    stat.setInt(1, userId)
                    stat.setInt(2, userId)
                    val resultSet = stat.executeQuery()
                    if (resultSet.next()) {
                        queue.add(Pair(
                            HistoryEntry(
                                resultSet.getInt("id"),
                                resultSet.getBlob("image"),
                                null,
                                resultSet.getString("species")
                            ),
                            resultSet.getInt("img_count")
                        ))
                    } else {
                        queue.add(Pair(null, -1))
                    }
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }.start()
            return queue.take()
        }
    }



    private fun connect(): Connection {
        Log.d(ContentValues.TAG, "Attempting connection")
        val connection = DriverManager.getConnection(
            URL,
            USER,
            PASSWORD
        )
        Log.d(ContentValues.TAG, "Connection established")
        return connection
    }

}