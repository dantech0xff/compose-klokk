package dev.dantech.klokk.persist

import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.preferences.core.PreferencesSerializer
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.cinterop.ExperimentalForeignApi
import okio.FileSystem
import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
actual fun defaultKlokkRepository(): KlokkRepository = sharedRepository {
    val storage = OkioStorage(
        fileSystem = FileSystem.SYSTEM,
        serializer = PreferencesSerializer,
        producePath = {
            val dir = NSFileManager.defaultManager.URLForDirectory(
                directory = NSDocumentDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = true,
                error = null,
            )
            (requireNotNull(dir).path + "/$DATA_STORE_FILE").toPath()
        },
    )
    PreferencesKlokkRepository(
        DataStoreFactory.create(
            storage = storage,
            corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
        ),
    )
}
