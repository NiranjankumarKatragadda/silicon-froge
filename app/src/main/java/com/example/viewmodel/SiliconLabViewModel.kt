package com.example.viewmodel

import androidx.lifecycle.ViewModel
import com.example.data.SiliconLabRepository
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SiliconLabViewModel : ViewModel() {

  // Current navigation tab: 0=Home, 1=Labs, 2=Industry Jobs & Testing, 3=Interview Prep, 4=Virtual Bench
  private val _selectedTab = MutableStateFlow(0)
  val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

  // Selected domain in Labs screen (DIGITAL, ANALOG, FABRICATION, PHYSICAL_DESIGN, TEST_VALIDATION)
  private val _selectedDomain = MutableStateFlow(DomainType.DIGITAL)
  val selectedDomain: StateFlow<DomainType> = _selectedDomain.asStateFlow()

  // -------------------------------------------------------------
  // DOMAIN 1: DIGITAL & TIMING
  // -------------------------------------------------------------
  private val _selectedCircuit = MutableStateFlow(SiliconLabRepository.digitalCircuits.first())
  val selectedCircuit: StateFlow<DigitalCircuit> = _selectedCircuit.asStateFlow()

  private val _clockFreqMhz = MutableStateFlow(1250f)
  val clockFreqMhz: StateFlow<Float> = _clockFreqMhz.asStateFlow()

  private val _tCombNs = MutableStateFlow(0.48f)
  val tCombNs: StateFlow<Float> = _tCombNs.asStateFlow()

  private val _clockSkewNs = MutableStateFlow(0.04f)
  val clockSkewNs: StateFlow<Float> = _clockSkewNs.asStateFlow()

  private val _timingResult = MutableStateFlow(
    SiliconLabRepository.computeTimingSlack(1250f, tCombNs = 0.48f, clockSkewNs = 0.04f)
  )
  val timingResult: StateFlow<TimingAnalysisResult> = _timingResult.asStateFlow()

  // -------------------------------------------------------------
  // DOMAIN 2: ANALOG & CMOS VTC
  // -------------------------------------------------------------
  private val _cmosSpec = MutableStateFlow(CmosInverterSpec())
  val cmosSpec: StateFlow<CmosInverterSpec> = _cmosSpec.asStateFlow()

  private val _vtcAnalysis = MutableStateFlow(
    SiliconLabRepository.simulateCmosVtc(CmosInverterSpec())
  )
  val vtcAnalysis: StateFlow<CmosVtcAnalysis> = _vtcAnalysis.asStateFlow()

  // -------------------------------------------------------------
  // DOMAIN 3: FABRICATION 8-STEP CLEANROOM FLOW
  // -------------------------------------------------------------
  private val _fabSteps = MutableStateFlow(SiliconLabRepository.fabStepsList)
  val fabSteps: StateFlow<List<FabStep>> = _fabSteps.asStateFlow()

  private val _currentFabStepIndex = MutableStateFlow(0)
  val currentFabStepIndex: StateFlow<Int> = _currentFabStepIndex.asStateFlow()

  // -------------------------------------------------------------
  // DOMAIN 4: PHYSICAL DESIGN (FLOORPLAN & DRC)
  // -------------------------------------------------------------
  private val _macros = MutableStateFlow(SiliconLabRepository.getInitialMacros())
  val macros: StateFlow<List<MacroBlock>> = _macros.asStateFlow()

  private val _coreUtilPercent = MutableStateFlow(72f)
  val coreUtilPercent: StateFlow<Float> = _coreUtilPercent.asStateFlow()

  private val _powerStripeCount = MutableStateFlow(6)
  val powerStripeCount: StateFlow<Int> = _powerStripeCount.asStateFlow()

  private val _floorplanState = MutableStateFlow(
    SiliconLabRepository.evaluateFloorplan(
      SiliconLabRepository.getInitialMacros(),
      72f,
      6
    )
  )
  val floorplanState: StateFlow<FloorplanState> = _floorplanState.asStateFlow()

  // -------------------------------------------------------------
  // DOMAIN 5: TEST & SILICON VALIDATION (ATE / SCHMOO / WAFER)
  // -------------------------------------------------------------
  private val _schmooDefect = MutableStateFlow(false)
  val schmooDefect: StateFlow<Boolean> = _schmooDefect.asStateFlow()

  private val _schmooPoints = MutableStateFlow(
    SiliconLabRepository.generateSchmooPlot(false)
  )
  val schmooPoints: StateFlow<List<SchmooPoint>> = _schmooPoints.asStateFlow()

  private val _waferDies = MutableStateFlow(
    SiliconLabRepository.generateWaferDies()
  )
  val waferDies: StateFlow<List<WaferDie>> = _waferDies.asStateFlow()

  // -------------------------------------------------------------
  // INDUSTRY JOBS & TEST PROCEDURE SIMULATION
  // -------------------------------------------------------------
  val industryJobs = SiliconLabRepository.industryJobsList
  val industryProcedures = SiliconLabRepository.industrialProcedures

  private val _selectedProcedure = MutableStateFlow(SiliconLabRepository.industrialProcedures.first())
  val selectedProcedure: StateFlow<IndustryTestProcedure> = _selectedProcedure.asStateFlow()

  // Interactive Test Bench Tuning parameters
  private val _testVdd = MutableStateFlow(1.10f)
  val testVdd: StateFlow<Float> = _testVdd.asStateFlow()

  private val _testTemp = MutableStateFlow(125f)
  val testTemp: StateFlow<Float> = _testTemp.asStateFlow()

  private val _testFreq = MutableStateFlow(1.8f)
  val testFreq: StateFlow<Float> = _testFreq.asStateFlow()

  private val _appliedFix = MutableStateFlow(false)
  val appliedFix: StateFlow<Boolean> = _appliedFix.asStateFlow()

  private val _generatedReports = MutableStateFlow<List<SiliconTestReport>>(emptyList())
  val generatedReports: StateFlow<List<SiliconTestReport>> = _generatedReports.asStateFlow()

  // -------------------------------------------------------------
  // INTERVIEW PREP & DIRECT SESSIONS WITH TOP ENGINEERS
  // -------------------------------------------------------------
  val topEngineers = SiliconLabRepository.topEngineers
  val interviewQuestions = SiliconLabRepository.interviewQuestions

  private val _selectedEngineer = MutableStateFlow(SiliconLabRepository.topEngineers.first())
  val selectedEngineer: StateFlow<EngineerPersona> = _selectedEngineer.asStateFlow()

  private val _activeInterviewSession = MutableStateFlow<InterviewSessionState?>(null)
  val activeInterviewSession: StateFlow<InterviewSessionState?> = _activeInterviewSession.asStateFlow()

  // -------------------------------------------------------------
  // VIRTUAL BENCH & CLEANROOM SAFETY
  // -------------------------------------------------------------
  private val _oscilloscopeState = MutableStateFlow(OscilloscopeState())
  val oscilloscopeState: StateFlow<OscilloscopeState> = _oscilloscopeState.asStateFlow()

  private val _pmuState = MutableStateFlow(ParametricMeasurementUnit())
  val pmuState: StateFlow<ParametricMeasurementUnit> = _pmuState.asStateFlow()

  private val _safetyState = MutableStateFlow(CleanroomSafetyState())
  val safetyState: StateFlow<CleanroomSafetyState> = _safetyState.asStateFlow()

  private val _labLogs = MutableStateFlow(SiliconLabRepository.getLabLogs())
  val labLogs: StateFlow<List<LabLogEntry>> = _labLogs.asStateFlow()

  // =============================================================
  // ACTION HANDLERS
  // =============================================================
  fun selectTab(tabIndex: Int) {
    _selectedTab.value = tabIndex
  }

  fun selectDomain(domain: DomainType) {
    _selectedDomain.value = domain
  }

  // Digital actions
  fun selectCircuit(circuit: DigitalCircuit) {
    _selectedCircuit.value = circuit
    _tCombNs.value = circuit.propagationDelayNs
    recalculateTiming()
  }

  fun updateClockFreq(freqMhz: Float) {
    _clockFreqMhz.value = freqMhz
    recalculateTiming()
  }

  fun updateCombDelay(combNs: Float) {
    _tCombNs.value = combNs
    recalculateTiming()
  }

  fun updateClockSkew(skewNs: Float) {
    _clockSkewNs.value = skewNs
    recalculateTiming()
  }

  private fun recalculateTiming() {
    _timingResult.value = SiliconLabRepository.computeTimingSlack(
      clockFreqMhz = _clockFreqMhz.value,
      tCqNs = 0.22f,
      tCombNs = _tCombNs.value,
      tSetupNs = _selectedCircuit.value.minSetupTimeNs,
      tHoldNs = _selectedCircuit.value.minHoldTimeNs,
      clockSkewNs = _clockSkewNs.value
    )
  }

  // Analog CMOS actions
  fun updateWpLpRatio(ratio: Float) {
    _cmosSpec.update { it.copy(wpLpRatio = ratio) }
    _vtcAnalysis.value = SiliconLabRepository.simulateCmosVtc(_cmosSpec.value)
  }

  fun updateWnLnRatio(ratio: Float) {
    _cmosSpec.update { it.copy(wnLnRatio = ratio) }
    _vtcAnalysis.value = SiliconLabRepository.simulateCmosVtc(_cmosSpec.value)
  }

  fun updateVdd(vdd: Float) {
    _cmosSpec.update { it.copy(vdd = vdd) }
    _vtcAnalysis.value = SiliconLabRepository.simulateCmosVtc(_cmosSpec.value)
  }

  // Fabrication actions
  fun advanceFabStep() {
    val currentIndex = _currentFabStepIndex.value
    if (currentIndex < _fabSteps.value.size - 1) {
      _fabSteps.update { list ->
        list.mapIndexed { idx, step ->
          if (idx == currentIndex) step.copy(isCompleted = true) else step
        }
      }
      _currentFabStepIndex.value = currentIndex + 1
    }
  }

  fun resetFabSteps() {
    _fabSteps.value = SiliconLabRepository.fabStepsList
    _currentFabStepIndex.value = 0
  }

  // Physical Design actions
  fun updateCoreUtilization(util: Float) {
    _coreUtilPercent.value = util
    _floorplanState.value = SiliconLabRepository.evaluateFloorplan(_macros.value, util, _powerStripeCount.value)
  }

  fun updatePowerStripeCount(stripes: Int) {
    _powerStripeCount.value = stripes
    _floorplanState.value = SiliconLabRepository.evaluateFloorplan(_macros.value, _coreUtilPercent.value, stripes)
  }

  fun moveMacro(macroId: String, newX: Float, newY: Float) {
    _macros.update { list ->
      list.map { m ->
        if (m.id == macroId) m.copy(posXPercent = newX.coerceIn(0.02f, 0.65f), posYPercent = newY.coerceIn(0.02f, 0.65f)) else m
      }
    }
    _floorplanState.value = SiliconLabRepository.evaluateFloorplan(_macros.value, _coreUtilPercent.value, _powerStripeCount.value)
  }

  // Test & ATE actions
  fun toggleSchmooDefect(defect: Boolean) {
    _schmooDefect.value = defect
    _schmooPoints.value = SiliconLabRepository.generateSchmooPlot(defect)
  }

  fun retestWaferYield() {
    _waferDies.value = SiliconLabRepository.generateWaferDies()
  }

  // Industry Testing Procedures
  fun selectProcedure(proc: IndustryTestProcedure) {
    _selectedProcedure.value = proc
    _testVdd.value = proc.defaultVddCorner
    _testTemp.value = proc.defaultTempCelsius
    _testFreq.value = proc.defaultFreqGhz
    _appliedFix.value = false
  }

  fun updateTestBenchVdd(v: Float) {
    _testVdd.value = v
  }

  fun updateTestBenchTemp(t: Float) {
    _testTemp.value = t
  }

  fun updateTestBenchFreq(f: Float) {
    _testFreq.value = f
  }

  fun applyEngineeringFix() {
    _appliedFix.value = true
  }

  fun executeIndustrialTestVerification(): SiliconTestReport {
    val proc = _selectedProcedure.value
    val isFixed = _appliedFix.value
    val measuredLeakage = if (isFixed) 18.4f else 264.0f
    val achievedFmax = if (isFixed) _testFreq.value else _testFreq.value * 0.72f
    val finalYield = if (isFixed) 94.8f else 62.1f
    val isQualified = isFixed && measuredLeakage <= proc.defaultIddqLimitUa

    val report = SiliconTestReport(
      id = "REP-${System.currentTimeMillis() % 100000}",
      candidateName = "VLSI Candidate Engineer",
      jobTitle = proc.title,
      company = proc.companyTarget,
      testProcedureTitle = proc.title,
      timestamp = System.currentTimeMillis(),
      finalYieldPercent = finalYield,
      measuredIddqUa = measuredLeakage,
      achievedFrequencyGhz = achievedFmax,
      isQualified = isQualified,
      qualificationGrade = if (isQualified) "Tier-1 Certified (Grade A+)" else "Sub-Yield Margin (Grade C)",
      failureMechanismResolved = if (isFixed) proc.targetFixDescription else "Defect remains uncorrected in silicon test bench.",
      hiringRecommendation = if (isQualified) "Strong Hire: Demonstrated deep understanding of industrial ATE diagnosis, bridging fault isolation, and signoff standards." else "Needs Review: Re-evaluate timing margins and leakages before volume ramp."
    )

    _generatedReports.update { listOf(report) + it }
    return report
  }

  // Mock Interview Sessions
  fun selectEngineerPersona(persona: EngineerPersona) {
    _selectedEngineer.value = persona
    startMockInterviewSession(persona)
  }

  fun startMockInterviewSession(persona: EngineerPersona) {
    val turns = SiliconLabRepository.getMockInterviewTurns(persona)
    _activeInterviewSession.value = InterviewSessionState(
      persona = persona,
      turns = turns,
      currentTurnIndex = 0,
      selectedAnswers = mutableMapOf(),
      isFinished = false,
      totalScore = 0
    )
  }

  fun selectInterviewAnswer(turnIndex: Int, optionIndex: Int) {
    _activeInterviewSession.update { current ->
      if (current == null) return@update null
      val updatedAnswers = current.selectedAnswers.toMutableMap()
      updatedAnswers[turnIndex] = optionIndex

      val turn = current.turns[turnIndex]
      val pts = if (optionIndex == turn.bestOptionIndex) 50 else 10
      val newScore = current.totalScore + pts

      val isLast = turnIndex >= current.turns.size - 1
      current.copy(
        selectedAnswers = updatedAnswers,
        currentTurnIndex = if (isLast) turnIndex else turnIndex + 1,
        isFinished = isLast,
        totalScore = newScore
      )
    }
  }

  // Virtual Bench Controls
  fun toggleOscilloscopeRunning() {
    _oscilloscopeState.update { it.copy(isRunning = !it.isRunning) }
  }

  fun setOscilloscopeTimebase(tb: Float) {
    _oscilloscopeState.update { it.copy(timeBaseMs = tb) }
  }

  fun setOscilloscopeVoltsPerDiv(v: Float) {
    _oscilloscopeState.update { it.copy(ch1VoltsPerDiv = v, ch2VoltsPerDiv = v) }
  }

  fun setWaveformSource(source: String) {
    _oscilloscopeState.update { it.copy(waveformSource = source) }
  }

  fun toggleEsdStrap() {
    _safetyState.update { it.copy(esdStrapGroundOk = !it.esdStrapGroundOk) }
  }

  fun toggleSilaneScrubber() {
    _safetyState.update { it.copy(silaneScrubberActive = !it.silaneScrubberActive) }
  }

  fun toggleHfSpillKit() {
    _safetyState.update { it.copy(hfAcidSpillKitVerified = !it.hfAcidSpillKitVerified) }
  }

  fun addLabNotebookEntry(title: String, domain: DomainType, values: String, remarks: String) {
    val newEntry = LabLogEntry(
      id = "log_${System.currentTimeMillis()}",
      timestamp = System.currentTimeMillis(),
      domain = domain,
      experimentTitle = title,
      recordedValues = values,
      remarks = remarks
    )
    SiliconLabRepository.addLabLog(newEntry)
    _labLogs.value = SiliconLabRepository.getLabLogs()
  }
}
