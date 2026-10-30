package com.example.model

data class EngineerPersona(
  val id: String,
  val name: String,
  val title: String,
  val companyBadge: String,
  val yearsExperience: Int,
  val bio: String,
  val specialty: DomainType,
  val interviewStyle: String
)

data class InterviewQuestion(
  val id: String,
  val domain: DomainType,
  val difficulty: String, // "Foundational", "Senior ASIC", "Principal Architect"
  val question: String,
  val circuitAscii: String? = null,
  val options: List<String>,
  val correctIndex: Int,
  val explanation: String,
  val hiringManagerSecret: String,
  val formula: String? = null
)

data class DirectInterviewTurn(
  val turnNumber: Int,
  val questionText: String,
  val contextBrief: String,
  val candidateOptions: List<String>,
  val bestOptionIndex: Int,
  val engineerFeedbackMap: Map<Int, String>,
  val hiringTip: String
)

data class InterviewSessionState(
  val persona: EngineerPersona,
  val turns: List<DirectInterviewTurn>,
  val currentTurnIndex: Int = 0,
  val selectedAnswers: MutableMap<Int, Int> = mutableMapOf(),
  val isFinished: Boolean = false,
  val totalScore: Int = 0
)
