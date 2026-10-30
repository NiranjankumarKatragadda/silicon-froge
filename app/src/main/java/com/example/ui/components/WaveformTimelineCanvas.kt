package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.model.TimingAnalysisResult
import com.example.ui.theme.*

@Composable
fun WaveformTimelineCanvas(
  timing: TimingAnalysisResult,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(SiliconSurfaceDark, RoundedCornerShape(12.dp))
      .border(1.dp, SiliconBorder, RoundedCornerShape(12.dp))
      .padding(12.dp)
      .testTag("waveform_timeline_container")
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Timing Diagram: Setup & Hold Windows",
        style = MaterialTheme.typography.titleSmall,
        color = NeonCyan,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "Tclk = ${String.format("%.2f", timing.clockPeriodNs)} ns (${timing.clockFreqMhz.toInt()} MHz)",
        style = MaterialTheme.typography.labelSmall,
        color = ConductorGold,
        fontFamily = FontFamily.Monospace
      )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(180.dp)
        .background(SiliconDark, RoundedCornerShape(8.dp))
        .border(1.dp, SiliconBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val rowHeight = h / 3f
        val leftPad = 50f
        val rightPad = 16f
        val activeW = w - leftPad - rightPad

        // Three rows: CLK, D_IN, Q_OUT
        // 1. Clock Waveform
        val clkYHigh = rowHeight * 0.25f
        val clkYLow = rowHeight * 0.85f
        val clkPath = Path()
        val halfPeriodPx = activeW / 4f

        for (i in 0..3) {
          val startX = leftPad + i * halfPeriodPx
          if (i == 0) clkPath.moveTo(startX, clkYLow)
          clkPath.lineTo(startX, clkYHigh)
          clkPath.lineTo(startX + halfPeriodPx / 2f, clkYHigh)
          clkPath.lineTo(startX + halfPeriodPx / 2f, clkYLow)
          clkPath.lineTo(startX + halfPeriodPx, clkYLow)
        }
        drawPath(clkPath, color = NeonCyan, style = Stroke(width = 2.5f))

        // Rising Edge Marker (At 1st posedge)
        val posedgeX = leftPad + halfPeriodPx / 2f
        drawLine(
          color = ConductorGold,
          start = Offset(posedgeX, 0f),
          end = Offset(posedgeX, h),
          strokeWidth = 1.5f
        )

        // Setup Window Shading (left of posedge)
        val setupW = (timing.tSetupNs / timing.clockPeriodNs) * activeW
        drawRect(
          color = if (timing.hasSetupViolation) SiliconErrorRed.copy(alpha = 0.35f) else LogicHighGreen.copy(alpha = 0.25f),
          topLeft = Offset(posedgeX - setupW, 0f),
          size = Size(setupW, h)
        )

        // Hold Window Shading (right of posedge)
        val holdW = (timing.tHoldNs / timing.clockPeriodNs) * activeW
        drawRect(
          color = if (timing.hasHoldViolation) SiliconErrorRed.copy(alpha = 0.35f) else CleanroomAmber.copy(alpha = 0.25f),
          topLeft = Offset(posedgeX, 0f),
          size = Size(holdW, h)
        )

        // 2. Data In (D) Waveform
        val dRowY = rowHeight * 1.5f
        val dHigh = dRowY - rowHeight * 0.3f
        val dLow = dRowY + rowHeight * 0.3f
        val dPath = Path()
        val dataTransitionX = posedgeX - setupW - 30f

        dPath.moveTo(leftPad, dLow)
        dPath.lineTo(dataTransitionX, dLow)
        dPath.lineTo(dataTransitionX + 12f, dHigh)
        dPath.lineTo(w - rightPad, dHigh)
        drawPath(dPath, color = ConductorGold, style = Stroke(width = 2.5f))

        // 3. Data Out (Q) Waveform
        val qRowY = rowHeight * 2.5f
        val qHigh = qRowY - rowHeight * 0.3f
        val qLow = qRowY + rowHeight * 0.3f
        val tcqPx = (timing.tCqNs / timing.clockPeriodNs) * activeW
        val qTransitionX = posedgeX + tcqPx

        val qPath = Path()
        qPath.moveTo(leftPad, qLow)
        qPath.lineTo(qTransitionX, qLow)
        qPath.lineTo(qTransitionX + 12f, qHigh)
        qPath.lineTo(w - rightPad, qHigh)
        drawPath(qPath, color = LogicHighGreen, style = Stroke(width = 2.5f))
      }

      // Labels along the left
      Column(
        modifier = Modifier
          .fillMaxHeight()
          .padding(start = 8.dp),
        verticalArrangement = Arrangement.SpaceAround
      ) {
        Text(text = "CLK", style = MaterialTheme.typography.labelSmall, color = NeonCyan, fontWeight = FontWeight.Bold)
        Text(text = "DATA D", style = MaterialTheme.typography.labelSmall, color = ConductorGold, fontWeight = FontWeight.Bold)
        Text(text = "OUT Q", style = MaterialTheme.typography.labelSmall, color = LogicHighGreen, fontWeight = FontWeight.Bold)
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Slack readout cards
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Box(
        modifier = Modifier
          .weight(1f)
          .background(
            if (timing.hasSetupViolation) SiliconErrorRed.copy(alpha = 0.15f) else LogicHighGreen.copy(alpha = 0.15f),
            RoundedCornerShape(8.dp)
          )
          .border(
            1.dp,
            if (timing.hasSetupViolation) SiliconErrorRed else LogicHighGreen,
            RoundedCornerShape(8.dp)
          )
          .padding(8.dp)
      ) {
        Column {
          Text(
            text = "Setup Slack",
            style = MaterialTheme.typography.labelSmall,
            color = if (timing.hasSetupViolation) SiliconErrorRed else LogicHighGreen
          )
          Text(
            text = "${String.format("%+.3f", timing.setupSlackNs)} ns",
            style = MaterialTheme.typography.titleSmall,
            color = if (timing.hasSetupViolation) SiliconErrorRed else LogicHighGreen,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
        }
      }

      Box(
        modifier = Modifier
          .weight(1f)
          .background(
            if (timing.hasHoldViolation) SiliconErrorRed.copy(alpha = 0.15f) else LogicHighGreen.copy(alpha = 0.15f),
            RoundedCornerShape(8.dp)
          )
          .border(
            1.dp,
            if (timing.hasHoldViolation) SiliconErrorRed else LogicHighGreen,
            RoundedCornerShape(8.dp)
          )
          .padding(8.dp)
      ) {
        Column {
          Text(
            text = "Hold Slack",
            style = MaterialTheme.typography.labelSmall,
            color = if (timing.hasHoldViolation) SiliconErrorRed else LogicHighGreen
          )
          Text(
            text = "${String.format("%+.3f", timing.holdSlackNs)} ns",
            style = MaterialTheme.typography.titleSmall,
            color = if (timing.hasHoldViolation) SiliconErrorRed else LogicHighGreen,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
        }
      }
    }
  }
}
