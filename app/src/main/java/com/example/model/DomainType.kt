package com.example.model

enum class DomainType(val title: String, val subtitle: String, val iconName: String) {
  CUSTOM_HDL("My RTL Code", "Write & Simulate Custom Verilog & Netlists", "Code"),
  CUSTOM_CHIP("My Nanometer Chip", "Design Custom Nanometer Chips (180nm - 2nm)", "DeveloperBoard"),
  DIGITAL("Digital & RTL", "Logic Gates, Waveforms & Timing Slack", "Memory"),
  ANALOG("Analog & CMOS", "CMOS Inverter VTC & Op-Amp Frequency Response", "ShowChart"),
  FABRICATION("Wafer Fab & Cleanroom", "8-Step Silicon Microfabrication & Safety", "Science"),
  PHYSICAL_DESIGN("Physical Design", "Floorplanning, Power Mesh & DRC Verification", "GridOn"),
  TEST_VALIDATION("ATE & Silicon Test", "Iddq, ATPG Scan Chains, Schmoo & Wafer Yield", "Speed"),
  CATALOG("Lab Catalog (Room)", "Persistent Database across CMOS, Digital & Layout", "Storage")
}

