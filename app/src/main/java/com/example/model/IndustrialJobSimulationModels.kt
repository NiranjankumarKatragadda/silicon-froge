package com.example.model

data class JobSimulationMilestone(
  val stepNumber: Int,
  val title: String,
  val departmentRole: String,
  val taskInstructions: String,
  val toolConsoleName: String,
  val liveTerminalPrompt: String,
  val parameters: List<ExperimentParameter>,
  val verificationRule: (Map<String, Float>) -> Pair<Boolean, String>,
  val isCompleted: Boolean = false
)

data class IndustrialJobWorkflow(
  val workflowId: String,
  val title: String,
  val targetCompany: String,
  val fabNode: String,
  val salaryLevel: String,
  val scenarioBrief: String,
  val milestones: List<JobSimulationMilestone>,
  val activeMilestoneIndex: Int = 0,
  val isSignedOff: Boolean = false
)
