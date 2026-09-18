package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.models.Severity
import com.example.data.repository.PotholeRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Smart Pothole", appName)
  }

  @Test
  fun `test syncWithCloudAndFetchLatest syncs pending and populates potholes`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val inMemoryDb = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    val repository = PotholeRepository(inMemoryDb.potholeDao())

    val result = repository.syncWithCloudAndFetchLatest()
    assertTrue(result.totalActivePotholes > 0)

    // Submit a manual report then sync
    repository.submitManualReport(
        lat = 19.5760,
        lng = 74.2120,
        severity = Severity.HIGH,
        description = "Deep crater near court",
        photoUri = null,
        roadName = "Court Road"
    )

    val syncResult = repository.syncWithCloudAndFetchLatest()
    assertTrue(syncResult.totalActivePotholes >= 4)
    inMemoryDb.close()
  }
}


