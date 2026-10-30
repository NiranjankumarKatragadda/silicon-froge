package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentCustomCodeDao {
  @Query("SELECT * FROM student_custom_codes ORDER BY lastModifiedTimestamp DESC")
  fun getAllCustomCodes(): Flow<List<StudentCustomCodeEntity>>

  @Query("SELECT * FROM student_custom_codes WHERE id = :id")
  suspend fun getCodeById(id: String): StudentCustomCodeEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdateCode(code: StudentCustomCodeEntity)

  @Delete
  suspend fun deleteCode(code: StudentCustomCodeEntity)

  @Query("UPDATE student_custom_codes SET isFavorite = :isFavorite WHERE id = :id")
  suspend fun updateFavorite(id: String, isFavorite: Boolean)

  @Query("SELECT COUNT(*) FROM student_custom_codes")
  suspend fun getCodeCount(): Int
}
