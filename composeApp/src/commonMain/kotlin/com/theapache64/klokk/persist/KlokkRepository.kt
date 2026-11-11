package com.theapache64.klokk.persist

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first

interface KlokkRepository {
    suspend fun load(): KlokkSnapshot
    suspend fun save(snapshot: KlokkSnapshot)
}

class MemoryKlokkRepository(
    initial: KlokkSnapshot = KlokkSnapshot.fresh(),
) : KlokkRepository {
    private var current = initial

    override suspend fun load(): KlokkSnapshot = current

    override suspend fun save(snapshot: KlokkSnapshot) {
        current = snapshot
    }
}

private val snapshotKey = stringPreferencesKey("snapshot")

class PreferencesKlokkRepository(
    private val store: DataStore<Preferences>,
) : KlokkRepository {
    override suspend fun load(): KlokkSnapshot = decodeSnapshot(store.data.first()[snapshotKey])

    override suspend fun save(snapshot: KlokkSnapshot) {
        val encoded = encodeSnapshot(snapshot)
        store.edit { prefs -> prefs[snapshotKey] = encoded }
    }
}

private var shared: KlokkRepository? = null

internal fun sharedRepository(create: () -> KlokkRepository): KlokkRepository {
    return shared ?: create().also { shared = it }
}
