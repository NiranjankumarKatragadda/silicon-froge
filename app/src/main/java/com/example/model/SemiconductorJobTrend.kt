package com.example.model

data class SemiconductorJobTrend(
  val id: String,
  val domain: String,
  val hiringGrowthYoY: Float, // e.g. 42.5%
  val activeOpeningsGlobal: Int,
  val averageBaseSalaryUsd: Int,
  val topFoundryNodes: List<String>,
  val topRequiredTools: List<String>,
  val topHiringCompanies: List<String>,
  val keyTrendInsight: String,
  val demandRating: String // "CRITICAL SURGE", "VERY HIGH DEMAND", "STEADY GROWTH"
)

data class SemiconductorJobPosting(
  val id: String,
  val jobTitle: String,
  val company: String,
  val location: String,
  val domain: String,
  val experienceLevel: String,
  val salaryRange: String,
  val targetFabNode: String,
  val edaStack: List<String>,
  val description: String,
  val responsibilities: List<String>,
  val minimumQualifications: List<String>,
  val postedTimestamp: Long = System.currentTimeMillis() - (1000L * 60 * 60 * 24 * 2), // 2 days ago default
  val isTrendingRole: Boolean = true,
  val applicantCount: Int = 24
)

data class MarketTelemetrySummary(
  val indexName: String = "SOX Global Silicon Hiring Index",
  val currentIndexValue: Float = 142.8f,
  val quarterGrowthPercentage: Float = +12.4f,
  val totalActiveRequisitions: Int = 26840,
  val topGrowingRegion: String = "Austin, TX & Hsinchu Science Park",
  val lastSyncTime: Long = System.currentTimeMillis()
)
