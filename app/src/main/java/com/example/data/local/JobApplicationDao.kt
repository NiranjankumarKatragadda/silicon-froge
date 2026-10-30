package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface JobApplicationDao {

  @Query("SELECT * FROM mock_job_applications ORDER BY submissionTimestamp DESC")
  fun getAllApplications(): Flow<List<JobApplicationEntity>>

  @Query("SELECT * FROM mock_job_applications WHERE id = :id LIMIT 1")
  suspend fun getApplicationById(id: String): JobApplicationEntity?

  @Query("SELECT * FROM mock_job_applications WHERE company = :company ORDER BY submissionTimestamp DESC")
  fun getApplicationsByCompany(company: String): Flow<List<JobApplicationEntity>>

  @Query("SELECT COUNT(*) FROM mock_job_applications")
  fun getApplicationCount(): Flow<Int>

  @Query("SELECT COUNT(*) FROM mock_job_applications")
  suspend fun getApplicationCountSync(): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertApplication(application: JobApplicationEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertApplications(applications: List<JobApplicationEntity>)

  @Update
  suspend fun updateApplication(application: JobApplicationEntity)

  @Query("UPDATE mock_job_applications SET status = :status, recruiterFeedback = :feedback WHERE id = :id")
  suspend fun updateApplicationStatus(id: String, status: String, feedback: String)

  @Delete
  suspend fun deleteApplication(application: JobApplicationEntity)

  @Query("DELETE FROM mock_job_applications")
  suspend fun deleteAllApplications()
}
