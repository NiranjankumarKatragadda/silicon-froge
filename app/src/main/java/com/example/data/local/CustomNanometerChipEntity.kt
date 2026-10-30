package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_nanometer_chips")
data class CustomNanometerChipEntity(
  @PrimaryKey
  val id: String,
  val chipName: String,
  val codename: String,
  val technologyNodeNm: Int, // e.g. 180, 65, 28, 16, 7, 3, 2
  val dieWidthMm: Double,
  val dieHeightMm: Double,
  val supplyVoltageV: Double,
  val targetClockGhz: Double,
  val metalLayersCount: Int,
  val targetApplication: String, // e.g. "Edge AI Co-Processor", "RISC-V Microcontroller", "Automotive Sensor Hub"
  val totalTransistorsMillion: Double,
  val totalTdpWatts: Double,
  val dieAreaMm2: Double,
  val diesPerWafer: Int,
  val waferYieldPercent: Double,
  val drcStatus: String, // "PASS", "WARNING"
  val lvsStatus: String, // "MATCHED", "UNCHECKED"
  val isTapeoutApproved: Boolean = false,
  val createdTimestamp: Long = System.currentTimeMillis()
)
