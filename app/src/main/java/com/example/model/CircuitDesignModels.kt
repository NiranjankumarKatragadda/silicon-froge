package com.example.model

enum class LogicGateType(
  val displayName: String,
  val symbol: String,
  val inputCount: Int,
  val baseDelayPs: Float,
  val inputCapacitanceFf: Float
) {
  AND("AND Gate", "AND", 2, 85f, 12f),
  OR("OR Gate", "OR", 2, 90f, 14f),
  NAND("NAND Gate", "NAND", 2, 60f, 10f),
  NOR("NOR Gate", "NOR", 2, 95f, 16f),
  XOR("XOR Gate", "XOR", 2, 130f, 22f),
  XNOR("XNOR Gate", "XNOR", 2, 135f, 24f),
  NOT("Inverter (NOT)", "NOT", 1, 45f, 8f),
  BUFFER("Buffer Cell", "BUF", 1, 55f, 10f),
  MUX21("2:1 Multiplexer", "MUX", 3, 110f, 18f),
  D_LATCH("Transparent D Latch", "DLAT", 2, 120f, 20f),
  HALF_ADDER("Half Adder Block", "HA", 2, 140f, 25f),
  FULL_ADDER("Full Adder Block", "FA", 3, 210f, 35f);

  fun evaluate(inputs: List<Boolean>): Map<String, Boolean> {
    return when (this) {
      AND -> mapOf("Y" to ((inputs.getOrNull(0) ?: false) && (inputs.getOrNull(1) ?: false)))
      OR -> mapOf("Y" to ((inputs.getOrNull(0) ?: false) || (inputs.getOrNull(1) ?: false)))
      NAND -> mapOf("Y" to !((inputs.getOrNull(0) ?: false) && (inputs.getOrNull(1) ?: false)))
      NOR -> mapOf("Y" to !((inputs.getOrNull(0) ?: false) || (inputs.getOrNull(1) ?: false)))
      XOR -> mapOf("Y" to ((inputs.getOrNull(0) ?: false) xor (inputs.getOrNull(1) ?: false)))
      XNOR -> mapOf("Y" to !((inputs.getOrNull(0) ?: false) xor (inputs.getOrNull(1) ?: false)))
      NOT -> mapOf("Y" to !(inputs.getOrNull(0) ?: false))
      BUFFER -> mapOf("Y" to (inputs.getOrNull(0) ?: false))
      MUX21 -> {
        val d0 = inputs.getOrNull(0) ?: false
        val d1 = inputs.getOrNull(1) ?: false
        val sel = inputs.getOrNull(2) ?: false
        mapOf("Y" to if (sel) d1 else d0)
      }
      D_LATCH -> {
        val d = inputs.getOrNull(0) ?: false
        val en = inputs.getOrNull(1) ?: false
        mapOf("Q" to d, "QN" to !d)
      }
      HALF_ADDER -> {
        val a = inputs.getOrNull(0) ?: false
        val b = inputs.getOrNull(1) ?: false
        mapOf("SUM" to (a xor b), "COUT" to (a && b))
      }
      FULL_ADDER -> {
        val a = inputs.getOrNull(0) ?: false
        val b = inputs.getOrNull(1) ?: false
        val cin = inputs.getOrNull(2) ?: false
        val sum = (a xor b) xor cin
        val cout = (a && b) || (cin && (a xor b))
        mapOf("SUM" to sum, "COUT" to cout)
      }
    }
  }

  fun generateTruthTable(): List<TruthTableRow> {
    val rows = mutableListOf<TruthTableRow>()
    val totalCombinations = 1 shl inputCount
    for (i in 0 until totalCombinations) {
      val inList = mutableListOf<Boolean>()
      for (b in (inputCount - 1) downTo 0) {
        inList.add(((i shr b) and 1) == 1)
      }
      val outMap = evaluate(inList)
      val inLabels = when (this) {
        NOT, BUFFER -> listOf("A=${if (inList[0]) 1 else 0}")
        MUX21 -> listOf("D0=${if (inList[0]) 1 else 0}", "D1=${if (inList[1]) 1 else 0}", "S=${if (inList[2]) 1 else 0}")
        FULL_ADDER -> listOf("A=${if (inList[0]) 1 else 0}", "B=${if (inList[1]) 1 else 0}", "Cin=${if (inList[2]) 1 else 0}")
        D_LATCH -> listOf("D=${if (inList[0]) 1 else 0}", "EN=${if (inList[1]) 1 else 0}")
        else -> listOf("A=${if (inList[0]) 1 else 0}", "B=${if (inList[1]) 1 else 0}")
      }
      val outLabels = outMap.map { "${it.key}=${if (it.value) 1 else 0}" }
      rows.add(TruthTableRow(inList, inLabels, outMap, outLabels))
    }
    return rows
  }
}

data class TruthTableRow(
  val inputValues: List<Boolean>,
  val inputLabels: List<String>,
  val outputValues: Map<String, Boolean>,
  val outputLabels: List<String>
)

data class CircuitGateInstance(
  val id: String,
  val gateType: LogicGateType,
  val inputNets: List<String>,
  val outputNets: List<String>,
  val posX: Float = 0.5f,
  val posY: Float = 0.5f
)

data class CircuitDesignModule(
  val id: String,
  val name: String,
  val category: String,
  val description: String,
  val primaryInputs: List<String>,
  val primaryOutputs: List<String>,
  val gates: List<CircuitGateInstance>,
  val schematicAscii: String,
  val verilogImplementation: String
) {
  fun simulate(inputs: Map<String, Boolean>): Map<String, Boolean> {
    val netStates = inputs.toMutableMap()
    // Multi-pass propagation to settle combinational logic
    for (pass in 0 until 4) {
      for (gate in gates) {
        val gateInVals = gate.inputNets.map { netStates[it] ?: false }
        val gateOuts = gate.gateType.evaluate(gateInVals)
        for ((idx, outNet) in gate.outputNets.withIndex()) {
          val outVal = gateOuts.values.toList().getOrNull(idx) ?: false
          netStates[outNet] = outVal
        }
      }
    }
    return netStates
  }
}
