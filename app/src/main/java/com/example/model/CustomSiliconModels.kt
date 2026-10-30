package com.example.model

/**
 * Supported Semiconductor Technology Nodes from legacy planar to next-gen CFET.
 */
enum class TechnologyNode(
  val nodeNm: Int,
  val displayName: String,
  val transistorArchitecture: String,
  val nominalVdd: Double,
  val gateLengthNm: Double,
  val oxideThicknessNm: Double,
  val transistorDensityMtrPerMm2: Double, // Million transistors per mm²
  val leakageCurrentNaPerUm: Double,
  val maxFrequencyGhz: Double,
  val defaultMetalLayers: Int,
  val waferCostUsd: Int,
  val defectDensityPerCm2: Double
) {
  NODE_180NM(
    nodeNm = 180,
    displayName = "180nm Planar CMOS",
    transistorArchitecture = "Planar Bulk MOSFET",
    nominalVdd = 1.80,
    gateLengthNm = 180.0,
    oxideThicknessNm = 3.5,
    transistorDensityMtrPerMm2 = 0.15,
    leakageCurrentNaPerUm = 0.01,
    maxFrequencyGhz = 0.4,
    defaultMetalLayers = 5,
    waferCostUsd = 1200,
    defectDensityPerCm2 = 0.04
  ),
  NODE_65NM(
    nodeNm = 65,
    displayName = "65nm Low-Power CMOS",
    transistorArchitecture = "Planar Strain Si / NiSi",
    nominalVdd = 1.20,
    gateLengthNm = 65.0,
    oxideThicknessNm = 1.6,
    transistorDensityMtrPerMm2 = 1.6,
    leakageCurrentNaPerUm = 0.12,
    maxFrequencyGhz = 1.2,
    defaultMetalLayers = 7,
    waferCostUsd = 2100,
    defectDensityPerCm2 = 0.06
  ),
  NODE_28NM(
    nodeNm = 28,
    displayName = "28nm HKMG / FD-SOI",
    transistorArchitecture = "High-k Metal Gate / Ultra-Thin Body",
    nominalVdd = 0.90,
    gateLengthNm = 28.0,
    oxideThicknessNm = 1.1,
    transistorDensityMtrPerMm2 = 15.0,
    leakageCurrentNaPerUm = 1.1,
    maxFrequencyGhz = 2.4,
    defaultMetalLayers = 9,
    waferCostUsd = 3600,
    defectDensityPerCm2 = 0.07
  ),
  NODE_16NM(
    nodeNm = 16,
    displayName = "16nm FinFET",
    transistorArchitecture = "3D Tri-Gate FinFET",
    nominalVdd = 0.80,
    gateLengthNm = 16.0,
    oxideThicknessNm = 0.95,
    transistorDensityMtrPerMm2 = 42.0,
    leakageCurrentNaPerUm = 2.4,
    maxFrequencyGhz = 3.6,
    defaultMetalLayers = 11,
    waferCostUsd = 6500,
    defectDensityPerCm2 = 0.08
  ),
  NODE_7NM(
    nodeNm = 7,
    displayName = "7nm EUV FinFET",
    transistorArchitecture = "EUV Self-Aligned Quad Patterning FinFET",
    nominalVdd = 0.75,
    gateLengthNm = 7.0,
    oxideThicknessNm = 0.82,
    transistorDensityMtrPerMm2 = 98.0,
    leakageCurrentNaPerUm = 4.8,
    maxFrequencyGhz = 4.5,
    defaultMetalLayers = 13,
    waferCostUsd = 10500,
    defectDensityPerCm2 = 0.09
  ),
  NODE_3NM(
    nodeNm = 3,
    displayName = "3nm GAAFET (Nanosheet)",
    transistorArchitecture = "Multi-Bridge-Channel Nanosheet (MBCFET)",
    nominalVdd = 0.70,
    gateLengthNm = 3.0,
    oxideThicknessNm = 0.70,
    transistorDensityMtrPerMm2 = 215.0,
    leakageCurrentNaPerUm = 8.2,
    maxFrequencyGhz = 5.0,
    defaultMetalLayers = 14,
    waferCostUsd = 17500,
    defectDensityPerCm2 = 0.11
  ),
  NODE_2NM(
    nodeNm = 2,
    displayName = "2nm Angstrom CFET",
    transistorArchitecture = "Complementary FET + Backside Power (BSPDN)",
    nominalVdd = 0.65,
    gateLengthNm = 2.0,
    oxideThicknessNm = 0.62,
    transistorDensityMtrPerMm2 = 310.0,
    leakageCurrentNaPerUm = 11.5,
    maxFrequencyGhz = 5.4,
    defaultMetalLayers = 15,
    waferCostUsd = 24000,
    defectDensityPerCm2 = 0.13
  );

  companion object {
    fun fromNodeNm(nm: Int): TechnologyNode {
      return values().firstOrNull { it.nodeNm == nm } ?: NODE_28NM
    }
  }
}

/**
 * Floorplan Macro Block on a student's custom nanometer die.
 */
data class CustomDieBlock(
  val id: String,
  val name: String,
  val relativeX: Float, // 0.0 to 1.0
  val relativeY: Float, // 0.0 to 1.0
  val relativeWidth: Float,
  val relativeHeight: Float,
  val colorHex: Long,
  val category: String, // "COMPUTE", "MEMORY", "ANALOG", "I_O"
  val powerAllocationPct: Double
)

/**
 * Real-time computed silicon tapeout metrics for a custom student chip.
 */
data class CustomChipMetrics(
  val dieAreaMm2: Double,
  val totalTransistorsMillion: Double,
  val dynamicPowerWatts: Double,
  val staticLeakageWatts: Double,
  val totalTdpWatts: Double,
  val powerDensityWattsPerCm2: Double,
  val estimatedJunctionTempC: Double,
  val diesPerWafer: Int,
  val waferYieldPercent: Double,
  val goodDiesPerWafer: Int,
  val estimatedCostPerGoodDieUsd: Double,
  val drcViolationsCount: Int,
  val lvsStatus: String
)

/**
 * Pre-configured starter RTL templates for student practice.
 */
data class StudentCodeTemplate(
  val id: String,
  val title: String,
  val category: String,
  val description: String,
  val defaultVerilog: String,
  val inputs: List<String>,
  val outputs: List<String>,
  val sampleTestVectors: List<Map<String, Int>>
)
