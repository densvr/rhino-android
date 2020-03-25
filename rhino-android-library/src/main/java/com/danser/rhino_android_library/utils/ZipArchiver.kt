package com.danser.rhino_android_library.utils

import android.util.Log
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ZipArchiver {

    private const val BUFFER_SIZE = 8192

    fun makeZipArchive(outputPath: String, files: Map<String, String>) {
        try {
            val outputFile = File(outputPath)
            outputFile.mkdirs()
            outputFile.delete()
            outputFile.createNewFile()
            val zipOutputStream =
                ZipOutputStream(BufferedOutputStream(FileOutputStream(outputPath)))
            val data = ByteArray(BUFFER_SIZE)
            for ((file, nameInZip) in files) {
                Log.v(ZipArchiver::class.java.simpleName, "Zipped: $file as $nameInZip")
                val fileInputStream = BufferedInputStream(FileInputStream(file), BUFFER_SIZE)
                val entry = ZipEntry(nameInZip)
                zipOutputStream.putNextEntry(entry)
                var count: Int
                while (fileInputStream.read(data, 0, BUFFER_SIZE).also { count = it } != -1) {
                    zipOutputStream.write(data, 0, count)
                }
                fileInputStream.close()
            }
            zipOutputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
