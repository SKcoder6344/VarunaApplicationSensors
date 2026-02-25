package com.varuna.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.varuna.app.databinding.FragmentDiseaseRiskBinding
import com.varuna.app.ml.WaterQualityPredictor
import com.varuna.app.models.DiseaseRiskResult
import com.varuna.app.utils.AlertManager
import com.varuna.app.utils.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DiseaseRiskFragment : Fragment() {

    private var _binding: FragmentDiseaseRiskBinding? = null
    private val binding get() = _binding!!

    private lateinit var predictor: WaterQualityPredictor
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiseaseRiskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        predictor = WaterQualityPredictor(requireContext())

        binding.btnPredictRisk.setOnClickListener {
            if (validateInputs()) performDiseaseRiskPrediction()
        }

        binding.btnLoadFromLatest.setOnClickListener {
            loadLatestWaterQualityData()
        }
    }

    private fun validateInputs(): Boolean {
        val fields = listOf(
            binding.etPh to "pH",
            binding.etTds to "TDS",
            binding.etTurbidity to "Turbidity",
            binding.etTemperature to "Temperature",
            binding.etHealthCases to "Health Cases Count"
        )
        for ((field, name) in fields) {
            if (field.text.toString().isEmpty()) {
                field.error = "$name is required"
                return false
            }
        }
        return true
    }

    private fun performDiseaseRiskPrediction() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnPredictRisk.isEnabled = false

        val ph = binding.etPh.text.toString().toDouble()
        val tds = binding.etTds.text.toString().toDouble()
        val turbidity = binding.etTurbidity.text.toString().toDouble()
        val temperature = binding.etTemperature.text.toString().toDouble()
        val healthCases = binding.etHealthCases.text.toString().toInt()
        val villageName = binding.etVillage.text.toString().trim().ifEmpty { "Unknown" }

        lifecycleScope.launch(Dispatchers.IO) {
            val riskMap = predictor.predictDiseaseRisk(ph, tds, turbidity, temperature)

            // Factor in reported health cases
            val adjustedRiskMap = adjustRiskWithHealthCases(riskMap, healthCases)

            val result = DiseaseRiskResult(
                villageName = villageName,
                choleraRisk = adjustedRiskMap["Cholera"] ?: "Low",
                typhoidRisk = adjustedRiskMap["Typhoid"] ?: "Low",
                diarrheaRisk = adjustedRiskMap["Diarrhea"] ?: "Low",
                healthCasesReported = healthCases,
                ph = ph,
                tds = tds,
                turbidity = turbidity,
                temperature = temperature,
                preventionGuidelines = getPreventionGuidelines(adjustedRiskMap),
                timestamp = System.currentTimeMillis()
            )

            val isHighRisk = adjustedRiskMap.values.any { it == "High" }

            withContext(Dispatchers.Main) {
                binding.progressBar.visibility = View.GONE
                binding.btnPredictRisk.isEnabled = true
                displayRiskResults(result)
                saveResultToFirestore(result)

                if (isHighRisk) {
                    AlertManager.triggerDiseaseRiskAlert(requireContext(), result)
                    NotificationHelper.sendDiseaseRiskAlert(requireContext(), villageName, adjustedRiskMap)
                }
            }
        }
    }

    private fun adjustRiskWithHealthCases(riskMap: Map<String, String>, healthCases: Int): Map<String, String> {
        if (healthCases <= 0) return riskMap

        val adjusted = riskMap.toMutableMap()
        // If many cases reported, escalate risk levels
        val escalationFactor = when {
            healthCases > 20 -> 2  // Escalate by 2 levels
            healthCases > 5 -> 1   // Escalate by 1 level
            else -> 0
        }

        for (key in adjusted.keys) {
            adjusted[key] = escalateRisk(adjusted[key] ?: "Low", escalationFactor)
        }
        return adjusted
    }

    private fun escalateRisk(current: String, levels: Int): String {
        val riskLevels = listOf("Low", "Medium", "High")
        val currentIndex = riskLevels.indexOf(current)
        val newIndex = (currentIndex + levels).coerceAtMost(riskLevels.size - 1)
        return riskLevels[newIndex]
    }

    private fun getPreventionGuidelines(riskMap: Map<String, String>): List<String> {
        val guidelines = mutableListOf<String>()
        val hasHighRisk = riskMap.values.any { it == "High" }
        val hasMediumRisk = riskMap.values.any { it == "Medium" }

        if (hasHighRisk || hasMediumRisk) {
            guidelines.addAll(listOf(
                "🧼 Wash hands thoroughly with soap before eating and after toilet use",
                "💊 Keep ORS (Oral Rehydration Solution) available in the household",
                "🚰 Avoid drinking from open/contaminated water sources",
                "🏥 Seek immediate medical attention if symptoms (diarrhea, fever, vomiting) appear",
                "🍳 Eat only fully cooked food; avoid raw/street food",
                "🪣 Store drinking water in clean, covered containers"
            ))
        }

        if (riskMap["Cholera"] == "High") {
            guidelines.add("🚨 CHOLERA ALERT: Contact District Health Officer immediately")
        }
        if (riskMap["Typhoid"] == "High") {
            guidelines.add("🚨 TYPHOID ALERT: Ensure water is boiled/treated before all use")
        }

        if (guidelines.isEmpty()) {
            guidelines.add("✅ Risk levels are low. Continue maintaining good hygiene.")
        }

        return guidelines
    }

    private fun displayRiskResults(result: DiseaseRiskResult) {
        binding.cardRiskResult.visibility = View.VISIBLE

        // Cholera
        binding.tvCholeraRisk.text = result.choleraRisk
        binding.tvCholeraRisk.setTextColor(getRiskColor(result.choleraRisk))

        // Typhoid
        binding.tvTyphoidRisk.text = result.typhoidRisk
        binding.tvTyphoidRisk.setTextColor(getRiskColor(result.typhoidRisk))

        // Diarrhea
        binding.tvDiarrheaRisk.text = result.diarrheaRisk
        binding.tvDiarrheaRisk.setTextColor(getRiskColor(result.diarrheaRisk))

        // Prevention guidelines
        binding.tvPreventionGuidelines.text = result.preventionGuidelines.joinToString("\n")

        // Emergency alert banner
        val hasHighRisk = listOf(result.choleraRisk, result.typhoidRisk, result.diarrheaRisk)
            .any { it == "High" }
        binding.bannerHighRisk.visibility = if (hasHighRisk) View.VISIBLE else View.GONE
    }

    private fun getRiskColor(risk: String): Int {
        return when (risk) {
            "High" -> 0xFFB71C1C.toInt()
            "Medium" -> 0xFFF57F17.toInt()
            else -> 0xFF2E7D32.toInt()
        }
    }

    private fun saveResultToFirestore(result: DiseaseRiskResult) {
        val uid = auth.currentUser?.uid ?: return
        val data = hashMapOf(
            "userId" to uid,
            "villageName" to result.villageName,
            "choleraRisk" to result.choleraRisk,
            "typhoidRisk" to result.typhoidRisk,
            "diarrheaRisk" to result.diarrheaRisk,
            "healthCasesReported" to result.healthCasesReported,
            "ph" to result.ph,
            "tds" to result.tds,
            "turbidity" to result.turbidity,
            "temperature" to result.temperature,
            "preventionGuidelines" to result.preventionGuidelines,
            "timestamp" to result.timestamp
        )
        db.collection("disease_risk_results").add(data)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Risk data saved ✓", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadLatestWaterQualityData() {
        val uid = auth.currentUser?.uid ?: return
        binding.progressBar.visibility = View.VISIBLE

        db.collection("water_quality_results")
            .whereEqualTo("userId", uid)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { docs ->
                binding.progressBar.visibility = View.GONE
                if (!docs.isEmpty) {
                    val doc = docs.documents[0]
                    binding.etPh.setText(doc.getDouble("ph")?.toString() ?: "")
                    binding.etTds.setText(doc.getDouble("tds")?.toString() ?: "")
                    binding.etTurbidity.setText(doc.getDouble("turbidity")?.toString() ?: "")
                    binding.etTemperature.setText(doc.getDouble("temperature")?.toString() ?: "")
                    binding.etVillage.setText(doc.getString("villageName") ?: "")
                    Toast.makeText(requireContext(), "Latest water data loaded!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "No previous water quality data found", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
