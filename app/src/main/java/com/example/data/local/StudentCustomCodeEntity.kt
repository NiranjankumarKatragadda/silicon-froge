package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "student_custom_codes")
data class StudentCustomCodeEntity(
  @PrimaryKey
  val id: String,
  val title: String,
  val description: String,
  val moduleName: String,
  val verilogCode: String,
  val targetNodeNm: Int = 28,
  val inputsList: String, // Comma separated, e.g. "clk,rst,a[3:0],b[3:0]"
  val outputsList: String, // Comma separated, e.g. "result[3:0],carry,zero"
  val estimatedGateCount: Int = 36,
  val estimatedAreaUm2: Double = 145.2,
  val estimatedDelayPs: Double = 85.0,
  val estimatedPowerUw: Double = 24.6,
  val isFavorite: Boolean = false,
  val lastModifiedTimestamp: Long = System.currentTimeMillis()
)
