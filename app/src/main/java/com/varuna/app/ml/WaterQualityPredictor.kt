package com.varuna.app.ml

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.abs

/**
 * WaterQualityPredictor
 *
 * Loads a pre-trained Random Forest model from assets/varuna_model.json
 * and runs inference entirely in Kotlin — zero external ML dependency needed.
 *
 * Model trained with scikit-learn RandomForest on 2000 samples using
 * WHO/BIS water quality standards as ground truth.
 *
 * Predicts:
 *  - WQI Score (0-100)
 *  - WQI Classification: Safe / Moderate / Unsafe
 *  - Disease Risk: Cholera / Typhoid / Diarrhea → Low / Medium / High
 */
class WaterQualityPredictor(private val context: Context) {

    private var scalerMean  = DoubleArray(7)
    private var scalerScale = DoubleArray(7)
    private var wqiRegressor      : List<DecisionTree> = emptyList()
    private var wqiClassifier     : List<DecisionTree> = emptyList()
    private var choleraClassifier : List<DecisionTree> = emptyList()
    private var typhoidClassifier : List<DecisionTree> = emptyList()
    private var diarrheaClassifier: List<DecisionTree> = emptyList()
    private var isModelLoaded = false

    init { loadModel() }

    // ── Public API ────────────────────────────────────────────────────────────

    fun predictWQI(
        ph: Double, tds: Double, turbidity: Double,
        hardness: Double, temperature: Double,
        chloride: Double, dissolvedOxygen: Double
    ): Double {
        val scaled = scaleFeatures(doubleArrayOf(ph, tds, turbidity, hardness, temperature, chloride, dissolvedOxygen))
        return if (isModelLoaded && wqiRegressor.isNotEmpty())
            predictRegression(wqiRegressor, scaled).coerceIn(0.0, 100.0)
        else
            fallbackWQI(ph, tds, turbidity, hardness, temperature, chloride, dissolvedOxygen)
    }

    fun classifyWQI(wqiScore: Double): String = when {
        wqiScore >= 75 -> "Safe"
        wqiScore >= 50 -> "Moderate"
        else           -> "Unsafe"
    }

    fun classifyWQIFromParams(
        ph: Double, tds: Double, turbidity: Double,
        hardness: Double, temperature: Double,
        chloride: Double, dissolvedOxygen: Double
    ): String {
        val scaled = scaleFeatures(doubleArrayOf(ph, tds, turbidity, hardness, temperature, chloride, dissolvedOxygen))
        return if (isModelLoaded && wqiClassifier.isNotEmpty()) {
            when (predictClassification(wqiClassifier, scaled, 3)) {
                2    -> "Safe"
                1    -> "Moderate"
                else -> "Unsafe"
            }
        } else {
            classifyWQI(fallbackWQI(ph, tds, turbidity, hardness, temperature, chloride, dissolvedOxygen))
        }
    }

    fun predictDiseaseRisk(
        ph: Double, tds: Double,
        turbidity: Double, temperature: Double
    ): Map<String, String> {
        val scaled = scaleFeatures(doubleArrayOf(ph, tds, turbidity, 0.0, temperature, 0.0, 0.0))
        val labels = listOf("Low", "Medium", "High")
        return if (isModelLoaded) {
            mapOf(
                "Cholera"  to labels[predictClassification(choleraClassifier,  scaled, 3)],
                "Typhoid"  to labels[predictClassification(typhoidClassifier,   scaled, 3)],
                "Diarrhea" to labels[predictClassification(diarrheaClassifier,  scaled, 3)]
            )
        } else {
            fallbackDiseaseRisk(ph, tds, turbidity, temperature)
        }
    }

    fun isModelReady() = isModelLoaded

    // ── Model Loading ─────────────────────────────────────────────────────────

    private fun loadModel() {
        try {
            val json = context.assets.open("varuna_model.json")
                .bufferedReader().use { it.readText() }
            val root = JSONObject(json)
            val sc = root.getJSONObject("scaler")
            scalerMean  = sc.getJSONArray("mean").toDoubleArray()
            scalerScale = sc.getJSONArray("scale").toDoubleArray()
            wqiRegressor       = parseForest(root.getJSONArray("wqi_regressor"))
            wqiClassifier      = parseForest(root.getJSONArray("wqi_classifier"))
            choleraClassifier  = parseForest(root.getJSONArray("cholera_classifier"))
            typhoidClassifier  = parseForest(root.getJSONArray("typhoid_classifier"))
            diarrheaClassifier = parseForest(root.getJSONArray("diarrhea_classifier"))
            isModelLoaded = true
        } catch (e: Exception) {
            isModelLoaded = false  // Graceful fallback to math model
        }
    }

    // ── Decision Tree Engine ──────────────────────────────────────────────────

    private data class DecisionTree(
        val childrenLeft : IntArray,
        val childrenRight: IntArray,
        val feature      : IntArray,
        val threshold    : DoubleArray,
        val value        : Array<DoubleArray>
    )

    private fun parseForest(arr: JSONArray): List<DecisionTree> =
        (0 until arr.length()).map { i ->
            val t = arr.getJSONObject(i)
            DecisionTree(
                childrenLeft  = t.getJSONArray("children_left").toIntArray(),
                childrenRight = t.getJSONArray("children_right").toIntArray(),
                feature       = t.getJSONArray("feature").toIntArray(),
                threshold     = t.getJSONArray("threshold").toDoubleArray(),
                value         = t.getJSONArray("value").toDoubleArrayArray()
            )
        }

    private fun traverseTree(tree: DecisionTree, x: DoubleArray): DoubleArray {
        var node = 0
        while (tree.childrenLeft[node] != -1) {
            val f = tree.feature[node]
            node = if (f < x.size && x[f] <= tree.threshold[node])
                tree.childrenLeft[node] else tree.childrenRight[node]
        }
        return tree.value[node]
    }

    private fun predictRegression(forest: List<DecisionTree>, x: DoubleArray): Double =
        if (forest.isEmpty()) 50.0
        else forest.map { traverseTree(it, x)[0] }.average()

    private fun predictClassification(forest: List<DecisionTree>, x: DoubleArray, n: Int): Int {
        if (forest.isEmpty()) return 0
        val votes = DoubleArray(n)
        forest.forEach { tree ->
            val leaf = traverseTree(tree, x)
            val total = leaf.sum().takeIf { it > 0 } ?: 1.0
            for (c in 0 until minOf(leaf.size, n)) votes[c] += leaf[c] / total
        }
        return votes.indices.maxByOrNull { votes[it] } ?: 0
    }

    private fun scaleFeatures(x: DoubleArray): DoubleArray {
        if (!isModelLoaded) return x
        return DoubleArray(x.size) { i ->
            val m = if (i < scalerMean.size)  scalerMean[i]  else 0.0
            val s = if (i < scalerScale.size) scalerScale[i] else 1.0
            if (s != 0.0) (x[i] - m) / s else 0.0
        }
    }

    // ── Fallback Math Models ──────────────────────────────────────────────────

    private fun fallbackWQI(
        ph: Double, tds: Double, turbidity: Double, hardness: Double,
        temperature: Double, chloride: Double, dissolvedOxygen: Double
    ): Double {
        val s = doubleArrayOf(
            100.0 * (1.0 - (abs(ph - 7.0) / 1.5).coerceIn(0.0, 1.0)),
            100.0 * (1.0 - (tds / 500.0).coerceIn(0.0, 1.0)),
            100.0 * (1.0 - (turbidity / 4.0).coerceIn(0.0, 1.0)),
            100.0 * (1.0 - (hardness / 300.0).coerceIn(0.0, 1.0)),
            100.0 * (1.0 - (abs(temperature - 20.0) / 10.0).coerceIn(0.0, 1.0)),
            100.0 * (1.0 - (chloride / 250.0).coerceIn(0.0, 1.0)),
            (dissolvedOxygen / 9.0 * 100.0).coerceIn(0.0, 100.0)
        )
        val w = doubleArrayOf(0.20, 0.20, 0.15, 0.10, 0.05, 0.15, 0.15)
        return w.indices.sumOf { w[it] * s[it] }.coerceIn(0.0, 100.0)
    }

    private fun fallbackDiseaseRisk(
        ph: Double, tds: Double, turbidity: Double, temperature: Double
    ): Map<String, String> {
        fun lvl(s: Double) = when { s >= 70 -> "High"; s >= 40 -> "Medium"; else -> "Low" }
        var ch = 0.0
        if (turbidity > 10) ch += 40.0 else if (turbidity > 4) ch += 20.0
        if (temperature > 25) ch += 30.0 else if (temperature > 20) ch += 15.0
        if (ph < 6.0 || ph > 9.0) ch += 30.0 else if (ph < 6.5 || ph > 8.5) ch += 15.0
        var ty = 0.0
        if (tds > 1000) ty += 50.0 else if (tds > 500) ty += 25.0
        if (turbidity > 10) ty += 50.0 else if (turbidity > 4) ty += 25.0
        var di = 0.0
        if (turbidity > 5) di += 35.0 else if (turbidity > 2) di += 15.0
        if (temperature > 30) di += 30.0 else if (temperature > 25) di += 15.0
        if (ph < 6.5 || ph > 8.5) di += 35.0
        return mapOf("Cholera" to lvl(ch), "Typhoid" to lvl(ty), "Diarrhea" to lvl(di))
    }

    // ── JSONArray helpers ─────────────────────────────────────────────────────

    private fun JSONArray.toDoubleArray() = DoubleArray(length()) { getDouble(it) }
    private fun JSONArray.toIntArray()    = IntArray(length())    { getInt(it) }
    private fun JSONArray.toDoubleArrayArray() = Array(length()) { i ->
        val outer = getJSONArray(i)
        DoubleArray(outer.length()) { j -> outer.getJSONArray(j).getDouble(0) }
    }
}
