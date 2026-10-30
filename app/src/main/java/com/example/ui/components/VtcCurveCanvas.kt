package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CmosVtcAnalysis
import com.example.ui.theme.*

@Composable
fun VtcCurveCanvas(
  analysis: CmosVtcAnalysis,
  vdd: Float,
  modifier: Modifier = Modifier
) {
  var selectedVin by remember { mutableStateOf<Float?>(null) }
  var selectedVout by remember { mutableStateOf<Float?>(null) }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(SiliconSurfaceDark, RoundedCornerShape(12.dp))
      .border(1.dp, SiliconBorder, RoundedCornerShape(12.dp))
      .padding(12.dp)
      .testTag("vtc_curve_canvas_container")
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "CMOS Inverter VTC (Vin vs Vout)",
        style = MaterialTheme.typography.titleSmall,
        color = NeonCyan,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "VM = ${String.format("%.3f", analysis.vthM)} V",
        style = MaterialTheme.typography.labelMedium,
        color = ConductorGold,
        fontFamily = FontFamily.Monospace
      )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(200.dp)
        .background(SiliconDark, RoundedCornerShape(8.dp))
        .border(1.dp, SiliconBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
        .pointerInput(analysis) {
          detectTapGestures { offset ->
            val w = size.width
            val h = size.height
            val margin = 32f
            val graphW = w - margin * 2
            val graphH = h - margin * 2
            if (offset.x in margin..(margin + graphW) && offset.y in margin..(margin + graphH)) {
              val normX = (offset.x - margin) / graphW
              val tappedVin = normX * vdd
              // Find closest point
              val closest = analysis.points.minByOrNull { kotlin.math.abs(it.vin - tappedVin) }
              if (closest != null) {
                selectedVin = closest.vin
                selectedVout = closest.vout
              }
            }
          }
        }
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val margin = 32f
        val graphW = size.width - margin * 2
        val graphH = size.height - margin * 2

        // Draw grid lines
        val gridLines = 4
        for (i in 0..gridLines) {
          val y = margin + (graphH / gridLines) * i
          drawLine(
            color = SiliconBorder.copy(alpha = 0.4f),
            start = Offset(margin, y),
            end = Offset(size.width - margin, y),
            strokeWidth = 1f
          )
          val x = margin + (graphW / gridLines) * i
          drawLine(
            color = SiliconBorder.copy(alpha = 0.4f),
            start = Offset(x, margin),
            end = Offset(x, size.height - margin),
            strokeWidth = 1f
          )
        }

        // Draw unity gain line Vout = Vin (dashed concept)
        drawLine(
          color = TextTertiary.copy(alpha = 0.5f),
          start = Offset(margin, size.height - margin),
          end = Offset(size.width - margin, margin),
          strokeWidth = 1.5f
        )

        // Draw VTC curve path
        val vtcPath = Path()
        if (analysis.points.isNotEmpty()) {
          for (i in analysis.points.indices) {
            val pt = analysis.points[i]
            val px = margin + (pt.vin / vdd) * graphW
            val py = (size.height - margin) - (pt.vout / vdd) * graphH
            if (i == 0) {
              vtcPath.moveTo(px, py)
            } else {
              vtcPath.lineTo(px, py)
            }
          }
        }
        drawPath(
          path = vtcPath,
          color = NeonCyan,
          style = Stroke(width = 3.5f)
        )

        // Draw VM marker (Switching threshold where Vin = Vout = VM)
        val vmX = margin + (analysis.vthM / vdd) * graphW
        val vmY = (size.height - margin) - (analysis.vthM / vdd) * graphH
        drawCircle(
          color = ConductorGold,
          radius = 6f,
          center = Offset(vmX, vmY)
        )

        // Draw tapped position marker if any
        if (selectedVin != null && selectedVout != null) {
          val selX = margin + (selectedVin!! / vdd) * graphW
          val selY = (size.height - margin) - (selectedVout!! / vdd) * graphH
          drawCircle(
            color = LogicHighGreen,
            radius = 7f,
            center = Offset(selX, selY)
          )
          drawLine(
            color = LogicHighGreen.copy(alpha = 0.7f),
            start = Offset(selX, size.height - margin),
            end = Offset(selX, selY),
            strokeWidth = 1.5f
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column {
        Text(
          text = "Noise Margin Low (NML): ${String.format("%.2f", analysis.nmL)} V",
          style = MaterialTheme.typography.labelSmall,
          color = LogicHighGreen,
          fontFamily = FontFamily.Monospace
        )
        Text(
          text = "Noise Margin High (NMH): ${String.format("%.2f", analysis.nmH)} V",
          style = MaterialTheme.typography.labelSmall,
          color = LogicHighGreen,
          fontFamily = FontFamily.Monospace
        )
      }
      if (selectedVin != null && selectedVout != null) {
        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = "Probe: Vin=${String.format("%.2f", selectedVin)}V",
            style = MaterialTheme.typography.labelSmall,
            color = ConductorGold,
            fontFamily = FontFamily.Monospace
          )
          Text(
            text = "Vout=${String.format("%.2f", selectedVout)}V",
            style = MaterialTheme.typography.labelSmall,
            color = NeonCyan,
            fontFamily = FontFamily.Monospace
          )
        }
      } else {
        Text(
          text = "Tap curve to probe",
          style = MaterialTheme.typography.labelSmall,
          color = TextSecondary
        )
      }
    }
  }
}
