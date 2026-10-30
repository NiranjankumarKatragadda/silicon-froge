package com.example.model

data class DigitalCircuit(
  val id: String,
  val name: String,
  val description: String,
  val inputs: List<String>,
  val outputs: List<String>,
  val verilogSnippet: String,
  val propagationDelayNs: Float,
  val minSetupTimeNs: Float,
  val minHoldTimeNs: Float
)

data class TimingAnalysisResult(
  val clockFreqMhz: Float,
  val clockPeriodNs: Float,
  val tCqNs: Float,
  val tCombNs: Float,
  val tSetupNs: Float,
  val tHoldNs: Float,
  val clockSkewNs: Float,
  val setupSlackNs: Float,
  val holdSlackNs: Float,
  val hasSetupViolation: Boolean,
  val hasHoldViolation: Boolean
)

data class VtcCurvePoint(
  val vin: Float,
  val vout: Float,
  val nmosRegion: String,
  val pmosRegion: String
)

data class CmosInverterSpec(
  val vdd: Float = 1.2f,
  val vtn: Float = 0.35f,
  val vtp: Float = 0.35f, // magnitude
  val wnLnRatio: Float = 2.0f,
  val wpLpRatio: Float = 4.0f,
  val knPrime: Float = 200f, // uA/V^2
  val kpPrime: Float = 80f   // uA/V^2
)

data class CmosVtcAnalysis(
  val vthM: Float,
  val vil: Float,
  val vih: Float,
  val vol: Float,
  val voh: Float,
  val nmL: Float,
  val nmH: Float,
  val betaRatio: Float,
  val points: List<VtcCurvePoint>
)

data class FabStep(
  val stepNumber: Int,
  val name: String,
  val description: String,
  val chamberTempCelsius: Float,
  val chemicalOrGas: String,
  val thicknessAngstroms: Float,
  val hazardType: String,
  val safetyCheck: String,
  val layerName: String,
  val isCompleted: Boolean = false
)

data class MacroBlock(
  val id: String,
  val name: String,
  val widthPercent: Float,
  val heightPercent: Float,
  val posXPercent: Float,
  val posYPercent: Float,
  val powerConsumptionMw: Float,
  val isPlaced: Boolean = true
)

data class FloorplanState(
  val dieAspectRatio: Float = 1.0f,
  val coreUtilizationPercent: Float = 70f,
  val powerStripeCount: Int = 6,
  val maxIrDropMv: Float = 32f,
  val drcViolationCount: Int = 0,
  val drcDetails: List<String> = emptyList(),
  val macros: List<MacroBlock> = emptyList()
)

data class AtePinChannel(
  val pinNumber: Int,
  val signalName: String,
  val driveState: String, // "H", "L", "Z"
  val expectState: String,
  val measuredVoltage: Float,
  val isPassing: Boolean
)

data class WaferDie(
  val id: Int,
  val x: Int,
  val y: Int,
  val binNumber: Int, // 1: Pass, 2: Leakage, 3: Timing, 4: Hard Fault
  val fmaxGhz: Float,
  val iddqUa: Float
)

data class SchmooPoint(
  val voltage: Float,
  val freqGhz: Float,
  val isPassing: Boolean
)
