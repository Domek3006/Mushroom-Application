package com.example.mushroomapp.modal

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Parcel
import android.os.Parcelable
import java.io.ByteArrayOutputStream
import java.sql.Blob

class InnerMushroom : Parcelable {
    var id: Int? = null
    var species: String? = null
    var image: Bitmap? = null
    var iconImg: Bitmap? = null
    var edibility: String? = null
    var latin_species: String? = null

    private fun convertBlobToBitmap(blob: Blob?): Bitmap {
        val blobLen = blob!!.length().toInt()
        val blobAsBytes = blob.getBytes(1, blobLen)
        return BitmapFactory.decodeByteArray(blobAsBytes, 0, blobAsBytes.size)
    }

    constructor(parcel: Parcel) : this() {
        id = parcel.readValue(Int::class.java.classLoader) as? Int
        species = parcel.readString()
        val byteArray = parcel.createByteArray()
        image = BitmapFactory.decodeByteArray(byteArray, 0, byteArray!!.size)
        edibility = parcel.readString()
        latin_species = parcel.readString()

    }

    constructor()

    constructor(id: Int, species: String, image: Blob, poisonous: String, latin_species: String) {
        this.id = id
        this.species = species
        this.image = convertBlobToBitmap(image)
        this.iconImg = Bitmap.createScaledBitmap(this.image!!, 250, 250, false)
        this.edibility = poisonous
        this.latin_species = latin_species
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(species)
        val bStream = ByteArrayOutputStream(100)
        image!!.compress(Bitmap.CompressFormat.JPEG, 100, bStream)
        val byteArray = bStream.toByteArray()
        parcel.writeByteArray(byteArray)
        parcel.writeString(edibility)
        parcel.writeString(latin_species)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<InnerMushroom> {
        override fun createFromParcel(parcel: Parcel): InnerMushroom {
            return InnerMushroom(parcel)
        }

        override fun newArray(size: Int): Array<InnerMushroom?> {
            return arrayOfNulls(size)
        }
    }
}