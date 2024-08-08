package com.example.mushroomapp.modal

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Parcel
import android.os.Parcelable
import java.io.ByteArrayOutputStream
import java.sql.Blob
import java.sql.Date
import java.text.SimpleDateFormat

class HistoryEntry : Parcelable {
    var mushroom_id: Int? = null
    var image: Bitmap? = null
    var date: Date? = null
    var mushroom_name: String? = null
    var user_id: Int? = null

    private fun convertBlobToBitmap(blob: Blob?): Bitmap {
        val blobLen = blob!!.length().toInt()
        val blobAsBytes = blob.getBytes(1, blobLen)
        return BitmapFactory.decodeByteArray(blobAsBytes, 0, blobAsBytes.size)
    }

    private fun convertBytesToBitmap(bytes: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    constructor(parcel: Parcel) : this() {
        mushroom_id = parcel.readValue(Int::class.java.classLoader) as? Int
        val byteArray = parcel.createByteArray()
        image = BitmapFactory.decodeByteArray(byteArray, 0, byteArray!!.size)
        val simpleDateFormat =  SimpleDateFormat("dd-mm-yyyy")
        val dateToParse = parcel.readString()
        if (dateToParse != "null") {
            date = simpleDateFormat.parse(dateToParse) as? Date
        }
        mushroom_name = parcel.readString()
    }

    constructor()

    constructor(mushroom_id: Int, image: Blob, date: Date?, mushroom_name: String) {
        this.mushroom_id = mushroom_id
        this.image = convertBlobToBitmap(image)
        this.date = date
        this.mushroom_name = mushroom_name
    }

    constructor(mushroom_id: Int, user_id: Int, image: ByteArray, date: Date?) {
        this.mushroom_id = mushroom_id
        this.user_id = user_id
        this.image = convertBytesToBitmap(image)
        this.date = date
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(mushroom_id)
        val bStream = ByteArrayOutputStream(100)
        image!!.compress(Bitmap.CompressFormat.JPEG, 100, bStream)
        val byteArray = bStream.toByteArray()
        parcel.writeByteArray(byteArray)
        parcel.writeString(date.toString())
        parcel.writeString(mushroom_name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<HistoryEntry> {
        override fun createFromParcel(parcel: Parcel): HistoryEntry {
            return HistoryEntry(parcel)
        }

        override fun newArray(size: Int): Array<HistoryEntry?> {
            return arrayOfNulls(size)
        }
    }

}