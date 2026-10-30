package com.example

import com.example.data.CircuitDesignRepository
import com.example.data.SiliconLabRepository
import com.example.model.CmosInverterSpec
import com.example.model.LogicGateType
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testTimingSlackCalculation_detectsSetupAndHold() {
    val timing = SiliconLabRepository.computeTimingSlack(
      clockFreqMhz = 1000f, // 1ns period
      tCqNs = 0.20f,
      tCombNs = 0.40f,
      tSetupNs = 0.15f,
      tHoldNs = 0.08f,
      clockSkewNs = 0.02f
    )
    assertFalse(timing.hasSetupViolation)
    assertTrue(timing.setupSlackNs > 0.2f)
    assertFalse(timing.hasHoldViolation)
  }

  @Test
  fun testCmosVtcSimulation_yieldsValidNoiseMargins() {
    val spec = CmosInverterSpec(
      vdd = 1.2f,
      wnLnRatio = 2.0f,
      wpLpRatio = 5.0f
    )
    val vtc = SiliconLabRepository.simulateCmosVtc(spec)
    assertTrue("VM should be close to VDD/2", vtc.vthM in 0.5f..0.7f)
    assertTrue("NML must be positive", vtc.nmL > 0.1f)
    assertTrue("NMH must be positive", vtc.nmH > 0.1f)
    assertEquals(41, vtc.points.size)
  }

  @Test
  fun testWaferDieGeneration_yieldsReasonableBinCounts() {
    val dies = SiliconLabRepository.generateWaferDies()
    assertTrue("Should generate circular grid of dies", dies.size > 80)
    val bin1Count = dies.count { it.binNumber == 1 }
    assertTrue("Majority of dies should be Bin 1 (Pass)", bin1Count > dies.size * 0.5)
  }

  @Test
  fun testLogicGateEvaluation_NandAndXorTruthTables() {
    // Test NAND Gate
    assertEquals(true, LogicGateType.NAND.evaluate(listOf(false, false))["Y"])
    assertEquals(true, LogicGateType.NAND.evaluate(listOf(false, true))["Y"])
    assertEquals(true, LogicGateType.NAND.evaluate(listOf(true, false))["Y"])
    assertEquals(false, LogicGateType.NAND.evaluate(listOf(true, true))["Y"])

    // Test XOR Gate
    assertEquals(false, LogicGateType.XOR.evaluate(listOf(false, false))["Y"])
    assertEquals(true, LogicGateType.XOR.evaluate(listOf(false, true))["Y"])
    assertEquals(true, LogicGateType.XOR.evaluate(listOf(true, false))["Y"])
    assertEquals(false, LogicGateType.XOR.evaluate(listOf(true, true))["Y"])

    // Test Full Adder
    val faOut = LogicGateType.FULL_ADDER.evaluate(listOf(true, true, true))
    assertEquals(true, faOut["SUM"])
    assertEquals(true, faOut["COUT"])
  }

  @Test
  fun testStructuralCircuitModule_4NandXorEquivalence() {
    val xorModule = CircuitDesignRepository.circuitModules.first { it.id == "mod_nand_xor" }

    // Test all 4 input combinations against XOR truth table
    assertEquals(false, xorModule.simulate(mapOf("A" to false, "B" to false))["Y"])
    assertEquals(true, xorModule.simulate(mapOf("A" to false, "B" to true))["Y"])
    assertEquals(true, xorModule.simulate(mapOf("A" to true, "B" to false))["Y"])
    assertEquals(false, xorModule.simulate(mapOf("A" to true, "B" to true))["Y"])
  }

  @Test
  fun testIndustrialJobWorkflows_MilestonesLoaded() {
    val workflows = CircuitDesignRepository.industrialJobWorkflows
    assertTrue("Should define multiple industry job workflows", workflows.size >= 2)
    val nvidiaFlow = workflows.first { it.workflowId == "flow_nvidia_timing" }
    assertEquals(3, nvidiaFlow.milestones.size)

    // Test milestone 1 verification rule with optimal 4 stages
    val passResult = nvidiaFlow.milestones[0].verificationRule(mapOf("logic_stages" to 4f))
    assertTrue(passResult.first)
  }
}
