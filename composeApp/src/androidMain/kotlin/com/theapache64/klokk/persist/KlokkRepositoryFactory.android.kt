package com.theapache64.klokk.persist

import android.content.Context
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.FileStorage
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferencesFileSerializer
import androidx.datastore.preferences.core.emptyPreferences

object KlokkAndroid {
    var appContext: Context? = null
}

fun initKlokkAndroid(context: Context) {
    KlokkAndroid.appContext = context.applicationContext
}

actual fun defaultKlokkRepository(): KlokkRepository = sharedRepository {
    val context = checkNotNull(KlokkAndroid.appContext) {
        "Call initKlokkAndroid from the activity before creating the repository."
    }
    val storage = FileStorage(
        serializer = PreferencesFileSerializer,
        produceFile = { context.filesDir.resolve(DATA_STORE_FILE) },
    )
    PreferencesKlokkRepository(
        DataStoreFactory.create(
            storage = storage,
            corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
        ),
    )
}
