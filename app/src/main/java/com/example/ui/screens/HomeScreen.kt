package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.model.DomainType
import com.example.ui.components.ExperimentDashboardView
import com.example.ui.theme.*
import com.example.viewmodel.SiliconLabViewModel
import com.example.viewmodel.VlsiExperimentCatalogViewModel

@Composable
fun HomeScreen(
  viewModel: SiliconLabViewModel,
  catalogViewModel: VlsiExperimentCatalogViewModel = viewModel(),
  modifier: Modifier = Modifier
) {
  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(SiliconDark)
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Hero Cleanroom Banner
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(190.dp)
          .clip(RoundedCornerShape(16.dp))
          .border(1.dp, SiliconBorder, RoundedCornerShape(16.dp))
          .testTag("home_hero_banner")
      ) {
        Image(
          painter = painterResource(id = R.drawable.hero_cleanroom_lab_1793380420525),
          contentDescription = "Semiconductor Cleanroom & ATE Lab",
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )

        // Gradient overlay
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.verticalGradient(
                colors = listOf(
                  Color.Transparent,
                  SiliconDark.copy(alpha = 0.85f),
                  SiliconDark
                )
              )
            )
        )

        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
          verticalArrangement = Arrangement.Bottom
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Box(
              modifier = Modifier
                .background(CleanroomAmber, RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = "TSMC / INTEL NODE 3nm-7nm READY",
                color = Color.Black,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp
              )
            }
            Box(
              modifier = Modifier
                .background(LogicHighGreen.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = "ATE BENCH ACTIVE",
                color = LogicHighGreen,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = "Virtual VLSI & Semiconductor Studio",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            fontWeight = FontWeight.ExtraBold
          )
          Text(
            text = "Interactive experiments across 5 silicon domains, industrial test procedures & top engineer interview sessions.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            maxLines = 2
          )
        }
      }
    }

    // Quick Action Bar
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Button(
          onClick = {
            viewModel.selectTab(1)
            viewModel.selectDomain(DomainType.DIGITAL)
          },
          colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .weight(1f)
            .testTag("launch_lab_experiments_button")
        ) {
          Icon(Icons.Default.Science, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Open Labs", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
          onClick = { viewModel.selectTab(2) },
          colors = ButtonDefaults.outlinedButtonColors(contentColor = ConductorGold),
          border = androidx.compose.foundation.BorderStroke(1.dp, ConductorGold),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .weight(1f)
            .testTag("launch_industry_tests_button")
        ) {
          Icon(Icons.Default.Speed, contentDescription = null, tint = ConductorGold, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Industry Tests", fontWeight = FontWeight.Bold)
        }
      }
    }

    // Student Custom Silicon & Code Studio Spotlight
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = SiliconSurfaceDark),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth().testTag("custom_silicon_studio_card")
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.DeveloperBoard, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "STUDENT CUSTOM SILICON STUDIO",
                color = NeonCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
            }
            Surface(
              color = NeonGreen.copy(alpha = 0.15f),
              shape = RoundedCornerShape(4.dp)
            ) {
              Text(
                text = "180nm - 2nm PDK",
                color = NeonGreen,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "Practice with Your Own Codes & Design Your Own Nanometer Chip",
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
          )

          Text(
            text = "Write and simulate custom Verilog RTL, configure 180nm down to 2nm GAAFET nodes, analyze transistor counts, power dissipation, wafer yield, and view physical die floorplans.",
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(vertical = 6.dp)
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = {
                viewModel.selectTab(1)
                viewModel.selectDomain(DomainType.CUSTOM_HDL)
              },
              colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f).testTag("btn_open_custom_code")
            ) {
              Icon(Icons.Default.Code, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("RTL Code IDE", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            OutlinedButton(
              onClick = {
                viewModel.selectTab(1)
                viewModel.selectDomain(DomainType.CUSTOM_CHIP)
              },
              colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonGreen),
              border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f).testTag("btn_open_custom_chip")
            ) {
              Icon(Icons.Default.DeveloperBoard, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Design Chip", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }
        }
      }
    }

    // Main Virtual Lab Experiments Dashboard (Room DB Categorized Sections)
    item {
      var homeExperimentsViewMode by remember { mutableStateOf(0) } // 0: Categorized Experiments (Room), 1: Core Domain Overview

      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Virtual Lab Experiments",
              style = MaterialTheme.typography.titleMedium,
              color = NeonCyan,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Categorized sections across 5 semiconductor domains",
              style = MaterialTheme.typography.labelSmall,
              color = TextSecondary,
              fontSize = 11.sp
            )
          }

          // View mode toggle
          Surface(
            color = SiliconSurfaceDark,
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder)
          ) {
            Row(modifier = Modifier.padding(2.dp)) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(if (homeExperimentsViewMode == 0) NeonCyan else Color.Transparent)
                  .clickable { homeExperimentsViewMode = 0 }
                  .padding(horizontal = 8.dp, vertical = 4.dp)
                  .testTag("tab_home_categorized")
              ) {
                Text(
                  text = "Categorized",
                  color = if (homeExperimentsViewMode == 0) Color.Black else TextSecondary,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold
                )
              }

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(if (homeExperimentsViewMode == 1) NeonCyan else Color.Transparent)
                  .clickable { homeExperimentsViewMode = 1 }
                  .padding(horizontal = 8.dp, vertical = 4.dp)
                  .testTag("tab_home_overview")
              ) {
                Text(
                  text = "Domains",
                  color = if (homeExperimentsViewMode == 1) Color.Black else TextSecondary,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }

        if (homeExperimentsViewMode == 0) {
          ExperimentDashboardView(
            labViewModel = viewModel,
            catalogViewModel = catalogViewModel
          )
        } else {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            DomainCard(
              domain = DomainType.DIGITAL,
              headline = "Digital VLSI & RTL Synthesis",
              details = "Logic gates, D-FF with clock gating, Verilog code synth, live setup/hold slack analyzer.",
              badge = "Waveform Scope",
              onClick = {
                viewModel.selectTab(1)
                viewModel.selectDomain(DomainType.DIGITAL)
              }
            )

            DomainCard(
              domain = DomainType.ANALOG,
              headline = "Analog & Mixed-Signal IC Design",
              details = "CMOS Inverter VTC (Wp/Lp tuning, VM switching threshold, NML/NMH noise margins) & Op-Amp.",
              badge = "VTC Canvas",
              onClick = {
                viewModel.selectTab(1)
                viewModel.selectDomain(DomainType.ANALOG)
              }
            )

            DomainCard(
              domain = DomainType.FABRICATION,
              headline = "Wafer Fab & Cleanroom Microfabrication",
              details = "8-Step process: Oxidation, 193nm Litho, RIE Etch, Ion Implant, Damascene Cu Metal, CMP & Safety.",
              badge = "Cleanroom ISO 3",
              onClick = {
                viewModel.selectTab(1)
                viewModel.selectDomain(DomainType.FABRICATION)
              }
            )

            DomainCard(
              domain = DomainType.PHYSICAL_DESIGN,
              headline = "Physical Design (ASIC Floorplan & DRC)",
              details = "Macro placement (SRAM, PLL, SerDes), Core utilization, Power stripe mesh & IR-drop heatmap.",
              badge = "DRC & STA",
              onClick = {
                viewModel.selectTab(1)
                viewModel.selectDomain(DomainType.PHYSICAL_DESIGN)
              }
            )

            DomainCard(
              domain = DomainType.TEST_VALIDATION,
              headline = "Silicon Test & DFT Validation",
              details = "Automated Test Equipment (ATE), Iddq quiescent leakage, ATPG scan chains, Shmoo plot & Wafer sort.",
              badge = "ATE Tester",
              onClick = {
                viewModel.selectTab(1)
                viewModel.selectDomain(DomainType.TEST_VALIDATION)
              }
            )

            DomainCard(
              domain = DomainType.CATALOG,
              headline = "VLSI Experiment Database (Room)",
              details = "Persistent lab catalog across CMOS, Digital Logic, Layout Design, Wafer Fab & Silicon Test.",
              badge = "Room DB",
              onClick = {
                viewModel.selectTab(1)
                viewModel.selectDomain(DomainType.CATALOG)
              }
            )
          }
        }
      }
    }

    // Direct Engineer Practice Spotlight Card
    item {
      Surface(
        color = SiliconCardDark,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder),
        modifier = Modifier
          .fillMaxWidth()
          .clickable { viewModel.selectTab(3) }
          .testTag("engineer_interview_spotlight_card")
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Box(
                modifier = Modifier
                  .size(32.dp)
                  .background(ConductorGold.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = ConductorGold, modifier = Modifier.size(18.dp))
              }
              Column {
                Text(
                  text = "Direct Practice Sessions",
                  style = MaterialTheme.typography.titleSmall,
                  color = TextPrimary,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "With Top Silicon Engineers",
                  style = MaterialTheme.typography.labelSmall,
                  color = ConductorGold
                )
              }
            }

            Box(
              modifier = Modifier
                .background(ConductorGold, RoundedCornerShape(20.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = "PRACTICE",
                color = Color.Black,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "Conduct real technical interview rounds with Dr. Elena Chen (Physical Design, Apple/TSMC) and Marcus Vance (Silicon Validation, NVIDIA). Receive instant critique on PVT corners, hold-time race conditions, and hiring manager recommendations.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
          )
        }
      }
    }
  }
}

@Composable
private fun DomainCard(
  domain: DomainType,
  headline: String,
  details: String,
  badge: String,
  onClick: () -> Unit
) {
  Surface(
    color = SiliconSurfaceDark,
    shape = RoundedCornerShape(12.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, SiliconBorder),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("domain_card_${domain.name.lowercase()}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Box(
        modifier = Modifier
          .size(44.dp)
          .background(
            when (domain) {
              DomainType.CUSTOM_HDL -> NeonCyan.copy(alpha = 0.15f)
              DomainType.CUSTOM_CHIP -> LogicHighGreen.copy(alpha = 0.15f)
              DomainType.DIGITAL -> NeonCyan.copy(alpha = 0.15f)
              DomainType.ANALOG -> ConductorGold.copy(alpha = 0.15f)
              DomainType.FABRICATION -> CleanroomAmber.copy(alpha = 0.15f)
              DomainType.PHYSICAL_DESIGN -> Color(0xFFA855F7).copy(alpha = 0.15f)
              DomainType.TEST_VALIDATION -> LogicHighGreen.copy(alpha = 0.15f)
              DomainType.CATALOG -> NeonCyan.copy(alpha = 0.15f)
            },
            RoundedCornerShape(10.dp)
          )
          .border(
            1.dp,
            when (domain) {
              DomainType.CUSTOM_HDL -> NeonCyan
              DomainType.CUSTOM_CHIP -> LogicHighGreen
              DomainType.DIGITAL -> NeonCyan
              DomainType.ANALOG -> ConductorGold
              DomainType.FABRICATION -> CleanroomAmber
              DomainType.PHYSICAL_DESIGN -> Color(0xFFA855F7)
              DomainType.TEST_VALIDATION -> LogicHighGreen
              DomainType.CATALOG -> NeonCyan
            },
            RoundedCornerShape(10.dp)
          ),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = when (domain) {
            DomainType.CUSTOM_HDL -> Icons.Default.Code
            DomainType.CUSTOM_CHIP -> Icons.Default.DeveloperBoard
            DomainType.DIGITAL -> Icons.Default.Memory
            DomainType.ANALOG -> Icons.Default.ShowChart
            DomainType.FABRICATION -> Icons.Default.Science
            DomainType.PHYSICAL_DESIGN -> Icons.Default.GridOn
            DomainType.TEST_VALIDATION -> Icons.Default.Speed
            DomainType.CATALOG -> Icons.Default.Storage
          },
          contentDescription = null,
          tint = when (domain) {
            DomainType.CUSTOM_HDL -> NeonCyan
            DomainType.CUSTOM_CHIP -> LogicHighGreen
            DomainType.DIGITAL -> NeonCyan
            DomainType.ANALOG -> ConductorGold
            DomainType.FABRICATION -> CleanroomAmber
            DomainType.PHYSICAL_DESIGN -> Color(0xFFA855F7)
            DomainType.TEST_VALIDATION -> LogicHighGreen
            DomainType.CATALOG -> NeonCyan
          },
          modifier = Modifier.size(24.dp)
        )
      }

      Column(modifier = Modifier.weight(1f)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = headline,
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = badge,
            style = MaterialTheme.typography.labelSmall,
            color = ConductorGold,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
          )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = details,
          style = MaterialTheme.typography.bodySmall,
          color = TextSecondary,
          fontSize = 12.sp,
          maxLines = 2
        )
      }

      Icon(
        imageVector = Icons.Default.ChevronRight,
        contentDescription = "Open",
        tint = TextTertiary,
        modifier = Modifier.size(20.dp)
      )
    }
  }
}
