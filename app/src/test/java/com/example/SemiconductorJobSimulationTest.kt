package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.SemiconductorJobTrendsRepository
import com.example.data.local.JobApplicationDao
import com.example.data.local.JobApplicationEntity
import com.example.data.local.VlsiDatabase
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
class SemiconductorJobSimulationTest {

  private lateinit var database: VlsiDatabase
  private lateinit var jobAppDao: JobApplicationDao
  private lateinit var trendsRepository: SemiconductorJobTrendsRepository

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    database = Room.inMemoryDatabaseBuilder(context, VlsiDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    jobAppDao = database.jobApplicationDao()
    trendsRepository = SemiconductorJobTrendsRepository()
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun testInitialJobApplicationsSeeding_populatesSampleRecords() = runBlocking {
    VlsiDatabase.populateInitialJobApplications(jobAppDao)

    val count = jobAppDao.getApplicationCountSync()
    assertTrue("Should seed at least 2 mock applications", count >= 2)

    val applications = jobAppDao.getAllApplications().first()
    assertEquals(count, applications.size)

    val nvidiaApp = applications.find { it.company == "NVIDIA" }
    assertNotNull("Should contain NVIDIA mock application", nvidiaApp)
    assertEquals("Interview Offered", nvidiaApp!!.status)
    assertEquals("TSMC 3nm N3E", nvidiaApp.targetFabNode)
    assertTrue("ATS score should be high", nvidiaApp.atsMatchScore > 85)
  }

  @Test
  fun testSubmitMockApplication_persistsInRoomDatabase() = runBlocking {
    val newApp = JobApplicationEntity(
      id = "APP-TEST-999",
      jobId = "job_apple_pd_sta",
      jobTitle = "Silicon Physical Design & Timing Signoff Engineer",
      company = "Apple Silicon",
      location = "Austin, TX",
      domain = "Physical Design",
      applicantName = "Marcus Vance",
      applicantEmail = "marcus.vance@berkeley.edu",
      degreeLevel = "Ph.D. Microelectronics",
      gpaOrGrade = "3.95",
      portfolioSummary = "Full custom 2nm GAAFET floorplanning and IR-drop signoff",
      targetFabNode = "TSMC 3nm GAAFET",
      atsMatchScore = 96,
      status = "Submitted",
      submissionTimestamp = System.currentTimeMillis(),
      recruiterFeedback = "Outstanding tapeout profile."
    )

    jobAppDao.insertApplication(newApp)

    val retrieved = jobAppDao.getApplicationById("APP-TEST-999")
    assertNotNull(retrieved)
    assertEquals("Marcus Vance", retrieved!!.applicantName)
    assertEquals("Apple Silicon", retrieved.company)
    assertEquals(96, retrieved.atsMatchScore)
  }

  @Test
  fun testAdvanceApplicationStatus_updatesFeedbackAndStatusInRoom() = runBlocking {
    val newApp = JobApplicationEntity(
      id = "APP-TEST-STATUS",
      jobId = "job_nv_tpu_arch",
      jobTitle = "AI Hardware Architecture & RTL Engineer",
      company = "NVIDIA",
      location = "Santa Clara, CA",
      domain = "AI Hardware",
      applicantName = "Elena Chen",
      applicantEmail = "elena.chen@mit.edu",
      degreeLevel = "M.S. VLSI",
      gpaOrGrade = "3.90",
      portfolioSummary = "4-bit ALU & Ring Oscillator",
      targetFabNode = "3nm N3E",
      atsMatchScore = 92,
      status = "Submitted"
    )
    jobAppDao.insertApplication(newApp)

    // Simulate recruiter status update
    jobAppDao.updateApplicationStatus(
      id = "APP-TEST-STATUS",
      status = "Interview Offered",
      feedback = "Passed technical screen. Scheduled round 1 panel."
    )

    val updated = jobAppDao.getApplicationById("APP-TEST-STATUS")
    assertEquals("Interview Offered", updated!!.status)
    assertEquals("Passed technical screen. Scheduled round 1 panel.", updated.recruiterFeedback)
  }

  @Test
  fun testWithdrawApplication_removesFromRoomDatabase() = runBlocking {
    val app = JobApplicationEntity(
      id = "APP-TO-DELETE",
      jobId = "job_qcom_modem_rtl",
      jobTitle = "Modem RTL Engineer",
      company = "Qualcomm",
      location = "San Diego, CA",
      domain = "Digital Logic",
      applicantName = "Test Candidate",
      applicantEmail = "test@qualcomm.com",
      degreeLevel = "B.S. ECE",
      gpaOrGrade = "3.5",
      portfolioSummary = "Basic logic gates",
      targetFabNode = "4nm FinFET",
      atsMatchScore = 78,
      status = "Submitted"
    )
    jobAppDao.insertApplication(app)

    val beforeDelete = jobAppDao.getApplicationById("APP-TO-DELETE")
    assertNotNull(beforeDelete)

    jobAppDao.deleteApplication(app)
    val afterDelete = jobAppDao.getApplicationById("APP-TO-DELETE")
    assertNull(afterDelete)
  }

  @Test
  fun testRealTimeTrendsRepository_generatesTrendsAndCalculatesAts() = runBlocking {
    val trends = trendsRepository.jobTrends.first()
    assertTrue("Should provide multiple trending sectors", trends.size >= 4)

    val aiHardwareTrend = trends.find { it.id == "trend_ai_hardware" }
    assertNotNull(aiHardwareTrend)
    assertTrue("AI Hardware YoY growth should be high", aiHardwareTrend!!.hiringGrowthYoY > 40f)

    // Test ATS scoring algorithm
    val (atsScore, feedback) = trendsRepository.calculateAtsScore(
      targetJobDomain = "Physical Design",
      applicantDomain = "Physical Design",
      hasCompletedLabs = true,
      completedLabCount = 5,
      hasCustomChip = true,
      degreeLevel = "M.S. VLSI Design",
      gpaOrGrade = "3.85"
    )

    assertTrue("ATS score should be high for matching domain & labs", atsScore >= 85)
    assertTrue("Feedback should reflect strong match", feedback.contains("Match"))
  }
}
