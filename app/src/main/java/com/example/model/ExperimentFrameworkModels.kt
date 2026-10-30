package com.example.model

data class ExperimentParameter(
  val id: String,
  val name: String,
  val unit: String,
  val currentValue: Float,
  val minValue: Float,
  val maxValue: Float,
  val step: Float,
  val description: String
)

data class ExperimentRubricItem(
  val criterionName: String,
  val targetSpecification: String,
  val achievedValue: String,
  val isMet: Boolean,
  val engineerFeedback: String
)

data class VlsiExperimentDefinition(
  val id: String,
  val title: String,
  val domain: DomainType,
  val objective: String,
  val theoryMarkdown: String,
  val circuitTopologyDescription: String,
  val defaultParameters: List<ExperimentParameter>,
  val industryRelevance: String,
  val calculateTelemetry: (Map<String, Float>) -> Map<String, String>,
  val evaluateRubric: (Map<String, Float>) -> List<ExperimentRubricItem>
)
