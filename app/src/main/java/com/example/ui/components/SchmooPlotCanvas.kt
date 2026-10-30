package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
import com.example.model.SchmooPoint
import com.example.ui.theme.*

@Composable
fun SchmooPlotCanvas(
  points: List<SchmooPoint>,
  hasDefect: Boolean,
  onToggleDefect: (Boolean) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedPoint by remember { mutableStateOf<SchmooPoint?>(null) }

  val voltages = points.map { it.voltage }.distinct().sorted()
  val frequencies = points.map { it.freqGhz }.distinct().sortedDescending()

  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(SiliconSurfaceDark, RoundedCornerShape(12.dp))
      .border(1.dp, SiliconBorder, RoundedCornerShape(12.dp))
      .padding(12.dp)
      .testTag("schmoo_plot_container")
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "ATE Shmoo Plot (Freq vs VDD)",
          style = MaterialTheme.typography.titleSmall,
          color = NeonCyan,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = if (hasDefect) "Defect Mode: Bridging Resistance Injected" else "Nominal PVT Characterization",
          style = MaterialTheme.typography.labelSmall,
          color = if (hasDefect) SiliconErrorRed else LogicHighGreen
        )
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "Defect", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Spacer(modifier = Modifier.width(4.dp))
        Switch(
          checked = hasDefect,
          onCheckedChange = onToggleDefect,
          modifier = Modifier.testTag("shmoo_defect_toggle")
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(190.dp)
        .background(SiliconDark, RoundedCornerShape(8.dp))
        .border(1.dp, SiliconBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
        .pointerInput(points) {
          detectTapGestures { offset ->
            val w = size.width
            val h = size.height
            val leftMargin = 38f
            val bottomMargin = 26f
            val plotW = w - leftMargin - 12f
            val plotH = h - bottomMargin - 8f

            if (offset.x in leftMargin..(leftMargin + plotW) && offset.y in 8f..(8f + plotH)) {
              val colWidth = plotW / voltages.size
              val rowHeight = plotH / frequencies.size

              val col = ((offset.x - leftMargin) / colWidth).toInt().coerceIn(0, voltages.size - 1)
              val row = ((offset.y - 8f) / rowHeight).toInt().coerceIn(0, frequencies.size - 1)

              val v = voltages[col]
              val f = frequencies[row]
              selectedPoint = points.find { it.voltage == v && it.freqGhz == f }
            }
          }
        }
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val leftMargin = 38f
        val bottomMargin = 26f
        val plotW = size.width - leftMargin - 12f
        val plotH = size.height - bottomMargin - 8f

        if (voltages.isNotEmpty() && frequencies.isNotEmpty()) {
          val colWidth = plotW / voltages.size
          val rowHeight = plotH / frequencies.size

          for (pt in points) {
            val colIndex = voltages.indexOf(pt.voltage)
            val rowIndex = frequencies.indexOf(pt.freqGhz)
            if (colIndex >= 0 && rowIndex >= 0) {
              val px = leftMargin + colIndex * colWidth
              val py = 8f + rowIndex * rowHeight
              val isSelected = selectedPoint?.voltage == pt.voltage && selectedPoint?.freqGhz == pt.freqGhz

              val cellColor = if (pt.isPassing) LogicHighGreen else SiliconErrorRed.copy(alpha = 0.85f)
              drawRect(
                color = cellColor,
                topLeft = Offset(px + 1f, py + 1f),
                size = Size(colWidth - 2f, rowHeight - 2f)
              )

              if (isSelected) {
                drawRect(
                  color = Color.White,
                  topLeft = Offset(px, py),
                  size = Size(colWidth, rowHeight),
                  style = Stroke(width = 2.5f)
                )
              }
            }
          }

          // Draw Axis Separator
          drawLine(
            color = SiliconBorder,
            start = Offset(leftMargin, 8f),
            end = Offset(leftMargin, 8f + plotH),
            strokeWidth = 2f
          )
          drawLine(
            color = SiliconBorder,
            start = Offset(leftMargin, 8f + plotH),
            end = Offset(leftMargin + plotW, 8f + plotH),
            strokeWidth = 2f
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "X: VDD (0.7V → 1.2V) | Y: Clock Freq (0.8 → 3.6GHz)",
        style = MaterialTheme.typography.labelSmall,
        color = TextSecondary,
        fontSize = 11.sp
      )

      if (selectedPoint != null) {
        val p = selectedPoint!!
        Text(
          text = "${p.voltage}V @ ${p.freqGhz}GHz: ${if (p.isPassing) "PASS (Safe)" else "FAIL (Violation)"}",
          style = MaterialTheme.typography.labelSmall,
          color = if (p.isPassing) LogicHighGreen else SiliconErrorRed,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
      } else {
        Text(
          text = "Tap cell to inspect",
          style = MaterialTheme.typography.labelSmall,
          color = TextTertiary
        )
      }
    }
  }
}
