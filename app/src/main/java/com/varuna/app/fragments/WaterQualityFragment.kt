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
import com.varuna.app.databinding.FragmentWaterQualityBinding
import com.varuna.app.ml.WaterQualityPredictor
import com.varuna.app.models.WaterQualityResult
import com.varuna.app.utils.AlertManager
import com.varuna.app.utils.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WaterQualityFragment : Fragment() {

    private var _binding: FragmentWaterQualityBinding? = null
    private val binding get() = _binding!!

    private lateinit var predictor: WaterQualityPredictor
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWaterQualityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        predictor = WaterQualityPredictor(requireContext())

        setupUI()
    }

    private fun setupUI() {
        binding.btnPredict.setOnClickListener {
            if (validateInputs()) {
                performPrediction()
            }
        }

        binding.btnClearForm.setOnClickListener {
            clearForm()
        }

        // Show WHO/BIS reference values on tap
        binding.btnShowStandards.setOnClickListener {
            showStandardsDialog()
        }
    }

    private fun validateInputs(): Boolean {
        val fields = listOf(
            binding.etPh to "pH",
            binding.etTds to "TDS (mg/L)",
            binding.etTurbidity to "Turbidity (NTU)",
            binding.etHardness to "Hardness (mg/L)",
            binding.etTemperature to "Temperature (°C)",
            binding.etChloride to "Chloride (mg/L)",
            binding.etDo to "Dissolved Oxygen (mg/L)"
        )

        for ((field, name) in fields) {
            if (field.text.toString().isEmpty()) {
                field.error = "$name is required"
                field.requestFocus()
                return false
            }
            val value = field.text.toString().toDoubleOrNull()
            if (value == null) {
                field.error = "Enter a valid number"
                return false
            }
        }
        return true
    }

    private fun performPrediction() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnPredict.isEnabled = false
        binding.cardResult.visibility = View.GONE

        val ph = binding.etPh.text.toString().toDouble()
        val tds = binding.etTds.text.toString().toDouble()
        val turbidity = binding.etTurbidity.text.toString().toDouble()
        val hardness = binding.etHardness.text.toString().toDouble()
        val temperature = binding.etTemperature.text.toString().toDouble()
        val chloride = binding.etChloride.text.toString().toDouble()
        val dissolvedOxygen = binding.etDo.text.toString().toDouble()
        val villageName = binding.etVillage.text.toString().trim().ifEmpty { "Unknown Village" }

        lifecycleScope.launch(Dispatchers.IO) {
            // ML prediction
            val wqiScore = predictor.predictWQI(ph, tds, turbidity, hardness, temperature, chloride, dissolvedOxygen)
            val classification = classifyWQI(wqiScore)
            val purificationSuggestions = getPurificationSuggestions(ph, tds, turbidity, hardness)
            val emergencyGuidelines = getEmergencyGuidelines(classification)
            val whoComplianceIssues = getWHOComplianceIssues(ph, tds, turbidity, hardness, chloride, dissolvedOxygen)

            val result = WaterQualityResult(
                wqiScore = wqiScore,
                classification = classification,
                ph = ph,
                tds = tds,
                turbidity = turbidity,
                hardness = hardness,
                temperature = temperature,
                chloride = chloride,
                dissolvedOxygen = dissolvedOxygen,
                villageName = villageName,
                purificationSuggestions = purificationSuggestions,
                emergencyGuidelines = emergencyGuidelines,
                whoComplianceIssues = whoComplianceIssues,
                timestamp = System.currentTimeMillis()
            )

            withContext(Dispatchers.Main) {
                binding.progressBar.visibility = View.GONE
                binding.btnPredict.isEnabled = true
                displayResults(result)
                saveResultToFirestore(result)

                // Trigger alert if unsafe or moderate
                if (classification == "Unsafe" || classification == "Moderate") {
                    AlertManager.triggerWaterQualityAlert(requireContext(), result)
                    NotificationHelper.sendWaterQualityAlert(requireContext(), villageName, classification, wqiScore)
                }
            }
        }
    }

    private fun classifyWQI(score: Double): String {
        return when {
            score >= 75 -> "Safe"
            score >= 50 -> "Moderate"
            else -> "Unsafe"
        }
    }

    private fun getPurificationSuggestions(ph: Double, tds: Double, turbidity: Double, hardness: Double): List<String> {
        val suggestions = mutableListOf<String>()

        if (turbidity > 4) suggestions.add("⚠️ High Turbidity → Use Filtration (Sand/Membrane) + Sedimentation for 24hr")
        if (tds > 500) suggestions.add("⚠️ High TDS → Use Reverse Osmosis (RO) or Distillation Treatment")
        if (ph < 6.5) suggestions.add("⚠️ Acidic Water (pH ${String.format("%.1f", ph)}) → Lime Treatment / pH Correction")
        if (ph > 8.5) suggestions.add("⚠️ Alkaline Water (pH ${String.format("%.1f", ph)}) → Neutralization with CO₂ / Acid Treatment")
        if (ph < 6.0 || turbidity > 10) suggestions.add("🦠 Possible Microbial Risk → Boiling for 10+ min + Chlorination (0.2 mg/L)")
        if (hardness > 300) suggestions.add("⚠️ High Hardness → Ion Exchange Softening Treatment Required")

        if (suggestions.isEmpty()) suggestions.add("✅ Water parameters are within acceptable ranges")
        return suggestions
    }

    private fun getEmergencyGuidelines(classification: String): List<String> {
        return if (classification == "Unsafe") {
            listOf(
                "🚫 Do NOT drink water directly",
                "💧 Use boiled water (boil for at least 10 minutes)",
                "🛒 Use bottled/packaged water if available",
                "📢 Immediately inform local water authority / Gram Panchayat",
                "🔬 Schedule immediate re-testing of water source",
                "👨‍⚕️ Seek medical help if symptoms appear"
            )
        } else if (classification == "Moderate") {
            listOf(
                "⚠️ Avoid drinking without treatment",
                "💧 Boil water before drinking",
                "🧪 Apply purification (RO / UV / Chlorination)",
                "📋 Report to local authority for monitoring",
                "🔁 Re-test water within 7 days"
            )
        } else {
            listOf("✅ Water appears safe. Continue regular monitoring.")
        }
    }

    private fun getWHOComplianceIssues(ph: Double, tds: Double, turbidity: Double, hardness: Double, chloride: Double, do_val: Double): List<String> {
        val issues = mutableListOf<String>()
        if (ph !in 6.5..8.5) issues.add("pH: ${String.format("%.1f",ph)} (WHO: 6.5-8.5)")
        if (tds > 500) issues.add("TDS: ${String.format("%.0f",tds)} mg/L (WHO/BIS: ≤500 mg/L)")
        if (turbidity > 4) issues.add("Turbidity: ${String.format("%.1f",turbidity)} NTU (WHO: ≤4 NTU)")
        if (hardness > 300) issues.add("Hardness: ${String.format("%.0f",hardness)} mg/L (BIS: ≤300 mg/L)")
        if (chloride > 250) issues.add("Chloride: ${String.format("%.0f",chloride)} mg/L (WHO: ≤250 mg/L)")
        if (do_val < 5) issues.add("DO: ${String.format("%.1f",do_val)} mg/L (Low - should be ≥5 mg/L)")
        return issues
    }

    private fun displayResults(result: WaterQualityResult) {
        binding.cardResult.visibility = View.VISIBLE

        binding.tvWqiScore.text = String.format("%.1f", result.wqiScore)
        binding.tvClassification.text = result.classification

        // Color coding
        val (bgColor, textColor) = when (result.classification) {
            "Safe" -> Pair(0xFFE8F5E9.toInt(), 0xFF2E7D32.toInt())
            "Moderate" -> Pair(0xFFFFF8E1.toInt(), 0xFFF57F17.toInt())
            else -> Pair(0xFFFFEBEE.toInt(), 0xFFB71C1C.toInt())
        }
        binding.cardClassification.setCardBackgroundColor(bgColor)
        binding.tvClassification.setTextColor(textColor)

        // WHO Compliance
        if (result.whoComplianceIssues.isNotEmpty()) {
            binding.tvWhoIssues.text = "⚠️ WHO/BIS Violations:\n" + result.whoComplianceIssues.joinToString("\n• ", "• ")
            binding.tvWhoIssues.visibility = View.VISIBLE
        } else {
            binding.tvWhoIssues.text = "✅ All parameters within WHO/BIS guidelines"
            binding.tvWhoIssues.visibility = View.VISIBLE
        }

        // Purification suggestions
        binding.tvPurificationSuggestions.text = result.purificationSuggestions.joinToString("\n")

        // Emergency guidelines
        binding.tvEmergencyGuidelines.text = result.emergencyGuidelines.joinToString("\n")

        // Show/hide emergency section
        binding.cardEmergency.visibility =
            if (result.classification != "Safe") View.VISIBLE else View.GONE
    }

    private fun saveResultToFirestore(result: WaterQualityResult) {
        val uid = auth.currentUser?.uid ?: return
        val data = hashMapOf(
            "userId" to uid,
            "villageName" to result.villageName,
            "wqiScore" to result.wqiScore,
            "classification" to result.classification,
            "ph" to result.ph,
            "tds" to result.tds,
            "turbidity" to result.turbidity,
            "hardness" to result.hardness,
            "temperature" to result.temperature,
            "chloride" to result.chloride,
            "dissolvedOxygen" to result.dissolvedOxygen,
            "purificationSuggestions" to result.purificationSuggestions,
            "emergencyGuidelines" to result.emergencyGuidelines,
            "whoComplianceIssues" to result.whoComplianceIssues,
            "timestamp" to result.timestamp
        )
        db.collection("water_quality_results").add(data)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Result saved to cloud ✓", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to save result", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearForm() {
        listOf(
            binding.etPh, binding.etTds, binding.etTurbidity,
            binding.etHardness, binding.etTemperature,
            binding.etChloride, binding.etDo, binding.etVillage
        ).forEach { it.text?.clear() }
        binding.cardResult.visibility = View.GONE
    }

    private fun showStandardsDialog() {
        val msg = """
WHO / BIS Water Quality Standards:
• pH: 6.5 – 8.5
• TDS: ≤ 500 mg/L (BIS: ≤ 500 mg/L)
• Turbidity: ≤ 4 NTU (BIS: ≤ 5 NTU)
• Hardness: ≤ 200 mg/L (BIS: ≤ 300 mg/L)
• Chloride: ≤ 250 mg/L
• Dissolved Oxygen: ≥ 5 mg/L
• Temperature: 10–30°C (ideal)
        """.trimIndent()

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("WHO + BIS Standards")
            .setMessage(msg)
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
