package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.VlsiDatabase
import com.example.data.local.VlsiExperimentEntity
import com.example.data.local.VlsiExperimentRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class CatalogFilterMode(val label: String) {
  ALL("All Experiments"),
  FAVORITES("Bookmarked"),
  COMPLETED("Completed"),
  PENDING("Pending")
}

data class ExperimentCatalogUiState(
  val experiments: List<VlsiExperimentEntity> = emptyList(),
  val availableDomains: List<String> = listOf("All", "CMOS", "Digital Logic", "Layout Design", "Wafer Fab", "Silicon Test"),
  val selectedDomain: String = "All",
  val filterMode: CatalogFilterMode = CatalogFilterMode.ALL,
  val searchQuery: String = "",
  val totalExperimentsCount: Int = 0,
  val completedExperimentsCount: Int = 0,
  val isLoading: Boolean = false
)

class VlsiExperimentCatalogViewModel(
  application: Application,
  private val repository: VlsiExperimentRepository = VlsiExperimentRepository(
    VlsiDatabase.getDatabase(application).experimentDao()
  )
) : AndroidViewModel(application) {

  private val _selectedDomain = MutableStateFlow("All")
  val selectedDomain: StateFlow<String> = _selectedDomain.asStateFlow()

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _filterMode = MutableStateFlow(CatalogFilterMode.ALL)
  val filterMode: StateFlow<CatalogFilterMode> = _filterMode.asStateFlow()

  // Base experiments flow reacting to database changes
  private val _allExperimentsFlow = repository.allExperiments

  // Domains list from database with fallback defaults
  private val _domainsFlow: Flow<List<String>> = repository.allDomains.map { list ->
    val base = mutableListOf("All")
    base.addAll(if (list.isEmpty()) listOf("CMOS", "Digital Logic", "Layout Design", "Wafer Fab", "Silicon Test") else list)
    base.distinct()
  }

  // Combined UI StateFlow
  val uiState: StateFlow<ExperimentCatalogUiState> = combine(
    _allExperimentsFlow,
    _selectedDomain,
    _searchQuery,
    _filterMode,
    _domainsFlow
  ) { allList, currentDomain, query, mode, domainList ->
    var filtered = allList

    // 1. Filter by Domain
    if (!currentDomain.equals("All", ignoreCase = true)) {
      filtered = filtered.filter { it.domain.equals(currentDomain, ignoreCase = true) }
    }

    // 2. Filter by Search Query
    if (query.isNotBlank()) {
      val q = query.trim().lowercase()
      filtered = filtered.filter {
        it.title.lowercase().contains(q) ||
            it.description.lowercase().contains(q) ||
            it.theoreticalFormula.lowercase().contains(q) ||
            it.industryApplication.lowercase().contains(q)
      }
    }

    // 3. Filter by Mode (Favorites / Completed / Pending)
    filtered = when (mode) {
      CatalogFilterMode.ALL -> filtered
      CatalogFilterMode.FAVORITES -> filtered.filter { it.isFavorite }
      CatalogFilterMode.COMPLETED -> filtered.filter { it.isCompleted }
      CatalogFilterMode.PENDING -> filtered.filter { !it.isCompleted }
    }

    ExperimentCatalogUiState(
      experiments = filtered,
      availableDomains = domainList,
      selectedDomain = currentDomain,
      filterMode = mode,
      searchQuery = query,
      totalExperimentsCount = allList.size,
      completedExperimentsCount = allList.count { it.isCompleted },
      isLoading = false
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = ExperimentCatalogUiState(isLoading = true)
  )

  init {
    // If the database was created without initial population, trigger seeding
    viewModelScope.launch {
      val existing = repository.getExperimentById("exp_cmos_vtc")
      if (existing == null) {
        VlsiDatabase.populateInitialExperiments(VlsiDatabase.getDatabase(getApplication()).experimentDao())
      }
    }
  }

  // ================= User Actions =================
  fun selectDomain(domain: String) {
    _selectedDomain.value = domain
  }

  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun setFilterMode(mode: CatalogFilterMode) {
    _filterMode.value = mode
  }

  fun toggleFavorite(experimentId: String) {
    viewModelScope.launch {
      repository.toggleFavorite(experimentId)
    }
  }

  fun toggleCompletion(experimentId: String, currentStatus: Boolean) {
    viewModelScope.launch {
      repository.setCompletionStatus(experimentId, !currentStatus)
    }
  }

  fun addCustomExperiment(
    title: String,
    domain: String,
    description: String,
    theoreticalFormula: String,
    difficulty: String,
    durationMinutes: Int,
    learningObjectives: String,
    industryApplication: String
  ) {
    viewModelScope.launch {
      val newEntity = VlsiExperimentEntity(
        id = "custom_${System.currentTimeMillis()}",
        title = title,
        domain = domain,
        description = description,
        theoreticalFormula = theoreticalFormula,
        difficulty = difficulty,
        durationMinutes = durationMinutes,
        isCompleted = false,
        isFavorite = false,
        keyLearningObjectives = learningObjectives,
        industryApplication = industryApplication
      )
      repository.insertExperiment(newEntity)
    }
  }

  fun deleteExperiment(experiment: VlsiExperimentEntity) {
    viewModelScope.launch {
      repository.deleteExperiment(experiment)
    }
  }

  fun resetCatalogToDefaults() {
    viewModelScope.launch {
      repository.resetToDefaults()
    }
  }
}
