package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.VlsiDatabase
import com.example.data.local.VlsiExperimentDao
import com.example.data.local.VlsiExperimentEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class VlsiDatabaseTest {

  private lateinit var db: VlsiDatabase
  private lateinit var dao: VlsiExperimentDao

  @Before
  fun createDb() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, VlsiDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    dao = db.experimentDao()
  }

  @After
  fun closeDb() {
    db.close()
  }

  @Test
  fun testInsertAndRetrieveExperiments_byDomain() = runBlocking {
    val exp1 = VlsiExperimentEntity(
      id = "test_cmos_1",
      title = "CMOS Inverter Characterization",
      domain = "CMOS",
      description = "Test CMOS transfer characteristics",
      theoreticalFormula = "VM = VDD / 2",
      difficulty = "Beginner",
      durationMinutes = 20,
      isCompleted = false,
      isFavorite = false,
      keyLearningObjectives = "Learn VTC",
      industryApplication = "TSMC 3nm cell library"
    )

    val exp2 = VlsiExperimentEntity(
      id = "test_digital_1",
      title = "Setup Time Margining",
      domain = "Digital Logic",
      description = "Timing analysis",
      theoreticalFormula = "Slack = Tperiod - Tcomb - Tsetup",
      difficulty = "Intermediate",
      durationMinutes = 30,
      isCompleted = false,
      isFavorite = true,
      keyLearningObjectives = "Learn STA",
      industryApplication = "Apple Silicon GPU"
    )

    dao.insertExperiments(listOf(exp1, exp2))

    // Query all
    val all = dao.getAllExperiments().first()
    assertEquals(2, all.size)

    // Query by domain "CMOS"
    val cmosList = dao.getExperimentsByDomain("CMOS").first()
    assertEquals(1, cmosList.size)
    assertEquals("CMOS Inverter Characterization", cmosList[0].title)

    // Query by domain "Digital Logic"
    val digitalList = dao.getExperimentsByDomain("Digital Logic").first()
    assertEquals(1, digitalList.size)
    assertEquals("Setup Time Margining", digitalList[0].title)

    // Toggle favorite
    dao.toggleFavorite("test_cmos_1")
    val updatedCmos = dao.getExperimentById("test_cmos_1")
    assertNotNull(updatedCmos)
    assertTrue(updatedCmos!!.isFavorite)

    // Update completion
    dao.setCompletionStatus("test_cmos_1", true)
    val completedCmos = dao.getExperimentById("test_cmos_1")
    assertTrue(completedCmos!!.isCompleted)
  }

  @Test
  fun testSearchExperiments() = runBlocking {
    val exp = VlsiExperimentEntity(
      id = "test_search",
      title = "Transmission Gate Multiplexer",
      domain = "CMOS",
      description = "Bidirectional analog switch",
      theoreticalFormula = "R_on = 1 / (mu * Cox)",
      difficulty = "Intermediate",
      durationMinutes = 25,
      isCompleted = false,
      isFavorite = false,
      keyLearningObjectives = "Pass transistor logic",
      industryApplication = "ARM Cortex M4 datapath"
    )
    dao.insertExperiment(exp)

    val searchResults = dao.searchExperiments("Transmission").first()
    assertEquals(1, searchResults.size)
    assertEquals("test_search", searchResults[0].id)
  }
}
