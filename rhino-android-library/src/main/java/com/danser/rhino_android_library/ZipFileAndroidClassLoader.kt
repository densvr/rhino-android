/*
 * Copyright (c) 2017 Lukas Morawietz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.danser.rhino_android_library

import com.android.dex.Dex
import com.danser.rhino_android_library.utils.ZipArchiver
import dalvik.system.DexClassLoader
import dalvik.system.PathClassLoader
import java.io.File
import java.io.IOException

/**
 *  This class is for Android < 21 DexClassLoader support
 *
 *  Android >= 21 looks for some.dex
 *  Android <  21 look for some.dex inside zip archive
 *
 *  So we need to write dex file and zip it before using DexClassLoader
 *
 */
internal class ZipFileAndroidClassLoader(
    parent: ClassLoader?,
    cacheDir: File
) : BaseAndroidClassLoader(parent) {

    private val dexFile: File

    /**
     * Create a new instance with the given parent classloader
     *
     * @param parent the parent
     */
    init {
        val id = instanceCounter++
        dexFile = File(cacheDir, "$id.dex")
        cacheDir.mkdirs()
        reset()
    }

    @Throws(ClassNotFoundException::class)
    override fun loadClass(dex: Dex, name: String): Class<*> {
        val zipPath = dexFile.path + ".zip"
        try {
            dex.writeTo(dexFile)
            ZipArchiver.makeZipArchive(zipPath, mapOf(dexFile.path to "classes.dex"))
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val loader = DexClassLoader(zipPath, dexFile.parent, "", parent)
        return loader.loadClass(name)
    }

    override fun getLastDex(): Dex? {
        if (dexFile.exists()) {
            try {
                return Dex(dexFile)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return null
    }

    override fun reset() {
        dexFile.delete()
    }

    companion object {
        private var instanceCounter = 0
    }
}
