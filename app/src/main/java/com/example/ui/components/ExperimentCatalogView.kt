package com.example.ui.components

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
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CheckCircle
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.VlsiExperimentEntity
import com.example.ui.theme.*
import com.example.viewmodel.CatalogFilterMode
import com.example.viewmodel.VlsiExperimentCatalogViewModel

@Composable
fun ExperimentCatalogView(
  modifier: Modifier = Modifier,
  catalogViewModel: VlsiExperimentCatalogViewModel = viewModel()
) {
  val state by catalogViewModel.uiState.collectAsStateWithLifecycle()
  var showAddDialog by remember { mutableStateOf(false) }

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
            .background(NeonCyan.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.Storage, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
        }
        Column {
          Text(
            text = "VLSI Experiment Database (Room)",
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Categorized persistent experiments list",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            fontSize = 11.sp
          )
        }
      }

      Button(
        onClick = { showAddDialog = true },
        colors = ButtonDefaults.buttonColors(containerColor = ConductorGold),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.testTag("add_custom_experiment_button")
      ) {
        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Add Lab", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
      }
    }

    // Progress bar for completed experiments
    val completionPercent = if (state.totalExperimentsCount > 0) {
      (state.completedExperimentsCount.toFloat() / state.totalExperimentsCount.toFloat())
    } else 0f

    Surface(
      color = SiliconCardDark,
      shape = RoundedCornerShape(8.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder)
    ) {
      Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(text = "Lab Completion Progress", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
          Text(
            text = "${state.completedExperimentsCount} of ${state.totalExperimentsCount} Completed (${(completionPercent * 100).toInt()}%)",
            style = MaterialTheme.typography.labelSmall,
            color = LogicHighGreen,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
          )
        }
        LinearProgressIndicator(
          progress = { completionPercent },
          color = LogicHighGreen,
          trackColor = SiliconBorder,
          modifier = Modifier.fillMaxWidth().height(6.dp)
        )
      }
    }

    // Search Bar
    OutlinedTextField(
      value = state.searchQuery,
      onValueChange = { catalogViewModel.setSearchQuery(it) },
      placeholder = { Text("Search by title, theory formula, or industry application...", fontSize = 11.sp) },
      leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp)) },
      trailingIcon = {
        if (state.searchQuery.isNotBlank()) {
          IconButton(onClick = { catalogViewModel.setSearchQuery("") }) {
            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary, modifier = Modifier.size(16.dp))
          }
        }
      },
      singleLine = true,
      modifier = Modifier.fillMaxWidth().testTag("search_experiments_input")
    )

    // Domain Filter Chips Carousel
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
      Text(text = "Domain Categories", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
      LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(state.availableDomains) { domain ->
          val isSelected = domain.equals(state.selectedDomain, ignoreCase = true)
          Surface(
            color = if (isSelected) NeonCyan else SiliconCardDark,
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) NeonCyan else SiliconBorder),
            modifier = Modifier.clickable { catalogViewModel.selectDomain(domain) }.testTag("domain_chip_$domain")
          ) {
            Text(
              text = domain,
              color = if (isSelected) Color.Black else TextPrimary,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
          }
        }
      }
    }

    // Filter Mode Chips (All / Bookmarked / Completed)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      CatalogFilterMode.values().forEach { mode ->
        val isSel = state.filterMode == mode
        Surface(
          color = if (isSel) ConductorGold.copy(alpha = 0.25f) else SiliconCardDark,
          shape = RoundedCornerShape(6.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) ConductorGold else SiliconBorder),
          modifier = Modifier.weight(1f).clickable { catalogViewModel.setFilterMode(mode) }
        ) {
          Box(modifier = Modifier.padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
            Text(
              text = mode.label,
              color = if (isSel) ConductorGold else TextSecondary,
              style = MaterialTheme.typography.labelSmall,
              fontSize = 10.sp,
              fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
            )
          }
        }
      }
    }

    // Experiments List
    if (state.experiments.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Icon(Icons.Default.SearchOff, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(36.dp))
          Text(text = "No experiments found in ${state.selectedDomain}", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        }
      }
    } else {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        state.experiments.forEach { exp ->
          ExperimentCard(
            entity = exp,
            onToggleFavorite = { catalogViewModel.toggleFavorite(exp.id) },
            onToggleCompletion = { catalogViewModel.toggleCompletion(exp.id, exp.isCompleted) }
          )
        }
      }
    }
  }

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

@Composable
private fun ExperimentCard(
  entity: VlsiExperimentEntity,
  onToggleFavorite: () -> Unit,
  onToggleCompletion: () -> Unit
) {
  val domainColor = when (entity.domain) {
    "CMOS" -> ConductorGold
    "Digital Logic" -> NeonCyan
    "Layout Design" -> Color(0xFFA855F7)
    "Wafer Fab" -> CleanroomAmber
    "Silicon Test" -> LogicHighGreen
    else -> NeonCyanSubtle
  }

  Surface(
    color = SiliconCardDark,
    shape = RoundedCornerShape(10.dp),
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      if (entity.isCompleted) LogicHighGreen.copy(alpha = 0.5f) else SiliconBorder
    ),
    modifier = Modifier.fillMaxWidth().testTag("experiment_card_${entity.id}")
  ) {
    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          Box(
            modifier = Modifier
              .background(domainColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = entity.domain,
              color = domainColor,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp
            )
          }

          Box(
            modifier = Modifier
              .background(SiliconBorder.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = "${entity.durationMinutes} min",
              color = TextSecondary,
              style = MaterialTheme.typography.labelSmall,
              fontSize = 10.sp
            )
          }

          Box(
            modifier = Modifier
              .background(SiliconBorder.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = entity.difficulty,
              color = when (entity.difficulty) {
                "Beginner" -> LogicHighGreen
                "Intermediate" -> ConductorGold
                else -> SiliconErrorRed
              },
              style = MaterialTheme.typography.labelSmall,
              fontSize = 10.sp
            )
          }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          IconButton(onClick = onToggleFavorite, modifier = Modifier.size(28.dp)) {
            Icon(
              imageVector = if (entity.isFavorite) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
              contentDescription = "Bookmark",
              tint = if (entity.isFavorite) ConductorGold else TextSecondary,
              modifier = Modifier.size(18.dp)
            )
          }

          IconButton(onClick = onToggleCompletion, modifier = Modifier.size(28.dp)) {
            Icon(
              imageVector = if (entity.isCompleted) Icons.Default.CheckCircle else Icons.Outlined.CheckCircle,
              contentDescription = "Completion",
              tint = if (entity.isCompleted) LogicHighGreen else TextSecondary,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      Text(
        text = entity.title,
        style = MaterialTheme.typography.titleSmall,
        color = TextPrimary,
        fontWeight = FontWeight.Bold
      )

      Text(
        text = entity.description,
        style = MaterialTheme.typography.bodySmall,
        color = TextSecondary
      )

      // Formula Card
      Surface(
        color = Color(0xFF040812),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder.copy(alpha = 0.5f))
      ) {
        Text(
          text = "Formula: ${entity.theoreticalFormula}",
          color = LogicHighGreen,
          fontFamily = FontFamily.Monospace,
          fontSize = 10.sp,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        )
      }

      Text(
        text = "Industry Relevance: ${entity.industryApplication}",
        style = MaterialTheme.typography.labelSmall,
        color = ConductorGold,
        fontSize = 11.sp
      )
    }
  }
}

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

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Add Custom VLSI Lab Experiment", color = TextPrimary, fontWeight = FontWeight.Bold) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Experiment Title") },
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = domain,
          onValueChange = { domain = it },
          label = { Text("Domain (e.g. CMOS, Digital Logic, Layout Design)") },
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = desc,
          onValueChange = { desc = it },
          label = { Text("Experiment Description") },
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = formula,
          onValueChange = { formula = it },
          label = { Text("Theoretical Formula (e.g. V_drop = I * R)") },
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = industry,
          onValueChange = { industry = it },
          label = { Text("Industry Application (e.g. Apple Silicon STA)") },
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
        Text("Save to Database", color = Color.Black, fontWeight = FontWeight.Bold)
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
