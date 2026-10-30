package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mock_job_applications")
data class JobApplicationEntity(
  @PrimaryKey
  val id: String,
  val jobId: String,
  val jobTitle: String,
  val company: String,
  val location: String,
  val domain: String, // e.g. "Physical Design", "Digital RTL", "Analog IC", "ATE & Silicon Test", "AI Hardware"
  val applicantName: String,
  val applicantEmail: String,
  val degreeLevel: String, // "B.S. Electrical / ECE", "M.S. VLSI Design", "Ph.D. Microelectronics"
  val gpaOrGrade: String,
  val portfolioSummary: String, // e.g. "Completed 3nm Tapeout, 4-bit ALU Verilog & STA Slack signoff"
  val targetFabNode: String, // e.g. "3nm GAAFET", "28nm FD-SOI"
  val atsMatchScore: Int, // 0 - 100%
  val status: String, // "Submitted", "ATS Screen Passed", "Technical Assessment", "Interview Offered", "Accepted"
  val submissionTimestamp: Long = System.currentTimeMillis(),
  val recruiterFeedback: String = ""
)
