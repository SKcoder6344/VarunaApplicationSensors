package com.varuna.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.varuna.app.databinding.FragmentAdminBinding
import java.text.SimpleDateFormat
import java.util.Locale

class AdminFragment : Fragment() {
    private var _binding: FragmentAdminBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadAdminStats()
        loadEmergencyAlerts()
        loadHelpRequests()
    }

    private fun loadAdminStats() {
        db.collection("water_quality_results").get().addOnSuccessListener { docs ->
            val total = docs.size()
            val unsafe = docs.count { it.getString("classification") == "Unsafe" }
            val moderate = docs.count { it.getString("classification") == "Moderate" }
            binding.tvTotalTests.text = "Total Tests: $total"
            binding.tvUnsafeCount.text = "Unsafe Cases: $unsafe"
            binding.tvModerateCount.text = "Moderate Cases: $moderate"
        }

        db.collection("users").get().addOnSuccessListener { docs ->
            binding.tvTotalUsers.text = "Registered Users: ${docs.size()}"
        }
    }

    private fun loadEmergencyAlerts() {
        db.collection("admin_alerts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { docs ->
                if (docs.isEmpty) {
                    binding.tvAdminAlerts.text = "No emergency alerts"
                    return@addOnSuccessListener
                }
                val sdf = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
                val alertsText = docs.documents.joinToString("\n\n") { doc ->
                    val type = doc.getString("type") ?: "Alert"
                    val msg = doc.getString("message") ?: ""
                    val village = doc.getString("village") ?: "Unknown"
                    val ts = doc.getLong("timestamp") ?: 0L
                    "🚨 $type\n📍 $village\n${sdf.format(ts)}\n$msg"
                }
                binding.tvAdminAlerts.text = alertsText
            }
    }

    private fun loadHelpRequests() {
        db.collection("help_requests")
            .whereEqualTo("status", "Pending")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { docs ->
                binding.tvHelpRequests.text = if (docs.isEmpty) "No pending help requests"
                else docs.documents.joinToString("\n\n") { doc ->
                    val issue = doc.getString("issue") ?: ""
                    val location = doc.getString("location") ?: "Unknown"
                    "📋 Location: $location\nIssue: $issue"
                }
            }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
