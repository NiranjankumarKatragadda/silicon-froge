package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LogicGateType
import com.example.ui.theme.*

@Composable
fun LogicGateWorkbenchView(
  modifier: Modifier = Modifier
) {
  var selectedGate by remember { mutableStateOf(LogicGateType.NAND) }
  var inputStates by remember { mutableStateOf(listOf(false, false, false)) }
  var loadCapacitanceFf by remember { mutableStateOf(15f) }
  var supplyVdd by remember { mutableStateOf(1.20f) }

  // Adjust input count when gate changes
  val activeInputs = remember(selectedGate, inputStates) {
    inputStates.take(selectedGate.inputCount)
  }
  val outputs = remember(selectedGate, activeInputs) {
    selectedGate.evaluate(activeInputs)
  }
  val truthTable = remember(selectedGate) {
    selectedGate.generateTruthTable()
  }

  // Electrical metrics
  val tpdPs = remember(selectedGate, loadCapacitanceFf, supplyVdd) {
    selectedGate.baseDelayPs + (loadCapacitanceFf * 2.8f / supplyVdd)
  }
  val dynPowerUw = remember(loadCapacitanceFf, supplyVdd) {
    loadCapacitanceFf * 1e-3f * supplyVdd * supplyVdd * 1.5f * 1000f // at 1.5GHz
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
            .background(NeonCyan.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.Memory, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
        }
        Column {
          Text(
            text = "Interactive Logic Gate Workbench",
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Live Boolean evaluation, schematic rendering & truth table",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            fontSize = 11.sp
          )
        }
      }

      Box(
        modifier = Modifier
          .background(ConductorGold.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
          .padding(horizontal = 6.dp, vertical = 2.dp)
      ) {
        Text(
          text = selectedGate.symbol,
          color = ConductorGold,
          style = MaterialTheme.typography.labelSmall,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold
        )
      }
    }

    // Gate Type Carousel
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      items(LogicGateType.values()) { gate ->
        val isSelected = gate == selectedGate
        Surface(
          color = if (isSelected) NeonCyan else SiliconCardDark,
          shape = RoundedCornerShape(8.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) NeonCyan else SiliconBorder),
          modifier = Modifier.clickable {
            selectedGate = gate
            inputStates = listOf(false, false, false)
          }.testTag("gate_select_${gate.name.lowercase()}")
        ) {
          Text(
            text = gate.displayName,
            color = if (isSelected) Color.Black else TextPrimary,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
          )
        }
      }
    }

    // Schematic Canvas Display
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(170.dp)
        .background(Color(0xFF040812), RoundedCornerShape(10.dp))
        .border(1.dp, SiliconBorder.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val gateCenterX = w * 0.50f
        val gateCenterY = h * 0.50f
        val gateW = 90f
        val gateH = 80f

        // Draw Inputs leads & wires
        val inCount = selectedGate.inputCount
        for (i in 0 until inCount) {
          val inY = when (inCount) {
            1 -> gateCenterY
            2 -> gateCenterY - gateH * 0.28f + (i * gateH * 0.56f)
            else -> gateCenterY - gateH * 0.35f + (i * gateH * 0.35f)
          }
          val isHigh = activeInputs.getOrNull(i) ?: false
          val wireColor = if (isHigh) NeonCyan else Color(0xFF334155)

          // Wire
          drawLine(
            color = wireColor,
            start = Offset(w * 0.15f, inY),
            end = Offset(gateCenterX - gateW * 0.5f, inY),
            strokeWidth = 4f
          )

          // Input Terminal Dot
          drawCircle(
            color = if (isHigh) LogicHighGreen else ConductorGold,
            radius = 6f,
            center = Offset(w * 0.15f, inY)
          )
        }

        // Draw Output leads & wires
        val outKeys = outputs.keys.toList()
        for ((idx, key) in outKeys.withIndex()) {
          val outY = if (outKeys.size == 1) gateCenterY else gateCenterY - 24f + (idx * 48f)
          val isHigh = outputs[key] ?: false
          val wireColor = if (isHigh) NeonCyan else Color(0xFF334155)

          val hasBubble = selectedGate in listOf(LogicGateType.NOT, LogicGateType.NAND, LogicGateType.NOR, LogicGateType.XNOR)
          val startLeadX = gateCenterX + gateW * 0.5f + (if (hasBubble) 14f else 0f)

          drawLine(
            color = wireColor,
            start = Offset(startLeadX, outY),
            end = Offset(w * 0.85f, outY),
            strokeWidth = 4f
          )

          // Output Terminal Dot
          drawCircle(
            color = if (isHigh) LogicHighGreen else SiliconErrorRed,
            radius = 6f,
            center = Offset(w * 0.85f, outY)
          )
        }

        // Draw Logic Gate Body
        val gatePath = Path()
        val left = gateCenterX - gateW * 0.5f
        val top = gateCenterY - gateH * 0.5f
        val right = gateCenterX + gateW * 0.5f
        val bottom = gateCenterY + gateH * 0.5f

        when (selectedGate) {
          LogicGateType.AND, LogicGateType.NAND -> {
            gatePath.moveTo(left, top)
            gatePath.lineTo(left + gateW * 0.4f, top)
            gatePath.cubicTo(right, top, right, bottom, left + gateW * 0.4f, bottom)
            gatePath.lineTo(left, bottom)
            gatePath.close()
          }
          LogicGateType.OR, LogicGateType.NOR -> {
            gatePath.moveTo(left, top)
            gatePath.cubicTo(left + 20f, gateCenterY, left + 20f, gateCenterY, left, bottom)
            gatePath.cubicTo(left + gateW * 0.5f, bottom, right, gateCenterY, right, gateCenterY)
            gatePath.cubicTo(right, gateCenterY, left + gateW * 0.5f, top, left, top)
            gatePath.close()
          }
          LogicGateType.XOR, LogicGateType.XNOR -> {
            // Back arc
            val backArc = Path()
            backArc.moveTo(left - 12f, top)
            backArc.cubicTo(left + 8f, gateCenterY, left + 8f, gateCenterY, left - 12f, bottom)
            drawPath(backArc, color = ConductorGold, style = Stroke(width = 3f))

            gatePath.moveTo(left, top)
            gatePath.cubicTo(left + 20f, gateCenterY, left + 20f, gateCenterY, left, bottom)
            gatePath.cubicTo(left + gateW * 0.5f, bottom, right, gateCenterY, right, gateCenterY)
            gatePath.cubicTo(right, gateCenterY, left + gateW * 0.5f, top, left, top)
            gatePath.close()
          }
          LogicGateType.NOT, LogicGateType.BUFFER -> {
            gatePath.moveTo(left, top)
            gatePath.lineTo(right, gateCenterY)
            gatePath.lineTo(left, bottom)
            gatePath.close()
          }
          else -> {
            // Block representation for complex/arithmetic cells
            drawRect(
              color = SiliconCardDark,
              topLeft = Offset(left, top),
              size = Size(gateW, gateH)
            )
            drawRect(
              color = NeonCyan,
              topLeft = Offset(left, top),
              size = Size(gateW, gateH),
              style = Stroke(width = 2.5f)
            )
          }
        }

        if (selectedGate !in listOf(LogicGateType.MUX21, LogicGateType.HALF_ADDER, LogicGateType.FULL_ADDER, LogicGateType.D_LATCH)) {
          drawPath(gatePath, color = SiliconCardElevated)
          drawPath(gatePath, color = ConductorGold, style = Stroke(width = 2.5f))
        }

        // Inversion bubble for NAND, NOR, NOT, XNOR
        if (selectedGate in listOf(LogicGateType.NOT, LogicGateType.NAND, LogicGateType.NOR, LogicGateType.XNOR)) {
          drawCircle(
            color = SiliconDark,
            radius = 6f,
            center = Offset(right + 7f, gateCenterY)
          )
          drawCircle(
            color = ConductorGold,
            radius = 6f,
            center = Offset(right + 7f, gateCenterY),
            style = Stroke(width = 2f)
          )
        }
      }

      // Input labels on the left
      Column(
        modifier = Modifier
          .fillMaxHeight()
          .padding(start = 12.dp),
        verticalArrangement = Arrangement.SpaceAround
      ) {
        val inCount = selectedGate.inputCount
        for (i in 0 until inCount) {
          val inName = when (selectedGate) {
            LogicGateType.NOT, LogicGateType.BUFFER -> "A"
            LogicGateType.MUX21 -> if (i == 0) "D0" else if (i == 1) "D1" else "SEL"
            LogicGateType.FULL_ADDER -> if (i == 0) "A" else if (i == 1) "B" else "Cin"
            LogicGateType.D_LATCH -> if (i == 0) "D" else "EN"
            else -> if (i == 0) "A" else "B"
          }
          val isHigh = activeInputs.getOrNull(i) ?: false
          Text(
            text = "$inName = ${if (isHigh) '1' else '0'}",
            color = if (isHigh) NeonCyan else TextSecondary,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
        }
      }

      // Output labels on the right
      Column(
        modifier = Modifier
          .fillMaxHeight()
          .align(Alignment.CenterEnd)
          .padding(end = 12.dp),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.End
      ) {
        outputs.forEach { (outPin, outVal) ->
          Text(
            text = "$outPin = ${if (outVal) '1' else '0'}",
            color = if (outVal) LogicHighGreen else SiliconErrorRed,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
        }
      }
    }

    // Interactive Input Toggles
    Surface(
      color = SiliconCardDark,
      shape = RoundedCornerShape(10.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder)
    ) {
      Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "Interactive Logic Inputs (Tap to Toggle State)",
          style = MaterialTheme.typography.labelSmall,
          color = TextSecondary
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          for (i in 0 until selectedGate.inputCount) {
            val inName = when (selectedGate) {
              LogicGateType.NOT, LogicGateType.BUFFER -> "Input A"
              LogicGateType.MUX21 -> if (i == 0) "Data D0" else if (i == 1) "Data D1" else "Select S"
              LogicGateType.FULL_ADDER -> if (i == 0) "Input A" else if (i == 1) "Input B" else "Carry In"
              LogicGateType.D_LATCH -> if (i == 0) "Data D" else "Enable EN"
              else -> if (i == 0) "Input A" else "Input B"
            }
            val isHigh = activeInputs.getOrNull(i) ?: false

            Button(
              onClick = {
                val updated = inputStates.toMutableList()
                updated[i] = !updated[i]
                inputStates = updated
              },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (isHigh) NeonCyan else SiliconCardElevated
              ),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f).testTag("toggle_input_$i")
            ) {
              Text(
                text = "$inName: ${if (isHigh) "HIGH (1)" else "LOW (0)"}",
                color = if (isHigh) Color.Black else TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }

    // Live Truth Table Matrix
    Surface(
      color = SiliconSurfaceDark,
      shape = RoundedCornerShape(10.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder)
    ) {
      Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
          text = "Truth Table Matrix (${selectedGate.displayName})",
          style = MaterialTheme.typography.titleSmall,
          color = ConductorGold,
          fontWeight = FontWeight.Bold
        )

        truthTable.forEachIndexed { rowIdx, row ->
          val isActiveRow = row.inputValues == activeInputs

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(
                if (isActiveRow) NeonCyan.copy(alpha = 0.20f) else Color.Transparent,
                RoundedCornerShape(4.dp)
              )
              .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              if (isActiveRow) {
                Text(text = "▶", color = NeonCyan, fontSize = 11.sp)
              }
              Text(
                text = row.inputLabels.joinToString(", "),
                color = if (isActiveRow) NeonCyan else TextSecondary,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = if (isActiveRow) FontWeight.Bold else FontWeight.Normal
              )
            }

            Text(
              text = row.outputLabels.joinToString(", "),
              color = if (isActiveRow) LogicHighGreen else ConductorGold,
              style = MaterialTheme.typography.labelSmall,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }

    // Telemetry Cards: Delay & Dynamic Power
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      Box(
        modifier = Modifier
          .weight(1f)
          .background(SiliconCardDark, RoundedCornerShape(8.dp))
          .padding(8.dp)
      ) {
        Column {
          Text(text = "Cell Delay (tpd)", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
          Text(
            text = "${String.format("%.1f", tpdPs)} ps",
            style = MaterialTheme.typography.titleSmall,
            color = ConductorGold,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
        }
      }

      Box(
        modifier = Modifier
          .weight(1f)
          .background(SiliconCardDark, RoundedCornerShape(8.dp))
          .padding(8.dp)
      ) {
        Column {
          Text(text = "Dynamic Power", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
          Text(
            text = "${String.format("%.2f", dynPowerUw)} µW",
            style = MaterialTheme.typography.titleSmall,
            color = LogicHighGreen,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
        }
      }

      Box(
        modifier = Modifier
          .weight(1f)
          .background(SiliconCardDark, RoundedCornerShape(8.dp))
          .padding(8.dp)
      ) {
        Column {
          Text(text = "Output Load", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
          Text(
            text = "${loadCapacitanceFf.toInt()} fF",
            style = MaterialTheme.typography.titleSmall,
            color = NeonCyan,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
        }
      }
    }
  }
}
