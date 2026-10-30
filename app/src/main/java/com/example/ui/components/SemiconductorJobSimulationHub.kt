package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.JobApplicationEntity
import com.example.model.MarketTelemetrySummary
import com.example.model.SemiconductorJobPosting
import com.example.model.SemiconductorJobTrend
import com.example.ui.theme.*
import com.example.viewmodel.JobSimulationViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SemiconductorJobSimulationHub(
  modifier: Modifier = Modifier,
  jobViewModel: JobSimulationViewModel = viewModel()
) {
  val uiState by jobViewModel.uiState.collectAsStateWithLifecycle()

  var selectedJobForApplication by remember { mutableStateOf<SemiconductorJobPosting?>(null) }
  var selectedJobForDetails by remember { mutableStateOf<SemiconductorJobPosting?>(null) }
  var applicationSuccessFeedback by remember { mutableStateOf<JobApplicationEntity?>(null) }

  Column(
    modifier = modifier
      .fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // 1. Real-Time Industry Trends Telemetry Banner
    RealTimeHiringTelemetryBanner(
      telemetry = uiState.telemetry,
      isRefreshing = uiState.isRefreshing,
      onRefreshClick = { jobViewModel.refreshLiveTrends() }
    )

    // 2. Semiconductor Hiring Trends Breakdown Carousel
    SemiconductorTrendsCarousel(trends = uiState.jobTrends)

    // 3. Tab Switcher: Job Requisitions vs Tracked Applications (Room DB)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(SiliconSurfaceDark, RoundedCornerShape(12.dp))
        .border(1.dp, SiliconBorder, RoundedCornerShape(12.dp))
        .padding(4.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Surface(
        color = if (uiState.activeTab == 0) NeonCyan else Color.Transparent,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
          .weight(1f)
          .clickable { jobViewModel.setTab(0) }
          .testTag("tab_job_requisitions")
      ) {
        Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(
              imageVector = Icons.Default.Work,
              contentDescription = null,
              tint = if (uiState.activeTab == 0) Color.Black else TextSecondary,
              modifier = Modifier.size(16.dp)
            )
            Text(
              text = "Open Job Requisitions (${uiState.jobPostings.size})",
              color = if (uiState.activeTab == 0) Color.Black else TextPrimary,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp
            )
          }
        }
      }

      Surface(
        color = if (uiState.activeTab == 1) ConductorGold else Color.Transparent,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
          .weight(1f)
          .clickable { jobViewModel.setTab(1) }
          .testTag("tab_mock_applications")
      ) {
        Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(
              imageVector = Icons.Default.Assignment,
              contentDescription = null,
              tint = if (uiState.activeTab == 1) Color.Black else TextSecondary,
              modifier = Modifier.size(16.dp)
            )
            Text(
              text = "Tracked Applications (${uiState.applications.size})",
              color = if (uiState.activeTab == 1) Color.Black else TextPrimary,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp
            )
          }
        }
      }
    }

    // Success Banner when application submitted
    applicationSuccessFeedback?.let { app ->
      Surface(
        color = LogicHighGreen.copy(alpha = 0.15f),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, LogicHighGreen),
        modifier = Modifier.fillMaxWidth().testTag("app_success_banner")
      ) {
        Row(
          modifier = Modifier.padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Icon(Icons.Default.CheckCircle, contentDescription = null, tint = LogicHighGreen, modifier = Modifier.size(24.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Mock Application Submitted to ${app.company}!",
              style = MaterialTheme.typography.labelSmall,
              color = LogicHighGreen,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Assigned ID: ${app.id} • ATS Match Score: ${app.atsMatchScore}% (${app.status})",
              style = MaterialTheme.typography.bodySmall,
              color = TextPrimary,
              fontSize = 11.sp
            )
            Text(
              text = app.recruiterFeedback,
              style = MaterialTheme.typography.bodySmall,
              color = TextSecondary,
              fontSize = 10.sp
            )
          }
          IconButton(onClick = { applicationSuccessFeedback = null }, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = TextSecondary, modifier = Modifier.size(16.dp))
          }
        }
      }
    }

    // Body content based on active tab
    if (uiState.activeTab == 0) {
      // TAB 0: OPEN JOB REQUISITIONS & FILTERS
      JobRequisitionsContent(
        uiState = uiState,
        onSearchChange = { jobViewModel.setSearchQuery(it) },
        onDomainFilterSelect = { jobViewModel.setDomainFilter(it) },
        onApplyClick = { selectedJobForApplication = it },
        onDetailsClick = { selectedJobForDetails = it }
      )
    } else {
      // TAB 1: LOCAL ROOM DB TRACKED APPLICATIONS TABLE
      TrackedApplicationsContent(
        applications = uiState.applications,
        onAdvanceStatus = { id, currentStatus ->
          jobViewModel.advanceApplicationStatus(id, currentStatus)
        },
        onWithdraw = { app ->
          jobViewModel.withdrawApplication(app)
        }
      )
    }
  }

  // Mock Application Submission Dialog
  selectedJobForApplication?.let { job ->
    MockApplicationSubmissionDialog(
      jobPosting = job,
      onDismiss = { selectedJobForApplication = null },
      onSubmitApplication = { name, email, degree, gpa, portfolio, domain ->
        val submittedApp = jobViewModel.submitMockApplication(
          jobPosting = job,
          applicantName = name,
          applicantEmail = email,
          degreeLevel = degree,
          gpaOrGrade = gpa,
          portfolioSummary = portfolio,
          applicantDomain = domain
        )
        applicationSuccessFeedback = submittedApp
        selectedJobForApplication = null
      }
    )
  }

  // Job Details Dialog
  selectedJobForDetails?.let { job ->
    JobDetailsDialog(
      jobPosting = job,
      onDismiss = { selectedJobForDetails = null },
      onApply = {
        selectedJobForApplication = job
        selectedJobForDetails = null
      }
    )
  }
}

/**
 * Real-time semiconductor hiring telemetry banner with index metrics and refresh button.
 */
@Composable
private fun RealTimeHiringTelemetryBanner(
  telemetry: MarketTelemetrySummary,
  isRefreshing: Boolean,
  onRefreshClick: () -> Unit
) {
  val dateStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(telemetry.lastSyncTime))

  Surface(
    color = SiliconSurfaceDark,
    shape = RoundedCornerShape(14.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder),
    modifier = Modifier.fillMaxWidth().testTag("hiring_telemetry_banner")
  ) {
    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Box(
            modifier = Modifier
              .size(10.dp)
              .background(LogicHighGreen, CircleShape)
          )
          Text(
            text = "LIVE SEMICONDUCTOR INDUSTRY TELEMETRY",
            style = MaterialTheme.typography.labelSmall,
            color = LogicHighGreen,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
          )
        }

        OutlinedButton(
          onClick = onRefreshClick,
          enabled = !isRefreshing,
          colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
          border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f)),
          shape = RoundedCornerShape(8.dp),
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
          modifier = Modifier.height(28.dp).testTag("btn_refresh_trends")
        ) {
          Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(if (isRefreshing) "Syncing..." else "Sync Trends", fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
      }

      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
          modifier = Modifier
            .weight(1f)
            .background(SiliconCardDark, RoundedCornerShape(8.dp))
            .padding(8.dp)
        ) {
          Column {
            Text(text = "Silicon Hiring Index", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 9.sp)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              Text(
                text = "${String.format("%.1f", telemetry.currentIndexValue)} pts",
                style = MaterialTheme.typography.labelMedium,
                color = ConductorGold,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
              Text(
                text = "${if (telemetry.quarterGrowthPercentage >= 0) "+" else ""}${String.format("%.1f", telemetry.quarterGrowthPercentage)}%",
                style = MaterialTheme.typography.labelSmall,
                color = LogicHighGreen,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }

        Box(
          modifier = Modifier
            .weight(1f)
            .background(SiliconCardDark, RoundedCornerShape(8.dp))
            .padding(8.dp)
        ) {
          Column {
            Text(text = "Global Open Roles", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 9.sp)
            Text(
              text = "${telemetry.totalActiveRequisitions} Active",
              style = MaterialTheme.typography.labelMedium,
              color = NeonCyan,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }
        }

        Box(
          modifier = Modifier
            .weight(1.2f)
            .background(SiliconCardDark, RoundedCornerShape(8.dp))
            .padding(8.dp)
        ) {
          Column {
            Text(text = "Top Hiring Hub", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 9.sp)
            Text(
              text = telemetry.topGrowingRegion,
              style = MaterialTheme.typography.labelSmall,
              color = TextPrimary,
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }

      Text(
        text = "Last verified data pull: $dateStr • Pulling live hiring requisitions across TSMC, Intel, Apple, NVIDIA, and Qualcomm.",
        style = MaterialTheme.typography.labelSmall,
        color = TextTertiary,
        fontSize = 10.sp
      )
    }
  }
}

/**
 * Carousel showing top semiconductor domain hiring trends.
 */
@Composable
private fun SemiconductorTrendsCarousel(trends: List<SemiconductorJobTrend>) {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = ConductorGold, modifier = Modifier.size(16.dp))
        Text(
          text = "Domain Hiring Growth Trends (YoY)",
          style = MaterialTheme.typography.titleSmall,
          color = TextPrimary,
          fontWeight = FontWeight.Bold
        )
      }
      Text(text = "${trends.size} Sectors", style = MaterialTheme.typography.labelSmall, color = ConductorGold, fontSize = 10.sp)
    }

    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
      items(trends, key = { it.id }) { trend ->
        Surface(
          color = SiliconSurfaceDark,
          shape = RoundedCornerShape(12.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder),
          modifier = Modifier.width(280.dp)
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .background(
                    when (trend.demandRating) {
                      "CRITICAL SURGE" -> CleanroomAmber.copy(alpha = 0.2f)
                      "VERY HIGH DEMAND" -> LogicHighGreen.copy(alpha = 0.2f)
                      else -> NeonCyan.copy(alpha = 0.2f)
                    },
                    RoundedCornerShape(4.dp)
                  )
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(
                  text = trend.demandRating,
                  color = when (trend.demandRating) {
                    "CRITICAL SURGE" -> CleanroomAmber
                    "VERY HIGH DEMAND" -> LogicHighGreen
                    else -> NeonCyan
                  },
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  fontSize = 9.sp
                )
              }

              Text(
                text = "+${trend.hiringGrowthYoY}% YoY",
                style = MaterialTheme.typography.labelSmall,
                color = LogicHighGreen,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
            }

            Text(
              text = trend.domain,
              style = MaterialTheme.typography.titleSmall,
              color = TextPrimary,
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              Text(
                text = "Avg: $${trend.averageBaseSalaryUsd / 1000}k Base",
                style = MaterialTheme.typography.labelSmall,
                color = ConductorGold,
                fontFamily = FontFamily.Monospace
              )
              Text(
                text = "${trend.activeOpeningsGlobal} Roles",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
              )
            }

            Text(
              text = trend.keyTrendInsight,
              style = MaterialTheme.typography.bodySmall,
              color = TextSecondary,
              fontSize = 11.sp,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis
            )

            // Top hiring companies pill
            Text(
              text = "Top Hiring: ${trend.topHiringCompanies.take(3).joinToString(", ")}",
              style = MaterialTheme.typography.labelSmall,
              color = NeonCyan,
              fontSize = 10.sp
            )
          }
        }
      }
    }
  }
}

/**
 * Open job postings tab with search and filter.
 */
@Composable
private fun JobRequisitionsContent(
  uiState: com.example.viewmodel.JobSimulationUiState,
  onSearchChange: (String) -> Unit,
  onDomainFilterSelect: (String) -> Unit,
  onApplyClick: (SemiconductorJobPosting) -> Unit,
  onDetailsClick: (SemiconductorJobPosting) -> Unit
) {
  val filterDomains = listOf("All", "AI Hardware", "Physical Design", "Silicon Test", "Digital Logic")

  Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
    // Search Bar
    OutlinedTextField(
      value = uiState.searchQuery,
      onValueChange = onSearchChange,
      placeholder = { Text("Search by company (NVIDIA, Apple...), role, or fab node (3nm)...", fontSize = 12.sp) },
      leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp)) },
      singleLine = true,
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = NeonCyan,
        unfocusedBorderColor = SiliconBorder,
        focusedContainerColor = SiliconSurfaceDark,
        unfocusedContainerColor = SiliconSurfaceDark
      ),
      shape = RoundedCornerShape(10.dp),
      modifier = Modifier.fillMaxWidth().testTag("job_search_input")
    )

    // Filter Chips Row
    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
      items(filterDomains) { domain ->
        val isSel = uiState.selectedDomainFilter.equals(domain, ignoreCase = true)
        FilterChip(
          selected = isSel,
          onClick = { onDomainFilterSelect(domain) },
          label = { Text(domain, fontSize = 11.sp) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = NeonCyan,
            selectedLabelColor = Color.Black,
            containerColor = SiliconCardDark,
            labelColor = TextSecondary
          ),
          border = FilterChipDefaults.filterChipBorder(
            borderColor = SiliconBorder,
            selectedBorderColor = NeonCyan,
            enabled = true,
            selected = isSel
          )
        )
      }
    }

    // Job Posting Cards List
    if (uiState.jobPostings.isEmpty()) {
      Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Icon(Icons.Default.SearchOff, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(36.dp))
          Text("No job postings match your filter", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
      }
    } else {
      uiState.jobPostings.forEach { job ->
        JobPostingCard(
          job = job,
          onApplyClick = { onApplyClick(job) },
          onDetailsClick = { onDetailsClick(job) }
        )
      }
    }
  }
}

/**
 * Individual Job Posting Card with company tag, node, and mock apply action.
 */
@Composable
private fun JobPostingCard(
  job: SemiconductorJobPosting,
  onApplyClick: () -> Unit,
  onDetailsClick: () -> Unit
) {
  Surface(
    color = SiliconSurfaceDark,
    shape = RoundedCornerShape(12.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder),
    modifier = Modifier.fillMaxWidth().testTag("job_card_${job.id}")
  ) {
    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
              text = job.company,
              style = MaterialTheme.typography.labelSmall,
              color = ConductorGold,
              fontWeight = FontWeight.Bold
            )
            if (job.isTrendingRole) {
              Box(
                modifier = Modifier
                  .background(CleanroomAmber.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                  .padding(horizontal = 5.dp, vertical = 2.dp)
              ) {
                Text(text = "🔥 TRENDING", color = CleanroomAmber, fontSize = 8.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
          Text(
            text = job.jobTitle,
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
          )
        }

        Surface(
          color = NeonCyan.copy(alpha = 0.15f),
          shape = RoundedCornerShape(6.dp)
        ) {
          Text(
            text = job.targetFabNode,
            color = NeonCyan,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
          )
        }
      }

      Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(text = "📍 ${job.location}", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 11.sp)
        Text(text = "💰 ${job.salaryRange}", style = MaterialTheme.typography.labelSmall, color = LogicHighGreen, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
      }

      Text(
        text = job.description,
        style = MaterialTheme.typography.bodySmall,
        color = TextSecondary,
        fontSize = 11.sp,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )

      // EDA Tool Stack Chips
      LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        items(job.edaStack) { tool ->
          Box(
            modifier = Modifier
              .background(SiliconCardDark, RoundedCornerShape(4.dp))
              .border(1.dp, SiliconBorder, RoundedCornerShape(4.dp))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(text = tool, style = MaterialTheme.typography.labelSmall, color = TextTertiary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "${job.applicantCount} Applicants • ${job.experienceLevel}",
          style = MaterialTheme.typography.labelSmall,
          color = TextTertiary,
          fontSize = 10.sp
        )

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          OutlinedButton(
            onClick = onDetailsClick,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
            border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier.height(30.dp)
          ) {
            Text("Details", fontSize = 11.sp)
          }

          Button(
            onClick = onApplyClick,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            modifier = Modifier.height(30.dp).testTag("btn_apply_${job.id}")
          ) {
            Icon(Icons.Default.Send, contentDescription = null, tint = Color.Black, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Apply (Mock ATS)", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

/**
 * Tracked Mock Applications content backed by the local Room database table.
 */
@Composable
private fun TrackedApplicationsContent(
  applications: List<JobApplicationEntity>,
  onAdvanceStatus: (String, String) -> Unit,
  onWithdraw: (JobApplicationEntity) -> Unit
) {
  if (applications.isEmpty()) {
    Surface(
      color = SiliconSurfaceDark,
      shape = RoundedCornerShape(12.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Icon(Icons.Default.AssignmentLate, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(40.dp))
          Text(text = "No Mock Applications Yet", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
          Text(
            text = "Switch to the 'Open Job Requisitions' tab and click 'Apply (Mock ATS)' to simulate your candidate application submission.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )
        }
      }
    }
  } else {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
      // Summary Metrics Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        MetricSummaryCard(
          title = "Submitted",
          count = applications.size,
          color = NeonCyan,
          modifier = Modifier.weight(1f)
        )
        MetricSummaryCard(
          title = "Screen Passed",
          count = applications.count { it.status.contains("Passed", ignoreCase = true) },
          color = ConductorGold,
          modifier = Modifier.weight(1f)
        )
        MetricSummaryCard(
          title = "In Review / Interview",
          count = applications.count { it.status.contains("Assessment", ignoreCase = true) || it.status.contains("Interview", ignoreCase = true) },
          color = LogicHighGreen,
          modifier = Modifier.weight(1f)
        )
      }

      Text(
        text = "Tracked Local Applications (${applications.size} Records in Room DB)",
        style = MaterialTheme.typography.titleSmall,
        color = NeonCyan,
        fontWeight = FontWeight.Bold
      )

      // Application Cards List
      applications.forEach { app ->
        TrackedApplicationCard(
          application = app,
          onAdvanceStatus = { onAdvanceStatus(app.id, app.status) },
          onWithdraw = { onWithdraw(app) }
        )
      }
    }
  }
}

@Composable
private fun MetricSummaryCard(title: String, count: Int, color: Color, modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .background(SiliconSurfaceDark, RoundedCornerShape(8.dp))
      .border(1.dp, SiliconBorder, RoundedCornerShape(8.dp))
      .padding(8.dp)
  ) {
    Column {
      Text(text = title, style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 9.sp)
      Text(
        text = "$count",
        style = MaterialTheme.typography.titleMedium,
        color = color,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
      )
    }
  }
}

/**
 * Individual tracked mock application card with recruiter feedback and status progression.
 */
@Composable
private fun TrackedApplicationCard(
  application: JobApplicationEntity,
  onAdvanceStatus: () -> Unit,
  onWithdraw: () -> Unit
) {
  val dateStr = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(application.submissionTimestamp))

  val statusColor = when (application.status) {
    "Submitted" -> NeonCyan
    "ATS Screen Passed" -> ConductorGold
    "Technical Assessment" -> CleanroomAmber
    "Interview Offered" -> LogicHighGreen
    "Accepted" -> LogicHighGreen
    else -> TextSecondary
  }

  Surface(
    color = SiliconSurfaceDark,
    shape = RoundedCornerShape(12.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.5f)),
    modifier = Modifier.fillMaxWidth().testTag("app_card_${application.id}")
  ) {
    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(text = application.company, style = MaterialTheme.typography.labelSmall, color = ConductorGold, fontWeight = FontWeight.Bold)
          Text(text = application.jobTitle, style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
        }

        Box(
          modifier = Modifier
            .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
            .border(1.dp, statusColor.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = application.status.uppercase(),
            color = statusColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp
          )
        }
      }

      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
          text = "Candidate: ${application.applicantName} (${application.degreeLevel})",
          style = MaterialTheme.typography.bodySmall,
          color = TextSecondary,
          fontSize = 11.sp
        )
        Text(
          text = "ATS Match: ${application.atsMatchScore}%",
          style = MaterialTheme.typography.labelSmall,
          color = if (application.atsMatchScore >= 80) LogicHighGreen else ConductorGold,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
      }

      // Portfolio and fab node
      Text(
        text = "Portfolio: ${application.portfolioSummary} • Node: ${application.targetFabNode}",
        style = MaterialTheme.typography.bodySmall,
        color = TextTertiary,
        fontSize = 10.sp
      )

      // Recruiter / ATS Feedback Box
      if (application.recruiterFeedback.isNotBlank()) {
        Surface(
          color = Color(0xFF060B14),
          shape = RoundedCornerShape(6.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder.copy(alpha = 0.5f))
        ) {
          Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Default.Feedback, contentDescription = null, tint = ConductorGold, modifier = Modifier.size(14.dp))
            Text(
              text = "ATS / Recruiter Feedback: ${application.recruiterFeedback}",
              style = MaterialTheme.typography.bodySmall,
              color = TextSecondary,
              fontSize = 10.sp
            )
          }
        }
      }

      // Actions: Advance status simulation + Withdraw
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(text = "Submitted: $dateStr", style = MaterialTheme.typography.labelSmall, color = TextTertiary, fontSize = 9.sp)

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          TextButton(
            onClick = onWithdraw,
            colors = ButtonDefaults.textButtonColors(contentColor = SiliconErrorRed),
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text("Withdraw", fontSize = 10.sp)
          }

          if (application.status != "Accepted") {
            Button(
              onClick = onAdvanceStatus,
              colors = ButtonDefaults.buttonColors(containerColor = statusColor),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
              shape = RoundedCornerShape(6.dp),
              modifier = Modifier.height(28.dp).testTag("btn_advance_${application.id}")
            ) {
              Text("Simulate Next Stage", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}

/**
 * Interactive Mock Application Submission Dialog.
 */
@Composable
private fun MockApplicationSubmissionDialog(
  jobPosting: SemiconductorJobPosting,
  onDismiss: () -> Unit,
  onSubmitApplication: (String, String, String, String, String, String) -> Unit
) {
  var name by remember { mutableStateOf("Alex Chen") }
  var email by remember { mutableStateOf("alex.chen@stanford.edu") }
  var degree by remember { mutableStateOf("M.S. VLSI & Computer Engineering") }
  var gpa by remember { mutableStateOf("3.88") }
  var portfolio by remember { mutableStateOf("Designed 4-bit Configurable ALU and Aegis-3X 3nm TPU with full STA timing closure") }
  var domain by remember { mutableStateOf(jobPosting.domain) }

  val degrees = listOf(
    "B.S. Electrical / ECE",
    "M.S. VLSI & Computer Engineering",
    "Ph.D. Microelectronics & Silicon Physics"
  )

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
          modifier = Modifier
            .size(32.dp)
            .background(NeonCyan.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.Send, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
        }
        Column {
          Text(text = "Submit Mock Application", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
          Text(text = "${jobPosting.company} • ${jobPosting.jobTitle}", style = MaterialTheme.typography.labelSmall, color = ConductorGold, fontSize = 10.sp)
        }
      }
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Candidate Full Name") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("app_name_input")
        )

        OutlinedTextField(
          value = email,
          onValueChange = { email = it },
          label = { Text("University / Contact Email") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("app_email_input")
        )

        // Degree selector
        Text(text = "Degree Level", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          items(degrees) { deg ->
            val isSel = degree == deg
            FilterChip(
              selected = isSel,
              onClick = { degree = deg },
              label = { Text(deg, fontSize = 10.sp) }
            )
          }
        }

        OutlinedTextField(
          value = gpa,
          onValueChange = { gpa = it },
          label = { Text("GPA / Academic Standing (out of 4.0)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("app_gpa_input")
        )

        OutlinedTextField(
          value = portfolio,
          onValueChange = { portfolio = it },
          label = { Text("Silicon Portfolio & Tapeout Summary") },
          maxLines = 3,
          modifier = Modifier.fillMaxWidth().testTag("app_portfolio_input")
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank() && email.isNotBlank()) {
            onSubmitApplication(name, email, degree, gpa, portfolio, domain)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
        modifier = Modifier.testTag("btn_confirm_submit_app")
      ) {
        Text("Submit to ATS & Save to Room", color = Color.Black, fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = TextSecondary)
      }
    },
    containerColor = SiliconSurfaceDark
  )
}

/**
 * Detailed Job Requisition View Modal.
 */
@Composable
private fun JobDetailsDialog(
  jobPosting: SemiconductorJobPosting,
  onDismiss: () -> Unit,
  onApply: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Column {
        Text(text = jobPosting.company, style = MaterialTheme.typography.labelSmall, color = ConductorGold, fontWeight = FontWeight.Bold)
        Text(text = jobPosting.jobTitle, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
      }
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(text = "📍 ${jobPosting.location}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
          Text(text = "💰 ${jobPosting.salaryRange}", style = MaterialTheme.typography.labelSmall, color = LogicHighGreen, fontFamily = FontFamily.Monospace)
        }

        Text(text = "Target Process Node: ${jobPosting.targetFabNode}", style = MaterialTheme.typography.labelSmall, color = NeonCyan, fontWeight = FontWeight.Bold)

        Text(text = jobPosting.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)

        Text(text = "Core Responsibilities:", style = MaterialTheme.typography.labelSmall, color = ConductorGold, fontWeight = FontWeight.Bold)
        jobPosting.responsibilities.forEach { r ->
          Text(text = "• $r", style = MaterialTheme.typography.bodySmall, color = TextSecondary, fontSize = 11.sp)
        }

        Text(text = "Minimum Qualifications:", style = MaterialTheme.typography.labelSmall, color = ConductorGold, fontWeight = FontWeight.Bold)
        jobPosting.minimumQualifications.forEach { q ->
          Text(text = "• $q", style = MaterialTheme.typography.bodySmall, color = TextSecondary, fontSize = 11.sp)
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onApply,
        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
      ) {
        Text("Apply Now", color = Color.Black, fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Close", color = TextSecondary)
      }
    },
    containerColor = SiliconSurfaceDark
  )
}
