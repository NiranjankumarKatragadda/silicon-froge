package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomNanometerChipDao {
  @Query("SELECT * FROM custom_nanometer_chips ORDER BY createdTimestamp DESC")
  fun getAllCustomChips(): Flow<List<CustomNanometerChipEntity>>

  @Query("SELECT * FROM custom_nanometer_chips WHERE id = :id")
  suspend fun getChipById(id: String): CustomNanometerChipEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdateChip(chip: CustomNanometerChipEntity)

  @Delete
  suspend fun deleteChip(chip: CustomNanometerChipEntity)

  @Query("UPDATE custom_nanometer_chips SET isTapeoutApproved = :approved WHERE id = :id")
  suspend fun updateTapeoutApproval(id: String, approved: Boolean)

  @Query("SELECT COUNT(*) FROM custom_nanometer_chips")
  suspend fun getChipCount(): Int
}
