package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.VlsiExperimentEntity
import com.example.model.DomainType
import com.example.ui.theme.*
import com.example.viewmodel.CatalogFilterMode
import com.example.viewmodel.SiliconLabViewModel
import com.example.viewmodel.VlsiExperimentCatalogViewModel

/**
 * Main Dashboard UI organizing available virtual VLSI lab experiments
 * into categorized sections based on the existing Room database schema.
 */
@Composable
fun ExperimentDashboardView(
  labViewModel: SiliconLabViewModel,
  catalogViewModel: VlsiExperimentCatalogViewModel = viewModel(),
  modifier: Modifier = Modifier
) {
  val state by catalogViewModel.uiState.collectAsStateWithLifecycle()

  var selectedExperimentForDetail by remember { mutableStateOf<VlsiExperimentEntity?>(null) }
  var showAddDialog by remember { mutableStateOf(false) }
  var selectedDifficultyFilter by remember { mutableStateOf<String?>(null) } // null = All, "Beginner", "Intermediate", "Advanced"

  // Base list filtered by difficulty if chosen
  val displayedExperiments = remember(state.experiments, selectedDifficultyFilter) {
    if (selectedDifficultyFilter == null) {
      state.experiments
    } else {
      state.experiments.filter { it.difficulty.equals(selectedDifficultyFilter, ignoreCase = true) }
    }
  }

  // Categorize experiments by domain according to Room schema
  val domainCategories = remember(displayedExperiments) {
    val predefinedOrder = listOf("CMOS", "Digital Logic", "Layout Design", "Wafer Fab", "Silicon Test")
    val grouped = displayedExperiments.groupBy { it.domain }
    val orderedKeys = predefinedOrder.filter { grouped.containsKey(it) } +
        grouped.keys.filterNot { predefinedOrder.contains(it) }.sorted()
    orderedKeys.map { domainKey ->
      domainKey to (grouped[domainKey] ?: emptyList())
    }
  }

  // Favorite / In-Progress experiments for quick jump tray
  val bookmarkedOrActive = remember(state.experiments) {
    state.experiments.filter { it.isFavorite || (!it.isCompleted && it.durationMinutes > 0) }.take(6)
  }

  Column(
    modifier = modifier
      .fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Dashboard Mastery & Progress Summary Card
    LaboratoryMasteryCard(
      totalCount = state.totalExperimentsCount,
      completedCount = state.completedExperimentsCount,
      allExperiments = state.experiments,
      onAddCustomLabClick = { showAddDialog = true }
    )

    // 2. Search & Filter Bar
    DashboardFilterToolbar(
      searchQuery = state.searchQuery,
      onSearchQueryChange = { catalogViewModel.setSearchQuery(it) },
      filterMode = state.filterMode,
      onFilterModeSelect = { catalogViewModel.setFilterMode(it) },
      selectedDifficulty = selectedDifficultyFilter,
      onDifficultySelect = { selectedDifficultyFilter = it }
    )

    // 3. Quick-Access Bookmarked / In-Progress Tray
    if (bookmarkedOrActive.isNotEmpty() && state.searchQuery.isBlank()) {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(
              imageVector = Icons.Default.Bookmark,
              contentDescription = null,
              tint = ConductorGold,
              modifier = Modifier.size(16.dp)
            )
            Text(
              text = "Bookmarked & Recommended Experiments",
              style = MaterialTheme.typography.titleSmall,
              color = TextPrimary,
              fontWeight = FontWeight.Bold
            )
          }
          Text(
            text = "${bookmarkedOrActive.size} saved",
            style = MaterialTheme.typography.labelSmall,
            color = ConductorGold,
            fontFamily = FontFamily.Monospace
          )
        }

        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
          items(bookmarkedOrActive, key = { it.id }) { exp ->
            QuickTrayExperimentCard(
              experiment = exp,
              onCardClick = { selectedExperimentForDetail = exp },
              onLaunchLab = { launchLabForExperiment(exp, labViewModel) },
              onToggleFavorite = { catalogViewModel.toggleFavorite(exp.id) }
            )
          }
        }
      }
    }

    // 4. Categorized Sections based on Room DB Schema
    if (domainCategories.isEmpty()) {
      EmptyDomainSearchResults(
        query = state.searchQuery,
        selectedDomain = state.selectedDomain,
        onClearFilter = {
          catalogViewModel.setSearchQuery("")
          catalogViewModel.selectDomain("All")
          selectedDifficultyFilter = null
        }
      )
    } else {
      domainCategories.forEach { (domain, experiments) ->
        CategorizedDomainSection(
          domain = domain,
          experiments = experiments,
          onExperimentClick = { selectedExperimentForDetail = it },
          onLaunchLab = { launchLabForExperiment(it, labViewModel) },
          onToggleFavorite = { catalogViewModel.toggleFavorite(it.id) },
          onToggleCompleted = { catalogViewModel.toggleCompletion(it.id, it.isCompleted) },
          onLaunchDomainWorkbench = { launchWorkbenchForDomain(domain, labViewModel) }
        )
      }
    }
  }

  // Detailed Experiment Inspector Dialog / BottomSheet
  selectedExperimentForDetail?.let { experiment ->
    ExperimentDetailDialog(
      experiment = experiment,
      onDismiss = { selectedExperimentForDetail = null },
      onToggleFavorite = {
        catalogViewModel.toggleFavorite(experiment.id)
        selectedExperimentForDetail = experiment.copy(isFavorite = !experiment.isFavorite)
      },
      onToggleCompleted = {
        catalogViewModel.toggleCompletion(experiment.id, experiment.isCompleted)
        selectedExperimentForDetail = experiment.copy(isCompleted = !experiment.isCompleted)
      },
      onLaunchLab = {
        launchLabForExperiment(experiment, labViewModel)
        selectedExperimentForDetail = null
      }
    )
  }

  // Add Custom Experiment Dialog
  if (showAddDialog) {
    AddExperimentDialog(
      onDismiss = { showAddDialog = false },
      onConfirm = { title, domain, desc, formula, diff, dur, obj, app ->
        catalogViewModel.addCustomExperiment(title, domain, desc, formula, diff, dur, obj, app)
        showAddDialog = false
      }
    )
  }
}

/**
 * Header Card displaying overall student completion progress across all Room experiments.
 */
@Composable
private fun LaboratoryMasteryCard(
  totalCount: Int,
  completedCount: Int,
  allExperiments: List<VlsiExperimentEntity>,
  onAddCustomLabClick: () -> Unit
) {
  val completionRate = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f
  val totalMinutes = allExperiments.sumOf { it.durationMinutes }
  val completedMinutes = allExperiments.filter { it.isCompleted }.sumOf { it.durationMinutes }

  Surface(
    color = SiliconSurfaceDark,
    shape = RoundedCornerShape(16.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("lab_mastery_card")
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .background(NeonCyan.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
              .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Science,
              contentDescription = null,
              tint = NeonCyan,
              modifier = Modifier.size(20.dp)
            )
          }

          Column {
            Text(
              text = "VLSI VIRTUAL LAB BENCH",
              style = MaterialTheme.typography.labelSmall,
              color = NeonCyan,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
              letterSpacing = 1.sp
            )
            Text(
              text = "Experiment Mastery Dashboard",
              style = MaterialTheme.typography.titleMedium,
              color = TextPrimary,
              fontWeight = FontWeight.Bold
            )
          }
        }

        IconButton(
          onClick = onAddCustomLabClick,
          modifier = Modifier
            .background(SiliconCardDark, CircleShape)
            .border(1.dp, ConductorGold.copy(alpha = 0.6f), CircleShape)
            .size(36.dp)
            .testTag("btn_add_lab_quick")
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Lab",
            tint = ConductorGold,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      // Progress bar with metrics
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Total Syllabus Progress: ${(completionRate * 100).toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
          )
          Text(
            text = "$completedCount of $totalCount Done (${completedMinutes}/${totalMinutes} min)",
            style = MaterialTheme.typography.labelSmall,
            color = LogicHighGreen,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
          )
        }

        LinearProgressIndicator(
          progress = { completionRate },
          color = LogicHighGreen,
          trackColor = SiliconBorder.copy(alpha = 0.6f),
          modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
        )
      }

      // Domain quick pill status row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        DomainProgressPill(
          domain = "CMOS",
          color = ConductorGold,
          completed = allExperiments.count { it.domain.equals("CMOS", ignoreCase = true) && it.isCompleted },
          total = allExperiments.count { it.domain.equals("CMOS", ignoreCase = true) },
          modifier = Modifier.weight(1f)
        )
        DomainProgressPill(
          domain = "Digital",
          color = NeonCyan,
          completed = allExperiments.count { it.domain.equals("Digital Logic", ignoreCase = true) && it.isCompleted },
          total = allExperiments.count { it.domain.equals("Digital Logic", ignoreCase = true) },
          modifier = Modifier.weight(1f)
        )
        DomainProgressPill(
          domain = "Layout",
          color = Color(0xFFA855F7),
          completed = allExperiments.count { it.domain.equals("Layout Design", ignoreCase = true) && it.isCompleted },
          total = allExperiments.count { it.domain.equals("Layout Design", ignoreCase = true) },
          modifier = Modifier.weight(1f)
        )
        DomainProgressPill(
          domain = "Fab",
          color = CleanroomAmber,
          completed = allExperiments.count { it.domain.equals("Wafer Fab", ignoreCase = true) && it.isCompleted },
          total = allExperiments.count { it.domain.equals("Wafer Fab", ignoreCase = true) },
          modifier = Modifier.weight(1f)
        )
        DomainProgressPill(
          domain = "Test",
          color = LogicHighGreen,
          completed = allExperiments.count { it.domain.equals("Silicon Test", ignoreCase = true) && it.isCompleted },
          total = allExperiments.count { it.domain.equals("Silicon Test", ignoreCase = true) },
          modifier = Modifier.weight(1f)
        )
      }
    }
  }
}

@Composable
private fun DomainProgressPill(
  domain: String,
  color: Color,
  completed: Int,
  total: Int,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .background(SiliconCardDark, RoundedCornerShape(8.dp))
      .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
      .padding(vertical = 6.dp, horizontal = 4.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
        text = domain,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp
      )
      Text(
        text = "$completed/$total",
        style = MaterialTheme.typography.labelSmall,
        color = if (completed == total && total > 0) LogicHighGreen else TextSecondary,
        fontFamily = FontFamily.Monospace,
        fontSize = 9.sp
      )
    }
  }
}

/**
 * Search & Filters bar with difficulty selectors and completion modes.
 */
@Composable
private fun DashboardFilterToolbar(
  searchQuery: String,
  onSearchQueryChange: (String) -> Unit,
  filterMode: CatalogFilterMode,
  onFilterModeSelect: (CatalogFilterMode) -> Unit,
  selectedDifficulty: String?,
  onDifficultySelect: (String?) -> Unit
) {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    // Search input
    OutlinedTextField(
      value = searchQuery,
      onValueChange = onSearchQueryChange,
      placeholder = {
        Text("Search experiments by title, formula, or company application...", fontSize = 12.sp, color = TextTertiary)
      },
      leadingIcon = {
        Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
      },
      trailingIcon = {
        if (searchQuery.isNotBlank()) {
          IconButton(onClick = { onSearchQueryChange("") }) {
            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary, modifier = Modifier.size(16.dp))
          }
        }
      },
      singleLine = true,
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = NeonCyan,
        unfocusedBorderColor = SiliconBorder,
        focusedContainerColor = SiliconSurfaceDark,
        unfocusedContainerColor = SiliconSurfaceDark,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary
      ),
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("dashboard_search_input")
    )

    // Filter Chips Row
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
      // 1. Completion modes
      items(CatalogFilterMode.values()) { mode ->
        val isSel = filterMode == mode
        FilterChip(
          selected = isSel,
          onClick = { onFilterModeSelect(mode) },
          label = {
            Text(
              text = mode.label,
              fontSize = 11.sp,
              fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
            )
          },
          leadingIcon = if (mode == CatalogFilterMode.FAVORITES) {
            { Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(12.dp)) }
          } else if (mode == CatalogFilterMode.COMPLETED) {
            { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(12.dp)) }
          } else null,
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = NeonCyan,
            selectedLabelColor = Color.Black,
            selectedLeadingIconColor = Color.Black,
            containerColor = SiliconCardDark,
            labelColor = TextSecondary
          ),
          border = FilterChipDefaults.filterChipBorder(
            borderColor = SiliconBorder,
            selectedBorderColor = NeonCyan,
            enabled = true,
            selected = isSel
          ),
          modifier = Modifier.testTag("filter_chip_${mode.name.lowercase()}")
        )
      }

      // 2. Difficulty filters
      item {
        val diffs = listOf("Beginner", "Intermediate", "Advanced")
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          diffs.forEach { diff ->
            val isDiffSel = selectedDifficulty == diff
            FilterChip(
              selected = isDiffSel,
              onClick = {
                onDifficultySelect(if (isDiffSel) null else diff)
              },
              label = {
                Text(
                  text = diff,
                  fontSize = 11.sp,
                  fontWeight = if (isDiffSel) FontWeight.Bold else FontWeight.Normal
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = when (diff) {
                  "Beginner" -> LogicHighGreen
                  "Intermediate" -> ConductorGold
                  else -> CleanroomAmber
                },
                selectedLabelColor = Color.Black,
                containerColor = SiliconCardDark,
                labelColor = TextSecondary
              ),
              border = FilterChipDefaults.filterChipBorder(
                borderColor = SiliconBorder,
                selectedBorderColor = ConductorGold,
                enabled = true,
                selected = isDiffSel
              ),
              modifier = Modifier.testTag("diff_chip_${diff.lowercase()}")
            )
          }
        }
      }
    }
  }
}

/**
 * Compact Quick Tray card for bookmarked or in-progress experiments.
 */
@Composable
private fun QuickTrayExperimentCard(
  experiment: VlsiExperimentEntity,
  onCardClick: () -> Unit,
  onLaunchLab: () -> Unit,
  onToggleFavorite: () -> Unit
) {
  val domainColor = getDomainColor(experiment.domain)

  Surface(
    color = SiliconSurfaceDark,
    shape = RoundedCornerShape(12.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, domainColor.copy(alpha = 0.5f)),
    modifier = Modifier
      .width(260.dp)
      .clickable { onCardClick() }
      .testTag("quick_tray_card_${experiment.id}")
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .background(domainColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(
            text = experiment.domain,
            color = domainColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp
          )
        }

        IconButton(
          onClick = onToggleFavorite,
          modifier = Modifier.size(24.dp)
        ) {
          Icon(
            imageVector = if (experiment.isFavorite) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
            contentDescription = "Bookmark",
            tint = if (experiment.isFavorite) ConductorGold else TextSecondary,
            modifier = Modifier.size(16.dp)
          )
        }
      }

      Text(
        text = experiment.title,
        style = MaterialTheme.typography.bodyMedium,
        color = TextPrimary,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )

      Text(
        text = experiment.description,
        style = MaterialTheme.typography.bodySmall,
        color = TextSecondary,
        fontSize = 11.sp,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "${experiment.durationMinutes} min • ${experiment.difficulty}",
          style = MaterialTheme.typography.labelSmall,
          color = TextTertiary,
          fontSize = 10.sp
        )

        Button(
          onClick = onLaunchLab,
          colors = ButtonDefaults.buttonColors(containerColor = domainColor),
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
          shape = RoundedCornerShape(6.dp),
          modifier = Modifier.height(26.dp)
        ) {
          Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(12.dp))
          Spacer(modifier = Modifier.width(2.dp))
          Text("Launch", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

/**
 * Categorized Section for a single domain from the Room database.
 */
@Composable
private fun CategorizedDomainSection(
  domain: String,
  experiments: List<VlsiExperimentEntity>,
  onExperimentClick: (VlsiExperimentEntity) -> Unit,
  onLaunchLab: (VlsiExperimentEntity) -> Unit,
  onToggleFavorite: (VlsiExperimentEntity) -> Unit,
  onToggleCompleted: (VlsiExperimentEntity) -> Unit,
  onLaunchDomainWorkbench: () -> Unit
) {
  val domainColor = getDomainColor(domain)
  val domainIcon = getDomainIcon(domain)
  val domainBadge = getDomainBadge(domain)

  val completedCount = experiments.count { it.isCompleted }

  Surface(
    color = SiliconSurfaceDark,
    shape = RoundedCornerShape(16.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("domain_section_${domain.lowercase().replace(" ", "_")}")
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Domain Section Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .background(domainColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
              .border(1.dp, domainColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = domainIcon,
              contentDescription = null,
              tint = domainColor,
              modifier = Modifier.size(20.dp)
            )
          }

          Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Text(
                text = getDomainFullName(domain),
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
              )
              Surface(
                color = domainColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(4.dp)
              ) {
                Text(
                  text = "${experiments.size} Labs",
                  color = domainColor,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  fontSize = 9.sp,
                  modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                )
              }
            }

            Text(
              text = "$domainBadge • $completedCount/${experiments.size} Completed",
              style = MaterialTheme.typography.labelSmall,
              color = if (completedCount == experiments.size && experiments.isNotEmpty()) LogicHighGreen else TextSecondary,
              fontSize = 11.sp
            )
          }
        }

        // Quick Launch Domain Lab Action
        OutlinedButton(
          onClick = onLaunchDomainWorkbench,
          colors = ButtonDefaults.outlinedButtonColors(contentColor = domainColor),
          border = androidx.compose.foundation.BorderStroke(1.dp, domainColor.copy(alpha = 0.6f)),
          shape = RoundedCornerShape(8.dp),
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
          modifier = Modifier.height(30.dp)
        ) {
          Text("Open Lab", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.width(4.dp))
          Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(14.dp))
        }
      }

      // Horizontal Row of Experiment Cards for this domain
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
      ) {
        items(experiments, key = { it.id }) { exp ->
          DashboardExperimentCard(
            experiment = exp,
            domainColor = domainColor,
            onClick = { onExperimentClick(exp) },
            onLaunchLab = { onLaunchLab(exp) },
            onToggleFavorite = { onToggleFavorite(exp) },
            onToggleCompleted = { onToggleCompleted(exp) }
          )
        }
      }
    }
  }
}

/**
 * Rich interactive card for each experiment inside a categorized section.
 */
@Composable
private fun DashboardExperimentCard(
  experiment: VlsiExperimentEntity,
  domainColor: Color,
  onClick: () -> Unit,
  onLaunchLab: () -> Unit,
  onToggleFavorite: () -> Unit,
  onToggleCompleted: () -> Unit
) {
  val diffColor = when (experiment.difficulty) {
    "Beginner" -> LogicHighGreen
    "Intermediate" -> ConductorGold
    else -> CleanroomAmber
  }

  Surface(
    color = SiliconCardDark,
    shape = RoundedCornerShape(12.dp),
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      if (experiment.isCompleted) LogicHighGreen.copy(alpha = 0.6f) else SiliconBorder
    ),
    modifier = Modifier
      .width(290.dp)
      .clickable { onClick() }
      .testTag("dashboard_exp_card_${experiment.id}")
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Top row: Chips & Actions
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          // Difficulty badge
          Box(
            modifier = Modifier
              .background(diffColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = experiment.difficulty,
              color = diffColor,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              fontSize = 9.sp
            )
          }

          // Duration badge
          Box(
            modifier = Modifier
              .background(SiliconBorder.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = "${experiment.durationMinutes} min",
              color = TextSecondary,
              style = MaterialTheme.typography.labelSmall,
              fontSize = 9.sp,
              fontFamily = FontFamily.Monospace
            )
          }
        }

        // Bookmark & Completed icons
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
          IconButton(
            onClick = onToggleFavorite,
            modifier = Modifier.size(28.dp).testTag("fav_btn_${experiment.id}")
          ) {
            Icon(
              imageVector = if (experiment.isFavorite) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
              contentDescription = "Bookmark",
              tint = if (experiment.isFavorite) ConductorGold else TextSecondary,
              modifier = Modifier.size(16.dp)
            )
          }

          IconButton(
            onClick = onToggleCompleted,
            modifier = Modifier.size(28.dp).testTag("complete_btn_${experiment.id}")
          ) {
            Icon(
              imageVector = if (experiment.isCompleted) Icons.Default.CheckCircle else Icons.Outlined.CheckCircle,
              contentDescription = "Mark Complete",
              tint = if (experiment.isCompleted) LogicHighGreen else TextSecondary,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }

      // Title
      Text(
        text = experiment.title,
        style = MaterialTheme.typography.titleSmall,
        color = TextPrimary,
        fontWeight = FontWeight.Bold,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )

      // Description
      Text(
        text = experiment.description,
        style = MaterialTheme.typography.bodySmall,
        color = TextSecondary,
        fontSize = 11.sp,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )

      // Theoretical Formula Monospace Snippet
      Surface(
        color = Color(0xFF060A14),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = "ƒ: ${experiment.theoreticalFormula}",
          color = LogicHighGreen,
          fontFamily = FontFamily.Monospace,
          fontSize = 10.sp,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
        )
      }

      // Industry Tag
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Work,
          contentDescription = null,
          tint = ConductorGold,
          modifier = Modifier.size(12.dp)
        )
        Text(
          text = experiment.industryApplication,
          style = MaterialTheme.typography.labelSmall,
          color = ConductorGold,
          fontSize = 10.sp,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      // Action row: Inspect Details + Launch Lab
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        OutlinedButton(
          onClick = onClick,
          shape = RoundedCornerShape(8.dp),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
          border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder),
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
          modifier = Modifier.weight(1f).height(30.dp)
        ) {
          Text("Inspect", fontSize = 11.sp)
        }

        Button(
          onClick = onLaunchLab,
          shape = RoundedCornerShape(8.dp),
          colors = ButtonDefaults.buttonColors(containerColor = domainColor),
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
          modifier = Modifier.weight(1.2f).height(30.dp).testTag("launch_lab_${experiment.id}")
        ) {
          Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(3.dp))
          Text("Start Lab", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

/**
 * Detailed Experiment Inspector Dialog.
 */
@Composable
private fun ExperimentDetailDialog(
  experiment: VlsiExperimentEntity,
  onDismiss: () -> Unit,
  onToggleFavorite: () -> Unit,
  onToggleCompleted: () -> Unit,
  onLaunchLab: () -> Unit
) {
  val domainColor = getDomainColor(experiment.domain)

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Box(
            modifier = Modifier
              .size(32.dp)
              .background(domainColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
          ) {
            Icon(getDomainIcon(experiment.domain), contentDescription = null, tint = domainColor, modifier = Modifier.size(18.dp))
          }
          Column {
            Text(
              text = experiment.domain,
              style = MaterialTheme.typography.labelSmall,
              color = domainColor,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Experiment Inspector",
              style = MaterialTheme.typography.labelSmall,
              color = TextSecondary,
              fontSize = 10.sp
            )
          }
        }

        Row {
          IconButton(onClick = onToggleFavorite, modifier = Modifier.size(30.dp)) {
            Icon(
              imageVector = if (experiment.isFavorite) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
              contentDescription = "Bookmark",
              tint = if (experiment.isFavorite) ConductorGold else TextSecondary,
              modifier = Modifier.size(18.dp)
            )
          }
          IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary, modifier = Modifier.size(18.dp))
          }
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(
          text = experiment.title,
          style = MaterialTheme.typography.titleMedium,
          color = TextPrimary,
          fontWeight = FontWeight.Bold
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Surface(
            color = SiliconBorder.copy(alpha = 0.5f),
            shape = RoundedCornerShape(4.dp)
          ) {
            Text(
              text = "Duration: ${experiment.durationMinutes} minutes",
              color = TextSecondary,
              style = MaterialTheme.typography.labelSmall,
              fontSize = 10.sp,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }

          Surface(
            color = when (experiment.difficulty) {
              "Beginner" -> LogicHighGreen.copy(alpha = 0.2f)
              "Intermediate" -> ConductorGold.copy(alpha = 0.2f)
              else -> CleanroomAmber.copy(alpha = 0.2f)
            },
            shape = RoundedCornerShape(4.dp)
          ) {
            Text(
              text = "Difficulty: ${experiment.difficulty}",
              color = when (experiment.difficulty) {
                "Beginner" -> LogicHighGreen
                "Intermediate" -> ConductorGold
                else -> CleanroomAmber
              },
              style = MaterialTheme.typography.labelSmall,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }

        Text(
          text = experiment.description,
          style = MaterialTheme.typography.bodySmall,
          color = TextSecondary
        )

        // Formula section
        Text(
          text = "Theoretical Derivation & Formula",
          style = MaterialTheme.typography.labelSmall,
          color = NeonCyan,
          fontWeight = FontWeight.Bold
        )
        Surface(
          color = Color(0xFF040812),
          shape = RoundedCornerShape(8.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder)
        ) {
          Text(
            text = experiment.theoreticalFormula,
            color = LogicHighGreen,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            modifier = Modifier.padding(8.dp)
          )
        }

        // Objectives
        Text(
          text = "Key Learning Objectives",
          style = MaterialTheme.typography.labelSmall,
          color = ConductorGold,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "• ${experiment.keyLearningObjectives}",
          style = MaterialTheme.typography.bodySmall,
          color = TextSecondary
        )

        // Industry relevance
        Text(
          text = "Semiconductor Industry Application",
          style = MaterialTheme.typography.labelSmall,
          color = ConductorGold,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = experiment.industryApplication,
          style = MaterialTheme.typography.bodySmall,
          color = TextSecondary
        )
      }
    },
    confirmButton = {
      Button(
        onClick = onLaunchLab,
        colors = ButtonDefaults.buttonColors(containerColor = domainColor)
      ) {
        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Launch Interactive Lab", color = Color.Black, fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      OutlinedButton(
        onClick = onToggleCompleted,
        colors = ButtonDefaults.outlinedButtonColors(
          contentColor = if (experiment.isCompleted) LogicHighGreen else TextSecondary
        ),
        border = androidx.compose.foundation.BorderStroke(
          1.dp,
          if (experiment.isCompleted) LogicHighGreen else SiliconBorder
        )
      ) {
        Icon(
          imageVector = if (experiment.isCompleted) Icons.Default.CheckCircle else Icons.Outlined.CheckCircle,
          contentDescription = null,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(if (experiment.isCompleted) "Completed" else "Mark Done")
      }
    },
    containerColor = SiliconSurfaceDark
  )
}

/**
 * Empty search results state.
 */
@Composable
private fun EmptyDomainSearchResults(
  query: String,
  selectedDomain: String,
  onClearFilter: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 32.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Icon(Icons.Default.SearchOff, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(40.dp))
      Text(
        text = "No experiments found matching '$query'",
        style = MaterialTheme.typography.titleSmall,
        color = TextPrimary
      )
      Text(
        text = "Try adjusting your search keywords, difficulty filter, or domain category.",
        style = MaterialTheme.typography.bodySmall,
        color = TextSecondary
      )
      Button(
        onClick = onClearFilter,
        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
      ) {
        Text("Reset Filters", color = Color.Black, fontWeight = FontWeight.Bold)
      }
    }
  }
}

/**
 * Helper dialog for students or professors to add custom experiments into Room.
 */
@Composable
private fun AddExperimentDialog(
  onDismiss: () -> Unit,
  onConfirm: (String, String, String, String, String, Int, String, String) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var domain by remember { mutableStateOf("CMOS") }
  var desc by remember { mutableStateOf("") }
  var formula by remember { mutableStateOf("") }
  var diff by remember { mutableStateOf("Intermediate") }
  var duration by remember { mutableStateOf("30") }
  var objectives by remember { mutableStateOf("") }
  var industry by remember { mutableStateOf("") }

  val domainOptions = listOf("CMOS", "Digital Logic", "Layout Design", "Wafer Fab", "Silicon Test")

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Add Custom VLSI Lab Experiment", color = TextPrimary, fontWeight = FontWeight.Bold) },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Experiment Title") },
          modifier = Modifier.fillMaxWidth().testTag("add_exp_title_input")
        )

        // Domain Selector
        Text("Domain Category", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          items(domainOptions) { opt ->
            val isSel = domain == opt
            FilterChip(
              selected = isSel,
              onClick = { domain = opt },
              label = { Text(opt, fontSize = 11.sp) }
            )
          }
        }

        OutlinedTextField(
          value = desc,
          onValueChange = { desc = it },
          label = { Text("Short Description") },
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = formula,
          onValueChange = { formula = it },
          label = { Text("Theoretical Formula (e.g. V_th = ...)") },
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = industry,
          onValueChange = { industry = it },
          label = { Text("Industry Application (e.g. TSMC / Apple)") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (title.isNotBlank()) {
            val dur = duration.toIntOrNull() ?: 30
            onConfirm(title, domain, desc, formula, diff, dur, objectives, industry)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
      ) {
        Text("Save Experiment to Room", color = Color.Black, fontWeight = FontWeight.Bold)
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

// -------------------------------------------------------------------
// Helper Navigation & Color Mapping Functions
// -------------------------------------------------------------------
private fun launchLabForExperiment(experiment: VlsiExperimentEntity, viewModel: SiliconLabViewModel) {
  viewModel.selectTab(1) // Tab 1: Labs
  when (experiment.domain) {
    "CMOS" -> viewModel.selectDomain(DomainType.ANALOG)
    "Digital Logic" -> viewModel.selectDomain(DomainType.DIGITAL)
    "Layout Design" -> viewModel.selectDomain(DomainType.PHYSICAL_DESIGN)
    "Wafer Fab" -> viewModel.selectDomain(DomainType.FABRICATION)
    "Silicon Test" -> viewModel.selectDomain(DomainType.TEST_VALIDATION)
    else -> viewModel.selectDomain(DomainType.CATALOG)
  }
}

private fun launchWorkbenchForDomain(domain: String, viewModel: SiliconLabViewModel) {
  viewModel.selectTab(1) // Tab 1: Labs
  when (domain) {
    "CMOS" -> viewModel.selectDomain(DomainType.ANALOG)
    "Digital Logic" -> viewModel.selectDomain(DomainType.DIGITAL)
    "Layout Design" -> viewModel.selectDomain(DomainType.PHYSICAL_DESIGN)
    "Wafer Fab" -> viewModel.selectDomain(DomainType.FABRICATION)
    "Silicon Test" -> viewModel.selectDomain(DomainType.TEST_VALIDATION)
    else -> viewModel.selectDomain(DomainType.CATALOG)
  }
}

private fun getDomainColor(domain: String): Color {
  return when (domain) {
    "CMOS" -> ConductorGold
    "Digital Logic" -> NeonCyan
    "Layout Design" -> Color(0xFFA855F7)
    "Wafer Fab" -> CleanroomAmber
    "Silicon Test" -> LogicHighGreen
    else -> NeonCyanSubtle
  }
}

private fun getDomainIcon(domain: String): ImageVector {
  return when (domain) {
    "CMOS" -> Icons.Default.ShowChart
    "Digital Logic" -> Icons.Default.Memory
    "Layout Design" -> Icons.Default.GridOn
    "Wafer Fab" -> Icons.Default.Science
    "Silicon Test" -> Icons.Default.Speed
    else -> Icons.Default.Storage
  }
}

private fun getDomainFullName(domain: String): String {
  return when (domain) {
    "CMOS" -> "CMOS & Analog Physics"
    "Digital Logic" -> "Digital Logic & Timing Closure"
    "Layout Design" -> "Physical Design & Layout"
    "Wafer Fab" -> "Wafer Fab Cleanroom"
    "Silicon Test" -> "Silicon Test & DFT Screening"
    else -> domain
  }
}

private fun getDomainBadge(domain: String): String {
  return when (domain) {
    "CMOS" -> "Sub-0.8V VTC & Noise Margins"
    "Digital Logic" -> "STA Slack & Glitch-Free Clock Gating"
    "Layout Design" -> "DRC, Power Grid Mesh & Floorplan"
    "Wafer Fab" -> "ISO-3 Microfabrication & Chemical Safety"
    "Silicon Test" -> "ATE Shmoo, Iddq Leakage & Scan Chains"
    else -> "Virtual Semiconductor Lab"
  }
}
