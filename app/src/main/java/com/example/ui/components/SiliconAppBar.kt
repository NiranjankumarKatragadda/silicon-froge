package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CleanroomSafetyState
import com.example.ui.theme.*

@Composable
fun SiliconAppBar(
  safetyState: CleanroomSafetyState,
  onSafetyClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val allSafe = safetyState.esdStrapGroundOk &&
      safetyState.silaneScrubberActive &&
      safetyState.hfAcidSpillKitVerified &&
      safetyState.bunnySuitParticlePpm < 35

  Surface(
    color = SiliconDark,
    tonalElevation = 4.dp,
    modifier = modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .background(NeonCyan.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .border(1.dp, NeonCyan, RoundedCornerShape(8.dp)),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Si",
            color = NeonCyan,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
          )
        }

        Column {
          Text(
            text = "SiliconForge",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.ExtraBold
          )
          Text(
            text = "VLSI & Semiconductor Studio",
            style = MaterialTheme.typography.labelSmall,
            color = ConductorGold,
            fontSize = 11.sp
          )
        }
      }

      // Cleanroom Safety Pill
      Surface(
        color = if (allSafe) LogicHighGreen.copy(alpha = 0.15f) else SiliconErrorRed.copy(alpha = 0.15f),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(
          1.dp,
          if (allSafe) LogicHighGreen else SiliconErrorRed
        ),
        modifier = Modifier
          .clickable { onSafetyClick() }
          .testTag("cleanroom_safety_pill")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(
            imageVector = if (allSafe) Icons.Default.Shield else Icons.Default.Warning,
            contentDescription = "Safety Status",
            tint = if (allSafe) LogicHighGreen else SiliconErrorRed,
            modifier = Modifier.size(14.dp)
          )
          Text(
            text = if (allSafe) "ISO 3 • SAFE" else "HAZARD ALERT",
            color = if (allSafe) LogicHighGreen else SiliconErrorRed,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
          )
        }
      }
    }
  }
}
