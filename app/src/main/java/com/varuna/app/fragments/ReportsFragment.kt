package com.varuna.app.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.varuna.app.databinding.FragmentReportsBinding
import com.varuna.app.utils.PdfReportGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var lastGeneratedPdf: File? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        loadPreviousReports()
        setupButtons()
    }

    private fun setupButtons() {
        binding.btnGenerateReport.setOnClickListener {
            generateComprehensiveReport()
        }

        binding.btnShareReport.setOnClickListener {
            lastGeneratedPdf?.let { sharePdf(it) }
                ?: Toast.makeText(requireContext(), "Generate a report first", Toast.LENGTH_SHORT).show()
        }

        binding.btnDownloadReport.setOnClickListener {
            lastGeneratedPdf?.let {
                Toast.makeText(requireContext(), "Report saved to: ${it.absolutePath}", Toast.LENGTH_LONG).show()
            } ?: Toast.makeText(requireContext(), "Generate a report first", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generateComprehensiveReport() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnGenerateReport.isEnabled = false

        val uid = auth.currentUser?.uid ?: return

        // Fetch latest water quality result
        db.collection("water_quality_results")
            .whereEqualTo("userId", uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { wqDocs ->
                db.collection("disease_risk_results")
                    .whereEqualTo("userId", uid)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .addOnSuccessListener { drDocs ->
                        lifecycleScope.launch(Dispatchers.IO) {
                            try {
                                val wqDoc = if (!wqDocs.isEmpty) wqDocs.documents[0] else null
                                val drDoc = if (!drDocs.isEmpty) drDocs.documents[0] else null

                                val pdfFile = PdfReportGenerator.generateReport(
                                    context = requireContext(),
                                    wqDoc = wqDoc,
                                    drDoc = drDoc,
                                    uid = uid
                                )

                                withContext(Dispatchers.Main) {
                                    binding.progressBar.visibility = View.GONE
                                    binding.btnGenerateReport.isEnabled = true

                                    if (pdfFile != null) {
                                        lastGeneratedPdf = pdfFile
                                        binding.tvReportGenerated.visibility = View.VISIBLE
                                        binding.tvReportGenerated.text = "✅ Report generated: ${pdfFile.name}"
                                        binding.cardReportActions.visibility = View.VISIBLE
                                        Toast.makeText(requireContext(), "PDF Report Generated!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(requireContext(), "Failed to generate report", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    binding.progressBar.visibility = View.GONE
                                    binding.btnGenerateReport.isEnabled = true
                                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
            }
    }

    private fun sharePdf(file: File) {
        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Varuna Water Quality Report")
            putExtra(Intent.EXTRA_TEXT, "Please find attached the Varuna Water Quality & Disease Risk Report.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Share Report"))
    }

    private fun loadPreviousReports() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("water_quality_results")
            .whereEqualTo("userId", uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .addOnSuccessListener { docs ->
                if (docs.isEmpty) {
                    binding.tvHistoryLabel.text = "No previous assessments found"
                    return@addOnSuccessListener
                }

                val sdf = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
                val historyText = docs.documents.joinToString("\n\n") { doc ->
                    val ts = doc.getLong("timestamp") ?: 0L
                    val village = doc.getString("villageName") ?: "Unknown"
                    val wqi = doc.getDouble("wqiScore") ?: 0.0
                    val cls = doc.getString("classification") ?: "Unknown"
                    "📋 ${sdf.format(ts)}\n📍 $village | WQI: ${String.format("%.1f", wqi)} | Status: $cls"
                }
                binding.tvReportHistory.text = historyText
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
