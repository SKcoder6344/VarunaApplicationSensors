package com.varuna.app.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.varuna.app.databinding.FragmentDashboardBinding
import java.text.SimpleDateFormat
import java.util.Locale

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        loadDashboardData()
        setupSwipeRefresh()
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            loadDashboardData()
        }
    }

    private fun loadDashboardData() {
        binding.progressBar.visibility = View.VISIBLE
        val uid = auth.currentUser?.uid ?: return

        // Fetch latest water quality results
        db.collection("water_quality_results")
            .whereEqualTo("userId", uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { docs ->
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false

                if (docs.isEmpty) {
                    binding.tvNoData.visibility = View.VISIBLE
                    binding.cardStats.visibility = View.GONE
                    return@addOnSuccessListener
                }

                binding.tvNoData.visibility = View.GONE
                binding.cardStats.visibility = View.VISIBLE

                val latest = docs.documents[0]
                val wqi = latest.getDouble("wqiScore") ?: 0.0
                val classification = latest.getString("classification") ?: "Unknown"
                val village = latest.getString("villageName") ?: "Unknown"
                val timestamp = latest.getLong("timestamp") ?: 0L

                // Update summary cards
                updateSummaryCards(wqi, classification, village, timestamp)

                // Calculate statistics
                val allScores = docs.documents.map { it.getDouble("wqiScore") ?: 0.0 }
                val safeCount = docs.documents.count { it.getString("classification") == "Safe" }
                val moderateCount = docs.documents.count { it.getString("classification") == "Moderate" }
                val unsafeCount = docs.documents.count { it.getString("classification") == "Unsafe" }

                binding.tvSafeCount.text = safeCount.toString()
                binding.tvModerateCount.text = moderateCount.toString()
                binding.tvUnsafeCount.text = unsafeCount.toString()
                binding.tvAvgWqi.text = String.format("%.1f", allScores.average())

                // Setup WQI trend chart
                setupWqiTrendChart(docs.documents.reversed())

                // Load recent alerts
                loadRecentAlerts()

                // Load disease risk summary
                loadDiseaseRiskSummary(uid)
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
                binding.tvNoData.visibility = View.VISIBLE
                binding.tvNoData.text = "Error loading data. Pull to refresh."
            }
    }

    private fun updateSummaryCards(wqi: Double, classification: String, village: String, timestamp: Long) {
        binding.tvCurrentWqi.text = String.format("%.1f", wqi)
        binding.tvCurrentStatus.text = classification
        binding.tvCurrentVillage.text = "📍 $village"

        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        binding.tvLastUpdated.text = "Last updated: ${sdf.format(timestamp)}"

        val (bgColor, textColor) = when (classification) {
            "Safe" -> Pair(0xFF4CAF50.toInt(), Color.WHITE)
            "Moderate" -> Pair(0xFFFF9800.toInt(), Color.WHITE)
            else -> Pair(0xFFF44336.toInt(), Color.WHITE)
        }

        binding.cardCurrentStatus.setCardBackgroundColor(bgColor)
        binding.tvCurrentStatus.setTextColor(textColor)
        binding.tvCurrentWqi.setTextColor(textColor)

        // Status icon
        binding.ivStatusIcon.setImageResource(
            when (classification) {
                "Safe" -> android.R.drawable.ic_dialog_info
                "Moderate" -> android.R.drawable.ic_dialog_alert
                else -> android.R.drawable.ic_delete
            }
        )
    }

    private fun setupWqiTrendChart(documents: List<com.google.firebase.firestore.DocumentSnapshot>) {
        val entries = documents.mapIndexed { index, doc ->
            Entry(index.toFloat(), (doc.getDouble("wqiScore") ?: 0.0).toFloat())
        }

        val dataset = LineDataSet(entries, "WQI Score").apply {
            color = 0xFF1565C0.toInt()
            setCircleColor(0xFF1565C0.toInt())
            lineWidth = 2f
            circleRadius = 4f
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            fillAlpha = 80
            fillColor = 0xFF1565C0.toInt()
            setDrawFilled(true)
        }

        binding.wqiTrendChart.apply {
            data = LineData(dataset)
            description.isEnabled = false
            legend.isEnabled = true
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)

            // Safe zone reference line
            axisLeft.apply {
                axisMinimum = 0f
                axisMaximum = 100f
                addLimitLine(com.github.mikephil.charting.components.LimitLine(75f, "Safe").apply {
                    lineColor = Color.GREEN
                    lineWidth = 1f
                    textColor = Color.GREEN
                })
                addLimitLine(com.github.mikephil.charting.components.LimitLine(50f, "Moderate").apply {
                    lineColor = Color.YELLOW
                    lineWidth = 1f
                    textColor = Color.YELLOW
                })
            }
            axisRight.isEnabled = false

            animateX(1000, Easing.EaseInOutCubic)
            invalidate()
        }
    }

    private fun loadRecentAlerts() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("alerts")
            .whereEqualTo("userId", uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(3)
            .get()
            .addOnSuccessListener { docs ->
                if (docs.isEmpty) {
                    binding.tvAlertLogs.text = "No recent alerts"
                    return@addOnSuccessListener
                }
                val alertText = docs.documents.mapIndexed { i, doc ->
                    val type = doc.getString("type") ?: "Alert"
                    val msg = doc.getString("message") ?: ""
                    "${i + 1}. $type: $msg"
                }.joinToString("\n")
                binding.tvAlertLogs.text = alertText
            }
    }

    private fun loadDiseaseRiskSummary(uid: String) {
        db.collection("disease_risk_results")
            .whereEqualTo("userId", uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { docs ->
                if (docs.isEmpty) {
                    binding.tvDiseaseRiskSummary.text = "No disease risk data available"
                    return@addOnSuccessListener
                }
                val doc = docs.documents[0]
                val cholera = doc.getString("choleraRisk") ?: "N/A"
                val typhoid = doc.getString("typhoidRisk") ?: "N/A"
                val diarrhea = doc.getString("diarrheaRisk") ?: "N/A"
                binding.tvDiseaseRiskSummary.text =
                    "🦠 Cholera: $cholera  |  🦠 Typhoid: $typhoid  |  🦠 Diarrhea: $diarrhea"
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
