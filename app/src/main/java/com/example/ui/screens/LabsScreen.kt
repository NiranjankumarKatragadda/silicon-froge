package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.SiliconLabRepository
import com.example.model.DigitalCircuit
import com.example.model.DomainType
import com.example.model.FabStep
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.SiliconLabViewModel

@Composable
fun LabsScreen(
  viewModel: SiliconLabViewModel,
  customSiliconViewModel: com.example.viewmodel.CustomSiliconViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
  modifier: Modifier = Modifier
) {
  val selectedDomain by viewModel.selectedDomain.collectAsStateWithLifecycle()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(SiliconDark)
  ) {
    // Horizontal Domain Selection Bar
    LazyRow(
      modifier = Modifier
        .fillMaxWidth()
        .background(SiliconSurfaceDark)
        .padding(horizontal = 12.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(DomainType.values()) { domain ->
        val isSelected = domain == selectedDomain
        Surface(
          color = if (isSelected) NeonCyan else SiliconCardDark,
          shape = RoundedCornerShape(20.dp),
          border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) NeonCyan else SiliconBorder
          ),
          modifier = Modifier
            .clickable { viewModel.selectDomain(domain) }
            .testTag("tab_${domain.name.lowercase()}")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(
              imageVector = when (domain) {
                DomainType.CUSTOM_HDL -> Icons.Default.Code
                DomainType.CUSTOM_CHIP -> Icons.Default.DeveloperBoard
                DomainType.DIGITAL -> Icons.Default.Memory
                DomainType.ANALOG -> Icons.Default.ShowChart
                DomainType.FABRICATION -> Icons.Default.Science
                DomainType.PHYSICAL_DESIGN -> Icons.Default.GridOn
                DomainType.TEST_VALIDATION -> Icons.Default.Speed
                DomainType.CATALOG -> Icons.Default.Storage
              },
              contentDescription = null,
              tint = if (isSelected) Color.Black else TextSecondary,
              modifier = Modifier.size(16.dp)
            )
            Text(
              text = domain.title,
              color = if (isSelected) Color.Black else TextPrimary,
              style = MaterialTheme.typography.labelMedium,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
          }
        }
      }
    }

    // Domain Content Body
    when (selectedDomain) {
      DomainType.CUSTOM_HDL -> CustomCodePlaygroundView(repository = customSiliconViewModel.repository)
      DomainType.CUSTOM_CHIP -> CustomNanometerChipStudioView(repository = customSiliconViewModel.repository)
      DomainType.DIGITAL -> DigitalLabTab(viewModel)
      DomainType.ANALOG -> AnalogLabTab(viewModel)
      DomainType.FABRICATION -> FabricationLabTab(viewModel)
      DomainType.PHYSICAL_DESIGN -> PhysicalDesignLabTab(viewModel)
      DomainType.TEST_VALIDATION -> TestValidationLabTab(viewModel)
      DomainType.CATALOG -> {
        LazyColumn(
          modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
          contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
        ) {
          item {
            ExperimentDashboardView(labViewModel = viewModel)
          }
        }
      }
    }
  }
}

// ====================================================================
// TAB 1: DIGITAL VLSI & TIMING
// ====================================================================
@Composable
private fun DigitalLabTab(viewModel: SiliconLabViewModel) {
  var digitalSubSection by remember { mutableStateOf(0) } // 0: Logic Gate Workbench, 1: Circuit Modules, 2: STA Timing & Slack

  val selectedCircuit by viewModel.selectedCircuit.collectAsStateWithLifecycle()
  val clockFreqMhz by viewModel.clockFreqMhz.collectAsStateWithLifecycle()
  val tCombNs by viewModel.tCombNs.collectAsStateWithLifecycle()
  val clockSkewNs by viewModel.clockSkewNs.collectAsStateWithLifecycle()
  val timingResult by viewModel.timingResult.collectAsStateWithLifecycle()

  var showVerilogModal by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Sub-domain Navigation Bar
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(SiliconSurfaceDark, RoundedCornerShape(10.dp))
          .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        val subTabs = listOf("Gate Workbench", "Circuit Modules", "Timing & Slack")
        subTabs.forEachIndexed { idx, label ->
          val isSel = digitalSubSection == idx
          Surface(
            color = if (isSel) NeonCyan else SiliconCardDark,
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) NeonCyan else SiliconBorder),
            modifier = Modifier
              .weight(1f)
              .clickable { digitalSubSection = idx }
              .testTag("digital_sub_${idx}")
          ) {
            Box(modifier = Modifier.padding(vertical = 7.dp), contentAlignment = Alignment.Center) {
              Text(
                text = label,
                color = if (isSel) Color.Black else TextPrimary,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.sp
              )
            }
          }
        }
      }
    }

    when (digitalSubSection) {
      0 -> {
        item {
          LogicGateWorkbenchView()
        }
      }
      1 -> {
        item {
          CircuitDesignModuleView()
        }
      }
      2 -> {
        // Circuit Selector
        item {
          Text(
            text = "Select Digital Logic Cell",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
          )
          Spacer(modifier = Modifier.height(6.dp))
          LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(SiliconLabRepository.digitalCircuits) { circ ->
              val isSelected = circ.id == selectedCircuit.id
              Surface(
                color = if (isSelected) NeonCyan.copy(alpha = 0.2f) else SiliconSurfaceDark,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(
                  1.dp,
                  if (isSelected) NeonCyan else SiliconBorder
                ),
                modifier = Modifier.clickable { viewModel.selectCircuit(circ) }
              ) {
                Text(
                  text = circ.name,
                  color = if (isSelected) NeonCyan else TextPrimary,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
              }
            }
          }
        }

        // Circuit Description & Verilog Button
        item {
          Surface(
            color = SiliconSurfaceDark,
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder)
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = selectedCircuit.name,
                  style = MaterialTheme.typography.titleSmall,
                  color = ConductorGold,
                  fontWeight = FontWeight.Bold
                )
                FilledTonalButton(
                  onClick = { showVerilogModal = !showVerilogModal },
                  colors = ButtonDefaults.filledTonalButtonColors(containerColor = SiliconCardDark),
                  contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                  Icon(Icons.Default.Code, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(if (showVerilogModal) "Hide Verilog" else "View Verilog", color = NeonCyan, fontSize = 11.sp)
                }
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(text = selectedCircuit.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)

              if (showVerilogModal) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                  color = Color(0xFF070B14),
                  shape = RoundedCornerShape(6.dp),
                  border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder.copy(alpha = 0.5f))
                ) {
                  Text(
                    text = selectedCircuit.verilogSnippet,
                    color = LogicHighGreen,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(8.dp)
                  )
                }
              }
            }
          }
        }

        // Live Timing Waveform Canvas
        item {
          WaveformTimelineCanvas(timing = timingResult)
        }

        // Interactive Knobs (Sliders)
        item {
          Surface(
            color = SiliconSurfaceDark,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder),
            modifier = Modifier.padding(top = 4.dp)
          ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
              Text(
                text = "Static Timing Analysis (STA) Parameters",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
              )

              // 1. Clock Frequency Slider
              Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Text("Clock Frequency (f_clk)", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                  Text("${clockFreqMhz.toInt()} MHz", style = MaterialTheme.typography.labelSmall, color = NeonCyan, fontFamily = FontFamily.Monospace)
                }
                Slider(
                  value = clockFreqMhz,
                  onValueChange = { viewModel.updateClockFreq(it) },
                  valueRange = 200f..2500f,
                  modifier = Modifier.testTag("clock_freq_slider")
                )
              }

              // 2. Combinational Delay Slider
              Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Text("Data Path Comb Delay (t_comb)", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                  Text("${String.format("%.3f", tCombNs)} ns", style = MaterialTheme.typography.labelSmall, color = ConductorGold, fontFamily = FontFamily.Monospace)
                }
                Slider(
                  value = tCombNs,
                  onValueChange = { viewModel.updateCombDelay(it) },
                  valueRange = 0.10f..1.10f,
                  modifier = Modifier.testTag("tcomb_delay_slider")
                )
              }

              // 3. Clock Skew Slider
              Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Text("Clock Skew (Δt_skew)", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                  Text("${String.format("%+.3f", clockSkewNs)} ns", style = MaterialTheme.typography.labelSmall, color = CleanroomAmber, fontFamily = FontFamily.Monospace)
                }
                Slider(
                  value = clockSkewNs,
                  onValueChange = { viewModel.updateClockSkew(it) },
                  valueRange = -0.12f..0.12f,
                  modifier = Modifier.testTag("clock_skew_slider")
                )
              }

              Button(
                onClick = {
                  viewModel.addLabNotebookEntry(
                    title = "Digital STA: ${selectedCircuit.name}",
                    domain = DomainType.DIGITAL,
                    values = "f_clk=${clockFreqMhz.toInt()}MHz, t_comb=${String.format("%.3f", tCombNs)}ns, skew=${String.format("%+.3f", clockSkewNs)}ns, SetupSlack=${String.format("%+.3f", timingResult.setupSlackNs)}ns, HoldSlack=${String.format("%+.3f", timingResult.holdSlackNs)}ns",
                    remarks = if (timingResult.hasSetupViolation) "Setup violation observed. Need pipelining or clock frequency reduction." else "Timing closed cleanly."
                  )
                },
                colors = ButtonDefaults.buttonColors(containerColor = SiliconCardElevated),
                modifier = Modifier.fillMaxWidth()
              ) {
                Icon(Icons.Default.BookmarkAdd, contentDescription = null, tint = ConductorGold, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Record Measurements to Lab Notebook", color = TextPrimary)
              }
            }
          }
        }
      }
    }
  }
}

// ====================================================================
// TAB 2: ANALOG & CMOS INVERTER VTC
// ====================================================================
@Composable
private fun AnalogLabTab(viewModel: SiliconLabViewModel) {
  val cmosSpec by viewModel.cmosSpec.collectAsStateWithLifecycle()
  val vtcAnalysis by viewModel.vtcAnalysis.collectAsStateWithLifecycle()

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      VtcCurveCanvas(analysis = vtcAnalysis, vdd = cmosSpec.vdd)
    }

    // Transistor Tuning Controls
    item {
      Surface(
        color = SiliconSurfaceDark,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder)
      ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "CMOS Transistor Sizing & Supply Controls",
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
          )

          // Wp/Lp Slider
          Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("PMOS Aspect Ratio (Wp/Lp)", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
              Text("${String.format("%.1f", cmosSpec.wpLpRatio)}", style = MaterialTheme.typography.labelSmall, color = ConductorGold, fontFamily = FontFamily.Monospace)
            }
            Slider(
              value = cmosSpec.wpLpRatio,
              onValueChange = { viewModel.updateWpLpRatio(it) },
              valueRange = 1.0f..10.0f,
              modifier = Modifier.testTag("wp_lp_slider")
            )
          }

          // Wn/Ln Slider
          Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("NMOS Aspect Ratio (Wn/Ln)", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
              Text("${String.format("%.1f", cmosSpec.wnLnRatio)}", style = MaterialTheme.typography.labelSmall, color = NeonCyan, fontFamily = FontFamily.Monospace)
            }
            Slider(
              value = cmosSpec.wnLnRatio,
              onValueChange = { viewModel.updateWnLnRatio(it) },
              valueRange = 1.0f..6.0f,
              modifier = Modifier.testTag("wn_ln_slider")
            )
          }

          // VDD Slider
          Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("Supply Voltage (VDD)", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
              Text("${String.format("%.2f", cmosSpec.vdd)} V", style = MaterialTheme.typography.labelSmall, color = LogicHighGreen, fontFamily = FontFamily.Monospace)
            }
            Slider(
              value = cmosSpec.vdd,
              onValueChange = { viewModel.updateVdd(it) },
              valueRange = 0.8f..1.8f,
              modifier = Modifier.testTag("vdd_slider")
            )
          }

          // Beta ratio indicator
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(SiliconCardDark, RoundedCornerShape(8.dp))
              .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Beta Ratio (βn/βp):", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text(
              text = "${String.format("%.2f", vtcAnalysis.betaRatio)} ${if (kotlin.math.abs(vtcAnalysis.betaRatio - 1f) < 0.2f) "(Symmetric)" else ""}",
              style = MaterialTheme.typography.labelSmall,
              color = if (kotlin.math.abs(vtcAnalysis.betaRatio - 1f) < 0.2f) LogicHighGreen else CleanroomAmber,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold
            )
          }

          Button(
            onClick = {
              viewModel.addLabNotebookEntry(
                title = "CMOS Inverter VTC Sweep",
                domain = DomainType.ANALOG,
                values = "VDD=${cmosSpec.vdd}V, Wp/Lp=${cmosSpec.wpLpRatio}, Wn/Ln=${cmosSpec.wnLnRatio}, VM=${String.format("%.3f", vtcAnalysis.vthM)}V, NML=${String.format("%.2f", vtcAnalysis.nmL)}V, NMH=${String.format("%.2f", vtcAnalysis.nmH)}V",
                remarks = "Verified noise margin sensitivity to PMOS/NMOS aspect ratio."
              )
            },
            colors = ButtonDefaults.buttonColors(containerColor = SiliconCardElevated),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(Icons.Default.BookmarkAdd, contentDescription = null, tint = ConductorGold, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Record VTC to Lab Notebook", color = TextPrimary)
          }
        }
      }
    }
  }
}

// ====================================================================
// TAB 3: WAFER FABRICATION & CLEANROOM SAFETY
// ====================================================================
@Composable
private fun FabricationLabTab(viewModel: SiliconLabViewModel) {
  val fabSteps by viewModel.fabSteps.collectAsStateWithLifecycle()
  val currentIndex by viewModel.currentFabStepIndex.collectAsStateWithLifecycle()
  val activeStep = fabSteps.getOrNull(currentIndex) ?: fabSteps.first()

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Cleanroom Chamber Status Header
    item {
      Surface(
        color = SiliconSurfaceDark,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder)
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Box(
                modifier = Modifier
                  .size(10.dp)
                  .background(LogicHighGreen, RoundedCornerShape(5.dp))
              )
              Text("Chamber Atmosphere: Ultra-High Vacuum", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
            }
            Text("Step ${activeStep.stepNumber} of 8", style = MaterialTheme.typography.labelSmall, color = ConductorGold, fontWeight = FontWeight.Bold)
          }

          Spacer(modifier = Modifier.height(8.dp))
          LinearProgressIndicator(
            progress = { (currentIndex + 1) / 8f },
            modifier = Modifier.fillMaxWidth(),
            color = NeonCyan,
            trackColor = SiliconCardDark
          )
        }
      }
    }

    // Step Details Card
    item {
      Surface(
        color = SiliconSurfaceDark,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder)
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = activeStep.name,
            style = MaterialTheme.typography.titleMedium,
            color = ConductorGold,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = activeStep.description,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
          )

          Spacer(modifier = Modifier.height(4.dp))

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FabMetricBadge(title = "Chamber Temp", value = "${activeStep.chamberTempCelsius.toInt()}°C", color = CleanroomAmber, modifier = Modifier.weight(1f))
            FabMetricBadge(title = "Chemical / Gas", value = activeStep.chemicalOrGas, color = NeonCyan, modifier = Modifier.weight(1f))
            FabMetricBadge(title = "Layer Thickness", value = "${activeStep.thicknessAngstroms.toInt()} Å", color = LogicHighGreen, modifier = Modifier.weight(1f))
          }

          Spacer(modifier = Modifier.height(4.dp))

          // Safety Alert
          Surface(
            color = SiliconErrorRed.copy(alpha = 0.12f),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SiliconErrorRed.copy(alpha = 0.5f))
          ) {
            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Icon(Icons.Default.Warning, contentDescription = null, tint = SiliconErrorRed, modifier = Modifier.size(18.dp))
              Column {
                Text(text = "Hazard: ${activeStep.hazardType}", style = MaterialTheme.typography.labelSmall, color = SiliconErrorRed, fontWeight = FontWeight.Bold)
                Text(text = "Safety Check: ${activeStep.safetyCheck}", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 11.sp)
              }
            }
          }
        }
      }
    }

    // 2D Cross Section Wafer Visualization Canvas
    item {
      Surface(
        color = SiliconSurfaceDark,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder),
        modifier = Modifier.padding(top = 4.dp)
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text(text = "Cross-Section Micrograph (Layer Deposition)", style = MaterialTheme.typography.titleSmall, color = NeonCyan, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(8.dp))

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(130.dp)
              .background(SiliconDark, RoundedCornerShape(6.dp))
              .border(1.dp, SiliconBorder, RoundedCornerShape(6.dp))
          ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
              val w = size.width
              val h = size.height

              // Base Silicon substrate
              drawRect(
                color = SiliconSubstrateColor,
                topLeft = Offset(0f, h * 0.65f),
                size = Size(w, h * 0.35f)
              )

              // Step 2+: Oxide layer
              if (currentIndex >= 1) {
                drawRect(
                  color = SiliconOxideColor,
                  topLeft = Offset(0f, h * 0.50f),
                  size = Size(w, h * 0.15f)
                )
              }

              // Step 3: Photoresist pattern
              if (currentIndex == 2) {
                drawRect(
                  color = PhotoresistColor,
                  topLeft = Offset(w * 0.1f, h * 0.35f),
                  size = Size(w * 0.35f, h * 0.15f)
                )
                drawRect(
                  color = PhotoresistColor,
                  topLeft = Offset(w * 0.55f, h * 0.35f),
                  size = Size(w * 0.35f, h * 0.15f)
                )
              }

              // Step 5: Well doping
              if (currentIndex >= 4) {
                drawRect(
                  color = Color(0xFF6366F1),
                  topLeft = Offset(w * 0.15f, h * 0.65f),
                  size = Size(w * 0.25f, h * 0.20f)
                )
                drawRect(
                  color = Color(0xFF10B981),
                  topLeft = Offset(w * 0.60f, h * 0.65f),
                  size = Size(w * 0.25f, h * 0.20f)
                )
              }

              // Step 6+: Poly gate
              if (currentIndex >= 5) {
                drawRect(
                  color = PolysiliconGateColor,
                  topLeft = Offset(w * 0.40f, h * 0.42f),
                  size = Size(w * 0.20f, h * 0.08f)
                )
              }

              // Step 7+: Copper Metallization
              if (currentIndex >= 6) {
                drawRect(
                  color = Metal1CopperColor,
                  topLeft = Offset(w * 0.18f, h * 0.25f),
                  size = Size(w * 0.12f, h * 0.25f)
                )
                drawRect(
                  color = Metal1CopperColor,
                  topLeft = Offset(w * 0.70f, h * 0.25f),
                  size = Size(w * 0.12f, h * 0.25f)
                )
              }

              // Step 8: Passivation layer
              if (currentIndex >= 7) {
                drawRect(
                  color = SiliconNitrideColor.copy(alpha = 0.5f),
                  topLeft = Offset(0f, h * 0.12f),
                  size = Size(w, h * 0.13f)
                )
              }
            }
          }
        }
      }
    }

    // Action Buttons
    item {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(
          onClick = { viewModel.advanceFabStep() },
          enabled = currentIndex < 7,
          colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
          modifier = Modifier
            .weight(1f)
            .testTag("advance_fab_step_button")
        ) {
          Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(if (currentIndex < 7) "Next Process Step" else "Wafer Complete", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
          onClick = { viewModel.resetFabSteps() },
          colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
          border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder),
          modifier = Modifier.testTag("reset_fab_steps_button")
        ) {
          Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Reset")
        }
      }
    }
  }
}

@Composable
private fun FabMetricBadge(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .background(SiliconCardDark, RoundedCornerShape(8.dp))
      .padding(6.dp)
  ) {
    Column {
      Text(text = title, style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 9.sp)
      Text(
        text = value,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        maxLines = 1
      )
    }
  }
}

// ====================================================================
// TAB 4: PHYSICAL DESIGN (FLOORPLAN & DRC)
// ====================================================================
@Composable
private fun PhysicalDesignLabTab(viewModel: SiliconLabViewModel) {
  val floorplanState by viewModel.floorplanState.collectAsStateWithLifecycle()
  val coreUtilPercent by viewModel.coreUtilPercent.collectAsStateWithLifecycle()
  val powerStripeCount by viewModel.powerStripeCount.collectAsStateWithLifecycle()

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      FloorplanCanvas(state = floorplanState)
    }

    // Floorplanning Sliders
    item {
      Surface(
        color = SiliconSurfaceDark,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder)
      ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(text = "Die Architecture & Power Grid Controls", style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)

          // Core Utilization
          Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("Target Core Utilization", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
              Text(
                text = "${coreUtilPercent.toInt()}% ${if (coreUtilPercent > 82f) "(Congestion Hazard)" else "(Optimal)"}",
                style = MaterialTheme.typography.labelSmall,
                color = if (coreUtilPercent > 82f) SiliconErrorRed else LogicHighGreen,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
              )
            }
            Slider(
              value = coreUtilPercent,
              onValueChange = { viewModel.updateCoreUtilization(it) },
              valueRange = 50f..92f,
              modifier = Modifier.testTag("core_util_slider")
            )
          }

          // Power Grid Stripes
          Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("Power Mesh Stripes (VDD/VSS)", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
              Text("$powerStripeCount Stripes", style = MaterialTheme.typography.labelSmall, color = ConductorGold, fontFamily = FontFamily.Monospace)
            }
            Slider(
              value = powerStripeCount.toFloat(),
              onValueChange = { viewModel.updatePowerStripeCount(it.toInt()) },
              valueRange = 2f..12f,
              steps = 9,
              modifier = Modifier.testTag("power_stripes_slider")
            )
          }

          // Macro Relocations
          Text(text = "Relocate Hard Macros", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
              onClick = { viewModel.moveMacro("sram_l2", 0.08f, 0.08f) },
              colors = ButtonDefaults.buttonColors(containerColor = SiliconCardElevated),
              modifier = Modifier.weight(1f)
            ) {
              Text("Corner Lock SRAM", fontSize = 11.sp, color = NeonCyan)
            }
            Button(
              onClick = { viewModel.moveMacro("sram_l2", 0.20f, 0.50f) },
              colors = ButtonDefaults.buttonColors(containerColor = SiliconCardElevated),
              modifier = Modifier.weight(1f)
            ) {
              Text("Center SRAM (DRC)", fontSize = 11.sp, color = CleanroomAmber)
            }
          }
        }
      }
    }
  }
}

// ====================================================================
// TAB 5: SILICON TEST & DFT VALIDATION
// ====================================================================
@Composable
private fun TestValidationLabTab(viewModel: SiliconLabViewModel) {
  val schmooDefect by viewModel.schmooDefect.collectAsStateWithLifecycle()
  val schmooPoints by viewModel.schmooPoints.collectAsStateWithLifecycle()
  val waferDies by viewModel.waferDies.collectAsStateWithLifecycle()

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      SchmooPlotCanvas(
        points = schmooPoints,
        hasDefect = schmooDefect,
        onToggleDefect = { viewModel.toggleSchmooDefect(it) }
      )
    }

    item {
      WaferMapCanvas(dies = waferDies)
    }

    // ATE DFT Summary Card
    item {
      Surface(
        color = SiliconSurfaceDark,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder)
      ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(text = "ATE Automated Test Equipment Bench Summary", style = MaterialTheme.typography.titleSmall, color = NeonCyan, fontWeight = FontWeight.Bold)

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
              modifier = Modifier
                .weight(1f)
                .background(SiliconCardDark, RoundedCornerShape(8.dp))
                .padding(8.dp)
            ) {
              Column {
                Text(text = "ATPG SAF Coverage", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Text(text = "98.4%", style = MaterialTheme.typography.titleSmall, color = LogicHighGreen, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
              }
            }

            Box(
              modifier = Modifier
                .weight(1f)
                .background(SiliconCardDark, RoundedCornerShape(8.dp))
                .padding(8.dp)
            ) {
              Column {
                Text(text = "Scan Chains", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Text(text = "64 Chains x 1.8k FF", style = MaterialTheme.typography.titleSmall, color = ConductorGold, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
              }
            }
          }

          Button(
            onClick = { viewModel.retestWaferYield() },
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
            modifier = Modifier.fillMaxWidth().testTag("retest_wafer_button")
          ) {
            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Re-Probe Silicon Wafer Lot", color = Color.Black, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
