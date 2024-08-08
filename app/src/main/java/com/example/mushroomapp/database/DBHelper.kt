package com.example.mushroomapp.database

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.example.mushroomapp.modal.HistoryEntry
import com.example.mushroomapp.modal.InnerMushroom
import java.io.ByteArrayOutputStream
import java.sql.Date
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.concurrent.LinkedBlockingQueue
import kotlin.system.measureNanoTime

class DBHelper(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VER) {
    companion object {
        private val DATABASE_VER = 1
        private val DATABASE_NAME = "MushroomsApp.db"
        //Mushrooms
        private val TABLE_NAME = "Mushrooms"
        private val COL_ID = "Id"
        private val COL_SPECIES_PL = "PolishSpecies"
        private val COL_SPECIES_LA = "LatinSpecies"
        private val COL_IMAGE = "MushroomImage"
        private val COL_ICON_IMG = "IconImg"
        private val COL_EDIBILITY = "Edibility"
        //History
        private val TABLE_NAME1 = "History"
        private val COL_HISTORY_ENTRY_ID = "HistoryEntryId"
        private val COL_USER_ID = "UserId"
        private val COL_MUSHROOM_ID = "MushroomId"
        private val COL_HISTORY_IMAGE = "HistoryImage"
        private val COL_DATE = "Date"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_TABLE_QUERY = ("CREATE TABLE IF NOT EXISTS $TABLE_NAME ($COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COL_SPECIES_PL TEXT NOT NULL, $COL_SPECIES_LA TEXT NOT NULL," +
                " $COL_IMAGE BLOB NOT NULL, $COL_ICON_IMG BLOB NOT NULL, $COL_EDIBILITY TEXT NOT NULL)")
        val CREATE_TABLE1_QUERY = ("CREATE TABLE IF NOT EXISTS $TABLE_NAME1 ($COL_HISTORY_ENTRY_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COL_USER_ID INTEGER NOT NULL," +
                " $COL_MUSHROOM_ID INT NOT NULL, $COL_HISTORY_IMAGE BLOB NOT NULL, $COL_DATE TEXT NOT NULL," +
                " FOREIGN KEY ($COL_MUSHROOM_ID) REFERENCES $TABLE_NAME ($COL_ID));")
        db!!.execSQL(CREATE_TABLE_QUERY)
        db.execSQL(CREATE_TABLE1_QUERY)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME1")
        onCreate(db)
    }

    val allMushrooms : List<InnerMushroom>
        get(){

            val listMushrooms = ArrayList<InnerMushroom>()
            val elapsed = measureNanoTime {
                val selectQuery = "SELECT $COL_ID, $COL_ICON_IMG, $COL_SPECIES_PL FROM $TABLE_NAME ORDER BY $COL_SPECIES_PL"
                val db = this.readableDatabase
                val cursor = db.rawQuery(selectQuery, null)
                if (cursor.moveToFirst()) {
                    do {
                        val mushroom = InnerMushroom()
                        mushroom.id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID))
                        mushroom.species =
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_SPECIES_PL))
                        /*mushroom.latin_species =
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_SPECIES_LA))*/
                        /*val imageByteArray = cursor.getBlob(cursor.getColumnIndexOrThrow(COL_IMAGE))
                        val imageBitmap =
                            BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.size)
                        mushroom.image = imageBitmap*/
                        val iconByteArray = cursor.getBlob(cursor.getColumnIndexOrThrow(COL_ICON_IMG))
                        val iconBitmap =
                            BitmapFactory.decodeByteArray(iconByteArray, 0, iconByteArray.size)
                        mushroom.iconImg = iconBitmap
                        /*mushroom.edibility =
                            cursor.getString(cursor.getColumnIndexOrThrow(COL_EDIBILITY))*/

                        listMushrooms.add(mushroom)
                    } while (cursor.moveToNext())
                }
                db.close()
            }
            Log.d(ContentValues.TAG, "Selecting data took $elapsed ns")
            return listMushrooms
        }

    fun dropAll(context: Context){
        context.deleteDatabase(DATABASE_NAME)
    }

    fun getClassifiedMushrooms(mushrooms: IntArray): List<InnerMushroom> {
        val listMushrooms = ArrayList<InnerMushroom>()
        val mush1 = mushrooms[0]
        val mush2 = mushrooms[1]
        val mush3 = mushrooms[2]
        val mush4 = mushrooms[3]
        val mush5 = mushrooms[4]
        val query = "SELECT $COL_ID, $COL_ICON_IMG, $COL_SPECIES_PL FROM $TABLE_NAME WHERE $COL_ID IN ($mush1, $mush2, $mush3, $mush4, $mush5)"
        val db = this.readableDatabase
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                val mushroom = InnerMushroom()
                mushroom.id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID))
                mushroom.species =
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_SPECIES_PL))
                val iconByteArray = cursor.getBlob(cursor.getColumnIndexOrThrow(COL_ICON_IMG))
                val iconBitmap =
                    BitmapFactory.decodeByteArray(iconByteArray, 0, iconByteArray.size)
                mushroom.iconImg = iconBitmap
                listMushrooms.add(mushroom)
            } while (cursor.moveToNext())
        }
        db.close()
        return listMushrooms
    }

    fun findMushroom(mushroomId: Int): InnerMushroom {
        val selectQuery = "SELECT * FROM $TABLE_NAME WHERE $COL_ID = $mushroomId"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        cursor.moveToFirst()
        val mushroom = InnerMushroom()
        mushroom.id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID))
        mushroom.species = cursor.getString(cursor.getColumnIndexOrThrow(COL_SPECIES_PL))
        mushroom.latin_species = cursor.getString(cursor.getColumnIndexOrThrow(COL_SPECIES_LA))
        val imageByteArray = cursor.getBlob(cursor.getColumnIndexOrThrow(COL_IMAGE))
        val imageBitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.size)
        mushroom.image = imageBitmap
        mushroom.edibility = cursor.getString(cursor.getColumnIndexOrThrow(COL_EDIBILITY))
        return mushroom
    }

    fun addMushroom(mushroom: InnerMushroom){
        val db= this.writableDatabase
        val values = ContentValues()
        values.put(COL_ID, mushroom.id)
        values.put(COL_SPECIES_PL, mushroom.species)
        values.put(COL_SPECIES_LA, mushroom.latin_species)
        val bStream = ByteArrayOutputStream(1000)
        mushroom.image!!.compress(Bitmap.CompressFormat.JPEG, 100, bStream)
        val byteArray = bStream.toByteArray()
        values.put(COL_IMAGE, byteArray)
        val iconStream = ByteArrayOutputStream(1000)
        mushroom.iconImg!!.compress(Bitmap.CompressFormat.JPEG, 100, iconStream)
        val iconArray = iconStream.toByteArray()
        values.put(COL_ICON_IMG, iconArray)
        values.put(COL_EDIBILITY, mushroom.edibility)

        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun getHistoryEntries(userId: Int): List<HistoryEntry> {
        val listHistoryEntries = ArrayList<HistoryEntry>()
        val selectQuery = "SELECT * FROM $TABLE_NAME1 WHERE $COL_USER_ID = '$userId'"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        if (cursor.moveToFirst()){
            do {
                val dateString = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE))
                val utilDate = SimpleDateFormat("yyyy-MM-dd").parse(dateString)
                val sqlDate = Date(utilDate.time)
                val historyEntry = HistoryEntry(cursor.getInt(cursor.getColumnIndexOrThrow(COL_MUSHROOM_ID)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID)),
                    cursor.getBlob(cursor.getColumnIndexOrThrow(COL_HISTORY_IMAGE)),
                    sqlDate
                )

                listHistoryEntries.add(historyEntry)
            } while (cursor.moveToNext())
        }
        return listHistoryEntries
    }

    fun addHistoryEntry(userId: Int, mushroomId: Int, image: ByteArray, date: Date): Boolean{
        val db= this.writableDatabase
        val values = ContentValues()
        values.put(COL_USER_ID, userId)
        values.put(COL_MUSHROOM_ID, mushroomId)
        values.put(COL_HISTORY_IMAGE, image)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        values.put(COL_DATE, dateFormat.format(date))
        val res = db.insert(TABLE_NAME1, null, values)
        db.close()
        return res > 0
    }

    fun deleteHistory(userId: Int) {
        val db= this.writableDatabase
        db.delete(TABLE_NAME1, "$COL_USER_ID = $userId", null)
        db.close()
    }

    fun checkDB(context : Context) : Boolean {
        val db = context.getDatabasePath(DATABASE_NAME)
        return db.exists()
    }
}