package com.varuna.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.varuna.app.databinding.FragmentHelpBinding

class HelpFragment : Fragment() {
    private var _binding: FragmentHelpBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHelpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSubmitRequest.setOnClickListener { submitHelpRequest() }
    }

    private fun submitHelpRequest() {
        val issue = binding.etIssueDescription.text.toString().trim()
        val location = binding.etLocation.text.toString().trim()
        if (issue.isEmpty()) { binding.etIssueDescription.error = "Please describe the issue"; return }

        val uid = auth.currentUser?.uid ?: return
        val request = hashMapOf(
            "userId" to uid,
            "issue" to issue,
            "location" to location,
            "status" to "Pending",
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("help_requests").add(request)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Help request submitted! Authorities notified.", Toast.LENGTH_LONG).show()
                binding.etIssueDescription.text?.clear()
                binding.etLocation.text?.clear()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to submit request", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
