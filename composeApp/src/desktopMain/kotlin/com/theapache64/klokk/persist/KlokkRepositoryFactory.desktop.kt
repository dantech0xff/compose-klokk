package com.theapache64.klokk.persist

import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.FileStorage
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferencesFileSerializer
import androidx.datastore.preferences.core.emptyPreferences
import java.io.File

actual fun defaultKlokkRepository(): KlokkRepository = sharedRepository {
    val dir = File(System.getProperty("user.home"), ".klokk").apply { mkdirs() }
    val storage = FileStorage(
        serializer = PreferencesFileSerializer,
        produceFile = { File(dir, DATA_STORE_FILE) },
    )
    PreferencesKlokkRepository(
        DataStoreFactory.create(
            storage = storage,
            corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
        ),
    )
}
