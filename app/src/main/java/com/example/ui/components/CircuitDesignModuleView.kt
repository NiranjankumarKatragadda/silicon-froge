package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.data.CircuitDesignRepository
import com.example.model.CircuitDesignModule
import com.example.ui.theme.*

@Composable
fun CircuitDesignModuleView(
  modifier: Modifier = Modifier
) {
  val modules = CircuitDesignRepository.circuitModules
  var selectedModule by remember { mutableStateOf(modules.first()) }
  var inputValues by remember { mutableStateOf(mapOf("A" to false, "B" to false, "Cin" to false, "D0" to false, "D1" to true, "S" to false, "D" to true, "EN" to true)) }
  var activeViewTab by remember { mutableStateOf(0) } // 0=Simulation & Nets, 1=Schematic ASCII, 2=Verilog Code

  // Simulate internal and output nets
  val simulatedNets = remember(selectedModule, inputValues) {
    selectedModule.simulate(inputValues)
  }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(SiliconSurfaceDark, RoundedCornerShape(14.dp))
      .border(1.dp, SiliconBorder, RoundedCornerShape(14.dp))
      .padding(14.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
          modifier = Modifier
            .size(32.dp)
            .background(ConductorGold.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.AccountTree, contentDescription = null, tint = ConductorGold, modifier = Modifier.size(18.dp))
        }
        Column {
          Text(
            text = "Circuit Design Modules",
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Multi-gate structural interconnects & netlist simulation",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            fontSize = 11.sp
          )
        }
      }

      Box(
        modifier = Modifier
          .background(LogicHighGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
          .padding(horizontal = 6.dp, vertical = 2.dp)
      ) {
        Text(
          text = selectedModule.category,
          color = LogicHighGreen,
          style = MaterialTheme.typography.labelSmall,
          fontSize = 10.sp
        )
      }
    }

    // Module Selector Chips
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      items(modules) { mod ->
        val isSelected = mod.id == selectedModule.id
        Surface(
          color = if (isSelected) ConductorGold else SiliconCardDark,
          shape = RoundedCornerShape(8.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) ConductorGold else SiliconBorder),
          modifier = Modifier.clickable {
            selectedModule = mod
          }.testTag("module_chip_${mod.id}")
        ) {
          Text(
            text = mod.name,
            color = if (isSelected) Color.Black else TextPrimary,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
          )
        }
      }
    }

    // View Selector Pills
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      val tabs = listOf("Simulation & Netlist", "Schematic Architecture", "Verilog RTL")
      tabs.forEachIndexed { idx, label ->
        val isSel = activeViewTab == idx
        Surface(
          color = if (isSel) NeonCyan.copy(alpha = 0.2f) else SiliconCardDark,
          shape = RoundedCornerShape(6.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) NeonCyan else SiliconBorder),
          modifier = Modifier
            .weight(1f)
            .clickable { activeViewTab = idx }
        ) {
          Box(modifier = Modifier.padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
            Text(
              text = label,
              color = if (isSel) NeonCyan else TextSecondary,
              style = MaterialTheme.typography.labelSmall,
              fontSize = 11.sp,
              fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
            )
          }
        }
      }
    }

    when (activeViewTab) {
      0 -> {
        // Primary Inputs Toggles
        Surface(
          color = SiliconCardDark,
          shape = RoundedCornerShape(10.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder)
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Primary Input Stimulus", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              selectedModule.primaryInputs.forEach { pin ->
                val isHigh = inputValues[pin] ?: false
                Button(
                  onClick = {
                    val updated = inputValues.toMutableMap()
                    updated[pin] = !isHigh
                    inputValues = updated
                  },
                  colors = ButtonDefaults.buttonColors(
                    containerColor = if (isHigh) NeonCyan else SiliconCardElevated
                  ),
                  shape = RoundedCornerShape(6.dp),
                  modifier = Modifier.weight(1f).testTag("module_input_$pin")
                ) {
                  Text(
                    text = "$pin: ${if (isHigh) "1" else "0"}",
                    color = if (isHigh) Color.Black else TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                  )
                }
              }
            }
          }
        }

        // Net States & Output Results
        Surface(
          color = SiliconCardDark,
          shape = RoundedCornerShape(10.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder)
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Internal Net States & Module Outputs", style = MaterialTheme.typography.labelSmall, color = ConductorGold, fontWeight = FontWeight.Bold)

            // Outputs Highlight
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              selectedModule.primaryOutputs.forEach { outPin ->
                val outVal = simulatedNets[outPin] ?: false
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .background(
                      if (outVal) LogicHighGreen.copy(alpha = 0.2f) else SiliconErrorRed.copy(alpha = 0.2f),
                      RoundedCornerShape(8.dp)
                    )
                    .border(1.dp, if (outVal) LogicHighGreen else SiliconErrorRed, RoundedCornerShape(8.dp))
                    .padding(8.dp)
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(text = "Output $outPin:", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                    Text(
                      text = if (outVal) "LOGIC 1" else "LOGIC 0",
                      color = if (outVal) LogicHighGreen else SiliconErrorRed,
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Bold,
                      fontFamily = FontFamily.Monospace
                    )
                  }
                }
              }
            }

            // Intermediate Internal Nets
            val internalNets = simulatedNets.filter { it.key !in selectedModule.primaryInputs && it.key !in selectedModule.primaryOutputs }
            if (internalNets.isNotEmpty()) {
              Spacer(modifier = Modifier.height(4.dp))
              Text(text = "Internal Interconnect Wires:", style = MaterialTheme.typography.labelSmall, color = TextTertiary, fontSize = 10.sp)
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                internalNets.forEach { (netName, netVal) ->
                  Surface(
                    color = Color(0xFF040812),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder),
                    modifier = Modifier.weight(1f)
                  ) {
                    Row(
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                      horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                      Text(text = netName, color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                      Text(
                        text = if (netVal) "1" else "0",
                        color = if (netVal) NeonCyan else Color(0xFF64748B),
                        fontWeight = FontWeight.Bold,
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

      1 -> {
        // Schematic ASCII
        Surface(
          color = Color(0xFF040812),
          shape = RoundedCornerShape(8.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder.copy(alpha = 0.6f))
        ) {
          Text(
            text = selectedModule.schematicAscii,
            color = ConductorGold,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            modifier = Modifier.padding(12.dp)
          )
        }
      }

      2 -> {
        // Verilog RTL
        Surface(
          color = Color(0xFF040812),
          shape = RoundedCornerShape(8.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder.copy(alpha = 0.6f))
        ) {
          Text(
            text = selectedModule.verilogImplementation,
            color = LogicHighGreen,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            modifier = Modifier.padding(12.dp)
          )
        }
      }
    }
  }
}
