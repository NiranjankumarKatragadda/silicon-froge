package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SemiconductorJobTrendsRepository
import com.example.data.local.JobApplicationDao
import com.example.data.local.JobApplicationEntity
import com.example.data.local.VlsiDatabase
import com.example.model.MarketTelemetrySummary
import com.example.model.SemiconductorJobPosting
import com.example.model.SemiconductorJobTrend
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class JobSimulationUiState(
  val applications: List<JobApplicationEntity> = emptyList(),
  val jobTrends: List<SemiconductorJobTrend> = emptyList(),
  val jobPostings: List<SemiconductorJobPosting> = emptyList(),
  val telemetry: MarketTelemetrySummary = MarketTelemetrySummary(),
  val isRefreshing: Boolean = false,
  val selectedDomainFilter: String = "All",
  val searchQuery: String = "",
  val activeTab: Int = 0 // 0: Live Trends & Openings, 1: Tracked Applications Table
)

class JobSimulationViewModel @JvmOverloads constructor(
  application: Application,
  private val jobAppDao: JobApplicationDao = VlsiDatabase.getDatabase(application).jobApplicationDao(),
  private val trendsRepository: SemiconductorJobTrendsRepository = SemiconductorJobTrendsRepository()
) : AndroidViewModel(application) {

  private val _selectedDomainFilter = MutableStateFlow("All")
  val selectedDomainFilter: StateFlow<String> = _selectedDomainFilter.asStateFlow()

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _activeTab = MutableStateFlow(0)
  val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

  val applications: StateFlow<List<JobApplicationEntity>> = jobAppDao.getAllApplications()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val jobTrends: StateFlow<List<SemiconductorJobTrend>> = trendsRepository.jobTrends
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val jobPostings: StateFlow<List<SemiconductorJobPosting>> = trendsRepository.jobPostings
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val telemetry: StateFlow<MarketTelemetrySummary> = trendsRepository.marketTelemetry
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MarketTelemetrySummary())

  val isRefreshing: StateFlow<Boolean> = trendsRepository.isRefreshing
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

  private val _filterParams = combine(_selectedDomainFilter, _searchQuery, _activeTab) { filter, query, tab ->
    Triple(filter, query, tab)
  }

  // Combined UI state
  val uiState: StateFlow<JobSimulationUiState> = combine(
    applications,
    jobTrends,
    jobPostings,
    telemetry,
    _filterParams
  ) { apps, trends, postings, telem, filterParams ->
    val (filter, query, tab) = filterParams
    var filteredPostings = postings
    if (!filter.equals("All", ignoreCase = true)) {
      filteredPostings = filteredPostings.filter {
        it.domain.contains(filter, ignoreCase = true) || it.jobTitle.contains(filter, ignoreCase = true)
      }
    }
    if (query.isNotBlank()) {
      val q = query.trim().lowercase()
      filteredPostings = filteredPostings.filter {
        it.jobTitle.lowercase().contains(q) ||
            it.company.lowercase().contains(q) ||
            it.targetFabNode.lowercase().contains(q) ||
            it.location.lowercase().contains(q)
      }
    }

    JobSimulationUiState(
      applications = apps,
      jobTrends = trends,
      jobPostings = filteredPostings,
      telemetry = telem,
      isRefreshing = false,
      selectedDomainFilter = filter,
      searchQuery = query,
      activeTab = tab
    )
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), JobSimulationUiState())

  init {
    viewModelScope.launch {
      // Ensure initial mock applications are populated if table is empty
      VlsiDatabase.populateInitialJobApplications(jobAppDao)
    }
  }

  fun setTab(tab: Int) {
    _activeTab.value = tab
  }

  fun setDomainFilter(domain: String) {
    _selectedDomainFilter.value = domain
  }

  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun refreshLiveTrends() {
    viewModelScope.launch {
      trendsRepository.refreshRealTimeTrends()
    }
  }

  fun submitMockApplication(
    jobPosting: SemiconductorJobPosting,
    applicantName: String,
    applicantEmail: String,
    degreeLevel: String,
    gpaOrGrade: String,
    portfolioSummary: String,
    applicantDomain: String
  ): JobApplicationEntity {
    val (atsScore, feedback) = trendsRepository.calculateAtsScore(
      targetJobDomain = jobPosting.domain,
      applicantDomain = applicantDomain,
      hasCompletedLabs = true,
      completedLabCount = 4,
      hasCustomChip = true,
      degreeLevel = degreeLevel,
      gpaOrGrade = gpaOrGrade
    )

    val newApp = JobApplicationEntity(
      id = "APP-${System.currentTimeMillis() % 1000000}",
      jobId = jobPosting.id,
      jobTitle = jobPosting.jobTitle,
      company = jobPosting.company,
      location = jobPosting.location,
      domain = jobPosting.domain,
      applicantName = applicantName,
      applicantEmail = applicantEmail,
      degreeLevel = degreeLevel,
      gpaOrGrade = gpaOrGrade,
      portfolioSummary = portfolioSummary,
      targetFabNode = jobPosting.targetFabNode,
      atsMatchScore = atsScore,
      status = if (atsScore >= 80) "ATS Screen Passed" else "Submitted",
      submissionTimestamp = System.currentTimeMillis(),
      recruiterFeedback = feedback
    )

    viewModelScope.launch {
      jobAppDao.insertApplication(newApp)
    }

    return newApp
  }

  fun advanceApplicationStatus(applicationId: String, currentStatus: String) {
    viewModelScope.launch {
      val nextStatus = when (currentStatus) {
        "Submitted" -> "ATS Screen Passed"
        "ATS Screen Passed" -> "Technical Assessment"
        "Technical Assessment" -> "Interview Offered"
        "Interview Offered" -> "Accepted"
        else -> "Interview Offered"
      }
      val nextFeedback = when (nextStatus) {
        "ATS Screen Passed" -> "Resume cleared algorithmic screening with high EDA keywords."
        "Technical Assessment" -> "Sent take-home Verilog and Static Timing Analysis problem set."
        "Interview Offered" -> "Advanced to full-day engineering on-site loop."
        "Accepted" -> "Offer letter generated with signing bonus and stock options."
        else -> "In review"
      }
      jobAppDao.updateApplicationStatus(applicationId, nextStatus, nextFeedback)
    }
  }

  fun withdrawApplication(application: JobApplicationEntity) {
    viewModelScope.launch {
      jobAppDao.deleteApplication(application)
    }
  }
}
