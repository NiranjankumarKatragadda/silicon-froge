package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.VlsiDatabase
import com.example.data.local.VlsiExperimentDao
import com.example.data.local.VlsiExperimentEntity
import com.example.data.local.VlsiExperimentRepository
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
@Config(sdk = [34])
class VlsiExperimentDashboardTest {

  private lateinit var database: VlsiDatabase
  private lateinit var experimentDao: VlsiExperimentDao
  private lateinit var repository: VlsiExperimentRepository

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    database = Room.inMemoryDatabaseBuilder(context, VlsiDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    experimentDao = database.experimentDao()
    repository = VlsiExperimentRepository(experimentDao)
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun testInitialSeeding_containsAllCoreVlsiDomains() = runBlocking {
    VlsiDatabase.populateInitialExperiments(experimentDao)

    val allExperiments = repository.allExperiments.first()
    assertTrue("Should seed at least 10 experiments", allExperiments.size >= 10)

    val domains = allExperiments.map { it.domain }.distinct()
    assertTrue("Should contain CMOS domain", domains.contains("CMOS"))
    assertTrue("Should contain Digital Logic domain", domains.contains("Digital Logic"))
    assertTrue("Should contain Layout Design domain", domains.contains("Layout Design"))
    assertTrue("Should contain Wafer Fab domain", domains.contains("Wafer Fab"))
    assertTrue("Should contain Silicon Test domain", domains.contains("Silicon Test"))

    // Verify domain grouping
    val grouped = allExperiments.groupBy { it.domain }
    assertEquals(5, grouped.size)
    assertTrue("CMOS domain should have multiple experiments", (grouped["CMOS"]?.size ?: 0) >= 3)
    assertTrue("Digital Logic domain should have multiple experiments", (grouped["Digital Logic"]?.size ?: 0) >= 3)
  }

  @Test
  fun testToggleCompletionAndFavorite_persistsInRoom() = runBlocking {
    VlsiDatabase.populateInitialExperiments(experimentDao)

    val exp = repository.getExperimentById("exp_cmos_vtc")
    assertNotNull(exp)
    val initialFav = exp!!.isFavorite
    val initialComp = exp.isCompleted

    // Toggle favorite
    repository.toggleFavorite("exp_cmos_vtc")
    val updatedFav = repository.getExperimentById("exp_cmos_vtc")
    assertEquals(!initialFav, updatedFav!!.isFavorite)

    // Toggle completion
    repository.setCompletionStatus("exp_cmos_vtc", !initialComp)
    val updatedComp = repository.getExperimentById("exp_cmos_vtc")
    assertEquals(!initialComp, updatedComp!!.isCompleted)
  }

  @Test
  fun testAddCustomExperiment_groupsIntoCustomOrExistingCategory() = runBlocking {
    val customExp = VlsiExperimentEntity(
      id = "custom_finfet_drc",
      title = "3nm GAAFET Gate-All-Around Leakage Lab",
      domain = "CMOS",
      description = "Measure nanosheet subthreshold swing and gate dielectric leakage.",
      theoreticalFormula = "SS = (kT/q) * ln(10) * (1 + Cd/Cox)",
      difficulty = "Advanced",
      durationMinutes = 45,
      isCompleted = false,
      isFavorite = true,
      keyLearningObjectives = "Understand quantum confinement in sub-3nm nanosheets.",
      industryApplication = "TSMC N3E / Intel 18A PDK standard cells"
    )

    repository.insertExperiment(customExp)

    val fetched = repository.getExperimentById("custom_finfet_drc")
    assertNotNull(fetched)
    assertEquals("3nm GAAFET Gate-All-Around Leakage Lab", fetched!!.title)
    assertEquals("CMOS", fetched.domain)

    val cmosList = repository.getExperimentsByDomain("CMOS").first()
    assertTrue(cmosList.any { it.id == "custom_finfet_drc" })
  }
}
