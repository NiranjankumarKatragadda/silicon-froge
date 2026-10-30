package com.example.data

import com.example.model.*
import kotlin.math.*

object SiliconLabRepository {

  // ==========================================
  // DOMAIN 1: DIGITAL VLSI & TIMING ENGINE
  // ==========================================
  val digitalCircuits = listOf(
    DigitalCircuit(
      id = "dff_reg",
      name = "D-Flip-Flop with Clock Gating",
      description = "Edge-triggered master-slave D-FF with integrated clock gating (ICG) latch cell.",
      inputs = listOf("D", "CLK", "EN", "RST_N"),
      outputs = listOf("Q", "QN"),
      verilogSnippet = """
        module dff_icg (
          input  wire clk,
          input  wire rst_n,
          input  wire en,
          input  wire d,
          output reg  q
        );
          reg en_latched;
          // Integrated Clock Gating cell
          always @(clk or en) begin
            if (!clk) en_latched <= en;
          end
          wire gated_clk = clk & en_latched;

          always @(posedge gated_clk or negedge rst_n) begin
            if (!rst_n)
              q <= 1'b0;
            else
              q <= d;
          end
        endmodule
      """.trimIndent(),
      propagationDelayNs = 0.32f,
      minSetupTimeNs = 0.15f,
      minHoldTimeNs = 0.08f
    ),
    DigitalCircuit(
      id = "nand2_cmos",
      name = "2-Input NAND Gate",
      description = "Standard CMOS logic cell with 2 series NMOS and 2 parallel PMOS transistors.",
      inputs = listOf("A", "B"),
      outputs = listOf("Y"),
      verilogSnippet = """
        module nand2_cell (
          input  wire A,
          input  wire B,
          output wire Y
        );
          assign #0.18 Y = ~(A & B);
        endmodule
      """.trimIndent(),
      propagationDelayNs = 0.18f,
      minSetupTimeNs = 0.05f,
      minHoldTimeNs = 0.02f
    ),
    DigitalCircuit(
      id = "mux4_one",
      name = "4-to-1 Multiplexer (Transmission Gate)",
      description = "Low-power high-speed transmission gate multiplexer with decoded select lines.",
      inputs = listOf("D0", "D1", "D2", "D3", "S0", "S1"),
      outputs = listOf("OUT"),
      verilogSnippet = """
        module mux4_tg (
          input  wire [3:0] d,
          input  wire [1:0] sel,
          output wire       out
        );
          assign out = (sel == 2'b00) ? d[0] :
                       (sel == 2'b01) ? d[1] :
                       (sel == 2'b10) ? d[2] : d[3];
        endmodule
      """.trimIndent(),
      propagationDelayNs = 0.45f,
      minSetupTimeNs = 0.20f,
      minHoldTimeNs = 0.10f
    ),
    DigitalCircuit(
      id = "full_adder",
      name = "4-Bit Ripple Carry Adder",
      description = "Cascaded mirror full adders with critical path carry propagation analysis.",
      inputs = listOf("A[3:0]", "B[3:0]", "Cin"),
      outputs = listOf("Sum[3:0]", "Cout"),
      verilogSnippet = """
        module rca_4bit (
          input  wire [3:0] a, b,
          input  wire       cin,
          output wire [3:0] sum,
          output wire       cout
        );
          assign {cout, sum} = a + b + cin;
        endmodule
      """.trimIndent(),
      propagationDelayNs = 0.85f,
      minSetupTimeNs = 0.35f,
      minHoldTimeNs = 0.12f
    )
  )

  fun computeTimingSlack(
    clockFreqMhz: Float,
    tCqNs: Float = 0.25f,
    tCombNs: Float = 0.65f,
    tSetupNs: Float = 0.15f,
    tHoldNs: Float = 0.08f,
    clockSkewNs: Float = 0.04f,
    jitterNs: Float = 0.02f
  ): TimingAnalysisResult {
    val periodNs = if (clockFreqMhz > 0f) 1000f / clockFreqMhz else 2.0f
    val effectivePeriod = periodNs - jitterNs
    val setupSlack = (effectivePeriod + clockSkewNs) - (tCqNs + tCombNs + tSetupNs)
    val holdSlack = (tCqNs + tCombNs) - (tHoldNs + clockSkewNs)

    return TimingAnalysisResult(
      clockFreqMhz = clockFreqMhz,
      clockPeriodNs = periodNs,
      tCqNs = tCqNs,
      tCombNs = tCombNs,
      tSetupNs = tSetupNs,
      tHoldNs = tHoldNs,
      clockSkewNs = clockSkewNs,
      setupSlackNs = setupSlack,
      holdSlackNs = holdSlack,
      hasSetupViolation = setupSlack < 0f,
      hasHoldViolation = holdSlack < 0f
    )
  }

  // ==========================================
  // DOMAIN 2: ANALOG & CMOS INVERTER VTC
  // ==========================================
  fun simulateCmosVtc(spec: CmosInverterSpec): CmosVtcAnalysis {
    val betaN = spec.knPrime * spec.wnLnRatio
    val betaP = spec.kpPrime * spec.wpLpRatio
    val betaRatio = betaN / (if (betaP > 0f) betaP else 1f)
    val r = sqrt(betaRatio)

    // Midpoint switching threshold VM
    val vm = (spec.vdd - spec.vtp + r * spec.vtn) / (1f + r)

    // Approx VIL and VIH
    val vil = (2f * spec.vdd / (1f + r)) * 0.42f + spec.vtn * 0.6f
    val vih = spec.vdd - ((spec.vdd - vm) * 0.55f)
    val vol = 0.04f
    val voh = spec.vdd - 0.03f

    val nmL = max(0f, vil - vol)
    val nmH = max(0f, voh - vih)

    val points = mutableListOf<VtcCurvePoint>()
    val steps = 40
    for (i in 0..steps) {
      val vin = (spec.vdd * i) / steps.toFloat()
      val vout = when {
        vin < spec.vtn -> {
          // Region A: NMOS Cutoff, PMOS Linear -> Vout = VDD
          spec.vdd
        }
        vin > (spec.vdd - spec.vtp) -> {
          // Region E: PMOS Cutoff, NMOS Linear -> Vout ~ 0
          0.02f
        }
        else -> {
          // Transition using hyperbolic tan approximation calibrated to VM and beta ratio
          val steepness = 7.5f * sqrt(betaRatio)
          val normalized = (vin - vm) * steepness / spec.vdd
          val sig = 1f / (1f + exp(normalized))
          spec.vdd * sig
        }
      }

      val nRegion = when {
        vin < spec.vtn -> "Cutoff"
        vout > (vin - spec.vtn) -> "Saturation"
        else -> "Linear (Triode)"
      }
      val pRegion = when {
        vin > (spec.vdd - spec.vtp) -> "Cutoff"
        (spec.vdd - vout) > (spec.vdd - vin - spec.vtp) -> "Saturation"
        else -> "Linear (Triode)"
      }

      points.add(VtcCurvePoint(vin, vout, nRegion, pRegion))
    }

    return CmosVtcAnalysis(
      vthM = vm,
      vil = vil,
      vih = vih,
      vol = vol,
      voh = voh,
      nmL = nmL,
      nmH = nmH,
      betaRatio = betaRatio,
      points = points
    )
  }

  // ==========================================
  // DOMAIN 3: FABRICATION 8-STEP CLEANROOM FLOW
  // ==========================================
  val fabStepsList = listOf(
    FabStep(
      stepNumber = 1,
      name = "Wafer Cleaning & RCA Preparation",
      description = "Removal of organic contaminants (SC-1: NH4OH + H2O2) and metallic impurities (SC-2: HCl + H2O2) with ultra-pure DI water rinse.",
      chamberTempCelsius = 75f,
      chemicalOrGas = "SC-1 & SC-2 Solutions",
      thicknessAngstroms = 0f,
      hazardType = "Chemical Burn & Acid Vapor",
      safetyCheck = "Exhaust fume scrubber engaged, neoprene apron & face shield verified.",
      layerName = "Silicon Substrate (p-type <100>)"
    ),
    FabStep(
      stepNumber = 2,
      name = "Thermal Oxidation (Deal-Grove Growth)",
      description = "Dry/Wet oxidation in quartz tube furnace creating high-purity SiO2 barrier layer.",
      chamberTempCelsius = 1050f,
      chemicalOrGas = "Dry Oxygen (O2) + Trace HCl",
      thicknessAngstroms = 3500f,
      hazardType = "High Temperature & Thermal Shock",
      safetyCheck = "Quartz furnace interlock locked; pyrometer calibrated to 1050°C.",
      layerName = "Thermal SiO2 Dielectric"
    ),
    FabStep(
      stepNumber = 3,
      name = "Photolithography (Coat, Expose & Develop)",
      description = "Spin-coating positive DUV photoresist at 4000 RPM, 193nm ArF immersion stepper exposure with quartz reticle mask.",
      chamberTempCelsius = 110f,
      chemicalOrGas = "TMAH 2.38% Developer & DUV Resist",
      thicknessAngstroms = 4200f,
      hazardType = "Flammable Solvent (PGMEA) & UV Radiation",
      safetyCheck = "Yellow room UV-filtered amber lighting active; spinner vacuum chuck sealed.",
      layerName = "Patterned Photoresist Mask"
    ),
    FabStep(
      stepNumber = 4,
      name = "Plasma Reactive Ion Etching (RIE)",
      description = "Anisotropic dry etch using CF4/CHF3 plasma to pattern oxide windows down to silicon.",
      chamberTempCelsius = 60f,
      chemicalOrGas = "CF4 + CHF3 + Ar Gas Plasma",
      thicknessAngstroms = 3500f,
      hazardType = "RF Plasma & Fluorocarbon Decomposition",
      safetyCheck = "RF chamber shielding locked; turbo-molecular vacuum below 1e-5 Torr.",
      layerName = "Etched Gate/Contact Windows"
    ),
    FabStep(
      stepNumber = 5,
      name = "Ion Implantation & Rapid Thermal Anneal (RTA)",
      description = "Doping silicon wells with Boron (p-well, 35 keV) and Phosphorus (n-well, 80 keV), followed by 1050°C RTA for lattice repair.",
      chamberTempCelsius = 1050f,
      chemicalOrGas = "Phosphine (PH3) / Boron Trifluoride (BF3)",
      thicknessAngstroms = 1800f,
      hazardType = "High Voltage Accelerator & Pyrophoric/Toxic Gas",
      safetyCheck = "Silane/Phosphine gas detector active (<0.05 ppm); high voltage cage interlocked.",
      layerName = "Doped Well Regions (p/n)"
    ),
    FabStep(
      stepNumber = 6,
      name = "Polysilicon Gate LPCVD Deposition",
      description = "Low-pressure chemical vapor deposition of poly-crystalline silicon from Silane (SiH4) gas, followed by heavy n+ doping.",
      chamberTempCelsius = 625f,
      chemicalOrGas = "Silane (SiH4) Gas",
      thicknessAngstroms = 2500f,
      hazardType = "Pyrophoric Gas (Spontaneous Combustion)",
      safetyCheck = "N2 purge gas cycle complete; toxic gas abatement burn box verified.",
      layerName = "Polysilicon Gate Electrode"
    ),
    FabStep(
      stepNumber = 7,
      name = "Damascene Metallization (Metal-1 to Metal-3)",
      description = "Dual-damascene copper electroplating with Ta/TaN barrier seeds for low-resistance on-chip interconnects.",
      chamberTempCelsius = 25f,
      chemicalOrGas = "CuSO4 / H2SO4 Electrolyte + TaN Seed",
      thicknessAngstroms = 4500f,
      hazardType = "Heavy Metal Plating Bath & Electrical Current",
      safetyCheck = "Spill containment berm active; electroplating current limiter set to 15A.",
      layerName = "Copper Interconnect Lines (M1-M3)"
    ),
    FabStep(
      stepNumber = 8,
      name = "Chemical Mechanical Planarization (CMP) & Passivation",
      description = "Nano-abrasive silica slurry polishing to achieve atomic flatness, followed by Si3N4 protective scratch passivation.",
      chamberTempCelsius = 22f,
      chemicalOrGas = "Colloidal Silica Slurry + Si3N4 PECVD",
      thicknessAngstroms = 6000f,
      hazardType = "Slurry Waste & Pressurized Mechanical Heads",
      safetyCheck = "Pad conditioning diamond arm aligned; DI water post-CMP sonic cleaner engaged.",
      layerName = "Passivated Finished Wafer"
    )
  )

  // ==========================================
  // DOMAIN 4: PHYSICAL DESIGN (FLOORPLAN & DRC)
  // ==========================================
  fun getInitialMacros(): List<MacroBlock> {
    return listOf(
      MacroBlock("sram_l2", "SRAM L2 Cache 512KB", 0.32f, 0.28f, 0.05f, 0.05f, 45f),
      MacroBlock("pll_clk", "PLL & Clock Generator", 0.18f, 0.16f, 0.75f, 0.05f, 18f),
      MacroBlock("pcie_phy", "PCIe Gen5 PHY SerDes", 0.35f, 0.22f, 0.60f, 0.72f, 62f),
      MacroBlock("dsp_core", "DSP Vector Accelerator", 0.30f, 0.32f, 0.10f, 0.60f, 55f)
    )
  }

  fun evaluateFloorplan(
    macros: List<MacroBlock>,
    coreUtilPercent: Float,
    stripeCount: Int
  ): FloorplanState {
    val violations = mutableListOf<String>()

    // Check macro overlaps
    for (i in macros.indices) {
      for (j in i + 1 until macros.size) {
        val m1 = macros[i]
        val m2 = macros[j]
        val overlapX = m1.posXPercent < m2.posXPercent + m2.widthPercent &&
            m1.posXPercent + m1.widthPercent > m2.posXPercent
        val overlapY = m1.posYPercent < m2.posYPercent + m2.heightPercent &&
            m1.posYPercent + m1.heightPercent > m2.posYPercent
        if (overlapX && overlapY) {
          violations.add("DRC Overlap: ${m1.name} intersects ${m2.name}")
        }
      }
    }

    // Boundary check
    for (m in macros) {
      if (m.posXPercent + m.widthPercent > 0.98f || m.posYPercent + m.heightPercent > 0.98f) {
        violations.add("DRC Boundary: ${m.name} violates I/O pad ring keepout (min 20um)")
      }
    }

    if (coreUtilPercent > 82f) {
      violations.add("Congestion Hazard: Core utilization ${coreUtilPercent.toInt()}% exceeds 80% routing threshold (risk of DRC shorts)")
    }

    // Max IR drop formula: Total power * sheet resistance / stripeCount
    val totalPower = macros.sumOf { it.powerConsumptionMw.toDouble() }.toFloat() + (coreUtilPercent * 1.2f)
    val maxIrDrop = (totalPower * 1.5f) / max(2, stripeCount)

    return FloorplanState(
      dieAspectRatio = 1.0f,
      coreUtilizationPercent = coreUtilPercent,
      powerStripeCount = stripeCount,
      maxIrDropMv = maxIrDrop,
      drcViolationCount = violations.size,
      drcDetails = violations,
      macros = macros
    )
  }

  // ==========================================
  // DOMAIN 5: SILICON TEST & DFT (ATE & SCHMOO)
  // ==========================================
  fun generateSchmooPlot(hasDefect: Boolean): List<SchmooPoint> {
    val points = mutableListOf<SchmooPoint>()
    val voltages = listOf(0.70f, 0.75f, 0.80f, 0.85f, 0.90f, 0.95f, 1.00f, 1.05f, 1.10f, 1.15f, 1.20f)
    val frequencies = listOf(0.8f, 1.2f, 1.6f, 2.0f, 2.4f, 2.8f, 3.2f, 3.6f)

    for (v in voltages) {
      // Physical delay decreases with higher voltage ~ (V - Vth)^1.3
      val vth = 0.35f
      val overdrive = max(0.05f, v - vth)
      var maxF = 0.5f + (overdrive * overdrive * 3.8f)
      if (hasDefect) {
        maxF *= 0.65f // Defect degrades fmax
      }

      for (f in frequencies) {
        val pass = f <= maxF && (if (hasDefect && v > 1.12f) false else true) // high voltage breakdown if defect
        points.add(SchmooPoint(v, f, pass))
      }
    }
    return points
  }

  fun generateWaferDies(seedYield: Float = 0.91f): List<WaferDie> {
    val dies = mutableListOf<WaferDie>()
    var idCounter = 1
    val radius = 5

    for (x in -radius..radius) {
      for (y in -radius..radius) {
        val distSq = x * x + y * y
        if (distSq <= radius * radius) {
          // Inside wafer circle
          val edgeDefectProb = if (distSq >= 20) 0.35f else 0.05f
          val randomVal = ((abs(sin((x * 17 + y * 31).toDouble())) * 1000).toInt() % 100) / 100f
          val bin = when {
            randomVal > (seedYield - edgeDefectProb) -> {
              if (randomVal > 0.95f) 4 else if (randomVal > 0.85f) 3 else 2
            }
            else -> 1 // Bin 1: Prime Good Die
          }

          val fmax = if (bin == 1) 3.2f + (x * 0.03f) else 1.8f
          val iddq = if (bin == 2) 185f else if (bin == 1) 22f else 450f

          dies.add(WaferDie(id = idCounter++, x = x + radius, y = y + radius, binNumber = bin, fmaxGhz = fmax, iddqUa = iddq))
        }
      }
    }
    return dies
  }

  // ==========================================
  // INDUSTRY JOBS & TEST SIMULATION PROCEDURES
  // ==========================================
  val industryJobsList = listOf(
    IndustryJob(
      id = "job_ate_validation",
      title = "Silicon Test & ATE Yield Engineer",
      company = "Advantest / TSMC Partner",
      location = "Austin, TX (Hybrid) / Hsinchu",
      experienceLevel = "Entry to Senior (L3-L5)",
      salaryRange = "\$135,000 - \$178,000 + RSU",
      domain = DomainType.TEST_VALIDATION,
      keyRequirements = listOf(
        "Advantest V93000 / Teradyne UltraFLEX test program debug",
        "Iddq quiescent current fault screening and ATPG pattern bringup",
        "Wafer sort binning and Schmoo voltage-frequency characterization",
        "Understanding of stuck-at fault models and silicon defect physics"
      ),
      industrialSimulationId = "proc_automotive_iddq"
    ),
    IndustryJob(
      id = "job_pnr_timing",
      title = "Physical Design & STA Timing Closure Lead",
      company = "Apple Silicon / NVIDIA",
      location = "Cupertino, CA / Bengaluru",
      experienceLevel = "Senior ASIC (L5)",
      salaryRange = "${'$'}175,000 - ${'$'}240,000 + RSU",
      domain = DomainType.PHYSICAL_DESIGN,
      keyRequirements = listOf(
        "3nm/5nm place-and-route (PnR) flow & floorplan optimization",
        "PrimeTime Static Timing Analysis (setup/hold slack, clock tree synthesis)",
        "Power mesh IR-drop analysis and electro-migration (EM) signoff",
        "Design Rule Check (DRC) and Layout Versus Schematic (LVS) resolution"
      ),
      industrialSimulationId = "proc_ddr5_setup_violation"
    ),
    IndustryJob(
      id = "job_rtl_verification",
      title = "ASIC Digital Verification Engineer (DV)",
      company = "Qualcomm Technologies",
      location = "San Diego, CA / Hyderabad",
      experienceLevel = "Graduate / Junior Engineer (L2-L3)",
      salaryRange = "${'$'}120,000 - ${'$'}155,000 + Bonus",
      domain = DomainType.DIGITAL,
      keyRequirements = listOf(
        "SystemVerilog & UVM testbench architecture",
        "Clock Domain Crossing (CDC) and asynchronous FIFO verification",
        "Constrained-random stimulus generation and functional coverage closure",
        "Assertion-Based Verification (SVA) for protocol compliance"
      ),
      industrialSimulationId = "proc_scan_dft_debug"
    ),
    IndustryJob(
      id = "job_analog_ic",
      title = "Analog & Mixed-Signal IC Design Engineer",
      company = "Texas Instruments / Analog Devices",
      location = "Dallas, TX / Munich",
      experienceLevel = "Mid-Level IC Designer",
      salaryRange = "${'$'}140,000 - ${'$'}185,000 + Stock",
      domain = DomainType.ANALOG,
      keyRequirements = listOf(
        "CMOS operational amplifier & bandgap voltage reference design",
        "Noise margin analysis, small-signal AC gain & phase margin compensation",
        "Deep submicron short-channel effects (DIBL, velocity saturation, channel length modulation)",
        "Cadence Spectre / SPICE simulation and layout matching techniques"
      ),
      industrialSimulationId = "proc_apu_schmoo_char"
    )
  )

  val industrialProcedures = listOf(
    IndustryTestProcedure(
      id = "proc_automotive_iddq",
      title = "Automotive Radar SoC: High-Temperature Iddq Leakage Screening (AEC-Q100)",
      companyTarget = "Advantest / NXP / TSMC",
      deviceUnderStudy = "77GHz FMCW Radar SoC (7nm FinFET)",
      standard = "AEC-Q100 Grade 1 & IEEE 1149.4 Mixed-Signal Test",
      briefDescription = "Detect bridging defects causing quiescent current spikes during automotive burn-in at 125°C.",
      fullScenario = "During qualification testing of a batch of 1500 radar dies, the quiescent current (Iddq) under vector #4872 spiked to 264uA (acceptable threshold: < 35uA). A metal bridging defect between Metal-2 clock line and VDD core is suspected.",
      defaultVddCorner = 1.10f,
      defaultTempCelsius = 125f,
      defaultFreqGhz = 1.8f,
      defaultIddqLimitUa = 35f,
      failureSymptom = "Iddq current measured at 264uA in vector state 0b10110010.",
      diagnosisClues = listOf(
        "Notice how reducing Vdd from 1.10V to 0.90V drops leakage non-linearly (indicates resistive bridge).",
        "Check pin electronics measurement unit compliance setting.",
        "Toggle ATPG test vector to un-assert the shorted node."
      ),
      targetFixDescription = "Re-bias test vector stimulus and apply delta-Iddq signature filtering algorithm to isolate good dies from latent gate oxide breakdown."
    ),
    IndustryTestProcedure(
      id = "proc_ddr5_setup_violation",
      title = "Server CPU: DDR5 PHY Timing Closure & Setup Slack Debug",
      companyTarget = "Apple Silicon / AMD",
      deviceUnderStudy = "DDR5-6400 PHY Clock Domain (3nm)",
      standard = "JEDEC DDR5 SDRAM Specification & PrimeTime Signoff",
      briefDescription = "Resolve a -180ps setup time violation in the memory controller's command/address skew domain.",
      fullScenario = "Static Timing Analysis at the slow corner (0.75V, 125°C, SS) flagged 14 paths in the command decoder failing setup slack by -180ps. Running at 3.2GHz requires a maximum comb delay under 0.28ns.",
      defaultVddCorner = 0.75f,
      defaultTempCelsius = 125f,
      defaultFreqGhz = 3.2f,
      defaultIddqLimitUa = 50f,
      failureSymptom = "Negative setup slack of -180ps in command address bus flip-flops.",
      diagnosisClues = listOf(
        "Look at the clock skew: currently +60ps early arrival at launch flip-flop.",
        "Consider clock tree buffer re-sizing or data path logic depth reduction.",
        "Verify hold time is not broken when fixing setup with delay buffers."
      ),
      targetFixDescription = "Re-balance clock tree buffers (CTS) to shift capture clock by +90ps and replace 4-stage carry logic with parallel prefix lookahead cells."
    ),
    IndustryTestProcedure(
      id = "proc_scan_dft_debug",
      title = "AI Neural NPU: Scan Chain Hold-Time & ATPG Coverage Closure",
      companyTarget = "Qualcomm / NVIDIA",
      deviceUnderStudy = "NPU Tensor Core with 120,000 Scan Flip-Flops",
      standard = "IEEE 1149.1 JTAG Boundary-Scan & ATPG SAF",
      briefDescription = "Debug scan chain shift failure where capture patterns corrupt data due to zero-delay hold violations.",
      fullScenario = "During scan chain shift at 100MHz ATE frequency, chain #14 fails shifting pattern 0xAAAA5555. Fast-corner hold time violation is causing race conditions along the scan path.",
      defaultVddCorner = 1.25f,
      defaultTempCelsius = -40f,
      defaultFreqGhz = 0.1f,
      defaultIddqLimitUa = 30f,
      failureSymptom = "Scan chain #14 output stuck at 0xFFFF on bit 8421.",
      diagnosisClues = listOf(
        "Fast corner (high Vdd 1.25V, cold temp -40°C) is the worst-case for hold time.",
        "Notice hold slack becomes negative when clock skew between adjacent scan flops exceeds clock-to-Q delay.",
        "Inserting delay lock buffers on the scan_data_in pin cures the race condition."
      ),
      targetFixDescription = "Insert standard lockup latches on cross-domain scan boundaries and delay cells to ensure min hold margin > 120ps across all PVT corners."
    ),
    IndustryTestProcedure(
      id = "proc_apu_schmoo_char",
      title = "Smartphone APU: 4nm FinFET Schmoo Characterization & Binning",
      companyTarget = "MediaTek / Apple Silicon",
      deviceUnderStudy = "Octa-Core Application Processor (4nm)",
      standard = "Production ATE Wafer Sort & Binning Flow",
      briefDescription = "Map out the operational boundary across voltages (0.7V - 1.2V) and frequencies (1.0GHz - 3.5GHz) to classify dies into premium vs standard bins.",
      fullScenario = "Production wafer #W2948 has finished fabrication. An ATE Schmoo matrix must run on each die to determine if it meets Tier-1 Extreme Performance (3.2GHz @ 0.95V) or should be binned down.",
      defaultVddCorner = 0.95f,
      defaultTempCelsius = 85f,
      defaultFreqGhz = 3.2f,
      defaultIddqLimitUa = 25f,
      failureSymptom = "Edge dies fail at 3.2GHz when Vdd drops below 1.05V due to threshold voltage roll-off.",
      diagnosisClues = listOf(
        "Schmoo plot shows wall of fails below 0.85V for frequencies > 2.4GHz.",
        "Observe the correlation between center dies and higher fmax.",
        "Adjust adaptive body bias (ABB) to recover 150MHz frequency headroom."
      ),
      targetFixDescription = "Execute multi-site parallel probe test, enable adaptive body bias (ABB) trim bits, and bin 78% of dies into Bin 1 (Premium Tier)."
    )
  )

  // ==========================================
  // INTERVIEW PREP: QUESTION BANK & TOP ENGINEERS
  // ==========================================
  val topEngineers = listOf(
    EngineerPersona(
      id = "eng_elena_chen",
      name = "Dr. Elena Chen",
      title = "Principal Physical Design Architect",
      companyBadge = "Apple Silicon / ex-TSMC",
      yearsExperience = 16,
      bio = "Led tape-outs on 7nm, 5nm, and 3nm nodes. Specializes in advanced clock tree synthesis, ultra-low voltage timing closure, and full-chip IR-drop signoff.",
      specialty = DomainType.PHYSICAL_DESIGN,
      interviewStyle = "Rigorous, analytical, asks deep 'why' questions about physical layout trade-offs and setup/hold timing margins."
    ),
    EngineerPersona(
      id = "eng_marcus_vance",
      name = "Marcus Vance",
      title = "Director of Post-Silicon Validation",
      companyBadge = "NVIDIA / Qualcomm",
      yearsExperience = 19,
      bio = "Brought up over 25 flagship GPU & modem SoCs from initial wafer probing through high-volume production test benches. Master of ATE, Iddq, and Schmoo debugging.",
      specialty = DomainType.TEST_VALIDATION,
      interviewStyle = "Pragmatic, scenario-driven, presents actual high-pressure silicon bring-up bugs and watches how candidates isolate faults."
    ),
    EngineerPersona(
      id = "eng_priya_sharma",
      name = "Priya Sharma",
      title = "Staff RTL Verification Lead",
      companyBadge = "AMD / Intel",
      yearsExperience = 12,
      bio = "Architected UVM verification environments for multi-core processors. Pioneer in formal verification, CDC analysis, and constrained-random protocol coverage.",
      specialty = DomainType.DIGITAL,
      interviewStyle = "Interactive and methodical, probes for deep understanding of race conditions, metastability, and asynchronous clock domains."
    ),
    EngineerPersona(
      id = "eng_kenji_sato",
      name = "Kenji Sato",
      title = "Chief Fellow, Analog IC Design",
      companyBadge = "Texas Instruments / ADI",
      yearsExperience = 22,
      bio = "Designed ultra-low-noise operational amplifiers, high-speed SerDes analog front-ends, and precision bandgap voltage references for aerospace and automotive systems.",
      specialty = DomainType.ANALOG,
      interviewStyle = "First-principles intuitive thinker, tests deep understanding of poles, zeros, small-signal models, and CMOS layout symmetry."
    )
  )

  val interviewQuestions = listOf(
    InterviewQuestion(
      id = "q1_hold_time",
      domain = DomainType.DIGITAL,
      difficulty = "Senior ASIC",
      question = "Why is hold time independent of clock period (frequency), and how can a hold time violation be fixed after silicon fabrication?",
      options = listOf(
        "Hold time depends on the clock period because data must arrive before the next edge; it can be fixed by lowering clock frequency.",
        "Hold time is evaluated at the same clock edge between launch and capture; it cannot be fixed by lowering clock frequency on fabricated silicon without voltage/temperature tuning.",
        "Hold time only matters for negative edge triggered flip-flops; it is fixed by adding inverters in the clock tree during software compilation.",
        "Hold time is cured simply by enabling integrated clock gating cells."
      ),
      correctIndex = 1,
      explanation = "Hold slack equation is: Thold_slack = (Tcq + Tcomb) - (Thold + Tskew). Notice that Tperiod does NOT appear anywhere in the equation! Both launch and capture actions refer to the exact same clock edge. Hence, slowing down the clock does NOT fix a hold violation. In fabricated silicon, one can only attempt to recover hold margin by adjusting supply voltage or operating temperature.",
      hiringManagerSecret = "Top interviewers at NVIDIA and Apple use this question to filter out candidates who memorize formulas without understanding the physical timing diagram.",
      formula = "Slack_hold = (t_cq + t_comb) - (t_hold + t_skew) > 0"
    ),
    InterviewQuestion(
      id = "q2_cmos_vtc",
      domain = DomainType.ANALOG,
      difficulty = "Foundational",
      question = "In a CMOS inverter, why is the PMOS transistor typically sized with a width (Wp) approximately 2 to 3 times wider than the NMOS transistor (Wn)?",
      options = listOf(
        "Because PMOS handles higher voltage than NMOS.",
        "Because electron mobility (μn) in silicon is roughly 2.5 times higher than hole mobility (μp), requiring a wider PMOS for equal drive currents and symmetric switching threshold (VM = VDD/2).",
        "To increase the gate oxide capacitance of the PMOS and decrease power consumption.",
        "Because PMOS transistors are placed in an n-well which has higher parasitic capacitance."
      ),
      correctIndex = 1,
      explanation = "Drive current is proportional to μ * Cox * (W/L). Because electrons have higher mobility (μn ~ 1350 cm²/V·s) than holes (μp ~ 480 cm²/V·s) in bulk silicon, βn = μn*Cox*(Wn/Ln) and βp = μp*Cox*(Wp/Lp). For symmetric rise and fall propagation times (t_plh = t_phl) and VM = VDD/2, we set βn = βp, which yields Wp/Lp ≈ 2.5 * (Wn/Ln).",
      hiringManagerSecret = "Engineers at TI look for candidates who connect transistor physics (carrier effective mass and scattering) directly to logic gate delay symmetry."
    ),
    InterviewQuestion(
      id = "q3_cdc_metastability",
      domain = DomainType.DIGITAL,
      difficulty = "Senior ASIC",
      question = "When transferring a multi-bit data bus across asynchronous clock domains, why is passing each bit through an independent 2-flop synchronizer strictly prohibited?",
      options = listOf(
        "Because 2-flop synchronizers consume too much dynamic power.",
        "Because random differences in routing delays and flip-flop settling times cause individual bits to arrive in different clock cycles, creating invalid transient data words (bus incoherence).",
        "Because the second flop will always burn out due to excessive clock jitter.",
        "Because SystemVerilog syntax does not permit arrays in synchronizer chains."
      ),
      correctIndex = 1,
      explanation = "While a 2-flop synchronizer reduces metastability MTBF for a single 1-bit control line, bits in a multi-bit bus have independent routing delays and resolving times. If bit 0 resolves in cycle N and bit 1 resolves in cycle N+1, the receiving logic reads a corrupted intermediate vector. Multi-bit CDC requires Gray coding (where only 1 bit transitions per count) or an Asynchronous FIFO with synchronized read/write pointers.",
      hiringManagerSecret = "This is a universal question at Qualcomm and AMD for any RTL verification or design role."
    ),
    InterviewQuestion(
      id = "q4_dft_atpg",
      domain = DomainType.TEST_VALIDATION,
      difficulty = "Principal Architect",
      question = "What is the primary physical difference between an Iddq test and a traditional functional logic test on an ATE tester?",
      options = listOf(
        "Iddq test operates at 10 GHz while functional test runs at 100 MHz.",
        "Functional test checks the logic voltage output table; Iddq test halts the circuit in a static state and measures nano/micro-amp quiescent supply current to expose bridging shorts and gate oxide pinholes.",
        "Iddq test only tests analog resistors and capacitors, not digital gates.",
        "Iddq test requires destroying the silicon chip after measurement."
      ),
      correctIndex = 1,
      explanation = "In static CMOS circuits, once inputs are stable, both pull-up and pull-down networks never conduct simultaneously (except for tiny junction leakage). If there is a bridging defect (short circuit between metal wires) or gate oxide breakdown, a conducting path from VDD to GND is established, causing quiescent current to jump by 100x to 1000x. Iddq testing catches defects that might otherwise pass functional speed tests but cause reliability failures in the field.",
      hiringManagerSecret = "Silicon validation leaders at Apple and Advantest prioritize candidates who understand defect mechanisms beyond simple 1s and 0s."
    )
  )

  fun getMockInterviewTurns(persona: EngineerPersona): List<DirectInterviewTurn> {
    return when (persona.id) {
      "eng_elena_chen" -> listOf(
        DirectInterviewTurn(
          turnNumber = 1,
          questionText = "Welcome to the physical design signoff round. In our 3nm tapeout, we noticed a critical clock path where setup slack is +20ps, but hold slack is -45ps at the SS corner. If the marketing team asks you to bump the clock frequency from 2.5GHz to 3.0GHz, what happens to this violation?",
          contextBrief = "3nm FinFET Core - Clock Frequency Bump & Hold Timing Behavior",
          candidateOptions = listOf(
            "Increasing frequency will worsen the hold violation because clock period is shorter.",
            "Increasing frequency will have zero impact on the hold violation because hold slack is independent of clock period.",
            "Increasing frequency fixes the hold violation because setup and hold are inversely proportional.",
            "We should insert a multi-cycle path constraint to suppress the error in PrimeTime."
          ),
          bestOptionIndex = 1,
          engineerFeedbackMap = mapOf(
            0 to "Common mistake. Hold slack equation is Thold_slack = (Tcq + Tcomb) - (Thold + Tskew). Clock period T never appears in it! Speeding up the clock won't touch the hold margin.",
            1 to "Spot on! That's the fundamental principle. Hold time checks race conditions on the same active clock edge. Changing the frequency doesn't help hold slack.",
            2 to "Dangerous assumption! That would result in fabricated silicon that fails immediately at all frequencies upon powering on.",
            3 to "Never suppress real timing errors with false constraints. That's a surefire way to kill a tapeout."
          ),
          hiringTip = "Top foundry architects test whether you truly grasp that hold time is a local min-delay race condition, not a frequency constraint."
        ),
        DirectInterviewTurn(
          turnNumber = 2,
          questionText = "To resolve that -45ps hold violation on fabricated silicon, you are in the lab with the ATE engineer. What operational levers do you have left?",
          contextBrief = "Post-Silicon Recovery Levers",
          candidateOptions = listOf(
            "We can increase Vdd supply voltage to speed up the data path (t_comb) and raise temperature to alter carrier mobility.",
            "We can re-run synthesis and download a new bitstream to the ASIC.",
            "We can use a focused ion beam (FIB) to manually rewire millions of transistors in the customer's phone.",
            "We have to abandon the entire silicon lot without testing anything else."
          ),
          bestOptionIndex = 0,
          engineerFeedbackMap = mapOf(
            0 to "Excellent. By raising Vdd slightly or manipulating operating temperature, data path delay shifts relative to clock arrival, potentially recovering positive hold slack in qualification.",
            1 to "An ASIC is hardwired silicon in metal; you cannot download a bitstream like an FPGA!",
            2 to "FIB is only feasible for single debug dies in failure analysis labs, never for volume production.",
            3 to "Always investigate operating envelope recovery before writing off multi-million dollar lots."
          ),
          hiringTip = "Show awareness of how PVT (Process, Voltage, Temperature) corners affect post-silicon silicon binning."
        )
      )
      else -> listOf(
        DirectInterviewTurn(
          turnNumber = 1,
          questionText = "In our post-silicon lab, we powered on first silicon of our AI accelerator. The board draws 280uA of quiescent current instead of the simulated 15uA. What test procedure do you execute first?",
          contextBrief = "First Silicon Bring-Up & Excessive Leakage Debug",
          candidateOptions = listOf(
            "Immediately turn on full-speed 4GHz functional tests to see if anything catches fire.",
            "Execute an ATE Iddq diagnostic pattern sweep across scan vectors to isolate which logic block exhibits the current anomaly.",
            "Replace all decoupling capacitors on the motherboard with bigger ones.",
            "Assume the simulator was wrong and ship the chips to customers."
          ),
          bestOptionIndex = 1,
          engineerFeedbackMap = mapOf(
            0 to "Running at full speed with suspected shorts will cause thermal runaway and destroy your only prototype package!",
            1 to "Precisely. Vector-by-vector Iddq scanning pinpoints the exact scan chain state where the bridging path conducts.",
            2 to "Board decoupling doesn't explain on-die quiescent current leakage.",
            3 to "A 20x leakage spike will drain mobile batteries in minutes and cause field returns."
          ),
          hiringTip = "Always prioritize methodical fault isolation over reckless testing on first silicon."
        ),
        DirectInterviewTurn(
          turnNumber = 2,
          questionText = "The Iddq scan pointed to a bridging fault in the SRAM control logic. What design-for-test (DFT) technique would prevent this in the next metal revision?",
          contextBrief = "DFT Architecture & Bridging Fault Prevention",
          candidateOptions = listOf(
            "Increase Metal-2 spacing design rules and add dedicated BIST pattern coverage for adjacent line shorts.",
            "Disable all scan chains so the tester cannot detect faults.",
            "Remove all clock gating cells.",
            "Switch to dynamic domino logic for the whole chip."
          ),
          bestOptionIndex = 0,
          engineerFeedbackMap = mapOf(
            0 to "Great answer. Layout spacing rules prevent physical bridging particles, while BIST detects any residual defects instantly.",
            1 to "Hiding faults from the tester guarantees shipping bad dies to customers.",
            2 to "Removing clock gating would cause catastrophic dynamic power dissipation.",
            3 to "Domino logic is notoriously sensitive to noise and leaks even more."
          ),
          hiringTip = "Connect physical defect causes (particles, CMP dishing) with DFT diagnostic structures."
        )
      )
    }
  }

  // ==========================================
  // LAB LOG STORAGE
  // ==========================================
  private val _labLogs = mutableListOf(
    LabLogEntry(
      id = "log_1",
      timestamp = System.currentTimeMillis() - 3600000,
      domain = DomainType.ANALOG,
      experimentTitle = "CMOS Inverter VTC Symmetry Test",
      recordedValues = "Wp/Lp = 4.0, Wn/Ln = 2.0, VM = 0.598V, NML = 0.44V, NMH = 0.48V",
      remarks = "Demonstrated near-ideal symmetric switching threshold at VDD/2 (1.2V supply)."
    ),
    LabLogEntry(
      id = "log_2",
      timestamp = System.currentTimeMillis() - 7200000,
      domain = DomainType.DIGITAL,
      experimentTitle = "D-Flip-Flop Setup/Hold Timing Analysis",
      recordedValues = "Freq = 1.25GHz, Tperiod = 0.8ns, SetupSlack = +0.12ns, HoldSlack = +0.07ns",
      remarks = "Clock gating latch cell successfully prevented clock power burn during idle cycles."
    )
  )

  fun getLabLogs(): List<LabLogEntry> = _labLogs.toList()

  fun addLabLog(entry: LabLogEntry) {
    _labLogs.add(0, entry)
  }
}
