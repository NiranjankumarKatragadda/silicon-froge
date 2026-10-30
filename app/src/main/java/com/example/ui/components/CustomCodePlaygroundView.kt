package com.example.ui.components

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
import com.example.data.CustomSiliconRepository
import com.example.data.RtlAnalysisResult
import com.example.data.local.StudentCustomCodeEntity
import com.example.model.TechnologyNode
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun CustomCodePlaygroundView(
  repository: CustomSiliconRepository,
  modifier: Modifier = Modifier
) {
  val coroutineScope = rememberCoroutineScope()
  val savedCodes by repository.customCodes.collectAsState(initial = emptyList())

  var activeSubTab by remember { mutableStateOf(0) } // 0: Editor & Synthesize, 1: Live Stimulus, 2: Saved Projects
  var selectedTemplateIndex by remember { mutableStateOf(0) }

  val templates = repository.starterTemplates
  var codeText by remember { mutableStateOf(templates[0].defaultVerilog) }
  var currentTitle by remember { mutableStateOf(templates[0].title) }
  var selectedNodeNm by remember { mutableStateOf(28) }

  // Simulation test vectors
  var inputA by remember { mutableStateOf(5) }
  var inputB by remember { mutableStateOf(3) }
  var aluSel by remember { mutableStateOf(0) }
  var clkState by remember { mutableStateOf(1) }
  var rstState by remember { mutableStateOf(1) }
  var pedSensorState by remember { mutableStateOf(0) }

  var showSaveDialog by remember { mutableStateOf(false) }
  var saveTitleInput by remember { mutableStateOf(currentTitle) }
  var saveDescInput by remember { mutableStateOf("Custom student hardware RTL implementation") }

  // Computed RTL synthesis result
  val analysisResult = remember(codeText, selectedNodeNm) {
    repository.analyzeStudentRtl(codeText, selectedNodeNm)
  }

  // Live simulation output
  val simOutputs = remember(analysisResult.moduleName, inputA, inputB, aluSel, clkState, rstState, pedSensorState) {
    repository.simulateTestVector(
      analysisResult.moduleName,
      mapOf(
        "A" to inputA,
        "B" to inputB,
        "ALU_Sel" to aluSel,
        "clk" to clkState,
        "rst_n" to rstState,
        "ped_sensor" to pedSensorState
      )
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
      val tabs = listOf("RTL Editor", "Live Stimulus", "Saved (${savedCodes.size})")
      tabs.forEachIndexed { idx, label ->
        val isSel = activeSubTab == idx
        Surface(
          color = if (isSel) NeonCyan else SiliconCardDark,
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier
            .clickable { activeSubTab = idx }
            .testTag("code_tab_$idx")
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
        // CODE EDITOR & SYNTHESIS VIEW
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // Starter Templates Row
          item {
            Column {
              Text(
                text = "STARTER HDL TEMPLATES",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
              Spacer(modifier = Modifier.height(6.dp))
              LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(templates.indices.toList()) { idx ->
                  val t = templates[idx]
                  val isSel = selectedTemplateIndex == idx
                  Surface(
                    color = if (isSel) NeonCyan.copy(alpha = 0.2f) else SiliconCardDark,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(
                      1.dp,
                      if (isSel) NeonCyan else SiliconBorder
                    ),
                    modifier = Modifier
                      .clickable {
                        selectedTemplateIndex = idx
                        codeText = t.defaultVerilog
                        currentTitle = t.title
                        saveTitleInput = t.title
                      }
                      .testTag("template_chip_$idx")
                  ) {
                    Text(
                      text = t.title,
                      color = if (isSel) NeonCyan else TextPrimary,
                      fontSize = 12.sp,
                      fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                  }
                }
              }
            }
          }

          // Technology Node Selection
          item {
            Card(
              colors = CardDefaults.cardColors(containerColor = SiliconSurfaceDark),
              shape = RoundedCornerShape(10.dp)
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "TARGET PDK NODE",
                    color = NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                  )
                  Text(
                    text = "${selectedNodeNm}nm Technology",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  val nodes = listOf(180, 65, 28, 16, 7, 3, 2)
                  items(nodes) { nodeNm ->
                    val isSel = selectedNodeNm == nodeNm
                    Surface(
                      color = if (isSel) NeonCyan else SiliconDark,
                      shape = RoundedCornerShape(6.dp),
                      modifier = Modifier.clickable { selectedNodeNm = nodeNm }
                    ) {
                      Text(
                        text = "${nodeNm}nm",
                        color = if (isSel) Color.Black else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                      )
                    }
                  }
                }
              }
            }
          }

          // Code Editor Area
          item {
            Card(
              colors = CardDefaults.cardColors(containerColor = SiliconSurfaceDark),
              shape = RoundedCornerShape(10.dp)
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      Icons.Default.Code,
                      contentDescription = null,
                      tint = NeonCyan,
                      modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = "STUDENT VERILOG / HDL CODE",
                      color = NeonCyan,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      fontFamily = FontFamily.Monospace
                    )
                  }
                  Button(
                    onClick = { showSaveDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("save_code_button")
                  ) {
                    Icon(Icons.Default.Save, contentDescription = "Save", tint = Color.Black, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                  }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Editable Code Box
                OutlinedTextField(
                  value = codeText,
                  onValueChange = { codeText = it },
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .testTag("verilog_code_editor"),
                  textStyle = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    color = NeonGreen,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                  ),
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = SiliconBorder,
                    focusedContainerColor = Color(0xFF090D14),
                    unfocusedContainerColor = Color(0xFF090D14)
                  )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Verilog Snippets
                Text(
                  text = "QUICK INSERT SYNTAX:",
                  color = TextSecondary,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  val snippets = listOf(
                    "assign" to "assign out = a & b;\n",
                    "always" to "always @(posedge clk) begin\nend\n",
                    "case" to "case(sel)\n  default: ;\nendcase\n",
                    "wire" to "wire [3:0] net;\n"
                  )
                  items(snippets) { (label, code) ->
                    Surface(
                      color = SiliconCardDark,
                      shape = RoundedCornerShape(4.dp),
                      modifier = Modifier.clickable { codeText += code }
                    ) {
                      Text(
                        text = "+ $label",
                        color = NeonCyan,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                      )
                    }
                  }
                }
              }
            }
          }

          // Live Synthesis Metrics Card
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
                    text = "RTL SYNTHESIS TELEMETRY (${selectedNodeNm}nm)",
                    color = NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                  )
                  Text(
                    text = "Module: ${analysisResult.moduleName}",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                  )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  MetricPill(
                    label = "EQUIV GATES",
                    value = "${analysisResult.gateCount} Gates",
                    color = NeonCyan,
                    modifier = Modifier.weight(1f)
                  )
                  MetricPill(
                    label = "CELL AREA",
                    value = "${analysisResult.areaUm2} µm²",
                    color = NeonGreen,
                    modifier = Modifier.weight(1f)
                  )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  MetricPill(
                    label = "PROP DELAY",
                    value = "${analysisResult.delayPs} ps",
                    color = Color(0xFFFFB300),
                    modifier = Modifier.weight(1f)
                  )
                  MetricPill(
                    label = "DYNAMIC POWER",
                    value = "${analysisResult.powerUw} µW",
                    color = Color(0xFFFF4081),
                    modifier = Modifier.weight(1f)
                  )
                }
              }
            }
          }
        }
      }

      1 -> {
        // LIVE STIMULUS & TESTBENCH VIEW
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          item {
            Card(
              colors = CardDefaults.cardColors(containerColor = SiliconSurfaceDark),
              shape = RoundedCornerShape(10.dp)
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Text(
                  text = "INPUT STIMULUS GENERATOR",
                  color = NeonCyan,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
                Text(
                  text = "Drive test vectors into module: ${analysisResult.moduleName}",
                  color = TextSecondary,
                  fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (analysisResult.moduleName.contains("alu", ignoreCase = true)) {
                  // ALU Inputs
                  Text("Input A [3:0]: $inputA (0b${inputA.toString(2).padStart(4, '0')})", color = TextPrimary, fontSize = 12.sp)
                  Slider(
                    value = inputA.toFloat(),
                    onValueChange = { inputA = it.toInt() },
                    valueRange = 0f..15f,
                    steps = 14,
                    colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
                  )

                  Text("Input B [3:0]: $inputB (0b${inputB.toString(2).padStart(4, '0')})", color = TextPrimary, fontSize = 12.sp)
                  Slider(
                    value = inputB.toFloat(),
                    onValueChange = { inputB = it.toInt() },
                    valueRange = 0f..15f,
                    steps = 14,
                    colors = SliderDefaults.colors(thumbColor = NeonGreen, activeTrackColor = NeonGreen)
                  )

                  Text("Opcode ALU_Sel [2:0]: $aluSel (${
                    when (aluSel) {
                      0 -> "ADD"
                      1 -> "SUB"
                      2 -> "AND"
                      3 -> "OR"
                      4 -> "XOR"
                      5 -> "SLT"
                      else -> "NOP"
                    }
                  })", color = Color(0xFFFFB300), fontSize = 12.sp)
                  Slider(
                    value = aluSel.toFloat(),
                    onValueChange = { aluSel = it.toInt() },
                    valueRange = 0f..5f,
                    steps = 4,
                    colors = SliderDefaults.colors(thumbColor = Color(0xFFFFB300), activeTrackColor = Color(0xFFFFB300))
                  )
                } else if (analysisResult.moduleName.contains("fsm", ignoreCase = true) || analysisResult.moduleName.contains("traffic", ignoreCase = true)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text("Pedestrian Sensor", color = TextPrimary, fontSize = 12.sp)
                    Switch(
                      checked = pedSensorState == 1,
                      onCheckedChange = { pedSensorState = if (it) 1 else 0 },
                      colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan)
                    )
                  }
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text("Reset Active Low (rst_n)", color = TextPrimary, fontSize = 12.sp)
                    Switch(
                      checked = rstState == 1,
                      onCheckedChange = { rstState = if (it) 1 else 0 },
                      colors = SwitchDefaults.colors(checkedThumbColor = NeonGreen)
                    )
                  }
                } else {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text("Clock Pin (clk)", color = TextPrimary, fontSize = 12.sp)
                    Button(
                      onClick = { clkState = if (clkState == 1) 0 else 1 },
                      colors = ButtonDefaults.buttonColors(containerColor = if (clkState == 1) NeonCyan else SiliconBorder)
                    ) {
                      Text(if (clkState == 1) "CLK: HIGH (1)" else "CLK: LOW (0)", color = if (clkState == 1) Color.Black else TextPrimary)
                    }
                  }
                }
              }
            }
          }

          // Live Simulation Outputs Card
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
                    text = "SIMULATION OUTPUT STATE",
                    color = NeonGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                  )
                  Text(
                    text = "Active Vectors: OK",
                    color = NeonGreen,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                  )
                }

                Spacer(modifier = Modifier.height(10.dp))

                simOutputs.forEach { (pin, value) ->
                  Surface(
                    color = SiliconDark,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(vertical = 3.dp)
                  ) {
                    Row(
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Text(
                        text = pin,
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                      )
                      Text(
                        text = value.toString(),
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }

      2 -> {
        // SAVED STUDENT PROJECTS VIEW
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          if (savedCodes.isEmpty()) {
            item {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(40.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "No saved student code designs yet.\nWrite RTL and tap 'Save' to add to your library.",
                  color = TextSecondary,
                  fontSize = 12.sp,
                  textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
              }
            }
          } else {
            items(savedCodes) { item ->
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
                        text = item.title,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                      )
                      Text(
                        text = "Module: ${item.moduleName} • ${item.targetNodeNm}nm PDK",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                      )
                    }

                    Row {
                      IconButton(
                        onClick = {
                          codeText = item.verilogCode
                          currentTitle = item.title
                          selectedNodeNm = item.targetNodeNm
                          activeSubTab = 0
                        }
                      ) {
                        Icon(Icons.Default.Edit, contentDescription = "Load", tint = NeonCyan)
                      }
                      IconButton(
                        onClick = {
                          coroutineScope.launch { repository.deleteCustomCode(item) }
                        }
                      ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f))
                      }
                    }
                  }

                  Text(
                    text = item.description,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                  )

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    Text(
                      text = "${item.estimatedGateCount} Gates",
                      color = NeonGreen,
                      fontSize = 10.sp,
                      fontFamily = FontFamily.Monospace
                    )
                    Text("•", color = TextSecondary, fontSize = 10.sp)
                    Text(
                      text = "${item.estimatedAreaUm2} µm²",
                      color = TextSecondary,
                      fontSize = 10.sp,
                      fontFamily = FontFamily.Monospace
                    )
                    Text("•", color = TextSecondary, fontSize = 10.sp)
                    Text(
                      text = "${item.estimatedPowerUw} µW",
                      color = Color(0xFFFFB300),
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

  // Save Code Dialog
  if (showSaveDialog) {
    AlertDialog(
      onDismissRequest = { showSaveDialog = false },
      title = { Text("Save Student RTL Project", color = TextPrimary) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = saveTitleInput,
            onValueChange = { saveTitleInput = it },
            label = { Text("Design Title") },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = saveDescInput,
            onValueChange = { saveDescInput = it },
            label = { Text("Description & Notes") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            coroutineScope.launch {
              val newCode = StudentCustomCodeEntity(
                id = "custom_" + System.currentTimeMillis(),
                title = saveTitleInput,
                description = saveDescInput,
                moduleName = analysisResult.moduleName,
                verilogCode = codeText,
                targetNodeNm = selectedNodeNm,
                inputsList = analysisResult.inputs.joinToString(", "),
                outputsList = analysisResult.outputs.joinToString(", "),
                estimatedGateCount = analysisResult.gateCount,
                estimatedAreaUm2 = analysisResult.areaUm2,
                estimatedDelayPs = analysisResult.delayPs,
                estimatedPowerUw = analysisResult.powerUw,
                isFavorite = false
              )
              repository.saveCustomCode(newCode)
              showSaveDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
        ) {
          Text("Save Design", color = Color.Black, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showSaveDialog = false }) {
          Text("Cancel", color = TextSecondary)
        }
      },
      containerColor = SiliconSurfaceDark
    )
  }
}

@Composable
private fun MetricPill(
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
