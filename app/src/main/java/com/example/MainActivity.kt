package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.SiliconAppBar
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.SiliconLabViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      SiliconForgeTheme {
        SiliconForgeApp()
      }
    }
  }
}

@Composable
fun SiliconForgeApp(
  viewModel: SiliconLabViewModel = viewModel()
) {
  val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
  val safetyState by viewModel.safetyState.collectAsStateWithLifecycle()

  var showSafetyDialog by remember { mutableStateOf(false) }

  // Support Android Back button navigation when on secondary screens
  BackHandler(enabled = selectedTab != 0) {
    viewModel.selectTab(0)
  }

  Scaffold(
    topBar = {
      SiliconAppBar(
        safetyState = safetyState,
        onSafetyClick = { showSafetyDialog = true }
      )
    },
    bottomBar = {
      NavigationBar(
        containerColor = SiliconSurfaceDark,
        tonalElevation = 8.dp,
        modifier = Modifier.testTag("main_navigation_bar")
      ) {
        NavigationBarItem(
          selected = selectedTab == 0,
          onClick = { viewModel.selectTab(0) },
          icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
          label = { Text("Home", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color.Black,
            selectedTextColor = NeonCyan,
            indicatorColor = NeonCyan,
            unselectedIconColor = TextSecondary,
            unselectedTextColor = TextSecondary
          ),
          modifier = Modifier.testTag("nav_home")
        )

        NavigationBarItem(
          selected = selectedTab == 1,
          onClick = { viewModel.selectTab(1) },
          icon = { Icon(Icons.Default.Science, contentDescription = "Labs") },
          label = { Text("Labs", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color.Black,
            selectedTextColor = NeonCyan,
            indicatorColor = NeonCyan,
            unselectedIconColor = TextSecondary,
            unselectedTextColor = TextSecondary
          ),
          modifier = Modifier.testTag("nav_labs")
        )

        NavigationBarItem(
          selected = selectedTab == 2,
          onClick = { viewModel.selectTab(2) },
          icon = { Icon(Icons.Default.Speed, contentDescription = "Industry Testing") },
          label = { Text("Testing", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color.Black,
            selectedTextColor = NeonCyan,
            indicatorColor = NeonCyan,
            unselectedIconColor = TextSecondary,
            unselectedTextColor = TextSecondary
          ),
          modifier = Modifier.testTag("nav_testing")
        )

        NavigationBarItem(
          selected = selectedTab == 3,
          onClick = { viewModel.selectTab(3) },
          icon = { Icon(Icons.Default.RecordVoiceOver, contentDescription = "Interview Prep") },
          label = { Text("Interview", fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal) },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color.Black,
            selectedTextColor = NeonCyan,
            indicatorColor = NeonCyan,
            unselectedIconColor = TextSecondary,
            unselectedTextColor = TextSecondary
          ),
          modifier = Modifier.testTag("nav_interview")
        )

        NavigationBarItem(
          selected = selectedTab == 4,
          onClick = { viewModel.selectTab(4) },
          icon = { Icon(Icons.Default.Tune, contentDescription = "Virtual Bench") },
          label = { Text("Bench", fontWeight = if (selectedTab == 4) FontWeight.Bold else FontWeight.Normal) },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color.Black,
            selectedTextColor = NeonCyan,
            indicatorColor = NeonCyan,
            unselectedIconColor = TextSecondary,
            unselectedTextColor = TextSecondary
          ),
          modifier = Modifier.testTag("nav_bench")
        )
      }
    },
    modifier = Modifier.fillMaxSize()
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .background(SiliconDark)
    ) {
      when (selectedTab) {
        0 -> HomeScreen(viewModel = viewModel)
        1 -> LabsScreen(viewModel = viewModel)
        2 -> IndustryTestingScreen(viewModel = viewModel)
        3 -> InterviewPrepScreen(viewModel = viewModel)
        4 -> VirtualBenchScreen(viewModel = viewModel)
      }
    }
  }

  if (showSafetyDialog) {
    CleanroomSafetyDialog(
      safety = safetyState,
      onDismiss = { showSafetyDialog = false },
      onOpenBenchSafety = {
        showSafetyDialog = false
        viewModel.selectTab(4)
      }
    )
  }
}

@Composable
private fun CleanroomSafetyDialog(
  safety: com.example.model.CleanroomSafetyState,
  onDismiss: () -> Unit,
  onOpenBenchSafety: () -> Unit
) {
  val isAllSafe = safety.esdStrapGroundOk && safety.silaneScrubberActive && safety.hfAcidSpillKitVerified

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(
          imageVector = if (isAllSafe) Icons.Default.Shield else Icons.Default.Warning,
          contentDescription = null,
          tint = if (isAllSafe) LogicHighGreen else SiliconErrorRed
        )
        Text(
          text = if (isAllSafe) "Cleanroom Environment Secure" else "Safety Interlock Hazard",
          color = TextPrimary,
          fontWeight = FontWeight.Bold
        )
      }
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "Cleanroom Classification: ${safety.cleanroomIsoClass}",
          style = MaterialTheme.typography.labelSmall,
          color = ConductorGold,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "• ESD Wrist Ground: ${if (safety.esdStrapGroundOk) "VERIFIED (<1 MΩ to Earth)" else "DISCONNECTED (Danger)"}",
          style = MaterialTheme.typography.bodySmall,
          color = if (safety.esdStrapGroundOk) LogicHighGreen else SiliconErrorRed
        )
        Text(
          text = "• Silane Scrubber: ${if (safety.silaneScrubberActive) "ENGAGED (<0.05 ppm)" else "OFFLINE (Fire Hazard)"}",
          style = MaterialTheme.typography.bodySmall,
          color = if (safety.silaneScrubberActive) LogicHighGreen else SiliconErrorRed
        )
        Text(
          text = "• HF Spill Neutralizer: ${if (safety.hfAcidSpillKitVerified) "READY (Calcium Gluconate)" else "UNVERIFIED"}",
          style = MaterialTheme.typography.bodySmall,
          color = if (safety.hfAcidSpillKitVerified) LogicHighGreen else SiliconErrorRed
        )
      }
    },
    confirmButton = {
      Button(
        onClick = onOpenBenchSafety,
        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
      ) {
        Text("Go to Safety Bench", color = Color.Black, fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Dismiss", color = TextSecondary)
      }
    },
    containerColor = SiliconSurfaceDark
  )
}
