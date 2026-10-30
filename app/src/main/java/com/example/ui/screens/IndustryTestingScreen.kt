package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.IndustryJob
import com.example.model.IndustryTestProcedure
import com.example.ui.components.IndustrialJobSimulationView
import com.example.model.SiliconTestReport
import com.example.ui.theme.*
import com.example.viewmodel.SiliconLabViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun IndustryTestingScreen(
  viewModel: SiliconLabViewModel,
  modifier: Modifier = Modifier
) {
  var selectedSubTab by remember { mutableStateOf(0) } // 0=Workflows, 1=Device Simulator, 2=Jobs, 3=Reports

  val selectedProcedure by viewModel.selectedProcedure.collectAsStateWithLifecycle()
  val testVdd by viewModel.testVdd.collectAsStateWithLifecycle()
  val testTemp by viewModel.testTemp.collectAsStateWithLifecycle()
  val testFreq by viewModel.testFreq.collectAsStateWithLifecycle()
  val appliedFix by viewModel.appliedFix.collectAsStateWithLifecycle()
  val generatedReports by viewModel.generatedReports.collectAsStateWithLifecycle()

  var lastGeneratedReport by remember { mutableStateOf<SiliconTestReport?>(null) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(SiliconDark)
  ) {
    // Top Sub-Tab Selector
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(SiliconSurfaceDark)
        .padding(horizontal = 12.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      TabPill(
        title = "Job Workflows",
        isSelected = selectedSubTab == 0,
        onClick = { selectedSubTab = 0 },
        modifier = Modifier.weight(1f).testTag("subtab_workflows")
      )
      TabPill(
        title = "Device Bench",
        isSelected = selectedSubTab == 1,
        onClick = { selectedSubTab = 1 },
        modifier = Modifier.weight(1f).testTag("subtab_simulator")
      )
      TabPill(
        title = "Job Openings",
        isSelected = selectedSubTab == 2,
        onClick = { selectedSubTab = 2 },
        modifier = Modifier.weight(1f).testTag("subtab_jobs")
      )
      TabPill(
        title = "Reports (${generatedReports.size})",
        isSelected = selectedSubTab == 3,
        onClick = { selectedSubTab = 3 },
        modifier = Modifier.weight(1f).testTag("subtab_reports")
      )
    }

    when (selectedSubTab) {
      0 -> {
        LazyColumn(
          modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
          contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp)
        ) {
          item {
            IndustrialJobSimulationView()
          }
        }
      }
      1 -> TestSimulatorTab(
        procedures = viewModel.industryProcedures,
        selectedProcedure = selectedProcedure,
        onSelectProcedure = { viewModel.selectProcedure(it) },
        testVdd = testVdd,
        onVddChange = { viewModel.updateTestBenchVdd(it) },
        testTemp = testTemp,
        onTempChange = { viewModel.updateTestBenchTemp(it) },
        testFreq = testFreq,
        onFreqChange = { viewModel.updateTestBenchFreq(it) },
        appliedFix = appliedFix,
        onApplyFix = { viewModel.applyEngineeringFix() },
        onRunVerification = {
          val rep = viewModel.executeIndustrialTestVerification()
          lastGeneratedReport = rep
        },
        latestReport = lastGeneratedReport
      )
      2 -> SemiconductorJobsTab(
        jobs = viewModel.industryJobs,
        onSelectProcedure = { procId ->
          val proc = viewModel.industryProcedures.find { it.id == procId }
          if (proc != null) {
            viewModel.selectProcedure(proc)
            selectedSubTab = 1
          }
        }
      )
      3 -> TestReportsTab(reports = generatedReports)
    }
  }
}

@Composable
private fun TabPill(
  title: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    color = if (isSelected) NeonCyan else SiliconCardDark,
    shape = RoundedCornerShape(20.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) NeonCyan else SiliconBorder),
    modifier = modifier.clickable { onClick() }
  ) {
    Box(
      modifier = Modifier.padding(vertical = 8.dp),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = title,
        color = if (isSelected) Color.Black else TextPrimary,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
      )
    }
  }
}

// ====================================================================
// SUB-TAB 1: TEST SIMULATOR BENCH
// ====================================================================
@Composable
private fun TestSimulatorTab(
  procedures: List<IndustryTestProcedure>,
  selectedProcedure: IndustryTestProcedure,
  onSelectProcedure: (IndustryTestProcedure) -> Unit,
  testVdd: Float,
  onVddChange: (Float) -> Unit,
  testTemp: Float,
  onTempChange: (Float) -> Unit,
  testFreq: Float,
  onFreqChange: (Float) -> Unit,
  appliedFix: Boolean,
  onApplyFix: () -> Unit,
  onRunVerification: () -> Unit,
  latestReport: SiliconTestReport?
) {
  var showClues by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Procedure Selector Chips
    item {
      Text(
        text = "Select Real-World Industry Testing Scenario",
        style = MaterialTheme.typography.labelMedium,
        color = TextSecondary
      )
      Spacer(modifier = Modifier.height(6.dp))
      LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(procedures) { proc ->
          val isSelected = proc.id == selectedProcedure.id
          Surface(
            color = if (isSelected) NeonCyan.copy(alpha = 0.2f) else SiliconSurfaceDark,
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) NeonCyan else SiliconBorder),
            modifier = Modifier.clickable { onSelectProcedure(proc) }
          ) {
            Text(
              text = proc.deviceUnderStudy,
              color = if (isSelected) NeonCyan else TextPrimary,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
          }
        }
      }
    }

    // Procedure Brief Card
    item {
      Surface(
        color = SiliconSurfaceDark,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder)
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = selectedProcedure.companyTarget,
              style = MaterialTheme.typography.labelSmall,
              color = ConductorGold,
              fontWeight = FontWeight.Bold
            )
            Box(
              modifier = Modifier
                .background(SiliconCardDark, RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = selectedProcedure.standard,
                style = MaterialTheme.typography.labelSmall,
                color = NeonCyan,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
              )
            }
          }

          Text(
            text = selectedProcedure.title,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
          )

          Text(
            text = selectedProcedure.fullScenario,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
          )

          // Failure Alert
          Surface(
            color = SiliconErrorRed.copy(alpha = 0.15f),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SiliconErrorRed.copy(alpha = 0.6f))
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = SiliconErrorRed, modifier = Modifier.size(20.dp))
              Column {
                Text(text = "Silicon Defect Symptom:", style = MaterialTheme.typography.labelSmall, color = SiliconErrorRed, fontWeight = FontWeight.Bold)
                Text(text = selectedProcedure.failureSymptom, style = MaterialTheme.typography.labelSmall, color = TextPrimary)
              }
            }
          }
        }
      }
    }

    // Engineering Clues Collapsible
    item {
      Surface(
        color = SiliconCardDark,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder),
        modifier = Modifier.clickable { showClues = !showClues }
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Icon(Icons.Default.Lightbulb, contentDescription = null, tint = ConductorGold, modifier = Modifier.size(16.dp))
              Text("Engineering Diagnostic Clues", style = MaterialTheme.typography.labelMedium, color = ConductorGold, fontWeight = FontWeight.Bold)
            }
            Icon(if (showClues) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = TextSecondary)
          }

          if (showClues) {
            Spacer(modifier = Modifier.height(8.dp))
            selectedProcedure.diagnosisClues.forEach { clue ->
              Text(
                text = "• $clue",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 2.dp)
              )
            }
          }
        }
      }
    }

    // ATE Test Bench Controls
    item {
      Surface(
        color = SiliconSurfaceDark,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder)
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(text = "Interactive Test Bench Tuning & PVT Corners", style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)

          // 1. VDD Corner Slider
          Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("Supply Voltage Corner (VDD)", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
              Text("${String.format("%.2f", testVdd)} V", style = MaterialTheme.typography.labelSmall, color = NeonCyan, fontFamily = FontFamily.Monospace)
            }
            Slider(
              value = testVdd,
              onValueChange = onVddChange,
              valueRange = 0.70f..1.35f,
              modifier = Modifier.testTag("test_vdd_slider")
            )
          }

          // 2. Temperature Corner Slider
          Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("Chamber Temperature Corner", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
              Text("${testTemp.toInt()} °C", style = MaterialTheme.typography.labelSmall, color = CleanroomAmber, fontFamily = FontFamily.Monospace)
            }
            Slider(
              value = testTemp,
              onValueChange = onTempChange,
              valueRange = -40f..150f,
              modifier = Modifier.testTag("test_temp_slider")
            )
          }

          // 3. Test Frequency Target
          Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("ATE Pin Strobe Frequency", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
              Text("${String.format("%.2f", testFreq)} GHz", style = MaterialTheme.typography.labelSmall, color = LogicHighGreen, fontFamily = FontFamily.Monospace)
            }
            Slider(
              value = testFreq,
              onValueChange = onFreqChange,
              valueRange = 0.1f..4.0f,
              modifier = Modifier.testTag("test_freq_slider")
            )
          }

          Spacer(modifier = Modifier.height(4.dp))

          // Apply Engineering Fix Toggle Button
          Button(
            onClick = onApplyFix,
            colors = ButtonDefaults.buttonColors(containerColor = if (appliedFix) LogicHighGreen else CleanroomAmber),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().testTag("apply_engineering_fix_button")
          ) {
            Icon(
              imageVector = if (appliedFix) Icons.Default.CheckCircle else Icons.Default.Build,
              contentDescription = null,
              tint = Color.Black,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (appliedFix) "Engineering Fix Applied (Calibrated)" else "Apply Engineering Diagnostic Fix & Re-bias",
              color = Color.Black,
              fontWeight = FontWeight.Bold
            )
          }

          // Run Signoff Verification Button
          Button(
            onClick = onRunVerification,
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().testTag("run_test_verification_button")
          ) {
            Icon(Icons.Default.Verified, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Run Industrial Test Signoff Verification", color = Color.Black, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // Latest Test Verification Report Card
    if (latestReport != null) {
      item {
        ReportCard(report = latestReport)
      }
    }
  }
}

// ====================================================================
// SUB-TAB 2: SEMICONDUCTOR JOBS
// ====================================================================
@Composable
private fun SemiconductorJobsTab(
  jobs: List<IndustryJob>,
  onSelectProcedure: (String) -> Unit
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Text(
        text = "Open Semiconductor Engineering Career Tracks",
        style = MaterialTheme.typography.titleMedium,
        color = NeonCyan,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "Each role maps to actual testing and verification procedures used by top silicon companies.",
        style = MaterialTheme.typography.bodySmall,
        color = TextSecondary
      )
    }

    items(jobs) { job ->
      Surface(
        color = SiliconSurfaceDark,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder)
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(text = job.company, style = MaterialTheme.typography.labelSmall, color = ConductorGold, fontWeight = FontWeight.Bold)
              Text(text = job.title, style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
            }
            Box(
              modifier = Modifier
                .background(NeonCyan.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(text = job.experienceLevel, style = MaterialTheme.typography.labelSmall, color = NeonCyan, fontSize = 10.sp)
            }
          }

          Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "📍 ${job.location}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text(text = "💰 ${job.salaryRange}", style = MaterialTheme.typography.labelSmall, color = LogicHighGreen, fontFamily = FontFamily.Monospace)
          }

          Text(text = "Core Competencies:", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
          job.keyRequirements.forEach { req ->
            Text(text = "• $req", style = MaterialTheme.typography.bodySmall, color = TextSecondary, fontSize = 12.sp)
          }

          Spacer(modifier = Modifier.height(4.dp))

          Button(
            onClick = { onSelectProcedure(job.industrialSimulationId) },
            colors = ButtonDefaults.buttonColors(containerColor = SiliconCardElevated),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(Icons.Default.PlayCircle, contentDescription = null, tint = ConductorGold, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Simulate This Job's Testing Procedure", color = TextPrimary)
          }
        }
      }
    }
  }
}

// ====================================================================
// SUB-TAB 3: TEST VERIFICATION REPORTS
// ====================================================================
@Composable
private fun TestReportsTab(reports: List<SiliconTestReport>) {
  if (reports.isEmpty()) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(32.dp),
      contentAlignment = Alignment.Center
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Default.Assessment, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(48.dp))
        Text("No Verification Reports Yet", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        Text(
          text = "Go to the 'Industry Test Simulator' tab, tune the bench parameters, apply engineering fixes, and run verification.",
          style = MaterialTheme.typography.bodySmall,
          color = TextSecondary,
          textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
      }
    }
  } else {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      items(reports) { rep ->
        ReportCard(report = rep)
      }
    }
  }
}

@Composable
private fun ReportCard(report: SiliconTestReport) {
  val dateStr = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(report.timestamp))

  Surface(
    color = SiliconSurfaceDark,
    shape = RoundedCornerShape(14.dp),
    border = androidx.compose.foundation.BorderStroke(
      1.5.dp,
      if (report.isQualified) LogicHighGreen else SiliconErrorRed
    ),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(text = "SILICON TEST VERIFICATION REPORT", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 9.sp)
          Text(text = report.testProcedureTitle, style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
        }
        Box(
          modifier = Modifier
            .background(
              if (report.isQualified) LogicHighGreen.copy(alpha = 0.2f) else SiliconErrorRed.copy(alpha = 0.2f),
              RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = if (report.isQualified) "QUALIFIED" else "UNQUALIFIED",
            color = if (report.isQualified) LogicHighGreen else SiliconErrorRed,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
          modifier = Modifier
            .weight(1f)
            .background(SiliconCardDark, RoundedCornerShape(8.dp))
            .padding(8.dp)
        ) {
          Column {
            Text(text = "Final Yield", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text(text = "${String.format("%.1f", report.finalYieldPercent)}%", style = MaterialTheme.typography.titleSmall, color = LogicHighGreen, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
          }
        }
        Box(
          modifier = Modifier
            .weight(1f)
            .background(SiliconCardDark, RoundedCornerShape(8.dp))
            .padding(8.dp)
        ) {
          Column {
            Text(text = "Measured Iddq", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text(text = "${String.format("%.1f", report.measuredIddqUa)} µA", style = MaterialTheme.typography.titleSmall, color = if (report.measuredIddqUa < 50f) LogicHighGreen else SiliconErrorRed, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
          }
        }
        Box(
          modifier = Modifier
            .weight(1f)
            .background(SiliconCardDark, RoundedCornerShape(8.dp))
            .padding(8.dp)
        ) {
          Column {
            Text(text = "Max Freq", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text(text = "${String.format("%.2f", report.achievedFrequencyGhz)} GHz", style = MaterialTheme.typography.titleSmall, color = NeonCyan, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
          }
        }
      }

      Text(
        text = "Assessment & Recommendation: ${report.hiringRecommendation}",
        style = MaterialTheme.typography.bodySmall,
        color = TextPrimary
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(text = "Report ID: ${report.id} • $dateStr", style = MaterialTheme.typography.labelSmall, color = TextTertiary, fontSize = 10.sp)
        Text(text = report.qualificationGrade, style = MaterialTheme.typography.labelSmall, color = ConductorGold, fontWeight = FontWeight.Bold)
      }
    }
  }
}
