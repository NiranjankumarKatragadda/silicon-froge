package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FloorplanState
import com.example.ui.theme.*

@Composable
fun FloorplanCanvas(
  state: FloorplanState,
  modifier: Modifier = Modifier
) {
  var showIrHeatmap by remember { mutableStateOf(true) }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(SiliconSurfaceDark, RoundedCornerShape(12.dp))
      .border(1.dp, SiliconBorder, RoundedCornerShape(12.dp))
      .padding(12.dp)
      .testTag("floorplan_canvas_container")
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "ASIC Floorplan & Power Mesh",
          style = MaterialTheme.typography.titleSmall,
          color = NeonCyan,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Max IR Drop: ${String.format("%.1f", state.maxIrDropMv)} mV | DRC: ${state.drcViolationCount} errors",
          style = MaterialTheme.typography.labelSmall,
          color = if (state.drcViolationCount == 0 && state.maxIrDropMv < 40f) LogicHighGreen else SiliconErrorRed
        )
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "IR Mesh", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Spacer(modifier = Modifier.width(4.dp))
        Switch(
          checked = showIrHeatmap,
          onCheckedChange = { showIrHeatmap = it },
          modifier = Modifier.testTag("ir_heatmap_toggle")
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(210.dp)
        .background(SiliconDark, RoundedCornerShape(8.dp))
        .border(1.dp, SiliconBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val pad = 16f
        val dieW = w - pad * 2
        val dieH = h - pad * 2

        // Draw Die Substrate
        drawRect(
          color = Color(0xFF0F172A),
          topLeft = Offset(pad, pad),
          size = Size(dieW, dieH)
        )

        // Draw I/O Pad Ring
        drawRect(
          color = ConductorGold.copy(alpha = 0.4f),
          topLeft = Offset(pad, pad),
          size = Size(dieW, dieH),
          style = Stroke(width = 8f)
        )

        // Draw Core boundary
        val coreMargin = 12f
        val coreLeft = pad + coreMargin
        val coreTop = pad + coreMargin
        val coreW = dieW - coreMargin * 2
        val coreH = dieH - coreMargin * 2

        drawRect(
          color = SiliconBorder,
          topLeft = Offset(coreLeft, coreTop),
          size = Size(coreW, coreH),
          style = Stroke(width = 1.5f)
        )

        // Draw Macros
        for (m in state.macros) {
          val mx = coreLeft + m.posXPercent * coreW
          val my = coreTop + m.posYPercent * coreH
          val mw = m.widthPercent * coreW
          val mh = m.heightPercent * coreH

          val macroColor = when {
            m.name.contains("SRAM") -> NeonCyan
            m.name.contains("PLL") -> ConductorGold
            m.name.contains("PCIe") -> Color(0xFFA855F7)
            else -> LogicHighGreen
          }

          drawRect(
            color = macroColor.copy(alpha = 0.35f),
            topLeft = Offset(mx, my),
            size = Size(mw, mh)
          )
          drawRect(
            color = macroColor,
            topLeft = Offset(mx, my),
            size = Size(mw, mh),
            style = Stroke(width = 2f)
          )
        }

        // Draw Power Grid Stripes (VDD/VSS mesh)
        val stripes = state.powerStripeCount
        for (i in 1 until stripes) {
          val sx = coreLeft + (coreW / stripes) * i
          drawLine(
            color = Metal1CopperColor.copy(alpha = 0.6f),
            start = Offset(sx, coreTop),
            end = Offset(sx, coreTop + coreH),
            strokeWidth = 2.5f
          )
          val sy = coreTop + (coreH / stripes) * i
          drawLine(
            color = Metal2CopperColor.copy(alpha = 0.45f),
            start = Offset(coreLeft, sy),
            end = Offset(coreLeft + coreW, sy),
            strokeWidth = 2.5f
          )
        }

        // Draw IR drop heatmap overlay if enabled
        if (showIrHeatmap) {
          val heatmapBrush = Brush.radialGradient(
            colors = listOf(
              SiliconErrorRed.copy(alpha = 0.45f),
              CleanroomAmber.copy(alpha = 0.25f),
              Color.Transparent
            ),
            center = Offset(coreLeft + coreW / 2f, coreTop + coreH / 2f),
            radius = coreW * 0.55f
          )
          drawRect(
            brush = heatmapBrush,
            topLeft = Offset(coreLeft, coreTop),
            size = Size(coreW, coreH)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // DRC Warnings or Status
    if (state.drcDetails.isNotEmpty()) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(SiliconErrorRed.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
          .padding(8.dp)
      ) {
        state.drcDetails.forEach { detail ->
          Text(
            text = "• $detail",
            style = MaterialTheme.typography.labelSmall,
            color = SiliconErrorRed,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp
          )
        }
      }
    } else {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(LogicHighGreen.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
          .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "✓ DRC Clean: Minimum spacing, antenna ratio & keepout rules satisfied",
          style = MaterialTheme.typography.labelSmall,
          color = LogicHighGreen
        )
      }
    }
  }
}
