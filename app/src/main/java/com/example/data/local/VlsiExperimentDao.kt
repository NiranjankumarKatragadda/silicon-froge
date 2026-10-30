package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VlsiExperimentDao {

  @Query("SELECT * FROM vlsi_experiments ORDER BY domain ASC, title ASC")
  fun getAllExperiments(): Flow<List<VlsiExperimentEntity>>

  @Query("SELECT * FROM vlsi_experiments WHERE domain = :domain ORDER BY title ASC")
  fun getExperimentsByDomain(domain: String): Flow<List<VlsiExperimentEntity>>

  @Query("SELECT DISTINCT domain FROM vlsi_experiments ORDER BY domain ASC")
  fun getAllDomains(): Flow<List<String>>

  @Query("SELECT * FROM vlsi_experiments WHERE isFavorite = 1 ORDER BY title ASC")
  fun getFavoriteExperiments(): Flow<List<VlsiExperimentEntity>>

  @Query("SELECT * FROM vlsi_experiments WHERE id = :id")
  suspend fun getExperimentById(id: String): VlsiExperimentEntity?

  @Query("SELECT * FROM vlsi_experiments WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR theoreticalFormula LIKE '%' || :query || '%'")
  fun searchExperiments(query: String): Flow<List<VlsiExperimentEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertExperiments(experiments: List<VlsiExperimentEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertExperiment(experiment: VlsiExperimentEntity)

  @Update
  suspend fun updateExperiment(experiment: VlsiExperimentEntity)

  @Query("UPDATE vlsi_experiments SET isCompleted = :isCompleted WHERE id = :id")
  suspend fun setCompletionStatus(id: String, isCompleted: Boolean)

  @Query("UPDATE vlsi_experiments SET isFavorite = NOT isFavorite WHERE id = :id")
  suspend fun toggleFavorite(id: String)

  @Delete
  suspend fun deleteExperiment(experiment: VlsiExperimentEntity)

  @Query("DELETE FROM vlsi_experiments")
  suspend fun deleteAll()
}
