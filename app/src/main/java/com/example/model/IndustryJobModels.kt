package com.example.model

data class IndustryJob(
  val id: String,
  val title: String,
  val company: String,
  val location: String,
  val experienceLevel: String,
  val salaryRange: String,
  val domain: DomainType,
  val keyRequirements: List<String>,
  val industrialSimulationId: String
)

data class IndustryTestProcedure(
  val id: String,
  val title: String,
  val companyTarget: String,
  val deviceUnderStudy: String,
  val standard: String,
  val briefDescription: String,
  val fullScenario: String,
  val defaultVddCorner: Float,
  val defaultTempCelsius: Float,
  val defaultFreqGhz: Float,
  val defaultIddqLimitUa: Float,
  val failureSymptom: String,
  val diagnosisClues: List<String>,
  val targetFixDescription: String
)

data class SiliconTestReport(
  val id: String,
  val candidateName: String,
  val jobTitle: String,
  val company: String,
  val testProcedureTitle: String,
  val timestamp: Long,
  val finalYieldPercent: Float,
  val measuredIddqUa: Float,
  val achievedFrequencyGhz: Float,
  val isQualified: Boolean,
  val qualificationGrade: String,
  val failureMechanismResolved: String,
  val hiringRecommendation: String
)
