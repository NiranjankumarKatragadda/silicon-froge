package com.example.data.local

import kotlinx.coroutines.flow.Flow

class VlsiExperimentRepository(
  private val dao: VlsiExperimentDao
) {
  val allExperiments: Flow<List<VlsiExperimentEntity>> = dao.getAllExperiments()

  val allDomains: Flow<List<String>> = dao.getAllDomains()

  val favoriteExperiments: Flow<List<VlsiExperimentEntity>> = dao.getFavoriteExperiments()

  fun getExperimentsByDomain(domain: String): Flow<List<VlsiExperimentEntity>> {
    return if (domain.equals("All", ignoreCase = true)) {
      dao.getAllExperiments()
    } else {
      dao.getExperimentsByDomain(domain)
    }
  }

  fun searchExperiments(query: String): Flow<List<VlsiExperimentEntity>> {
    return dao.searchExperiments(query)
  }

  suspend fun getExperimentById(id: String): VlsiExperimentEntity? {
    return dao.getExperimentById(id)
  }

  suspend fun insertExperiment(experiment: VlsiExperimentEntity) {
    dao.insertExperiment(experiment)
  }

  suspend fun updateExperiment(experiment: VlsiExperimentEntity) {
    dao.updateExperiment(experiment)
  }

  suspend fun setCompletionStatus(id: String, isCompleted: Boolean) {
    dao.setCompletionStatus(id, isCompleted)
  }

  suspend fun toggleFavorite(id: String) {
    dao.toggleFavorite(id)
  }

  suspend fun deleteExperiment(experiment: VlsiExperimentEntity) {
    dao.deleteExperiment(experiment)
  }

  suspend fun resetToDefaults() {
    dao.deleteAll()
    VlsiDatabase.populateInitialExperiments(dao)
  }
}
