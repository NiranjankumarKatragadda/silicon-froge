package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
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
import com.example.model.CustomDieBlock
import com.example.model.TechnologyNode
import com.example.ui.theme.*

@Composable
fun CustomDieCanvas(
  node: TechnologyNode,
  dieWidthMm: Double,
  dieHeightMm: Double,
  drcViolations: Int,
  modifier: Modifier = Modifier
) {
  var selectedBlock by remember { mutableStateOf<CustomDieBlock?>(null) }

  // Define macro blocks on student's custom nanometer die
  val blocks = remember(node) {
    listOf(
      CustomDieBlock(
        id = "block_compute",
        name = "RISC-V / Compute Core",
        relativeX = 0.12f,
        relativeY = 0.12f,
        relativeWidth = 0.42f,
        relativeHeight = 0.45f,
        colorHex = 0xFF00E5FF, // Neon Cyan
        category = "COMPUTE",
        powerAllocationPct = 48.0
      ),
      CustomDieBlock(
        id = "block_sram",
        name = "SRAM L2 Cache",
        relativeX = 0.58f,
        relativeY = 0.12f,
        relativeWidth = 0.30f,
        relativeHeight = 0.45f,
        colorHex = 0xFF76FF03, // Neon Green
        category = "MEMORY",
        powerAllocationPct = 18.0
      ),
      CustomDieBlock(
        id = "block_npu",
        name = "Tensor Matrix Array",
        relativeX = 0.12f,
        relativeY = 0.62f,
        relativeWidth = 0.50f,
        relativeHeight = 0.26f,
        colorHex = 0xFFFFAB00, // Amber
        category = "COMPUTE",
        powerAllocationPct = 26.0
      ),
      CustomDieBlock(
        id = "block_pll",
        name = "Analog PLL & Clock Tree",
        relativeX = 0.66f,
        relativeY = 0.62f,
        relativeWidth = 0.22f,
        relativeHeight = 0.12f,
        colorHex = 0xFFFF4081, // Pink/Purple
        category = "ANALOG",
        powerAllocationPct = 5.0
      ),
      CustomDieBlock(
        id = "block_tsensor",
        name = "Thermal Diode & PMIC",
        relativeX = 0.66f,
        relativeY = 0.77f,
        relativeWidth = 0.22f,
        relativeHeight = 0.11f,
        colorHex = 0xFFE040FB, // Magenta
        category = "ANALOG",
        powerAllocationPct = 3.0
      )
    )
  }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(SiliconSurfaceDark, RoundedCornerShape(12.dp))
      .padding(12.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "PHYSICAL DIE FLOORPLAN & POWER GRID",
          color = NeonCyan,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
        Text(
          text = "${node.nodeNm}nm ${node.transistorArchitecture} • ${dieWidthMm}mm × ${dieHeightMm}mm",
          color = TextSecondary,
          fontSize = 11.sp
        )
      }

      Surface(
        color = if (drcViolations == 0) NeonGreen.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.2f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(
          1.dp,
          if (drcViolations == 0) NeonGreen else Color.Red
        )
      ) {
        Text(
          text = if (drcViolations == 0) "DRC: CLEAN (0)" else "DRC: $drcViolations ERRORS",
          color = if (drcViolations == 0) NeonGreen else Color.Red,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(230.dp)
        .background(Color(0xFF070B12), RoundedCornerShape(8.dp))
        .pointerInput(blocks) {
          detectTapGestures { tapOffset ->
            val w = size.width.toFloat()
            val h = size.height.toFloat()
            val hit = blocks.firstOrNull { b ->
              val bx = b.relativeX * w
              val by = b.relativeY * h
              val bw = b.relativeWidth * w
              val bh = b.relativeHeight * h
              tapOffset.x in bx..(bx + bw) && tapOffset.y in by..(by + bh)
            }
            selectedBlock = hit
          }
        }
        .testTag("custom_die_canvas_box")
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // 1. Outer Silicon Boundary
        drawRoundRect(
          color = Color(0xFF101926),
          size = size,
          cornerRadius = CornerRadius(8f, 8f)
        )

        // 2. I/O Pad Ring & Micro-Bumps around perimeter
        val padPitch = 24f
        val padSize = 10f
        // Top and bottom pad rows
        var x = 16f
        while (x < w - 16f) {
          drawRect(color = Color(0xFFB0BEC5), topLeft = Offset(x, 6f), size = Size(padSize, padSize))
          drawRect(color = Color(0xFFB0BEC5), topLeft = Offset(x, h - 16f), size = Size(padSize, padSize))
          x += padPitch
        }
        // Left and right pad columns
        var y = 16f
        while (y < h - 16f) {
          drawRect(color = Color(0xFFB0BEC5), topLeft = Offset(6f, y), size = Size(padSize, padSize))
          drawRect(color = Color(0xFFB0BEC5), topLeft = Offset(w - 16f, y), size = Size(padSize, padSize))
          y += padPitch
        }

        // 3. Seal Ring Guard
        drawRoundRect(
          color = Color(0xFF37474F),
          topLeft = Offset(18f, 18f),
          size = Size(w - 36f, h - 36f),
          style = Stroke(width = 2f),
          cornerRadius = CornerRadius(4f, 4f)
        )

        // 4. Macro Blocks
        blocks.forEach { b ->
          val bx = b.relativeX * w
          val by = b.relativeY * h
          val bw = b.relativeWidth * w
          val bh = b.relativeHeight * h
          val isSel = selectedBlock?.id == b.id

          drawRoundRect(
            color = Color(b.colorHex).copy(alpha = if (isSel) 0.40f else 0.22f),
            topLeft = Offset(bx, by),
            size = Size(bw, bh),
            cornerRadius = CornerRadius(4f, 4f)
          )

          drawRoundRect(
            color = if (isSel) Color.White else Color(b.colorHex),
            topLeft = Offset(bx, by),
            size = Size(bw, bh),
            cornerRadius = CornerRadius(4f, 4f),
            style = Stroke(width = if (isSel) 2.5f else 1.2f)
          )
        }

        // 5. Power Grid Distribution Stripes (VDD NeonCyan, VSS Amber)
        val stripeCount = 14
        val stepX = (w - 44f) / stripeCount
        for (i in 0..stripeCount) {
          val sx = 22f + (i * stepX)
          val color = if (i % 2 == 0) NeonCyan.copy(alpha = 0.25f) else Color(0xFFFFB300).copy(alpha = 0.25f)
          drawLine(
            color = color,
            start = Offset(sx, 22f),
            end = Offset(sx, h - 22f),
            strokeWidth = 1.0f
          )
        }
      }

      // Information pill on bottom overlay
      Surface(
        color = Color.Black.copy(alpha = 0.75f),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .padding(bottom = 6.dp)
      ) {
        Text(
          text = selectedBlock?.let {
            "${it.name} • ${it.powerAllocationPct}% Power • Tap block to inspect"
          } ?: "Tap any block (Compute, SRAM, NPU, PLL) to inspect physical telemetry",
          color = selectedBlock?.let { Color(it.colorHex) } ?: TextSecondary,
          fontSize = 10.sp,
          fontFamily = FontFamily.Monospace,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
      }
    }
  }
}
