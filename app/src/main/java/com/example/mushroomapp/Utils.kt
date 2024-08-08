package com.example.mushroomapp

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class Utils {
    companion object {
        fun assetFilePath(context: Context, assetName: String): String {

            val file = File(context.getFilesDir(), assetName)
            try {
                context.getAssets().open(assetName).use { `is` ->
                    FileOutputStream(file).use { os ->
                        val buffer = ByteArray(4 * 1024)
                        var read: Int
                        while (`is`.read(buffer).also { read = it } != -1) {
                            os.write(buffer, 0, read)
                        }
                        os.flush()
                    }
                    return file.getAbsolutePath()
                }
            } catch (e: IOException) {
                Log.e("pytorchandroid", "Error process asset $assetName to file path")
            }
            return ""
        }

    }
}