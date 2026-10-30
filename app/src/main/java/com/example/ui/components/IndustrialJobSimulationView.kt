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
import com.example.model.IndustrialJobWorkflow
import com.example.ui.theme.*

@Composable
fun IndustrialJobSimulationView(
  modifier: Modifier = Modifier
) {
  val workflows = CircuitDesignRepository.industrialJobWorkflows
  var selectedWorkflow by remember { mutableStateOf(workflows.first()) }
  var activeMilestoneIdx by remember { mutableStateOf(0) }
  var parameterValues by remember { mutableStateOf<MutableMap<String, Float>>(mutableMapOf()) }
  var verificationFeedback by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
  var completedMilestones by remember { mutableStateOf(setOf<Int>()) }

  // Initialize parameters when milestone changes
  val activeMilestone = selectedWorkflow.milestones.getOrNull(activeMilestoneIdx) ?: selectedWorkflow.milestones.first()
  LaunchedEffect(activeMilestone) {
    val initial = activeMilestone.parameters.associate { it.id to it.currentValue }.toMutableMap()
    parameterValues = initial
    verificationFeedback = null
  }

  val isWorkflowComplete = completedMilestones.size == selectedWorkflow.milestones.size

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
            .background(CleanroomAmber.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.Engineering, contentDescription = null, tint = CleanroomAmber, modifier = Modifier.size(18.dp))
        }
        Column {
          Text(
            text = "Industrial Job Simulation Workflow",
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Real-world testing, timing signoff & ATE wafer diagnostics",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            fontSize = 11.sp
          )
        }
      }

      Box(
        modifier = Modifier
          .background(NeonCyan.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
          .padding(horizontal = 6.dp, vertical = 2.dp)
      ) {
        Text(
          text = selectedWorkflow.fabNode,
          color = NeonCyan,
          style = MaterialTheme.typography.labelSmall,
          fontSize = 10.sp,
          fontFamily = FontFamily.Monospace
        )
      }
    }

    // Workflow Selection Chips
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      items(workflows) { flow ->
        val isSelected = flow.workflowId == selectedWorkflow.workflowId
        Surface(
          color = if (isSelected) CleanroomAmber else SiliconCardDark,
          shape = RoundedCornerShape(8.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) CleanroomAmber else SiliconBorder),
          modifier = Modifier.clickable {
            selectedWorkflow = flow
            activeMilestoneIdx = 0
            completedMilestones = emptySet()
            verificationFeedback = null
          }.testTag("workflow_chip_${flow.workflowId}")
        ) {
          Text(
            text = flow.targetCompany,
            color = if (isSelected) Color.Black else TextPrimary,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
          )
        }
      }
    }

    // Workflow Scenario Description
    Surface(
      color = SiliconCardDark,
      shape = RoundedCornerShape(10.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder)
    ) {
      Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(text = selectedWorkflow.title, style = MaterialTheme.typography.titleSmall, color = ConductorGold, fontWeight = FontWeight.Bold)
          Text(text = selectedWorkflow.salaryLevel, style = MaterialTheme.typography.labelSmall, color = LogicHighGreen, fontFamily = FontFamily.Monospace)
        }
        Text(text = selectedWorkflow.scenarioBrief, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
      }
    }

    // Milestone Stepper
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      selectedWorkflow.milestones.forEachIndexed { idx, ms ->
        val isCurrent = idx == activeMilestoneIdx
        val isDone = completedMilestones.contains(idx)

        Surface(
          color = when {
            isDone -> LogicHighGreen.copy(alpha = 0.2f)
            isCurrent -> NeonCyan.copy(alpha = 0.2f)
            else -> SiliconCardDark
          },
          shape = RoundedCornerShape(6.dp),
          border = androidx.compose.foundation.BorderStroke(
            1.dp,
            when {
              isDone -> LogicHighGreen
              isCurrent -> NeonCyan
              else -> SiliconBorder
            }
          ),
          modifier = Modifier
            .weight(1f)
            .clickable {
              activeMilestoneIdx = idx
              verificationFeedback = null
            }
        ) {
          Column(modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "Milestone ${ms.stepNumber}",
              color = if (isDone) LogicHighGreen else if (isCurrent) NeonCyan else TextSecondary,
              style = MaterialTheme.typography.labelSmall,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = if (isDone) "✓ Done" else if (isCurrent) "Active" else "Pending",
              color = if (isDone) LogicHighGreen else TextTertiary,
              fontSize = 9.sp
            )
          }
        }
      }
    }

    // Active Milestone Workbench
    Surface(
      color = SiliconSurfaceDark,
      shape = RoundedCornerShape(10.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder)
    ) {
      Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = activeMilestone.title,
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = activeMilestone.departmentRole,
            style = MaterialTheme.typography.labelSmall,
            color = ConductorGold,
            fontSize = 10.sp
          )
        }

        Text(text = activeMilestone.taskInstructions, style = MaterialTheme.typography.bodySmall, color = TextSecondary)

        // Live Terminal Console Prompt
        Surface(
          color = Color(0xFF03060C),
          shape = RoundedCornerShape(6.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0F2B48))
        ) {
          Column(modifier = Modifier.padding(8.dp)) {
            Text(text = "// ${activeMilestone.toolConsoleName}", color = TextTertiary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            Text(text = activeMilestone.liveTerminalPrompt, color = LogicHighGreen, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
          }
        }

        // Tunable Parameters
        activeMilestone.parameters.forEach { param ->
          val currentVal = parameterValues[param.id] ?: param.currentValue
          Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text(text = param.name, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
              Text(
                text = "${String.format("%.1f", currentVal)} ${param.unit}",
                style = MaterialTheme.typography.labelSmall,
                color = NeonCyan,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
              )
            }
            Slider(
              value = currentVal,
              onValueChange = { newVal ->
                val updated = parameterValues.toMutableMap()
                updated[param.id] = newVal
                parameterValues = updated
                verificationFeedback = null
              },
              valueRange = param.minValue..param.maxValue,
              modifier = Modifier.testTag("param_slider_${param.id}")
            )
          }
        }

        // Run Verification Action Button
        Button(
          onClick = {
            val result = activeMilestone.verificationRule(parameterValues)
            verificationFeedback = result
            if (result.first) {
              completedMilestones = completedMilestones + activeMilestoneIdx
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth().testTag("verify_milestone_button")
        ) {
          Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Execute Industrial Diagnostic & Check Milestone", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        // Verification Feedback Banner
        if (verificationFeedback != null) {
          val (isPass, message) = verificationFeedback!!
          Surface(
            color = if (isPass) LogicHighGreen.copy(alpha = 0.15f) else SiliconErrorRed.copy(alpha = 0.15f),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isPass) LogicHighGreen else SiliconErrorRed)
          ) {
            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Icon(
                imageVector = if (isPass) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = if (isPass) LogicHighGreen else SiliconErrorRed,
                modifier = Modifier.size(20.dp)
              )
              Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = if (isPass) LogicHighGreen else SiliconErrorRed,
                fontWeight = FontWeight.Medium
              )
            }
          }
        }
      }
    }

    // Industrial Signoff Certificate (Unlocked when all milestones passed)
    if (isWorkflowComplete) {
      Surface(
        color = Color(0xFF04121A),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, LogicHighGreen)
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "INDUSTRIAL SILICON SIGNOFF CERTIFIED",
                style = MaterialTheme.typography.labelSmall,
                color = LogicHighGreen,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Target Role: ${selectedWorkflow.targetCompany}",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
              )
            }
            Box(
              modifier = Modifier
                .background(LogicHighGreen, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(text = "QUALIFIED", color = Color.Black, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
            }
          }

          Text(
            text = "Candidate successfully satisfied all critical milestones: timing closure, dynamic voltage drop constraints, and production ATE diagnostic qualification on ${selectedWorkflow.fabNode}.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
          )
        }
      }
    }
  }
}
