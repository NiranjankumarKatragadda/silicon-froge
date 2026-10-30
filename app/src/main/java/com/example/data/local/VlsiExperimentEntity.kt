package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vlsi_experiments")
data class VlsiExperimentEntity(
  @PrimaryKey
  val id: String,
  val title: String,
  val domain: String, // e.g. "CMOS", "Digital Logic", "Layout Design", "Wafer Fab", "Silicon Test"
  val description: String,
  val theoreticalFormula: String,
  val difficulty: String, // "Beginner", "Intermediate", "Advanced"
  val durationMinutes: Int,
  val isCompleted: Boolean = false,
  val isFavorite: Boolean = false,
  val keyLearningObjectives: String,
  val industryApplication: String
)
