package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.DomainType
import com.example.model.EngineerPersona
import com.example.model.InterviewQuestion
import com.example.ui.theme.*
import com.example.viewmodel.SiliconLabViewModel

@Composable
fun InterviewPrepScreen(
  viewModel: SiliconLabViewModel,
  modifier: Modifier = Modifier
) {
  var selectedSubTab by remember { mutableStateOf(0) } // 0=Direct Practice, 1=Question Bank, 2=Whiteboard Challenge

  val selectedEngineer by viewModel.selectedEngineer.collectAsStateWithLifecycle()
  val activeSession by viewModel.activeInterviewSession.collectAsStateWithLifecycle()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(SiliconDark)
  ) {
    // Sub-tab Navigation
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(SiliconSurfaceDark)
        .padding(horizontal = 16.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      TabPill(
        title = "Top Engineer Sessions",
        isSelected = selectedSubTab == 0,
        onClick = { selectedSubTab = 0 },
        modifier = Modifier.weight(1f).testTag("interview_subtab_engineers")
      )
      TabPill(
        title = "Question Bank",
        isSelected = selectedSubTab == 1,
        onClick = { selectedSubTab = 1 },
        modifier = Modifier.weight(1f).testTag("interview_subtab_qbank")
      )
      TabPill(
        title = "Whiteboard Challenge",
        isSelected = selectedSubTab == 2,
        onClick = { selectedSubTab = 2 },
        modifier = Modifier.weight(1f).testTag("interview_subtab_whiteboard")
      )
    }

    when (selectedSubTab) {
      0 -> DirectEngineerPracticeTab(
        engineers = viewModel.topEngineers,
        selectedEngineer = selectedEngineer,
        onSelectEngineer = { viewModel.selectEngineerPersona(it) },
        sessionState = activeSession,
        onSelectAnswer = { turnIdx, optIdx -> viewModel.selectInterviewAnswer(turnIdx, optIdx) },
        onRestartSession = { viewModel.startMockInterviewSession(selectedEngineer) }
      )
      1 -> QuestionBankTab(questions = viewModel.interviewQuestions)
      2 -> WhiteboardChallengeTab()
    }
  }
}

@Composable
private fun TabPill(
  title: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    color = if (isSelected) NeonCyan else SiliconCardDark,
    shape = RoundedCornerShape(20.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) NeonCyan else SiliconBorder),
    modifier = modifier.clickable { onClick() }
  ) {
    Box(
      modifier = Modifier.padding(vertical = 8.dp),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = title,
        color = if (isSelected) Color.Black else TextPrimary,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
      )
    }
  }
}

// ====================================================================
// SUB-TAB 0: DIRECT PRACTICE WITH TOP ENGINEERS
// ====================================================================
@Composable
private fun DirectEngineerPracticeTab(
  engineers: List<EngineerPersona>,
  selectedEngineer: EngineerPersona,
  onSelectEngineer: (EngineerPersona) -> Unit,
  sessionState: com.example.model.InterviewSessionState?,
  onSelectAnswer: (Int, Int) -> Unit,
  onRestartSession: () -> Unit
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Select Interviewer Carousel
    item {
      Text(text = "Choose Your Silicon Interviewer", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
      Spacer(modifier = Modifier.height(6.dp))
      LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(engineers) { eng ->
          val isSelected = eng.id == selectedEngineer.id
          Surface(
            color = if (isSelected) NeonCyan.copy(alpha = 0.15f) else SiliconSurfaceDark,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) NeonCyan else SiliconBorder),
            modifier = Modifier
              .width(230.dp)
              .clickable { onSelectEngineer(eng) }
          ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(text = eng.name, style = MaterialTheme.typography.titleSmall, color = if (isSelected) NeonCyan else TextPrimary, fontWeight = FontWeight.Bold)
                Box(
                  modifier = Modifier
                    .background(ConductorGold.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                  Text(text = "${eng.yearsExperience}y Exp", style = MaterialTheme.typography.labelSmall, color = ConductorGold, fontSize = 9.sp)
                }
              }
              Text(text = eng.title, style = MaterialTheme.typography.labelSmall, color = ConductorGold, fontSize = 11.sp)
              Text(text = eng.companyBadge, style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 10.sp)
              Spacer(modifier = Modifier.height(2.dp))
              Text(text = eng.bio, style = MaterialTheme.typography.bodySmall, color = TextSecondary, fontSize = 11.sp, maxLines = 2)
            }
          }
        }
      }
    }

    // Active Mock Interview Session
    if (sessionState == null) {
      item {
        Button(
          onClick = onRestartSession,
          colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text("Begin Technical Interview Session", color = Color.Black, fontWeight = FontWeight.Bold)
        }
      }
    } else {
      // Session Header
      item {
        Surface(
          color = SiliconSurfaceDark,
          shape = RoundedCornerShape(12.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(text = "LIVE INTERVIEW SESSION", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 9.sp)
              Text(text = "Round with ${sessionState.persona.name}", style = MaterialTheme.typography.titleSmall, color = ConductorGold, fontWeight = FontWeight.Bold)
            }

            Box(
              modifier = Modifier
                .background(LogicHighGreen.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = "Score: ${sessionState.totalScore} pts",
                style = MaterialTheme.typography.labelSmall,
                color = LogicHighGreen,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }

      // Interview Turns
      items(sessionState.turns.indices.toList()) { turnIdx ->
        val turn = sessionState.turns[turnIdx]
        val selectedAnswer = sessionState.selectedAnswers[turnIdx]

        Surface(
          color = SiliconSurfaceDark,
          shape = RoundedCornerShape(12.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder)
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(text = "Question ${turn.turnNumber}", style = MaterialTheme.typography.labelSmall, color = NeonCyan, fontWeight = FontWeight.Bold)
              Text(text = turn.contextBrief, style = MaterialTheme.typography.labelSmall, color = TextTertiary, fontSize = 10.sp)
            }

            Text(
              text = "\"${turn.questionText}\"",
              style = MaterialTheme.typography.bodyMedium,
              color = TextPrimary,
              fontWeight = FontWeight.Medium
            )

            // Candidate Answer Options
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              turn.candidateOptions.forEachIndexed { optIdx, optText ->
                val isChosen = selectedAnswer == optIdx
                val isBest = optIdx == turn.bestOptionIndex
                val isEvaluated = selectedAnswer != null

                Surface(
                  color = when {
                    !isEvaluated -> SiliconCardDark
                    isChosen && isBest -> LogicHighGreen.copy(alpha = 0.2f)
                    isChosen && !isBest -> SiliconErrorRed.copy(alpha = 0.2f)
                    isBest -> LogicHighGreen.copy(alpha = 0.1f)
                    else -> SiliconCardDark.copy(alpha = 0.5f)
                  },
                  shape = RoundedCornerShape(8.dp),
                  border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    when {
                      !isEvaluated -> SiliconBorder
                      isChosen && isBest -> LogicHighGreen
                      isChosen && !isBest -> SiliconErrorRed
                      isBest -> LogicHighGreen.copy(alpha = 0.6f)
                      else -> SiliconBorder.copy(alpha = 0.3f)
                    }
                  ),
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isEvaluated) {
                      onSelectAnswer(turnIdx, optIdx)
                    }
                ) {
                  Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    Text(
                      text = "${('A' + optIdx)}.",
                      color = if (isChosen) (if (isBest) LogicHighGreen else SiliconErrorRed) else TextSecondary,
                      fontWeight = FontWeight.Bold,
                      fontFamily = FontFamily.Monospace
                    )
                    Text(
                      text = optText,
                      style = MaterialTheme.typography.bodySmall,
                      color = TextPrimary
                    )
                  }
                }
              }
            }

            // Engineer Feedback & Hiring Tip
            if (selectedAnswer != null) {
              val feedback = turn.engineerFeedbackMap[selectedAnswer] ?: ""
              Surface(
                color = SiliconCardElevated,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder)
              ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Psychology, contentDescription = null, tint = ConductorGold, modifier = Modifier.size(16.dp))
                    Text(text = "${sessionState.persona.name}'s Feedback:", style = MaterialTheme.typography.labelSmall, color = ConductorGold, fontWeight = FontWeight.Bold)
                  }
                  Text(text = feedback, style = MaterialTheme.typography.bodySmall, color = TextPrimary)

                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = "💡 Hiring Manager Tip: ${turn.hiringTip}",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonCyan,
                    fontSize = 11.sp
                  )
                }
              }
            }
          }
        }
      }

      // Final Evaluation Report Card
      if (sessionState.isFinished) {
        item {
          Surface(
            color = SiliconSurfaceDark,
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, LogicHighGreen)
          ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
              Text(
                text = "TECHNICAL INTERVIEW COMPLETION EVALUATION",
                style = MaterialTheme.typography.labelSmall,
                color = LogicHighGreen,
                fontWeight = FontWeight.Bold
              )

              Text(
                text = if (sessionState.totalScore >= 80) "VERDICT: STRONG HIRE (Senior ASIC Signoff Grade)" else "VERDICT: HIRE WITH RESERVATION (Focus on PVT Corners)",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
              )

              Text(
                text = "Candidate demonstrated exceptional physical insight into clock skew, setup/hold independence, and industrial ATE fault debugging. Recommended for Tier-1 IC verification and design teams.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
              )

              Button(
                onClick = onRestartSession,
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                modifier = Modifier.fillMaxWidth()
              ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Practice Again with Another Persona", color = Color.Black, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }
  }
}

// ====================================================================
// SUB-TAB 1: QUESTION BANK
// ====================================================================
@Composable
private fun QuestionBankTab(questions: List<InterviewQuestion>) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Text(
        text = "High-Yield VLSI & Silicon Interview Questions",
        style = MaterialTheme.typography.titleMedium,
        color = NeonCyan,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "Curated from actual technical rounds at Apple, NVIDIA, TSMC, Qualcomm, and AMD.",
        style = MaterialTheme.typography.bodySmall,
        color = TextSecondary
      )
    }

    items(questions) { q ->
      var isRevealed by remember { mutableStateOf(false) }

      Surface(
        color = SiliconSurfaceDark,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder)
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .background(ConductorGold.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(text = q.domain.title, style = MaterialTheme.typography.labelSmall, color = ConductorGold, fontSize = 9.sp)
            }
            Text(text = q.difficulty, style = MaterialTheme.typography.labelSmall, color = TextTertiary, fontSize = 10.sp)
          }

          Text(text = q.question, style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)

          if (q.formula != null) {
            Surface(
              color = Color(0xFF060B12),
              shape = RoundedCornerShape(6.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder.copy(alpha = 0.4f))
            ) {
              Text(
                text = "Formula: ${q.formula}",
                style = MaterialTheme.typography.labelSmall,
                color = LogicHighGreen,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(6.dp)
              )
            }
          }

          if (isRevealed) {
            Surface(
              color = SiliconCardDark,
              shape = RoundedCornerShape(8.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder)
            ) {
              Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                  text = "✓ Correct Answer: Option ${('A' + q.correctIndex)}",
                  style = MaterialTheme.typography.labelSmall,
                  color = LogicHighGreen,
                  fontWeight = FontWeight.Bold
                )
                Text(text = q.explanation, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "🎯 Hiring Manager Insight: ${q.hiringManagerSecret}",
                  style = MaterialTheme.typography.labelSmall,
                  color = ConductorGold,
                  fontSize = 11.sp
                )
              }
            }
          }

          Button(
            onClick = { isRevealed = !isRevealed },
            colors = ButtonDefaults.buttonColors(containerColor = if (isRevealed) SiliconCardElevated else NeonCyan),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(if (isRevealed) "Hide Explanation" else "Reveal Answer & Hiring Secret", color = if (isRevealed) TextPrimary else Color.Black, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

// ====================================================================
// SUB-TAB 2: WHITEBOARD CHALLENGE
// ====================================================================
@Composable
private fun WhiteboardChallengeTab() {
  var freqInput by remember { mutableStateOf("2.0") }
  var tcqInput by remember { mutableStateOf("0.25") }
  var tsetupInput by remember { mutableStateOf("0.15") }
  var skewInput by remember { mutableStateOf("0.05") }

  val maxCombDelay = remember(freqInput, tcqInput, tsetupInput, skewInput) {
    val f = freqInput.toFloatOrNull() ?: 2.0f
    val tcq = tcqInput.toFloatOrNull() ?: 0.25f
    val tsetup = tsetupInput.toFloatOrNull() ?: 0.15f
    val skew = skewInput.toFloatOrNull() ?: 0.05f
    val period = if (f > 0f) 1f / f else 0.5f // in ns
    (period + skew) - (tcq + tsetup)
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Surface(
        color = SiliconSurfaceDark,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder)
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(text = "Interactive Whiteboard: Maximum Logic Path Calculator", style = MaterialTheme.typography.titleSmall, color = NeonCyan, fontWeight = FontWeight.Bold)
          Text(
            text = "Derive maximum permissible combinational delay (t_comb_max) before setup violation occurs using the fundamental setup slack equation:",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
          )

          Surface(
            color = Color(0xFF040810),
            shape = RoundedCornerShape(6.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder)
          ) {
            Text(
              text = "t_comb_max = (T_period + T_skew) - (t_cq + t_setup)",
              color = ConductorGold,
              fontFamily = FontFamily.Monospace,
              style = MaterialTheme.typography.labelMedium,
              modifier = Modifier.padding(8.dp)
            )
          }

          // Inputs
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = freqInput,
              onValueChange = { freqInput = it },
              label = { Text("Freq (GHz)", fontSize = 10.sp) },
              modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
              value = tcqInput,
              onValueChange = { tcqInput = it },
              label = { Text("t_cq (ns)", fontSize = 10.sp) },
              modifier = Modifier.weight(1f)
            )
          }

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = tsetupInput,
              onValueChange = { tsetupInput = it },
              label = { Text("t_setup (ns)", fontSize = 10.sp) },
              modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
              value = skewInput,
              onValueChange = { skewInput = it },
              label = { Text("Skew (ns)", fontSize = 10.sp) },
              modifier = Modifier.weight(1f)
            )
          }

          // Output Result
          Surface(
            color = if (maxCombDelay > 0f) LogicHighGreen.copy(alpha = 0.15f) else SiliconErrorRed.copy(alpha = 0.15f),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (maxCombDelay > 0f) LogicHighGreen else SiliconErrorRed)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Max Allowed Comb Delay:",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
              )
              Text(
                text = "${String.format("%.3f", maxCombDelay)} ns",
                style = MaterialTheme.typography.titleMedium,
                color = if (maxCombDelay > 0f) LogicHighGreen else SiliconErrorRed,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }
      }
    }
  }
}
