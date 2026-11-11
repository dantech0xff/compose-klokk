package dev.dantech.klokk.persist

import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.FileStorage
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferencesFileSerializer
import androidx.datastore.preferences.core.emptyPreferences
import dev.dantech.klokk.state.City
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class PreferencesKlokkRepositoryTest {

    @Test
    fun aFileRoundTripsAndAMissingFileIsFresh() = runBlocking {
        val dir = File(System.getProperty("java.io.tmpdir"), "klokk-test-${System.nanoTime()}")
        dir.mkdirs()
        val repo = PreferencesKlokkRepository(
            DataStoreFactory.create(
                storage = FileStorage(
                    serializer = PreferencesFileSerializer,
                    produceFile = { File(dir, DATA_STORE_FILE) },
                ),
                corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
            ),
        )
        assertEquals(KlokkSnapshot.fresh(), repo.load())
        val saved = KlokkSnapshot(plus = true, cities = listOf(City("Hanoi", "Asia/Bangkok")))
        repo.save(saved)
        assertEquals(saved, repo.load())
    }
}
