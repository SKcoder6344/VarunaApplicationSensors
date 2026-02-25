package com.varuna.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.varuna.app.adapters.AlertAdapter
import com.varuna.app.databinding.FragmentAlertsBinding
import com.varuna.app.models.AlertModel
import java.text.SimpleDateFormat
import java.util.Locale

class AlertsFragment : Fragment() {

    private var _binding: FragmentAlertsBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val alertList = mutableListOf<AlertModel>()
    private lateinit var adapter: AlertAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlertsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setupRecyclerView()
        loadAlerts()
        setupFilters()
    }

    private fun setupRecyclerView() {
        adapter = AlertAdapter(alertList)
        binding.rvAlerts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAlerts.adapter = adapter
    }

    private fun loadAlerts() {
        binding.progressBar.visibility = View.VISIBLE
        val uid = auth.currentUser?.uid ?: return

        db.collection("alerts")
            .whereEqualTo("userId", uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .get()
            .addOnSuccessListener { docs ->
                binding.progressBar.visibility = View.GONE
                alertList.clear()

                if (docs.isEmpty) {
                    binding.tvNoAlerts.visibility = View.VISIBLE
                    binding.rvAlerts.visibility = View.GONE
                    return@addOnSuccessListener
                }

                binding.tvNoAlerts.visibility = View.GONE
                binding.rvAlerts.visibility = View.VISIBLE

                for (doc in docs) {
                    alertList.add(
                        AlertModel(
                            id = doc.id,
                            type = doc.getString("type") ?: "Alert",
                            message = doc.getString("message") ?: "",
                            village = doc.getString("village") ?: "Unknown",
                            severity = doc.getString("severity") ?: "Low",
                            timestamp = doc.getLong("timestamp") ?: 0L,
                            isRead = doc.getBoolean("isRead") ?: false
                        )
                    )
                }
                adapter.notifyDataSetChanged()
                updateAlertCount()
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                binding.tvNoAlerts.visibility = View.VISIBLE
                binding.tvNoAlerts.text = "Error loading alerts"
            }
    }

    private fun setupFilters() {
        binding.chipAll.setOnClickListener { filterAlerts("all") }
        binding.chipWaterQuality.setOnClickListener { filterAlerts("water_quality") }
        binding.chipDiseaseRisk.setOnClickListener { filterAlerts("disease_risk") }
        binding.chipEmergency.setOnClickListener { filterAlerts("emergency") }
    }

    private fun filterAlerts(type: String) {
        val filtered = if (type == "all") {
            alertList
        } else {
            alertList.filter { it.type.lowercase().replace(" ", "_") == type }
        }
        adapter.updateData(filtered)
    }

    private fun updateAlertCount() {
        val unread = alertList.count { !it.isRead }
        val high = alertList.count { it.severity == "High" }
        binding.tvAlertSummary.text = "Total: ${alertList.size} | Unread: $unread | High Priority: $high"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
