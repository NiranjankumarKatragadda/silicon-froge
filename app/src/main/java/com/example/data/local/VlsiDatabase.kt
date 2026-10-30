package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
  entities = [
    VlsiExperimentEntity::class,
    StudentCustomCodeEntity::class,
    CustomNanometerChipEntity::class,
    JobApplicationEntity::class
  ],
  version = 3,
  exportSchema = false
)
abstract class VlsiDatabase : RoomDatabase() {

  abstract fun experimentDao(): VlsiExperimentDao
  abstract fun studentCodeDao(): StudentCustomCodeDao
  abstract fun customChipDao(): CustomNanometerChipDao
  abstract fun jobApplicationDao(): JobApplicationDao

  companion object {
    @Volatile
    private var INSTANCE: VlsiDatabase? = null

    fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): VlsiDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          VlsiDatabase::class.java,
          "silicon_forge_vlsi.db"
        )
          .fallbackToDestructiveMigration()
          .addCallback(DatabaseCallback(scope))
          .build()
        INSTANCE = instance
        instance
      }
    }

    private class DatabaseCallback(
      private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
      override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        INSTANCE?.let { database ->
          scope.launch {
            populateInitialExperiments(database.experimentDao())
            populateInitialStudentProjects(database.studentCodeDao(), database.customChipDao())
            populateInitialJobApplications(database.jobApplicationDao())
          }
        }
      }
    }

    suspend fun populateInitialJobApplications(appDao: JobApplicationDao) {
      if (appDao.getApplicationCountSync() == 0) {
        appDao.insertApplications(
          listOf(
            JobApplicationEntity(
              id = "app_nv_001",
              jobId = "job_nv_tpu_arch",
              jobTitle = "AI Hardware Architecture & RTL Engineer",
              company = "NVIDIA",
              location = "Santa Clara, CA",
              domain = "AI ASIC & NPU Hardware Architecture",
              applicantName = "Alex Chen",
              applicantEmail = "alex.chen@stanford.edu",
              degreeLevel = "M.S. VLSI & Computer Engineering",
              gpaOrGrade = "3.88",
              portfolioSummary = "Designed 4-bit Configurable ALU and Aegis-3X 3nm TPU on TSMC N3E PDK",
              targetFabNode = "TSMC 3nm N3E",
              atsMatchScore = 94,
              status = "Interview Offered",
              submissionTimestamp = System.currentTimeMillis() - (1000L * 60 * 60 * 36),
              recruiterFeedback = "Strong Verilog and 3nm transistor sizing credentials. Advanced to Round 1 Technical Panel with Principal Architecture Lead."
            ),
            JobApplicationEntity(
              id = "app_apple_002",
              jobId = "job_apple_pd_sta",
              jobTitle = "Silicon Physical Design & Timing Signoff Engineer",
              company = "Apple Silicon",
              location = "Austin, TX",
              domain = "Physical Design & Signoff (Floorplan/STA)",
              applicantName = "Alex Chen",
              applicantEmail = "alex.chen@stanford.edu",
              degreeLevel = "M.S. VLSI & Computer Engineering",
              gpaOrGrade = "3.88",
              portfolioSummary = "Completed STA Slack Closure, Macro Placement & IR-Drop Power Mesh Virtual Labs",
              targetFabNode = "TSMC 3nm GAAFET",
              atsMatchScore = 89,
              status = "Technical Assessment",
              submissionTimestamp = System.currentTimeMillis() - (1000L * 60 * 60 * 18),
              recruiterFeedback = "ATS verified setup/hold margin calculations. Coding challenge link dispatched."
            )
          )
        )
      }
    }

    suspend fun populateInitialStudentProjects(
      codeDao: StudentCustomCodeDao,
      chipDao: CustomNanometerChipDao
    ) {
      if (codeDao.getCodeCount() == 0) {
        codeDao.insertOrUpdateCode(
          StudentCustomCodeEntity(
            id = "starter_alu_4bit",
            title = "4-bit Arithmetic Logic Unit (ALU)",
            description = "Custom 4-bit ALU with opcode selector [2:0] supporting ADD, SUB, AND, OR, XOR, and SLT with Zero/Carry flags.",
            moduleName = "alu_4bit",
            verilogCode = """
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
            3'b000: tmp = A + B;       // ADD
            3'b001: tmp = A - B;       // SUB
            3'b010: tmp = {1'b0, A & B}; // AND
            3'b011: tmp = {1'b0, A | B}; // OR
            3'b100: tmp = {1'b0, A ^ B}; // XOR
            3'b101: tmp = {4'b0, (A < B)}; // SLT
            default: tmp = 5'b00000;
        endcase
        ALU_Out = tmp[3:0];
    end

    assign CarryOut = tmp[4];
    assign Zero = (ALU_Out == 4'b0000);
endmodule
            """.trimIndent(),
            targetNodeNm = 28,
            inputsList = "A[3:0], B[3:0], ALU_Sel[2:0]",
            outputsList = "ALU_Out[3:0], CarryOut, Zero",
            estimatedGateCount = 68,
            estimatedAreaUm2 = 124.5,
            estimatedDelayPs = 142.0,
            estimatedPowerUw = 38.4,
            isFavorite = true
          )
        )

        codeDao.insertOrUpdateCode(
          StudentCustomCodeEntity(
            id = "starter_fsm_traffic",
            title = "Traffic Light Controller FSM",
            description = "3-State Moore FSM with RED, GREEN, and YELLOW cycle sequencing and pedestrian priority interrupt.",
            moduleName = "traffic_fsm",
            verilogCode = """
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
            targetNodeNm = 65,
            inputsList = "clk, rst_n, ped_sensor",
            outputsList = "light_state[1:0]",
            estimatedGateCount = 42,
            estimatedAreaUm2 = 210.8,
            estimatedDelayPs = 195.0,
            estimatedPowerUw = 18.2,
            isFavorite = false
          )
        )
      }

      if (chipDao.getChipCount() == 0) {
        chipDao.insertOrUpdateChip(
          CustomNanometerChipEntity(
            id = "chip_neural_3nm",
            chipName = "Aegis-3X Neural TPU",
            codename = "NANO-TENSOR-GAA",
            technologyNodeNm = 3,
            dieWidthMm = 7.5,
            dieHeightMm = 6.8,
            supplyVoltageV = 0.70,
            targetClockGhz = 3.6,
            metalLayersCount = 14,
            targetApplication = "On-Device LLM & Vision Transformer Acceleration",
            totalTransistorsMillion = 8200.0,
            totalTdpWatts = 18.5,
            dieAreaMm2 = 51.0,
            diesPerWafer = 1120,
            waferYieldPercent = 89.2,
            drcStatus = "PASS",
            lvsStatus = "MATCHED",
            isTapeoutApproved = true
          )
        )

        chipDao.insertOrUpdateChip(
          CustomNanometerChipEntity(
            id = "chip_iot_28nm",
            chipName = "SenseEdge-28 Ultra-Low-Power MCU",
            codename = "FDSOI-MICRO",
            technologyNodeNm = 28,
            dieWidthMm = 3.2,
            dieHeightMm = 3.0,
            supplyVoltageV = 0.90,
            targetClockGhz = 1.2,
            metalLayersCount = 9,
            targetApplication = "Automotive Battery Management & IoT Sensor Hub",
            totalTransistorsMillion = 110.0,
            totalTdpWatts = 0.45,
            dieAreaMm2 = 9.6,
            diesPerWafer = 5820,
            waferYieldPercent = 96.8,
            drcStatus = "PASS",
            lvsStatus = "MATCHED",
            isTapeoutApproved = true
          )
        )
      }
    }

    suspend fun populateInitialExperiments(dao: VlsiExperimentDao) {
      val initialList = listOf(
        // ================= CMOS DOMAIN =================
        VlsiExperimentEntity(
          id = "exp_cmos_vtc",
          title = "CMOS Inverter VTC & Noise Margin Analysis",
          domain = "CMOS",
          description = "Measure voltage transfer curve (VTC), evaluate midpoint threshold VM, and compute noise margins NML and NMH across Wp/Wn aspect ratios.",
          theoreticalFormula = "VM = (VDD - |Vtp| + r*Vtn) / (1 + r), where r = sqrt(beta_n / beta_p)",
          difficulty = "Beginner",
          durationMinutes = 25,
          isCompleted = true,
          isFavorite = true,
          keyLearningObjectives = "Understand carrier mobility compensation, derive switching threshold VM = VDD/2, and calculate static noise immunity.",
          industryApplication = "Essential for sub-0.8V standard cell library characterization at TSMC and Intel."
        ),
        VlsiExperimentEntity(
          id = "exp_cmos_transmission_gate",
          title = "CMOS Transmission Gate & Pass-Transistor Logic",
          domain = "CMOS",
          description = "Investigate bidirectional signal transmission, full-rail swing without threshold voltage drop (Vtn / |Vtp| loss), and Ron resistance modulation.",
          theoreticalFormula = "R_on = R_n || R_p ≈ 1 / (mu_n*Cox*(W/L)_n*(VDD - Vth))",
          difficulty = "Intermediate",
          durationMinutes = 30,
          isCompleted = false,
          isFavorite = false,
          keyLearningObjectives = "Compare single NMOS pass transistors with parallel complementary TG switches to eliminate degraded logic 1 transfer.",
          industryApplication = "Widely used in low-power multiplexers, SRAM access transistors, and barrel shifters in ARM and Apple processors."
        ),
        VlsiExperimentEntity(
          id = "exp_cmos_dynamic_domino",
          title = "Dynamic Domino CMOS Logic & Charge Sharing",
          domain = "CMOS",
          description = "Simulate precharge and evaluate clock phases, charge leakage through parasitic junction capacitance, and keeper transistor sizing.",
          theoreticalFormula = "V_drop = VDD * (C_internal / (C_internal + C_out))",
          difficulty = "Advanced",
          durationMinutes = 40,
          isCompleted = false,
          isFavorite = true,
          keyLearningObjectives = "Analyze high-speed dynamic logic hazards, race conditions, and keeper sizing to mitigate subthreshold charge leakage.",
          industryApplication = "High-performance integer execution ALUs and register files in IBM POWER and AMD Zen compute cores."
        ),

        // ================= DIGITAL LOGIC DOMAIN =================
        VlsiExperimentEntity(
          id = "exp_digital_icg_clock",
          title = "Integrated Clock Gating (ICG) Timing Closure",
          domain = "Digital Logic",
          description = "Configure latch-based ICG cells to suppress clock tree switching activity during register idle states without causing clock glitches.",
          theoreticalFormula = "P_dyn = C_tree * VDD^2 * f * alpha, where alpha is switching activity",
          difficulty = "Intermediate",
          durationMinutes = 35,
          isCompleted = true,
          isFavorite = true,
          keyLearningObjectives = "Prevent clock glitches during enable transitions, minimize dynamic clock network dissipation, and verify setup/hold on enable latch.",
          industryApplication = "Standard implementation across all low-power mobile SoCs (Qualcomm Snapdragon, Apple A-series)."
        ),
        VlsiExperimentEntity(
          id = "exp_digital_async_fifo",
          title = "Clock Domain Crossing (CDC) & Async FIFO",
          domain = "Digital Logic",
          description = "Design dual-clock asynchronous FIFO with Gray-coded read/write pointer synchronization across independent clock domains.",
          theoreticalFormula = "MTBF = exp(t_resolve / tau) / (T0 * f_clk * f_data)",
          difficulty = "Advanced",
          durationMinutes = 45,
          isCompleted = false,
          isFavorite = false,
          keyLearningObjectives = "Avoid bus incoherence during clock domain crossings, calculate metastability Mean Time Between Failures (MTBF), and master Gray code pointers.",
          industryApplication = "Inter-core communications, PCIe controllers, and DDR5 memory controller PHY interfaces."
        ),
        VlsiExperimentEntity(
          id = "exp_digital_sta_slack",
          title = "Static Timing Analysis (STA) Setup & Hold Margins",
          domain = "Digital Logic",
          description = "Derive and close timing on a 2.5GHz datapath with clock skew, jitter, and PVT (Process, Voltage, Temperature) corners.",
          theoreticalFormula = "Slack_setup = (T_period + T_skew) - (t_cq + t_comb + t_setup) > 0",
          difficulty = "Intermediate",
          durationMinutes = 30,
          isCompleted = false,
          isFavorite = true,
          keyLearningObjectives = "Master the critical difference between setup (max-delay) and hold (min-delay) constraints under fast and slow signoff corners.",
          industryApplication = "Synopsys PrimeTime signoff required on 100% of ASIC and GPU tapeouts."
        ),

        // ================= LAYOUT DESIGN DOMAIN =================
        VlsiExperimentEntity(
          id = "exp_layout_floorplan_aspect",
          title = "ASIC Floorplanning, Macro Placement & Core Aspect Ratio",
          domain = "Layout Design",
          description = "Optimize die boundaries, core utilization percentage (50-80%), and place SRAM/PLL macros to minimize wire congestion.",
          theoreticalFormula = "Core_Util = (Area_standard_cells + Area_macros) / Area_core * 100%",
          difficulty = "Intermediate",
          durationMinutes = 35,
          isCompleted = false,
          isFavorite = false,
          keyLearningObjectives = "Minimize critical routing channel congestion, establish clean halo keepouts around macros, and align I/O pad rings.",
          industryApplication = "Initial physical design floorplanning stage in Cadence Innovus / Synopsys ICC2."
        ),
        VlsiExperimentEntity(
          id = "exp_layout_ir_drop_mesh",
          title = "Power Grid Mesh Design & Static/Dynamic IR-Drop",
          domain = "Layout Design",
          description = "Synthesize vertical and horizontal VDD/VSS metal stripes across top copper layers (M6/M7) to prevent supply voltage droop.",
          theoreticalFormula = "V_drop = I_total * (R_stripe / N_stripes) <= 25mV",
          difficulty = "Advanced",
          durationMinutes = 40,
          isCompleted = false,
          isFavorite = true,
          keyLearningObjectives = "Evaluate electro-migration (EM) limits, prevent transistor starvation, and optimize decoupling capacitance (decap) placement.",
          industryApplication = "Crucial signoff step using Ansys RedHawk for 3nm/5nm chips to prevent clock jitter and unexpected silicon resets."
        ),
        VlsiExperimentEntity(
          id = "exp_layout_antenna_effect",
          title = "Antenna Effect Prevention & Diode Insertion",
          domain = "Layout Design",
          description = "Identify long interconnect metal lines accumulating plasma charge during RIE etching and insert reverse-biased protection diodes.",
          theoreticalFormula = "Antenna_Ratio = Area_metal_conductor / Area_gate_oxide <= Limit",
          difficulty = "Intermediate",
          durationMinutes = 25,
          isCompleted = false,
          isFavorite = false,
          keyLearningObjectives = "Prevent gate oxide electrostatic punch-through during wafer metallization by inserting antenna diodes or routing layer jumpers.",
          industryApplication = "Mandatory Design Rule Check (DRC) signoff at TSMC, Samsung Foundry, and GlobalFoundries."
        ),

        // ================= WAFER FAB DOMAIN =================
        VlsiExperimentEntity(
          id = "exp_fab_deal_grove",
          title = "Thermal Oxidation & Deal-Grove Kinetics",
          domain = "Wafer Fab",
          description = "Model dry and wet thermal oxidation growth kinetics in a 1050°C diffusion furnace for silicon dioxide (SiO2) gate and isolation layers.",
          theoreticalFormula = "x_ox^2 + A * x_ox = B * (t + tau), where B is parabolic rate constant",
          difficulty = "Beginner",
          durationMinutes = 20,
          isCompleted = true,
          isFavorite = false,
          keyLearningObjectives = "Differentiate linear reaction-rate regime from diffusion-limited parabolic regime in thin versus thick oxide growth.",
          industryApplication = "Foundational furnace operations in semiconductor manufacturing cleanrooms worldwide."
        ),

        // ================= SILICON TEST DOMAIN =================
        VlsiExperimentEntity(
          id = "exp_test_iddq_bridging",
          title = "ATE Iddq Quiescent Current Defect Screening",
          domain = "Silicon Test",
          description = "Execute vector-by-vector quiescent current leakage measurement to isolate metal bridging shorts and gate dielectric breakdown.",
          theoreticalFormula = "Iddq_pass < 35uA, Iddq_defect > 200uA",
          difficulty = "Intermediate",
          durationMinutes = 30,
          isCompleted = false,
          isFavorite = true,
          keyLearningObjectives = "Isolate physical manufacturing defects that pass functional speed test patterns but cause latent catastrophic failures in the field.",
          industryApplication = "Automotive qualification testing under AEC-Q100 standards for zero-defect automotive chips."
        )
      )

      dao.insertExperiments(initialList)
    }
  }
}
