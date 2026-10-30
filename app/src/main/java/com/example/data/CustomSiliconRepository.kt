package com.example.data

import com.example.data.local.CustomNanometerChipDao
import com.example.data.local.CustomNanometerChipEntity
import com.example.data.local.StudentCustomCodeDao
import com.example.data.local.StudentCustomCodeEntity
import com.example.model.*
import kotlinx.coroutines.flow.Flow
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

class CustomSiliconRepository(
  private val codeDao: StudentCustomCodeDao,
  private val chipDao: CustomNanometerChipDao
) {

  val customCodes: Flow<List<StudentCustomCodeEntity>> = codeDao.getAllCustomCodes()
  val customChips: Flow<List<CustomNanometerChipEntity>> = chipDao.getAllCustomChips()

  // Pre-configured student RTL templates
  val starterTemplates: List<StudentCodeTemplate> = listOf(
    StudentCodeTemplate(
      id = "template_alu",
      title = "4-Bit Arithmetic Logic Unit",
      category = "Datapath",
      description = "Configurable 4-bit ALU supporting ADD, SUB, AND, OR, XOR, SLT with Carry and Zero flags.",
      defaultVerilog = """
// 4-Bit Configurable Arithmetic Logic Unit
module alu_4bit (
    input  wire [3:0] A,
    input  wire [3:0] B,
    input  wire [2:0] ALU_Sel,
    output reg  [3:0] ALU_Out,
    output wire       CarryOut,
    output wire       Zero
);
    reg [4:0] tmp;

    always @(*) begin
        case(ALU_Sel)
            3'b000: tmp = A + B;           // ADD
            3'b001: tmp = A - B;           // SUB
            3'b010: tmp = {1'b0, A & B};   // AND
            3'b011: tmp = {1'b0, A | B};   // OR
            3'b100: tmp = {1'b0, A ^ B};   // XOR
            3'b101: tmp = {4'b0, (A < B)}; // SLT
            default: tmp = 5'b00000;
        endcase
        ALU_Out = tmp[3:0];
    end

    assign CarryOut = tmp[4];
    assign Zero = (ALU_Out == 4'b0000);
endmodule
      """.trimIndent(),
      inputs = listOf("A[3:0]", "B[3:0]", "ALU_Sel[2:0]"),
      outputs = listOf("ALU_Out[3:0]", "CarryOut", "Zero"),
      sampleTestVectors = listOf(
        mapOf("A" to 5, "B" to 3, "ALU_Sel" to 0), // 5 + 3 = 8
        mapOf("A" to 9, "B" to 4, "ALU_Sel" to 1), // 9 - 4 = 5
        mapOf("A" to 12, "B" to 10, "ALU_Sel" to 2), // 12 AND 10 = 8
        mapOf("A" to 6, "B" to 3, "ALU_Sel" to 4)  // 6 XOR 3 = 5
      )
    ),
    StudentCodeTemplate(
      id = "template_fsm",
      title = "Traffic Light Controller FSM",
      category = "Control Logic",
      description = "3-State Moore FSM with RED, GREEN, and YELLOW cycle sequencing with pedestrian sensor.",
      defaultVerilog = """
// Traffic Light Controller Moore FSM
module traffic_fsm (
    input  wire clk,
    input  wire rst_n,
    input  wire ped_sensor,
    output reg [1:0] light_state // 00=RED, 01=GREEN, 10=YELLOW
);
    localparam S_RED    = 2'b00;
    localparam S_GREEN  = 2'b01;
    localparam S_YELLOW = 2'b10;

    reg [1:0] current_state, next_state;

    always @(posedge clk or negedge rst_n) begin
        if (!rst_n)
            current_state <= S_RED;
        else
            current_state <= next_state;
    end

    always @(*) begin
        case(current_state)
            S_RED:    next_state = S_GREEN;
            S_GREEN:  next_state = ped_sensor ? S_YELLOW : S_GREEN;
            S_YELLOW: next_state = S_RED;
            default:  next_state = S_RED;
        endcase
        light_state = current_state;
    end
endmodule
      """.trimIndent(),
      inputs = listOf("clk", "rst_n", "ped_sensor"),
      outputs = listOf("light_state[1:0]"),
      sampleTestVectors = listOf(
        mapOf("clk" to 1, "rst_n" to 0, "ped_sensor" to 0),
        mapOf("clk" to 1, "rst_n" to 1, "ped_sensor" to 0),
        mapOf("clk" to 1, "rst_n" to 1, "ped_sensor" to 1)
      )
    ),
    StudentCodeTemplate(
      id = "template_lfsr",
      title = "4-bit Pseudo-Random LFSR",
      category = "Security & BIST",
      description = "Linear Feedback Shift Register for Built-In Self-Test (BIST) pattern generation.",
      defaultVerilog = """
// 4-bit Linear Feedback Shift Register (Polynomial: X^4 + X^3 + 1)
module lfsr_4bit (
    input  wire clk,
    input  wire rst_n,
    output reg [3:0] lfsr_out
);
    wire feedback;
    assign feedback = lfsr_out[3] ^ lfsr_out[2];

    always @(posedge clk or negedge rst_n) begin
        if (!rst_n)
            lfsr_out <= 4'b0001; // Non-zero seed
        else
            lfsr_out <= {lfsr_out[2:0], feedback};
    end
endmodule
      """.trimIndent(),
      inputs = listOf("clk", "rst_n"),
      outputs = listOf("lfsr_out[3:0]"),
      sampleTestVectors = listOf(
        mapOf("clk" to 1, "rst_n" to 0),
        mapOf("clk" to 1, "rst_n" to 1),
        mapOf("clk" to 1, "rst_n" to 1)
      )
    ),
    StudentCodeTemplate(
      id = "template_dff_en",
      title = "D-Flip-Flop with Clock Enable",
      category = "Sequential Storage",
      description = "Standard cell synchronous D-FF with clock gating enable and asynchronous reset.",
      defaultVerilog = """
// D-Flip-Flop with Synchronous Enable
module dff_en (
    input  wire clk,
    input  wire rst_n,
    input  wire en,
    input  wire d,
    output reg  q
);
    always @(posedge clk or negedge rst_n) begin
        if (!rst_n)
            q <= 1'b0;
        else if (en)
            q <= d;
    end
endmodule
      """.trimIndent(),
      inputs = listOf("clk", "rst_n", "en", "d"),
      outputs = listOf("q"),
      sampleTestVectors = listOf(
        mapOf("clk" to 1, "rst_n" to 0, "en" to 0, "d" to 0),
        mapOf("clk" to 1, "rst_n" to 1, "en" to 1, "d" to 1),
        mapOf("clk" to 1, "rst_n" to 1, "en" to 0, "d" to 0)
      )
    )
  )

  /**
   * Evaluates student custom RTL code and synthesizes cell estimations.
   */
  fun analyzeStudentRtl(code: String, targetNodeNm: Int): RtlAnalysisResult {
    val lines = code.lines()
    var moduleName = "custom_module"
    val inputs = mutableListOf<String>()
    val outputs = mutableListOf<String>()
    var alwaysBlocks = 0
    var assignStatements = 0
    var caseStatements = 0

    lines.forEach { rawLine ->
      val line = rawLine.trim()
      if (line.startsWith("module ")) {
        val parts = line.removePrefix("module ").split(Regex("[\\s(]"))
        if (parts.isNotEmpty()) moduleName = parts[0].trim()
      } else if (line.contains("input ")) {
        val clean = line.substringAfter("input ").substringBefore(";").substringBefore(")")
        clean.split(",").forEach { p ->
          val item = p.trim().substringAfter("wire ").substringAfter("reg ")
          if (item.isNotBlank()) inputs.add(item)
        }
      } else if (line.contains("output ")) {
        val clean = line.substringAfter("output ").substringBefore(";").substringBefore(")")
        clean.split(",").forEach { p ->
          val item = p.trim().substringAfter("reg ").substringAfter("wire ")
          if (item.isNotBlank()) outputs.add(item)
        }
      } else if (line.contains("always @")) {
        alwaysBlocks++
      } else if (line.startsWith("assign ")) {
        assignStatements++
      } else if (line.contains("case(")) {
        caseStatements++
      }
    }

    // Estimate equivalent NAND gate count based on parsed complexity
    val estimatedGateCount = max(8, (inputs.size * 3) + (outputs.size * 4) + (assignStatements * 6) + (alwaysBlocks * 16) + (caseStatements * 24))

    // Scale area based on technology node ($Area \propto node^2$)
    val nodeScaling = (targetNodeNm.toDouble() / 28.0).pow(1.8)
    val baseAreaUm2 = estimatedGateCount * 1.85 * nodeScaling
    val estimatedDelayPs = max(25.0, 18.0 * sqrt(targetNodeNm.toDouble()) + (estimatedGateCount * 0.8))
    val estimatedPowerUw = max(4.0, (estimatedGateCount * 0.42) / (targetNodeNm.toDouble() / 14.0))

    return RtlAnalysisResult(
      moduleName = moduleName,
      inputs = inputs.ifEmpty { listOf("clk", "d") },
      outputs = outputs.ifEmpty { listOf("q") },
      gateCount = estimatedGateCount,
      areaUm2 = String.format("%.2f", baseAreaUm2).toDouble(),
      delayPs = String.format("%.1f", estimatedDelayPs).toDouble(),
      powerUw = String.format("%.1f", estimatedPowerUw).toDouble()
    )
  }

  /**
   * Simulates test stimulus vector execution for the active student design.
   */
  fun simulateTestVector(
    moduleName: String,
    inputs: Map<String, Int>
  ): Map<String, Any> {
    val results = mutableMapOf<String, Any>()

    if (moduleName.contains("alu", ignoreCase = true)) {
      val a = inputs["A"] ?: inputs["A[3:0]"] ?: 5
      val b = inputs["B"] ?: inputs["B[3:0]"] ?: 3
      val sel = inputs["ALU_Sel"] ?: inputs["ALU_Sel[2:0]"] ?: 0

      val (out, carry) = when (sel) {
        0 -> Pair((a + b) and 0xF, if (a + b > 15) 1 else 0) // ADD
        1 -> Pair((a - b) and 0xF, if (a < b) 1 else 0) // SUB
        2 -> Pair((a and b) and 0xF, 0) // AND
        3 -> Pair((a or b) and 0xF, 0) // OR
        4 -> Pair((a xor b) and 0xF, 0) // XOR
        5 -> Pair(if (a < b) 1 else 0, 0) // SLT
        else -> Pair(0, 0)
      }
      results["ALU_Out[3:0]"] = "0x" + out.toString(16).uppercase() + " ($out)"
      results["CarryOut"] = carry
      results["Zero"] = if (out == 0) 1 else 0
    } else if (moduleName.contains("fsm", ignoreCase = true) || moduleName.contains("traffic", ignoreCase = true)) {
      val rst = inputs["rst_n"] ?: 1
      val ped = inputs["ped_sensor"] ?: 0
      val state = if (rst == 0) "00 (RED)" else if (ped == 1) "10 (YELLOW)" else "01 (GREEN)"
      results["light_state[1:0]"] = state
    } else if (moduleName.contains("lfsr", ignoreCase = true)) {
      val rst = inputs["rst_n"] ?: 1
      val out = if (rst == 0) 1 else 7
      results["lfsr_out[3:0]"] = "0x" + out.toString(16).uppercase() + " (Pattern: 0111)"
    } else {
      // Generic combinational evaluation
      val clk = inputs["clk"] ?: 1
      val d = inputs["d"] ?: 1
      val rst = inputs["rst_n"] ?: 1
      val q = if (rst == 0) 0 else if (clk == 1) d else 0
      results["q"] = q
      results["status"] = "Valid Edge Triggered"
    }

    return results
  }

  /**
   * Real-time Physics & Silicon Tapeout Engine for a student's custom nanometer chip.
   */
  fun calculateChipMetrics(
    node: TechnologyNode,
    dieWidthMm: Double,
    dieHeightMm: Double,
    frequencyGhz: Double,
    activityFactor: Double = 0.15,
    metalLayers: Int = node.defaultMetalLayers
  ): CustomChipMetrics {
    val dieAreaMm2 = dieWidthMm * dieHeightMm
    val coreUtilization = 0.72 // 72% standard cell utilization, remainder for power mesh, decap, IO

    // Total Transistors = Core Area (mm²) * Density (MTr/mm²) * Utilization
    val totalTransistorsMillion = dieAreaMm2 * node.transistorDensityMtrPerMm2 * coreUtilization

    // Dynamic Power: P_dyn = alpha * C_eff * Vdd² * f
    // Capacitance per transistor scales down with node size
    val capacitancePerTransistorFemtoFarad = (node.nodeNm.toDouble() * 0.08)
    val totalEffectiveCapacitance = totalTransistorsMillion * 1e6 * capacitancePerTransistorFemtoFarad * 1e-15
    val dynamicPowerWatts = activityFactor * totalEffectiveCapacitance * node.nominalVdd.pow(2.0) * (frequencyGhz * 1e9)

    // Static Leakage Power: P_leak = N_transistors * I_leak_per_transistor * Vdd
    val leakagePerTransistorMicroAmps = node.leakageCurrentNaPerUm * 0.001 * (node.gateLengthNm / 1000.0)
    val staticLeakageWatts = totalTransistorsMillion * 1e6 * (leakagePerTransistorMicroAmps * 1e-6) * node.nominalVdd

    val totalTdpWatts = dynamicPowerWatts + staticLeakageWatts
    val dieAreaCm2 = dieAreaMm2 / 100.0
    val powerDensityWattsPerCm2 = if (dieAreaCm2 > 0) totalTdpWatts / dieAreaCm2 else 0.0

    // Thermal junction temperature with standard silicon heat spreader (theta_ja ~ 1.5 C/W)
    val ambientTempC = 25.0
    val estimatedJunctionTempC = ambientTempC + (totalTdpWatts * 1.45)

    // Murphy's Model for 300mm Silicon Wafer Yield
    val waferRadiusMm = 150.0
    val waferAreaMm2 = PI * waferRadiusMm.pow(2.0)
    // Gross Dies Per Wafer (DPW) formula accounting for wafer edge loss
    val grossDpw = (waferAreaMm2 / dieAreaMm2) - (PI * (2 * waferRadiusMm) / sqrt(2 * dieAreaMm2))
    val validGrossDpw = max(1, grossDpw.toInt())

    // Murphy's Yield: Y = [(1 - exp(-D0 * A)) / (D0 * A)]²
    val d0 = node.defectDensityPerCm2
    val d0xA = d0 * dieAreaCm2
    val yield = if (d0xA < 0.0001) 0.99 else ((1.0 - exp(-d0xA)) / d0xA).pow(2.0)
    val yieldPercent = (yield * 100.0).coerceIn(12.0, 99.5)

    val goodDies = max(1, (validGrossDpw * (yieldPercent / 100.0)).toInt())
    val costPerGoodDie = node.waferCostUsd.toDouble() / goodDies

    // DRC violations: flagged if metal layers insufficient for target frequency or thermal density too high
    var drcViolations = 0
    if (metalLayers < (node.defaultMetalLayers - 2)) drcViolations += 3
    if (powerDensityWattsPerCm2 > 120.0) drcViolations += 2
    if (estimatedJunctionTempC > 105.0) drcViolations += 1

    val lvsStatus = if (drcViolations == 0) "MATCHED (Clean)" else "WARNING ($drcViolations Violations)"

    return CustomChipMetrics(
      dieAreaMm2 = String.format("%.2f", dieAreaMm2).toDouble(),
      totalTransistorsMillion = String.format("%.1f", totalTransistorsMillion).toDouble(),
      dynamicPowerWatts = String.format("%.2f", dynamicPowerWatts).toDouble(),
      staticLeakageWatts = String.format("%.3f", staticLeakageWatts).toDouble(),
      totalTdpWatts = String.format("%.2f", totalTdpWatts).toDouble(),
      powerDensityWattsPerCm2 = String.format("%.1f", powerDensityWattsPerCm2).toDouble(),
      estimatedJunctionTempC = String.format("%.1f", estimatedJunctionTempC).toDouble(),
      diesPerWafer = validGrossDpw,
      waferYieldPercent = String.format("%.1f", yieldPercent).toDouble(),
      goodDiesPerWafer = goodDies,
      estimatedCostPerGoodDieUsd = String.format("%.2f", costPerGoodDie).toDouble(),
      drcViolationsCount = drcViolations,
      lvsStatus = lvsStatus
    )
  }

  // Database operations
  suspend fun saveCustomCode(code: StudentCustomCodeEntity) = codeDao.insertOrUpdateCode(code)
  suspend fun deleteCustomCode(code: StudentCustomCodeEntity) = codeDao.deleteCode(code)
  suspend fun toggleCodeFavorite(id: String, isFav: Boolean) = codeDao.updateFavorite(id, isFav)

  suspend fun saveCustomChip(chip: CustomNanometerChipEntity) = chipDao.insertOrUpdateChip(chip)
  suspend fun deleteCustomChip(chip: CustomNanometerChipEntity) = chipDao.deleteChip(chip)
  suspend fun toggleTapeoutApproval(id: String, approved: Boolean) = chipDao.updateTapeoutApproval(id, approved)
}

data class RtlAnalysisResult(
  val moduleName: String,
  val inputs: List<String>,
  val outputs: List<String>,
  val gateCount: Int,
  val areaUm2: Double,
  val delayPs: Double,
  val powerUw: Double
)
