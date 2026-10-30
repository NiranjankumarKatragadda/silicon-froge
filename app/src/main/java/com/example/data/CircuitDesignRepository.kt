package com.example.data

import com.example.model.*

object CircuitDesignRepository {

  // ==========================================
  // PRE-BUILT CIRCUIT DESIGN MODULES
  // ==========================================
  val circuitModules = listOf(
    CircuitDesignModule(
      id = "mod_nand_xor",
      name = "Universal 4-NAND XOR Module",
      category = "Combinational VLSI",
      description = "Implements Y = A XOR B using exactly 4 two-input NAND gates, minimizing silicon area in standard cell libraries.",
      primaryInputs = listOf("A", "B"),
      primaryOutputs = listOf("Y"),
      gates = listOf(
        CircuitGateInstance("G1", LogicGateType.NAND, listOf("A", "B"), listOf("N1"), 0.25f, 0.5f),
        CircuitGateInstance("G2", LogicGateType.NAND, listOf("A", "N1"), listOf("N2"), 0.55f, 0.25f),
        CircuitGateInstance("G3", LogicGateType.NAND, listOf("N1", "B"), listOf("N3"), 0.55f, 0.75f),
        CircuitGateInstance("G4", LogicGateType.NAND, listOf("N2", "N3"), listOf("Y"), 0.85f, 0.5f)
      ),
      schematicAscii = """
         A ----+------------[ G2: NAND ]--- N2 ---+
               |               ^                  |
               +--[ G1: NAND ]-+                  +--[ G4: NAND ]---> Y
               |     (N1)      |                  |
         B ----+---------------+--[ G3: NAND ]--- N3 ---+
               |               v
               +---------------+
      """.trimIndent(),
      verilogImplementation = """
        module xor_4nand (
          input  wire A, B,
          output wire Y
        );
          wire n1, n2, n3;
          nand #0.06 g1 (n1, A, B);
          nand #0.06 g2 (n2, A, n1);
          nand #0.06 g3 (n3, n1, B);
          nand #0.06 g4 (Y, n2, n3);
        endmodule
      """.trimIndent()
    ),
    CircuitDesignModule(
      id = "mod_full_adder",
      name = "1-Bit Full Adder (Sum & Carry)",
      category = "Arithmetic Datapath",
      description = "Standard mirror full-adder cell computing Sum = A ^ B ^ Cin and Cout = (A & B) | (Cin & (A ^ B)).",
      primaryInputs = listOf("A", "B", "Cin"),
      primaryOutputs = listOf("SUM", "COUT"),
      gates = listOf(
        CircuitGateInstance("FA_BLOCK", LogicGateType.FULL_ADDER, listOf("A", "B", "Cin"), listOf("SUM", "COUT"), 0.5f, 0.5f)
      ),
      schematicAscii = """
         A ----\
         B -----+--[ Full Adder Cell ]---> SUM
         Cin --/                    \---> COUT
      """.trimIndent(),
      verilogImplementation = """
        module full_adder_cell (
          input  wire A, B, Cin,
          output wire SUM, COUT
        );
          assign #0.15 {COUT, SUM} = A + B + Cin;
        endmodule
      """.trimIndent()
    ),
    CircuitDesignModule(
      id = "mod_mux21_tg",
      name = "2:1 Multiplexer (Transmission Gate)",
      category = "Datapath Steering",
      description = "CMOS transmission gate multiplexer selecting between data input D0 and D1 based on Select line S.",
      primaryInputs = listOf("D0", "D1", "S"),
      primaryOutputs = listOf("Y"),
      gates = listOf(
        CircuitGateInstance("MUX_BLOCK", LogicGateType.MUX21, listOf("D0", "D1", "S"), listOf("Y"), 0.5f, 0.5f)
      ),
      schematicAscii = """
         D0 -----\
         D1 ------+--[ MUX 2:1 ]---> Y
         S  -----/
      """.trimIndent(),
      verilogImplementation = """
        module mux21_cell (
          input  wire D0, D1, S,
          output wire Y
        );
          assign #0.08 Y = S ? D1 : D0;
        endmodule
      """.trimIndent()
    ),
    CircuitDesignModule(
      id = "mod_d_latch",
      name = "Level-Sensitive D Latch",
      category = "Sequential Storage",
      description = "Transparent D-Latch holding data state when Enable EN is low and passing data when EN is high.",
      primaryInputs = listOf("D", "EN"),
      primaryOutputs = listOf("Q", "QN"),
      gates = listOf(
        CircuitGateInstance("DLAT_BLOCK", LogicGateType.D_LATCH, listOf("D", "EN"), listOf("Q", "QN"), 0.5f, 0.5f)
      ),
      schematicAscii = """
         D  -----\
                  +--[ Transparent D-Latch ]---> Q
         EN -----/                          \---> QN
      """.trimIndent(),
      verilogImplementation = """
        module d_latch (
          input  wire D, EN,
          output reg  Q, QN
        );
          always @(D or EN) begin
            if (EN) begin
              Q  <= D;
              QN <= !D;
            end
          end
        endmodule
      """.trimIndent()
    )
  )

  // ==========================================
  // EXTENSIBLE VLSI LAB EXPERIMENTS
  // ==========================================
  val virtualExperiments = listOf(
    VlsiExperimentDefinition(
      id = "exp_gate_fanout_rc",
      title = "CMOS Fan-Out & RC Delay Optimization",
      domain = DomainType.DIGITAL,
      objective = "Measure how increasing capacitive fan-out load (CL) impacts propagation delay (tpd) and dynamic power dissipation.",
      theoryMarkdown = "Propagation delay is governed by tpd = t_intrinsic + 0.69 * R_out * CL. For high fan-out nets, inserting an optimal inverter buffer chain with stage ratio alpha ≈ 3 to 4 minimizes total delay.",
      circuitTopologyDescription = "Inverter driver feeding N parallel capacitive gate loads (C_load = N * C_in).",
      defaultParameters = listOf(
        ExperimentParameter("fanout_count", "Fan-Out Load (N gates)", "cells", 4f, 1f, 16f, 1f, "Number of downstream logic gates driven by the cell."),
        ExperimentParameter("supply_vdd", "Supply Voltage (VDD)", "V", 1.20f, 0.70f, 1.40f, 0.05f, "Core power supply rail voltage."),
        ExperimentParameter("clock_freq", "Switching Frequency", "GHz", 1.50f, 0.20f, 3.00f, 0.10f, "Operating clock transition rate.")
      ),
      industryRelevance = "High fan-out net optimization is fundamental for clock distribution networks, reset trees, and 64-bit ALU datapath routing at Intel and TSMC.",
      calculateTelemetry = { params ->
        val n = params["fanout_count"] ?: 4f
        val vdd = params["supply_vdd"] ?: 1.20f
        val f = params["clock_freq"] ?: 1.50f
        val clFf = n * 12f + 8f
        val tpdPs = 45f + (clFf * 3.2f / vdd)
        val pDynUw = clFf * 1e-3f * vdd * vdd * f * 1000f
        mapOf(
          "Total CL" to "${String.format("%.1f", clFf)} fF",
          "Propagation Delay (tpd)" to "${String.format("%.1f", tpdPs)} ps",
          "Dynamic Power" to "${String.format("%.2f", pDynUw)} µW",
          "Slew Rate" to "${String.format("%.2f", vdd / (tpdPs * 1e-3f))} V/ns"
        )
      },
      evaluateRubric = { params ->
        val n = params["fanout_count"] ?: 4f
        val vdd = params["supply_vdd"] ?: 1.20f
        val clFf = n * 12f + 8f
        val tpdPs = 45f + (clFf * 3.2f / vdd)
        listOf(
          ExperimentRubricItem(
            criterionName = "Propagation Delay Threshold",
            targetSpecification = "tpd < 200 ps",
            achievedValue = "${String.format("%.1f", tpdPs)} ps",
            isMet = tpdPs < 200f,
            engineerFeedback = if (tpdPs < 200f) "Excellent. High-speed timing constraint satisfied." else "Warning: Excessive fanout. Consider inserting an intermediate buffer cell."
          ),
          ExperimentRubricItem(
            criterionName = "Supply Voltage Margin",
            targetSpecification = "0.90V <= VDD <= 1.25V",
            achievedValue = "${String.format("%.2f", vdd)} V",
            isMet = vdd in 0.90f..1.25f,
            engineerFeedback = if (vdd in 0.90f..1.25f) "Nominal FinFET operating range." else "Out of optimal power-performance envelope."
          )
        )
      }
    ),
    VlsiExperimentDefinition(
      id = "exp_clock_gating_power",
      title = "Integrated Clock Gating (ICG) Power Reduction",
      domain = DomainType.DIGITAL,
      objective = "Demonstrate dynamic power savings achieved by replacing standard free-running clock trees with Integrated Clock Gating (ICG) latch cells.",
      theoryMarkdown = "Dynamic clock power accounts for 30-40% of SoC energy: P_clk = C_tree * VDD^2 * f * alpha. An ICG cell halts clock switching during idle states (alpha = 0) without inducing clock glitches.",
      circuitTopologyDescription = "Negative-latch based AND clock gater driving a 32-bit register bank.",
      defaultParameters = listOf(
        ExperimentParameter("activity_factor", "Clock Activity Factor (alpha)", "%", 30f, 5f, 100f, 5f, "Percentage of clock cycles where register enable is active."),
        ExperimentParameter("reg_count", "Register Count", "bits", 64f, 16f, 256f, 16f, "Width of the gated register bank."),
        ExperimentParameter("freq_ghz", "Core Clock Rate", "GHz", 2.0f, 0.5f, 3.5f, 0.1f, "Master PLL clock frequency.")
      ),
      industryRelevance = "Used across all ARM Cortex and Apple Silicon cores to achieve all-day smartphone battery life.",
      calculateTelemetry = { params ->
        val alpha = (params["activity_factor"] ?: 30f) / 100f
        val regs = params["reg_count"] ?: 64f
        val f = params["freq_ghz"] ?: 2.0f
        val vdd = 1.0f
        val cClkPerFlop = 8f // fF
        val cTotal = regs * cClkPerFlop
        val pUnGatedMw = cTotal * 1e-3f * vdd * vdd * f
        val pGatedMw = cTotal * 1e-3f * vdd * vdd * f * alpha
        val savedMw = pUnGatedMw - pGatedMw
        mapOf(
          "Ungated Clock Power" to "${String.format("%.2f", pUnGatedMw)} mW",
          "Gated Clock Power" to "${String.format("%.2f", pGatedMw)} mW",
          "Energy Savings" to "${String.format("%.1f", (1f - alpha) * 100f)}%",
          "Power Saved" to "${String.format("%.2f", savedMw)} mW"
        )
      },
      evaluateRubric = { params ->
        val alpha = (params["activity_factor"] ?: 30f) / 100f
        val savings = (1f - alpha) * 100f
        listOf(
          ExperimentRubricItem(
            criterionName = "Power Reduction Target",
            targetSpecification = "Energy Savings >= 50%",
            achievedValue = "${String.format("%.1f", savings)}%",
            isMet = savings >= 50f,
            engineerFeedback = if (savings >= 50f) "Substantial power reduction achieved. Meets mobile battery envelope." else "Increase idle gating duty cycle to reach power target."
          )
        )
      }
    )
  )

  // ==========================================
  // INDUSTRIAL JOB SIMULATION WORKFLOWS
  // ==========================================
  val industrialJobWorkflows = listOf(
    IndustrialJobWorkflow(
      workflowId = "flow_nvidia_timing",
      title = "NVIDIA: 3nm Tensor Core Timing Closure & Buffer Insertion",
      targetCompany = "NVIDIA Corporation",
      fabNode = "TSMC N3E FinFET (3nm)",
      salaryLevel = "\$185,000 - \$245,000",
      scenarioBrief = "A 64-bit matrix multiply unit experiences negative setup slack of -145ps on the critical carry propagation path at the SS slow corner (0.75V, 125°C). Your goal is to optimize buffer sizing and clock skew to achieve timing signoff without inducing hold violations.",
      milestones = listOf(
        JobSimulationMilestone(
          stepNumber = 1,
          title = "Analyze PrimeTime STA Slack & Critical Path Depth",
          departmentRole = "ASIC Timing Signoff Engineer",
          taskInstructions = "Inspect the critical path logic depth. Adjust the logic stage count from 8 stages to 4 stages using carry-lookahead synthesis.",
          toolConsoleName = "Synopsys PrimeTime STA Shell (v2026.06)",
          liveTerminalPrompt = "pt_shell> report_timing -delay_type max -path_type full_clock_expanded",
          parameters = listOf(
            ExperimentParameter("logic_stages", "Carry Logic Depth", "stages", 8f, 3f, 8f, 1f, "Number of cascaded logic gate levels."),
            ExperimentParameter("stage_delay", "Average Stage Gate Delay", "ps", 42f, 25f, 60f, 1f, "Standard cell delay per stage.")
          ),
          verificationRule = { p ->
            val stages = p["logic_stages"] ?: 8f
            val isPassed = stages <= 4f
            Pair(isPassed, if (isPassed) "Logic depth reduced to $stages stages. Data arrival advanced by 168ps!" else "Too many logic stages. Critical path still exceeds 300ps clock period.")
          }
        ),
        JobSimulationMilestone(
          stepNumber = 2,
          title = "CTS Clock Tree Re-balancing & Skew Engineering",
          departmentRole = "Physical Design & CTS Lead",
          taskInstructions = "Manipulate useful clock skew to give the capture flop more time without violating hold time at the fast FF corner.",
          toolConsoleName = "Cadence Innovus Implementation System",
          liveTerminalPrompt = "innovus 26.1> set_clock_tree_options -target_skew 25ps -buffer_chain BUFF_X4",
          parameters = listOf(
            ExperimentParameter("clock_skew_target", "Useful Clock Skew", "ps", 10f, 0f, 60f, 5f, "Intentional clock delay to capture flip-flop."),
            ExperimentParameter("buffer_drive_strength", "CTS Buffer Size", "X", 2f, 1f, 8f, 1f, "Drive strength multiplier for clock tree buffers.")
          ),
          verificationRule = { p ->
            val skew = p["clock_skew_target"] ?: 10f
            val drive = p["buffer_drive_strength"] ?: 2f
            val isPassed = skew in 25f..50f && drive >= 4f
            Pair(isPassed, if (isPassed) "CTS balanced. Clock skew +${skew.toInt()}ps recovered setup slack while maintaining hold margin." else "Clock tree slew or skew out of safe signoff window.")
          }
        ),
        JobSimulationMilestone(
          stepNumber = 3,
          title = "Signoff DRC, IR-Drop & Tapeout Seal",
          departmentRole = "Tapeout Signoff Director",
          taskInstructions = "Execute final signoff. Ensure power mesh IR drop remains below 28mV and timing slack is positive across all PVT corners.",
          toolConsoleName = "Ansys RedHawk-SC Power Signoff",
          liveTerminalPrompt = "redhawk> analyze_static_ir_drop -vdd 0.75V -temp 125C",
          parameters = listOf(
            ExperimentParameter("power_grid_density", "Metal-6/7 Mesh Density", "%", 18f, 10f, 35f, 1f, "Power stripe metal coverage percentage."),
            ExperimentParameter("decap_cells", "Decoupling Capacitance", "nF", 12f, 5f, 30f, 1f, "Filler decap cells to suppress dynamic voltage droop.")
          ),
          verificationRule = { p ->
            val density = p["power_grid_density"] ?: 18f
            val decap = p["decap_cells"] ?: 12f
            val isPassed = density >= 24f && decap >= 18f
            Pair(isPassed, if (isPassed) "Signoff Clean! Max IR drop: 21.4mV (<28mV spec). Timing slack +18ps. Tapeout approved." else "IR drop violation detected in center logic tiles. Increase grid density and decap.")
          }
        )
      )
    ),
    IndustrialJobWorkflow(
      workflowId = "flow_tsmc_ate",
      title = "TSMC / Advantest: Wafer Sort & ATE Bridging Defect Isolation",
      targetCompany = "TSMC / Advantest Partner",
      fabNode = "TSMC N5 5nm FinFET",
      salaryLevel = "\$140,000 - \$180,000",
      scenarioBrief = "Wafer sort testing on lot #A984 flags 18% of dies failing Iddq screening with current spikes exceeding 250uA. You must execute vector-by-vector diagnostic scanning to localize the physical metal bridge and calibrate the ATE test program.",
      milestones = listOf(
        JobSimulationMilestone(
          stepNumber = 1,
          title = "ATE Pin Electronics & Parametric Setup",
          departmentRole = "ATE Test Program Engineer",
          taskInstructions = "Configure the V93000 Parametric Measurement Unit (PMU) compliance current and settle time for quiescent measurement.",
          toolConsoleName = "Advantest SmarTest 8 Production Tester",
          liveTerminalPrompt = "smartest> pmu.set_force_voltage(1.10V).set_compliance(15mA).measure_iddq()",
          parameters = listOf(
            ExperimentParameter("settle_time_us", "Measurement Settle Time", "µs", 15f, 5f, 50f, 5f, "Delay after clock halt before current strobing."),
            ExperimentParameter("strobe_threshold_ua", "Iddq Pass Threshold", "µA", 40f, 15f, 100f, 5f, "Maximum allowable quiescent current.")
          ),
          verificationRule = { p ->
            val settle = p["settle_time_us"] ?: 15f
            val thresh = p["strobe_threshold_ua"] ?: 40f
            val isPassed = settle >= 25f && thresh in 30f..45f
            Pair(isPassed, if (isPassed) "PMU calibrated correctly. Capacitive charging currents subsided before strobe." else "Measurement error: settle time too short or threshold non-compliant with AEC-Q100.")
          }
        ),
        JobSimulationMilestone(
          stepNumber = 2,
          title = "Isolate Bridging Vector & Defect Node",
          departmentRole = "Failure Analysis & DFT Engineer",
          taskInstructions = "Step through the 12,000 ATPG vectors to identify the exact state transition where the bridging path begins conducting.",
          toolConsoleName = "Mentor Tessent FastScan Diagnostic Tool",
          liveTerminalPrompt = "tessent> diagnose_failures -vector_log lot_A984.fail -fault_model bridging",
          parameters = listOf(
            ExperimentParameter("scan_clock_mhz", "Diagnostic Scan Rate", "MHz", 50f, 10f, 100f, 10f, "Scan shift frequency for diagnostic capture."),
            ExperimentParameter("delta_iddq_limit", "Delta-Iddq Filtering Window", "µA", 15f, 5f, 30f, 1f, "Signature threshold between adjacent vectors.")
          ),
          verificationRule = { p ->
            val delta = p["delta_iddq_limit"] ?: 15f
            val isPassed = delta in 8f..18f
            Pair(isPassed, if (isPassed) "Defect isolated: Metal-3 short between net clk_div2 and bus_data[14]! Fault localized to die coordinate [X:44, Y:19]." else "Delta-Iddq window failed to separate intrinsic leakage from defect current.")
          }
        ),
        JobSimulationMilestone(
          stepNumber = 3,
          title = "Wafer Binning & Production Yield Signoff",
          departmentRole = "Production Silicon Yield Director",
          taskInstructions = "Apply adaptive test program flow. Route defect dies into Bin 2 (rework/scrap) and certify Bin 1 good dies for packaging.",
          toolConsoleName = "TSMC Yield Manager & Die Binning Controller",
          liveTerminalPrompt = "yield_mgr> update_binning_matrix -wafer_map lot_A984 -commit_signoff",
          parameters = listOf(
            ExperimentParameter("yield_target", "Target Pass Yield", "%", 90f, 80f, 98f, 1f, "Minimum lot yield for wafer shipment."),
            ExperimentParameter("voltage_guardband", "ATE Voltage Guardband", "mV", 20f, 10f, 50f, 5f, "Tester guardband to ensure field reliability.")
          ),
          verificationRule = { p ->
            val y = p["yield_target"] ?: 90f
            val gb = p["voltage_guardband"] ?: 20f
            val isPassed = y <= 93f && gb in 15f..35f
            Pair(isPassed, if (isPassed) "Wafer lot signoff approved! Recovered yield: 92.4%. Zero defective dies escaped to packaging." else "Guardband too aggressive or yield target unachievable.")
          }
        )
      )
    )
  )
}
