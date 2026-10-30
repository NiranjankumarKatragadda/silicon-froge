package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.CustomSiliconRepository
import com.example.data.local.CustomNanometerChipDao
import com.example.data.local.CustomNanometerChipEntity
import com.example.data.local.StudentCustomCodeDao
import com.example.data.local.StudentCustomCodeEntity
import com.example.data.local.VlsiDatabase
import com.example.model.TechnologyNode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CustomSiliconTest {

  private lateinit var database: VlsiDatabase
  private lateinit var codeDao: StudentCustomCodeDao
  private lateinit var chipDao: CustomNanometerChipDao
  private lateinit var repository: CustomSiliconRepository

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    database = Room.inMemoryDatabaseBuilder(context, VlsiDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    codeDao = database.studentCodeDao()
    chipDao = database.customChipDao()
    repository = CustomSiliconRepository(codeDao, chipDao)
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun testStudentRtlAnalysis_calculatesSensibleGatesAndArea() {
    val verilogCode = """
      module custom_alu (
        input wire [3:0] A,
        input wire [3:0] B,
        input wire [2:0] sel,
        output reg [3:0] Y,
        output wire zero
      );
        always @(*) begin
          case(sel)
            3'b000: Y = A + B;
            default: Y = 4'b0000;
          endcase
        end
        assign zero = (Y == 4'b0000);
      endmodule
    """.trimIndent()

    val analysis28nm = repository.analyzeStudentRtl(verilogCode, targetNodeNm = 28)
    assertEquals("custom_alu", analysis28nm.moduleName)
    assertTrue("Should estimate non-zero gate count", analysis28nm.gateCount >= 20)
    assertTrue("Should estimate realistic cell area", analysis28nm.areaUm2 > 0.0)
    assertTrue("Should estimate propagation delay", analysis28nm.delayPs > 0.0)

    // Test scaling to 3nm
    val analysis3nm = repository.analyzeStudentRtl(verilogCode, targetNodeNm = 3)
    assertTrue("3nm cell area should be much smaller than 28nm", analysis3nm.areaUm2 < analysis28nm.areaUm2)
  }

  @Test
  fun testAluTestVectorSimulation_returnsAccurateResults() {
    val inputs = mapOf(
      "A" to 7,
      "B" to 5,
      "ALU_Sel" to 0 // ADD
    )
    val result = repository.simulateTestVector("alu_4bit", inputs)
    assertNotNull(result["ALU_Out[3:0]"])
    assertTrue("Sum of 7+5 should equal 12", result["ALU_Out[3:0]"].toString().contains("12"))
    assertEquals(0, result["CarryOut"])
    assertEquals(0, result["Zero"])
  }

  @Test
  fun testCustomChipPhysics_computesSensibleMetricsAcrossNodes() {
    // 28nm Edge AI chip: 6.0mm x 5.0mm = 30mm² at 2.0 GHz
    val metrics28nm = repository.calculateChipMetrics(
      node = TechnologyNode.NODE_28NM,
      dieWidthMm = 6.0,
      dieHeightMm = 5.0,
      frequencyGhz = 2.0
    )
    assertEquals(30.0, metrics28nm.dieAreaMm2, 0.01)
    assertTrue("28nm 30mm² should have over 200 million transistors", metrics28nm.totalTransistorsMillion > 200.0)
    assertTrue("Yield should be high for 30mm² die", metrics28nm.waferYieldPercent > 60.0)
    assertTrue("TDP should be within reasonable silicon budget", metrics28nm.totalTdpWatts > 0.5)

    // 3nm GAAFET Flagship chip: 10.0mm x 10.0mm = 100mm² at 4.0 GHz
    val metrics3nm = repository.calculateChipMetrics(
      node = TechnologyNode.NODE_3NM,
      dieWidthMm = 10.0,
      dieHeightMm = 10.0,
      frequencyGhz = 4.0
    )
    assertTrue("3nm 100mm² should reach billions of transistors", metrics3nm.totalTransistorsMillion > 10000.0)
    assertTrue("Good dies should be positive", metrics3nm.goodDiesPerWafer > 0)
  }

  @Test
  fun testDatabaseStudentCodeAndChipPersistence() = runBlocking {
    val code = StudentCustomCodeEntity(
      id = "test_code_1",
      title = "Test Ring Oscillator",
      description = "5-stage inverter ring oscillator",
      moduleName = "ring_osc",
      verilogCode = "module ring_osc(output wire osc_out); endmodule",
      targetNodeNm = 65,
      inputsList = "none",
      outputsList = "osc_out"
    )

    repository.saveCustomCode(code)
    val savedCodes = repository.customCodes.first()
    assertEquals(1, savedCodes.size)
    assertEquals("test_code_1", savedCodes[0].id)
    assertEquals("Test Ring Oscillator", savedCodes[0].title)

    val chip = CustomNanometerChipEntity(
      id = "test_chip_1",
      chipName = "Zenith-3nm HPC",
      codename = "ZEN-3-GAA",
      technologyNodeNm = 3,
      dieWidthMm = 12.0,
      dieHeightMm = 10.0,
      supplyVoltageV = 0.70,
      targetClockGhz = 4.5,
      metalLayersCount = 14,
      targetApplication = "Datacenter AI Accelerator",
      totalTransistorsMillion = 18500.0,
      totalTdpWatts = 85.0,
      dieAreaMm2 = 120.0,
      diesPerWafer = 450,
      waferYieldPercent = 78.4,
      drcStatus = "PASS",
      lvsStatus = "MATCHED",
      isTapeoutApproved = true
    )

    repository.saveCustomChip(chip)
    val savedChips = repository.customChips.first()
    assertEquals(1, savedChips.size)
    assertEquals("Zenith-3nm HPC", savedChips[0].chipName)
    assertEquals(3, savedChips[0].technologyNodeNm)
  }
}
