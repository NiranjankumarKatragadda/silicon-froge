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
import com.example.model.DomainType
import com.example.ui.components.OscilloscopeCanvas
import com.example.ui.theme.*
import com.example.viewmodel.SiliconLabViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun VirtualBenchScreen(
  viewModel: SiliconLabViewModel,
  modifier: Modifier = Modifier
) {
  var selectedSubTab by remember { mutableStateOf(0) } // 0=Oscilloscope & PMU, 1=Cleanroom Safety, 2=Lab Notebook

  val oscState by viewModel.oscilloscopeState.collectAsStateWithLifecycle()
  val pmuState by viewModel.pmuState.collectAsStateWithLifecycle()
  val safetyState by viewModel.safetyState.collectAsStateWithLifecycle()
  val labLogs by viewModel.labLogs.collectAsStateWithLifecycle()

  var showNewLogDialog by remember { mutableStateOf(false) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(SiliconDark)
  ) {
    // Sub-tab Navigation
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(SiliconSurfaceDark)
        .padding(horizontal = 16.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      TabPill(
        title = "Scope & PMU",
        isSelected = selectedSubTab == 0,
        onClick = { selectedSubTab = 0 },
        modifier = Modifier.weight(1f).testTag("bench_subtab_scope")
      )
      TabPill(
        title = "Cleanroom Safety",
        isSelected = selectedSubTab == 1,
        onClick = { selectedSubTab = 1 },
        modifier = Modifier.weight(1f).testTag("bench_subtab_safety")
      )
      TabPill(
        title = "Lab Log (${labLogs.size})",
        isSelected = selectedSubTab == 2,
        onClick = { selectedSubTab = 2 },
        modifier = Modifier.weight(1f).testTag("bench_subtab_logs")
      )
    }

    when (selectedSubTab) {
      0 -> OscilloscopeAndPmuTab(
        oscState = oscState,
        pmuState = pmuState,
        onToggleRun = { viewModel.toggleOscilloscopeRunning() },
        onSetTimebase = { viewModel.setOscilloscopeTimebase(it) },
        onSetVoltsDiv = { viewModel.setOscilloscopeVoltsPerDiv(it) },
        onSetSource = { viewModel.setWaveformSource(it) }
      )
      1 -> CleanroomSafetyTab(
        safety = safetyState,
        onToggleEsd = { viewModel.toggleEsdStrap() },
        onToggleSilane = { viewModel.toggleSilaneScrubber() },
        onToggleHf = { viewModel.toggleHfSpillKit() }
      )
      2 -> LabNotebookTab(
        logs = labLogs,
        onOpenAddLog = { showNewLogDialog = true }
      )
    }
  }

  if (showNewLogDialog) {
    NewLogDialog(
      onDismiss = { showNewLogDialog = false },
      onSave = { title, domain, vals, remarks ->
        viewModel.addLabNotebookEntry(title, domain, vals, remarks)
        showNewLogDialog = false
      }
    )
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
// SUB-TAB 0: SCOPE & PMU
// ====================================================================
@Composable
private fun OscilloscopeAndPmuTab(
  oscState: com.example.model.OscilloscopeState,
  pmuState: com.example.model.ParametricMeasurementUnit,
  onToggleRun: () -> Unit,
  onSetTimebase: (Float) -> Unit,
  onSetVoltsDiv: (Float) -> Unit,
  onSetSource: (String) -> Unit
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      OscilloscopeCanvas(state = oscState)
    }

    // Oscilloscope Controls
    item {
      Surface(
        color = SiliconSurfaceDark,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder)
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(text = "Digital Scope Knobs & Triggering", style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
            Button(
              onClick = onToggleRun,
              colors = ButtonDefaults.buttonColors(containerColor = if (oscState.isRunning) CleanroomAmber else LogicHighGreen),
              shape = RoundedCornerShape(8.dp),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
              modifier = Modifier.testTag("oscilloscope_run_toggle")
            ) {
              Text(if (oscState.isRunning) "STOP" else "RUN", color = Color.Black, fontWeight = FontWeight.Bold)
            }
          }

          // Source Picker
          Text(text = "Waveform Signal Probe Source:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            val sources = listOf("CLOCK_GEN" to "Clock Generator", "CMOS_INVERTER" to "CMOS Inverter", "RING_OSC" to "Ring Oscillator")
            sources.forEach { (srcKey, label) ->
              val isSel = oscState.waveformSource == srcKey
              Surface(
                color = if (isSel) NeonCyan.copy(alpha = 0.2f) else SiliconCardDark,
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) NeonCyan else SiliconBorder),
                modifier = Modifier.clickable { onSetSource(srcKey) }
              ) {
                Text(
                  text = label,
                  color = if (isSel) NeonCyan else TextPrimary,
                  style = MaterialTheme.typography.labelSmall,
                  fontSize = 11.sp,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )
              }
            }
          }

          // Timebase Knobs
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
              onClick = { onSetTimebase(0.5f) },
              colors = ButtonDefaults.buttonColors(containerColor = if (oscState.timeBaseMs == 0.5f) NeonCyan else SiliconCardElevated),
              modifier = Modifier.weight(1f)
            ) {
              Text("0.5 ms/div", color = if (oscState.timeBaseMs == 0.5f) Color.Black else TextPrimary, fontSize = 11.sp)
            }
            Button(
              onClick = { onSetTimebase(1.0f) },
              colors = ButtonDefaults.buttonColors(containerColor = if (oscState.timeBaseMs == 1.0f) NeonCyan else SiliconCardElevated),
              modifier = Modifier.weight(1f)
            ) {
              Text("1.0 ms/div", color = if (oscState.timeBaseMs == 1.0f) Color.Black else TextPrimary, fontSize = 11.sp)
            }
            Button(
              onClick = { onSetTimebase(2.0f) },
              colors = ButtonDefaults.buttonColors(containerColor = if (oscState.timeBaseMs == 2.0f) NeonCyan else SiliconCardElevated),
              modifier = Modifier.weight(1f)
            ) {
              Text("2.0 ms/div", color = if (oscState.timeBaseMs == 2.0f) Color.Black else TextPrimary, fontSize = 11.sp)
            }
          }
        }
      }
    }

    // PMU (Parametric Measurement Unit) Card
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
            Text(text = "Parametric Measurement Unit (PMU)", style = MaterialTheme.typography.titleSmall, color = ConductorGold, fontWeight = FontWeight.Bold)
            Box(
              modifier = Modifier
                .background(LogicHighGreen.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(text = "FORCE V / MEASURE I", style = MaterialTheme.typography.labelSmall, color = LogicHighGreen, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
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
                Text(text = "Forced Voltage", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Text(text = "${pmuState.forceVoltage} V", style = MaterialTheme.typography.titleSmall, color = NeonCyan, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
              }
            }
            Box(
              modifier = Modifier
                .weight(1f)
                .background(SiliconCardDark, RoundedCornerShape(8.dp))
                .padding(8.dp)
            ) {
              Column {
                Text(text = "Measured Leakage", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Text(text = "${pmuState.measuredCurrentUa} µA", style = MaterialTheme.typography.titleSmall, color = LogicHighGreen, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
              }
            }
            Box(
              modifier = Modifier
                .weight(1f)
                .background(SiliconCardDark, RoundedCornerShape(8.dp))
                .padding(8.dp)
            ) {
              Column {
                Text(text = "Current Clamp", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Text(text = "${pmuState.complianceCurrentMa} mA", style = MaterialTheme.typography.titleSmall, color = CleanroomAmber, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
              }
            }
          }
        }
      }
    }
  }
}

// ====================================================================
// SUB-TAB 1: CLEANROOM SAFETY
// ====================================================================
@Composable
private fun CleanroomSafetyTab(
  safety: com.example.model.CleanroomSafetyState,
  onToggleEsd: () -> Unit,
  onToggleSilane: () -> Unit,
  onToggleHf: () -> Unit
) {
  val isAllSafe = safety.esdStrapGroundOk && safety.silaneScrubberActive && safety.hfAcidSpillKitVerified

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Surface(
        color = if (isAllSafe) LogicHighGreen.copy(alpha = 0.15f) else SiliconErrorRed.copy(alpha = 0.15f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isAllSafe) LogicHighGreen else SiliconErrorRed)
      ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          Icon(
            imageVector = if (isAllSafe) Icons.Default.Shield else Icons.Default.Warning,
            contentDescription = null,
            tint = if (isAllSafe) LogicHighGreen else SiliconErrorRed,
            modifier = Modifier.size(28.dp)
          )
          Column {
            Text(
              text = if (isAllSafe) "CLEANROOM SAFETY STATUS: VERIFIED" else "HAZARD INTERLOCK TRIPPED!",
              style = MaterialTheme.typography.titleSmall,
              color = if (isAllSafe) LogicHighGreen else SiliconErrorRed,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = if (isAllSafe) "ISO Class 3 environment certified. Hazardous gases abated. ESD grounding verified." else "Immediate risk of gate oxide electrostatic punch-through or toxic gas accumulation.",
              style = MaterialTheme.typography.bodySmall,
              color = TextPrimary
            )
          }
        }
      }
    }

    item {
      Surface(
        color = SiliconSurfaceDark,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder)
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(text = "Safety Protocols & Interlock Checklist", style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)

          SafetySwitchRow(
            title = "ESD Wrist Strap & Bench Mat Grounding",
            description = "Prevents sub-100V dielectric breakdown of 3nm gate oxide during wafer handling.",
            isChecked = safety.esdStrapGroundOk,
            onToggle = onToggleEsd
          )

          SafetySwitchRow(
            title = "Silane (SiH4) Pyrophoric Gas Scrubber & Burn Box",
            description = "Neutralizes spontaneous combustion hazards during LPCVD polysilicon deposition.",
            isChecked = safety.silaneScrubberActive,
            onToggle = onToggleSilane
          )

          SafetySwitchRow(
            title = "Hydrofluoric Acid (HF) Spill Kit & Calcium Gluconate",
            description = "Immediate antidote ready for accidental contact during wafer wet oxide etching.",
            isChecked = safety.hfAcidSpillKitVerified,
            onToggle = onToggleHf
          )
        }
      }
    }

    item {
      Surface(
        color = SiliconCardDark,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder)
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(text = "Air Purity Monitoring: ISO Class 3", style = MaterialTheme.typography.titleSmall, color = ConductorGold, fontWeight = FontWeight.Bold)
          Text(text = "Particle Count (≥0.5µm): ${safety.bunnySuitParticlePpm} particles / m³ (Spec limit: < 35 particles / m³)", style = MaterialTheme.typography.labelSmall, color = LogicHighGreen, fontFamily = FontFamily.Monospace)
          Text(text = "Laminar flow HEPA filtration operating at 99.999% retention. Full bunny suit, face mask, and nitrile cleanroom gloves mandatory.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
      }
    }
  }
}

@Composable
private fun SafetySwitchRow(
  title: String,
  description: String,
  isChecked: Boolean,
  onToggle: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(SiliconCardDark, RoundedCornerShape(8.dp))
      .padding(10.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(text = title, style = MaterialTheme.typography.labelMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
      Text(text = description, style = MaterialTheme.typography.bodySmall, color = TextSecondary, fontSize = 11.sp)
    }
    Switch(
      checked = isChecked,
      onCheckedChange = { onToggle() },
      modifier = Modifier.testTag("safety_switch_${title.take(6).lowercase()}")
    )
  }
}

// ====================================================================
// SUB-TAB 2: LAB NOTEBOOK
// ====================================================================
@Composable
private fun LabNotebookTab(
  logs: List<com.example.model.LabLogEntry>,
  onOpenAddLog: () -> Unit
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(text = "Student Lab Notebook & History", style = MaterialTheme.typography.titleMedium, color = NeonCyan, fontWeight = FontWeight.Bold)
          Text(text = "Recorded parameters and verification runs", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
        Button(
          onClick = onOpenAddLog,
          colors = ButtonDefaults.buttonColors(containerColor = ConductorGold),
          shape = RoundedCornerShape(8.dp),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
          modifier = Modifier.testTag("new_lab_log_button")
        ) {
          Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("New Log", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
      }
    }

    items(logs) { entry ->
      val dateStr = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(entry.timestamp))
      Surface(
        color = SiliconSurfaceDark,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder)
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .background(ConductorGold.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(text = entry.domain.title, style = MaterialTheme.typography.labelSmall, color = ConductorGold, fontSize = 10.sp)
            }
            Text(text = dateStr, style = MaterialTheme.typography.labelSmall, color = TextTertiary, fontSize = 10.sp)
          }

          Text(text = entry.experimentTitle, style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)

          Surface(
            color = Color(0xFF060B12),
            shape = RoundedCornerShape(6.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder.copy(alpha = 0.5f))
          ) {
            Text(
              text = entry.recordedValues,
              color = LogicHighGreen,
              fontFamily = FontFamily.Monospace,
              style = MaterialTheme.typography.labelSmall,
              modifier = Modifier.padding(8.dp)
            )
          }

          Text(text = "Observations: ${entry.remarks}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
      }
    }
  }
}

@Composable
private fun NewLogDialog(
  onDismiss: () -> Unit,
  onSave: (String, DomainType, String, String) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var values by remember { mutableStateOf("") }
  var remarks by remember { mutableStateOf("") }
  var selectedDomain by remember { mutableStateOf(DomainType.DIGITAL) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Record Lab Experiment", color = TextPrimary, fontWeight = FontWeight.Bold) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Experiment Title") },
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = values,
          onValueChange = { values = it },
          label = { Text("Measured Parameters (e.g. VDD=1.2V, VM=0.6V)") },
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = remarks,
          onValueChange = { remarks = it },
          label = { Text("Lab Observations / Conclusions") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (title.isNotBlank()) {
            onSave(title, selectedDomain, values, remarks)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
      ) {
        Text("Save Entry", color = Color.Black, fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = TextSecondary)
      }
    },
    containerColor = SiliconSurfaceDark
  )
}
