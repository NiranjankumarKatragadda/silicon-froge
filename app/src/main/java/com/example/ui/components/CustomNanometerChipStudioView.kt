package com.example.ui.components

import androidx.compose.foundation.background
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
import com.example.data.CustomSiliconRepository
import com.example.data.local.CustomNanometerChipEntity
import com.example.model.TechnologyNode
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun CustomNanometerChipStudioView(
  repository: CustomSiliconRepository,
  modifier: Modifier = Modifier
) {
  val coroutineScope = rememberCoroutineScope()
  val savedChips by repository.customChips.collectAsState(initial = emptyList())

  var activeSubTab by remember { mutableStateOf(0) } // 0: Chip Designer & Physical Canvas, 1: Saved Portfolio

  // Student configurable parameters
  var selectedNode by remember { mutableStateOf(TechnologyNode.NODE_28NM) }
  var chipNameInput by remember { mutableStateOf("SiliconCore-Alpha") }
  var codenameInput by remember { mutableStateOf("SCA-28") }
  var dieWidthMm by remember { mutableStateOf(6.5) }
  var dieHeightMm by remember { mutableStateOf(5.8) }
  var targetFreqGhz by remember { mutableStateOf(2.2) }
  var metalLayers by remember { mutableStateOf(selectedNode.defaultMetalLayers) }
  var targetApp by remember { mutableStateOf("Edge AI Co-Processor") }

  var showSaveSuccessDialog by remember { mutableStateOf(false) }

  // Recalculate metrics in real-time
  val metrics = remember(selectedNode, dieWidthMm, dieHeightMm, targetFreqGhz, metalLayers) {
    repository.calculateChipMetrics(
      node = selectedNode,
      dieWidthMm = dieWidthMm,
      dieHeightMm = dieHeightMm,
      frequencyGhz = targetFreqGhz,
      metalLayers = metalLayers
    )
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(SiliconDark)
  ) {
    // Mode Navigation Sub-Tabs
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(SiliconSurfaceDark)
        .padding(horizontal = 12.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      val tabs = listOf("PDK & Chip Designer", "My Chip Portfolio (${savedChips.size})")
      tabs.forEachIndexed { idx, label ->
        val isSel = activeSubTab == idx
        Surface(
          color = if (isSel) NeonCyan else SiliconCardDark,
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier
            .clickable { activeSubTab = idx }
            .testTag("nanometer_tab_$idx")
        ) {
          Text(
            text = label,
            color = if (isSel) Color.Black else TextSecondary,
            fontSize = 12.sp,
            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
          )
        }
      }
    }

    when (activeSubTab) {
      0 -> {
        // CHIP DESIGNER WORKSPACE
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // 1. Nanometer Technology Node Selector
          item {
            Card(
              colors = CardDefaults.cardColors(containerColor = SiliconSurfaceDark),
              shape = RoundedCornerShape(10.dp)
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "SELECT NANOMETER PDK NODE",
                    color = NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                  )
                  Text(
                    text = "${selectedNode.nodeNm}nm",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                  )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  items(TechnologyNode.values()) { node ->
                    val isSel = node == selectedNode
                    Surface(
                      color = if (isSel) NeonCyan.copy(alpha = 0.2f) else SiliconCardDark,
                      shape = RoundedCornerShape(8.dp),
                      border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSel) NeonCyan else SiliconBorder
                      ),
                      modifier = Modifier
                        .clickable {
                          selectedNode = node
                          metalLayers = node.defaultMetalLayers
                          targetFreqGhz = (node.maxFrequencyGhz * 0.75).coerceIn(0.2, 5.0)
                        }
                        .testTag("node_chip_${node.nodeNm}")
                    ) {
                      Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Text(
                          text = "${node.nodeNm}nm",
                          color = if (isSel) NeonCyan else TextPrimary,
                          fontSize = 13.sp,
                          fontWeight = FontWeight.Bold
                        )
                        Text(
                          text = node.transistorArchitecture.take(14) + "...",
                          color = TextSecondary,
                          fontSize = 9.sp
                        )
                      }
                    }
                  }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                  text = "Architecture: ${selectedNode.transistorArchitecture} • VDD: ${selectedNode.nominalVdd}V • Tox: ${selectedNode.oxideThicknessNm}nm • Wafer Cost: $${selectedNode.waferCostUsd}",
                  color = TextSecondary,
                  fontSize = 10.sp,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
          }

          // 2. Interactive Die Floorplan & Power Grid Canvas
          item {
            CustomDieCanvas(
              node = selectedNode,
              dieWidthMm = dieWidthMm,
              dieHeightMm = dieHeightMm,
              drcViolations = metrics.drcViolationsCount
            )
          }

          // 3. Physical Dimensions & Clock Frequency Controls
          item {
            Card(
              colors = CardDefaults.cardColors(containerColor = SiliconSurfaceDark),
              shape = RoundedCornerShape(10.dp)
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Text(
                  text = "CUSTOM CHIP GEOMETRY & CLOCK TARGETS",
                  color = NeonCyan,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Die Width Slider
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text("Die Width", color = TextPrimary, fontSize = 12.sp)
                  Text(String.format("%.1f mm", dieWidthMm), color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                Slider(
                  value = dieWidthMm.toFloat(),
                  onValueChange = { dieWidthMm = String.format("%.1f", it).toDouble() },
                  valueRange = 2.0f..20.0f,
                  colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
                )

                // Die Height Slider
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text("Die Height", color = TextPrimary, fontSize = 12.sp)
                  Text(String.format("%.1f mm", dieHeightMm), color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                Slider(
                  value = dieHeightMm.toFloat(),
                  onValueChange = { dieHeightMm = String.format("%.1f", it).toDouble() },
                  valueRange = 2.0f..20.0f,
                  colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
                )

                // Target Clock Frequency Slider
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text("Target Clock Frequency", color = TextPrimary, fontSize = 12.sp)
                  Text(String.format("%.2f GHz", targetFreqGhz), color = Color(0xFFFFB300), fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                Slider(
                  value = targetFreqGhz.toFloat(),
                  onValueChange = { targetFreqGhz = String.format("%.2f", it).toDouble() },
                  valueRange = 0.2f..selectedNode.maxFrequencyGhz.toFloat(),
                  colors = SliderDefaults.colors(thumbColor = Color(0xFFFFB300), activeTrackColor = Color(0xFFFFB300))
                )

                // Metal Layers Stepper
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text("Interconnect Metal Layers (M1-M${metalLayers})", color = TextPrimary, fontSize = 12.sp)
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                      onClick = { if (metalLayers > 4) metalLayers-- },
                      modifier = Modifier.size(28.dp)
                    ) {
                      Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = TextSecondary)
                    }
                    Text(
                      text = "$metalLayers Layers",
                      color = NeonGreen,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold,
                      fontFamily = FontFamily.Monospace,
                      modifier = Modifier.padding(horizontal = 6.dp)
                    )
                    IconButton(
                      onClick = { if (metalLayers < 15) metalLayers++ },
                      modifier = Modifier.size(28.dp)
                    ) {
                      Icon(Icons.Default.Add, contentDescription = "Increase", tint = TextSecondary)
                    }
                  }
                }
              }
            }
          }

          // 4. Live Silicon Tapeout Telemetry Dashboard
          item {
            Card(
              colors = CardDefaults.cardColors(containerColor = SiliconSurfaceDark),
              shape = RoundedCornerShape(10.dp)
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Text(
                  text = "TAPEOUT & SILICON METRICS",
                  color = NeonCyan,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  TelemetryPill(
                    label = "TRANSISTOR COUNT",
                    value = if (metrics.totalTransistorsMillion >= 1000) {
                      String.format("%.2f B", metrics.totalTransistorsMillion / 1000.0)
                    } else {
                      "${metrics.totalTransistorsMillion} MTr"
                    },
                    color = NeonCyan,
                    modifier = Modifier.weight(1f)
                  )
                  TelemetryPill(
                    label = "DIE AREA",
                    value = "${metrics.dieAreaMm2} mm²",
                    color = NeonGreen,
                    modifier = Modifier.weight(1f)
                  )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  TelemetryPill(
                    label = "TOTAL TDP POWER",
                    value = "${metrics.totalTdpWatts} W",
                    color = Color(0xFFFFB300),
                    modifier = Modifier.weight(1f)
                  )
                  TelemetryPill(
                    label = "JUNCTION TEMP",
                    value = "${metrics.estimatedJunctionTempC} °C",
                    color = if (metrics.estimatedJunctionTempC > 85.0) Color.Red else NeonGreen,
                    modifier = Modifier.weight(1f)
                  )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  TelemetryPill(
                    label = "WAFER YIELD (300mm)",
                    value = "${metrics.waferYieldPercent}% (${metrics.goodDiesPerWafer} Good Dies)",
                    color = NeonGreen,
                    modifier = Modifier.weight(1f)
                  )
                  TelemetryPill(
                    label = "EST. DIE COST",
                    value = "$${metrics.estimatedCostPerGoodDieUsd}",
                    color = Color(0xFF00E5FF),
                    modifier = Modifier.weight(1f)
                  )
                }
              }
            }
          }

          // 5. Tapeout Approval & Save to Portfolio
          item {
            Card(
              colors = CardDefaults.cardColors(containerColor = SiliconSurfaceDark),
              shape = RoundedCornerShape(10.dp)
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Text(
                  text = "SAVE CHIP TO STUDENT PORTFOLIO",
                  color = NeonCyan,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                  value = chipNameInput,
                  onValueChange = { chipNameInput = it },
                  label = { Text("Chip Project Name") },
                  modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                  value = codenameInput,
                  onValueChange = { codenameInput = it },
                  label = { Text("Silicon Codename / Revision") },
                  modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                  onClick = {
                    coroutineScope.launch {
                      val newChip = CustomNanometerChipEntity(
                        id = "chip_" + System.currentTimeMillis(),
                        chipName = chipNameInput,
                        codename = codenameInput,
                        technologyNodeNm = selectedNode.nodeNm,
                        dieWidthMm = dieWidthMm,
                        dieHeightMm = dieHeightMm,
                        supplyVoltageV = selectedNode.nominalVdd,
                        targetClockGhz = targetFreqGhz,
                        metalLayersCount = metalLayers,
                        targetApplication = targetApp,
                        totalTransistorsMillion = metrics.totalTransistorsMillion,
                        totalTdpWatts = metrics.totalTdpWatts,
                        dieAreaMm2 = metrics.dieAreaMm2,
                        diesPerWafer = metrics.diesPerWafer,
                        waferYieldPercent = metrics.waferYieldPercent,
                        drcStatus = if (metrics.drcViolationsCount == 0) "PASS" else "WARNING",
                        lvsStatus = metrics.lvsStatus,
                        isTapeoutApproved = metrics.drcViolationsCount == 0
                      )
                      repository.saveCustomChip(newChip)
                      showSaveSuccessDialog = true
                    }
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_chip_button")
                ) {
                  Icon(Icons.Default.Verified, contentDescription = null, tint = Color.Black)
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "Approve Tapeout & Save Chip",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                  )
                }
              }
            }
          }
        }
      }

      1 -> {
        // SAVED CHIPS PORTFOLIO VIEW
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          if (savedChips.isEmpty()) {
            item {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(40.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "No custom chips saved yet.\nDesign a chip and tap 'Approve Tapeout & Save Chip' to build your portfolio.",
                  color = TextSecondary,
                  fontSize = 12.sp,
                  textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
              }
            }
          } else {
            items(savedChips) { chip ->
              Card(
                colors = CardDefaults.cardColors(containerColor = SiliconSurfaceDark),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(12.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                        text = chip.chipName,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                      )
                      Text(
                        text = "${chip.technologyNodeNm}nm • Codename: ${chip.codename}",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                      )
                    }

                    Row {
                      IconButton(
                        onClick = {
                          selectedNode = TechnologyNode.fromNodeNm(chip.technologyNodeNm)
                          dieWidthMm = chip.dieWidthMm
                          dieHeightMm = chip.dieHeightMm
                          targetFreqGhz = chip.targetClockGhz
                          metalLayers = chip.metalLayersCount
                          chipNameInput = chip.chipName
                          codenameInput = chip.codename
                          activeSubTab = 0
                        }
                      ) {
                        Icon(Icons.Default.Tune, contentDescription = "Load Config", tint = NeonCyan)
                      }
                      IconButton(
                        onClick = {
                          coroutineScope.launch { repository.deleteCustomChip(chip) }
                        }
                      ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f))
                      }
                    }
                  }

                  Text(
                    text = "App: ${chip.targetApplication} • Clock: ${chip.targetClockGhz} GHz • VDD: ${chip.supplyVoltageV}V",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                  )

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    Text(
                      text = "${chip.dieAreaMm2} mm²",
                      color = NeonGreen,
                      fontSize = 10.sp,
                      fontFamily = FontFamily.Monospace
                    )
                    Text("•", color = TextSecondary, fontSize = 10.sp)
                    Text(
                      text = "${chip.totalTdpWatts} W TDP",
                      color = Color(0xFFFFB300),
                      fontSize = 10.sp,
                      fontFamily = FontFamily.Monospace
                    )
                    Text("•", color = TextSecondary, fontSize = 10.sp)
                    Text(
                      text = "Yield: ${chip.waferYieldPercent}%",
                      color = NeonCyan,
                      fontSize = 10.sp,
                      fontFamily = FontFamily.Monospace
                    )
                    Text("•", color = TextSecondary, fontSize = 10.sp)
                    Text(
                      text = "DRC: ${chip.drcStatus}",
                      color = if (chip.drcStatus == "PASS") NeonGreen else Color.Red,
                      fontSize = 10.sp,
                      fontFamily = FontFamily.Monospace
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  if (showSaveSuccessDialog) {
    AlertDialog(
      onDismissRequest = { showSaveSuccessDialog = false },
      title = { Text("Tapeout Spec Saved!", color = NeonCyan) },
      text = {
        Text(
          text = "Your custom ${selectedNode.nodeNm}nm chip '${chipNameInput}' has been verified and registered in your persistent Room database portfolio.",
          color = TextPrimary,
          fontSize = 13.sp
        )
      },
      confirmButton = {
        Button(
          onClick = { showSaveSuccessDialog = false },
          colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
        ) {
          Text("Continue", color = Color.Black, fontWeight = FontWeight.Bold)
        }
      },
      containerColor = SiliconSurfaceDark
    )
  }
}

@Composable
private fun TelemetryPill(
  label: String,
  value: String,
  color: Color,
  modifier: Modifier = Modifier
) {
  Surface(
    color = SiliconDark,
    shape = RoundedCornerShape(6.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f)),
    modifier = modifier
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
      Text(
        text = label,
        color = TextSecondary,
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
      )
      Text(
        text = value,
        color = color,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
      )
    }
  }
}
