package com.example.data

import com.example.model.MarketTelemetrySummary
import com.example.model.SemiconductorJobPosting
import com.example.model.SemiconductorJobTrend
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SemiconductorJobTrendsRepository {

  private val _marketTelemetry = MutableStateFlow(MarketTelemetrySummary())
  val marketTelemetry: Flow<MarketTelemetrySummary> = _marketTelemetry.asStateFlow()

  private val _jobTrends = MutableStateFlow(getInitialTrends())
  val jobTrends: Flow<List<SemiconductorJobTrend>> = _jobTrends.asStateFlow()

  private val _jobPostings = MutableStateFlow(getInitialPostings())
  val jobPostings: Flow<List<SemiconductorJobPosting>> = _jobPostings.asStateFlow()

  private val _isRefreshing = MutableStateFlow(false)
  val isRefreshing: Flow<Boolean> = _isRefreshing.asStateFlow()

  suspend fun refreshRealTimeTrends() {
    _isRefreshing.value = true
    delay(600) // Simulated network telemetry poll to semiconductor industry job index

    val jitter = ((-5..8).random()).toFloat()
    _marketTelemetry.update { current ->
      current.copy(
        currentIndexValue = current.currentIndexValue + (jitter * 0.2f),
        totalActiveRequisitions = current.totalActiveRequisitions + (jitter.toInt() * 45),
        lastSyncTime = System.currentTimeMillis()
      )
    }

    _jobPostings.update { list ->
      list.map { posting ->
        val applicantsDelta = (1..4).random()
        posting.copy(applicantCount = posting.applicantCount + applicantsDelta)
      }
    }

    _isRefreshing.value = false
  }

  fun calculateAtsScore(
    targetJobDomain: String,
    applicantDomain: String,
    hasCompletedLabs: Boolean,
    completedLabCount: Int,
    hasCustomChip: Boolean,
    degreeLevel: String,
    gpaOrGrade: String
  ): Pair<Int, String> {
    var score = 55

    // Domain alignment
    if (targetJobDomain.equals(applicantDomain, ignoreCase = true) ||
      applicantDomain.contains(targetJobDomain, ignoreCase = true) ||
      targetJobDomain.contains(applicantDomain, ignoreCase = true)
    ) {
      score += 20
    } else {
      score += 8
    }

    // Hands-on Room labs completed
    if (hasCompletedLabs) {
      score += (completedLabCount * 3).coerceAtMost(15)
    }

    // Custom Nanometer Chip or RTL portfolio
    if (hasCustomChip) {
      score += 10
    }

    // Education / GPA
    if (degreeLevel.contains("M.S.") || degreeLevel.contains("Ph.D.")) {
      score += 5
    }
    val gpaVal = gpaOrGrade.toDoubleOrNull() ?: 3.5
    if (gpaVal >= 3.7) {
      score += 5
    }

    val finalScore = score.coerceIn(50, 98)

    val feedback = when {
      finalScore >= 88 -> "Exceptional Silicon ATS Match: Verified tapeout metrics and extensive virtual cleanroom & STA lab credentials match foundry senior hiring rubric."
      finalScore >= 75 -> "Strong Candidate Match: Relevant EDA tooling knowledge and practical circuit experiments identified. Recommended for First-Round Technical Phone Screen."
      else -> "Moderate Match: Solid foundational knowledge. Completing additional advanced STA timing closure and ATE scan-chain labs will elevate interview priority."
    }

    return Pair(finalScore, feedback)
  }

  companion object {
    fun getInitialTrends(): List<SemiconductorJobTrend> {
      return listOf(
        SemiconductorJobTrend(
          id = "trend_ai_hardware",
          domain = "AI ASIC & NPU Hardware Architecture",
          hiringGrowthYoY = 48.6f,
          activeOpeningsGlobal = 3450,
          averageBaseSalaryUsd = 185000,
          topFoundryNodes = listOf("TSMC 3nm N3E", "Intel 18A", "Samsung 2nm GAAFET"),
          topRequiredTools = listOf("Synopsys PrimeTime", "SystemVerilog", "Cadence Palladium", "Python/PyTorch"),
          topHiringCompanies = listOf("NVIDIA", "Apple Silicon", "Google TPU", "Meta MTIA", "Tesla Dojo"),
          keyTrendInsight = "Explosive demand for tensor processing units (TPUs), transformer accelerators, and high-bandwidth memory (HBM3e) interfaces.",
          demandRating = "CRITICAL SURGE"
        ),
        SemiconductorJobTrend(
          id = "trend_physical_design",
          domain = "Physical Design & Signoff (Floorplan/STA)",
          hiringGrowthYoY = 36.2f,
          activeOpeningsGlobal = 4120,
          averageBaseSalaryUsd = 172000,
          topFoundryNodes = listOf("TSMC N3/N4", "Samsung 3nm", "GlobalFoundries 12LP"),
          topRequiredTools = listOf("Cadence Innovus", "Synopsys ICC2", "Ansys RedHawk-SC", "Calibre DRC/LVS"),
          topHiringCompanies = listOf("Apple", "Qualcomm", "AMD", "Broadcom", "MediaTek"),
          keyTrendInsight = "Severe industry shortage of engineers capable of sub-0.75V IR-drop signoff and routing closure on 100M+ gate hierarchical partitions.",
          demandRating = "VERY HIGH DEMAND"
        ),
        SemiconductorJobTrend(
          id = "trend_advanced_packaging",
          domain = "Advanced Packaging (CoWoS / Chiplets / 2.5D)",
          hiringGrowthYoY = 44.1f,
          activeOpeningsGlobal = 2190,
          averageBaseSalaryUsd = 168000,
          topFoundryNodes = listOf("TSMC CoWoS-S", "Intel EMIB", "ASE 2.5D/3D"),
          topRequiredTools = listOf("Ansys HFSS", "Cadence Allegro SiP", "Synopsys 3DIC Compiler"),
          topHiringCompanies = listOf("TSMC", "Intel Foundry", "NVIDIA", "ASE Group", "Amkor"),
          keyTrendInsight = "Moore's Law limits are driving massive migration to multi-die heterogeneous chiplets interconnected via silicon interposers and microbumps.",
          demandRating = "CRITICAL SURGE"
        ),
        SemiconductorJobTrend(
          id = "trend_silicon_test_ate",
          domain = "Silicon Test & DFT Diagnostics (ATE)",
          hiringGrowthYoY = 29.8f,
          activeOpeningsGlobal = 2860,
          averageBaseSalaryUsd = 155000,
          topFoundryNodes = listOf("TSMC Automotive 5nm", "Intel 7", "TI 45nm Analog"),
          topRequiredTools = listOf("Siemens Tessent", "Advantest 93000", "Teradyne UltraFLEX", "Synopsys TestMAX"),
          topHiringCompanies = listOf("Qualcomm", "NXP", "Texas Instruments", "Advantest", "Teradyne"),
          keyTrendInsight = "Automotive AEC-Q100 zero-defect mandates and high wafer fabrication costs have made pre-packaging wafer sort and Iddq leakage screening paramount.",
          demandRating = "STEADY GROWTH"
        ),
        SemiconductorJobTrend(
          id = "trend_analog_rf",
          domain = "Analog & Mixed-Signal RFIC Design",
          hiringGrowthYoY = 24.5f,
          activeOpeningsGlobal = 1980,
          averageBaseSalaryUsd = 162000,
          topFoundryNodes = listOf("TSMC 16FFC", "GlobalFoundries 22FDX", "TowerJazz 65nm"),
          topRequiredTools = listOf("Cadence Virtuoso", "Spectre RF", "Keysight ADS"),
          topHiringCompanies = listOf("Skyworks", "Qorvo", "Broadcom", "Qualcomm", "Analog Devices"),
          keyTrendInsight = "5G Advanced, 6G research, and low-power automotive radar sensor front-ends require deep analog transistor-level layout and noise margin tuning.",
          demandRating = "STEADY GROWTH"
        )
      )
    }

    fun getInitialPostings(): List<SemiconductorJobPosting> {
      return listOf(
        SemiconductorJobPosting(
          id = "job_nv_tpu_arch",
          jobTitle = "AI Hardware Architecture & RTL Engineer",
          company = "NVIDIA",
          location = "Santa Clara, CA (Hybrid)",
          domain = "AI ASIC & NPU Hardware Architecture",
          experienceLevel = "New College Grad / Junior (0-2 yrs)",
          salaryRange = "$150,000 - $190,000 + Stock",
          targetFabNode = "TSMC 3nm N3E",
          edaStack = listOf("Verilog/SystemVerilog", "Synopsys Design Compiler", "Python", "VCS"),
          description = "Join the GPU Tensor Core team responsible for architecting the next-generation Blackwell and Vera Rubin AI compute engines.",
          responsibilities = listOf(
            "Design and synthesize high-throughput matrix multiply units (FP8 / INT4)",
            "Perform cycle-accurate C++ and SystemVerilog co-simulation",
            "Optimize datapath pipelines for sub-nanosecond clock closure at 3.5 GHz"
          ),
          minimumQualifications = listOf(
            "B.S. or M.S. in Electrical Engineering, Computer Engineering, or related field",
            "Proficiency in Verilog / SystemVerilog RTL coding and digital logic design",
            "Understanding of pipeline hazard resolution, arithmetic ALUs, and clock gating"
          ),
          isTrendingRole = true,
          applicantCount = 38
        ),
        SemiconductorJobPosting(
          id = "job_apple_pd_sta",
          jobTitle = "Silicon Physical Design & Timing Signoff Engineer",
          company = "Apple Silicon",
          location = "Austin, TX (On-site)",
          domain = "Physical Design & Signoff (Floorplan/STA)",
          experienceLevel = "Entry to Mid-Level (1-3 yrs)",
          salaryRange = "$155,000 - $195,000 + RSUs",
          targetFabNode = "TSMC 3nm GAAFET",
          edaStack = listOf("Cadence Innovus", "Synopsys PrimeTime", "Ansys RedHawk", "Tcl"),
          description = "Deliver world-class M-series and A-series processors with state-of-the-art power, performance, and area (PPA) across billions of transistors.",
          responsibilities = listOf(
            "Floorplan hierarchical partitions, place hard macros (SRAM, PLLs), and power grid mesh synthesis",
            "Static Timing Analysis (STA) closure across multi-corner multi-mode (MCMM) PVT operating points",
            "Mitigate dynamic IR-drop droop and ensure clean DRC/LVS rule compliance"
          ),
          minimumQualifications = listOf(
            "Degree in ECE / VLSI Design with solid understanding of setup/hold slack equations",
            "Familiarity with standard cell library characterization and clock tree synthesis (CTS)",
            "Hands-on experience with ASIC layout tools or academic physical design projects"
          ),
          isTrendingRole = true,
          applicantCount = 29
        ),
        SemiconductorJobPosting(
          id = "job_tsmc_test_ate",
          jobTitle = "Advanced Silicon Test & DFT Validation Engineer",
          company = "TSMC",
          location = "Hsinchu, Taiwan / Phoenix, AZ",
          domain = "Silicon Test & DFT Diagnostics (ATE)",
          experienceLevel = "College Grad / Early Career",
          salaryRange = "$135,000 - $170,000 + Bonus",
          targetFabNode = "TSMC N4P / N3",
          edaStack = listOf("Siemens Tessent", "Advantest V93000", "Iddq Diagnostics", "ATPG"),
          description = "Drive silicon bringup and production test yield optimization for premier fabless customers on cutting-edge 3nm and 4nm foundry wafers.",
          responsibilities = listOf(
            "Develop scan chain patterns, memory BIST (MBIST), and boundary scan test vectors",
            "Diagnose wafer-level defect failures using automated test equipment (ATE) and Shmoo voltage-frequency plots",
            "Perform Iddq leakage screening to weed out bridging and gate dielectric shorts"
          ),
          minimumQualifications = listOf(
            "B.S./M.S. in Electrical Engineering, Semiconductor Physics, or related discipline",
            "Knowledge of fault modeling (Stuck-At, Transition Delay, Bridging) and scan compression",
            "Familiarity with semiconductor cleanroom manufacturing flows and wafer probers"
          ),
          isTrendingRole = true,
          applicantCount = 18
        ),
        SemiconductorJobPosting(
          id = "job_qcom_modem_rtl",
          jobTitle = "Snapdragon 5G/6G Modem RTL & Low-Power Design",
          company = "Qualcomm",
          location = "San Diego, CA (Hybrid)",
          domain = "Digital Logic & Timing Closure",
          experienceLevel = "Junior (1-3 yrs)",
          salaryRange = "$140,000 - $175,000 + Stock",
          targetFabNode = "4nm FinFET",
          edaStack = listOf("SystemVerilog", "UPF Low-Power", "Synopsys Formality", "Python"),
          description = "Engineer ultra-low-power digital signal processing blocks for mobile Snapdragon platforms operating under tight thermal envelopes.",
          responsibilities = listOf(
            "Implement multi-voltage island power domains using Unified Power Format (UPF)",
            "Integrate latch-based clock gating cells (ICG) to minimize dynamic switching power",
            "Resolve clock domain crossing (CDC) metastability in asynchronous FIFO interfaces"
          ),
          minimumQualifications = listOf(
            "Strong foundation in digital logic, finite state machines (FSM), and Gray code pointers",
            "Experience writing synthesizable Verilog and lint checking"
          ),
          isTrendingRole = false,
          applicantCount = 22
        )
      )
    }
  }
}
