package com.example.model

data class OscilloscopeState(
  val isRunning: Boolean = true,
  val timeBaseMs: Float = 1.0f,
  val ch1VoltsPerDiv: Float = 1.0f,
  val ch2VoltsPerDiv: Float = 1.0f,
  val ch1Enabled: Boolean = true,
  val ch2Enabled: Boolean = true,
  val triggerLevelV: Float = 0.6f,
  val waveformSource: String = "CLOCK_GEN" // "CLOCK_GEN", "CMOS_INVERTER", "RING_OSC", "SCAN_CHAIN"
)

data class ParametricMeasurementUnit(
  val forceVoltage: Float = 1.2f,
  val complianceCurrentMa: Float = 10.0f,
  val measuredCurrentUa: Float = 14.2f,
  val pinName: String = "VDD_CORE_PAD_24",
  val isOutputEnabled: Boolean = true
)

data class CleanroomSafetyState(
  val esdStrapGroundOk: Boolean = true,
  val bunnySuitParticlePpm: Int = 12, // ISO 3 requirement < 35
  val silaneScrubberActive: Boolean = true,
  val hfAcidSpillKitVerified: Boolean = true,
  val cleanroomIsoClass: String = "ISO Class 3 (1nm - 5nm spec)",
  val emergencyExhaustOk: Boolean = true
)

data class LabLogEntry(
  val id: String,
  val timestamp: Long,
  val domain: DomainType,
  val experimentTitle: String,
  val recordedValues: String,
  val remarks: String
)
