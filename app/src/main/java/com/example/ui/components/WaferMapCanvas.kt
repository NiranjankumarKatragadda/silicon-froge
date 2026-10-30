package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WaferDie
import com.example.ui.theme.*

@Composable
fun WaferMapCanvas(
  dies: List<WaferDie>,
  modifier: Modifier = Modifier
) {
  var selectedDie by remember { mutableStateOf<WaferDie?>(null) }
  val totalDies = dies.size
  val passingDies = dies.count { it.binNumber == 1 }
  val yieldPercent = if (totalDies > 0) (passingDies.toFloat() / totalDies) * 100f else 0f

  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(SiliconSurfaceDark, RoundedCornerShape(12.dp))
      .border(1.dp, SiliconBorder, RoundedCornerShape(12.dp))
      .padding(12.dp)
      .testTag("wafer_map_canvas_container")
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "300mm Silicon Wafer Sort Map",
          style = MaterialTheme.typography.titleSmall,
          color = NeonCyan,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Production Lot Yield: ${String.format("%.1f", yieldPercent)}% ($passingDies/$totalDies)",
          style = MaterialTheme.typography.labelMedium,
          color = if (yieldPercent >= 88f) LogicHighGreen else CleanroomAmber
        )
      }

      // Legend
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        LegendDot(color = LogicHighGreen, label = "Bin 1 (Pass)")
        LegendDot(color = CleanroomAmber, label = "Bin 2 (Iddq)")
        LegendDot(color = SiliconErrorRed, label = "Bin 4 (Fail)")
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(230.dp)
        .background(SiliconDark, RoundedCornerShape(8.dp))
        .border(1.dp, SiliconBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
        .pointerInput(dies) {
          detectTapGestures { offset ->
            val w = size.width
            val h = size.height
            val center = Offset(w / 2f, h / 2f)
            val waferRadius = (minOf(w, h) / 2f) * 0.9f
            val gridRadius = 5
            val dieSize = (waferRadius * 1.8f) / (gridRadius * 2 + 1)
            val startX = center.x - waferRadius * 0.9f
            val startY = center.y - waferRadius * 0.9f

            // Find clicked die
            for (die in dies) {
              val dx = startX + die.x * dieSize
              val dy = startY + die.y * dieSize
              if (offset.x in dx..(dx + dieSize) && offset.y in dy..(dy + dieSize)) {
                selectedDie = die
                break
              }
            }
          }
        }
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val waferRadius = (minOf(size.width, size.height) / 2f) * 0.88f

        // Draw wafer bevel outline
        drawCircle(
          color = SiliconSubstrateColor,
          radius = waferRadius,
          center = center
        )
        drawCircle(
          color = NeonCyan.copy(alpha = 0.5f),
          radius = waferRadius,
          center = center,
          style = Stroke(width = 2.5f)
        )

        // Draw wafer notch indicator at bottom
        drawLine(
          color = NeonCyan,
          start = Offset(center.x - 12f, center.y + waferRadius - 2f),
          end = Offset(center.x + 12f, center.y + waferRadius - 2f),
          strokeWidth = 3f
        )

        val gridRadius = 5
        val dieSize = (waferRadius * 1.8f) / (gridRadius * 2 + 1)
        val startX = center.x - waferRadius * 0.9f
        val startY = center.y - waferRadius * 0.9f

        for (die in dies) {
          val dx = startX + die.x * dieSize
          val dy = startY + die.y * dieSize
          val isSelected = selectedDie?.id == die.id

          val dieColor = when (die.binNumber) {
            1 -> LogicHighGreen
            2 -> CleanroomAmber
            3 -> NeonCyanSubtle
            else -> SiliconErrorRed
          }

          drawRect(
            color = dieColor.copy(alpha = if (isSelected) 1.0f else 0.85f),
            topLeft = Offset(dx + 1f, dy + 1f),
            size = Size(dieSize - 2f, dieSize - 2f)
          )

          if (isSelected) {
            drawRect(
              color = Color.White,
              topLeft = Offset(dx, dy),
              size = Size(dieSize, dieSize),
              style = Stroke(width = 2.5f)
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Die inspector panel
    if (selectedDie != null) {
      val d = selectedDie!!
      val binTitle = when (d.binNumber) {
        1 -> "Bin 1: Prime Good Die"
        2 -> "Bin 2: Elevated Iddq Leakage"
        3 -> "Bin 3: Timing Margin Degraded"
        else -> "Bin 4: Hard Logic Defect"
      }
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(SiliconCardDark, RoundedCornerShape(8.dp))
          .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Die #${d.id} (Grid [${d.x}, ${d.y}])",
            style = MaterialTheme.typography.labelMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = binTitle,
            style = MaterialTheme.typography.labelSmall,
            color = if (d.binNumber == 1) LogicHighGreen else CleanroomAmber
          )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
          Column(horizontalAlignment = Alignment.End) {
            Text(text = "Fmax", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text(
              text = "${String.format("%.2f", d.fmaxGhz)} GHz",
              style = MaterialTheme.typography.labelSmall,
              color = NeonCyan,
              fontFamily = FontFamily.Monospace
            )
          }
          Column(horizontalAlignment = Alignment.End) {
            Text(text = "Iddq", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text(
              text = "${String.format("%.0f", d.iddqUa)} µA",
              style = MaterialTheme.typography.labelSmall,
              color = if (d.iddqUa < 50f) LogicHighGreen else SiliconErrorRed,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }
    } else {
      Text(
        text = "Tap on any silicon die to inspect parametric ATE measurements",
        style = MaterialTheme.typography.labelSmall,
        color = TextSecondary
      )
    }
  }
}

@Composable
private fun LegendDot(color: Color, label: String) {
  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
    Box(
      modifier = Modifier
        .size(8.dp)
        .background(color, RoundedCornerShape(2.dp))
    )
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = TextSecondary,
      fontSize = 10.sp
    )
  }
}
