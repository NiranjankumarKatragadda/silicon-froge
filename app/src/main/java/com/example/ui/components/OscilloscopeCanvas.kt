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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OscilloscopeState
import com.example.ui.theme.*
import kotlin.math.sin

@Composable
fun OscilloscopeCanvas(
  state: OscilloscopeState,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(Color(0xFF040810), RoundedCornerShape(12.dp))
      .border(2.dp, Color(0xFF1E3A5F), RoundedCornerShape(12.dp))
      .padding(12.dp)
      .testTag("oscilloscope_screen")
  ) {
    // Top Bar: Scope Readouts
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
          modifier = Modifier
            .background(if (state.isRunning) LogicHighGreen else CleanroomAmber, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(
            text = if (state.isRunning) "RUN AUTO" else "STOPPED",
            color = Color.Black,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
          )
        }
        Text(
          text = "RIGOL/KEYSIGHT 200MHz DSO",
          style = MaterialTheme.typography.labelSmall,
          color = TextSecondary,
          fontFamily = FontFamily.Monospace,
          fontSize = 10.sp
        )
      }

      Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = "CH1: ${state.ch1VoltsPerDiv}V/div",
          color = NeonCyan,
          style = MaterialTheme.typography.labelSmall,
          fontFamily = FontFamily.Monospace,
          fontSize = 11.sp
        )
        Text(
          text = "CH2: ${state.ch2VoltsPerDiv}V/div",
          color = ConductorGold,
          style = MaterialTheme.typography.labelSmall,
          fontFamily = FontFamily.Monospace,
          fontSize = 11.sp
        )
        Text(
          text = "TB: ${state.timeBaseMs}ms",
          color = TextPrimary,
          style = MaterialTheme.typography.labelSmall,
          fontFamily = FontFamily.Monospace,
          fontSize = 11.sp
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Graticule & Waveform Display
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(180.dp)
        .background(Color(0xFF02070E), RoundedCornerShape(6.dp))
        .border(1.dp, Color(0xFF0D253A), RoundedCornerShape(6.dp))
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Draw 10x8 Graticule Grid
        val cols = 10
        val rows = 8
        val colW = w / cols
        val rowH = h / rows

        for (i in 0..cols) {
          val x = i * colW
          drawLine(
            color = Color(0xFF0D334C),
            start = Offset(x, 0f),
            end = Offset(x, h),
            strokeWidth = if (i == 5) 1.5f else 0.7f
          )
        }

        for (j in 0..rows) {
          val y = j * rowH
          drawLine(
            color = Color(0xFF0D334C),
            start = Offset(0f, y),
            end = Offset(w, y),
            strokeWidth = if (j == 4) 1.5f else 0.7f
          )
        }

        // Draw Trigger Level Horizontal Line
        val trigY = (h / 2f) - (state.triggerLevelV / state.ch1VoltsPerDiv) * rowH
        drawLine(
          color = CleanroomAmber.copy(alpha = 0.5f),
          start = Offset(0f, trigY),
          end = Offset(w, trigY),
          strokeWidth = 1f
        )

        // CH1 Waveform (e.g. Clock / Input pulse)
        if (state.ch1Enabled) {
          val ch1Path = Path()
          val centerY = rowH * 2.8f
          val amp = rowH * 1.6f
          val periods = 3.5f * (1f / state.timeBaseMs)

          for (px in 0..w.toInt() step 2) {
            val progress = px / w
            val angle = progress * periods * 2 * Math.PI
            val waveVal = if (state.waveformSource == "CLOCK_GEN") {
              if (sin(angle) >= 0) 1f else -1f
            } else {
              sin(angle).toFloat()
            }
            val py = centerY - waveVal * amp * 0.5f
            if (px == 0) ch1Path.moveTo(px.toFloat(), py) else ch1Path.lineTo(px.toFloat(), py)
          }
          drawPath(ch1Path, color = NeonCyan, style = Stroke(width = 2.5f))
        }

        // CH2 Waveform (e.g. Inverted or Delayed output)
        if (state.ch2Enabled) {
          val ch2Path = Path()
          val centerY = rowH * 5.5f
          val amp = rowH * 1.5f
          val periods = 3.5f * (1f / state.timeBaseMs)

          for (px in 0..w.toInt() step 2) {
            val progress = px / w
            val angle = (progress * periods * 2 * Math.PI) + Math.PI // 180 deg out of phase
            val waveVal = if (state.waveformSource == "CLOCK_GEN") {
              if (sin(angle) >= 0) 1f else -1f
            } else {
              sin(angle).toFloat() * 0.85f
            }
            val py = centerY - waveVal * amp * 0.5f
            if (px == 0) ch2Path.moveTo(px.toFloat(), py) else ch2Path.lineTo(px.toFloat(), py)
          }
          drawPath(ch2Path, color = ConductorGold, style = Stroke(width = 2.5f))
        }
      }
    }
  }
}
